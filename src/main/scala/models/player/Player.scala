package models.player

import models.event.Mission
import models.monster.OriginZone
import models.world.World

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
                   currentHp: Int,
                   currentMp: Int,
                   baseAttributes: Attributes,
                   behaviorType: BehaviorType,
                   inventory: Map[Item, Int] = Map.empty,
                   equipment: Map[EquipmentSlot, Option[Equipment]] = EquipmentSlot.values.map(_ -> None).toMap,
                   skills: List[Skill] = List.empty,
                   missions: List[Mission] = List.empty,
                   gold: Double,
                   currentZone: OriginZone
                 ) extends Entity:

  val behavior: Behavior = BehaviorResolver.getBehavior(behaviorType)

  def attributes: Attributes =
    val bonuses = equipment.values.flatten.map(_.statBonus)
    bonuses.foldLeft(baseAttributes)(_ + _)

  def inventorySize: Int = inventory.size
  

  def levelUp(): Player =
    this.copy(
      level = level + 1,
      exp = 0,
      currentHp = hp,
      currentMp = mp
    ).powerUpAttributes()

  def levelDown(): Player =
    this.copy(
      level = (level - 1).max(1),
      exp = 0,
      currentHp = hp,
      currentMp = mp
    ).powerDownAttributes()

  /*TODO calcolo danno*/
  override def receiveDamage(amount: Int): Player =
    val finalDamage = behavior.onDamageTaken(this, amount)
    val newHP = (hp - finalDamage).max(0)
    this.copy(hp = newHP)

  override def receiveHealing(amount: Int): Player =
    val newHP = (hp + amount).min(hp)
    this.copy(hp = newHP)

  def useSkill(skill: Skill): Boolean = mp match
    case a if mp >= skill.manaCost => this.copy(mp = mp - skill.manaCost); true
    case _ => false

  def learnSkill(skill: Skill): Player =
    if skills.exists(_.name == skill.name) then this
    else this.copy(skills = skill :: skills)

  def obtainItem(item: Item, amount: Int = 1): Player =
    val updated = inventory.updatedWith(item) {
      case Some(count) => Some(count + amount)
      case None => Some(amount)
    }
    this.copy(inventory = updated)


  def sellItem(): (Player, String) =
    if inventory.isEmpty then (this, "Inventory empty. Nothing sold.")
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
      (updatedPlayer, s"Sold $toRemove × ${item.name} for $totalGold gold.")


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

  def doDamage(damage: Int): Int = behavior.onBattleDamage(this, damage)

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

  def restore(): Player =
    this.copy(currentHp = hp, currentMp = mp)

  def addMission(m: Mission): Player = this.copy(missions = m :: missions)

  def activeMissions: List[Mission] = missions.filterNot(_.isCompleted)

  def progressMission(mission: Mission): Player =
    val updatedMissions = missions.map { m =>
      if m.id == mission.id then m.progressed() else m
    }
    this.copy(missions = updatedMissions)

  def powerUpAttributes(): Player =
    this.copy(baseAttributes = baseAttributes.incrementRandomAttributes())

  def powerDownAttributes(): Player =
    this.copy(baseAttributes = baseAttributes.decrementRandomAttributes())

object PlayerFactory:
  def createDefaultPlayer(name: String, identity: Identity, attributes: Attributes, behavior: BehaviorType): Player =
    val hp = attributes.constitution * 5
    val mp = attributes.intelligence * 2
    val gold = 0.0

    Player(
      name = name,
      identity = identity,
      level = 1,
      exp = 0,
      hp = hp,
      mp = mp,
      currentHp = hp,
      currentMp = mp,
      baseAttributes = attributes,
      behaviorType = behavior,
      gold = gold,
      currentZone = OriginZone.Plains
    )
