package view

import models.player.{EquipmentSlot, Player, Skill, SkillEffectType}
import models.world.World
import models.monster.Monster
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.*
import scalafx.scene.layout.*
import scalafx.stage.Stage
import scalafx.stage.Screen
import scalafx.Includes.jfxNode2sfx
import scalafx.scene.SceneIncludes.jfxScene2sfx

object GameUi:

  private val screenBounds = Screen.primary.visualBounds
  private val screenWidth: Double = screenBounds.width
  private val screenHeight: Double = screenBounds.height
  private var stageOpt: Option[Stage] = None
  var playerOpt: Option[Player] = None

  // Event log storage
  private var eventMessages: List[String] = List.empty
  private var combatMessages: List[String] = List.empty

  // Current monster info storage
  private var currentMonster: Option[models.monster.Monster] = None

  // Hero Diary progress bar reference
  private var heroDiaryProgressBar: Option[ProgressBar] = None

  /** Call this to open the main game UI window */
  def open(): Unit =
    val player = playerOpt.getOrElse(throw new Exception("Player not set"))
    val root = createRoot(player)
    val stage = new Stage:
      title = "ProgressQuest"
      width = screenWidth * 0.8
      height = screenHeight * 0.8
      scene = new Scene(root)
    stage.show()
    stageOpt = Some(stage)

  /** Creates the main UI root node for a given player */
  private def createRoot(player: Player): BorderPane =
    new BorderPane:
      padding = Insets(15)
      style = "-fx-background-color: #ecf0f1"
      center = new ScrollPane:
        fitToWidth = true
        fitToHeight = true
        hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        content = new HBox:
          spacing = 20
          alignment = Pos.TopLeft
          children = Seq(
            createLeftColumn(player),
            createRightColumn()
          )

  private def createLeftColumn(player: Player): VBox =
    new VBox:
      spacing = 15
      prefWidth = screenWidth * 0.5
      children = Seq(
        createPanelWithHeader("Character", "\uD83D\uDC64", new VBox(createCharacterContent(player)*)),
        createPanelWithHeader("Stats", "\uD83D\uDCCA", new VBox(createStatsContent(player)*)),
        createPanelWithHeader("World", "\uD83C\uDF0D", createWorldContent(player)),
        createPanelWithHeader("Mission List", "\uD83D\uDCDC", createMissionContent(player)),
        createPanelWithHeader("Skills", "\u2728", createSkillsContent(player)),
        createPanelWithHeader("Equipment", "\uD83D\uDEE1", new VBox(createEquipmentContent(player)*)),
        createPanelWithHeader("Inventory", "\uD83D\uDCC3", createInventoryContent(player))

      )
  private def createRightColumn(): VBox =
    new VBox:
      spacing = 15
      prefWidth = screenWidth * 0.5
      children = Seq(

        createPanelWithHeader("Hero Diary", "\uD83D\uDCD6", createHeroDiaryPanel()),
        createPanelWithHeader("Combat Log", "\u2694", createCombatLogContent()),
        createPanelWithHeader("Monster Info", "\uD83D\uDC7E", createMonsterInfoContent())
      )


  private def createSectionRow(panels: Seq[Node]): HBox =
    new HBox:
      spacing = 15
      alignment = Pos.TopLeft
      children = panels
      children.foreach: child =>
        HBox.setHgrow(child, Priority.Always)
        child.maxWidth(Double.MaxValue)

  private def createPanelWithHeader(title: String, icon: String, content: Node): VBox =
    new VBox:
      spacing = 0
      style = "-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;"
      padding = Insets(15)
      children = Seq(
        new HBox:
          spacing = 8
          alignment = Pos.CenterLeft
          children = Seq(
            new Label(icon):
              style = "-fx-font-size: 16px;",

            new Label(title):
              style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
          ),

          content
      )

  private def createCharacterContent(player: Player): Seq[Node] = Seq(
    createTableRow("Name", player.name),
    createTableRow("Race", player.identity.race.toString),
    createTableRow("Class", player.identity.classType.toString),
    createTableRow("Level", player.level.toString),
    createTableRow("Gold", player.gold.toString),
    createTableRow("Exp/NextLevel", s"${player.exp} / ${player.level * 100}")
  )

  private def createEquipmentContent(player: Player): Seq[Node] =
    EquipmentSlot.values.toSeq.map: slot =>
      val equipped = player.equipment.getOrElse(slot, None)
      val label = equipped.map(e => s"${e.name} (Value: ${e.value})").getOrElse("None")
      createTableRow(slot.toString, label)

  private def createStatWithBarRow(label: String, value: String, progressValue: Double, color: String): VBox =
    val labelRow = new HBox:
      alignment = Pos.CenterLeft
      spacing = 10
      children = Seq(
        new Label(s"$label:"):
          style = "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #34495e;"
        ,
        new Label(value):
          style = "-fx-font-size: 14px; -fx-text-fill: #34495e;"
      )

    val progressBar = new ProgressBar:
      progress = progressValue
      prefWidth = 250
      prefHeight = 20
      minHeight = 20
      maxHeight = 20
      style = s"-fx-accent: $color; -fx-control-inner-background: #dfe6e9; -fx-border-color: #b2bec3;"

    new VBox:
      spacing = 6
      padding = Insets(10, 20, 10, 20)
      children = Seq(labelRow, progressBar)

  private def createStatsContent(player: Player): Seq[Node] =
    val attrRows = Seq(
      createTableRow("STR", player.attributes.strength.toString),
      createTableRow("CON", player.attributes.constitution.toString),
      createTableRow("DEX", player.attributes.dexterity.toString),
      createTableRow("INT", player.attributes.intelligence.toString),
      createTableRow("WIS", player.attributes.wisdom.toString),
      createTableRow("LUK", player.attributes.lucky.toString)
    )

    val hpLabel = new Label(s"${player.currentHp} / ${player.hp}")
    val mpLabel = new Label(s"${player.currentMp} / ${player.mp}")

    attrRows ++ Seq(
      createStatWithBarRow("HP", s"${player.currentHp} / ${player.hp}", player.currentHp.toDouble / player.hp, "#4682b4"),
      createStatWithBarRow("MP", s"${player.currentMp} / ${player.mp}", player.currentMp.toDouble / player.mp, "#9370db")
    )


  private def createInventoryContent(player: Player): Node = new GridPane:
    hgap = 50
    vgap = 10
    padding = Insets(5)

    add(createTableHeader("Item"), 0, 0)
    add(createTableHeader("Quantity"), 1, 0)

    player.inventory.toSeq.zipWithIndex.foreach { case ((item, qty), idx) =>
      add(new Label(item.name), 0, idx + 1)
      add(new Label(qty.toString), 1, idx + 1)
    }

  private def createDiaryContent(): Node = new TextArea:
    text = if eventMessages.isEmpty then "Waiting for adventures..." else eventMessages.mkString("\n")
    editable = false
    prefHeight = 230
    style = "-fx-font-family: monospace; -fx-font-size: 12; -fx-background-color: transparent"
    focusTraversable = false


  private def createCombatLogContent(): Node =
    new TextArea:
      text = if combatMessages.isEmpty then "No combat yet..." else combatMessages.mkString("\n")
      editable = false
      style = "-fx-font-family: monospace; -fx-font-size: 12; -fx-background-color: transparent"
      focusTraversable = false

  private def createMonsterInfoContent(): Node =
    currentMonster match
      case Some(monster) =>
        val hpLabel = new Label(s"HP: ${monster.attributes.currentHp}/${monster.attributes.hp}")

        val hpBar = new ProgressBar:
          progress = monster.attributes.currentHp.toDouble / monster.attributes.hp
          prefWidth = 150
          style = "-fx-accent: #4682b4"

        val grid = new GridPane:
          hgap = 8
          vgap = 4
          padding = Insets(5)

        // Ligne 0 : nom et level
        grid.add(new Label(s"${monster.name} (Level ${monster.level})") {
          style = "-fx-font-weight: bold; -fx-font-size: 14;"
        }, 0, 0, 2, 1)

        // Ligne 1 : type
        grid.add(new Label(s"Type: ${monster.monsterType}"), 0, 1)
        grid.add(new Label(s"Zone: ${monster.originZone}"), 0, 2)

        // Ligne 3 : HP
        grid.add(hpLabel, 0, 3)
        grid.add(hpBar, 1, 3)

        // Ligne 4 : Attack / Physical weakness
        grid.add(new Label(s"Attack: ${monster.attributes.attack}"), 0, 4)
        grid.add(new Label(f"Physical weakness: ${monster.attributes.weaknessPhysical}%.2f"), 1, 4)

        // Ligne 5 : Defense / Magical weakness
        grid.add(new Label(s"Defense: ${monster.attributes.defense}"), 0, 5)
        grid.add(new Label(f"Magical weakness: ${monster.attributes.weaknessMagic}%.2f"), 1, 5)

        // Ligne 6 : Behavior
        grid.add(new Label(s"Behavior: ${monster.behavior}"), 0, 6, 2, 1)

        // Ligne 7 : Rewards
        grid.add(new Label(s"Gold Reward: ${monster.goldReward}"), 0, 7)
        grid.add(new Label(s"EXP Reward: ${monster.experienceReward}"), 0, 8)

        // Ligne 8 : Item reward
        grid.add(new Label(s"Item Reward: ${monster.itemReward.fold("None")(_.toString)}"), 0, 9, 2, 1)
        grid.add(new Label(s"Equipment Reward: ${monster.equipReward.fold("None")(_.toString)}"), 0,10,2, 1)

        // Ligne 10 : description
        grid.add(new Label(monster.description) {
          style = "-fx-font-size: 11; -fx-text-fill: #555555; -fx-wrap-text: true"
          maxWidth = 300
        }, 0, 11,2, 1)

        grid

      case None =>
        new Label("No monster encountered yet...") {
          style = "-fx-font-style: italic; -fx-text-fill: #888888;"
        }

  private def createWorldContent(player: Player): Node =
    new VBox:
      spacing = 10
      children = Seq(
        new Label("Current World:"):
          style = "-fx-font-weight: bold"
        ,
        new Label(player.currentZone.toString):
          style = "-fx-font-size: 14; -fx-font-weight: bold"
        ,
        new Label(World.getZoneDescription(player.currentZone)):
          style = "-fx-font-size: 12; -fx-text-fill: #666666; -fx-wrap-text: true"
          maxWidth = 200
      )

  private def createSkillsContent(player: Player): Node =
    val physicalSkills = player.skills.filter(_.effectType == SkillEffectType.Physical)
    val magicSkills = player.skills.filter(_.effectType == SkillEffectType.Magic)
    val healingSkills = player.skills.filter(_.effectType == SkillEffectType.Healing)

    def formatSkill(skill: Skill): Label =
      new Label(s"${skill.name} (Lv.${skill.powerLevel}, MP: ${skill.manaCost})"):
        style = "-fx-font-size: 11"

    def makeColumn(title: String, skills: List[Skill]) =
      new VBox:
        spacing = 5
        alignment = Pos.TopLeft
        children = Seq(
          new Label(title):
            style = "-fx-font-weight: bold; -fx-underline: true"
        ) ++ skills.map(formatSkill)

    new HBox:
      spacing = 40
      alignment = Pos.TopLeft
      children = Seq(
        makeColumn("Physical", physicalSkills),
        makeColumn("Magic", magicSkills),
        makeColumn("Healing", healingSkills)
      )

  private def createMissionContent(player: Player): Node = new VBox:
    spacing = 5
    children =
      if player.activeMissions.isEmpty then
        Seq(
          new Label("No missions accepted"):
            style = "-fx-font-weight: bold"
        )
      else
        val missionLabels = player.activeMissions.map: mission =>
          new HBox:
            spacing = 10
            children = Seq(
              new Label(s"• ${mission.name}"):
                style = "-fx-font-size: 12; -fx-font-weight: bold"
              ,
              new Label(mission.description):
                style = "-fx-font-size: 11; -fx-text-fill: #555555"
              ,
              new Label(f"${mission.progression}/${mission.goal}"):
                style = "-fx-font-size: 11; -fx-text-fill: #888888"
            )

        Seq(
          new Label(s"Current Missions: ${player.activeMissions.size}"):
            style = "-fx-font-weight: bold"
        ) ++ missionLabels


  private def createTableRow(label: String, value: String): HBox =
    val labelNode = new Label(label):
      style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;"
      maxWidth = Double.MaxValue

    val valueNode = new Label(value):
      style = "-fx-font-size: 14px; -fx-text-fill: #2c3e50;"
      maxWidth = Double.MaxValue
      alignment = Pos.CenterRight

    HBox.setHgrow(labelNode, Priority.Always)
    HBox.setHgrow(valueNode, Priority.Always)

    new HBox:
      spacing = 40
      alignment = Pos.CenterLeft
      padding = Insets(10, 20, 10, 20)
      style =
        "-fx-background-color: white;" +
          "-fx-border-color: transparent;"
      children = Seq(labelNode, valueNode)

  private def createTableHeader(text: String): Label = new Label(text):
    style = "-fx-font-weight: bold; -fx-underline: true"

  /**
   * Update the UI with current player information
   * This method should be called from GameController
   */
  def updatePlayerInfo(player: Player): Unit =
    playerOpt = Some(player) // update stored player reference
    stageOpt.foreach: stage =>
      val newRoot = createRoot(player) // rebuild UI with updated player
      stage.scene().root = newRoot // replace root node
    println(s"UI Update: ${player.name} - Level ${player.level} - HP: ${player.currentHp}/${player.hp}")

  /**
   * Add a message to the combat log
   */
  def addCombatLog(message: String): Unit =
    combatMessages = (combatMessages :+ message).takeRight(20) // Keep last 20 messages
    updateCurrentUI()

  /**
   * Add a message to the event diary
   */
  def addEventLog(message: String): Unit =
    eventMessages = (eventMessages :+ message).takeRight(30) // Keep last 30 messages

    // Update UI first to get fresh progress bar reference
    updateCurrentUI()

    // Then animate the progress bar after a small delay
    val animationTimer = new java.util.Timer()
    animationTimer.schedule(new java.util.TimerTask():
      override def run(): Unit =
        animateHeroDiaryProgress()
        animationTimer.cancel()
      , 100) // Small delay to ensure UI is fully updated

  /**
   * Update the current monster info
   */
  def updateMonsterInfo(monster: Option[Monster]): Unit =
    currentMonster = monster
    updateCurrentUI()

  def showGameOverWithRestart(onRestart: () => Unit): Unit =
    // Create a simple game over window with restart option
    val gameOverStage = new Stage:
      title = "Game Over"
      width = 400
      height = 200

    val gameOverScene = new Scene:
      root = new VBox:
        spacing = 20
        padding = Insets(20)
        alignment = Pos.Center
        children = Seq(
          new Label("Game Over!"):
            style = "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #d32f2f"
          ,
          new Label("Your hero has fallen..."):
            style = "-fx-font-size: 16px; -fx-text-fill: #666666"
          ,
          new HBox:
            spacing = 15
            alignment = Pos.Center
            children = Seq(
              new Button("Restart Game"):
                style = "-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20"
                onAction = _ =>
                  gameOverStage.close()
                  // Close current game window
                  stageOpt.foreach(_.close())
                  stageOpt = None
                  playerOpt = None
                  // Call restart callback
                  onRestart()
              ,
              new Button("Exit"):
                style = "-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20"
                onAction = _ =>
                  gameOverStage.close()
                  // Close current game window
                  stageOpt.foreach(_.close())
                  stageOpt = None
                  playerOpt = None
            )
        )

    gameOverStage.scene = gameOverScene
    gameOverStage.show()

  /**
   * Update the current UI if it's open
   */
  private def updateCurrentUI(): Unit =
    playerOpt.foreach: player =>
      stageOpt.foreach: stage =>
        val newRoot = createRoot(player)
        stage.scene().root = newRoot

  /**
   * Show game over screen
   */
  def showGameOver(): Unit =
    // TODO: Show game over dialog with restart option
    println("GAME OVER - Show restart dialog")

  private def createHeroDiaryPanel(): VBox =
    val progressBar = new ProgressBar:
      progress = 0.0
      prefWidth = 150
      style = "-fx-accent: rgb(63,147,156);"


    heroDiaryProgressBar = Some(progressBar)


    val progressSection = new HBox:
      spacing = 10
      alignment = Pos.CenterLeft
      children = Seq(
        new Label("Progress:"):
          style = "-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #34495e;"
        ,
        progressBar
      )


    val diaryContent = createDiaryContent()


    new VBox:
      spacing = 10
      padding = Insets(10)
      children = Seq(progressSection, diaryContent)





  /**
   * Animate the Hero Diary progress bar
   */
  private def animateHeroDiaryProgress(): Unit =
    heroDiaryProgressBar.foreach: progressBar =>
      // Reset to 0 and animate to 1.0
      progressBar.progress = 0.0

      // Simple animation using a thread with Platform.runLater
      val animationThread = new Thread(() =>
        try
          for i <- 0 to 10 do
            val progress = i / 10.0
            scalafx.application.Platform.runLater(() =>
              progressBar.progress = progress
            )
            Thread.sleep(100) // 100ms delay
          Thread.sleep(500) // Stay at 100% for 500ms
          scalafx.application.Platform.runLater(() =>
            progressBar.progress = 0.0 // Reset to 0
          )
        catch
          case _: InterruptedException => // Thread was interrupted
      )
      animationThread.setDaemon(true)
      animationThread.start()
