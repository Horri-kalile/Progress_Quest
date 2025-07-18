package models.world

import models.monster.Monster
import util.GameConfig

import scala.util.Random

/**
 * World management system that handles zone-based mechanics and environmental effects.
 *
 * The World object provides functionality for managing the game world's different zones
 * and their effects on monsters and gameplay. It handles zone transitions, applies
 * environmental buffs to monsters when they are in their native zones, and provides
 * descriptive information about each zone's characteristics.
 *
 * Key features:
 * - Random zone selection for world transitions
 * - Zone-specific environmental buffs and effects
 */
object World:

  /**
   * Selects a random zone different from the current zone for world transitions.
   *
   * This method is used when the player moves to a new area, ensuring they don't
   * stay in the same zone.
   *
   * @param currentZone The zone the player is currently in
   * @return A randomly selected OriginZone different from the current zone
   */
  def randomWorld(currentZone: OriginZone): OriginZone =
    val zones = OriginZone.values.filterNot(_ == currentZone)
    Random.shuffle(zones.toList).head

  /**
   * Applies environmental buffs to monsters when they are in their native zone.
   *
   * Monsters receive significant advantages when encountered in their origin zone,
   * Each zone provides different types of buffs based on its environmental characteristics.
   * Monsters outside their native zone receive no environmental bonuses.
   *
   * Zone-specific buffs:
   * - Forest: Enhanced defense 
   * - Desert: Increased attack power 
   * - Volcano: Boosted HP 
   * - Swamp: Reduced vulnerabilities 
   * - Plains: No special effects
   *
   * @param monster The monster to potentially buff
   * @param currentZone The zone where the encounter is taking place
   * @return A new Monster instance with zone buffs applied if applicable
   */
  def applyZoneBuffs(monster: Monster, currentZone: OriginZone): Monster =
    if monster.originZone != currentZone then return monster

    val attrs = monster.attributes

    val buffedAttributes = currentZone match
      case OriginZone.Forest =>
        val defenseBuff = 1.0 + Random.nextDouble() * GameConfig.maxBuffByZone
        attrs.copy(defense = (attrs.defense * defenseBuff).toInt)

      case OriginZone.Desert =>
        val attackBuff = 1.0 + Random.nextDouble() * GameConfig.maxBuffByZone
        attrs.copy(attack = (attrs.attack * attackBuff).toInt)

      case OriginZone.Volcano =>
        val hpBuff = 1.0 + Random.nextDouble() * GameConfig.maxBuffByZone
        val newHp = (attrs.hp * hpBuff).toInt
        attrs.copy(currentHp = newHp, hp = newHp)

      case OriginZone.Swamp =>
        val physicalBuff = 1.0 + Random.nextDouble() * GameConfig.maxBuffByZone
        val magicBuff = 1.0 + Random.nextDouble() * GameConfig.maxBuffByZone
        attrs.copy(
          weaknessPhysical = attrs.weaknessPhysical * physicalBuff,
          weaknessMagic = attrs.weaknessMagic * magicBuff
        )

      case OriginZone.Plains =>
        attrs // No buffs

    monster.copy(attributes = buffedAttributes)

  /**
   * Provides descriptive text explaining each zone's environmental effects.
   *
   * Returns user-friendly descriptions that explain what advantages monsters
   * gain when encountered in their native zones. This information is displayed
   * in the UI to help players understand the tactical implications of different
   *
   * @param zone The zone to get a description for
   * @return A descriptive string explaining the zone's effects on monsters
   */
  def getZoneDescription(zone: OriginZone): String = zone match
    case OriginZone.Forest => "A dense forest where monsters have enhanced defense"
    case OriginZone.Desert => "A harsh desert where monsters deal more physical damage"
    case OriginZone.Volcano => "A volcanic region where monsters have increased HP"
    case OriginZone.Swamp => "A mysterious swamp where monsters are less vulnerable to both physical and magical damage"
    case OriginZone.Plains => "Normal plains with no special effects"
