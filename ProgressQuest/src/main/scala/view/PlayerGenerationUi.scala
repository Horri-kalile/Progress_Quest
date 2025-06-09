package view

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.paint.Color.*
import models.player.{Attributes, BehaviorType, ClassType, Identity, Race}
import scalafx.Includes.*

import scala.util.Random

object PlayerGenerationUi extends JFXApp3:

  private var selectedRace: Race = Race.Human
  private var selectedClass: ClassType = ClassType.Warrior
  private var selectedBehavior: BehaviorType = BehaviorType.Aggressive
  private var randomAttributes: Attributes = Attributes.random()
  private val name: String = String("Player")
  private var identity: Identity = Identity(name = name, race = selectedRace, classType = selectedClass)

  override def start(): Unit =
    val raceLabel = new Label(selectedRace.toString)
    val classLabel = new Label(selectedClass.toString)
    val behaviorLabel = new Label(selectedBehavior.toString)
    val playerNameLabel = new Label(name);
    val strengthLabel = new Label(s"Strength: ${randomAttributes.strength}")
    val constitutionLabel = new Label(s"Constitution: ${randomAttributes.constitution}")
    val dexterityLabel = new Label(s"Dexterity: ${randomAttributes.dexterity}")
    val intelligenceLabel = new Label(s"Intelligence: ${randomAttributes.intelligence}")
    val wisdomLabel = new Label(s"Wisdom: ${randomAttributes.wisdom}")
    val luckyLabel = new Label(s"Lucky: ${randomAttributes.lucky}")

    val contentBox = new VBox:
      spacing = 10
      padding = Insets(20)
      children = Seq(
        // Race
        new HBox:
          spacing = 10
          children = Seq(
            new Label("Race: "),
            raceLabel,
            new Button("Roll"):
              onAction = _ =>
                selectedRace = Random.shuffle(Race.values.toList).head
                identity = Identity(name = name, race = selectedRace, classType = selectedClass)
                raceLabel.text = selectedRace.toString
          )
        ,

        // Class
        new HBox:
          spacing = 10
          children = Seq(
            new Label("Class: "),
            classLabel,
            new Button("Roll"):
              onAction = _ =>
                selectedClass = Random.shuffle(ClassType.values.toList).head
                identity = Identity(name = name, race = selectedRace, classType = selectedClass)
                classLabel.text = selectedClass.toString
          )
        ,

        // Behavior
        new HBox:
          spacing = 10
          children = Seq(
            new Label("Behavior: "),
            behaviorLabel,
            new Button("Roll"):
              onAction = _ =>
                selectedBehavior = Random.shuffle(BehaviorType.values.toList).head
                behaviorLabel.text = selectedBehavior.toString
          )
        ,
        // Attributes Roll Button
        new Button("Roll Attributes"):
          onAction = _ =>
            randomAttributes = Attributes.random()
            strengthLabel.text = s"Strength: ${randomAttributes.strength}"
            constitutionLabel.text = s"Constitution: ${randomAttributes.constitution}"
            dexterityLabel.text = s"Dexterity: ${randomAttributes.dexterity}"
            intelligenceLabel.text = s"Intelligence: ${randomAttributes.intelligence}"
            wisdomLabel.text = s"Wisdom: ${randomAttributes.wisdom}"
            luckyLabel.text = s"Lucky: ${randomAttributes.lucky}"
      )

    val attributesBox = new VBox:
      spacing = 8
      padding = Insets(10)
      alignment = Pos.TopRight
      children = Seq(
        strengthLabel,
        constitutionLabel,
        dexterityLabel,
        intelligenceLabel,
        wisdomLabel,
        luckyLabel
      )
    // TODO: Proceed to start game
    val bottomBox = new HBox:
      spacing = 8
      alignment = Pos.Center
      children = Seq(
        new Button("Confirm"):
          onAction = _ =>
            println(s"Player Confirmed: Race = $selectedRace, Class = $selectedClass, Behavior = $selectedBehavior, Identity = $identity, Attributes = $randomAttributes")
      )
    stage = new JFXApp3.PrimaryStage:
      title = "Generate Your Player"
      width = 400
      height = 400
      scene = new Scene:
        fill = LightPink
        root = new BorderPane:
          center = contentBox
          right = attributesBox
          bottom = bottomBox
