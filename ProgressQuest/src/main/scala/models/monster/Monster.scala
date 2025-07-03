package models.monster

import util.MonsterLoader
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
                    behavior: MonsterBehavior,
                    description: String
                  ):
  var regenerating: Boolean = false
  var berserk: Boolean = false
  private var currentHP: Int = attributes.hp

  behavior.apply(this) // Apply directly the behavior of the monster

  def attackPlayer(playerLevel: Int): Int =
    val baseDamage = attributes.attack - (playerLevel * 2)
    val damage = if (berserk) baseDamage + ((attributes.hp - currentHP) / 10) else baseDamage
    if (damage < 0) 0 else damage

  def takeDamage(amount: Int): Unit =
    currentHP -= amount
    if behavior == Explosive && isDead then println(s"$name esplode!")

  private def isDead: Boolean = currentHP <= 0

  def dropLoot(): String =
    s"${name} ha droppato un oggetto raro!"

  def regenerate(): Unit =
    if regenerating && !isDead then currentHP += 5

object MonstersFactory:
  private val monsterNames: Map[String, List[String]] = MonsterLoader.loadMonsters()

  def randomMonsterForZone(zone: OriginZone, playerLevel: Int, strong: Boolean = false): Monster =
    val names = monsterNames.getOrElse(zone.toString, Nil)
    val name = Random.shuffle(names).headOption.getOrElse("Unknown Monster")
    val monsterLevel = scaleLevel(playerLevel, strong)
    val attributes = generateAttributes(monsterLevel, strong)
    val rewards: (Int, Int) = generateRewards(monsterLevel, strong)
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
      behavior = behavior,
      description = s"A ${if strong then "powerful " else ""}$name from the $zone"
    )

  private def scaleLevel(playerLevel: Int, strong: Boolean): Int =
    val base = if strong then playerLevel + Random.between(2, 5)
    else playerLevel + Random.between(-2, 2)
    Math.max(base, 1)

  private def generateAttributes(level: Int, strong: Boolean): MonsterAttributes =
    val factor = if strong then 1.5 else 1.0
    val hp = ((level * 20 + Random.between(0, 20)) * factor).toInt
    val attack = ((level * 2 + Random.between(0, 5)) * factor).toInt
    val defense = ((level * 2 + Random.between(0, 5)) * factor).toInt
    MonsterAttributes(hp, attack, defense)

  private def generateRewards(level: Int, strong: Boolean): (Int, Int) =
    val factor = if strong then 2 else 1
    val gold = level * 5 * factor + Random.between(0, 20)
    val exp = level * 10 * factor + Random.between(10, 30)
    (gold, exp)






