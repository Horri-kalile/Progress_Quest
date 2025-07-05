package models.event

import models.player.{EquipmentFactory, Item, Player}
import scala.util.Random

enum EventType:
  case fight, mission, training, restore, sell, special, gameOver

sealed trait GameEvent:
  def action(player: Player): Player

case object FightEvent extends GameEvent:
  override def action(player: Player): Player = {
    val monster = controllers.CombatController.getRandomMonster(player.level)
    val (updatedPlayer, combatLog) = controllers.CombatController.simulateFight(player, monster)
    
    println(combatLog)
    
    if (updatedPlayer.hp <= 0) then
      GameOverEvent.action(updatedPlayer)
    else
      updatedPlayer
  }

case object MissionEvent extends GameEvent:
  override def action(player: Player): Player =
    if player.activeMissions.nonEmpty && Random.nextBoolean() then
      val mission = Random.shuffle(player.activeMissions).head
      println(s"You progressed on mission: ${mission.description}")
      player.progressMission(mission)
    else
      val mission = MissionFactory.random()
      println(s"You accepted a new mission: ${mission.description}")
      player.addMission(mission)

case object TrainingEvent extends GameEvent:
  override def action(player: Player): Player =
    val exp = Random.between((player.exp * 0.25).toInt, (player.exp * 0.5).toInt + 1)
    println(s"Training completed: +$exp EXP")
    player.gainExp(exp)

case object RestoreEvent extends GameEvent:
  override def action(player: Player): Player =
    println("You rested and recovered fully.")
    player.restore()

case object PowerUpEvent extends GameEvent:
  private val cost = 100

  def canPowerUp(player: Player): Boolean = player.gold >= cost

  override def action(player: Player): Player =
    if canPowerUp(player) then
      println(s"You spend $cost gold to power up your stats!")
      player.spendGold(cost).map(_.powerUp()).getOrElse(player)
    else player

case object SellEvent extends GameEvent:
  override def action(player: Player): Player =
    val itemCountToSell = Random.between(1, player.inventorySize + 1)
    val (finalPlayer, messages) = (1 to itemCountToSell).foldLeft((player, List.empty[String])) {
      case ((p, logs), _) =>
        val (updated, msg) = p.sellItem()
        (updated, logs :+ msg)
    }

    messages.foreach(println)

    PowerUpEvent.action(finalPlayer)


case object SpecialEvent extends GameEvent:
  override def action(player: Player): Player =
    Random.nextInt(8) match
      case 0 =>
        val change = Random.between(1, 4)
        if Random.nextBoolean() then
          val newPlayer = (1 to change).foldLeft(player)((p, _) => p.levelUp())
          println(s"Blessing! You leveled up $change times.")
          newPlayer
        else
          val newLevel = math.max(1, player.level - change)
          println(s"Curse! You lost $change levels.")
          player.copy(level = newLevel)

      case 1 =>
        val eq = EquipmentFactory.generateRandomEquipment(probabilityDrop = 1.0, playerLevel = player.level)
        println("You defeated a powerful monster!")
        println(s"You looted a rare item: ${eq.get.name}")
        player.replaceEquipment(eq.get)

      case 2 =>
        println("You were defeated by a powerful monster. Game over!")
        GameOverEvent.action(player)

      case 3 =>
        val eq = EquipmentFactory.generateRandomEquipment(probabilityDrop = 1.0, playerLevel = player.level)
        println("You discovered a hidden dungeon and found rare equipment!")
        println(s"You found: ${eq.get.name}")
        player.replaceEquipment(eq.get)

      case 4 =>
        println("You were injured in a dungeon trap! HP and MP halved.")
        player.copy(hp = math.max(1, player.hp / 2), mp = math.max(0, player.mp / 2))

      case 5 =>
        val gain = Random.between(50, 151)
        println(s"You helped villagers and gained $gain EXP.")
        player.gainExp(gain)

      case 6 =>
        println("It was a trap! You were killed. Game over!")
        GameOverEvent.action(player)

      case 7 =>
        val result = player.stealFromInventory()
        println(result)
        player

case object GameOverEvent extends GameEvent:
  override def action(player: Player): Player =
    println("GAME OVER.")
    player.copy(hp = 0)

object EventFactory:
  def executeEvent(eventType: EventType, player: Player): Player =
    val event: GameEvent = eventType match
      case EventType.fight => FightEvent
      case EventType.mission => MissionEvent
      case EventType.training => TrainingEvent
      case EventType.restore => RestoreEvent
      case EventType.sell => SellEvent
      case EventType.special => SpecialEvent
      case EventType.gameOver => GameOverEvent
    event.action(player)
