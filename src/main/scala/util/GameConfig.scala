package util

protected[util] object GameConfig:
  val baseSpecialChance: Double = 0.02 // 2%
  val specialBonusPerLucky: Double = 0.001 // 0.1% per lucky
  val maxSpecialChance: Double = 0.09 // 9%

  val baseGameOverChance: Double = 0.05 // 5%
  val minGameOverChance: Double = 0.01
