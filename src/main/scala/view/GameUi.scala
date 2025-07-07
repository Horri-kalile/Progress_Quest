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
        createHeroDiaryPanel(),
        createPanelWithHeader("Combat Log", createCombatLogContent()),
        createPanelWithHeader("Monster Info", createMonsterInfoContent())
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
      mouseTransparent = true
      focusTraversable = false

  private def createMonsterInfoContent(): Node =
    val monsterText = currentMonster match
      case Some(monster) =>
        s"""${monster.name} (Level ${monster.level})
           |Type: ${monster.monsterType}
           |Zone: ${monster.originZone}
           |HP: ${monster.attributes.hp}
           |Attack: ${monster.attributes.attack}
           |Defense: ${monster.attributes.defense}
           |Behavior: ${monster.behavior}
           |Gold Reward: ${monster.goldReward}
           |EXP Reward: ${monster.experienceReward}
           |
           |${monster.description}""".stripMargin
      case None =>
        "No monster encountered yet..."
    
    new TextArea:
      text = monsterText
      editable = false
      style = "-fx-font-family: monospace; -fx-font-size: 12; -fx-background-color: transparent"
      mouseTransparent = true
      focusTraversable = false

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
        val missionLabels = player.activeMissions.map { mission =>
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
        }

        Seq(
          new Label(s"Current Missions: ${player.activeMissions.size}"):
            style = "-fx-font-weight: bold"
        ) ++ missionLabels


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
    animationTimer.schedule(new java.util.TimerTask() {
      override def run(): Unit = {
        animateHeroDiaryProgress()
        animationTimer.cancel()
      }
    }, 100) // Small delay to ensure UI is fully updated

  /**
   * Update the current monster info
   */
  def updateMonsterInfo(monster: Option[Monster]): Unit =
    currentMonster = monster
    updateCurrentUI()

  /**
   * Update the current UI if it's open
   */
  private def updateCurrentUI(): Unit =
    playerOpt.foreach { player =>
      stageOpt.foreach { stage =>
        val newRoot = createRoot(player)
        stage.scene().root = newRoot
      }
    }

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
      style = "-fx-accent:rgb(63, 147, 156)"

    // Store the progress bar reference
    heroDiaryProgressBar = Some(progressBar)

    new VBox:
      spacing = 0
      children = Seq(
        new HBox:
          style = "-fx-background-color: #a9a9a9; -fx-padding: 5 10 5 10"
          prefHeight = 30
          alignment = Pos.CenterLeft
          spacing = 10
          children = Seq(
            new Label("Hero Diary"):
              style = "-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14"
            ,
            progressBar
          )
        ,
        new VBox:
          style = "-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 0 1 1 1"
          padding = Insets(10)
          children = Seq(createDiaryContent())
      )

  /**
   * Animate the Hero Diary progress bar
   */
  private def animateHeroDiaryProgress(): Unit =
    heroDiaryProgressBar.foreach { progressBar =>
      // Reset to 0 and animate to 1.0
      progressBar.progress = 0.0

      // Simple animation using a thread with Platform.runLater
      val animationThread = new Thread(() => {
        try {
          for (i <- 0 to 10) {
            val progress = i / 10.0
            scalafx.application.Platform.runLater(() => {
              progressBar.progress = progress
            })
            Thread.sleep(100) // 100ms delay
          }
          Thread.sleep(500) // Stay at 100% for 500ms
          scalafx.application.Platform.runLater(() => {
            progressBar.progress = 0.0 // Reset to 0
          })
        } catch {
          case _: InterruptedException => // Thread was interrupted
        }
      })
      animationThread.setDaemon(true)
      animationThread.start()
    }

