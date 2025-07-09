package controllers

import models.event.Mission
import models.player.*


object PlayerController {


  def isAlive(player: Player): Boolean =
    player.currentHp > 0

  def calculatePlayerAttack(player: Player): Int =
    // Base attack from level + equipment bonuses
    val str = player.attributes.strength
    val equip = player.equipment.values.flatten.map(_.statBonus.strength).sum
    str + equip + player.level

  def takeDamage(player: Player, damage: Int): Player = {
    val newHp = (player.currentHp - damage).max(0)
    player.copy(currentHp = newHp)
  }

  def heal(player: Player, amount: Int): Player = {
    player.copy(currentHp = player.currentHp + amount)
  }


  def gainXP(player: Player, xpGained: Int): Player =
    val newExp = player.exp + xpGained
    if newExp >= player.level * 100 then player.levelUp()
    else player.copy(exp = newExp)

  def addItem(player: Player, item: Item, quantity: Int = 1): Player = {
    val updatedInventory = player.inventory + (item -> (player.inventory.getOrElse(item, 0) + quantity))
    player.copy(inventory = updatedInventory)
  }


  private def removeItem(player: Player, item: Item, quantity: Int = 1): Player = {
    val currentQty = player.inventory.getOrElse(item, 0) - quantity
    val updatedInventory =
      if (currentQty > 0) player.inventory.updated(item, currentQty)
      else player.inventory - item
    player.copy(inventory = updatedInventory)
  }


  def equipItem(player: Player, slot: EquipmentSlot, equipment: Equipment): Player = {
    val updatedEquipment = player.equipment + (slot -> Some(equipment))
    player.copy(equipment = updatedEquipment)
  }


  def unequipItem(player: Player, slot: EquipmentSlot): Player = {
    val updatedEquipment = player.equipment + (slot -> None)
    player.copy(equipment = updatedEquipment)
  }


  def useItem(player: Player, itemName: String): Player = {
    val maybeItem = player.inventory.keys.find(_.name == itemName)
    maybeItem match {
      case Some(item) => removeItem(player, item, 1)
      case None => player
    }
  }


  def addGold(player: Player, amount: Double): Player =
    player.copy(gold = player.gold + amount)


  def spendGold(player: Player, amount: Double): Player =
    player.copy(gold = (player.gold - amount).max(0))


  def addMission(player: Player, mission: Mission): Player =
    player.copy(missions = player.missions :+ mission)


  def addSkill(player: Player, skill: Skill): Player =
    player.copy(skills = player.skills :+ skill)


  def changeBehavior(player: Player, newBehavior: BehaviorType): Player =
    player.copy(behaviorType = newBehavior)


  def changeIdentity(player: Player, newIdentity: Identity): Player =
    player.copy(identity = newIdentity)

  def levelDownAndDecreaseStats(player: Player, levels: Int): Player =
    player.copy()


}
