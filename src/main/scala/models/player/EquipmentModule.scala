package models.player

import util.EquipmentNameLoader
import util.GameConfig.{baseDropChance, maxDropChance, specialBonusPerLucky}
import scala.util.Random

/**
 * Module for generating and enhancing in-game equipment.
 *
 * Provides:
 * - Equipment slot enum
 * - Equipment case class
 * - Factory trait for polymorphic equipment generation
 * - Two factory implementations:
 *    - AlwaysDropFactory: always drops equipment
 *    - ProbBasedFactory: drops based on probability + luck
 *      - Shared private helpers for randomness and name lookup
 */
object EquipmentModule:

  /**
   * Enumeration of valid equipment slots.
   */
  enum EquipmentSlot:
    case Weapon, Shield, Head, Body, Jewelry1, Jewelry2, Shoes, Gauntlets

  /**
   * Representation of a piece of equipment.
   *
   * @param name      Equipment name
   * @param slot      Equipment slot it occupies
   * @param statBonus Attribute bonuses granted
   * @param value     Computed value (usually sum of attributes)
   */
  case class Equipment(name: String, slot: EquipmentSlot, statBonus: Attributes, value: Int)

  /**
   * Trait defining the interface for equipment generation and enhancement.
   */
  trait EquipmentFactory:

    /**
     * Generates a random piece of equipment if drop conditions are met.
     *
     * @param probabilityDrop Base drop chance (0.0 - 1.0)
     * @param playerLucky     Player's luck stat affecting drop chance
     * @param playerLevel     Player's level for attribute scaling
     * @return Some(Equipment) if drop occurs, None otherwise
     */
    def generateRandomEquipment(probabilityDrop: Double, playerLucky: Int, playerLevel: Int): Option[Equipment]
  

  /**
   * Factory object to obtain different implementations of EquipmentFactory.
   */
  object EquipmentFactory:

    /**
     * Creates a factory that always drops equipment (drop chance 100%).
     */
    def alwaysDrop(playerLevel: Int): Option[Equipment] =
      AlwaysDropFactory().generateRandomEquipment(1.0, playerLucky = 0, playerLevel)

    /**
     * CCreates a factory that drops equipment based on probability and luck.
     *
     * @param playerLucky player's luck stat boosting drop chance
     * @param playerLevel player's level for attribute scaling
     * @return optionally dropped equipment
     */
    def probBased(playerLucky: Int, playerLevel: Int): Option[Equipment] =
      ProbBasedFactory().generateRandomEquipment(baseDropChance, playerLucky, playerLevel)

    /**
     * Enhances an equipment item by randomly increasing some of its attributes.
     *
     * @param equipment Equipment to enhance
     * @return New enhanced equipment instance
     */
    def powerUpEquip(equipment: Equipment): Equipment =
      val boosted = equipment.statBonus.incrementRandomAttributes()
      equipment.copy(statBonus = boosted, value = boosted.total)

  /**
   * Private trait providing shared helper methods for implementations.
   */
  private trait EquipmentFactoryHelpers:

    /** Preloaded equipment names mapped by slot. */
    private lazy val prefabNames: Map[EquipmentSlot, List[String]] =
      EquipmentNameLoader.loadEquipmentNames()

    /** Randomly selects a slot from the enum. */
    protected def randomSlot(): Option[EquipmentSlot] =
      Random.shuffle(EquipmentSlot.values.toList).headOption

    /** Selects a random equipment name from the prefab list for the slot. */
    protected def randomNameForSlot(slot: EquipmentSlot): Option[String] =
      prefabNames.get(slot).flatMap(names => Random.shuffle(names).headOption)

  /**
   * Factory that always generates equipment without drop chance checks.
   */
  private case class AlwaysDropFactory() extends EquipmentFactory with EquipmentFactoryHelpers:

    override def generateRandomEquipment(probabilityDrop: Double, playerLucky: Int, playerLevel: Int): Option[Equipment] =
      for
        slot <- randomSlot()
        name <- randomNameForSlot(slot)
        stats = Attributes.biasedFor(slot, playerLevel)
      yield Equipment(name, slot, stats, stats.total)


  /**
   * Factory that generates equipment based on drop probability and player luck.
   */
  private case class ProbBasedFactory() extends EquipmentFactory with EquipmentFactoryHelpers:

    override def generateRandomEquipment(probabilityDrop: Double, playerLucky: Int, playerLevel: Int): Option[Equipment] =
      for
        _ <- tryDrop(probabilityDrop, playerLucky)
        slot <- randomSlot()
        name <- randomNameForSlot(slot)
        stats = Attributes.biasedFor(slot, playerLevel)
      yield Equipment(name, slot, stats, stats.total)

    /** Calculates if the drop occurs based on base chance and luck stat. */
    private def tryDrop(baseChance: Double, lucky: Int): Option[Unit] =
      val chance = (baseChance + lucky * specialBonusPerLucky).min(maxDropChance)
      if Random.nextDouble() < chance then Some(()) else None
