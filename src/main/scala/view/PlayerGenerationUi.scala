package view

import models.player.*
import models.player.Behavior.BehaviorType
import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.{BorderPane, HBox, VBox}

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
  var mainStage: JFXApp3.PrimaryStage = _

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
    resetSelections()
    mainStage = new JFXApp3.PrimaryStage:
      title = "Progress Quest - Player Generation"
      width = 800
      height = 600
      scene = new Scene:
        root = createPlayerGenerationWindow()
    mainStage.centerOnScreen()
    stage = mainStage

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

  /** Reset selection fields to defaults */
  private def resetSelections(): Unit =
    selectedRace = Race.Human
    selectedClass = ClassType.Warrior
    selectedBehavior = BehaviorType.Aggressive
    randomAttributes = Attributes.random()
    identity = Identity(race = selectedRace, classType = selectedClass)

  /** Creates styled Label with common font and color settings */
  private def createStyledLabel(text: String, fontSize: Int = 14, bold: Boolean = true, color: String = "#2c3e50", wrapText: Boolean = false, maxWidth: Double = Double.MaxValue): Label =
    val lbl = new Label(text)
    lbl.style = s"-fx-font-size: ${fontSize}px;${if bold then " -fx-font-weight: bold;" else ""} -fx-text-fill: $color;"
    if wrapText then
      lbl.setWrapText(true)
      lbl.setMaxWidth(maxWidth)
    lbl

  /** Creates a button with a roll icon and common styling */
  private def createRollButton(onClick: () => Unit): Button =
    val btn = new Button("🎲 Roll")
    btn.style = "-fx-background-color: #8B8B8B; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-border-radius: 5px; -fx-background-radius: 5px;"
    btn.onAction = _ => onClick()
    btn

  /** Updates attribute labels from current randomAttributes */
  private def updateAttributeLabels(strengthLabel: Label, constitutionLabel: Label, dexterityLabel: Label, intelligenceLabel: Label, wisdomLabel: Label, luckyLabel: Label): Unit =
    strengthLabel.text = s"Strength: ${randomAttributes.strength}"
    constitutionLabel.text = s"Constitution: ${randomAttributes.constitution}"
    dexterityLabel.text = s"Dexterity: ${randomAttributes.dexterity}"
    intelligenceLabel.text = s"Intelligence: ${randomAttributes.intelligence}"
    wisdomLabel.text = s"Wisdom: ${randomAttributes.wisdom}"
    luckyLabel.text = s"Lucky: ${randomAttributes.lucky}"

  /**
   * Creates and displays the player generation window using a regular Stage.
   * This method is used for reopening the UI without launching a new JFXApp3 instance.
   */
  private def createPlayerGenerationWindow(): BorderPane =
    val raceLabel = createStyledLabel(selectedRace.toString)
    val classLabel = createStyledLabel(selectedClass.toString)
    val behaviorLabel = createStyledLabel(selectedBehavior.toString)

    val raceDescriptionLabel = createStyledLabel(getRaceDescription(selectedRace), fontSize = 12, bold = false, color = "#7f8c8d", wrapText = true, maxWidth = 250)
    val classDescriptionLabel = createStyledLabel(getClassDescription(selectedClass), fontSize = 12, bold = false, color = "#7f8c8d", wrapText = true, maxWidth = 250)

    val playerNameLabel = new TextField:
      promptText = "Enter your Player name"
      text = "Player"
      style = "-fx-font-size: 14px; -fx-padding: 8px; -fx-border-radius: 5px; -fx-background-radius: 5px;"
      prefWidth = 200


    val strengthLabel = createStyledLabel(s"Strength: ${randomAttributes.strength}", fontSize = 13, bold = false, color = "#34495e")
    val constitutionLabel = createStyledLabel(s"Constitution: ${randomAttributes.constitution}", fontSize = 13, bold = false, color = "#34495e")
    val dexterityLabel = createStyledLabel(s"Dexterity: ${randomAttributes.dexterity}", fontSize = 13, bold = false, color = "#34495e")
    val intelligenceLabel = createStyledLabel(s"Intelligence: ${randomAttributes.intelligence}", fontSize = 13, bold = false, color = "#34495e")
    val wisdomLabel = createStyledLabel(s"Wisdom: ${randomAttributes.wisdom}", fontSize = 13, bold = false, color = "#34495e")
    val luckyLabel = createStyledLabel(s"Lucky: ${randomAttributes.lucky}", fontSize = 13, bold = false, color = "#34495e")

    def updateRace(): Unit =
      selectedRace = Random.shuffle(Race.values.toList).head
      identity = Identity(race = selectedRace, classType = selectedClass)
      raceLabel.text = selectedRace.toString
      raceDescriptionLabel.text = getRaceDescription(selectedRace)

    def updateClass(): Unit =
      selectedClass = Random.shuffle(ClassType.values.toList).head
      identity = Identity(race = selectedRace, classType = selectedClass)
      classLabel.text = selectedClass.toString
      classDescriptionLabel.text = getClassDescription(selectedClass)

    def updateBehavior(): Unit =
      selectedBehavior = Random.shuffle(BehaviorType.values.toList).head
      behaviorLabel.text = selectedBehavior.toString

    val characterSection = new VBox:
      spacing = 15
      padding = Insets(20)
      style =
        "-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;"
      children = Seq(
        createStyledLabel("Character Creation", fontSize = 18),
        new VBox:
          spacing = 8
          children = Seq(
            createStyledLabel("Player Name:", color = "#34495e"),
            playerNameLabel
          )
        ,
        new VBox:
          spacing = 8
          children = Seq(
            createStyledLabel("Race:", color = "#34495e"),
            new HBox:
              spacing = 10
              alignment = Pos.CenterLeft
              children = Seq(
                raceLabel,
                createRollButton(updateRace)
              )
            ,
            raceDescriptionLabel
          )
        ,
        new VBox:
          spacing = 8
          children = Seq(
            createStyledLabel("Class:", color = "#34495e"),
            new HBox:
              spacing = 10
              alignment = Pos.CenterLeft
              children = Seq(
                classLabel,
                createRollButton(updateClass)
              )
            ,
            classDescriptionLabel
          )
        ,
        new VBox:
          spacing = 8
          children = Seq(
            createStyledLabel("Behavior:", color = "#34495e"),
            new HBox:
              spacing = 10
              alignment = Pos.CenterLeft
              children = Seq(
                behaviorLabel,
                createRollButton(updateBehavior)
              )
          )
      )

    val attributesSection = new VBox:
      spacing = 15
      padding = Insets(20)
      style =
        "-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;"
      children = Seq(
        createStyledLabel("Attributes", fontSize = 18),
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
        new Button("🎲 Roll All Attributes"):
          style = "-fx-background-color: #8B8B8B; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 15; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold;"
          onAction = _ =>
            randomAttributes = Attributes.random()
            updateAttributeLabels(strengthLabel, constitutionLabel, dexterityLabel, intelligenceLabel, wisdomLabel, luckyLabel)
      )

    val bottomButtonBar = new HBox:
      spacing = 10
      alignment = Pos.CenterRight
      padding = Insets(10)
      children = Seq(
        new Button("Create Player"):
          style =
            "-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold;"
          onAction = _ =>
            val name = playerNameLabel.text()
            val player = Player(name, identity, randomAttributes, selectedBehavior)
            onPlayerCreated(PlayerBonusesApplication.applyRaceAndClassBonuses(player))
        ,
        new Button("Cancel"):
          style =
            "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-font-weight: bold;"
          onAction = _ => stage.close()
      )

    val mainContent = new HBox:
      spacing = 40
      padding = Insets(25)
      alignment = Pos.Center
      children = Seq(characterSection, attributesSection)

    new BorderPane:
      center = mainContent
      bottom = bottomButtonBar
      padding = Insets(15)


  /**
   * Entry point for the JFXApp3 application.
   * Sets up the primary stage with the player generation UI.
   */
  override def start(): Unit =
    resetSelections()
    mainStage = new JFXApp3.PrimaryStage:
      title = "Progress Quest - Player Generation"
      width = 800
      height = 600
      scene = new Scene:
        root = createPlayerGenerationWindow()

    stage = mainStage


  /**
   * Provides descriptive text for each race to help users understand their characteristics.
   *
   * @param race The race enum value to get description for
   * @return A user-friendly description string explaining the race's traits
   */
  def getRaceDescription(race: Race): String = race match
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
  def getClassDescription(classType: ClassType): String = classType match
    case ClassType.Warrior => "Starts with more Hp"
    case ClassType.Poisoner => "Starts with more Mp"
    case ClassType.CowBoy => "Starts with more Hp and Mp"
    case ClassType.Paladin => "Starts with healing magic"
    case ClassType.Assassin => "Starts with physical skill"
    case ClassType.Mage => "Starts with magic skill"
    case ClassType.Cleric => "Starts with random magic"

