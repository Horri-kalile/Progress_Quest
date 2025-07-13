package controllers

import models.player.Player
import models.event.{EventFactory, EventType}
import util.RandomFunctions
import view.GameUi
import scalafx.application.Platform

import java.util.{Timer, TimerTask}
import scalafx.animation.PauseTransition
import javafx.util.Duration
import models.monster.Monster
import scalafx.Includes.*

/**
 * Main Game Controller - Handles the game loop and coordinates between models and views
 */
object GameController {
  @volatile private var eventInProgress: Boolean = false
  private var currentPlayer: Option[Player] = None
  private var gameTimer: Option[Timer] = None
  private var isGameRunning: Boolean = false
  private val eventInterval: Long = 3000 // 3 seconds between events

  /**
   * Initialize the game with a player
   */
  def startGame(player: Player): Unit = {
    currentPlayer = Some(player)
    isGameRunning = true
    startGameLoop()
    updateUI()
  }

  /**
   * Stop the game loop
   */
  def stopGame(): Unit = {
    isGameRunning = false
    gameTimer.foreach(_.cancel())
    gameTimer = None
  }

  /**
   * Main game loop - triggers events automatically
   */
  private def startGameLoop(): Unit =
    gameTimer = Some(new Timer(true))

    gameTimer.foreach(_.scheduleAtFixedRate(new TimerTask {
      override def run(): Unit = {
        if isGameRunning && !eventInProgress then
          currentPlayer.foreach { player =>
            if (PlayerController.isAlive(player)) {
              triggerRandomEvent()
            } else {
              handleGameOver()
            }
          }
      }
    }, eventInterval, eventInterval))

  /**
   * Trigger a random event and update the game state
   */


  private def triggerRandomEvent(): Unit =
    if eventInProgress then return

    eventInProgress = true

    currentPlayer match
      case Some(player) =>
        val eventType = RandomFunctions.getRandomEventType(player.attributes.lucky)

        if eventType == EventType.fight then
          val monster = CombatController.getRandomMonsterForZone(
            player.level,
            player.attributes.lucky,
            player.currentZone
          )

          CombatController.setLastMonster(monster)
          val fightSteps = CombatController.simulateFight(player, monster)
          val finalPlayer = fightSteps.lastOption.map(_._1).getOrElse(player)
          val finalMonster = fightSteps.lastOption.flatMap(_._2)

          // Post-fight check: game over or other events
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
        // No player loaded yet — nothing to do
        eventInProgress = false


  private def showFightStepsSequentially(steps: List[(Player, Option[Monster], String)], finalPlayer: Player): Unit =
    steps match
      case Nil =>
        Platform.runLater {
          currentPlayer = Some(finalPlayer)
          GameUi.updateMonsterInfo(None)

          if !PlayerController.isAlive(finalPlayer) then
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
   * Handle game over scenario
   */
  private def handleGameOver(): Unit = {
    stopGame()
    Platform.runLater(() => {
      // Show game over screen
      val gameOverMessage = "GAME OVER - Player has died!"
      println(gameOverMessage)
      GameUi.addEventLog(gameOverMessage)
      // TODO: Show restart option in UI
    })
  }

  /**
   * Update the UI with current player state
   */
  private def updateUI(): Unit =
    currentPlayer match
      case Some(player) => GameUi.updatePlayerInfo(player)
      case None => handleGameOver()

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
    currentPlayer.foreach { player =>
      val (updatedPlayer, messages, result) = EventFactory.executeEvent(eventType, player)
      currentPlayer = Some(updatedPlayer)

      // Update monster info based on event type
      if (eventType == EventType.fight) {
        GameUi.updateMonsterInfo(result)
      } else {
        GameUi.updateMonsterInfo(None)
      }

      updateUI()
    }
}
