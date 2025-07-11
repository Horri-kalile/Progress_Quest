package view

import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType}
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.application.Platform
import java.util.{Timer, TimerTask}
import java.util.concurrent.CountDownLatch

object SpecialEventDialog:
  
  /**
   * Show blessing/curse dialog with 5-second auto-ignore timer
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
   * Show powerful monster dialog with 5-second auto-flee timer
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

  def showHiddenDungeonDialog(): Option[Boolean] =
    showTimedDialog(
      title = "Hidden Dungeon", 
      header = "You discovered a hidden dungeon!",
      content = "Do you want to explore it?\n(Might contain rare equipment or traps!)",
      yesText = "Explore",
      noText = "Leave"
    )
  
  def showVillagerHelpDialog(): Option[Boolean] =
    showTimedDialog(
      title = "Villagers in Need",
      header = "You encounter villagers who need help!",
      content = "Do you want to help them?\n(May reward experience based on your wisdom!)",
      yesText = "Help",
      noText = "Ignore"
    )
  /**
   * Show game over notification for powerful monster defeat
   */
  def showGameOverMonsterDialog(): Unit =
    showInfoDialog(
      title = "Defeated!",
      header = "💀 You were defeated by a powerful monster!",
      content = "The powerful monster was too strong for you.\n\nGame Over!"
    )

  /**
   * Show game over notification for trap
   */
  def showGameOverTrapDialog(): Unit =
    showInfoDialog(
      title = "Deadly Trap!",
      header = "💀 It was a trap!",
      content = "You fell into a deadly trap and were killed!\n\nGame Over!"
    )

  /**
   * Show dungeon trap notification
   */
  def showDungeonTrapDialog(): Unit =
    showInfoDialog(
      title = "Dungeon Trap!",
      header = "You triggered a dangerous trap!",
      content = "You stepped on a hidden pressure plate!\n\nYour HP and MP have been halved!"
    )

  /**
   * Show theft notification
   */
  def showTheftDialog(): Unit =
    showInfoDialog(
      title = "Thieves!",
      header = "You were robbed!",
      content = "Sneaky thieves appeared and stole some of your items!\n\nCheck your inventory to see what's missing."
    )

  /**
   * Private helper method to show timed dialog with 5-second auto-close
   */
  private def showTimedDialog(
    title: String,
    header: String,
    content: String,
    yesText: String,
    noText: String
  ): Option[Boolean] =
  
    // Use a blocking approach to wait for dialog result
    @volatile var dialogResult: Option[Boolean] = null
    val latch = new java.util.concurrent.CountDownLatch(1)
    
    // Execute dialog on JavaFX Application Thread
    Platform.runLater { () =>
      try {
        val dialog = new Alert(AlertType.Confirmation)
        
        // Set properties after creation to avoid ambiguity
        dialog.title = title
        dialog.headerText = header
        dialog.contentText = s"$content\n\n⏰ Auto-ignore in 5 seconds..."

        val yesButton = new ButtonType(yesText, ButtonData.Yes)
        val noButton = new ButtonType(noText, ButtonData.No)
        dialog.buttonTypes = Seq(yesButton, noButton)

        // 5-second timer for auto-close
        val timer = new Timer()
        timer.schedule(new TimerTask {
            def run(): Unit = Platform.runLater(() => dialog.close())
          }, 5000)

        val result = dialog.showAndWait()
        timer.cancel() // Cancel timer if user made a choice

        dialogResult = result match
          case Some(`yesButton`) => Some(true) // Player chose "yes" action
          case Some(`noButton`) => Some(false) // Player chose "no" action
          case Some(_) => None // Any other button = treat as timeout
          case None => None // Timed out = auto-ignore
          
      } finally {
        latch.countDown() // Signal that dialog is done
      }
    }
    
    // Wait for dialog to complete (blocks the Timer thread)
    latch.await()
    dialogResult

  /**
   * Private helper method to show info-only dialog (no choices)
   */
  private def showInfoDialog(title: String, header: String, content: String): Unit =
    Platform.runLater { () =>
      val dialog = new Alert(AlertType.Information)
      
      dialog.title = title
      dialog.headerText = header
      dialog.contentText = content
      
      val okButton = new ButtonType("OK", ButtonData.OKDone)
      dialog.buttonTypes = Seq(okButton)
      
      dialog.showAndWait()
    }