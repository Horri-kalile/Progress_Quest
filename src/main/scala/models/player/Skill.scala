package models.player

import util.SkillLoader

import scala.util.Random

/** A container holding categorized skill name pools. */
case class SkillNameData(physical: List[String], magic: List[String], healing: List[String])

/** Defines what kind of effect a skill produces. */
enum SkillEffectType:
  case Physical, Magic, Healing

/** A base trait for all skills. */
sealed trait Skill:
  def name: String

  def manaCost: Int

  def powerLevel: Int

  def baseMultiplier: Double

  def effectType: SkillEffectType

  def poweredUp: Skill

/** A simple implementation of [[Skill]], used as a general-purpose ability. */
case class GenericSkill(
    name: String,
    effectType: SkillEffectType,
    manaCost: Int,
    baseMultiplier: Double,
    powerLevel: Int = 1
) extends Skill:
  override def poweredUp: Skill = copy(powerLevel = powerLevel + 1)

/** Factory for generating random or class-based skills.
  *
  * This utility loads skill names and provides methods for:
  *   - Generating a class-appropriate starting skill
  *   - Generating a fully random skill from any category
  */
object SkillFactory:

  private lazy val data: SkillNameData = SkillLoader.loadSkillNames()

  /** Generates a starting skill based on the player's class and level.
    *
    * @param playerLevel
    *   Level to scale mana cost and power
    * @param classType
    *   Player's class
    * @return
    *   An optional [[Skill]] matching the class
    */
  def generateStartingSkill(playerLevel: Int, classType: ClassType): Option[Skill] =
    val mana = randomManaCost(playerLevel)
    val multiplier = randomMultiplier()
    classType match
      case ClassType.Mage => Some(makeSkill(data.magic, SkillEffectType.Magic, mana, multiplier))
      case ClassType.Paladin => Some(makeSkill(data.healing, SkillEffectType.Healing, mana, multiplier))
      case ClassType.Assassin => Some(makeSkill(data.physical, SkillEffectType.Physical, mana, multiplier))
      case ClassType.Cleric => Some(randomSkill(playerLevel))
      case _ => None

  /** Generates a completely random skill from any effect type.
    *
    * @param playerLevel
    *   Optional level to scale mana cost
    */
  def randomSkill(playerLevel: Int = 1): Skill =
    val mana = randomManaCost(playerLevel)
    val multiplier = randomMultiplier()
    Random.nextInt(3) match
      case 0 => makeSkill(data.physical, SkillEffectType.Physical, mana, multiplier)
      case 1 => makeSkill(data.magic, SkillEffectType.Magic, mana, multiplier)
      case _ => makeSkill(data.healing, SkillEffectType.Healing, mana, multiplier)

  private def makeSkill(names: List[String], effect: SkillEffectType, mana: Int, multiplier: Double): GenericSkill =
    GenericSkill(Random.shuffle(names).head, effect, mana, multiplier)

  private def randomManaCost(playerLevel: Int): Int =
    Random.between(4, 13) * playerLevel

  private def randomMultiplier(): Double =
    Random.between(0.6, 1.5)
