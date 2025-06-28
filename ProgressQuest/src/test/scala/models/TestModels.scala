package models

import models.event.MissionData
import models.player.ItemFactory
import org.scalatest.funsuite.AnyFunSuite
import util.{ItemNameLoader, MissionLoader}

class TestModels extends AnyFunSuite {
  val missions: List[MissionData] = MissionLoader.loadMissions()
  val itemNames: List[String] = ItemNameLoader.loadItemNames()


  // TestMission
  test("load missions from JSON file") {
    assert(missions.nonEmpty)
    assert(missions.exists(_.name == "Goblin Hunt"))
    assert(missions.exists(_.description.contains("wolves")))
  }

  // TestItem
  test("randomItem should create an item with valid name, gold > 0, and valid rarity") {
    assert(itemNames.nonEmpty)
    val item = ItemFactory.randomItem(itemNames)

    assert(itemNames.contains(item.name), "Item name should be from base list")
    assert(item.gold > 0, "Gold value should be positive")

    println(s"Randomly generated item: $item")
  }
}
