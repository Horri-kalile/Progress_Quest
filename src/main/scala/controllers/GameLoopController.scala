package controllers

object GameLoopController {
  def tick(player: Player, monster: Monster, mission: Mission): Player = {
    val (p1, msg1) = MissionController.start(player, mission)
    println(msg1)

    // Battle
    val (pAfterAttack, mAfterAttack, msg2) = BattleController.attack(player, monster)
    println(msg2)

    val (pAfterDamage, msg3) = BattleController.takeDamage(pAfterAttack, monster.strength)
    println(msg3)

    // End of the mission
    val (pFinal, msg4) = MissionController.complete(pAfterDamage, mission)
    println(msg4)

    // Gain XP
    val (pWithXP, msg5) = BattleController.gainXP(pFinal, mission.xpReward) // Correction: gainXP au lieu de bainXP
    println(msg5)

    pWithXP
  }

  def run(): Unit = {
    var player = Player("ZeroMan", 100, 0, 1)
    val mission = Mission("Hunt the goblins", 50)
    val monster = Monster("Goblin", 30, 5)

    for (i <- 0 until 3) { 
      println(s"--- Tick $i ---") 
      player = tick(player, monster, mission)
      println(s"État final: ${player}")
    }
  }
}