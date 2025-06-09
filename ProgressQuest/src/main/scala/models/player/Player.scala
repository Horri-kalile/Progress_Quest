package models.player

import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Random

class Player(val name: String,
             val identity: Identity,
             var level: Int,
             var exp: Int,
             var hp: Int,
             var mp: Int,
             var attributes: Attributes,
             val behaviorType: BehaviorType
            ) extends Entity:

  private val inventory: ListBuffer[Item] = ListBuffer.empty
  private var equipment: HashMap[EquipmentSlot, Option[Equipment]] = HashMap.from(EquipmentSlot.values.map(slot => slot -> None))
  val skills: List[Skill] = List.empty
  private val behavior: Behavior = BehaviorResolver.getBehavior(behaviorType)

  /*TODO algoritmo di calcolo*/
  def maxHP: Int = attributes.constitution * 10

  def maxMP: Int = attributes.intelligence * 5

  def currentHP: Int = hp

  def currentMP: Int = mp

  //TODO calcolare exp necessaria in base al livello
  def gainExp(amount: Int): Unit =
    exp += amount
    if exp >= level * 100 then levelUp()

  /*TODO aggiornare stats*/
  def levelUp(): Unit =
    level += 1
    exp = 0
    hp = maxHP
    mp = maxMP

  /*TODO calcolo danno*/
  override def receiveDamage(amount: Int): Int =
    val finalDamage = ???
    hp = 0
    finalDamage

  override def receiveHealing(amount: Int): Unit =
    hp = math.min(maxHP, hp + amount)

  def useSkill(skill: Skill): Boolean = mp match
    case a if mp >= skill.manaCost => mp -= skill.manaCost; true
    case _ => false

  def equip(item: Equipment): Unit =
    equipment = equipment.updated(item.slot, Some(item))
    recalculateAttributes()

  def unequipped(slot: EquipmentSlot): Unit =
    equipment = equipment.updated(slot, None)
    recalculateAttributes()

  private def recalculateAttributes(): Unit =
    val bonus = equipment.values.flatten.foldLeft(attributes)(_ + _.statBonus)
    attributes = attributes + bonus


  def useItem(itemName: String): Boolean =
    inventory.find(_.name == itemName).foreach { item =>
      item.applyTo(this)
      inventory -= item
    }
    true

  def emptyInventory(): Boolean =
    inventory.isEmpty

  def stealFromInventory(): Option[Item] =
    if inventory.nonEmpty then
      val index = Random.nextInt(inventory.size)
      Some(inventory.remove(index))
    else None

  def startGame(): Unit =
    // Apply any behavior effects that happen on game start
    behavior.onGameStart(this)


  def doDamage(damage: Int): Unit =
    // Any behavior effects during a battle
    behavior.onBattleDamage(this, damage)

  def takeDamage(damage: Int): Unit =
    // Behavior can modify the incoming damage
    val finalDamage = behavior.onDamageTaken(this, damage)
    hp = (hp - finalDamage).max(0)
  // println (s"$name took $finalDamage damage, HP now $hp")

  def isAlive: Boolean = hp > 0

  def replaceEquipment(current: Equipment, incoming: Equipment): Unit =
    if incoming.value > current.value then equipment = equipment.updated(incoming.slot, Some(incoming))
    recalculateAttributes()