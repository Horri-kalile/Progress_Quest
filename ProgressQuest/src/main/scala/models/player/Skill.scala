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

  def effectType: SkillEffectType

  def use(caster: Player, target: Entity): (Player, Entity)

  def poweredUp: Skill

trait Entity:
  def receiveDamage(amount: Int): Entity

  def receiveHealing(amount: Int): Entity

case class GenericSkill(
                         name: String,
                         effectType: SkillEffectType,
                         manaCost: Int,
                         baseMultiplier: Double,
                         powerLevel: Int = 1
                       ) extends Skill:

  override def use(caster: Player, target: Entity): (Player, Entity) =
    if caster.currentMP < manaCost then return (caster, target)

    val updatedCaster = caster.copy(mp = caster.currentMP - manaCost)
    val multiplier = Random.between(0.1, baseMultiplier)

    effectType match
      case SkillEffectType.Physical =>
        val attributes = caster.attributes
        val eqPower = caster.equipment.values.flatten.map(_.value).sum
        val damage = ((attributes.strength + caster.level + eqPower) * multiplier * powerLevel).toInt
        (updatedCaster, target.receiveDamage(damage))

      case SkillEffectType.Magic =>
        val attributes = caster.attributes
        val eqPower = caster.equipment.values.flatten.map(_.value).sum
        val magicPower = attributes.intelligence + attributes.wisdom
        val damage = ((magicPower + caster.level + eqPower) * multiplier * powerLevel).toInt
        (updatedCaster, target.receiveDamage(damage))

      case SkillEffectType.Healing =>
        val healingPower = caster.attributes.wisdom + caster.level
        val heal = (healingPower * multiplier * powerLevel).toInt
        (updatedCaster, target.receiveHealing(heal))

  override def poweredUp: Skill = copy(powerLevel = powerLevel + 1)

object SkillFactory:
  private val data: SkillNameData = SkillLoader.loadSkillNames()

  def randomSkill(): Skill =
    val skillType = Random.nextInt(3)
    val (name, effectType) = skillType match
      case 0 => (Random.shuffle(data.physical).head, SkillEffectType.Physical)
      case 1 => (Random.shuffle(data.magic).head, SkillEffectType.Magic)
      case 2 => (Random.shuffle(data.healing).head, SkillEffectType.Healing)

    val mana = Random.between(4, 13)
    val multiplier = Random.between(1.1, 2.0)

    GenericSkill(name, effectType, mana, multiplier)
