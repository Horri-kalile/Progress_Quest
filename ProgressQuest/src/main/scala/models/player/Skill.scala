package models.player

sealed trait Skill:
  def name: String

  def manaCost: Int

  def use(caster: Player, target: Entity): Unit

trait Entity:
  def receiveDamage(amount: Int): Int

  def receiveHealing(amount: Int): Unit

case class DamagingSkill(name: String, manaCost: Int, damage: Int) extends Skill:
  override def use(caster: Player, target: Entity): Unit =
    if caster.currentMP >= manaCost then
      caster.mp -= manaCost
      target.receiveDamage(damage)

case class HealingSkill(name: String, manaCost: Int, healAmount: Int) extends Skill:
  override def use(caster: Player, target: Entity): Unit =
    if caster.currentMP >= manaCost then
      caster.mp -= manaCost
      target.receiveHealing(healAmount)
