package models.monster

import models.player.EquipmentModule.{Equipment, EquipmentFactory}
import models.player.ItemModule.{Item, ItemFactory}
import models.player.ItemModule
import models.world.{OriginZone, World}
import util.GameConfig.baseDropChance
import util.MonsterLoader


import scala.util.Random

/**
 * Enumeration of monster types.
 * Each type represents a different category of creatures with distinct characteristics,
 * combat tendencies, and reward profiles.
 */
enum MonsterType:
  /** Beasts tend to be physical attackers with moderate loot chances. */
  case Beast
  /** Undead monsters often have magical vulnerabilities and higher XP rewards. */
  case Undead
  /** Humanoids have balanced stats and good item and equipment drop rates. */
  case Humanoid
  /** Dragons are powerful monsters with high gold and experience rewards. */
  case Dragon
  /** Demons have high attack and experience rewards but lower equipment drops. */
  case Demon
  /** Elements resist magic and have moderate loot chances. */
  case Elemental

/**
 * Data structure for organizing monster names by zones.
 * Used by MonsterLoader to map zone names to lists of appropriate monster names.
 *
 * @param zones Map from zone name strings to lists of monster names available in that zone
 */
case class MonsterNameData(zones: Map[String, List[String]])

/**
 * Represents a monster entity in the game with complete stats, rewards, and behavior.
 *
 * Monsters are the primary opponents that players encounter during their adventures.
 * Each monster has unique attributes, belongs to a specific zone, and provides rewards
 * when defeated. Monsters can have special states like berserk or regenerating that
 * affect their combat behavior.
 *
 * @param name             The display name of the monster
 * @param level            The monster's level, affecting stats and rewards
 * @param monsterType      The category/type of monster (Beast, Undead, etc.)
 * @param originZone       The world zone where this monster originates
 * @param attributes       Combat statistics including HP, attack, defense, and weaknesses
 * @param goldReward       Amount of gold awarded when monster is defeated
 * @param experienceReward Amount of experience points awarded when defeated
 * @param itemReward       Optional consumable item dropped when defeated
 * @param equipReward      Optional equipment piece dropped when defeated
 * @param behavior         AI behavior pattern that controls monster combat decisions
 * @param description      Flavor text describing the monster's appearance or nature
 * @param berserk          Whether the monster is in a berserk state (increased aggression)
 * @param regenerating     Whether the monster can regenerate health over time
 */
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
                  ):

  /**
   * Applies damage to the monster, reducing its current HP.
   * HP cannot go below 0.
   *
   * @param amount The amount of damage to apply
   * @return A new Monster instance with reduced HP
   */
  def receiveDamage(amount: Int): Monster =
    val newHP = (this.attributes.currentHp - amount).max(0)
    val newAttributes = attributes.copy(currentHp = newHP)
    this.copy(attributes = newAttributes)

  /**
   * Heals the monster, increasing its current HP.
   * HP cannot exceed the monster's maximum HP.
   *
   * @param amount The amount of healing to apply
   * @return A new Monster instance with increased HP
   */
  def receiveHealing(amount: Int): Monster =
    val newHP = (this.attributes.currentHp + amount).min(attributes.hp)
    val newAttributes = attributes.copy(currentHp = newHP)
    this.copy(attributes = newAttributes)

  /**
   * Calculates the damage dealt when the monster explodes or uses special attacks.
   * Currently, returns the monster's base attack value.
   *
   * @return The amount of explosion damage the monster can deal
   */
  def explosionDamage: Int =
    this.attributes.attack

  /**
   * Checks if the monster has been defeated (HP reached 0).
   *
   * @return true if the monster is dead, false otherwise
   */
  def isDead: Boolean = attributes.currentHp == 0

/**
 * Factory object for creating monsters with appropriate stats and rewards.
 *
 * This object handles the procedural generation of monsters based on player level,
 * zone requirements, and random factors. It manages monster name loading,
 * attribute scaling, reward calculation, and zone-specific buffs.
 */
object MonstersFactory:
  /**
   * Preloaded monster names organized by zone for consistent creature spawning.
   * Loaded once at startup from external data files.
   */
  private lazy val monsterNames: Map[String, List[String]] = MonsterLoader.loadMonsters()

  /**
   * Generates a random monster appropriate for the specified zone and player stats.
   *
   * Creates a monster with level and attributes scaled to provide appropriate
   * challenge for the player. Optionally creates "strong" monsters with enhanced
   * stats and better rewards for special encounters.
   *
   * @param zone        The world zone where the monster will appear
   * @param playerLevel Player's current level for stat scaling
   * @param playerLucky Player's luck attribute affecting reward generation
   * @param strong      Whether to create a powerful version with enhanced stats
   * @return A fully configured Monster instance ready for combat
   */
  def randomMonsterForZone(zone: OriginZone, playerLevel: Int, playerLucky: Int, strong: Boolean = false): Monster =
    val names = monsterNames.getOrElse(zone.toString, Nil)
    val name = Random.shuffle(names).headOption.getOrElse("Unknown Monster")
    val monsterLevel = scaleLevel(playerLevel, strong)
    val monsterType = Random.shuffle(MonsterType.values.toList).head
    val attributes = generateAttributes(monsterLevel, strong, monsterType)
    val rewards: (Int, Int, Option[Item], Option[Equipment]) = generateRewards(monsterLevel, playerLevel, playerLucky, strong, monsterType)
    val behavior = MonsterBehavior.randomBehavior
    val monster = Monster(
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
    World.applyZoneBuffs(behavior(monster), zone)

  /**
   * Calculates appropriate monster level based on player level and encounter type.
   *
   * Regular monsters are within 1 level of the player (±1).
   * Strong monsters are 1-2 levels above the player for increased challenge.
   * Minimum level is always 1.
   *
   * @param playerLevel The player's current level
   * @param strong      Whether this is a strong encounter
   * @return Scaled monster level appropriate for the encounter
   */
  private def scaleLevel(playerLevel: Int, strong: Boolean): Int =
    val base = if strong then playerLevel + Random.between(1, 2)
    else playerLevel + Random.between(-1, 1)
    Math.max(base, 1)

  /**
   * Generates monster combat attributes scaled to the monster's level.
   *
   * Strong monsters have reduced weaknesses (0.5-1.0 multiplier, taking less damage)
   * while regular monsters have standard or increased weaknesses (1.0-1.5 multiplier).
   * All stats scale linearly with monster level.
   *
   * @param level  The monster's level
   * @param strong Whether this is a strong monster with enhanced stats
   * @return MonsterAttributes with HP, attack, defense, and damage weaknesses
   */
  private def generateAttributes(level: Int, strong: Boolean, monsterType: MonsterType): MonsterAttributes =
    val (physicalWeakness, magicalWeakness) = if strong then
      (Random.between(0.5, 1.0), Random.between(0.5, 1.0))
    else
      (Random.between(1.0, 1.5), Random.between(1.0, 1.5))
    val hp = Random.between(20, 40) * level
    val attack = Random.between(1, 5) * level
    val defense = Random.between(1, 5) * level
    val base = MonsterAttributes(hp, hp, attack, defense, physicalWeakness, magicalWeakness)
    applyTypeModifiers(base, monsterType)

  /**
   * Applies monster type-specific modifications to base attributes.
   *
   * Example: Undead take more magical damage but less physical, Dragons have high HP etc...
   *
   * @param attrs Base MonsterAttributes to modify.
   * @param t     MonsterType specifying which modifiers to apply.
   * @return Modified MonsterAttributes with type-specific adjustments.
   */
  private def applyTypeModifiers(attrs: MonsterAttributes, t: MonsterType): MonsterAttributes = t match
    case MonsterType.Undead => attrs.copy(weaknessMagic = (attrs.weaknessMagic * 1.5).min(2.0), weaknessPhysical = (attrs.weaknessPhysical * 0.8).max(0.5))
    case MonsterType.Dragon => attrs.copy(hp = (attrs.hp * 1.1).toInt, currentHp = (attrs.currentHp * 1.1).toInt)
    case MonsterType.Demon => attrs.copy(attack = (attrs.attack * 1.1).toInt)
    case MonsterType.Elemental => attrs.copy(weaknessPhysical = attrs.weaknessPhysical * 1.1)
    case MonsterType.Beast => attrs.copy(weaknessMagic = (attrs.weaknessMagic * 1.3).min(2.0))
    case MonsterType.Humanoid => attrs.copy(defense = (attrs.defense * 0.9).max(0.5).toInt)


  /**
   * Calculates rewards granted by a monster based on its level, type, player stats, and strength.
   *
   * Rewards include gold, experience points, and optional item drops.
   * MonsterType influences multipliers on gold and XP, and increases item drop chances.
   *
   * @param level       Monster's level for base reward scaling.
   * @param playerLevel Player's level to scale equipment drops.
   * @param playerLucky Player's luck attribute, increasing drop chances.
   * @param strong      Whether monster is a strong variant doubling rewards.
   * @param monsterType The monster's type affecting reward profile.
   * @return Tuple of (gold: Int, experience: Int, item: Option[Item], equipment: Option[Equipment])
   */
  private def generateRewards(level: Int, playerLevel: Int, playerLucky: Int, strong: Boolean, monsterType: MonsterType): (Int, Int, Option[Item], Option[Equipment]) =
    val baseFactor = if strong then 2 else 1
    val (goldMult, expMult, itemDropBonus) = monsterType match
      case MonsterType.Dragon => (1.5, 1.5, 0.15)
      case MonsterType.Demon => (1.2, 1.5, 0.05)
      case MonsterType.Humanoid => (1.0, 1.0, 0.03)
      case MonsterType.Beast => (1.2, 0.8, 0.04)
      case MonsterType.Undead => (0.8, 1.3, 0.10)
      case MonsterType.Elemental => (1.0, 1.0, 0.02)

    val gold = (Random.between(1, 20) * level * goldMult * baseFactor).toInt
    val exp = (Random.between(1, 20) * level * expMult * baseFactor).toInt

    val randomItem: Option[Item] = ItemFactory.probBasedCreate(baseDropChance + itemDropBonus).createRandomItem(playerLucky)

    (gold, exp, randomItem, EquipmentFactory.probBased(playerLucky = playerLucky, playerLevel = playerLevel))






