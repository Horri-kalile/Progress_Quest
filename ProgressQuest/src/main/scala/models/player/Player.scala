package models.player

import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Random

case class Player(
                   name: String,
                   identity: Identity,
                   level: Int,
                   exp: Int,
                   hp: Int,
                   mp: Int,
                   baseAttributes: Attributes,
                   behaviorType: BehaviorType,
                   inventory: Map[Item, Int] = Map.empty,
                   equipment: Map[EquipmentSlot, Option[Equipment]] = EquipmentSlot.values.map(_ -> None).toMap,
                   skills: List[Skill] = List.empty,
                   gold: Double
                 ) extends Entity:

  val behavior: Behavior = BehaviorResolver.getBehavior(behaviorType)

  def attributes: Attributes =
    val bonuses = equipment.values.flatten.map(_.statBonus)
    bonuses.foldLeft(baseAttributes)(_ + _)

  /*TODO algoritmo di calcolo*/
  def maxHP: Int = attributes.constitution * 10

  def maxMP: Int = attributes.intelligence * 5

  def currentHP: Int = hp

  def currentMP: Int = mp


  //TODO calcolare exp necessaria in base al livello
  def gainExp(amount: Int): Player =
    val newExp = exp + amount
    if newExp >= level * 100 then levelUp()
    else this.copy(exp = newExp)

  def levelUp(): Player =
    this.copy(
      level = level + 1,
      exp = 0,
      hp = maxHP,
      mp = maxMP
    )

  /*TODO calcolo danno*/
  override def receiveDamage(amount: Int): Int =
    val finalDamage = behavior.onDamageTaken(this, amount)
    val newHP = (hp - finalDamage).max(0)
    finalDamage

  override def receiveHealing(amount: Int): Unit =
    val newHP = (hp + amount).min(maxHP)
    this.copy(hp = newHP)

  def useSkill(skill: Skill): Boolean = mp match
    case a if mp >= skill.manaCost => this.copy(mp = mp - skill.manaCost); true
    case _ => false

  def equip(item: Equipment): Player =
    val updated = equipment.updated(item.slot, Some(item))
    this.copy(equipment = updated)

  def unequip(slot: EquipmentSlot): Player =
    val updated = equipment.updated(slot, None)
    this.copy(equipment = updated)

  def learnSkill(skill: Skill): Player =
    if skills.exists(_.name == skill.name) then this
    else this.copy(skills = skill :: skills)

  def obtainItem(item: Item, amount: Int = 1): Player =
    val updated = inventory.updatedWith(item) {
      case Some(count) => Some(count + amount)
      case None => Some(amount)
    }
    this.copy(inventory = updated)


  def sellItem(): String =
    if inventory.isEmpty then "Inventory empty. Nothing sold."
    else
      val itemList = inventory.toList
      val (item, count) = itemList(Random.nextInt(itemList.size))
      val toRemove = Random.between(1, count + 1)
      val updatedInventory =
        if count > toRemove then inventory.updated(item, count - toRemove)
        else inventory - item

      val totalGold = item.gold * toRemove
      val updatedPlayer = this.copy(
        inventory = updatedInventory,
        gold = gold + totalGold
      )
      s"Sold $toRemove × ${item.name} for $totalGold gold."


  def stealFromInventory(): String =
    if inventory.isEmpty then "Nothing to steal."
    else
      val itemList = inventory.toList
      val (item, count) = itemList(Random.nextInt(itemList.size))
      val updatedInventory =
        if count > 1 then inventory.updated(item, count - 1)
        else inventory - item

      val updatedPlayer = this.copy(inventory = updatedInventory)
      s"A ${item.name} was stolen from your inventory."


  def emptyInventory: Boolean = inventory.isEmpty

  def startGame(): Player = behavior.onGameStart(this)

  def doDamage(damage: Int): Player = behavior.onBattleDamage(this, damage)

  def takeDamage(damage: Int): Player =
    val finalDmg = behavior.onDamageTaken(this, damage)
    val newHP = (hp - finalDmg).max(0)
    this.copy(hp = newHP)

  def replaceEquipment(incoming: Equipment): Player =
    val currentOpt = equipment(incoming.slot)
    if currentOpt.forall(incoming.value > _.value) then
      val updated = equipment.updated(incoming.slot, Some(incoming))
      this.copy(equipment = updated)
    else this

  def equipmentList: Iterable[Equipment] =
    equipment.values.flatten

  def earnGold(amount: Double): Player =
    this.copy(gold = gold + amount)

  def spendGold(amount: Double): Option[Player] =
    if gold >= amount then Some(this.copy(gold = gold - amount))
    else None
