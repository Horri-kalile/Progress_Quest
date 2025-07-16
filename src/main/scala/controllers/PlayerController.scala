package controllers

import models.event.Mission
import models.monster.Monster
import models.player.*
import models.player.Behavior.BehaviorType.{FastLeveling, Heal}
import models.world.OriginZone
import util.GameConfig

import scala.annotation.tailrec
import scala.util.Random

object PlayerController:

  /**
   * Calculates the attack damage from a player to a monster.
   *
   * Damage depends on player's strength, equipment bonuses,
   * level, and monster's defense and weaknesses.
   *
   * @param player  the attacking player
   * @param monster the defending monster
   * @return the calculated damage amount as Int (minimum 1)
   */
  def calculatePlayerAttack(player: Player, monster: Monster): Int =
    val str = player.attributes.strength
    val equip = player.equipment.values.flatten.map(_.statBonus.strength).sum
    val (defense, physical, _) = MonsterController.getMonsterDefenceAndWeakness(monster)
    val baseDamage = ((str + equip + player.level - defense) * physical).max(1).toInt
    player.behavior.onBattleDamage(player, baseDamage)

  /**
   * Applies damage to the player, reducing current HP.
   *
   * @param player the player receiving damage
   * @param damage the damage amount to apply
   * @return updated Player instance with reduced HP
   */
  def takeDamage(player: Player, damage: Int): Player =
    player.receiveDamage(damage)

  /**
   * Heals the player by a given amount, increasing current HP up to max HP.
   *
   * @param player the player to heal
   * @param amount the healing amount
   * @return updated Player instance with increased HP
   */
  def heal(player: Player, amount: Int): Player =
    player.receiveHealing(amount)

  /**
   * Grants experience points (XP) to the player and handles leveling up.
   *
   * Depending on the player's behaviorType, healing or special XP
   * gains may apply at battle end.
   * If XP threshold for leveling is reached, player levels up,
   * possibly learns a new skill.
   *
   * @param player   the player gaining XP
   * @param xpGained the amount of XP to add
   * @return updated Player instance after XP gain and potential level-up
   */
  def gainXP(player: Player, xpGained: Int): Player =
    // Compute behavior bonus onBattleEnd
    val (heal, extraXP) = player.behaviorType match
      case Heal => (player.behavior.onBattleEnd(player.hp), 0)
      case FastLeveling => (0, player.behavior.onBattleEnd(xpGained))
      case _ => (0, 0)

    // Apply healing effect if any
    val healedPlayer = if heal > 0 then player.receiveHealing(heal) else player

    // Recurse level-ups if XP over multiple thresholds
    @tailrec
    def levelUpLoop(p: Player, xp: Int): Player =
      val threshold = p.level * 100
      if xp >= threshold then
        val leveled = levelUp(p)
        levelUpLoop(leveled, xp - threshold)
      else p.withExp(xp)

    val finalPlayer = levelUpLoop(healedPlayer, healedPlayer.exp + xpGained + extraXP)

    // Try to learn a skill after level-up chain
    maybeLearnSkill(finalPlayer)


  /**
   * Adds a specified quantity of an item to the player's inventory.
   *
   * @param player   the player receiving the item
   * @param item     the item to add
   * @param quantity the number of items to add (default 1)
   * @return updated Player instance with added item(s)
   */
  def addItem(player: Player, item: Item, quantity: Int = 1): Player =
    val updated = player.inventory.updatedWith(item):
      case Some(qty) => Some(qty + quantity)
      case None => Some(quantity)
    player.withInventory(updated)

  /**
   * Removes a specified quantity of an item from the player's inventory.
   * Private helper method.
   *
   * @param player   the player losing the item
   * @param item     the item to remove
   * @param quantity the number of items to remove (default 1)
   * @return updated Player instance with removed item(s)
   */
  private def removeItem(player: Player, item: Item, quantity: Int = 1): Player =
    val currentQty = player.inventory.getOrElse(item, 0) - quantity
    val updatedInventory =
      if (currentQty > 0) player.inventory.updated(item, currentQty)
      else player.inventory - item
    player.withInventory(updatedInventory)

  /**
   * Equips an equipment item on the player in the specified slot.
   *
   * @param player    the player equipping the item
   * @param slot      the equipment slot to equip to
   * @param equipment the equipment to equip
   * @return updated Player instance with new equipment
   */
  def equipmentOn(player: Player, slot: EquipmentSlot, equipment: Equipment): Player =
    val updatedEquipment = player.equipment + (slot -> Some(equipment))
    player.withEquipment(updatedEquipment)

  /**
   * Unequips equipment from the specified slot.
   *
   * @param player the player removing equipment
   * @param slot   the equipment slot to clear
   * @return updated Player instance with slot cleared
   */
  def equipmentOff(player: Player, slot: EquipmentSlot): Player =
    val updatedEquipment = player.equipment + (slot -> None)
    player.withEquipment(updatedEquipment)

  /**
   * Adds gold to the player's total.
   *
   * @param player the player receiving gold
   * @param amount the amount of gold to add
   * @return updated Player instance with increased gold
   */
  def addGold(player: Player, amount: Double): Player =
    player.withGold(player.gold + amount)

  /**
   * Changes player zone.
   *
   * @param player  the player receiving gold
   * @param newZone the new zone
   * @return updated Player instance with new zone
   */
  def changeWorld(player: Player, newZone: OriginZone): Player =
    player.withCurrentZone(newZone)

  /**
   * Spends gold from the player's total, cannot go below zero.
   *
   * @param player the player spending gold
   * @param amount the amount of gold to spend
   * @return updated Player instance with decreased gold
   */
  def spendGold(player: Player, amount: Double): Player =
    if player.gold >= amount then player.withGold(player.gold - amount) else player


  /**
   * Adds a skill to the player's skill list.
   * If skill already exists, upgrade its power level by 1.
   *
   * @param player the player learning the skill
   * @param skill  the skill to add or upgrade
   * @return updated Player instance with new or upgraded skill
   */
  def addSkill(player: Player, skill: Skill): Player =
    val maybeExisting = player.skills.find(_.name == skill.name)
    maybeExisting match
      case Some(existing: GenericSkill) =>
        val updatedSkills = player.skills.map(s => if s.name == existing.name then s.poweredUp else s)
        player.withSkills(updatedSkills)
      case None =>
        player.withSkills(skill :: player.skills)

  /**
   * Lowers the player's level by a given number and decreases stats.
   *
   * @param player the player to level down
   * @param levels number of levels to decrease (minimum level 1)
   * @return updated Player instance after level down
   */
  def levelDownAndDecreaseStats(player: Player, levels: Int): Player =
    player.withLevel((player.level - levels).max(1))

  /**
   * Increases player level and restores HP/MP.
   * Also applies attribute increase.
   *
   * @param player the player
   * @return updated Player instance
   */
  def levelUp(player: Player): Player =
    val newHp = player.hp * Random.between(1.05, 1.2)
    val newMp = player.mp * Random.between(1.05, 1.2)
    player.withLevel(player.level + 1)
      .withExp(0)
      .withHp(newHp.toInt)
      .withMp(newMp.toInt)
      .withCurrentHp(newHp.toInt)
      .withCurrentMp(newMp.toInt)
      .powerUpAttributes()

  /**
   * Decreases player level and stats, not going below level 1.
   *
   * @param player the player
   * @return updated Player instance
   */
  def levelDown(player: Player): Player =
    val newHp = player.hp * Random.between(0.90, 0.99)
    val newMp = player.mp * Random.between(0.90, 0.99)
    player.withLevel((player.level - 1).max(1))
      .withExp(0)
      .withHp(newHp.toInt)
      .withMp(newMp.toInt)
      .withCurrentHp(newHp.toInt)
      .withCurrentMp(newMp.toInt)
      .powerDownAttributes()

  /**
   * Possibly teaches a new skill based on lucky stat and config probability.
   *
   * @param player the player
   * @return updated Player, possibly with a new skill
   */
  private def maybeLearnSkill(player: Player): Player =
    val chance = GameConfig.baseLearnSkillChance +
      GameConfig.specialBonusPerLucky * player.attributes.lucky
    if Random.nextDouble() < chance then addSkill(player, SkillFactory.randomSkill())
    else player

  /**
   * Randomly sells one item from inventory and adds gold.
   *
   * @param player the player
   * @return (updated Player, message string)
   */
  def sellRandomItem(player: Player): (Player, String) =
    if player.inventory.isEmpty then (player, "Inventory empty. Nothing sold.")
    else
      val (item, qty) = Random.shuffle(player.inventory.toList).head
      val amount = Random.between(1, qty + 1)
      val newInventory = if qty > amount then player.inventory.updated(item, qty - amount) else player.inventory - item
      val gold = item.gold * amount
      val updatedPlayer = player.withInventory(newInventory).withGold(gold)
      (updatedPlayer, s"Sold $amount × ${item.name} for $gold gold.")

  /**
   * Randomly removes one item from inventory (e.g. due to theft).
   *
   * @param player the player
   * @return (updated Player, message string)
   */
  def stealRandomItem(player: Player): (Player, String) =
    if player.inventory.isEmpty then (player, "Nothing to steal.")
    else
      val (item, qty) = Random.shuffle(player.inventory.toList).head
      val updated = if qty > 1 then player.inventory.updated(item, qty - 1) else player.inventory - item
      (player.withInventory(updated), s"A ${item.name} was stolen from your inventory.")

  def playerInjured(player: Player): Player =
    player.withCurrentHp(player.currentHp / 2).withCurrentMp(player.currentMp / 2)

  def useSkill(player: Player, skill: Skill, target: Monster): (Player, Monster, String) =
    if player.currentMp < skill.manaCost then
      (player, target, s"Not enough mana to cast ${skill.name}.")

    else
      val updatedPlayer = player.withCurrentMp(player.currentMp - skill.manaCost)
      val multiplier = Random.between(0.1, skill.baseMultiplier)

      skill.effectType match
        case SkillEffectType.Physical =>
          val dmg =
            ((player.attributes.strength + player.level + player.equipmentList.map(_.value).sum)
              * multiplier * skill.powerLevel).toInt
          val damagedMonster = target.receiveDamage(dmg)
          (updatedPlayer, damagedMonster, s"You dealt $dmg physical damage with ${skill.name}.")

        case SkillEffectType.Magic =>
          val magicPower = player.attributes.intelligence
          val dmg = ((magicPower + player.level + player.equipmentList.map(_.value).sum)
            * multiplier * skill.powerLevel).toInt
          val damagedMonster = target.receiveDamage(dmg)
          (updatedPlayer, damagedMonster, s"You dealt $dmg magic damage with ${skill.name}.")

        case SkillEffectType.Healing =>
          val heal = ((player.attributes.wisdom + player.level)
            * multiplier * skill.powerLevel).toInt
          val healedPlayer = updatedPlayer.receiveHealing(heal)
          (healedPlayer, target, s"You healed yourself for $heal HP using ${skill.name}.")
