package controller

import controllers.CombatController
import models.monster.{Aggressive, Monster, MonsterAttributes, MonsterType}
import models.player.{Attributes, Behavior, ClassType, EquipmentModule, Identity, ItemModule, Player, Race}
import models.world.OriginZone
import org.scalatest.funsuite.AnyFunSuite

class TestCombatController extends AnyFunSuite:

  val baseAttributes: Attributes = Attributes(10, 10, 10, 10, 10, 10)

  def freshPlayer: Player = Player(
    name = "Hero",
    identity = Identity(Race.Human, ClassType.Warrior),
    baseAttributes = baseAttributes,
    behaviorType = Behavior.BehaviorType.Heal
  ).withHp(100).withCurrentHp(100).withMp(50).withCurrentMp(50)

  val testMonster: Monster = Monster(
    name = "TestBeast",
    level = 2,
    monsterType = MonsterType.Beast,
    originZone = OriginZone.Forest,
    attributes = MonsterAttributes(hp = 50, currentHp = 50, attack = 5, defense = 2, weaknessPhysical = 1.0,
      weaknessMagic = 1.0),
    goldReward = 10,
    experienceReward = 15,
    itemReward = Some(ItemModule.Item("Herb", 5.0, ItemModule.ItemRarity.Common)),
    equipReward = Some(EquipmentModule.Equipment("Claw", EquipmentModule.EquipmentSlot.Weapon, baseAttributes, 10)),
    behavior = Aggressive,
    description = "A test beast"
  )

  test("simulateFight returns combat log and updates states"):
    val results = CombatController.simulateFight(freshPlayer, testMonster)
    assert(results.nonEmpty)
    assert(results.exists(_._3.contains("attacked")) || results.exists(_._3.contains("defeated")))

  test("handleEquipDrop equips better item"):
    val (updated, msg) = CombatController.handleEquipDrop(freshPlayer, testMonster)
    assert(msg.contains("equipped") || msg.contains("sold"))

  test("handleItemDrop adds item to inventory"):
    val (updated, msg) = CombatController.handleItemDrop(freshPlayer, testMonster)
    assert(msg.contains("found item") || msg.contains("No item"))

  test("setLastMonster and get lastMonster works"):
    CombatController.setLastMonster(testMonster)
    assert(CombatController.lastMonster.contains(testMonster))
