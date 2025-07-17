package view

import models.player.{Player, Skill, SkillEffectType}
import models.world.World
import models.monster.Monster
import models.player.EquipmentModule.EquipmentSlot
import util.GameConfig.*
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.*
import scalafx.scene.layout.*
import scalafx.stage.Stage
import scalafx.stage.Screen
import scalafx.Includes.jfxNode2sfx
import scalafx.application.Platform
import scalafx.scene.SceneIncludes.jfxScene2sfx

/**
 * Main game user interface that displays all player information and game state.
 *
 * This object manages the primary game window containing character stats, equipment,
 * inventory, world information, skills, missions, combat log, and monster details.
 * The UI is organized in a grid layout with multiple panels that update in real-time
 * as the game progresses.
 *
 * Features:
 * - Real-time player stat updates (HP, MP, level, experience)
 * - Equipment and inventory management display
 * - Skills categorized by type (physical, magic, healing)
 * - Current world zone information with description
 * - Active missions with progression tracking
 * - Combat log with scrolling message history
 * - Monster information panel for current encounters
 * - Hero diary with animated progress indicators
 * - Game over screen with restart functionality
 * - Responsive layout that adapts to screen size
 */
object GameUi:

  /** Screen dimensions for responsive UI sizing */
  private val screenBounds = Screen.primary.visualBounds
  private val screenWidth: Double = screenBounds.width
  private val screenHeight: Double = screenBounds.height
  private var stageOpt: Option[Stage] = None
  var playerOpt: Option[Player] = None

  /** Event log storage - */
  private var eventMessages: List[String] = List.empty

  /** Combat log storage */
  private var combatMessages: List[String] = List.empty

  /** Current monster information for monster info panel */
  private var currentMonster: Option[Monster] = None

  /** Reference to hero diary progress bar for animations */
  private var heroDiaryProgressBar: Option[ProgressBar] = None

  /**
   * Helper function to combine multiple styles.
   *
   * @param styles Variable number of style strings to combine
   * @return Combined style string
   */
  private def combineStyles(styles: String*): String = styles.mkString("; ")

  /**
   * Helper function to create styled labels with consistent formatting.
   *
   * @param text   The label text
   * @param styles Variable number of style strings to apply
   * @return Styled Label component
   */
  private def styledLabel(text: String, styles: String*): Label =
    new Label(text):
      style = combineStyles(styles: _*)

  /**
   * Opens the main game UI window.
   *
   * Creates and displays the primary game interface with all panels
   * and information displays. The window is sized to 80% of screen dimensions.
   *
   * @throws Exception if no player has been set via playerOpt
   */
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

  /**
   * Creates the main UI root layout containing all game panels.
   *
   * Organizes the interface into three horizontal rows:
   * - Top: Character info, Equipment, Stats
   * - Center: Inventory, World info, Skills, Missions
   * - Bottom: Hero Diary, Combat Log, Monster Info
   *
   * @param player The player whose information to display
   * @return BorderPane containing the complete UI layout
   */
  private def createRoot(player: Player): BorderPane =
    new BorderPane:
      padding = Insets(15)
      style = backgroundMain

      top = createSectionRow(Seq(
        createPanelWithHeader("Character Player", new VBox:
          children = createCharacterContent(player)
        ),
        createPanelWithHeader("Equipment", new VBox:
          children = createEquipmentContent(player)
        ),
        createPanelWithHeader("Stats", new VBox:
          children = createStatsContent(player)
        )
      ))

      center = createSectionRow(Seq(
        createPanelWithHeader("Inventory", createInventoryContent(player)),
        createPanelWithHeader("World", createWorldContent(player)),
        createPanelWithHeader("Skills", createSkillsContent(player)),
        createPanelWithHeader("Mission", createMissionContent(player))
      ))

      bottom = createSectionRow(Seq(
        createHeroDiaryPanel(),
        createPanelWithHeader("Combat Log", createCombatLogContent()),
        createPanelWithHeader("Monster Info", new VBox:
          children = createMonsterInfoContent()
        )
      ))

  /**
   * Creates a horizontal row of panels with equal spacing and growth.
   *
   * @param panels Sequence of UI nodes to arrange horizontally
   * @return HBox containing the arranged panels
   */
  private def createSectionRow(panels: Seq[Node]): HBox =
    new HBox:
      spacing = 15
      alignment = Pos.TopLeft
      children = panels
      children.foreach: child =>
        HBox.setHgrow(child, Priority.Always)
        child.maxWidth(Double.MaxValue)

  /**
   * Creates a standardized panel with header and content area.
   *
   * @param title   The panel header text
   * @param content The main content node for the panel
   * @return VBox containing the styled panel with header
   */
  private def createPanelWithHeader(title: String, content: Node): VBox =
    new VBox:
      spacing = 0
      children = Seq(
        new HBox:
          style = panelHeader
          prefHeight = 30
          alignment = Pos.CenterLeft
          children = Seq(
            styledLabel(title, labelHeader, "-fx-text-fill: white")
          )
        ,
        new VBox:
          style = panelBody
          padding = Insets(10)
          children = Seq(content)
      )

  /**
   * Creates character information display showing basic player details.
   *
   * @param player The player whose character info to display
   * @return Sequence of nodes showing name, race, class, level, gold, and experience
   */
  private def createCharacterContent(player: Player): Seq[Node] = Seq(
    createTableRow("Name", player.name),
    createTableRow("Race", player.identity.race.toString),
    createTableRow("Class", player.identity.classType.toString),
    createTableRow("Level", player.level.toString),
    createTableRow("Gold", player.gold.toString),
    createTableRow("Behavior", player.behaviorType.toString),
    createTableRow("Exp/NextLevel", s"${player.exp} / ${player.level * 100}")
  )

  /**
   * Creates equipment display showing all equipped items by slot.
   *
   * @param player The player whose equipment to display
   * @return Sequence of nodes showing equipment in each slot
   */
  private def createEquipmentContent(player: Player): Seq[Node] =
    EquipmentSlot.values.toSeq.map: slot =>
      val equipped = player.equipment.getOrElse(slot, None)
      val label = equipped match
        case Some(equip) =>
          val attrs = equip.statBonus
          val bonusStats = s"STR: ${attrs.strength}, CON: ${attrs.constitution}, DEX: ${attrs.dexterity}, INT: ${attrs.intelligence}, WIS: ${attrs.wisdom}, LUCK: ${attrs.lucky}"
          s"${equip.name} (Value: ${equip.value}) | Bonus: [$bonusStats]"
        case None => "None"
      createTableRow(slot.toString, label)

  /**
   * Creates stats display showing attributes and health/mana with progress bars.
   *
   * @param player The player whose stats to display
   * @return Sequence of nodes showing all attributes plus HP/MP bars
   */
  private def createStatsContent(player: Player): Seq[Node] =
    val attrRows = Seq(
      createTableRow("STR", player.attributes.strength.toString),
      createTableRow("CON", player.attributes.constitution.toString),
      createTableRow("DEX", player.attributes.dexterity.toString),
      createTableRow("INT", player.attributes.intelligence.toString),
      createTableRow("WIS", player.attributes.wisdom.toString),
      createTableRow("LUK", player.attributes.lucky.toString)
    )

    val hpLabel = styledLabel(s"${player.currentHp} / ${player.hp}", labelHeader)
    val mpLabel = styledLabel(s"${player.currentMp} / ${player.mp}", labelHeader)

    attrRows ++ Seq(
      createTableRow("HP", ""),
      new VBox:
        spacing = 2
        children = Seq(
          hpLabel,
          new ProgressBar:
            progress = player.currentHp.toDouble / player.hp
            prefWidth = 200
            style = progressBarHP
        )
      ,
      createTableRow("MP", ""),
      new VBox:
        spacing = 2
        children = Seq(
          mpLabel,
          new ProgressBar:
            progress = player.currentMp.toDouble / player.mp
            prefWidth = 200
            style = progressBarMP
        )
    )

  /**
   * Creates inventory display showing all items and quantities in a grid.
   *
   * @param player The player whose inventory to display
   * @return GridPane node containing item names and quantities
   */
  private def createInventoryContent(player: Player): Node = new GridPane:
    hgap = 50
    vgap = 10
    padding = Insets(5)

    add(createTableHeader("Item"), 0, 0)
    add(createTableHeader("Quantity"), 1, 0)
    add(createTableHeader("Gold"), 2, 0)
    add(createTableHeader("Rarity"), 3, 0)

    player.inventory.toSeq.zipWithIndex.foreach { case ((item, qty), idx) =>
      add(styledLabel(item.name), 0, idx + 1)
      add(styledLabel(qty.toString), 1, idx + 1)
      add(styledLabel(item.gold.toString), 2, idx + 1)
      add(styledLabel(item.rarity.toString), 3, idx + 1)
    }

  /**
   * Creates hero diary content showing event messages in a text area.
   *
   * @return TextArea containing recent adventure events
   */
  private def createDiaryContent(): Node = new TextArea:
    text = if eventMessages.isEmpty then "Waiting for adventures..." else eventMessages.mkString("\n")
    editable = false
    prefHeight = 230
    style = textAreaStyle
    focusTraversable = false

  /**
   * Creates combat log content showing recent combat messages.
   *
   * @return TextArea containing recent combat events
   */
  private def createCombatLogContent(): Node =
    val area = new TextArea:
      text = if combatMessages.isEmpty then "No combat yet..." else combatMessages.mkString("\n")
      editable = false
      style = textAreaStyle
      focusTraversable = false
      wrapText = true

    Platform.runLater {
      area.positionCaret(area.text.value.length)
    }
    area

  /**
   * Creates monster information display showing current encounter details.
   *
   * @return Sequence of nodes containing detailed monster information
   */
  private def createMonsterInfoContent(): Seq[Node] =
    currentMonster match
      case Some(monster) =>
        val hpLabel = styledLabel(s"HP: ${monster.attributes.currentHp}/${monster.attributes.hp}")
        val hpBar = new ProgressBar:
          progress = monster.attributes.currentHp.toDouble / monster.attributes.hp
          prefWidth = 150
          style = progressBarHP

        val infoGrid = new GridPane:
          hgap = 8
          vgap = 4

          add(styledLabel(s"${monster.name} (Level ${monster.level})", labelMedium, labelBold), 0, 0)
          add(styledLabel(s"Type: ${monster.monsterType}"), 0, 1)
          add(styledLabel(s"Zone: ${monster.originZone}"), 0, 2)
          add(hpLabel, 0, 3)
          add(hpBar, 1, 3)
          add(styledLabel(s"Attack: ${monster.attributes.attack}"), 0, 4)
          add(styledLabel(f"Physical weakness: ${monster.attributes.weaknessPhysical}%.2f"), 1, 4)
          add(styledLabel(s"Defense: ${monster.attributes.defense}"), 0, 5)
          add(styledLabel(f"Magical weakness: ${monster.attributes.weaknessMagic}%.2f"), 1, 5)
          add(styledLabel(s"Behavior: ${monster.behavior}"), 0, 6)
          add(styledLabel(s"Gold Reward: ${monster.goldReward}"), 0, 7)
          add(styledLabel(s"EXP Reward: ${monster.experienceReward}"), 0, 8)
          add(styledLabel(s"Item Reward: ${monster.itemReward.fold("None")(_.toString)}"), 0, 9)
          add(styledLabel(s"Equipment Reward: ${monster.equipReward.fold("None")(_.toString)}"), 0, 10)
          add(styledLabel(monster.description, labelSmall, textGray), 0, 11, 2, 1)

        Seq(infoGrid)

      case None =>
        Seq(styledLabel("No monster encountered yet...", textGray))

  /**
   * Creates world information display showing current zone and description.
   *
   * @param player The player whose current zone to display
   * @return VBox containing zone name and description
   */
  private def createWorldContent(player: Player): Node =
    new VBox:
      spacing = 10
      children = Seq(
        styledLabel("Current World:", labelBold),
        styledLabel(player.currentZone.toString, labelHeader),
        new Label(World.getZoneDescription(player.currentZone)):
          style = combineStyles(labelMedium, textGray, "-fx-wrap-text: true")
          maxWidth = 200
      )

  /**
   * Creates skills display organized by effect type (Physical, Magic, Healing).
   *
   * @param player The player whose skills to display
   * @return HBox containing three columns of skills by type
   */
  private def createSkillsContent(player: Player): Node =
    val physicalSkills = player.skills.filter(_.effectType == SkillEffectType.Physical)
    val magicSkills = player.skills.filter(_.effectType == SkillEffectType.Magic)
    val healingSkills = player.skills.filter(_.effectType == SkillEffectType.Healing)

    def formatSkill(skill: Skill): Label =
      styledLabel(s"${skill.name} (Lv.${skill.powerLevel}, MP: ${skill.manaCost})", labelSmall)

    def makeColumn(title: String, skills: List[Skill]) =
      new VBox:
        spacing = 5
        alignment = Pos.TopLeft
        children = Seq(
          styledLabel(title, labelBold, "-fx-underline: true")
        ) ++ skills.map(formatSkill)

    new HBox:
      spacing = 40
      alignment = Pos.TopLeft
      children = Seq(
        makeColumn("Physical", physicalSkills),
        makeColumn("Magic", magicSkills),
        makeColumn("Healing", healingSkills)
      )

  /**
   * Creates mission display showing active missions and their progress.
   *
   * @param player The player whose missions to display
   * @return VBox containing mission list or "no missions" message
   */
  private def createMissionContent(player: Player): Node = new VBox:
    spacing = 5
    children =
      if player.activeMissions.isEmpty then
        Seq(styledLabel("No missions accepted", labelBold))
      else
        val missionLabels = player.activeMissions.map: mission =>
          new HBox:
            spacing = 10
            children = Seq(
              styledLabel(s"• ${mission.name}", labelMedium, labelBold),
              styledLabel(mission.description, labelSmall, textDarkGray),
              styledLabel(f"${mission.progression}/${mission.goal}", labelSmall, textLightGray),
              styledLabel(f"${mission.rewardGold},${mission.rewardExp},${mission.rewardItem}", labelSmall, textLightGray)
            )

        Seq(styledLabel(s"Current Missions: ${player.activeMissions.size}", labelBold)) ++ missionLabels

  /**
   * Creates a standardized table row with label and value.
   *
   * @param label The row label text
   * @param value The row value text
   * @return HBox containing formatted label-value pair
   */
  private def createTableRow(label: String, value: String): HBox = new HBox:
    spacing = 10
    children = Seq(
      styledLabel(label, labelBold, "-fx-min-width: 80"),
      styledLabel(value, "-fx-min-width: 120")
    )

  /**
   * Creates a standardized table header label.
   *
   * @param text The header text
   * @return Label with header styling (bold and underlined)
   */
  private def createTableHeader(text: String): Label =
    styledLabel(text, labelBold, "-fx-underline: true")

  /**
   * Updates the UI with current player information.
   *
   * @param player The updated player object to display
   */
  def updatePlayerInfo(player: Player): Unit =
    playerOpt = Some(player)
    stageOpt.foreach: stage =>
      val newRoot = createRoot(player)
      stage.scene().root = newRoot
    println(s"UI Update: ${player.name} - Level ${player.level} - HP: ${player.currentHp}/${player.hp}")

  /**
   * Adds a message to the combat log with automatic scrolling.
   *
   * @param message The combat message to add
   */
  def addCombatLog(message: String): Unit =
    combatMessages = (combatMessages :+ message).takeRight(20)
    updateCurrentUI()

  /**
   * Adds a message to the event diary with progress bar animation.
   *
   * @param message The event message to add
   */
  def addEventLog(message: String): Unit =
    eventMessages = (eventMessages :+ message).takeRight(30)
    updateCurrentUI()

    val animationTimer = new java.util.Timer()
    animationTimer.schedule(
      new java.util.TimerTask() {
        override def run(): Unit =
          animateHeroDiaryProgress()
          animationTimer.cancel()
      },
      100
    )

  /**
   * Updates the current monster information display.
   *
   * @param monster Optional monster to display, or None to clear the display
   */
  def updateMonsterInfo(monster: Option[Monster]): Unit =
    currentMonster = monster
    updateCurrentUI()

  /**
   * Shows game over screen with restart functionality.
   *
   * @param onRestart Callback function to execute when player chooses to restart
   */
  def showGameOverWithRestart(onRestart: () => Unit): Unit =
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
          styledLabel("Game Over!", "-fx-font-size: 24px", labelBold, "-fx-text-fill: #d32f2f"),
          styledLabel("Your hero has fallen...", "-fx-font-size: 16px", textGray),
          new HBox:
            spacing = 15
            alignment = Pos.Center
            children = Seq(
              new Button("Restart Game"):
                style = buttonGreen
                onAction = _ =>
                  gameOverStage.close()
                  stageOpt.foreach(_.close())
                  stageOpt = None
                  playerOpt = None
                  onRestart()
              ,
              new Button("Exit"):
                style = buttonRed
                onAction = _ =>
                  gameOverStage.close()
                  stageOpt.foreach(_.close())
                  stageOpt = None
                  playerOpt = None
            )
        )

    gameOverStage.scene = gameOverScene
    gameOverStage.show()

  /**
   * Updates the current UI if a window is open.
   */
  private def updateCurrentUI(): Unit =
    playerOpt.foreach: player =>
      stageOpt.foreach: stage =>
        val newRoot = createRoot(player)
        stage.scene().root = newRoot

  /**
   * Shows a basic game over notification.
   */
  def showGameOver(): Unit =
    println("GAME OVER - Show restart dialog")

  /**
   * Creates the hero diary panel with animated progress bar.
   *
   * @return VBox containing the complete hero diary panel
   */
  private def createHeroDiaryPanel(): VBox =
    val progressBar = new ProgressBar:
      progress = 0.0
      prefWidth = 150
      style = "-fx-accent:rgb(63, 147, 156)"

    heroDiaryProgressBar = Some(progressBar)

    new VBox:
      spacing = 0
      children = Seq(
        new HBox:
          style = panelHeader
          prefHeight = 30
          alignment = Pos.CenterLeft
          spacing = 10
          children = Seq(
            styledLabel("Hero Diary", labelHeader, "-fx-text-fill: white"),
            progressBar
          )
        ,
        new VBox:
          style = panelBody
          padding = Insets(10)
          children = Seq(createDiaryContent())
      )

  /**
   * Animates the Hero Diary progress bar to provide visual feedback.
   */
  private def animateHeroDiaryProgress(): Unit =
    heroDiaryProgressBar.foreach: progressBar =>
      progressBar.progress = 0.0

      val animationThread = new Thread(() =>
        try
          for i <- 0 to 10 do
            val progress = i / 10.0
            scalafx.application.Platform.runLater(() =>
              progressBar.progress = progress
            )
            Thread.sleep(100)
          Thread.sleep(500)
          scalafx.application.Platform.runLater(() =>
            progressBar.progress = 0.0
          )
        catch
          case _: InterruptedException =>
      )
      animationThread.setDaemon(true)
      animationThread.start()

