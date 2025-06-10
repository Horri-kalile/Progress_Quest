import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.Font
import scalafx.scene.paint.Color
import scalafx.Includes._
import scalafx.scene.Node

object ProgressQuestUI extends JFXApp3 {

  override def start(): Unit = {
    stage = new JFXApp3.PrimaryStage {
      title = "ProgressQuest"
      width = 800
      height = 600
      scene = new Scene {
        root = new BorderPane {
          padding = Insets(15)
          style = "-fx-background-color: #e0e0e0"

          // Top Section: Character, Equipment, Stats
          top = new HBox {
            spacing = 15
            alignment = Pos.TopLeft
            children = Seq(
              createPanelWithHeader("Character Player", new VBox { children = createCharacterContent() }),
              createPanelWithHeader("Equipment", new VBox { children = createEquipmentContent() }),
              createPanelWithHeader("Stats", new VBox { children = createStatsContent() })
            )
          }

          // Center Section: Inventory, Mondo, Skill, Mission
          center = new HBox {
            spacing = 15
            alignment = Pos.CenterLeft
            children = Seq(
              createPanelWithHeader("Inventory", createInventoryContent()),
              createPanelWithHeader("Mondo", createMondoContent()),
              createPanelWithHeader("Skill", createSkillContent()),
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

  private def createCharacterContent(): Seq[Node] = Seq(
    createTableRow("Name", "saitama"),
    createTableRow("Race", "Low Elf"),
    createTableRow("Class", "Mage"),
    createTableRow("Level", "5"),
    createTableRow("Gold", "123")
  )

  private def createEquipmentContent(): Seq[Node] = Seq(
    createTableRow("Weapon", "Sword"),
    createTableRow("Armor", "Robe"),
    createTableRow("Accessories", "Ring of Mana"),
    createTableRow("Accessories", "Gloves of Dexterity"),
    createTableRow("Accessories", "Boots of Speed")
  )

  private def createStatsContent(): Seq[Node] = Seq(
    createTableRow("STR", "10"),
    createTableRow("DEX", "8"),
    createTableRow("INT", "15"),
    createTableRow("HP", ""),
    new ProgressBar {
      progress = 0.8
      prefWidth = 200
      style = "-fx-accent: #4682b4"
    },
    createTableRow("MP", ""),
    new ProgressBar {
      progress = 0.4
      prefWidth = 200
      style = "-fx-accent: #9370db"
    }
  )

  private def createInventoryContent(): Node = {
    new GridPane {
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
  }

  private def createMondoContent(): Node =
    new Label("World exploration coming soon...")

  private def createSkillContent(): Node =
    new Label("Skills will be displayed here.")

  private def createMissionContent(): Node =
    new Label("No active mission.")

  private def createDiaryContent(): Node = {
    new TextArea {
      text = "Hero entered the dungeon...\nFound a mysterious sword\nMet a friendly merchant"
      editable = false
      prefHeight = 150
      style = "-fx-font-family: monospace; -fx-font-size: 12; -fx-background-color: transparent"
      mouseTransparent = true
      focusTraversable = false
    }
  }

  private def createCombatLogContent(): Node = {
    new TextArea {
      text = "Fought a goblin!\nTook 5 damage!\nDefeated the goblin!\nGained 10 XP!"
      editable = false
      prefHeight = 150
      style = "-fx-font-family: monospace; -fx-font-size: 12; -fx-background-color: transparent"
      mouseTransparent = true
      focusTraversable = false
    }
  }

  private def createTableRow(label: String, value: String): HBox = {
    new HBox {
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
  }

  private def createTableHeader(text: String): Label = {
    new Label(text) {
      style = "-fx-font-weight: bold; -fx-underline: true"
    }
  }
}
