package models.monster

import models.player.{Entity, Equipment, Item, ItemFactory, Player, EquipmentFactory}
import util.MonsterLoader
import util.RandomFunctions
import scala.util.Random

enum MonsterType:
  case Beast, Undead, Humanoid, Dragon, Demon, Elemental

case class MonsterNameData(zones: Map[String, List[String]])

case class Monster(
                    name: String,
                    level: Int,
                    monsterType: MonsterType,
                    originZone: OriginZone,
                    attributes: MonsterAttributes,
                    goldReward: Int,
                    experienceReward: Int,
                    itemReward: Option[Item],
                    equipReward: Option[Equipment],
                    behavior: MonsterBehavior,
                    description: String
                  ) extends Entity:
  var regenerating: Boolean = false
  var berserk: Boolean = false

  behavior.apply(this) // Apply directly the behavior of the monster

  def takeDamage(amount: Int): (Monster, Option[Int]) =
    val damaged = this.receiveDamage(amount)

    val explosion =
      if damaged.behavior == Explosive && damaged.isDead then Some(damaged.explosionDamage)
      else None

    (damaged, explosion)

  override def receiveDamage(amount: Int): Monster =
    val newHP = (this.attributes.currentHp - amount).max(0)
    val newAttributes = attributes.copy(currentHp = newHP)
    this.copy(attributes = newAttributes)

  override def receiveHealing(amount: Int): Monster =
    val newHP = (this.attributes.currentHp + Random.between(10 * level, 50 * level)).min(attributes.hp)
    val newAttributes = attributes.copy(currentHp = newHP)
    this.copy(attributes = newAttributes)


  private def explosionDamage: Int =
    this.attributes.attack


  private def isDead: Boolean = attributes.currentHp <= 0

  def dropLoot(): String =
    s"${name} ha droppato un oggetto raro!"

  def regenerate(): Unit =
    if regenerating && !isDead then receiveHealing(1)

object MonstersFactory:
  private val monsterNames: Map[String, List[String]] = MonsterLoader.loadMonsters()

  def randomMonsterForZone(zone: OriginZone, playerLevel: Int, playerLucky: Int, strong: Boolean = false): Monster =
    val names = monsterNames.getOrElse(zone.toString, Nil)
    val name = Random.shuffle(names).headOption.getOrElse("Unknown Monster")
    val monsterLevel = scaleLevel(playerLevel, strong)
    val attributes = generateAttributes(monsterLevel, strong)
    val rewards: (Int, Int, Option[Item], Option[Equipment]) = generateRewards(monsterLevel, playerLevel, playerLucky, strong)
    val monsterType = Random.shuffle(MonsterType.values.toList).head
    val behavior = MonsterBehavior.randomBehavior

    Monster(
      name = name,
      level = monsterLevel,
      monsterType = monsterType,
      originZone = zone,
      attributes = attributes,
      goldReward = rewards._1,
      experienceReward = rewards._2,
      itemReward = rewards._3,
      equipReward = rewards._4,
      behavior = behavior,
      description = s"A ${if strong then "powerful " else ""}$name from the $zone"
    )

  private def scaleLevel(playerLevel: Int, strong: Boolean): Int =
    val base = if strong then playerLevel + Random.between(2, 5)
    else playerLevel + Random.between(-2, 2)
    Math.max(base, 1)

  private def generateAttributes(level: Int, strong: Boolean): MonsterAttributes =
    val factor = if strong then 2.0 else 1.0
    val hp = (Random.between(50 * level, 100 * level) * factor).toInt
    val attack = (Random.between(10 * level, 20 * level) * factor).toInt
    val defense = (Random.between(5 * level, 10 * level) * factor).toInt
    MonsterAttributes(hp, hp, attack, defense)

  private def generateRewards(level: Int, playerLevel: Int, playerLucky: Int, strong: Boolean): (Int, Int, Option[Item], Option[Equipment]) =
    val factor = if strong then 2 else 1
    val gold = Random.between(1 * level, 50 * level) * factor
    val exp = Random.between(1 * level, 50 * level) * factor
    var randomItem: Option[Item] = None
    if RandomFunctions.randomDropFlags(playerLucky) then
      randomItem = Some(ItemFactory.randomItem(playerLucky))

    (gold, exp, randomItem, EquipmentFactory.generateRandomEquipment(playerLucky = playerLucky, playerLevel = playerLevel))






