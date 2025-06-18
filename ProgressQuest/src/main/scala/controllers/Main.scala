package controllers

import controllers.{BattleController, GameLoopController, MonsterController, Player}

object Main {
  def main(args: Array[String]): Unit = {
    println("Démarrage du test...")

    val player = Player("Hero", 100, 0, 1)
    val monster = MonsterController.createMonster("Goblin", 50, 10)
    val mission = Mission("fight gobelin", 50)

    val (_, _, msg) = BattleController.attack(player, monster)
    println(msg)

    GameLoopController.run()


    val (updatedPlayer, decision) = MissionController.proposeMission(player, mission)

    decision match {
      case MissionDecision.Accept => println("Test OK: accepted")
      case MissionDecision.Refuse => println("Test OK: refused")
  } }
}
