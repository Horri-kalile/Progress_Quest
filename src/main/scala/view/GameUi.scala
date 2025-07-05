package view

import models.player.{EquipmentSlot, Player, Skill, SkillEffectType}
import models.world.World
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
  def createRoot(player: Player): BorderPane =
    new BorderPane:
      padding = Insets(15)
      style = "-fx-background-color: #e0e0e0"

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
        createPanelWithHeader("Hero Diary", createDiaryContent()),
        createPanelWithHeader("Combat Log", createCombatLogContent())
      ))

  private def createSectionRow(panels: Seq[Node]): HBox =
    new HBox:
      spacing = 15
      alignment = Pos.TopLeft
      children = panels
      children.foreach { child =>
        HBox.setHgrow(child, Priority.Always)
        child.maxWidth(Double.MaxValue)
      }

  private def createPanelWithHeader(title: String, content: Node): VBox =
    new VBox:
      spacing = 0
      children = Seq(
        new HBox:
          style = "-fx-background-color: #a9a9a9; -fx-padding: 5 10 5 10"
          prefHeight = 30
          alignment = Pos.CenterLeft
          children = Seq(
            new Label(title):
              style = "-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14"
          )
        ,
        new VBox:
          style = "-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 0 1 1 1"
          padding = Insets(10)
          children = Seq(content)
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
    EquipmentSlot.values.toSeq.map { slot =>
      val equipped = player.equipment.getOrElse(slot, None)
      val label = equipped.map(e => s"${e.name} (Value: ${e.value})").getOrElse("None")
      createTableRow(slot.toString, label)
    }

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
      createTableRow("HP", ""),
      new VBox:
        spacing = 2
        children = Seq(
          hpLabel,
          new ProgressBar:
            progress = player.currentHp.toDouble / player.hp
            prefWidth = 200
            style = "-fx-accent: #4682b4"
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
            style = "-fx-accent: #9370db"
        )
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
    text = "Hero entered the dungeon...\nFound a mysterious sword\nMet a friendly merchant"
    editable = false
    prefHeight = 230
    style = "-fx-font-family: monospace; -fx-font-size: 12; -fx-background-color: transparent"
    mouseTransparent = true
    focusTraversable = false


  private def createCombatLogContent(): Node =
    val combatLogArea = new TextArea:
      text = "Fought a goblin!\nTook 5 damage!\nDefeated the goblin!\nGained 10 XP!"
      editable = false
      style = "-fx-font-family: monospace; -fx-font-size: 12; -fx-background-color: transparent"
      mouseTransparent = true
      focusTraversable = false

    val monsterInfoArea = new TextArea:
      text = "Goblin" //TODO MANAGE SHOW INFO
      editable = false
      style = "-fx-font-family: monospace; -fx-font-size: 12; -fx-background-color: transparent"
      mouseTransparent = true
      focusTraversable = false

    val container = new HBox:
      padding = Insets(5)
      spacing = 10
      children = Seq(combatLogArea, monsterInfoArea)

    // Bind preferred widths proportionally to container width
    combatLogArea.prefWidth <== container.width * 0.7
    monsterInfoArea.prefWidth <== container.width * 0.3

    container

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
    children = Seq(
      new Label("Current Mission:"):
        style = "-fx-font-weight: bold"
      ,
      new Label("Defeat the Dragon"):
        style = "-fx-font-size: 12"
      ,
      new Label("Progress:"):
        style = "-fx-font-weight: bold; -fx-padding: 5 0 0 0"
      ,
      new ProgressBar:
        progress = 0.3
        prefWidth = 150
        style = "-fx-accent: #4CAF50"
    )

  private def createTableRow(label: String, value: String): HBox = new HBox:
    spacing = 10
    children = Seq(
      new Label(label):
        style = "-fx-font-weight: bold; -fx-min-width: 80"
      ,
      new Label(value):
        style = "-fx-min-width: 120"
    )

  private def createTableHeader(text: String): Label = new Label(text):
    style = "-fx-font-weight: bold; -fx-underline: true"

  /**
   * Update the UI with current player information
   * This method should be called from GameController
   */
  def updatePlayerInfo(player: Player): Unit =
    playerOpt = Some(player) // update stored player reference
    stageOpt.foreach { stage =>
      val newRoot = createRoot(player) // rebuild UI with updated player
      stage.scene().root = newRoot // replace root node
    }
    println(s"UI Update: ${player.name} - Level ${player.level} - HP: ${player.currentHp}/${player.hp}")


  /**
   * Add a message to the combat log
   */
  def addCombatLog(message: String): Unit =
    // TODO: Add message to combat log panel
    println(s"Combat Log: $message")

  /**
   * Show game over screen
   */
  def showGameOver(): Unit =
    // TODO: Show game over dialog with restart option
    println("GAME OVER - Show restart dialog")

