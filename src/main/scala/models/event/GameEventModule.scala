package models.event

import controllers.{CombatController, MissionController, MonsterController, PlayerController}
import models.monster.Monster
import models.player.EquipmentModule.{Equipment, EquipmentFactory}
import models.player.{ItemFactory, Player, SkillFactory}
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
   * - `power`: Power up player stats if he has enough gold
   * - `sell`: Selling items from inventory
   * - `special`: Randomized, unpredictable event
   * - `craft`: Craft new equip or power up an old one
   * - `magic`: Learn new skill or power up an existing one
   * - `gameOver`: Ends the game
   *
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

  /** Resolves a post-fight between the player and the last encountered monster.
   * Rewards XP and gold, and possibly drops equipment or items.
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


  /** A special unpredictable event with 8 possible outcomes.
   * random choice fallback when player doesn't respond to dialogs.
   */
  private case object SpecialEvent extends GameEvent:

    override def action(player: Player): (Player, List[String], Option[Monster]) =
      val randomCase = Random.nextInt(8)
      actionWithCase(player, randomCase, useDialogs = true)

    /** Executes a specific special case.
     *
     * @param player     the player involved
     * @param caseIndex  the specific case to trigger (0–7)
     * @param useDialogs whether to show interactive dialogs (true = gameplay, false = test mode)
     * @return a tuple with updated player, messages, and optional monster
     *
     *         Interactive cases (0, 1, 2, 3, 4, 5, 6) now handle timeout scenarios by making
     *         random choices automatically, ensuring all special events produce outcomes.
     */
    def actionWithCase(player: Player, caseIndex: Int, useDialogs: Boolean): (Player, List[String], Option[Monster]) = caseIndex match
      case 0 =>
        if useDialogs then
          SpecialEventDialog.showBlessingCurseDialog() match
            case Some(true) =>
              val change = Random.between(1, 4)
              if Random.nextBoolean() then
                val p2 = (1 to change).foldLeft(player)((p, _) => PlayerController.levelUp(p))
                (p2, List(s"Blessing! You leveled up $change times."), None)
              else
                val p2 = (1 to change).foldLeft(player)((p, _) => PlayerController.levelDown(p))
                (p2, List(s"Curse! You lost $change levels."), None)
            case Some(false) =>
              (player, List("You ignored the shrine and continued on your path."), None)
            case None =>
              val randomChoice = Random.nextBoolean()
              if randomChoice then
                val change = Random.between(1, 4)
                val isBlessing = Random.nextBoolean()
                val updated = (1 to change).foldLeft(player)((p, _) =>
                  if isBlessing then PlayerController.levelUp(p) else PlayerController.levelDown(p)
                )
                val msg = if isBlessing then s"Random blessing! You leveled up $change times." else s"Random curse! You lost $change levels."
                (updated, List(msg), None)
              else
                (player, List("You randomly ignored the shrine."), None)
        else
          val change = Random.between(1, 4)
          val isBlessing = Random.nextBoolean()
          val updated = (1 to change).foldLeft(player)((p, _) =>
            if isBlessing then PlayerController.levelUp(p) else PlayerController.levelDown(p)
          )
          val msg = if isBlessing then s"Blessing! You leveled up $change times." else s"Curse! You lost $change levels."
          (updated, List(msg), None)

      case 1 =>
        if useDialogs then
          SpecialEventDialog.showPowerfulMonsterDialog() match
            case Some(true) =>
              generateEquipOutcome(player)
            case Some(false) =>
              (player, List("You fled from the powerful monster and continued safely."), None)
            case None =>
              // Random choice fallback - should make random decision instead of fixed message
              if Random.nextBoolean() then
                generateEquipOutcome(player)
              else
                (player, List("You randomly decided to flee from the monster."), None)
        else
          generateEquipOutcome(player)

      case 2 =>
        def fightOutcome(p: Player): (Player, List[String], Option[Monster]) =
          if Random.nextBoolean() then
            val reward = 100 * p.level
            val p2 = PlayerController.addGold(p, reward)
            val msg = s"You defeated the lethal monster and gained $reward gold!"
            (p2, List(msg), None)
          else
            val p2 = p.withExp(0)
            val msg = s"You fought and lost... All your EXP is gone!"
            (p2, List(msg), None)

        def escapeOutcome(p: Player): (Player, List[String], Option[Monster]) =
          (p, List("You escaped from the deadly monster just in time."), None)

        if useDialogs then SpecialEventDialog.showGameOverMonsterDialog() match
          case Some(true) =>
            fightOutcome(player)

          case Some(false) =>
            escapeOutcome(player)

          case None =>
            if Random.nextBoolean() then fightOutcome(player)
            else escapeOutcome(player)
        else if Random.nextBoolean() then fightOutcome(player) else escapeOutcome(player)


      case 3 =>
        if useDialogs then
          SpecialEventDialog.showHiddenDungeonDialog() match
            case Some(true) =>
              val item = ItemFactory.randomItem(player.attributes.lucky)
              val msg1 = "You explored a hidden dungeon and found an item!"
              val msg2 = s"Found: ${item.name}, worth ${item.gold}."
              (PlayerController.addItem(player, item), List(msg1, msg2), None)
            case Some(false) =>
              (player, List("You ignored the dungeon and continued."), None)
            case None =>
              // Random choice fallback for timeout scenarios
              if Random.nextBoolean() then
                val item = ItemFactory.randomItem(player.attributes.lucky)
                val msg1 = "You randomly decided to explore the dungeon and found an item!"
                val msg2 = s"Found: ${item.name}, worth ${item.gold}."
                (PlayerController.addItem(player, item), List(msg1, msg2), None)
              else
                (player, List("You randomly decided to ignore the dungeon."), None)
        else
          val item = ItemFactory.randomItem(player.attributes.lucky)
          val msg1 = "You discovered an item in a hidden dungeon."
          val msg2 = s"Item: ${item.name}, value: ${item.gold}"
          (PlayerController.addItem(player, item), List(msg1, msg2), None)

      case 4 =>
        def trapOutcome(p: Player): (Player, List[String], Option[Monster]) =
          if Random.nextBoolean() then
            val msg = "You triggered a trap! HP and MP were halved."
            (PlayerController.playerInjured(p), List(msg), None)
          else
            val exp = Random.between(1, 100) * player.level
            val msg = "You narrowly avoided the trap — lucky you!"
            (PlayerController.gainXP(p, exp), List(msg), None)

        def escapeOutcome(p: Player): (Player, List[String], Option[Monster]) =
          (p, List("You sensed danger and escaped the trap."), None)

        if useDialogs then SpecialEventDialog.showDungeonTrapDialog() match
          case Some(true) => trapOutcome(player)
          case Some(false) => escapeOutcome(player)
          case None =>
            if Random.nextBoolean() then trapOutcome(player)
            else escapeOutcome(player)
        else if Random.nextBoolean() then trapOutcome(player) else escapeOutcome(player)


      case 5 =>
        if useDialogs then
          SpecialEventDialog.showVillagerHelpDialog() match
            case Some(true) =>
              val gain = Random.between(50, 151) * (1 + player.attributes.wisdom / 100)
              val msg = s"You helped villagers and gained $gain EXP."
              (PlayerController.gainXP(player, gain), List(msg), None)
            case Some(false) =>
              (player, List("You ignored the villagers and moved on."), None)
            case None =>
              // Random choice fallback for timeout scenarios
              if Random.nextBoolean() then
                val gain = Random.between(50, 151) * (1 + player.attributes.wisdom / 100)
                val msg = s"You randomly decided to help villagers and gained $gain EXP."
                (PlayerController.gainXP(player, gain), List(msg), None)
              else
                (player, List("You randomly decided to ignore the villagers."), None)
        else
          val gain = Random.between(50, 151) * (1 + player.attributes.wisdom / 100)
          val msg = s"You helped villagers and gained $gain EXP."
          (PlayerController.gainXP(player, gain), List(msg), None)

      case 6 =>
        def deadlyTrapOutcome(p: Player): (Player, List[String], Option[Monster]) =
          if Random.nextBoolean() then
            val msg = "It was a deadly trap! You died instantly."
            val (p2, msgs, result) = GameOverEvent.action(p)
            (p2, msg :: msgs, result)
          else
            val (hpBonus, mpBonus) = (Random.between(1, 4) * p.level, Random.between(1, 4) * p.level)
            val msg = s"You evaded the deadly trap just in time! You have increased your maximum hp by ${hpBonus} and mp by ${mpBonus}"
            (p.withHp(p.hp + hpBonus).withMp(p.mp + mpBonus), List(msg), None)

        def safeEscapeOutcome(p: Player): (Player, List[String], Option[Monster]) =
          (p, List("You noticed something was wrong and carefully backed away."), None)

        if useDialogs then SpecialEventDialog.showGameOverTrapDialog() match
          case Some(true) => deadlyTrapOutcome(player)
          case Some(false) => safeEscapeOutcome(player)
          case None =>
            if Random.nextBoolean() then deadlyTrapOutcome(player)
            else safeEscapeOutcome(player)
        else if Random.nextBoolean() then deadlyTrapOutcome(player) else safeEscapeOutcome(player)


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

  /**
   * Learns a new skill, or upgrades it if the player already knows it.
   *
   * @param player the player to modify
   * @return a tuple: (updated player, descriptive message)
   */
  private def learnOrUpgradeSkill(player: Player): (Player, String) =
    val newSkill = SkillFactory.randomSkill()
    val (updatedPlayer, isNew) = PlayerController.addSkill(player, newSkill)
    val msg =
      if isNew then s"Learned new skill: ${newSkill.name}"
      else s"Upgraded existing skill: ${newSkill.name} to level ${newSkill.poweredUp.powerLevel}"
    (updatedPlayer, msg)


  /**
   * Attempts to equip a new equipment or power up an existing one.
   *
   * @param player        The player to modify
   * @param upgradeChance Probability (0.0 to 1.0) of upgrading existing equipment
   * @return A new Player with equipment changes and a descriptive message
   */
  def forgeOrUpgradeEquipment(player: Player, upgradeChance: Double = 0.5): (Player, String) =
    val newEq = EquipmentFactory.probBased(player.attributes.lucky, player.level)
    val maybeExisting: Option[Equipment] = player.equipment(newEq.get.slot)

    if maybeExisting.isDefined && Random.nextDouble() < upgradeChance then
      val upgraded = EquipmentFactory.powerUpEquip(maybeExisting.get)
      val updatedPlayer = PlayerController.equipmentOn(player, upgraded.slot, upgraded)
      (updatedPlayer, s"Upgraded equipment: ${maybeExisting.get.name} with ${maybeExisting.get.value} ${upgraded.name}, now worth ${upgraded.value}")
    else
      EquipmentFactory.alwaysDrop(player.level) match
        case Some(newEq) =>
          val updatedPlayer = PlayerController.equipmentOn(player, newEq.slot, newEq)
          (updatedPlayer, s"Equipped new item: ${newEq.name}")
        case None =>
          (player, "No equipment was forged.")