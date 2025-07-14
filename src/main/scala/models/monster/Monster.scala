package models.monster

import models.player.{Entity, Equipment, EquipmentFactory, Item, ItemFactory, Player}
import models.world.World
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
                    description: String,
                    berserk: Boolean = false,
                    regenerating: Boolean = false
                  ) extends Entity:

  override def receiveDamage(amount: Int): Monster =
    val newHP = (this.attributes.currentHp - amount).max(0)
    val newAttributes = attributes.copy(currentHp = newHP)
    this.copy(attributes = newAttributes)

  override def receiveHealing(amount: Int): Monster =
    val newHP = (this.attributes.currentHp + amount).min(attributes.hp)
    val newAttributes = attributes.copy(currentHp = newHP)
    this.copy(attributes = newAttributes)


  def explosionDamage: Int =
    this.attributes.attack

  def isDead: Boolean = attributes.currentHp <= 0


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
    val monster = Monster(name = name, level = monsterLevel, monsterType = monsterType, originZone = zone, attributes = attributes, goldReward = rewards._1, experienceReward = rewards._2, itemReward = rewards._3, equipReward = rewards._4, behavior = behavior, description = s"A ${if strong then "powerful " else ""}$name from the $zone")
    println(monster)
    World.applyZoneBuffs(behavior(monster), zone)

  private def scaleLevel(playerLevel: Int, strong: Boolean): Int =
    val base = if strong then playerLevel + Random.between(2, 5)
    else playerLevel + Random.between(-1, 1)
    Math.max(base, 1)

  private def generateAttributes(level: Int, strong: Boolean): MonsterAttributes =
    val factor = if strong then 2.0 else 1.0
    val hp = (Random.between(20 * level, 80 * level) * factor).toInt
    val attack = (Random.between(10 * level, 20 * level) * factor).toInt
    val defense = (Random.between(5 * level, 10 * level) * factor).toInt
    MonsterAttributes(hp, hp, attack, defense, factor, factor)

  private def generateRewards(level: Int, playerLevel: Int, playerLucky: Int, strong: Boolean): (Int, Int, Option[Item], Option[Equipment]) =
    val factor = if strong then 2 else 1
    val gold = Random.between(1 * level, 50 * level) * factor
    val exp = Random.between(1 * level, 50 * level) * factor
    var randomItem: Option[Item] = None
    if RandomFunctions.randomDropFlags(playerLucky) then
      randomItem = Some(ItemFactory.randomItem(playerLucky))

    (gold, exp, randomItem, EquipmentFactory.generateRandomEquipment(playerLucky = playerLucky, playerLevel = playerLevel))






