package models.player

import util.EquipmentNameLoader

import scala.util.Random

enum EquipmentSlot:
  case Weapon, Shield, Head, Body, Jewelry1, Jewelry2, Shoes, Gauntlets

case class Equipment(name: String, slot: EquipmentSlot, statBonus: Attributes, value: Int)

object EquipmentFactory:

  private val prefabNames: Map[EquipmentSlot, List[String]] = EquipmentNameLoader.loadEquipmentNames()

  private def tryDrop(prob: Double): Option[Unit] =
    if Random.nextDouble() < prob then Some(()) else None

  private def randomSlot(): Option[EquipmentSlot] =
    Random.shuffle(EquipmentSlot.values.toList).headOption

  private def randomNameForSlot(slot: EquipmentSlot): Option[String] =
    prefabNames.get(slot).flatMap(list => Random.shuffle(list).headOption)

  def generateRandomEquipment(probabilityDrop: Double = 0.3, playerLevel: Int): Option[Equipment] =
    for
      drop <- tryDrop(probabilityDrop)
      slot <- randomSlot()
      name <- randomNameForSlot(slot)
      attrs = Attributes.biasedFor(slot, playerLevel)
    yield Equipment(name, slot, attrs, attrs.total)