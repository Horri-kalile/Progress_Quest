package models.event

import controllers.{CombatController, MissionController, MonsterController, PlayerController}
import models.monster.Monster
import models.player.EquipmentModule.{Equipment, EquipmentFactory, EquipmentSlot}
import models.player.ItemModule.ItemFactory
import models.player.{Player, SkillFactory}
import models.world.World
import util.{GameConfig, RandomFunctions}
import view.SpecialEventDialog

import scala.annotation.tailrec
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
    *   - `fight`: A battle against a monster
    *   - `mission`: Accepting or progressing a mission
    *   - `changeWorld`: Moving to a new zone
    *   - `training`: Passive XP gain
    *   - `restore`: Full player restoration
    *   - `power`: Power up player stats if he has enough gold
    *   - `sell`: Selling items from inventory
    *   - `special`: Randomized, unpredictable event
    *   - `craft`: Craft new equip or power up an old one
    *   - `magic`: Learn new skill or power up an existing one
    *   - `gameOver`: Ends the game
    */
  enum EventType:
    case fight, mission, changeWorld, training, restore, power, sell, special, craft, magic, gameOver

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
      * @param eventType
      *   The type of event to execute (e.g. `fight`, `mission`, etc.)
      * @param player
      *   The player affected by the event
      * @return
      *   A tuple of the updated player, a list of event messages, and an optional monster (if relevant)
      */
    def executeEvent(eventType: EventType, player: Player): (Player, List[String], Option[Monster]) =
      val event = resolve(eventType)
      val (updated, messages, result) = event.action(player)
      val header = s"${eventType.toString.capitalize} Event triggered:"
      (updated, header +: messages, result)

    /** Test-only entry point to directly trigger a specific SpecialEvent case by index.
      *
      * This method is intended solely for unit testing to ensure all branches of [[SpecialEvent]] behave correctly. It
      * should never be used in production logic.
      *
      * @param player
      *   the player involved in the event
      * @param caseIndex
      *   the specific branch (0–7) to execute within [[SpecialEvent]]
      * @return
      *   a tuple with updated player, messages, and optional monster
      */
    def testSpecialCase(player: Player, caseIndex: Int): (Player, List[String], Option[Monster]) =
      SpecialEvent.actionWithCase(player, caseIndex, false)

    /** Resolves the internal [[GameEvent]] implementation for the given [[EventType]]. */
    private def resolve(eventType: EventType): GameEvent = eventType match
      case EventType.fight => FightEvent
      case EventType.mission => MissionEvent
      case EventType.changeWorld => ChangeWorldEvent
      case EventType.training => TrainingEvent
      case EventType.restore => RestoreEvent
      case EventType.sell => SellEvent
      case EventType.power => PowerUpEvent
      case EventType.craft => CraftEvent
      case EventType.magic => MagicEvent
      case EventType.special => SpecialEvent
      case EventType.gameOver => GameOverEvent

  // ---------------------------
  // Internal Game Event Implementations
  // ---------------------------

  /** Resolves a post-fight between the player and the last encountered monster. Rewards XP and gold, and possibly drops
    * equipment or items.
    */
  private case object FightEvent extends GameEvent:
    def action(player: Player): (Player, List[String], Option[Monster]) =
      if !player.isAlive then
        GameOverEvent.action(player)
      else
        CombatController.lastMonster match
          case Some(monster) if !monster.isDead =>
            val summary = List(s"You escaped from ${monster.name}. You got nothing")
            (player, summary, None)

          case Some(monster) =>
            val (p1, eqMsg) = CombatController.handleEquipDrop(player, monster)
            val (p2, itemMsg) = CombatController.handleItemDrop(p1, monster)
            val pWithXp = PlayerController.gainXP(p2, MonsterController.getExpReward(monster))
            val finalP = PlayerController.addGold(pWithXp, MonsterController.getGoldReward(monster))

            val summary = List(
              eqMsg,
              itemMsg,
              s"You earned ${monster.goldReward} gold and ${monster.experienceReward} XP.",
              "You have won!"
            )
            (finalP, summary, None)

          case None =>
            val msg = "No monster was found for the fight."
            (player, List(msg), None)

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
        val updated = player.withGold(player.gold - cost).powerUpAttributes()
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
        (pAfterSell, sellMsgs, None)

  private case object MagicEvent extends GameEvent:
    def action(player: Player): (Player, List[String], Option[Monster]) =
      val (updatedPlayer, msg) = learnOrUpgradeSkill(player: Player)
      (updatedPlayer, List(msg), None)

  private case object CraftEvent extends GameEvent:
    def action(player: Player): (Player, List[String], Option[Monster]) =
      val (updatedPlayer, msg) = forgeOrUpgradeEquipment(player: Player)
      (updatedPlayer, List(msg), None)

  /** A special unpredictable event with 8 possible outcomes. random choice fallback when player doesn't respond to
    * dialogs.
    */
  private case object SpecialEvent extends GameEvent:

    override def action(player: Player): (Player, List[String], Option[Monster]) =
      val randomCase = Random.nextInt(8)
      actionWithCase(player, randomCase, useDialogs = true)

    /** Executes a specific special case.
      *
      * @param player
      *   the player involved
      * @param caseIndex
      *   the specific case to trigger (0–7)
      * @param useDialogs
      *   whether to show interactive dialogs (true = gameplay, false = test mode)
      * @return
      *   a tuple with updated player, messages, and optional monster
      *
      * Interactive cases (0, 1, 2, 3, 4, 5, 6, 7) now handle timeout scenarios by making random choices automatically,
      * ensuring all special events produce outcomes.
      */

    def actionWithCase(player: Player, caseIndex: Int, useDialogs: Boolean): (Player, List[String], Option[Monster]) =
      caseIndex match
        case 0 =>
          val handler = (p: Player, accept: Boolean) =>
            if accept then handleShrineBlessingOrCurse(p)
            else handleShrineIgnore(p)
          handleWithDialogOrRandom(player, useDialogs)(SpecialEventDialog.showBlessingCurseDialog())(handler)

        case 1 =>
          val handler = (p: Player, accept: Boolean) =>
            if accept then generateEquipOutcome(p)
            else (p, List("You fled from the powerful monster and continued safely."), None)
          handleWithDialogOrRandom(player, useDialogs)(SpecialEventDialog.showPowerfulMonsterDialog())(handler)

        case 2 =>
          val handler = (p: Player, accept: Boolean) =>
            if accept then handleLethalMonsterFight(p)
            else handleLethalMonsterEscape(p)
          handleWithDialogOrRandom(player, useDialogs)(SpecialEventDialog.showGameOverMonsterDialog())(handler)

        case 3 =>
          val handler = (p: Player, accept: Boolean) =>
            if accept then handleDungeonFoundItem(p)
            else handleDungeonIgnore(p)
          handleWithDialogOrRandom(player, useDialogs)(SpecialEventDialog.showHiddenDungeonDialog())(handler)

        case 4 =>
          val handler = (p: Player, accept: Boolean) =>
            if accept then handleTrapTriggered(p)
            else handleTrapEscape(p)
          handleWithDialogOrRandom(player, useDialogs)(SpecialEventDialog.showDungeonTrapDialog())(handler)

        case 5 =>
          val handler = (p: Player, accept: Boolean) =>
            if accept then handleVillagerHelp(p)
            else (p, List("You ignored the villagers and moved on."), None)
          handleWithDialogOrRandom(player, useDialogs)(SpecialEventDialog.showVillagerHelpDialog())(handler)

        case 6 =>
          val handler = (p: Player, accept: Boolean) =>
            if accept then handleDeadlyTrapOutcome(p)
            else handleDeadlyTrapEscape(p)
          handleWithDialogOrRandom(player, useDialogs)(SpecialEventDialog.showGameOverTrapDialog())(handler)

        case 7 =>
          if useDialogs then SpecialEventDialog.showTheftDialog()
          val (p2, msg) = PlayerController.stealRandomItem(player)
          (p2, List(msg), None)

        case _ =>
          (player, List("Nothing happened."), None)

    /** Helper for equipment outcome logic */
    private def generateEquipOutcome(player: Player): (Player, List[String], Option[Monster]) =
      EquipmentFactory.probBased(player.attributes.lucky, player.level) match
        case Some(newEq) =>
          val msg1 = "You defeated a powerful monster!"
          val msg2 = s"You looted: ${newEq.name}"
          val maybeOld = player.equipment.get(newEq.slot)
          val (updatedPlayer, msg3) = maybeOld.get match
            case Some(old) if old.value >= newEq.value =>
              val p2 = PlayerController.addGold(player, newEq.value)
              (p2, s"You sold ${newEq.name} for ${newEq.value} gold.")
            case _ =>
              val p2 = PlayerController.equipmentOn(player, newEq.slot, newEq)
              (p2, s"You equipped: ${newEq.name}")
          (updatedPlayer, List(msg1, msg2, msg3), None)

        case None =>
          (player, List("You found no loot."), None)

  /** Marks the player as dead and ends the game. */
  private case object GameOverEvent extends GameEvent:
    def action(player: Player): (Player, List[String], Option[Monster]) =
      val msg = "GAME OVER!"
      (player.withCurrentHp(0), List(msg), None)

  /** Learns a new skill, or upgrades it if the player already knows it.
    *
    * @param player
    *   the player to modify
    * @return
    *   a tuple: (updated player, descriptive message)
    */
  private def learnOrUpgradeSkill(player: Player): (Player, String) =
    val newSkill = SkillFactory.randomSkill()
    val (updatedPlayer, isNew) = PlayerController.addSkill(player, newSkill)
    val msg =
      if isNew then s"Learned new skill: ${newSkill.name}"
      else s"Upgraded existing skill: ${newSkill.name} to level ${newSkill.poweredUp.powerLevel}"
    (updatedPlayer, msg)

  /** Attempts to equip a new equipment or power up an existing one.
    *
    * @param player
    *   The player to modify
    * @param upgradeChance
    *   Probability (0.0 to 1.0) of upgrading existing equipment
    * @return
    *   A new Player with equipment changes and a descriptive message
    */
  private def forgeOrUpgradeEquipment(player: Player, upgradeChance: Double = 0.5): (Player, String) =
    val equipmentSlot: EquipmentSlot = RandomFunctions.randomEquipmentSlot()
    val playerEquip = player.equipment(equipmentSlot)

    if playerEquip.isDefined && Random.nextDouble() < upgradeChance then
      val upgraded = EquipmentFactory.powerUpEquip(playerEquip.get)
      val updatedPlayer = PlayerController.equipmentOn(player, upgraded.slot, upgraded)
      (
        updatedPlayer,
        s"Upgraded equipment: ${playerEquip.get.name} with ${playerEquip.get.value} ${upgraded.name}, now worth ${upgraded.value}"
      )
    else
      EquipmentFactory.probBased(player.attributes.lucky, player.level) match
        case Some(newEq) =>
          val updatedPlayer = PlayerController.equipmentOn(player, newEq.slot, newEq)
          (updatedPlayer, s"Forged and equipped a new equipment: ${newEq.name}")
        case None =>
          (player, "No equipment was forged.")

  /** Executes an event handler based on dialog input or a fallback random choice.
    */
  private def handleWithDialogOrRandom(
      player: Player,
      useDialogs: Boolean
  )(
      dialogResult: => Option[Boolean]
  )(handler: (Player, Boolean) => (Player, List[String], Option[Monster])): (Player, List[String], Option[Monster]) =
    if useDialogs then
      dialogResult match
        case Some(accept) => handler(player, accept)
        case None => handler(player, Random.nextBoolean())
    else
      handler(player, Random.nextBoolean())

  // --- Event Logic Functions ---

  /** Applies a random blessing or curse to the player, adjusting their level. */
  private def handleShrineBlessingOrCurse(player: Player): (Player, List[String], Option[Monster]) =
    val change = Random.between(1, 4)
    val isBlessing = Random.nextBoolean()
    val updated = (1 to change).foldLeft(player) { (acc, _) =>
      if isBlessing then PlayerController.levelUp(acc)
      else PlayerController.levelDown(acc)
    }
    val msg = if isBlessing then s"Blessing! You leveled up $change times."
    else s"Curse! You lost $change levels."
    (updated, List(msg), None)

  /** Skips the shrine event with a simple flavor message. */
  private def handleShrineIgnore(player: Player): (Player, List[String], Option[Monster]) =
    (player, List("You ignored the shrine and continued on your path."), None)

  /** Simulates a fight with a lethal monster. Player may win gold or lose all EXP. */
  private def handleLethalMonsterFight(player: Player): (Player, List[String], Option[Monster]) =
    if Random.nextBoolean() then
      val reward = 100 * player.level
      val updated = PlayerController.addGold(player, reward)
      (updated, List(s"You defeated the lethal monster and gained $reward gold!"), None)
    else
      val updated = player.withExp(0)
      (updated, List("You fought and lost... All your EXP is gone!"), None)

  /** Outcome when player avoids fighting a lethal monster. */
  private def handleLethalMonsterEscape(player: Player): (Player, List[String], Option[Monster]) =
    (player, List("You escaped from the deadly monster just in time."), None)

  /** Awards the player with an item found in a dungeon, if lucky. */
  private def handleDungeonFoundItem(player: Player): (Player, List[String], Option[Monster]) =
    ItemFactory.alwaysCreate().createRandomItem(player.attributes.lucky) match
      case Some(item) =>
        val updated = PlayerController.addItem(player, item)
        val msg = s"Found: ${item.name}, worth ${item.gold}, rarity: ${item.rarity}"
        (updated, List("You explored a hidden dungeon and found an item!", msg), None)
      case None =>
        (player, List("No item found."), None)

  /** Ignores the dungeon with a simple message. */
  private def handleDungeonIgnore(player: Player): (Player, List[String], Option[Monster]) =
    (player, List("You ignored the dungeon and continued."), None)

  /** Handles a trap scenario; player is injured or gains EXP. */
  private def handleTrapTriggered(player: Player): (Player, List[String], Option[Monster]) =
    if Random.nextBoolean() then
      val msg = "You triggered a trap! HP and MP were halved."
      (PlayerController.playerInjured(player), List(msg), None)
    else
      val exp = Random.between(1, 100) * player.level
      val msg = "You narrowly avoided the trap — lucky you!"
      (PlayerController.gainXP(player, exp), List(msg), None)

  /** Player avoids the trap altogether. */
  private def handleTrapEscape(player: Player): (Player, List[String], Option[Monster]) =
    (player, List("You sensed danger and escaped the trap."), None)

  /** Handles helping villagers, resulting in an EXP reward. */
  private def handleVillagerHelp(player: Player): (Player, List[String], Option[Monster]) =
    val gain = Random.between(50, 151) * (1 + player.attributes.wisdom / 100)
    val msg = s"You helped villagers and gained $gain EXP."
    (PlayerController.gainXP(player, gain), List(msg), None)

  /** Player faces a deadly trap and either dies or gains max HP/MP. */
  private def handleDeadlyTrapOutcome(player: Player): (Player, List[String], Option[Monster]) =
    if Random.nextBoolean() then
      val (p2, msgs, result) = GameOverEvent.action(player)
      (p2, "It was a deadly trap! You died instantly." :: msgs, result)
    else
      val hpGain = Random.between(1, 4) * player.level
      val mpGain = Random.between(1, 4) * player.level
      val updated = player.withHp(player.hp + hpGain).withMp(player.mp + mpGain)
      val msg = s"You evaded the deadly trap! Max HP +$hpGain, MP +$mpGain"
      (updated, List(msg), None)

  /** Player successfully avoids the deadly trap. */
  private def handleDeadlyTrapEscape(player: Player): (Player, List[String], Option[Monster]) =
    (player, List("You noticed something was wrong and carefully backed away."), None)
