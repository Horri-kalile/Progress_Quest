package models.monster

import scala.util.Random

sealed trait MonsterBehavior:
  def apply(monster: Monster): Unit

case object Aggressive extends MonsterBehavior:
  def apply(monster: Monster): Unit =
    monster.attributes.copy(attack = (monster.attributes.attack * 1.5).toInt)

case object Defensive extends MonsterBehavior:
  def apply(monster: Monster): Unit =
    monster.attributes.copy(defense = (monster.attributes.defense * 1.5).toInt)

case object DoubleHP extends MonsterBehavior:
  def apply(monster: Monster): Unit =
    monster.attributes.copy(hp = monster.attributes.hp * 2)

case object Berserk extends MonsterBehavior:
  def apply(monster: Monster): Unit =
    monster.berserk = true // logica nella classe Monster

case object OneShot extends MonsterBehavior:
  def apply(monster: Monster): Unit = {} // gestione nel metodo attaccaGiocatore

case object Explosive extends MonsterBehavior:
  def apply(monster: Monster): Unit = {} // gestione in prendiDanno

case object Regenerating extends MonsterBehavior:
  def apply(monster: Monster): Unit =
    monster.regenerating = true // logica nella classe Monster

case object MonsterBehavior:
  private val allBehaviors: List[MonsterBehavior] = List(
    Aggressive, Defensive, DoubleHP,
    Berserk, OneShot, Explosive, Regenerating
  )

  def randomBehavior: MonsterBehavior = allBehaviors(Random.nextInt(allBehaviors.length))