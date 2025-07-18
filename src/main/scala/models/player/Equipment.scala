package models.player

import util.EquipmentNameLoader
import util.GameConfig.{baseDropChance, maxDropChance, specialBonusPerLucky}
import scala.util.Random

/**
 * Enumeration of equipment slots where gear can be equipped.
 */
enum EquipmentSlot:
  case Weapon, Shield, Head, Body, Jewelry1, Jewelry2, Shoes, Gauntlets

/**
 * Represents a piece of equipment.
 *
 * @param name      the name of the equipment
 * @param slot      the slot this equipment is worn in
 * @param statBonus the attributes this equipment grants
 * @param value     the item's power score (usually the sum of its attributes)
 */
case class Equipment(name: String, slot: EquipmentSlot, statBonus: Attributes, value: Int)

/**
 * Factory object for generating random [[Equipment]] based on player stats and drop chance.
 */
object EquipmentFactory:

  /** Preloaded equipment names grouped by slot (e.g., weapon names, armor names, etc.). */
  private val prefabNames: Map[EquipmentSlot, List[String]] = EquipmentNameLoader.loadEquipmentNames()

  /**
   * Determines whether an equipment drop occurs based on drop chance and player luck.
   *
   * @param probabilityDrop base drop probability (e.g., 0.25 = 25%)
   * @param playerLucky     the player's luck stat, which increases drop chance
   * @return Some(()) if drop occurs, None otherwise
   */
  private def tryDrop(probabilityDrop: Double, playerLucky: Int): Option[Unit] =
    val bonusChance = playerLucky * specialBonusPerLucky
    val effectiveChance = (probabilityDrop + bonusChance).min(maxDropChance)
    if Random.nextDouble() < effectiveChance then Some(()) else None

  /**
   * Selects a random equipment slot.
   *
   * @return an optional [[EquipmentSlot]], None only if the enum is empty (unlikely)
   */
  private def randomSlot(): Option[EquipmentSlot] =
    Random.shuffle(EquipmentSlot.values.toList).headOption

  /**
   * Selects a random equipment name for a given slot.
   *
   * @param slot the equipment slot
   * @return an optional equipment name from the slot's name list
   */
  private def randomNameForSlot(slot: EquipmentSlot): Option[String] =
    prefabNames.get(slot).flatMap(list => Random.shuffle(list).headOption)

  /**
   * Generates a random piece of equipment if drop conditions are met.
   *
   * Drop chance is affected by the given probability and the player's luck.
   * The slot, name, and stats are all generated randomly if the drop occurs.
   *
   * @param probabilityDrop base drop chance (default from config)
   * @param playerLucky     player's luck stat (affects chance)
   * @param playerLevel     player's level (affects attribute scaling)
   * @return Some(Equipment) if drop occurs, None otherwise
   */
  def generateRandomEquipment(probabilityDrop: Double = baseDropChance, playerLucky: Int, playerLevel: Int): Option[Equipment] =
    for
      drop <- tryDrop(probabilityDrop, playerLucky)
      slot <- randomSlot()
      name <- randomNameForSlot(slot)
      stats = Attributes.biasedFor(slot, playerLevel)
    yield Equipment(name, slot, stats, stats.total)
