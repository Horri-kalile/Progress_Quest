package controllers

import models.player.Player
import models.event.{EventFactory, EventType}
import util.RandomFunctions
import view.{GameUi, PlayerGenerationUi}
import scalafx.application.Platform

import java.util.{Timer, TimerTask}
import scalafx.animation.PauseTransition
import javafx.util.Duration
import models.monster.Monster
import scalafx.Includes.*

/**
 * Main game controller that manages the overall game flow and timing.
 *
 * GameController coordinates between the game models (player, monsters, events)
 * and the user interface, handling automatic event triggering, combat sequences,
 * and game state management. It runs the main game loop that drives all gameplay.
 */
object GameController:

  // Game state variables
  private var currentPlayer: Option[Player] = None
  private var gameTimer: Option[Timer] = None
  private var isGameRunning: Boolean = false
  private val eventInterval: Long = 3000 // 3 seconds between events
  private var eventInProgress: Boolean = false

  /**
   * Starts a new game with the given player.
   * Initializes the game loop and updates the UI.
   *
   * @param player The player to start the game with
   */
  def startGame(player: Player): Unit =
    currentPlayer = Some(player)
    isGameRunning = true
    startGameLoop()
    updateUI()

  /**
   * Stops the current game by canceling timers and resetting state.
   */
  private def stopGame(): Unit =
    isGameRunning = false
    gameTimer.foreach(_.cancel())
    gameTimer = None

  /**
   * Starts the automatic game loop that triggers events 
   * Events only trigger if the game is running
   */
  private def startGameLoop(): Unit =
    gameTimer = Some(new Timer(true))

    gameTimer.foreach(_.scheduleAtFixedRate(new TimerTask {
      override def run(): Unit =
        if (isGameRunning && !eventInProgress) then
          currentPlayer.foreach: player =>
            if player.isAlive then
              triggerRandomEvent()
            else
              handleGameOver()
    }, eventInterval, eventInterval))

  /**
   * Triggers a random event based on player's luck attribute.
   */
  private def triggerRandomEvent(): Unit =
    if eventInProgress then return
    eventInProgress = true

    currentPlayer match
      case Some(player) =>
        val eventType = RandomFunctions.getRandomEventType(player.attributes.lucky)
        if eventType == EventType.fight then
          val monster = CombatController.getRandomMonsterForZone(player.level, player.attributes.lucky, player.currentZone)

          CombatController.setLastMonster(monster)
          val fightSteps = CombatController.simulateFight(player, monster)
          val finalPlayer = fightSteps.lastOption.map(_._1).getOrElse(player)
          val finalMonster = fightSteps.lastOption.flatMap(_._2)

          // Process post-fight events (rewards, experience, etc.)
          val (updatedPlayer, postFightMessages, _) = EventFactory.executeEvent(EventType.fight, finalPlayer)
          val postFightSteps = postFightMessages.map(msg => (updatedPlayer, finalMonster, msg))

          showFightStepsSequentially(fightSteps ++ postFightSteps, updatedPlayer)

        else
          val (updatedPlayer, messages, _) = EventFactory.executeEvent(eventType, player)

          Platform.runLater {
            currentPlayer = Some(updatedPlayer)
            messages.foreach(GameUi.addEventLog)
            GameUi.updateMonsterInfo(None)
            updateUI()
            eventInProgress = false
          }

      case None =>
        eventInProgress = false

  /**
   * Displays combat steps one by one with 500ms delays for dramatic effect.
   * Updates UI after each step and handles game over conditions.
   *
   * @param steps List of combat steps (player state, monster state, message)
   * @param finalPlayer The final player state after all steps
   */
  private def showFightStepsSequentially(steps: List[(Player, Option[Monster], String)], finalPlayer: Player): Unit =
    steps match
      case Nil =>
        Platform.runLater {
          currentPlayer = Some(finalPlayer)
          GameUi.updateMonsterInfo(None)

          if !finalPlayer.isAlive then
            GameUi.showGameOver()
          else
            updateUI()
          eventInProgress = false
        }

      case (player, maybeMonster, log) :: tail =>
        Platform.runLater {
          currentPlayer = Some(player)
          GameUi.addCombatLog(log)
          GameUi.updateMonsterInfo(maybeMonster)
          updateUI()
        }

        val pause = new PauseTransition(Duration.millis(500))
        pause.setOnFinished(_ => showFightStepsSequentially(tail, finalPlayer))
        pause.play()

  /**
   * Handles game over by stopping the game loop and showing restart dialog.
   */
  private def handleGameOver(): Unit =
    stopGame()
    Platform.runLater(() =>
      GameUi.showGameOverWithRestart(() =>
        // Close current game window and open player creation
        PlayerGenerationUi.openPlayerGeneration(newPlayer => startGame(newPlayer))
      )
    )

  /**
   * Updates the UI with the current player's information.
   */
  private def updateUI(): Unit =
    currentPlayer match
      case Some(player) => GameUi.updatePlayerInfo(player)
      case None => handleGameOver()

  /**
   * Gets the current player state.
   *
   * @return Option containing the current player, or None if no game is active
   */
  def getCurrentPlayer: Option[Player] = currentPlayer

  /**
   * Checks if the game is currently running.
   *
   * @return true if game loop is active, false otherwise
   */
  def isRunning: Boolean = isGameRunning

  /**
   * Manually triggers a specific event type (mainly for testing).
   *
   * @param eventType The type of event to trigger
   */
  def triggerEvent(eventType: EventType): Unit =
    currentPlayer.foreach: player =>
      val (updatedPlayer, messages, result) = EventFactory.executeEvent(eventType, player)
      currentPlayer = Some(updatedPlayer)

      // Update monster info for combat events
      if eventType == EventType.fight then
        GameUi.updateMonsterInfo(result)
      else
        GameUi.updateMonsterInfo(None)

      updateUI()
