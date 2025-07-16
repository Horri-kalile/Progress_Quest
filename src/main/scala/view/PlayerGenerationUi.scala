package view

import models.player.*
import models.player.Behavior.BehaviorType
import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.*
import scalafx.stage.Stage

import scala.util.Random

/**
 * User interface for creating new player characters.
 * Provides interactive controls for selecting race, class, behavior and attributes.
 *
 * This object manages the character creation process, allowing users to:
 * - Enter a custom player name
 * - Select or randomly roll race, class, and behavior
 * - Generate random attributes with reroll capability
 * - View descriptive information for races and classes
 * - Create a fully configured player character
 */
object PlayerGenerationUi extends JFXApp3:

  /** Callback function executed when a player is successfully created */
  private var onPlayerCreated: Player => Unit = _ => ()

  /**
   * Launches the player generation UI with a callback function.
   *
   * @param callback Function to handle the created player when character creation is complete
   */
  def launch(callback: Player => Unit): Unit =
    onPlayerCreated = callback
    main(Array.empty)

  /**
   * Opens player generation UI directly without using launch() method.
   * Used for restarting the character creation process.
   *
   * @param callback Function to handle the created player
   */
  def openPlayerGeneration(callback: Player => Unit): Unit =
    onPlayerCreated = callback
    // Reset selections to defaults
    selectedRace = Race.Human
    selectedClass = ClassType.Warrior
    selectedBehavior = BehaviorType.Aggressive
    randomAttributes = Attributes.random()
    identity = Identity(race = selectedRace, classType = selectedClass)

    // Create the UI directly with a regular Stage instead of JFXApp3.PrimaryStage
    createPlayerGenerationWindow()

  /** Currently selected race for the character being created */
  private var selectedRace: Race = Race.Human

  /** Currently selected class for the character being created */
  private var selectedClass: ClassType = ClassType.Warrior

  /** Currently selected behavior pattern for the character being created */
  private var selectedBehavior: BehaviorType = BehaviorType.Aggressive

  /** Current set of randomly generated attributes */
  private var randomAttributes: Attributes = Attributes.random()

  /** Current character identity combining race and class */
  private var identity: Identity = Identity(race = selectedRace, classType = selectedClass)

  /**
   * Creates and displays the player generation window using a regular Stage.
   * This method is used for reopening the UI without launching a new JFXApp3 instance.
   */
  private def createPlayerGenerationWindow(): Unit =
    // UI element labels for displaying current selections
    val raceLabel = new Label(selectedRace.toString):
      style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
    val classLabel = new Label(selectedClass.toString):
      style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
    val behaviorLabel = new Label(selectedBehavior.toString):
      style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"

    // Description labels for providing helpful information to users
    val raceDescriptionLabel = new Label(getRaceDescription(selectedRace)):
      style = "-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;"
      maxWidth = 250

    val classDescriptionLabel = new Label(getClassDescription(selectedClass)):
      style = "-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;"
      maxWidth = 250

    // Input field for player name
    val playerNameLabel = new TextField {
      promptText = "Enter your Player name"
      text = "Player"
      style = "-fx-font-size: 14px; -fx-padding: 8px; -fx-border-radius: 5px; -fx-background-radius: 5px;"
      prefWidth = 200
    }

    // Attribute display labels showing current random values
    val strengthLabel = new Label(s"Strength: ${randomAttributes.strength}"):
      style = "-fx-font-size: 13px; -fx-text-fill: #34495e;"
    val constitutionLabel = new Label(s"Constitution: ${randomAttributes.constitution}"):
      style = "-fx-font-size: 13px; -fx-text-fill: #34495e;"
    val dexterityLabel = new Label(s"Dexterity: ${randomAttributes.dexterity}"):
      style = "-fx-font-size: 13px; -fx-text-fill: #34495e;"
    val intelligenceLabel = new Label(s"Intelligence: ${randomAttributes.intelligence}"):
      style = "-fx-font-size: 13px; -fx-text-fill: #34495e;"
    val wisdomLabel = new Label(s"Wisdom: ${randomAttributes.wisdom}"):
      style = "-fx-font-size: 13px; -fx-text-fill: #34495e;"
    val luckyLabel = new Label(s"Lucky: ${randomAttributes.lucky}"):
      style = "-fx-font-size: 13px; -fx-text-fill: #34495e;"

    // Character creation section containing name, race, class, and behavior selection
    val characterSection = new VBox:
      spacing = 15
      padding = Insets(20)
      style =
        "-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;"
      children = Seq(
        new Label("Character Creation"):
          style =
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        ,
        // Player name input section
        new VBox:
          spacing = 8
          children = Seq(
            new Label("Player Name:"):
              style =
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;"
            ,
            playerNameLabel
          )
        ,
        // Race selection section with roll button and description
        new VBox:
          spacing = 8
          children = Seq(
            new Label("Race:"):
              style =
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;"
            ,
            new HBox:
              spacing = 10
              alignment = Pos.CenterLeft
              children = Seq(
                raceLabel,
                new Button("🎲 Roll"):
                  style =
                    "-fx-background-color: #8B8B8B; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-border-radius: 5px; -fx-background-radius: 5px;"
                  onAction = _ =>
                    // Randomly select a new race and update UI
                    selectedRace = Random.shuffle(Race.values.toList).head
                    identity = Identity(race = selectedRace, classType = selectedClass)
                    raceLabel.text = selectedRace.toString
                    raceDescriptionLabel.text = getRaceDescription(selectedRace)
              )
            ,
            raceDescriptionLabel
          )
        ,
        // Class selection section with roll button and description
        new VBox:
          spacing = 8
          children = Seq(
            new Label("Class:"):
              style =
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;"
            ,
            new HBox:
              spacing = 10
              alignment = Pos.CenterLeft
              children = Seq(
                classLabel,
                new Button("🎲 Roll"):
                  style =
                    "-fx-background-color: #8B8B8B; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-border-radius: 5px; -fx-background-radius: 5px;"
                  onAction = _ =>
                    // Randomly select a new class and update UI
                    selectedClass = Random.shuffle(ClassType.values.toList).head
                    identity = Identity(race = selectedRace, classType = selectedClass)
                    classLabel.text = selectedClass.toString
                    classDescriptionLabel.text = getClassDescription(selectedClass)
              )
            ,
            classDescriptionLabel
          )
        ,
        // Behavior selection section with roll button
        new VBox:
          spacing = 8
          children = Seq(
            new Label("Behavior:"):
              style =
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;"
            ,
            new HBox:
              spacing = 10
              alignment = Pos.CenterLeft
              children = Seq(
                behaviorLabel,
                new Button("🎲 Roll"):
                  style =
                    "-fx-background-color: #8B8B8B; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-border-radius: 5px; -fx-background-radius: 5px;"
                  onAction = _ =>
                    // Randomly select a new behavior pattern
                    selectedBehavior = Random.shuffle(BehaviorType.values.toList).head
                    behaviorLabel.text = selectedBehavior.toString
              )
          )
      )

    // Attributes section displaying character stats with reroll capability
    val attributesSection = new VBox:
      spacing = 15
      padding = Insets(20)
      style =
        "-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;"
      children = Seq(
        new Label("Attributes"):
          style =
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        ,
        // Individual attribute display
        new VBox:
          spacing = 8
          children = Seq(
            strengthLabel,
            constitutionLabel,
            dexterityLabel,
            intelligenceLabel,
            wisdomLabel,
            luckyLabel
          )
        ,
        // Button to regenerate all attributes
        new Button("🎲 Roll All Attributes"):
          style =
            "-fx-background-color: #8B8B8B; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 15; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold;"
          onAction = _ =>
            // Generate new random attributes and update all labels
            randomAttributes = Attributes.random()
            strengthLabel.text = s"Strength: ${randomAttributes.strength}"
            constitutionLabel.text = s"Constitution: ${randomAttributes.constitution}"
            dexterityLabel.text = s"Dexterity: ${randomAttributes.dexterity}"
            intelligenceLabel.text = s"Intelligence: ${randomAttributes.intelligence}"
            wisdomLabel.text = s"Wisdom: ${randomAttributes.wisdom}"
            luckyLabel.text = s"Lucky: ${randomAttributes.lucky}"
      )

    // Main layout combining character and attributes sections
    val mainContent = new HBox:
      spacing = 20
      padding = Insets(20)
      children = Seq(characterSection, attributesSection)

    // Create a regular Stage (not JFXApp3.PrimaryStage) for window reuse
    val newStage = new Stage()
    newStage.title = "Progress Quest - Player Generation"
    newStage.width = 800
    newStage.height = 600
    newStage.scene = new Scene:
      root = new BorderPane:
        center = mainContent
        bottom = new HBox:
          spacing = 15
          padding = Insets(20)
          alignment = Pos.Center
          style = "-fx-background-color: #ecf0f1;"
          children = Seq(
            // Final character creation button
            new Button("✨ Create Hero"):
              style =
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 12 25; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-font-weight: bold;"
              onAction = _ =>
                // Create player with current selections and apply bonuses
                val player = Player(playerNameLabel.text.value.trim, identity, randomAttributes, selectedBehavior)
                val finalPlayer = PlayerBonusesApplication.applyRaceAndClassBonuses(player)
                println(s"Player Created: $finalPlayer")
                onPlayerCreated(finalPlayer)
                newStage.close()
          )

    newStage.show()

  /**
   * Main entry point for the JFXApp3 application.
   * Creates and displays the player generation interface.
   */
  override def start(): Unit =
    // UI element labels for displaying current selections
    val raceLabel = new Label(selectedRace.toString):
      style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
    val classLabel = new Label(selectedClass.toString):
      style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
    val behaviorLabel = new Label(selectedBehavior.toString):
      style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"

    // Description labels for providing helpful information to users
    val raceDescriptionLabel = new Label(getRaceDescription(selectedRace)):
      style = "-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;"
      maxWidth = 250

    val classDescriptionLabel = new Label(getClassDescription(selectedClass)):
      style = "-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;"
      maxWidth = 250

    // Input field for player name
    val playerNameLabel = new TextField {
      promptText = "Enter your Player name"
      text = "Player"
      style = "-fx-font-size: 14px; -fx-padding: 8px; -fx-border-radius: 5px; -fx-background-radius: 5px;"
      prefWidth = 200
    }

    // Attribute display labels showing current random values
    val strengthLabel = new Label(s"Strength: ${randomAttributes.strength}"):
      style = "-fx-font-size: 13px; -fx-text-fill: #34495e;"
    val constitutionLabel = new Label(s"Constitution: ${randomAttributes.constitution}"):
      style = "-fx-font-size: 13px; -fx-text-fill: #34495e;"
    val dexterityLabel = new Label(s"Dexterity: ${randomAttributes.dexterity}"):
      style = "-fx-font-size: 13px; -fx-text-fill: #34495e;"
    val intelligenceLabel = new Label(s"Intelligence: ${randomAttributes.intelligence}"):
      style = "-fx-font-size: 13px; -fx-text-fill: #34495e;"
    val wisdomLabel = new Label(s"Wisdom: ${randomAttributes.wisdom}"):
      style = "-fx-font-size: 13px; -fx-text-fill: #34495e;"
    val luckyLabel = new Label(s"Lucky: ${randomAttributes.lucky}"):
      style = "-fx-font-size: 13px; -fx-text-fill: #34495e;"

    // Character creation section containing name, race, class, and behavior selection
    val characterSection = new VBox:
      spacing = 15
      padding = Insets(20)
      style =
        "-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;"
      children = Seq(
        new Label("Character Creation"):
          style =
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        ,
        // Player name input section
        new VBox:
          spacing = 8
          children = Seq(
            new Label("Player Name:"):
              style =
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;"
            ,
            playerNameLabel
          )
        ,
        // Race selection section with roll button and description
        new VBox:
          spacing = 8
          children = Seq(
            new Label("Race:"):
              style =
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;"
            ,
            new HBox:
              spacing = 10
              alignment = Pos.CenterLeft
              children = Seq(
                raceLabel,
                new Button("🎲 Roll"):
                  style =
                    "-fx-background-color: #8B8B8B; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-border-radius: 5px; -fx-background-radius: 5px;"
                  onAction = _ =>
                    // Randomly select a new race and update UI
                    selectedRace = Random.shuffle(Race.values.toList).head
                    identity = Identity(race = selectedRace, classType = selectedClass)
                    raceLabel.text = selectedRace.toString
                    raceDescriptionLabel.text = getRaceDescription(selectedRace)
              )
            ,
            raceDescriptionLabel
          )
        ,
        // Class selection section with roll button and description
        new VBox:
          spacing = 8
          children = Seq(
            new Label("Class:"):
              style =
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;"
            ,
            new HBox:
              spacing = 10
              alignment = Pos.CenterLeft
              children = Seq(
                classLabel,
                new Button("🎲 Roll"):
                  style =
                    "-fx-background-color: #8B8B8B; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-border-radius: 5px; -fx-background-radius: 5px;"
                  onAction = _ =>
                    // Randomly select a new class and update UI
                    selectedClass = Random.shuffle(ClassType.values.toList).head
                    identity = Identity(race = selectedRace, classType = selectedClass)
                    classLabel.text = selectedClass.toString
                    classDescriptionLabel.text = getClassDescription(selectedClass)
              )
            ,
            classDescriptionLabel
          )
        ,
        // Behavior selection section with roll button
        new VBox:
          spacing = 8
          children = Seq(
            new Label("Behavior:"):
              style =
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;"
            ,
            new HBox:
              spacing = 10
              alignment = Pos.CenterLeft
              children = Seq(
                behaviorLabel,
                new Button("🎲 Roll"):
                  style =
                    "-fx-background-color: #8B8B8B; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-border-radius: 5px; -fx-background-radius: 5px;"
                  onAction = _ =>
                    // Randomly select a new behavior pattern
                    selectedBehavior = Random.shuffle(BehaviorType.values.toList).head
                    behaviorLabel.text = selectedBehavior.toString
              )
          )
      )

    // Attributes section displaying character stats with reroll capability
    val attributesSection = new VBox:
      spacing = 15
      padding = Insets(20)
      style =
        "-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;"
      children = Seq(
        new Label("Attributes"):
          style =
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        ,
        // Individual attribute display
        new VBox:
          spacing = 8
          children = Seq(
            strengthLabel,
            constitutionLabel,
            dexterityLabel,
            intelligenceLabel,
            wisdomLabel,
            luckyLabel
          )
        ,
        // Button to regenerate all attributes
        new Button("🎲 Roll All Attributes"):
          style =
            "-fx-background-color: #8B8B8B; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 15; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold;"
          onAction = _ =>
            // Generate new random attributes and update all labels
            randomAttributes = Attributes.random()
            strengthLabel.text = s"Strength: ${randomAttributes.strength}"
            constitutionLabel.text = s"Constitution: ${randomAttributes.constitution}"
            dexterityLabel.text = s"Dexterity: ${randomAttributes.dexterity}"
            intelligenceLabel.text = s"Intelligence: ${randomAttributes.intelligence}"
            wisdomLabel.text = s"Wisdom: ${randomAttributes.wisdom}"
            luckyLabel.text = s"Lucky: ${randomAttributes.lucky}"
      )

    // Main layout combining character and attributes sections
    val mainContent = new HBox:
      spacing = 20
      padding = Insets(20)
      children = Seq(characterSection, attributesSection)

    // Bottom section with character creation button
    val bottomSection = new HBox:
      spacing = 15
      padding = Insets(20)
      alignment = Pos.Center
      style = "-fx-background-color: #ecf0f1;"
      children = Seq(
        new Button("✨ Create Hero"):
          style =
            "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 12 25; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-font-weight: bold;"
          onAction = _ =>
            // Create player with current selections and apply bonuses
            val player = Player(playerNameLabel.text.value.trim, identity, randomAttributes, selectedBehavior)
            val finalPlayer = PlayerBonusesApplication.applyRaceAndClassBonuses(player)
            println(s"Player Created: $finalPlayer")
            onPlayerCreated(finalPlayer)
            stage.close()
      )

    // Configure primary stage for the application
    stage = new JFXApp3.PrimaryStage:
      title = "Progress Quest - Player Generation"
      width = 800
      height = 600
      scene = new Scene:
        root = new BorderPane:
          center = mainContent
          bottom = bottomSection

  /**
   * Provides descriptive text for each race to help users understand their characteristics.
   *
   * @param race The race enum value to get description for
   * @return A user-friendly description string explaining the race's traits
   */
  private def getRaceDescription(race: Race): String = race match
    case Race.Human => "Balanced race, good for beginners. Starts with a random Equipment"
    case Race.Titan => "Massive beings with high strength."
    case Race.Orc => "Fierce warriors with more wisdom."
    case Race.Gnome => "Small but highly intelligence."
    case Race.Elf => "Graceful with high dexterity."
    case Race.Dwarf => "Hardy folk with strong constitution."
    case Race.PandaMan => "Peaceful but wise and lucky."
    case Race.Gundam => "Mechanical warriors, more strength and wisdom."

  /**
   * Provides descriptive text for each class to help users understand their combat role.
   *
   * @param classType The class enum value to get description for
   * @return A user-friendly description string explaining the class's abilities and playstyle
   */
  private def getClassDescription(classType: ClassType): String = classType match
    case ClassType.Warrior => "Starts with more Hp"
    case ClassType.Poisoner => "Starts with more Mp"
    case ClassType.CowBoy => "Starts with more Hp and Mp"
    case ClassType.Paladin => "Starts with healing magic"
    case ClassType.Assassin => "Starts with physical skill"
    case ClassType.Mage => "Starts with magic skill"
    case ClassType.Cleric => "Starts with random magic"
