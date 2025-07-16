package models.monster
/**
 * Represents the combat statistics and damage vulnerabilities of a monster.
 *
 * MonsterAttributes encapsulates all the numerical values that define a monster's
 *
 * @param hp The monster's maximum hit points (health)
 * @param currentHp The monster's current hit points (0 when defeated)
 * @param attack The monster's offensive power used for damage calculations
 * @param defense The monster's defensive power reducing incoming damage
 * @param weaknessPhysical Damage multiplier for physical attacks 
 * @param weaknessMagic Damage multiplier for magical attacks 
 */
case class MonsterAttributes(hp: Int, currentHp: Int, attack: Int, defense: Int, weaknessPhysical: Double = 1.0, weaknessMagic: Double = 1.0)
