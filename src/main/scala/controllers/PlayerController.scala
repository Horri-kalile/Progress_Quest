package controllers

import models.event.Mission
import models.monster.Monster
import models.player.*
import util.GameConfig

import scala.util.Random


object PlayerController:

  def calculatePlayerAttack(player: Player, monster: Monster): Int =
    // Base attack from level + equipment bonuses
    val str = player.attributes.strength
    val equip = player.equipment.values.flatten.map(_.statBonus.strength).sum
    val (defence, physical, _) = MonsterController.getMonsterDefenceAndWeakness(monster)
    ((str + equip + player.level - defence) * physical).max(1).toInt

  def takeDamage(player: Player, damage: Int): Player =
    val newHp = (player.currentHp - damage).max(0)
    player.copy(currentHp = newHp)

  def heal(player: Player, amount: Int): Player =
    player.copy(currentHp = player.currentHp + amount)


  def gainXP(player: Player, xpGained: Int): Player =
    val totalXP = player.exp + xpGained
    val levelUpThreshold = player.level * 100

    if totalXP >= levelUpThreshold then
      val leveledPlayer = player.levelUp()
      val learnChance = GameConfig.baseLearnSkillChance + GameConfig.specialBonusPerLucky * leveledPlayer.attributes.lucky

      if Random.nextDouble() < learnChance then
        val newSkill = SkillFactory.randomSkill()
        PlayerController.addSkill(leveledPlayer, newSkill)
      else
        leveledPlayer
    else
      player.copy(exp = totalXP)


  def addItem(player: Player, item: Item, quantity: Int = 1): Player =
    val updatedInventory = player.inventory + (item -> (player.inventory.getOrElse(item, 0) + quantity))
    player.copy(inventory = updatedInventory)


  private def removeItem(player: Player, item: Item, quantity: Int = 1): Player =
    val currentQty = player.inventory.getOrElse(item, 0) - quantity
    val updatedInventory =
      if (currentQty > 0) player.inventory.updated(item, currentQty)
      else player.inventory - item
    player.copy(inventory = updatedInventory)

  //TODO DA TESTARE
  def equipmentOn(player: Player, slot: EquipmentSlot, equipment: Equipment): Player =
    val updatedEquipment = player.equipment + (slot -> Some(equipment))
    player.copy(equipment = updatedEquipment)


  def equipmentOff(player: Player, slot: EquipmentSlot): Player =
    val updatedEquipment = player.equipment + (slot -> None)
    player.copy(equipment = updatedEquipment)


  def addGold(player: Player, amount: Double): Player =
    player.copy(gold = player.gold + amount)


  def spendGold(player: Player, amount: Double): Player =
    player.copy(gold = (player.gold - amount).max(0))


  def addMission(player: Player, mission: Mission): Player =
    player.copy(missions = player.missions :+ mission)


  def addSkill(player: Player, newSkill: Skill): Player =
    val maybeExisting = player.skills.find(_.name == newSkill.name)

    val updatedSkills = maybeExisting match
      case Some(existing: GenericSkill) =>
        val upgraded = existing.copy(powerLevel = existing.powerLevel + 1)
        player.skills.map {
          case s if s.name == existing.name => upgraded
          case s => s
        }
      case None =>
        player.skills :+ newSkill

    player.copy(skills = updatedSkills)


  def changeBehavior(player: Player, newBehavior: BehaviorType): Player =
    player.copy(behaviorType = newBehavior)


  def changeIdentity(player: Player, newIdentity: Identity): Player =
    player.copy(identity = newIdentity)

  def levelDownAndDecreaseStats(player: Player, levels: Int): Player =
    player.copy()


