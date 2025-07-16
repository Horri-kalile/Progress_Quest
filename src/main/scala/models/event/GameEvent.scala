package models.event

import controllers.{CombatController, MissionController, MonsterController, PlayerController}
import models.monster.Monster
import models.player.{EquipmentFactory, Item, ItemFactory, Player}
import models.world.World
import util.GameConfig

import scala.util.Random

/**
 * Enumeration of all possible event types that can occur during gameplay.
 * Each event type corresponds to a specific game mechanic or interaction.
 */
enum EventType:
  case fight, mission, changeWorld, training, restore, sell, special, gameOver

/**
 * Base trait for all game events that can occur during player adventures.
 * 
 * All events follow the same pattern: they take a player state and return
 * an updated player state, along with messages describing what happened
 * and optionally a monster for combat encounters.
 */
sealed trait GameEvent:
  /**
   * Executes the event's action on the player.
   *
   * @param player The current player state
   * @return Tuple of (updated player, event messages, optional monster)
   */
  def action(player: Player): (Player, List[String], Option[Monster])

/**
 * Event triggered after winning a combat encounter.
 * Handles loot distribution, experience gains, and gold rewards.
 */
case object FightEvent extends GameEvent:
  /**
   * Processes victory rewards from the last combat encounter.
   *
   * @param player The victorious player
   * @return Updated player with rewards and victory messages
   */
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    if !player.isAlive then return GameOverEvent.action(player)

    val monster = CombatController.lastMonster.get
    val (p1, equipMsg) = CombatController.handleEquipDrop(player, monster)
    val (p2, itemMsg) = CombatController.handleItemDrop(p1, monster)
    val withXp = PlayerController.gainXP(p2, MonsterController.getExpReward(monster))
    val finalPlayer = PlayerController.addGold(withXp, MonsterController.getGoldReward(monster))

    val summary = List(equipMsg, itemMsg, s"You earned ${monster.goldReward} gold and ${monster.experienceReward} XP.", "You have won!")

    (finalPlayer, summary, None)

/**
 * Event that handles mission system interactions.
 */
case object MissionEvent extends GameEvent:
  /**
   * Manages mission progression or creation.
   *
   * @param player The player to update with mission changes
   * @return Updated player with mission progress and status messages
   */
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    if player.activeMissions.nonEmpty && Random.nextBoolean() then
      val mission = Random.shuffle(player.activeMissions).head
      val msg = s"You progressed on mission: ${mission.description}"
      println(msg)
      (MissionController.progressMission(player, mission), List(msg), None)
    else
      val mission = MissionController.createRandomMission(player)
      val msg = s"You accepted a new mission: ${mission.description}"
      println(msg)
      (MissionController.addMission(player, mission), List(msg), None)

/**
 * Event that handles world zone transitions.
 * Moves the player to a new random zone different from their current location.
 */
case object ChangeWorldEvent extends GameEvent:
  /**
   * Transitions player to a new random world zone.
   *
   * @param player The player to move to a new zone
   * @return Updated player in new zone with transition message
   */
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    val newWorld = World.randomWorld(player.currentZone)
    val msg = s"Player has moved on another zone: +$newWorld"
    (PlayerController.changeWorld(player, newWorld), List(msg), None)

/**
 * Event that provides experience gain through training activities.
 * Gives experience points based on player level and random factors.
 */
case object TrainingEvent extends GameEvent:
  /**
   * Awards experience points from training activities.
   * Experience gained scales with player level.
   *
   * @param player The player to grant experience to
   * @return Updated player with experience gain and training message
   */
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    val exp = player.level * Random.between(1, 100)
    val msg = s"Training completed: +$exp EXP"
    println(msg)
    (PlayerController.gainXP(player, exp), List(msg), None)

/**
 * Event that fully restores player health and mana.
 * Provides a way for players to recover from damage without items.
 */
case object RestoreEvent extends GameEvent:
  /**
   * Fully restores player HP and MP to maximum values.
   *
   * @param player The player to restore
   * @return Fully restored player with recovery message
   */
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    val msg = "You rested and recovered fully."
    println(msg)
    (player.restore(), List(msg), None)

/**
 * Event that allows players to spend gold to permanently boost their attributes.
 */
case object PowerUpEvent extends GameEvent:
  /**
   * Checks if the player has enough gold for a power-up.
   *
   * @param player The player to check
   * @return true if player can afford the power-up cost
   */
  private def canPowerUp(player: Player): Boolean = player.gold >= GameConfig.powerUpCost * player.level

  /**
   * Attempts to power up player attributes using gold.
   * Cost increases with player level to maintain progression balance.
   *
   * @param player The player attempting to power up
   * @return Updated player with boosted stats or unchanged if insufficient gold
   */
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

/**
 * Event that handles selling inventory items for gold.
 */
case object SellEvent extends GameEvent:
  /**
   * Sells random items from player inventory and attempts power-ups.
   *
   * @param player The player selling items
   * @return Updated player with sold items converted to gold and possible power-ups
   */
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

/**
 * Event that triggers random special encounters with various outcomes.
 * Can result in blessings, curses, powerful monsters, treasures, or traps.
 */
case object SpecialEvent extends GameEvent:
  /**
   * Executes a random special encounter with multiple possible outcomes:
   * - Blessing/Curse: Level changes (±1 to ±3 levels)
   * - Powerful Monster: Rare equipment drop or game over
   * - Hidden Dungeon: Item discovery or deadly trap
   * - Village Help: Experience gain based on wisdom
   * - Theft: Random items stolen from inventory
   *
   * @param player The player experiencing the special event
   * @return Updated player state with event outcome and descriptive messages
   */
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

/**
 * Event that handles game over conditions.
 * Sets player HP to 0 and provides game over message.
 */
case object GameOverEvent extends GameEvent:
  /**
   * Triggers game over state by setting player HP to 0.
   *
   * @param player The player to set as game over
   * @return Player with 0 HP and game over message
   */
  override def action(player: Player): (Player, List[String], Option[Monster]) =
    val msg = "GAME OVER."
    (player.withCurrentHp(0), List(msg), None)

/**
 * Factory object for creating and executing game events.
 */
object EventFactory:
  /**
   * Creates and executes the appropriate event based on the event type.
   *
   * @param eventType The type of event to execute
   * @param player The player to apply the event to
   * @return Tuple of (updated player, formatted messages, optional monster)
   */
  def executeEvent(eventType: EventType, player: Player): (Player, List[String], Option[Monster]) =
    val event: GameEvent = eventType match
      case EventType.fight => FightEvent
      case EventType.mission => MissionEvent
      case EventType.changeWorld => ChangeWorldEvent
      case EventType.training => TrainingEvent
      case EventType.restore => RestoreEvent
      case EventType.sell => SellEvent
      case EventType.special => SpecialEvent
      case EventType.gameOver => GameOverEvent

    val (updatedPlayer, messages, result) = event.action(player)
    val header = s"${eventType.toString.capitalize} Event triggered:"
    (updatedPlayer, header +: messages, result)

