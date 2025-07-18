
import org.scalatest.funsuite.AnyFunSuite
import models.player.*
import models.player.Behavior.BehaviorType
import models.player.PlayerBonusesApplication.applyRaceAndClassBonuses
import models.world.OriginZone.Desert

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

  test("Elf gets dexterity bonus"):
    val player = basePlayer(Race.Elf, ClassType.Mage)
    val updated = applyRaceAndClassBonuses(player)
    println(player)
    println(updated)
    assert(updated.mp > 50)
    assert(updated.baseAttributes.dexterity > 5)

  test("Titan has more strength"):
    val player = basePlayer(Race.Titan, ClassType.Mage)
    val updated = applyRaceAndClassBonuses(player)

    assert(updated.baseAttributes.strength > 5)

  test("CowBoy class gets both HP and MP bonus"):
    val player = basePlayer(Race.Human, ClassType.CowBoy)
    val updated = applyRaceAndClassBonuses(player)

    assert(updated.hp > 100)
    assert(updated.mp > 50)

  test("Starting skill is generated"):
    val player = basePlayer(Race.Human, ClassType.Assassin)
    val updated = applyRaceAndClassBonuses(player)

    assert(updated.skills.nonEmpty)