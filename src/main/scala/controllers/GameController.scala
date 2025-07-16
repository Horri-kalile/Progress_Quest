package controllers

import models.player.Player
import util.RandomFunctions
import view.{GameUi, PlayerGenerationUi}
import scalafx.application.Platform

import java.util.{Timer, TimerTask}
import scalafx.animation.PauseTransition
import javafx.util.Duration
import models.event.GameEventModule.EventType
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
   * Stop the game loop
   */
  private def stopGame(): Unit =
    isGameRunning = false
    gameTimer.foreach(_.cancel())
    gameTimer = None

  /**
   * Main game loop - triggers events automatically
   */
  private def startGameLoop(): Unit =
    gameTimer = Some(new Timer(true))

    gameTimer.foreach(_.scheduleAtFixedRate(new TimerTask {
      override def run(): Unit =
        if isGameRunning && !eventInProgress then
          currentPlayer.foreach: player =>
            if player.isAlive then
              triggerRandomEvent()
            else
              handleGameOver()
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
          val monster = MonsterController.getRandomMonsterForZone(player.level, player.attributes.lucky, player.currentZone)

          val fightSteps = CombatController.simulateFight(player, monster)
          val finalPlayer = fightSteps.lastOption.map(_._1).getOrElse(player)
          val finalMonster = fightSteps.lastOption.flatMap(_._2)
          CombatController.setLastMonster(finalMonster.get)
          println(finalMonster)
          // Post-fight check: game over or other events
          val (updatedPlayer, postFightMessages, _) = EventController.runEvent(EventType.fight, finalPlayer)
          val postFightSteps = postFightMessages.map(msg => (updatedPlayer, finalMonster, msg))

          showFightStepsSequentially(fightSteps ++ postFightSteps, updatedPlayer)

        else
          val (updatedPlayer, messages, _) = EventController.runEvent(eventType, player)

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
   * @param steps       List of combat steps (player state, monster state, message)
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
      val (updatedPlayer, messages, result) = EventController.runEvent(eventType, player)
      currentPlayer = Some(updatedPlayer)

      // Update monster info based on event type
      if eventType == EventType.fight then
        GameUi.updateMonsterInfo(result)
      else
        GameUi.updateMonsterInfo(None)

      updateUI()
