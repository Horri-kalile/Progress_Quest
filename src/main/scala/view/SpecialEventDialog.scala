package view

import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType}
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.application.Platform
import java.util.{Timer, TimerTask}
import java.util.concurrent.CountDownLatch
import scala.util.Random

/**
 * Dialog utility for displaying special event popups during gameplay.
 *
 * This object manages interactive dialogs that appear during random events,
 * providing players with choices that can affect their character's progress.
 * All choice dialogs include a 5-second auto-timeout mechanism that makes
 * a RANDOM CHOICE when the player doesn't respond in time.
 */
object Special:

  /**
   * Show blessing/curse dialog with 5-second auto-random timer.
   *
   * Presents the player with a mysterious shrine that could provide
   * beneficial or harmful effects when interacted with. If no choice
   * is made within 5 seconds, a random decision is automatically selected.
   *
   * @return Some(true) if player/random chooses to pray, Some(false) if ignore
   */
  def showBlessingCurseDialog(): Option[Boolean] =
    showTimedDialog(
      title = "Mysterious Shrine",
      header = "You encounter a mysterious shrine...",
      content = "Do you want to pray at the shrine?\n(Could be a blessing or a curse!)",
      yesText = "Pray",
      noText = "Ignore"
    )

  /**
   * Show powerful monster dialog with 5-second auto-random timer.
   *
   * Presents the player with a challenging combat encounter that
   * offers high risk but potentially valuable rewards. If no choice
   * is made within 5 seconds, a random decision is automatically selected.
   *
   * @return Some(true) if player/random chooses to fight, Some(false) if flee
   */
  def showPowerfulMonsterDialog(): Option[Boolean] =
    showTimedDialog(
      title = "Powerful Monster",
      header = "A powerful monster blocks your path!",
      content = "Do you want to fight it?\n(High risk, but might drop rare equipment!)",
      yesText = "Fight",
      noText = "Flee"
    )

  /**
   * Show hidden dungeon discovery dialog with 5-second auto-random timer.
   *
   * Presents the player with an exploration opportunity that could
   * contain valuable loot or dangerous traps. If no choice is made
   * within 5 seconds, a random decision is automatically selected.
   *
   * @return Some(true) if player/random chooses to explore, Some(false) if leave
   */
  def showHiddenDungeonDialog(): Option[Boolean] =
    showTimedDialog(
      title = "Hidden Dungeon",
      header = "You discovered a hidden dungeon!",
      content = "Do you want to explore it?\n(Might contain rare equipment or traps!)",
      yesText = "Explore",
      noText = "Leave"
    )

  /**
   * Show villager help request dialog with 5-second auto-random timer.
   *
   * Presents the player with an opportunity to help NPCs, potentially
   * gaining experience based on their wisdom attribute. If no choice
   * is made within 5 seconds, a random decision is automatically selected.
   *
   * @return Some(true) if player/random chooses to help, Some(false) if ignore
   */
  def showVillagerHelpDialog(): Option[Boolean] =
    showTimedDialog(
      title = "Villagers in Need",
      header = "You encounter villagers who need help!",
      content = "Do you want to help them?\n(May reward experience based on your wisdom!)",
      yesText = "Help",
      noText = "Ignore"
    )


  def showGameOverMonsterDialog(): Option[Boolean] =
    showTimedDialog(
      title = "Powerful Monster Encounter",
      header = "A deadly monster appears!",
      content = "This monster looks extremely dangerous!\nDo you want to face it or try to escape?",
      yesText = "Fight",
      noText = "Escape"
    )

  /**
   * Show game over notification for powerful monster defeat.
   *
   * Displays a notification when the player is defeated by a powerful
   * monster encounter, signaling the end of the game session.
   */
  def showGameOverMonsterDialogg(): Unit =
    showInfoDialog(
      title = "Defeated!",
      header = "💀 You were defeated by a powerful monster!",
      content = "The powerful monster was too strong for you.\n\nGame Over!"
    )


  def showGameOverTrapDialog(): Option[Boolean] =
    showTimedDialog(
      title = "Deadly Trap!",
      header = "You found a suspicious trap!",
      content = "This trap looks lethal!\nDo you want to try to disarm it or avoid it?",
      yesText = "Disarm",
      noText = "Avoid"
    )

  /**
   * Show game over notification for deadly trap.
   *
   * Displays a notification when the player is killed by a trap,
   * signaling the end of the game session.
   */
  def showGameOverTrapDialogg(): Unit =
    showInfoDialog(
      title = "Deadly Trap!",
      header = "💀 It was a trap!",
      content = "You fell into a deadly trap and were killed!\n\nGame Over!"
    )

  /**
   * Show dungeon trap notification (non-fatal).
   *
   * Displays a notification when the player triggers a trap that
   * damages but doesn't kill them, reducing HP and MP by half.
   */
  def showDungeonTrapDialog(): Option[Boolean] =
    showTimedDialog(
      title = "Dungeon Trap!",
      header = "You discovered a trap mechanism!",
      content = "There's a trap here that might hurt you.\nDo you want to trigger it or find another way?",
      yesText = "Trigger",
      noText = "Find Another Way"
    )

  /**
   * Show theft notification.
   *
   * Displays a notification when the player's inventory is affected
   * by thieves, informing them that items have been stolen.
   */
  def showTheftDialog(): Unit =
    showInfoDialog(
      title = "Thieves!",
      header = "You were robbed!",
      content = "Sneaky thieves appeared and stole some of your items!\n\nCheck your inventory to see what's missing."
    )

  /**
   * Private helper method to show timed dialog with 5-second auto-random choice.
   *
   * Creates a confirmation dialog with two choices and an automatic random selection
   * if the player doesn't respond within 5 seconds.
   *
   * @param title The dialog window title
   * @param header The main dialog header text
   * @param content The detailed dialog content/question
   * @param yesText Text for the positive action button
   * @param noText Text for the negative action button
   * @return Some(true) for yes choice, Some(false) for no choice (guaranteed non-None)
   */
  private def showTimedDialog(
                               title: String,
                               header: String,
                               content: String,
                               yesText: String,
                               noText: String
                             ): Option[Boolean] =

    // Thread-safe result storage with countdown latch for synchronization
    @volatile var dialogResult: Option[Boolean] = null
    val latch = new java.util.concurrent.CountDownLatch(1)

    // Execute dialog creation and display on JavaFX Application Thread
    Platform.runLater: () =>
      try
        // Create confirmation dialog with custom buttons
        val dialog = new Alert(AlertType.Confirmation)

        // Set dialog properties after creation to avoid type ambiguity
        dialog.title = title
        dialog.headerText = header
        dialog.contentText = s"$content\n\n⏰ Random choice in 5 seconds..."

        // Create custom button types for clearer user choices
        val yesButton = new ButtonType(yesText, ButtonData.Yes)
        val noButton = new ButtonType(noText, ButtonData.No)
        dialog.buttonTypes = Seq(yesButton, noButton)

        // Setup 5-second auto-random-choice timer
        val timer = new Timer()
        timer.schedule(new TimerTask:
          def run(): Unit =
            Platform.runLater(() =>
              // Make a random choice and close dialog
              val randomChoice = Random.nextBoolean()
              dialogResult = Some(randomChoice)
              dialog.close()
            )
          , 5000) // 5000ms = 5 seconds

        // Show dialog and wait for user response or timeout
        val result = dialog.showAndWait()
        timer.cancel() // Cancel timer if user made a choice before timeout

        // Process dialog result and convert to Option[Boolean]
        // If dialogResult was already set by timer, keep that value
        if dialogResult == null then
          dialogResult = result match
            case Some(`yesButton`) => Some(true) // Player chose positive action
            case Some(`noButton`) => Some(false) // Player chose negative action
            case Some(_) => Some(Random.nextBoolean()) // Any other button = random choice
            case None => Some(Random.nextBoolean()) // Dialog closed without choice = random choice

      finally
        // Always signal completion to unblock waiting thread
        latch.countDown()

    // Block current thread until dialog is complete (maintains event timing)
    latch.await()
    dialogResult

  /**
   * Private helper method to show information-only dialog (no choices).
   *
   * Creates a simple notification dialog that only requires acknowledgment.
   * Used for displaying game state changes, notifications, and outcomes
   * that don't require player decision-making.
   *
   * @param title The dialog window title
   * @param header The main dialog header text
   * @param content The detailed information content
   */
  private def showInfoDialog(title: String, header: String, content: String): Unit =
    // Execute on JavaFX Application Thread for UI safety
    Platform.runLater: () =>
      // Create information dialog with single OK button
      val dialog = new Alert(AlertType.Information)

      // Set dialog properties
      dialog.title = title
      dialog.headerText = header
      dialog.contentText = content

      // Create single acknowledgment button
      val okButton = new ButtonType("OK", ButtonData.OKDone)
      dialog.buttonTypes = Seq(okButton)

      // Show dialog and wait for user acknowledgment
      dialog.showAndWait()
      
      
  