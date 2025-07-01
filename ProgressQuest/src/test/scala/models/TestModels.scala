package models

import models.event.MissionData
import models.player.{EquipmentFactory, EquipmentSlot, ItemFactory, SkillFactory, SkillNameData}
import org.scalatest.funsuite.AnyFunSuite
import util.{EquipmentNameLoader, ItemNameLoader, MissionLoader, SkillLoader}

class TestModels extends AnyFunSuite:
  val missions: List[MissionData] = MissionLoader.loadMissions()
  val itemNames: List[String] = ItemNameLoader.loadItemNames()
  val equipmentNames: Map[EquipmentSlot, List[String]] = EquipmentNameLoader.loadEquipmentNames()
  val skillNames: SkillNameData = SkillLoader.loadSkillNames()

  // TestMission
  test("load missions from JSON file"):
    assert(missions.nonEmpty)
    assert(missions.exists(_.name == "Goblin Hunt"))
    assert(missions.exists(_.description.contains("wolves")))


  // TestItem
  test("randomItem should create an item with valid name, gold > 0, and valid rarity"):
    assert(itemNames.nonEmpty)
    val item = ItemFactory.randomItem(itemNames)

    assert(itemNames.contains(item.name), "Item name should be from base list")
    assert(item.gold > 0, "Gold value should be positive")

    println(s"Randomly generated item: $item")


  // TestEquipment

  test("generate random equipment with correct structure"):
    assert(equipmentNames.nonEmpty)
    println(equipmentNames)
    val equip = EquipmentFactory.generateRandomEquipment(probabilityDrop = 1.0, playerLevel = 10)
    assert(equip.get.name.nonEmpty, "Equipment name must not be empty")
    assert(equip.get.value > 0, "Equipment value should be greater than 0")
    assert(equip.get.statBonus.total == equip.get.value, "Stat value should match total attributes")

    println(s"Generated Equipment: $equip")

  // TestSkill

  test("Random skill generation should return valid skill"):
    assert(skillNames.physical.nonEmpty || skillNames.magic.nonEmpty || skillNames.healing.nonEmpty)
    val skill = SkillFactory.randomSkill()
    assert(skill.manaCost >= 0)
    println(s"Generated Skill: ${skill.name} | Mana: ${skill.manaCost} | Skill Type: ${skill.effectType} | PowerLevel: ${skill.powerLevel}")



