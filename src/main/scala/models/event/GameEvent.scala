package models.event

import controllers.PlayerController
import models.monster.Monster
import models.player.{EquipmentFactory, Item, Player}

import scala.util.Random

enum EventType:
  case fight, mission, training, restore, sell, special, gameOver

sealed trait GameEvent:
  def action(player: Player): (Player, List[String], Option[Monster])

case object FightEvent extends GameEvent:
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    val monster = controllers.CombatController.getRandomMonsterForZone(player.level, player.currentZone)
    val (updatedPlayer, combatLog) = controllers.CombatController.simulateFight(player, monster)
    println(combatLog)
    val messages = combatLog.split("\n").toList

    if updatedPlayer.currentHp <= 0 then
      val (finalPlayer, endMsgs, result) = GameOverEvent.action(updatedPlayer)
      (finalPlayer, messages ++ endMsgs, result)
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
    val minExp = math.max(1, (player.exp * 0.25).toInt)
    val maxExp = math.max(minExp + 1, (player.exp * 0.5).toInt)
    val exp = Random.between(minExp, maxExp)
    val msg = s"Training completed: +$exp EXP"
    println(msg)
    (PlayerController.gainXP(player, exp), List(msg), None)

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
      case 0 => // Blessing/Curse
        import view.SpecialEventDialog
        SpecialEventDialog.showBlessingCurseDialog() match
          case Some(true) => // Player chose to pray
            val change = Random.between(1, 4)
            if Random.nextBoolean() then
              val newPlayer = (1 to change).foldLeft(player)((p, _) => p.levelUp())
              val msg = s"Blessing! You leveled up $change times."
              println(msg)
              (newPlayer, List(msg), None)
            else
              val newLevel = math.max(1, player.level - change)
              val newPlayer = (1 to change).foldLeft(player)((p, _) => p.levelDown())
              val msg = s"Curse! You lost $change levels."
              println(msg)
              (newPlayer, List(msg), None)
          case Some(false) => // Player chose to ignore
            val msg = "You ignored the shrine and continued on your path."
            (player, List(msg), None)
          case None => // Timed out
            val msg = "You hesitated too long and the shrine vanished. You continued on your path."
            (player, List(msg), None)

      case 1 => // Powerful Monster
        import view.SpecialEventDialog
        SpecialEventDialog.showPowerfulMonsterDialog() match
          case Some(true) => // Player chose to fight
            val eq = EquipmentFactory.generateRandomEquipment(probabilityDrop = 1.0, playerLevel = player.level)
            val msg1 = "You defeated a powerful monster!"
            val msg2 = s"You looted a rare item: ${eq.get.name}"
            println(msg1)
            println(msg2)
            (player.replaceEquipment(eq.get), List(msg1, msg2), None)
          case Some(false) => // Player chose to flee
            val msg = "You fled from the powerful monster and continued safely."
            println(msg)
            (player, List(msg), None)
          case None => // Timed out
            val msg = "You hesitated too long! The monster attacked, but you managed to escape."
            println(msg)
            (player, List(msg), None)

      case 2 => // Powerful Monster Defeat (Game Over)
        import view.SpecialEventDialog
        SpecialEventDialog.showGameOverMonsterDialog()
        val msg = "You were defeated by a powerful monster. Game over!"
        println(msg)
        val (finalPlayer, endMsgs, result) = GameOverEvent.action(player)
        (finalPlayer, msg :: endMsgs, result)

      case 3 => // Hidden Dungeon
        import view.SpecialEventDialog
        SpecialEventDialog.showHiddenDungeonDialog() match
          case Some(true) => // Player chose to explore
            val eq = EquipmentFactory.generateRandomEquipment(probabilityDrop = 1.0, playerLevel = player.level)
            val msg1 = "You discovered a hidden dungeon and found rare equipment!"
            val msg2 = s"You found: ${eq.get.name}"
            println(msg1)
            println(msg2)
            (player.replaceEquipment(eq.get), List(msg1, msg2), None)
          case Some(false) => // Player chose to leave
            val msg = "You decided not to explore the dangerous dungeon and continued safely."
            println(msg)
            (player, List(msg), None)
          case None => // Timed out
            val msg = "You hesitated too long and the dungeon entrance collapsed. You continued on your path."
            println(msg)
            (player, List(msg), None)

      case 4 => // Dungeon Trap
        import view.SpecialEventDialog
        SpecialEventDialog.showDungeonTrapDialog()
        val msg = "You were injured in a dungeon trap! HP and MP halved."
        println(msg)
        (player.copy(hp = math.max(1, player.currentHp / 2), mp = math.max(0, player.currentMp / 2)), List(msg), None)

      case 5 => // Help Villagers
        import view.SpecialEventDialog
        SpecialEventDialog.showVillagerHelpDialog() match
          case Some(true) => // Player chose to help
            val gain = Random.between(50, 151) * (1 + (player.attributes.wisdom / 100))
            val msg = s"You helped villagers and gained $gain EXP."
            println(msg)
            (PlayerController.gainXP(player, gain), List(msg), None)
          case Some(false) => // Player chose to ignore
            val msg = "You ignored the villagers and continued on your way."
            println(msg)
            (player, List(msg), None)
          case None => // Timed out
            val msg = "You hesitated too long and the villagers found someone else to help them."
            println(msg)
            (player, List(msg), None)

      case 6 => // Deadly Trap (Game Over)
        import view.SpecialEventDialog
        SpecialEventDialog.showGameOverTrapDialog()
        val msg = "It was a trap! You were killed. Game over!"
        println(msg)
        val (finalPlayer, endMsgs, result) = GameOverEvent.action(player)
        (finalPlayer, msg :: endMsgs, result)

      case 7 => // Theft
        import view.SpecialEventDialog
        SpecialEventDialog.showTheftDialog()
        val result = player.stealFromInventory()
        println(result)
        (player, List(result), None)

case object GameOverEvent extends GameEvent:
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    val msg = "GAME OVER."
    println(msg)
    (player.copy(currentHp = 0), List(msg), None)

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

