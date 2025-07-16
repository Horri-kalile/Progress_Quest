package util

object GameConfig:
  // Game mechanics configuration
  val baseSpecialChance: Double = 0.02 // 2%
  val baseDropChance: Double = 0.30 // 30%
  val baseLearnSkillChance: Double = 0.30
  val specialBonusPerLucky: Double = 0.001 // 0.1% per lucky
  val maxSpecialChance: Double = 0.09 // 9%
  val maxDropChance: Double = 1.00
  val baseGameOverChance: Double = 0.02 // 2%
  val minGameOverChance: Double = 0.01
  val maxBuffByZone: Double = 0.20 // 20% maximum buff
  val probabilityToMissAttackBonus: Double = 0.001
  val powerUpCost: Int = 100
  val dodgeBonusByDexterity: Double = 0.005
  val maxDodgeChance: Double = 0.50

  // UI Style constants - eliminate redundancy
  val baseFont: String = "-fx-font-family: monospace; -fx-font-size: 12"
  val labelBold: String = "-fx-font-weight: bold"
  val labelHeader: String = "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: black"
  val labelSmall: String = "-fx-font-size: 11"
  val labelMedium: String = "-fx-font-size: 12"
  val textGray: String = "-fx-text-fill: #666666"
  val textDarkGray: String = "-fx-text-fill: #555555"
  val textLightGray: String = "-fx-text-fill: #888888"
  val panelHeader: String = "-fx-background-color: #a9a9a9; -fx-padding: 5 10 5 10"
  val panelBody: String = "-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 0 1 1 1"
  val backgroundMain: String = "-fx-background-color: #e0e0e0"
  val textAreaStyle: String = s"$baseFont; -fx-background-color: transparent"
  val buttonGreen: String = "-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20"
  val buttonRed: String = "-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20"
  val progressBarHP: String = "-fx-accent: #4682b4"
  val progressBarMP: String = "-fx-accent: #9370db"
