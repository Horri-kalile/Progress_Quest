package models.player

import scala.util.Random

sealed trait Skill:
  def name: String

  def manaCost: Int

  def powerLevel: Int

  def use(caster: Player, target: Entity): (Player, Entity)

  def poweredUp: Skill

trait Entity:
  def receiveDamage(amount: Int): Entity

  def receiveHealing(amount: Int): Entity

case class PhysicalDamageSkill(name: String, manaCost: Int, baseMultiplier: Double, powerLevel: Int = 1) extends Skill:
  override def use(caster: Player, target: Entity): (Player, Entity) =
    if caster.currentMP >= manaCost then
      val updatedCaster = caster.copy(mp = caster.currentMP - manaCost)
      val attributes = updatedCaster.attributes
      val eqPower = updatedCaster.equipment.values.flatten.map(_.value).sum
      val multiplier = Random.between(0.1, baseMultiplier)
      val damage = ((attributes.strength + updatedCaster.level + eqPower) * multiplier * powerLevel).toInt
      val updatedTarget = target.receiveDamage(damage)
      (updatedCaster, updatedTarget)
    else (caster, target)

  override def poweredUp: Skill = copy(powerLevel = powerLevel + 1)

case class MagicDamageSkill(name: String, manaCost: Int, baseMultiplier: Double, powerLevel: Int = 1) extends Skill:
  override def use(caster: Player, target: Entity): (Player, Entity) =
    if caster.currentMP >= manaCost then
      val updatedCaster = caster.copy(mp = caster.currentMP - manaCost)
      val attributes = updatedCaster.attributes
      val eqPower = updatedCaster.equipment.values.flatten.map(_.value).sum
      val magicPower = attributes.intelligence + attributes.wisdom
      val multiplier = Random.between(0.1, baseMultiplier)
      val damage = ((magicPower + updatedCaster.level + eqPower) * multiplier * powerLevel).toInt
      val updatedTarget = target.receiveDamage(damage)
      (updatedCaster, updatedTarget)
    else (caster, target)

  override def poweredUp: Skill = copy(powerLevel = powerLevel + 1)

case class HealingSkill(name: String, manaCost: Int, baseMultiplier: Double, powerLevel: Int = 1) extends Skill:
  override def use(caster: Player, target: Entity): (Player, Entity) =
    if caster.currentMP >= manaCost then
      val updatedCaster = caster.copy(mp = caster.currentMP - manaCost)
      val healingPower = caster.attributes.wisdom + caster.level
      val multiplier = Random.between(0.1, baseMultiplier)
      val heal = (healingPower * multiplier * powerLevel).toInt
      val updatedTarget = target.receiveHealing(heal)
      (updatedCaster, updatedTarget)
    else (caster, target)

  override def poweredUp: Skill = copy(powerLevel = powerLevel + 1)

object SkillFactory:
  val allSkills: List[Skill] = List(
    PhysicalDamageSkill("Slash", 4, 1.2),
    PhysicalDamageSkill("Piercing Strike", 6, 1.5),
    PhysicalDamageSkill("Crushing Blow", 8, 1.8),
    PhysicalDamageSkill("Whirlwind", 10, 1.7),
    PhysicalDamageSkill("Power Bash", 5, 1.3),
    PhysicalDamageSkill("Lunging Thrust", 7, 1.6),
    PhysicalDamageSkill("Savage Cleave", 9, 2.0),
    PhysicalDamageSkill("Quick Jab", 3, 1.1),
    PhysicalDamageSkill("Finishing Strike", 11, 2.0),
    PhysicalDamageSkill("Shield Breaker", 6, 1.4),

    MagicDamageSkill("Fireball", 8, 1.9),
    MagicDamageSkill("Ice Lance", 6, 1.5),
    MagicDamageSkill("Arcane Bolt", 4, 1.2),
    MagicDamageSkill("Lightning Surge", 10, 2.0),
    MagicDamageSkill("Shadow Blast", 7, 1.6),
    MagicDamageSkill("Meteor Strike", 12, 2.0),
    MagicDamageSkill("Wind Cutter", 5, 1.3),
    MagicDamageSkill("Dark Pulse", 9, 1.8),
    MagicDamageSkill("Holy Beam", 6, 1.4),
    MagicDamageSkill("Void Ripple", 11, 2.0),

    HealingSkill("Minor Heal", 5, 1.2),
    HealingSkill("Rejuvenation", 8, 1.6),
    HealingSkill("Greater Heal", 10, 1.8),
    HealingSkill("Light's Grace", 7, 1.4),
    HealingSkill("Holy Remedy", 11, 2.0)
  )

  def randomSkill(): Skill =
    Random.shuffle(allSkills).head
