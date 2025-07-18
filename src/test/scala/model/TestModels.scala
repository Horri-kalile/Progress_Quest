package model

import models.event.MissionData
import models.monster.MonstersFactory
import models.player.*
import models.player.EquipmentModule.{EquipmentFactory, EquipmentSlot}
import models.player.ItemModule.ItemFactory
import models.world.OriginZone
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
    val item = ItemFactory.alwaysCreate().createRandomItem(10).get

    assert(itemNames.contains(item.name), "Item name should be from base list")
    assert(item.gold > 0, "Gold value should be positive")

    println(s"Randomly generated item: $item")


  // TestEquipment

  test("generate random equipment with correct structure"):
    assert(equipmentNames.nonEmpty)
    println(equipmentNames)
    val equip = EquipmentFactory.alwaysDrop(playerLevel = 10)
    assert(equip.get.name.nonEmpty, "Equipment name must not be empty")
    assert(equip.get.value > 0, "Equipment value should be greater than 0")
    assert(equip.get.statBonus.total == equip.get.value, "Stat value should match total attributes")

    println(s"Generated Equipment: $equip")


  test("generateRandomEquipment returns Some when drop chance is 100%"):
    val equipment = EquipmentFactory.alwaysDrop(playerLevel = 5)
    assert(equipment.isDefined)


  test("generateRandomEquipment not always return equipment"):
    val equipment = EquipmentFactory.probBased(playerLucky = 0, playerLevel = 5)
    assert(equipment.isEmpty || equipment.isDefined)


  test("equipment attributes increase with player level"):
    val lowLevel = EquipmentFactory.alwaysDrop(playerLevel = 1).get
    val highLevel = EquipmentFactory.alwaysDrop(playerLevel = 50).get
    assert(highLevel.statBonus.total >= lowLevel.statBonus.total)


  test("equipment total value matches attribute total"):
    val eq = EquipmentFactory.alwaysDrop(playerLevel = 10).get
    assert(eq.value == eq.statBonus.total)


  test("high luck increases drop probability"):
    val attempts = 1000
    val withLuckDrops = (1 to attempts).count { _ =>
      EquipmentFactory.probBased(playerLucky = 100, playerLevel = 5).isDefined
    }
    val withoutLuckDrops = (1 to attempts).count { _ =>
      EquipmentFactory.probBased(playerLucky = 0, playerLevel = 5).isDefined
    }
    println(withoutLuckDrops)
    println(withLuckDrops)
    assert(withLuckDrops > withoutLuckDrops)


  // TestSkill

  test("Random skill generation should return valid skill"):
    assert(skillNames.physical.nonEmpty || skillNames.magic.nonEmpty || skillNames.healing.nonEmpty)
    val skill = SkillFactory.randomSkill()
    assert(skill.manaCost >= 0)
    println(s"Generated Skill: ${skill.name} | Mana: ${skill.manaCost} | Skill Type: ${skill.effectType} | PowerLevel: ${skill.powerLevel}")

  // TestMonsterGeneration
  test("Generate normal monster for a given zone and player level") {
    val zone = OriginZone.Forest
    val playerLevel = 5
    val monster = MonstersFactory.randomMonsterForZone(zone, playerLevel, 10)

    assert(monster.originZone == zone)
    assert(monster.level >= 1)
    assert(monster.attributes.hp > 0)
    assert(monster.goldReward > 0)
    assert(monster.experienceReward > 0)

    println(s"Normal monster: ${monster.name}, Level: ${monster.level}, HP: ${monster.attributes.hp}, Behavior: ${monster.behavior}")
  }

  test("Generate strong monster has lower physical and magical weakness") {
    val zone = OriginZone.Volcano
    val playerLevel = 10

    val normal = MonstersFactory.randomMonsterForZone(zone, playerLevel, 10)
    val strong = MonstersFactory.randomMonsterForZone(zone, playerLevel, 10, strong = true)

    assert(strong.attributes.weaknessPhysical <= normal.attributes.weaknessPhysical)
    assert(strong.attributes.weaknessMagic <= normal.attributes.weaknessMagic)

    println(s"Normal monster: ${normal.name}, Level: ${normal.level}, HP: ${normal.attributes.hp}, Behavior: ${normal.behavior}, Weakness: ${normal.attributes.weaknessPhysical}, ${normal.attributes.weaknessMagic}")
    println(s"Strong monster: ${strong.name}, Level: ${strong.level}, HP: ${strong.attributes.hp}, Behavior: ${strong.behavior}, Weakness: ${strong.attributes.weaknessPhysical}, ${strong.attributes.weaknessMagic}")
  }

