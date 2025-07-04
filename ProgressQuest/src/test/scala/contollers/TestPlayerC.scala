package test.scala.contollers

import controllers._
import models.player.*


object TestPlayerC extends App {

  // === Crée un Player de départ ===
  val initialPlayer = Player(
    name = "Hero",
    identity = Identity("Hero", Race.Human, ClassType.Warrior),
    level = 1,
    exp = 0,
    hp = 100,
    mp = 30,
    baseAttributes = Attributes(10, 10, 10,10, 10, 10),
    behaviorType = BehaviorType.Aggressive,  // Exemple d'un BehaviorType concret
    inventory = Map.empty,
    equipment = Map.empty,
    skills = List.empty,
    missions = List.empty,
    gold = 50
  )

  println(s"Player initial : $initialPlayer")

  // === Test: Changer le behavior ===
  val newBehavior = BehaviorType.Defensive
  val updatedPlayer = PlayerController.changeBehavior(initialPlayer, newBehavior)

  println(s"Player après changement de behavior : $updatedPlayer")
}
