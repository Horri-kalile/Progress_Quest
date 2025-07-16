package models.event

import controllers.{CombatController, MissionController, MonsterController, PlayerController}
import models.monster.Monster
import models.player.{EquipmentFactory, ItemFactory, Player}
import models.world.World
import util.GameConfig
import view.SpecialEventDialog

import scala.util.Random

/** The GameEventModule contains the event system for player interactions during the game simulation.
 *
 * It provides:
 *   - The [[GameEvent]] trait representing a single type of game interaction
 *   - The [[EventType]] enumeration for all possible event kinds
 *   - The [[GameEventFactory]] for executing and dispatching events
 */
object GameEventModule:

  /** Enumeration of supported event types that can occur in the game.
   *
   * - `fight`: A battle against a monster
   * - `mission`: Accepting or progressing a mission
   * - `changeWorld`: Moving to a new zone
   * - `training`: Passive XP gain
   * - `restore`: Full player restoration
   * - `sell`: Selling items from inventory
   * - `special`: Randomized, unpredictable event
   * - `gameOver`: Ends the game
   */
  enum EventType:
    case fight, mission, changeWorld, training, restore, sell, special, gameOver

  /** A game event represents a single action that may modify the player's state.
   *
   * Each event implementation is immutable and returns a tuple:
   *   - The updated player state
   *   - A list of summary messages describing the outcome
   *   - Optionally, a monster involved in the event
   */
  private trait GameEvent:
    def action(player: Player): (Player, List[String], Option[Monster])

  /** Factory and dispatcher for game events.
   *
   * Provides access to the correct [[GameEvent]] implementation based on an [[EventType]].
   */
  object GameEventFactory:

    /** Executes the appropriate [[GameEvent]] based on the provided [[EventType]].
     *
     * @param eventType The type of event to execute (e.g. `fight`, `mission`, etc.)
     * @param player    The player affected by the event
     * @return A tuple of the updated player, a list of event messages, and an optional monster (if relevant)
     */
    def executeEvent(eventType: EventType, player: Player): (Player, List[String], Option[Monster]) =
      val event = resolve(eventType)
      val (updated, messages, result) = event.action(player)
      val header = s"${eventType.toString.capitalize} Event triggered:"
      (updated, header +: messages, result)

    /** Test-only entry point to directly trigger a specific SpecialEvent case by index.
     *
     * This method is intended solely for unit testing to ensure all branches of
     * [[SpecialEvent]] behave correctly. It should never be used in production logic.
     *
     * @param player    the player involved in the event
     * @param caseIndex the specific branch (0–7) to execute within [[SpecialEvent]]
     * @return a tuple with updated player, messages, and optional monster
     */
    def testSpecialCase(player: Player, caseIndex: Int): (Player, List[String], Option[Monster]) =
      SpecialEvent.actionWithCase(player, caseIndex)

    /** Resolves the internal [[GameEvent]] implementation for the given [[EventType]]. */
    private def resolve(eventType: EventType): GameEvent = eventType match
      case EventType.fight => FightEvent
      case EventType.mission => MissionEvent
      case EventType.changeWorld => ChangeWorldEvent
      case EventType.training => TrainingEvent
      case EventType.restore => RestoreEvent
      case EventType.sell => SellEvent
      case EventType.special => SpecialEvent
      case EventType.gameOver => GameOverEvent

  // ---------------------------
  // Internal Game Event Implementations
  // ---------------------------

  /** Simulates a fight between the player and the last encountered monster.
   * Rewards XP and gold, and possibly drops equipment or items.
   */
  private case object FightEvent extends GameEvent:
    def action(player: Player): (Player, List[String], Option[Monster]) =
      if !player.isAlive then GameOverEvent.action(player)
      else
        val monster = CombatController.lastMonster.get
        val (p1, eqMsg) = CombatController.handleEquipDrop(player, monster)
        val (p2, itemMsg) = CombatController.handleItemDrop(p1, monster)
        val pWithXp = PlayerController.gainXP(p2, MonsterController.getExpReward(monster))
        val finalP = PlayerController.addGold(pWithXp, MonsterController.getGoldReward(monster))
        val summary = List(eqMsg, itemMsg, s"You earned ${monster.goldReward} gold and ${monster.experienceReward} XP.", "You have won!")
        (finalP, summary, None)

  /** Manages mission progression or generates a new random mission. */
  private case object MissionEvent extends GameEvent:
    def action(player: Player): (Player, List[String], Option[Monster]) =
      if player.activeMissions.nonEmpty && Random.nextBoolean() then
        val m = Random.shuffle(player.activeMissions).head
        val msg = s"You progressed on mission: ${m.description}"
        (MissionController.progressMission(player, m), List(msg), None)
      else
        val newMission = MissionController.createRandomMission(player)
        val msg = s"You accepted a new mission: ${newMission.description}"
        (MissionController.addMission(player, newMission), List(msg), None)

  /** Moves the player to a new world zone randomly. */
  private case object ChangeWorldEvent extends GameEvent:
    def action(player: Player): (Player, List[String], Option[Monster]) =
      val newWorld = World.randomWorld(player.currentZone)
      val msg = s"Player has moved to a new zone: $newWorld"
      (PlayerController.changeWorld(player, newWorld), List(msg), None)

  /** Provides XP to the player as a result of passive training. */
  private case object TrainingEvent extends GameEvent:
    def action(player: Player): (Player, List[String], Option[Monster]) =
      val exp = player.level * Random.between(1, 100)
      val msg = s"Training completed: +$exp EXP"
      (PlayerController.gainXP(player, exp), List(msg), None)

  /** Fully restores the player's HP and MP. */
  private case object RestoreEvent extends GameEvent:
    def action(player: Player): (Player, List[String], Option[Monster]) =
      val msg = "You rested and recovered fully."
      (player.restore(), List(msg), None)

  /** Powers up the player's attributes if they can afford it. */
  private case object PowerUpEvent extends GameEvent:

    /** Determines if the player has enough gold to power up. */
    private def canPowerUp(player: Player) =
      player.gold >= GameConfig.powerUpCost * player.level

    def action(player: Player): (Player, List[String], Option[Monster]) =
      val cost = GameConfig.powerUpCost * player.level
      if canPowerUp(player) then
        val updated = player.withGold(cost).powerUpAttributes()
        val msg = s"You spent $cost gold to power up your stats!"
        (updated, List(msg), None)
      else
        (player, List("Not enough gold to power up!"), None)

  /** Sells a random selection of inventory items for gold. May trigger a PowerUp. */
  private case object SellEvent extends GameEvent:
    def action(player: Player): (Player, List[String], Option[Monster]) =
      if player.inventory.isEmpty then (player, List("Inventory empty. Nothing to sell."), None)
      else
        val items = player.inventory.keys.toList
        val itemsToSell = Random.shuffle(items).take(Random.between(1, items.size + 1))
        val (pAfterSell, sellMsgs) = itemsToSell.foldLeft((player, List.empty[String])) {
          case ((p, logs), item) =>
            val qty = p.inventory(item)
            val gold = item.gold * qty
            val updated = p.withGold(p.gold + gold).withInventory(p.inventory - item)
            val msg = s"Sold $qty × ${item.name} for $gold gold."
            (updated, logs :+ msg)
        }
        val (pAfterPowerUp, msgs, r) = PowerUpEvent.action(pAfterSell)
        (pAfterPowerUp, sellMsgs ++ msgs, r)

  /** A special unpredictable event with 8 possible outcomes:
   *      1. Level up or down
   *         2. Loot rare equipment
   *         3. Game over from defeat
   *         4. Discover an item
   *         5. Take trap damage
   *         6. Gain XP by helping villagers
   *         7. Instant death trap
   *         8. Random item theft
   */
  private case object SpecialEvent extends GameEvent:

    override def action(player: Player): (Player, List[String], Option[Monster]) =
      val randomCase = Random.nextInt(8)
      actionWithCase(player, randomCase)

    def actionWithCase(player: Player, caseIndex: Int): (Player, List[String], Option[Monster]) = caseIndex match
      case 0 => // Blessing or Curse via dialog
        SpecialEventDialog.showBlessingCurseDialog() match
          case Some(true) => // Player chose to pray
            val change = Random.between(1, 3)
            if Random.nextBoolean() then
              val newPlayer = (1 to change).foldLeft(player)((p, _) => PlayerController.levelUp(p))
              val msg = s"Blessing! You leveled up $change times."
              (newPlayer, List(msg), None)
            else
              val change = Random.between(1, 4)
              val newPlayer = (1 to change).foldLeft(player)((p, _) => PlayerController.levelDown(p))
              val msg = s"Curse! You lost $change levels."
              (newPlayer, List(msg), None)
          case Some(false) => // Player ignored
            val msg = "You ignored the shrine and continued on your path."
            (player, List(msg), None)
          case None => // Timed out
            val msg = "You hesitated too long and the shrine vanished. You continued on your path."
            (player, List(msg), None)

      case 1 => // Powerful Monster dialog
        SpecialEventDialog.showPowerfulMonsterDialog() match
          case Some(true) =>
            EquipmentFactory.generateRandomEquipment(0.80, player.attributes.lucky, player.level) match
              case Some(newEq) =>
                val msg1 = "You defeated a powerful monster!"
                val msg2 = s"You looted a new equipment: ${newEq.name}"

                val maybeOld = player.equipment.get(newEq.slot)

                val (updatedPlayer, msg3) = maybeOld.get match
                  case Some(old) if old.value >= newEq.value =>
                    val updated = PlayerController.addGold(player, newEq.value)
                    (updated, s"You found ${newEq.name}, sold for ${newEq.value} gold.")
                  case _ =>
                    val updated = PlayerController.equipmentOn(player, newEq.slot, newEq)
                    (updated, s"You equipped: ${newEq.name} (${newEq.slot}).")

                (updatedPlayer, List(msg1, msg2, msg3), None)

              case None =>
                (player, List("You defeated a monster but found no loot."), None)

          case Some(false) => // Player fled
            val msg = "You fled from the powerful monster and continued safely."
            (player, List(msg), None)
          case None => // Timed out
            val msg = "You hesitated too long! The monster attacked, but you managed to escape."
            (player, List(msg), None)

      case 2 => // Powerful Monster Defeat (Game Over)
        SpecialEventDialog.showGameOverMonsterDialog()
        val msg = "You were defeated by a powerful monster. Game over!"
        val (deadPlayer, msgs, result) = GameOverEvent.action(player)
        (deadPlayer, msg :: msgs, result)

      case 3 => // Hidden Dungeon dialog
        SpecialEventDialog.showHiddenDungeonDialog() match
          case Some(true) => // Player chose to explore
            val item = ItemFactory.randomItem(player.attributes.lucky)
            val msg1 = "You discovered a hidden dungeon and found a new item!"
            val msg2 = s"You found: ${item.name}, worth ${item.gold}, rarity: ${item.rarity}"
            (PlayerController.addItem(player, item), List(msg1, msg2), None)
          case Some(false) => // Player left
            val msg = "You decided not to explore the dangerous dungeon and continued safely."
            (player, List(msg), None)
          case None => // Timed out
            val msg = "You hesitated too long and the dungeon entrance collapsed. You continued on your path."
            (player, List(msg), None)

      case 4 => // Dungeon Trap event
        import view.SpecialEventDialog
        SpecialEventDialog.showDungeonTrapDialog()
        val msg = "You were injured in a dungeon trap! HP and MP halved."
        (PlayerController.playerInjured(player), List(msg), None)

      case 5 => // Help Villagers dialog
        SpecialEventDialog.showVillagerHelpDialog() match
          case Some(true) => // Player helped
            val gain = Random.between(50, 151) * (1 + (player.attributes.wisdom / 100))
            val msg = s"You helped villagers and gained $gain EXP."
            (PlayerController.gainXP(player, gain), List(msg), None)
          case Some(false) => // Player ignored
            val msg = "You ignored the villagers and continued on your way."
            (player, List(msg), None)
          case None => // Timed out
            val msg = "You hesitated too long and the villagers found someone else to help them."
            (player, List(msg), None)

      case 6 => // Deadly Trap (Game Over)
        SpecialEventDialog.showGameOverTrapDialog()
        val msg = "It was a trap! You were killed. Game over!"
        val (deadPlayer, msgs, result) = GameOverEvent.action(player)
        (deadPlayer, msg :: msgs, result)

      case 7 => // Theft event
        import view.SpecialEventDialog
        SpecialEventDialog.showTheftDialog()
        val (updatedPlayer, msg) = PlayerController.stealRandomItem(player)
        (updatedPlayer, List(msg), None)

      case _ => // fallback
        (player, List("Nothing happened."), None)


  /** Marks the player as dead and ends the game. */
  private case object GameOverEvent extends GameEvent:
    def action(player: Player): (Player, List[String], Option[Monster]) =
      val msg = "GAME OVER."
      (player.withCurrentHp(0), List(msg), None)
