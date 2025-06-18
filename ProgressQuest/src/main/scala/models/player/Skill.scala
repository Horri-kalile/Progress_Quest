package models.player

import scala.util.Random

sealed trait Skill:
  def name: String

  def manaCost: Int

  def use(caster: Player, target: Entity): Unit

trait Entity:
  def receiveDamage(amount: Int): Int

  def receiveHealing(amount: Int): Unit

case class PhysicalDamageSkill(name: String, manaCost: Int, baseMultiplier: Double) extends Skill:
  override def use(caster: Player, target: Entity): Unit =
    if caster.currentMP >= manaCost then
      caster.mp -= manaCost
      val str = caster.attributes.strength
      val eqPower = caster.equipment.map(_.value).sumOption.getOrElse(0)
      val level = caster.level
      val multiplier = Random.between(0.1, baseMultiplier)
      val damage = ((str + level + eqPower) * multiplier).toInt
      target.receiveDamage(damage)

case class MagicDamageSkill(name: String, manaCost: Int, baseMultiplier: Double) extends Skill:
  override def use(caster: Player, target: Entity): Unit =
    if caster.currentMP >= manaCost then
      caster.mp -= manaCost
      val magicPower = caster.attributes.intelligence + caster.attributes.wisdom
      val eqPower = caster.equipment.map(_.value).sumOption.getOrElse(0)
      val level = caster.level
      val multiplier = Random.between(0.1, baseMultiplier)
      val damage = ((magicPower + level + eqPower) * multiplier).toInt
      target.receiveDamage(damage)

case class HealingSkill(name: String, manaCost: Int, baseMultiplier: Double) extends Skill:
  override def use(caster: Player, target: Entity): Unit =
    if caster.currentMP >= manaCost then
      caster.mp -= manaCost
      val healingPower = caster.attributes.wisdom + caster.level
      val multiplier = Random.between(0.1, baseMultiplier)
      val heal = (healingPower * multiplier).toInt
      target.receiveHealing(heal)

object SkillFactory:

  val allSkills: List[Skill] = List(
    // --- Physical Damage Skills ---
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

    // --- Magic Damage Skills ---
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

    // --- Healing Skills ---
    HealingSkill("Minor Heal", 5, 1.2),
    HealingSkill("Rejuvenation", 8, 1.6),
    HealingSkill("Greater Heal", 10, 1.8),
    HealingSkill("Light's Grace", 7, 1.4),
    HealingSkill("Holy Remedy", 11, 2.0),
  )

  def randomSkill(): Skill =
    scala.util.Random.shuffle(allSkills).head