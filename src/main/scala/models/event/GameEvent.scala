package models.event

import models.monster.Monster
import models.player.{EquipmentFactory, Item, Player}

import scala.util.Random

enum EventType:
  case fight, mission, training, restore, sell, special, gameOver

sealed trait GameEvent:
  def action(player: Player): (Player, List[String], Option[Monster])

case object FightEvent extends GameEvent:
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    val monster = controllers.CombatController.getRandomMonster(player.level)
    val (updatedPlayer, combatLog) = controllers.CombatController.simulateFight(player, monster)
    println(combatLog)
    val messages = combatLog.split("\n").toList

    if updatedPlayer.currentHp <= 0 then
      val (finalPlayer, endMsgs) = GameOverEvent.action(updatedPlayer)
      (finalPlayer, messages ++ endMsgs, Some(monster))
    else
      (updatedPlayer, messages, Some(monster))

case object MissionEvent extends GameEvent:
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    if player.activeMissions.nonEmpty && Random.nextBoolean() then
      val mission = Random.shuffle(player.activeMissions).head
      val msg = s"You progressed on mission: ${mission.description}"
      println(msg)
      (player.progressMission(mission), List(msg), None)
    else
      val mission = MissionFactory.randomMission()
      val msg = s"You accepted a new mission: ${mission.description}"
      println(msg)
      (player.addMission(mission), List(msg), None)

case object TrainingEvent extends GameEvent:
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    val exp = Random.between((player.exp * 0.25).toInt, (player.exp * 0.5).toInt)
    val msg = s"Training completed: +$exp EXP"
    println(msg)
    (player.gainExp(exp), List(msg), None)

case object RestoreEvent extends GameEvent:
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    val msg = "You rested and recovered fully."
    println(msg)
    (player.restore(), List(msg), None)

case object PowerUpEvent extends GameEvent:
  private val cost = 100

  private def canPowerUp(player: Player): Boolean = player.gold >= cost

  override def action(player: Player): (Player, List[String], Option[Monster]) =
    if canPowerUp(player) then
      val updated = player.spendGold(cost).map(_.powerUpAttributes()).getOrElse(player)
      val msg = s"You spend $cost gold to power up your stats!"
      println(msg)
      (updated, List(msg), None)
    else
      val msg = "You don't have enough gold to power up your stats!"
      (player, List(msg), None)

case object SellEvent extends GameEvent:
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    if player.inventory.isEmpty then
      (player, List("Inventory empty. Nothing to sell."), None)
    else
      val items = player.inventory.keys.toList
      val countToSell = Random.between(1, items.size + 1)
      val itemsToSell = Random.shuffle(items).take(countToSell)

      val (updatedPlayer, messages) = itemsToSell.foldLeft((player, List.empty[String])) {
        case ((p, logs), item) =>
          val qty = p.inventory.getOrElse(item, 0)
          if qty > 0 then
            val goldGained = item.gold * qty
            val newInventory = p.inventory - item
            val updated = p.copy(inventory = newInventory, gold = p.gold + goldGained)
            val msg = s"Sold $qty × ${item.name} for $goldGained gold."
            (updated, logs :+ msg)
          else (p, logs)
      }

      val (finalPlayer, powerUpMsgs, result) = PowerUpEvent.action(updatedPlayer)
      (finalPlayer, messages ++ powerUpMsgs, result)

case object SpecialEvent extends GameEvent:
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    Random.nextInt(8) match
      case 0 =>
        val change = Random.between(1, 4)
        if Random.nextBoolean() then
          val newPlayer = (1 to change).foldLeft(player)((p, _) => p.levelUp())
          val msg = s"Blessing! You leveled up $change times."
          println(msg)
          (newPlayer.powerUpAttributes(), List(msg), None)
        else
          val newLevel = math.max(1, player.level - change)
          val newPlayer = player.powerDownAttributes(newLevel)
          val msg = s"Curse! You lost $change levels."
          println(msg)
          (newPlayer.copy(level = newLevel, exp = 0), List(msg), None)

      case 1 =>
        val eq = EquipmentFactory.generateRandomEquipment(probabilityDrop = 1.0, playerLevel = player.level)
        val msg1 = "You defeated a powerful monster!"
        val msg2 = s"You looted a rare item: ${eq.get.name}"
        println(msg1)
        println(msg2)
        (player.replaceEquipment(eq.get), List(msg1, msg2))

      case 2 =>
        val msg = "You were defeated by a powerful monster. Game over!"
        println(msg)
        val (finalPlayer, endMsgs) = GameOverEvent.action(player)
        (finalPlayer, msg :: endMsgs)

      case 3 =>
        val eq = EquipmentFactory.generateRandomEquipment(probabilityDrop = 1.0, playerLevel = player.level)
        val msg1 = "You discovered a hidden dungeon and found rare equipment!"
        val msg2 = s"You found: ${eq.get.name}"
        println(msg1)
        println(msg2)
        (player.replaceEquipment(eq.get), List(msg1, msg2))

      case 4 =>
        val msg = "You were injured in a dungeon trap! HP and MP halved."
        println(msg)
        (player.copy(hp = math.max(1, player.hp / 2), mp = math.max(0, player.mp / 2)), List(msg))

      case 5 =>
        val gain = Random.between(50, 151)
        val msg = s"You helped villagers and gained $gain EXP."
        println(msg)
        (player.gainExp(gain), List(msg))

      case 6 =>
        val msg = "It was a trap! You were killed. Game over!"
        println(msg)
        val (finalPlayer, endMsgs) = GameOverEvent.action(player)
        (finalPlayer, msg :: endMsgs)

      case 7 =>
        val result = player.stealFromInventory()
        println(result)
        (player, List(result))

case object GameOverEvent extends GameEvent:
  override def action(player: Player): (Player, List[String]) =
    val msg = "GAME OVER."
    println(msg)
    (player.copy(currentHp = 0), List(msg))

object EventFactory:
  def executeEvent(eventType: EventType, player: Player): (Player, List[String]) =
    val event: GameEvent = eventType match
      case EventType.fight => FightEvent
      case EventType.mission => MissionEvent
      case EventType.training => TrainingEvent
      case EventType.restore => RestoreEvent
      case EventType.sell => SellEvent
      case EventType.special => SpecialEvent
      case EventType.gameOver => GameOverEvent

    val (updatedPlayer, messages) = event.action(player)
    val header = s"${eventType.toString.capitalize} Event triggered:"
    (updatedPlayer, header +: messages)

