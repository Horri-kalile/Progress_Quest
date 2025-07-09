package models.player

import util.EquipmentNameLoader
import util.GameConfig.{baseDropChance, maxDropChance, specialBonusPerLucky}

import scala.util.Random

enum EquipmentSlot:
  case Weapon, Shield, Head, Body, Jewelry1, Jewelry2, Shoes, Gauntlets

case class Equipment(name: String, slot: EquipmentSlot, statBonus: Attributes, value: Int)

object EquipmentFactory:

  private val prefabNames: Map[EquipmentSlot, List[String]] = EquipmentNameLoader.loadEquipmentNames()

  private def tryDrop(probabilityDrop: Double, playerLucky: Int): Option[Unit] =
    if Random.nextDouble() < (probabilityDrop + playerLucky * specialBonusPerLucky).min(maxDropChance) then Some(()) else None

  private def randomSlot(): Option[EquipmentSlot] =
    Random.shuffle(EquipmentSlot.values.toList).headOption

  private def randomNameForSlot(slot: EquipmentSlot): Option[String] =
    prefabNames.get(slot).flatMap(list => Random.shuffle(list).headOption)

  def generateRandomEquipment(probabilityDrop: Double = baseDropChance, playerLucky: Int, playerLevel: Int): Option[Equipment] =
    for
      drop <- tryDrop(probabilityDrop, playerLucky)
      slot <- randomSlot()
      name <- randomNameForSlot(slot)
      attrs = Attributes.biasedFor(slot, playerLevel)
    yield Equipment(name, slot, attrs, attrs.total)