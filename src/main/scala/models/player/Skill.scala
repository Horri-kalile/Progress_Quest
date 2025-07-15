package models.player

import util.SkillLoader

import scala.util.Random

case class SkillNameData(physical: List[String], magic: List[String], healing: List[String])

enum SkillEffectType:
  case Physical, Magic, Healing

sealed trait Skill:
  def name: String

  def manaCost: Int

  def powerLevel: Int

  def baseMultiplier: Double

  def effectType: SkillEffectType

  def poweredUp: Skill

case class GenericSkill(
                         name: String,
                         effectType: SkillEffectType,
                         manaCost: Int,
                         baseMultiplier: Double,
                         powerLevel: Int = 1
                       ) extends Skill:

  override def poweredUp: Skill = copy(powerLevel = powerLevel + 1)

object SkillFactory:
  private val data: SkillNameData = SkillLoader.loadSkillNames()

  def generateStartingSkill(playerLevel: Int, classType: ClassType): Option[Skill] =
    val mana = Random.between(4, 13) * playerLevel
    val multiplier = Random.between(1.1, 2.0).toInt
    classType match
      case ClassType.Mage => Some(GenericSkill(Random.shuffle(data.magic).head, SkillEffectType.Magic, mana, multiplier))
      case ClassType.Paladin => Some(GenericSkill(Random.shuffle(data.healing).head, SkillEffectType.Healing, mana, multiplier))
      case ClassType.Assassin => Some(GenericSkill(Random.shuffle(data.physical).head, SkillEffectType.Physical, mana, multiplier))
      case ClassType.Cleric => Some(randomSkill())
      case _ => None

  def randomSkill(): Skill =
    val skillType = Random.nextInt(3)
    val (name, effectType) = skillType match
      case 0 => (Random.shuffle(data.physical).head, SkillEffectType.Physical)
      case 1 => (Random.shuffle(data.magic).head, SkillEffectType.Magic)
      case 2 => (Random.shuffle(data.healing).head, SkillEffectType.Healing)

    val mana = Random.between(4, 13)
    val multiplier = Random.between(1.1, 2.0).toInt

    GenericSkill(name, effectType, mana, multiplier)
