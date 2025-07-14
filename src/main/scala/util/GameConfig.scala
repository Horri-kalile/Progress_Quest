package util

object GameConfig:
  val baseSpecialChance: Double = 0.02 // 2%
  val baseDropChance: Double = 0.30 // 90%
  val baseLearnSkillChance: Double = 0.30
  val specialBonusPerLucky: Double = 0.001 // 0.1% per lucky
  val maxSpecialChance: Double = 0.09 // 9%
  val maxDropChance: Double = 1.00
  val baseGameOverChance: Double = 0.02 // 2%
  val minGameOverChance: Double = 0.01
  val maxBuffByZone: Double = 0.20 // 20% maximum buff
  val probabilityToMissAttackBonus: Double = 0.001

