package view

import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType}
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.application.Platform
import java.util.{Timer, TimerTask}

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
   * Private helper method to show timed dialog with 5-second auto-close
   */
  private def showTimedDialog(
    title: String, 
    header: String, 
    content: String, 
    yesText: String, 
    noText: String
  ): Option[Boolean] =
    val dialog = new Alert(AlertType.Confirmation):
      this.title = title
      headerText = header
      contentText = s"$content\n\n⏰ Auto-ignore in 5 seconds..."
      
    val yesButton = new ButtonType(yesText, ButtonData.YesButton)
    val noButton = new ButtonType(noText, ButtonData.NoButton)
    dialog.buttonTypes = Seq(yesButton, noButton)
    
    // 5-second timer for auto-close
    val timer = new Timer()
    timer.schedule(new TimerTask {
      def run(): Unit = Platform.runLater(() => dialog.close())
    }, 5000)
    
    val result = dialog.showAndWait()
    timer.cancel() // Cancel timer if user made a choice
    
    result match
      case Some(`yesButton`) => Some(true)   // Player chose "yes" action
      case Some(`noButton`) => Some(false)   // Player chose "no" action  
      case None => None                      // Timed out = auto-ignore

