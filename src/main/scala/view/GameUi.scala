package view

import models.monster.OriginZone
import models.player.{EquipmentSlot, Player}
import models.world.World
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.*
import scalafx.scene.layout.*
import scalafx.stage.Stage

object GameUi {

  private var stageOpt: Option[Stage] = None
  var playerOpt: Option[Player] = None

  /** Call this to open the main game UI window */
  def open(): Unit = {
    val player = playerOpt.getOrElse(throw new Exception("Player not set"))
    val stage = new Stage {
      title = "ProgressQuest"
      width = 850
      height = 700
      scene = new Scene {
        root = new BorderPane {
          padding = Insets(15)
          style = "-fx-background-color: #e0e0e0"

          // Top Section: Character, Equipment, Stats
          top = new HBox {
            spacing = 15
            alignment = Pos.TopLeft
            children = Seq(
              createPanelWithHeader("Character Player", new VBox {
                children = createCharacterContent(player)
              }),
              createPanelWithHeader("Equipment", new VBox {
                children = createEquipmentContent(player)
              }),
              createPanelWithHeader("Stats", new VBox {
                children = createStatsContent(player)
              })
            )
          }

          // Center Section: Inventory, World, Skills, and Mission
          center = new HBox {
            spacing = 15
            children = Seq(
              createPanelWithHeader("Inventory", createInventoryContent()),
              createPanelWithHeader("World", createWorldContent()),
              createPanelWithHeader("Skills", createSkillsContent()),
              createPanelWithHeader("Mission", createMissionContent())
            )
          }

          // Bottom Section: Diary and Combat Log
          bottom = new HBox {
            spacing = 15
            children = Seq(
              createPanelWithHeader("Hero Diary", createDiaryContent()),
              createPanelWithHeader("Combat Log", createCombatLogContent())
            )
          }
        }
      }
    }

    stage.show()
    stageOpt = Some(stage)
  }

  private def createPanelWithHeader(title: String, content: Node): VBox = {
    new VBox {
      spacing = 0
      children = Seq(
        // Gray header band
        new HBox {
          style = "-fx-background-color: #a9a9a9; -fx-padding: 5 10 5 10"
          prefHeight = 30
          alignment = Pos.CenterLeft
          children = Seq(
            new Label(title) {
              style = "-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14"
            }
          )
        },
        // White content area
        new VBox {
          style = "-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 0 1 1 1"
          padding = Insets(10)
          children = Seq(content)
        }
      )
    }
  }

  private def createCharacterContent(player: Player): Seq[Node] = Seq(
    createTableRow("Name", player.name),
    createTableRow("Race", player.identity.race.toString),
    createTableRow("Class", player.identity.classType.toString),
    createTableRow("Level", player.level.toString),
    createTableRow("Gold", player.gold.toString),
    createTableRow("Exp/NextLevel", new Label(s"${player.exp} / ${player.level * 100}").text.value.trim)
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


  private def createInventoryContent(): Node = new GridPane {
    hgap = 10
    vgap = 5
    padding = Insets(5)
    add(createTableHeader("Item"), 0, 0)
    add(createTableHeader("Qty"), 1, 0)
    add(new Label("Potion"), 0, 1)
    add(new Label("2"), 1, 1)
    add(new Label("Sword"), 0, 2)
    add(new Label("1"), 1, 2)
    add(new Label("Scroll"), 0, 3)
    add(new Label("3"), 1, 3)
  }

  private def createDiaryContent(): Node = new TextArea {
    text = "Hero entered the dungeon...\nFound a mysterious sword\nMet a friendly merchant"
    editable = false
    prefHeight = 230
    style = "-fx-font-family: monospace; -fx-font-size: 12; -fx-background-color: transparent"
    mouseTransparent = true
    focusTraversable = false
  }

  private def createCombatLogContent(): Node = new TextArea {
    text = "Fought a goblin!\nTook 5 damage!\nDefeated the goblin!\nGained 10 XP!"
    editable = false
    prefHeight = 230
    style = "-fx-font-family: monospace; -fx-font-size: 12; -fx-background-color: transparent"
    mouseTransparent = true
    focusTraversable = false
  }

  private def createWorldContent(): Node = {
    val zones = OriginZone.values.toList
    val currentZone = zones(scala.util.Random.nextInt(zones.length))
    val mondoInstance = new World(currentZone)

    new VBox {
      spacing = 10
      children = Seq(
        new Label("Current World:") {
          style = "-fx-font-weight: bold"
        },
        new Label(currentZone.toString) {
          style = "-fx-font-size: 14; -fx-font-weight: bold"
        },
        new Label(mondoInstance.getZoneDescription) {
          style = "-fx-font-size: 12; -fx-text-fill: #666666; -fx-wrap-text: true"
          maxWidth = 200
        }
      )
    }
  }

  private def createSkillsContent(): Node = new VBox {
    spacing = 5
    children = Seq(
      createTableRow("Magic", "Level 2"),
      createTableRow("Swordsmanship", "Level 1"),
      createTableRow("Archery", "Level 3"),
      createTableRow("Stealth", "Level 1")
    )
  }

  private def createMissionContent(): Node = new VBox {
    spacing = 5
    children = Seq(
      new Label("Current Mission:") {
        style = "-fx-font-weight: bold"
      },
      new Label("Defeat the Dragon") {
        style = "-fx-font-size: 12"
      },
      new Label("Progress:") {
        style = "-fx-font-weight: bold; -fx-padding: 5 0 0 0"
      },
      new ProgressBar {
        progress = 0.3
        prefWidth = 150
        style = "-fx-accent: #4CAF50"
      }
    )
  }

  private def createTableRow(label: String, value: String): HBox = new HBox {
    spacing = 10
    children = Seq(
      new Label(label) {
        style = "-fx-font-weight: bold; -fx-min-width: 80"
      },
      new Label(value) {
        style = "-fx-min-width: 120"
      }
    )
  }

  private def createTableHeader(text: String): Label = new Label(text) {
    style = "-fx-font-weight: bold; -fx-underline: true"
  }
}
