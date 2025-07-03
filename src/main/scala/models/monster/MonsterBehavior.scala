package models.monster

trait MonsterBehavior:
  def apply(monster: Monster): Unit

object Aggressive extends MonsterBehavior:
  def apply(monster: Monster): Unit =
    monster.attributes = monster.attributes.copy(attack = (monster.attributes.attack * 1.5).toInt)

object Defensive extends MonsterBehavior:
  def apply(monster: Monster): Unit =
    monster.attributes = monster.attributes.copy(defense = (monster.attributes.defense * 1.5).toInt)

object DoubleHP extends MonsterBehavior:
  def apply(monster: Monster): Unit =
    monster.attributes = monster.attributes.copy(hp = monster.attributes.hp * 2)

object Berserker extends MonsterBehavior:
  def apply(monster: Monster): Unit =
    monster.berserk = true // logica nella classe Monster

object OneShot extends MonsterBehavior:
  def apply(monster: Monster): Unit = {} // gestione nel metodo attaccaGiocatore

object Explosive extends MonsterBehavior:
  def apply(monster: Monster): Unit = {} // gestione in prendiDanno

object Regenerating extends MonsterBehavior:
  def apply(monster: Monster): Unit =
    monster.regenerating = true // logica nella classe Monster
