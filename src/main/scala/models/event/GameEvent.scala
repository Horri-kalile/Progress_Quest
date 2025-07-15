package models.event

import controllers.{CombatController, MissionController, MonsterController, PlayerController}
import models.monster.Monster
import models.player.{EquipmentFactory, Item, ItemFactory, Player}
import util.GameConfig

import scala.util.Random

enum EventType:
  case fight, mission, training, restore, sell, special, gameOver

sealed trait GameEvent:
  def action(player: Player): (Player, List[String], Option[Monster])

case object FightEvent extends GameEvent:
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    if !player.isAlive then return GameOverEvent.action(player)

    val monster = CombatController.lastMonster.get
    val (p1, equipMsg) = CombatController.handleEquipDrop(player, monster)
    val (p2, itemMsg) = CombatController.handleItemDrop(p1, monster)
    val withXp = PlayerController.gainXP(p2, MonsterController.getExpReward(monster))
    val finalPlayer = PlayerController.addGold(withXp, MonsterController.getGoldReward(monster))

    val summary = List(equipMsg, itemMsg, s"You earned ${monster.goldReward} gold and ${monster.experienceReward} XP.", "You have won!")

    (finalPlayer, summary, None)


case object MissionEvent extends GameEvent:
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    if player.activeMissions.nonEmpty && Random.nextBoolean() then
      val mission = Random.shuffle(player.activeMissions).head
      val msg = s"You progressed on mission: ${mission.description}"
      println(msg)
      (MissionController.progressMission(player, mission), List(msg), None)
    else
      val mission = MissionFactory.randomMission()
      val msg = s"You accepted a new mission: ${mission.description}"
      println(msg)
      (MissionController.addMission(player, mission), List(msg), None)

case object TrainingEvent extends GameEvent:
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    val exp = player.level * Random.between(1, 100)
    val msg = s"Training completed: +$exp EXP"
    println(msg)
    (PlayerController.gainXP(player, exp), List(msg), None)

case object RestoreEvent extends GameEvent:
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    val msg = "You rested and recovered fully."
    println(msg)
    (player.restore(), List(msg), None)

case object PowerUpEvent extends GameEvent:

  private def canPowerUp(player: Player): Boolean = player.gold >= GameConfig.powerUpCost * player.level

  override def action(player: Player): (Player, List[String], Option[Monster]) =
    val cost = GameConfig.powerUpCost * player.level
    if canPowerUp(player) then
      val updated = player.withGold(cost).powerUpAttributes()
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
            val updated = p.withGold((p.gold + goldGained)).withInventory(newInventory)
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
          val newPlayer = (1 to change).foldLeft(player)((p, _) => PlayerController.levelUp(player))
          val msg = s"Blessing! You leveled up $change times."
          (newPlayer, List(msg), None)
        else
          val newLevel = math.max(1, player.level - change)
          val newPlayer = (1 to change).foldLeft(player)((p, _) => PlayerController.levelDown(player))
          val msg = s"Curse! You lost $change levels."
          (newPlayer, List(msg), None)

      case 1 =>
        val eq = EquipmentFactory.generateRandomEquipment(probabilityDrop = 1.0, player.attributes.lucky, playerLevel = player.level)
        val msg1 = "You defeated a powerful monster!"
        val msg2 = s"You looted a new equipment: ${eq.get.name}"
        val (updatedPlayer, msg3) = player.equipment.getOrElse(eq.get.slot, None) match
          case Some(old) if old.value >= eq.get.value =>
            val updated = PlayerController.addGold(player, eq.get.value)
            (updated, s"You found ${eq.get.name}, sold for ${eq.get.value} gold.")
          case _ =>
            val updated = PlayerController.equipmentOn(player, eq.get.slot, eq.get)
            (updated, s"You equipped: ${eq.get.name} (${eq.get.slot}).")
        (updatedPlayer, List(msg1, msg2, msg3), None)

      case 2 =>
        val msg = "You were defeated by a powerful monster. Game over!"
        val (finalPlayer, endMsgs, result) = GameOverEvent.action(player)
        (finalPlayer, msg :: endMsgs, result)

      case 3 =>
        val eq = ItemFactory.randomItem(player.attributes.lucky)
        val msg1 = "You discovered a hidden dungeon and found new item!"
        val msg2 = s"You found: ${eq.name}, ${eq.gold} , ${eq.rarity}"
        (PlayerController.addItem(player, eq), List(msg1, msg2), None)

      case 4 =>
        val msg = "You were injured in a dungeon trap! HP and MP halved."
        (PlayerController.playerInjured(player), List(msg), None)

      case 5 =>
        val gain = Random.between(50, 100) * player.level + player.attributes.wisdom
        val msg = s"You helped villagers and gained $gain EXP."
        println(msg)
        (PlayerController.gainXP(player, gain), List(msg), None)

      case 6 =>
        val msg = "It was a trap! You were killed. Game over!"
        val (finalPlayer, endMsgs, result) = GameOverEvent.action(player)
        (finalPlayer, msg :: endMsgs, result)

      case 7 =>
        val (updatedPlayer, msg): (Player, String) = PlayerController.stealRandomItem(player)
        (updatedPlayer, List(msg), None)

case object GameOverEvent extends GameEvent:
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    val msg = "GAME OVER."
    (player.withCurrentHp(0), List(msg), None)

object EventFactory:
  def executeEvent(eventType: EventType, player: Player): (Player, List[String], Option[Monster]) =
    val event: GameEvent = eventType match
      case EventType.fight => FightEvent
      case EventType.mission => MissionEvent
      case EventType.training => TrainingEvent
      case EventType.restore => RestoreEvent
      case EventType.sell => SellEvent
      case EventType.special => SpecialEvent
      case EventType.gameOver => GameOverEvent

    val (updatedPlayer, messages, result) = event.action(player)
    val header = s"${eventType.toString.capitalize} Event triggered:"
    (updatedPlayer, header +: messages, result)

