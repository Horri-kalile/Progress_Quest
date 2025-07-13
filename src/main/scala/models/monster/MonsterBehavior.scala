package models.monster

import scala.util.Random

sealed trait MonsterBehavior:
  def apply(monster: Monster): Monster

case object Aggressive extends MonsterBehavior:
  def apply(monster: Monster): Monster =
    monster.copy(attributes = monster.attributes.copy(
      attack = (monster.attributes.attack * 1.25).toInt
    ))

case object Defensive extends MonsterBehavior:
  def apply(monster: Monster): Monster =
    monster.copy(attributes = monster.attributes.copy(
      defense = (monster.attributes.defense * 1.25).toInt
    ))

case object DoubleHP extends MonsterBehavior:
  def apply(monster: Monster): Monster =
    val newHP = (monster.attributes.hp * 1.25).toInt
    monster.copy(attributes = monster.attributes.copy(hp = newHP, currentHp = newHP))

case object Berserk extends MonsterBehavior:
  def apply(monster: Monster): Monster =
    monster.copy(berserk = true)

case object OneShot extends MonsterBehavior:
  def apply(monster: Monster): Monster = // Behavior managed automatically when creating
    monster.copy(attributes = monster.attributes.copy(attack = monster.attributes.attack * 10))

case object Explosive extends MonsterBehavior:
  def apply(monster: Monster): Monster = monster // Behavior used after death

case object Regenerating extends MonsterBehavior:
  def apply(monster: Monster): Monster = // Behavior used after monster attacked
    monster.copy(regenerating = true)

object MonsterBehavior:
  private val allBehaviors: List[MonsterBehavior] = List(
    Aggressive, Defensive, DoubleHP,
    Berserk, OneShot, Explosive, Regenerating
  )

  def randomBehavior: MonsterBehavior = allBehaviors(Random.nextInt(allBehaviors.length))