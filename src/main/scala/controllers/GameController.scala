package controllers

import models.player.Player
import models.event.{EventFactory, EventType}
import util.RandomFunctions
import view.{GameUi, PlayerGenerationUi}
import scalafx.application.Platform
import java.util.{Timer, TimerTask}
import scala.util.Random

/**
 * Main Game Controller - Handles the game loop and coordinates between models and views
 */
object GameController:

  private var currentPlayer: Option[Player] = None
  private var gameTimer: Option[Timer] = None
  private var isGameRunning: Boolean = false
  private val eventInterval: Long = 3000 // 3 seconds between events

  /**
   * Initialize the game with a player
   */
  def startGame(player: Player): Unit =
    currentPlayer = Some(player)
    isGameRunning = true
    startGameLoop()
    updateUI()

  /**
   * Stop the game loop
   */
  def stopGame(): Unit =
    isGameRunning = false
    gameTimer.foreach(_.cancel())
    gameTimer = None

  /**
   * Main game loop - triggers events automatically
   */
  private def startGameLoop(): Unit =
    gameTimer = Some(new Timer(true))

    gameTimer.foreach(_.scheduleAtFixedRate(new TimerTask:
      override def run(): Unit =
        if isGameRunning then
          currentPlayer.foreach: player =>
            if PlayerController.isAlive(player) then
              triggerRandomEvent()
            else
              handleGameOver()
    , eventInterval, eventInterval))

  /**
   * Trigger a random event and update the game state
   */
  private def triggerRandomEvent(): Unit =
    currentPlayer.foreach: player =>
      val eventType = RandomFunctions.getRandomEventType(player.attributes.lucky)
      val (updatedPlayer, messages, result) = EventFactory.executeEvent(eventType, player)

      currentPlayer = Some(updatedPlayer)

      // Send messages to UI
      Platform.runLater(() =>
        if eventType == EventType.fight then
          messages.foreach(GameUi.addCombatLog)
          // Update monster info for fight events
          GameUi.updateMonsterInfo(result)
        else
          messages.foreach(GameUi.addEventLog)
          // Clear monster info for non-fight events
          GameUi.updateMonsterInfo(None)
        updateUI()
      )

  /**
   * Handle game over scenario
   */
  private def handleGameOver(): Unit =
    stopGame()
    Platform.runLater(() =>
      GameUi.showGameOverWithRestart(() =>
        // Close current game window
        // Open PlayerGenerationUi again
        PlayerGenerationUi.openPlayerGeneration(newPlayer => startGame(newPlayer))
      )
    )

  /**
   * Update the UI with current player state
   */
  private def updateUI(): Unit =
    currentPlayer.foreach: player =>
      GameUi.updatePlayerInfo(player)

  /**
   * Get current player state
   */
  def getCurrentPlayer: Option[Player] = currentPlayer

  /**
   * Check if game is running
   */
  def isRunning: Boolean = isGameRunning

  /**
   * Manual event trigger for testing
   */
  def triggerEvent(eventType: EventType): Unit =
    currentPlayer.foreach: player =>
      val (updatedPlayer, messages, result) = EventFactory.executeEvent(eventType, player)
      currentPlayer = Some(updatedPlayer)
      
      // Update monster info based on event type
      if eventType == EventType.fight then
        GameUi.updateMonsterInfo(result)
      else
        GameUi.updateMonsterInfo(None)
      
      updateUI()
