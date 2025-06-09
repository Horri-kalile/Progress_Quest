package models.player

import scala.util.Random
import scala.util.Random.nextDouble

enum EquipmentSlot:
  case Weapon, Shield, Head, Body, Jewelry1, Jewelry2, Shoes, Gauntlets

case class Equipment(name: String, slot: EquipmentSlot, statBonus: Attributes, value: Int)

object EquipmentFactory:

  private val prefabNames: Map[EquipmentSlot, List[String]] = Map(
    EquipmentSlot.Weapon -> List(
      "Soulreaver Blade", "Ironfang Axe", "Skywarden Bow", "Stormpiercer Lance",
      "Dagger of the Deep", "Inferno Scythe", "Emberfang Katana", "Hammer of Sundering"
    ),
    EquipmentSlot.Shield -> List(
      "Mirror of the Sentinel", "Aegis of the Forgotten", "Bastion of Ash",
      "Stonewall Tower Shield", "Shield of Echoed Light", "Emberguard Bulwark"
    ),
    EquipmentSlot.Head -> List(
      "Helm of Inner Flame", "Bone Crown", "Mask of the War Priest",
      "Visor of the Voidwatch", "Hood of the Trickster"
    ),
    EquipmentSlot.Body -> List(
      "Dragonhide Cuirass", "Warlord’s Breastplate", "Robe of the Silent Court",
      "Crystalsteel Hauberk", "Voidforged Armor"
    ),
    EquipmentSlot.Shoes -> List(
      "Greaves of the Dancer", "Phantomstep Boots", "Frostbound Footwraps",
      "Thunderstride Sandals", "Emberstep Sabatons"
    ),
    EquipmentSlot.Gauntlets -> List(
      "Fistwraps of Flame", "Gauntlets of the Depths", "Spiked Grip",
      "Gloves of the Feral", "Ironbind Handguards"
    ),
    EquipmentSlot.Jewelry1 -> List(
      "Ring of Binding Ice", "Pendant of Black Suns", "Band of the Blood Moon"
    ),
    EquipmentSlot.Jewelry2 -> List(
      "Necklace of the Wilds", "Choker of the Forgotten Flame", "Sigil of Unseen Paths"
    )
  )

  private def tryDrop(prob: Double): Option[Unit] =
    if Random.nextDouble() < prob then Some(()) else None

  private def randomSlot(): Option[EquipmentSlot] =
    Random.shuffle(EquipmentSlot.values.toList).headOption

  private def randomNameForSlot(slot: EquipmentSlot): Option[String] =
    prefabNames.get(slot).flatMap(list => Random.shuffle(list).headOption)

  /*Already managed drop rate*/
  def generateRandomEquipment(probabilityDrop: Double = 0.3, playerLevel: Int): Option[Equipment] =
    for
      drop <- tryDrop(probabilityDrop)
      slot <- randomSlot()
      name <- randomNameForSlot(slot)
      attrs = Attributes.biasedFor(slot, playerLevel)
    yield Equipment(name, slot, attrs, attrs.total)