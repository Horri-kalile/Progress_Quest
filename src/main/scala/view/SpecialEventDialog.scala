package view

import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType}
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.application.Platform
import java.util.{Timer, TimerTask}
import java.util.concurrent.CountDownLatch

/**
 * Dialog utility for displaying special event popups during gameplay.
 * 
 * This object manages interactive dialogs that appear during random events,
 * providing players with choices that can affect their character's progress.
 * All choice dialogs include a 5-second auto-timeout mechanism to maintain
 * game flow and prevent the game from hanging if the player is away.
 * 
 * Features:
 * - Timed dialogs with automatic fallback choices
 * - Thread-safe dialog handling using JavaFX Platform
 * - Consistent styling and behavior across all event types
 * - Information-only dialogs for notifications
 */
object SpecialEventDialog:
  
  /**
   * Show blessing/curse dialog with 5-second auto-ignore timer.
   * 
   * Presents the player with a mysterious shrine that could provide
   * beneficial or harmful effects when interacted with.
   * 
   * @return Some(true) if player chooses to pray, Some(false) if ignore, None if timeout
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
   * Show powerful monster dialog with 5-second auto-flee timer.
   * 
   * Presents the player with a challenging combat encounter that
   * offers high risk but potentially valuable rewards.
   * 
   * @return Some(true) if player chooses to fight, Some(false) if flee, None if timeout
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
   * Show hidden dungeon discovery dialog with 5-second auto-leave timer.
   * 
   * Presents the player with an exploration opportunity that could
   * contain valuable loot or dangerous traps.
   * 
   * @return Some(true) if player chooses to explore, Some(false) if leave, None if timeout
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
   * Show villager help request dialog with 5-second auto-ignore timer.
   * 
   * Presents the player with an opportunity to help NPCs, potentially
   * gaining experience based on their wisdom attribute.
   * 
   * @return Some(true) if player chooses to help, Some(false) if ignore, None if timeout
   */
  def showVillagerHelpDialog(): Option[Boolean] =
    showTimedDialog(
      title = "Villagers in Need",
      header = "You encounter villagers who need help!",
      content = "Do you want to help them?\n(May reward experience based on your wisdom!)",
      yesText = "Help",
      noText = "Ignore"
    )

  /**
   * Show game over notification for powerful monster defeat.
   * 
   * Displays a notification when the player is defeated by a powerful
   * monster encounter, signaling the end of the game session.
   */
  def showGameOverMonsterDialog(): Unit =
    showInfoDialog(
      title = "Defeated!",
      header = "💀 You were defeated by a powerful monster!",
      content = "The powerful monster was too strong for you.\n\nGame Over!"
    )

  /**
   * Show game over notification for deadly trap.
   * 
   * Displays a notification when the player is killed by a trap,
   * signaling the end of the game session.
   */
  def showGameOverTrapDialog(): Unit =
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
  def showDungeonTrapDialog(): Unit =
    showInfoDialog(
      title = "Dungeon Trap!",
      header = "You triggered a dangerous trap!",
      content = "You stepped on a hidden pressure plate!\n\nYour HP and MP have been halved!"
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
   * Private helper method to show timed dialog with 5-second auto-close.
   * 
   * Creates a confirmation dialog with two choices and an automatic timeout.
   * Uses thread-safe mechanisms to handle JavaFX UI interactions from
   * background threads without blocking the UI.
   * 
   * @param title The dialog window title
   * @param header The main dialog header text
   * @param content The detailed dialog content/question
   * @param yesText Text for the positive action button
   * @param noText Text for the negative action button
   * @return Some(true) for yes choice, Some(false) for no choice, None for timeout
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
        dialog.contentText = s"$content\n\n⏰ Auto-ignore in 5 seconds..."

        // Create custom button types for clearer user choices
        val yesButton = new ButtonType(yesText, ButtonData.Yes)
        val noButton = new ButtonType(noText, ButtonData.No)
        dialog.buttonTypes = Seq(yesButton, noButton)

        // Setup 5-second auto-close timer to prevent game hanging
        val timer = new Timer()
        timer.schedule(new TimerTask:
            def run(): Unit = Platform.runLater(() => dialog.close())
          , 5000) // 5000ms = 5 seconds

        // Show dialog and wait for user response or timeout
        val result = dialog.showAndWait()
        timer.cancel() // Cancel timer if user made a choice before timeout

        // Process dialog result and convert to Option[Boolean]
        dialogResult = result match
          case Some(`yesButton`) => Some(true) // Player chose positive action
          case Some(`noButton`) => Some(false) // Player chose negative action
          case Some(_) => None // Any other button = treat as timeout
          case None => None // Dialog closed without choice = timeout
          
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