package controllers

import models.player.Player
import models.event.{EventFactory, EventType}
import util.RandomFunctions
import view.GameUi
import scalafx.application.Platform
import java.util.{Timer, TimerTask}
import scala.util.Random

/**
 * Main Game Controller - Handles the game loop and coordinates between models and views
 */
object GameController {

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
        if (isGameRunning) {
          currentPlayer.foreach { player =>
            if (PlayerController.isAlive(player)) {
              triggerRandomEvent()
            } else {
              handleGameOver()
            }
          }
        }
      }
    }, eventInterval, eventInterval))

  /**
   * Trigger a random event and update the game state
   */
  private def triggerRandomEvent(): Unit = {
    currentPlayer.foreach { player =>
      val eventType = Random.shuffle(EventType.values.toList).head
      val (updatedPlayer, messages) = EventFactory.executeEvent(eventType, player)

      currentPlayer = Some(updatedPlayer)

      // Send combat messages to UI
      Platform.runLater(() => {
        if (eventType == EventType.fight) {
          messages.foreach(GameUi.addCombatLog)
        }
        updateUI()
      })
    }
  }

  /**
   * Handle game over scenario
   */
  private def handleGameOver(): Unit = {
    stopGame()
    Platform.runLater(() => {
      // Show game over screen
      println("GAME OVER - Player has died!")
      // TODO: Show restart option in UI
    })
  }

  /**
   * Update the UI with current player state
   */
  private def updateUI(): Unit = {
    currentPlayer.foreach { player =>
      GameUi.updatePlayerInfo(player)
    }
  }

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
      val updatedPlayer = EventFactory.executeEvent(eventType, player)
      currentPlayer = Some(updatedPlayer._1)
      updateUI()
    }
}
