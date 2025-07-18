package models.monster

import scala.util.Random

/** MonsterBehavior represents different special abilities that monsters can exhibit during combat. Each behavior
  * applies specific modifications to a monster's base attributes, creating varied and strategic combat encounters.
  * Behaviors are applied when monsters are created and can affect stats, special states, or combat mechanics.
  *
  * The apply method transforms a base monster into a specialized variant with enhanced or modified capabilities based
  * on the specific behavior pattern.
  */
sealed trait MonsterBehavior:
  /** Applies this behavior's modifications to a monster.
    *
    * @param monster
    *   The base monster to modify
    * @return
    *   A new Monster instance with behavior-specific modifications applied
    */
  def apply(monster: Monster): Monster

/** Aggressive behavior increases the monster's offensive capabilities.
  */
case object Aggressive extends MonsterBehavior:
  /** Increases the monster's attack power
    *
    * @param monster
    *   The monster to make aggressive
    * @return
    *   Monster with boosted attack attribute
    */
  def apply(monster: Monster): Monster =
    monster.copy(attributes = monster.attributes.copy(
      attack = (monster.attributes.attack * 1.25).toInt
    ))

/** Defensive behavior increases the monster's protective capabilities.
  */
case object Defensive extends MonsterBehavior:
  /** Increases the monster's defense power
    *
    * @param monster
    *   The monster to make defensive
    * @return
    *   Monster with boosted defense attribute
    */
  def apply(monster: Monster): Monster =
    monster.copy(attributes = monster.attributes.copy(
      defense = (monster.attributes.defense * 1.25).toInt
    ))

/** MoreHp behavior increases the monster's health pool significantly.
  */
case object MoreHp extends MonsterBehavior:
  /** Increases the monster's maximum and current HP
    *
    * @param monster
    *   The monster to give more health
    * @return
    *   Monster with increased HP values
    */
  def apply(monster: Monster): Monster =
    val newHP = (monster.attributes.hp * 1.25).toInt
    monster.copy(attributes = monster.attributes.copy(hp = newHP, currentHp = newHP))

/** Berserk behavior puts the monster into an enraged state.
  *
  * Berserk monsters have the berserk flag set to true, which may trigger special combat behaviors or interactions
  * during battle.
  */
case object Berserk extends MonsterBehavior:
  /** Sets the monster's berserk state to true.
    *
    * @param monster
    *   The monster to make berserk
    * @return
    *   Monster with berserk flag enabled
    */
  def apply(monster: Monster): Monster =
    monster.copy(berserk = true)

/** OneShot behavior creates monsters with devastating single attacks.
  *
  * OneShot monsters have double attack power, making them capable of dealing massive damage in single strikes.
  */
case object OneShot extends MonsterBehavior:
  /** Doubles the monster's attack power.
    *
    * @param monster
    *   The monster
    * @return
    *   Monster with doubled attack attribute
    */
  def apply(monster: Monster): Monster = // Behavior managed automatically when creating
    monster.copy(attributes = monster.attributes.copy(attack = monster.attributes.attack * 2))

/** Explosive behavior enables post-death explosion mechanics.
  */
case object Explosive extends MonsterBehavior:
  /** Returns the monster unchanged as explosive behavior is handled after death.
    *
    * @param monster
    *   The monster to mark as explosive
    * @return
    *   The same monster instance (explosion handled elsewhere)
    */
  def apply(monster: Monster): Monster = monster

/** Regenerating behavior enables health recovery during combat.
  *
  * Regenerating monsters have the regenerating flag set to true, allowing them to recover health over time during
  * extended battles.
  */
case object Regenerating extends MonsterBehavior:
  /** Sets the monster's regenerating state to true.
    *
    * @param monster
    *   The monster to give regeneration
    * @return
    *   Monster with regenerating flag enabled
    */
  def apply(monster: Monster): Monster = // Behavior used after monster attacked
    monster.copy(regenerating = true)

/** Companion object providing utilities for random behavior selection.
  *
  * This object manages the collection of all available monster behaviors and provides methods for randomly selecting
  * behaviors during monster generation,
  */
object MonsterBehavior:
  /** Complete list of all available monster behaviors for random selection. Used by the monster generation system to
    * create varied encounters.
    */
  private val allBehaviors: List[MonsterBehavior] = List(
    Aggressive,
    Defensive,
    MoreHp,
    Berserk,
    OneShot,
    Explosive,
    Regenerating
  )

  /** Randomly selects a behavior from all available behaviors.
    *
    * @return
    *   A randomly chosen MonsterBehavior for use in monster generation
    */
  def randomBehavior: MonsterBehavior = allBehaviors(Random.nextInt(allBehaviors.length))
