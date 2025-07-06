package view

import models.player.*
import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.*

import scala.util.Random

object PlayerGenerationUi extends JFXApp3:

  private var onPlayerCreated: Player => Unit = _ => ()

  def launch(callback: Player => Unit): Unit =
    onPlayerCreated = callback
    main(Array.empty)

  private var selectedRace: Race = Race.Human
  private var selectedClass: ClassType = ClassType.Warrior
  private var selectedBehavior: BehaviorType = BehaviorType.Aggressive
  private var randomAttributes: Attributes = Attributes.random()
  private var identity: Identity = Identity(race = selectedRace, classType = selectedClass)

  override def start(): Unit =
    val raceLabel = new Label(selectedRace.toString):
      style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
    val classLabel = new Label(selectedClass.toString):
      style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
    val behaviorLabel = new Label(selectedBehavior.toString):
      style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
    
    val playerNameLabel = new TextField {
      promptText = "Enter your Player name"
      text = "Player"
      style = "-fx-font-size: 14px; -fx-padding: 8px; -fx-border-radius: 5px; -fx-background-radius: 5px;"
      prefWidth = 200
    }
    
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

    // Character Section
    val characterSection = new VBox:
      spacing = 15
      padding = Insets(20)
      style = "-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;"
      children = Seq(
        new Label("Character Creation"):
          style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        ,
        // Name
        new VBox:
          spacing = 8
          children = Seq(
            new Label("Player Name:"):
              style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;"
            ,
            playerNameLabel
          )
        ,
        // Race
        new VBox:
          spacing = 8
          children = Seq(
            new Label("Race:"):
              style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;"
            ,
            new HBox:
              spacing = 10
              alignment = Pos.CenterLeft
              children = Seq(
                raceLabel,
                new Button("🎲 Roll"):
                  style = "-fx-background-color: #8B8B8B; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-border-radius: 5px; -fx-background-radius: 5px;"
                  onAction = _ =>
                    selectedRace = Random.shuffle(Race.values.toList).head
                    identity = Identity(race = selectedRace, classType = selectedClass)
                    raceLabel.text = selectedRace.toString
              )
          )
        ,
        // Class
        new VBox:
          spacing = 8
          children = Seq(
            new Label("Class:"):
              style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;"
            ,
            new HBox:
              spacing = 10
              alignment = Pos.CenterLeft
              children = Seq(
                classLabel,
                new Button("🎲 Roll"):
                  style = "-fx-background-color: #8B8B8B; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-border-radius: 5px; -fx-background-radius: 5px;"
                  onAction = _ =>
                    selectedClass = Random.shuffle(ClassType.values.toList).head
                    identity = Identity(race = selectedRace, classType = selectedClass)
                    classLabel.text = selectedClass.toString
              )
          )
        ,
        // Behavior
        new VBox:
          spacing = 8
          children = Seq(
            new Label("Behavior:"):
              style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;"
            ,
            new HBox:
              spacing = 10
              alignment = Pos.CenterLeft
              children = Seq(
                behaviorLabel,
                new Button("🎲 Roll"):
                  style = "-fx-background-color: #8B8B8B; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-border-radius: 5px; -fx-background-radius: 5px;"
                  onAction = _ =>
                    selectedBehavior = Random.shuffle(BehaviorType.values.toList).head
                    behaviorLabel.text = selectedBehavior.toString
              )
          )
      )

    // Attributes Section
    val attributesSection = new VBox:
      spacing = 15
      padding = Insets(20)
      style = "-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;"
      children = Seq(
        new Label("Attributes"):
          style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        ,
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
            strengthLabel.text = s"Strength: ${randomAttributes.strength}"
            constitutionLabel.text = s"Constitution: ${randomAttributes.constitution}"
            dexterityLabel.text = s"Dexterity: ${randomAttributes.dexterity}"
            intelligenceLabel.text = s"Intelligence: ${randomAttributes.intelligence}"
            wisdomLabel.text = s"Wisdom: ${randomAttributes.wisdom}"
            luckyLabel.text = s"Lucky: ${randomAttributes.lucky}"
      )

    val mainContent = new HBox:
      spacing = 20
      padding = Insets(20)
      children = Seq(characterSection, attributesSection)

    // Bottom Section
    val bottomSection = new HBox:
      spacing = 15
      padding = Insets(20)
      alignment = Pos.Center
      style = "-fx-background-color: #ecf0f1;"
      children = Seq(
        new Button("✨ Create Hero"):
          style = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 12 25; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-font-weight: bold;"
          onAction = _ =>
            val player = PlayerFactory.createDefaultPlayer(playerNameLabel.text.value.trim, identity, randomAttributes, selectedBehavior)
            val finalPlayer = PlayerBonusesApplication.applyRaceAndClassBonuses(player)
            println(s"Player Created: $finalPlayer")
            onPlayerCreated(finalPlayer)
            stage.close()
      )

    stage = new JFXApp3.PrimaryStage:
      title = "Progress Quest - Player Generation"
      width = 600
      height = 500
      scene = new Scene:
        root = new BorderPane:
          center = mainContent
          bottom = bottomSection
