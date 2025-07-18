package model

import models.player.*
import models.player.Behavior.BehaviorType
import models.player.PlayerBonusesApplication.applyRaceAndClassBonuses
import models.world.OriginZone.Desert
import org.scalatest.funsuite.AnyFunSuite

class TestPlayerBonusApplication extends AnyFunSuite:

  def basePlayer(race: Race, classType: ClassType): Player =
    Player(
      name = "Player",
      identity = Identity(race, classType),
      level = 1,
      exp = 0,
      baseAttributes = Attributes(5, 5, 5, 5, 5, 5),
      equipment = Map.empty,
      inventory = Map.empty,
      behaviorType = BehaviorType.Aggressive,
      gold = 0,
      hp = 100,
      mp = 50,
      currentHp = 100,
      currentMp = 50,
      skills = List.empty,
      missions = List.empty,
      currentZone = Desert
    )

  test("Human gets equipment and unchanged stats"):
    val player = basePlayer(Race.Human, ClassType.Mage)
    val updated = applyRaceAndClassBonuses(player)

    assert(updated.equipment.nonEmpty)
    assert(updated.hp == 100)
    assert(updated.mp == 50)

  test("Elf gets dexterity bonus and mp multiplier"):
    val player = basePlayer(Race.Elf, ClassType.Mage)
    val updated = applyRaceAndClassBonuses(player)

    assert(updated.mp > 50)
    assert(updated.baseAttributes.dexterity > 5)

  test("Dwarf gets constitution bonus and hp multiplier"):
    val player = basePlayer(Race.Dwarf, ClassType.Mage)
    val updated = applyRaceAndClassBonuses(player)

    assert(updated.hp > 100)
    assert(updated.baseAttributes.constitution > 5)

  test("Orc gets wisdom bonus and high hp multiplier, low mp multiplier"):
    val player = basePlayer(Race.Orc, ClassType.Mage)
    val updated = applyRaceAndClassBonuses(player)

    assert(updated.hp > 100)
    assert(updated.mp < 50)
    assert(updated.baseAttributes.wisdom > 5)

  test("Gnome gets intelligence bonus and mp multiplier"):
    val player = basePlayer(Race.Gnome, ClassType.Mage)
    val updated = applyRaceAndClassBonuses(player)

    assert(updated.mp > 50)
    assert(updated.baseAttributes.intelligence > 5)

  test("Titan gets strength bonus and high hp multiplier"):
    val player = basePlayer(Race.Titan, ClassType.Mage)
    val updated = applyRaceAndClassBonuses(player)

    assert(updated.hp > 100)
    assert(updated.baseAttributes.strength > 5)

  test("PandaMan gets lucky bonus and mp multiplier"):
    val player = basePlayer(Race.PandaMan, ClassType.Mage)
    val updated = applyRaceAndClassBonuses(player)

    assert(updated.mp > 50)
    assert(updated.baseAttributes.lucky > 5)

  test("Gundam gets wisdom and strength bonuses"):
    val player = basePlayer(Race.Gundam, ClassType.Mage)
    val updated = applyRaceAndClassBonuses(player)

    assert(updated.baseAttributes.wisdom > 5)
    assert(updated.baseAttributes.strength > 5)

  test("Warrior class gets HP bonus only"):
    val player = basePlayer(Race.Human, ClassType.Warrior)
    val updated = applyRaceAndClassBonuses(player)

    assert(updated.hp > 100)
    assert(updated.mp == 50)

  test("Poisoner class gets MP bonus only"):
    val player = basePlayer(Race.Human, ClassType.Poisoner)
    val updated = applyRaceAndClassBonuses(player)

    assert(updated.mp > 50)
    assert(updated.hp == 100)

  test("CowBoy class gets both HP and MP bonus"):
    val player = basePlayer(Race.Human, ClassType.CowBoy)
    val updated = applyRaceAndClassBonuses(player)

    assert(updated.hp > 100)
    assert(updated.mp > 50)

  test("Mage, Cleric, Paladin, Assassin classes get no HP or MP bonus"):
    for classType <- List(ClassType.Mage, ClassType.Cleric, ClassType.Paladin, ClassType.Assassin) do
      val player = basePlayer(Race.Human, classType)
      val updated = applyRaceAndClassBonuses(player)

      assert(updated.hp == 100)
      assert(updated.mp == 50)

  test("Starting skills are only generated for Mage, Paladin, Assassin, and Cleric"):
    val skillClasses = Set(ClassType.Mage, ClassType.Paladin, ClassType.Assassin, ClassType.Cleric)

    for classType <- ClassType.values do
      val player = basePlayer(Race.Human, classType)
      val updated = applyRaceAndClassBonuses(player)

      if skillClasses.contains(classType) then
        assert(updated.skills.nonEmpty, s"Expected starting skills for $classType")
      else
        assert(updated.skills.isEmpty, s"Did NOT expect starting skills for $classType")


  test("Equipment is added only for Human race"):
    for race <- Race.values do
      val player = basePlayer(race, ClassType.Mage)
      val updated = applyRaceAndClassBonuses(player)

      if race == Race.Human then
        assert(updated.equipment.nonEmpty, "Human should get equipment")
      else
        assert(updated.equipment.isEmpty, s"$race should not get equipment")

