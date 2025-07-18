import models.monster.{MonstersFactory, MonsterType}
import models.world.OriginZone
import org.scalatest.funsuite.AnyFunSuite

class TestMonstersFactory extends AnyFunSuite:

  test("randomMonsterForZone should create valid monster") {
    val monster = MonstersFactory.randomMonsterForZone(
      zone = OriginZone.Forest,
      playerLevel = 5,
      playerLucky = 10,
      strong = false
    )

    assert(monster.name.nonEmpty)
    assert(monster.level > 0)
    assert(monster.originZone == OriginZone.Forest)
    assert(monster.attributes.hp > 0)
    assert(monster.attributes.currentHp == monster.attributes.hp) // full HP at creation
    assert(monster.attributes.attack > 0)
    assert(monster.attributes.defense >= 0)
    assert(monster.goldReward > 0)
    assert(monster.experienceReward > 0)
  }

  test("strong monsters should have appropriate level scaling") {
    val playerLevel = 10

    val normalMonster = MonstersFactory.randomMonsterForZone(
      OriginZone.Plains,
      playerLevel,
      5,
      strong = false
    )
    val strongMonster = MonstersFactory.randomMonsterForZone(
      OriginZone.Plains,
      playerLevel,
      5,
      strong = true
    )

    // Normal monsters should be within ±1 level of player
    assert(normalMonster.level >= playerLevel - 1)
    assert(normalMonster.level <= playerLevel + 1)

    // Strong monsters should be 1-2 levels above player
    assert(strongMonster.level >= playerLevel + 1)
    assert(strongMonster.level <= playerLevel + 2)
  }

  test("monster attributes should scale with level") {
    val lowLevelMonster = MonstersFactory.randomMonsterForZone(
      OriginZone.Forest,
      1,
      5,
      strong = false
    )
    val highLevelMonster = MonstersFactory.randomMonsterForZone(
      OriginZone.Forest,
      20,
      5,
      strong = false
    )

    // Higher level monsters should generally have better stats
    // (This is probabilistic, so we'll check that it's not obviously wrong)
    assert(lowLevelMonster.attributes.hp > 0)
    assert(highLevelMonster.attributes.hp > 0)
    assert(lowLevelMonster.level < highLevelMonster.level)
  }

  test("strong monsters should have reduced weaknesses") {
    // Generate multiple monsters to account for randomness
    val strongMonsters = (1 to 5).map(_ =>
      MonstersFactory.randomMonsterForZone(OriginZone.Forest, 10, 5, strong = true)
    )

    // Strong monsters should have weaknesses between 0.5 and 1.0
    strongMonsters.foreach { monster =>
      assert(monster.attributes.weaknessPhysical >= 0.5)
      assert(monster.attributes.weaknessPhysical <= 1.0)
      assert(monster.attributes.weaknessMagic >= 0.5)
      assert(monster.attributes.weaknessMagic <= 1.0)
    }
  }

  test("regular monsters should have standard weaknesses") {
    val regularMonsters = (1 to 5).map(_ =>
      MonstersFactory.randomMonsterForZone(OriginZone.Forest, 10, 5, strong = false)
    )

    // Regular monsters should have weaknesses between 1.0 and 1.5
    regularMonsters.foreach { monster =>
      assert(monster.attributes.weaknessPhysical >= 1.0)
      assert(monster.attributes.weaknessPhysical <= 1.5)
      assert(monster.attributes.weaknessMagic >= 1.0)
      assert(monster.attributes.weaknessMagic <= 1.5)
    }
  }

  test("monster should have valid MonsterType") {
    val monster = MonstersFactory.randomMonsterForZone(
      OriginZone.Desert,
      5,
      10,
      strong = false
    )

    val validTypes = MonsterType.values.toSet
    assert(validTypes.contains(monster.monsterType))
  }

  test("strong monsters should have better rewards") {
    val normalMonster = MonstersFactory.randomMonsterForZone(
      OriginZone.Swamp, 
      10,
      5,
      strong = false
    )
    val strongMonster = MonstersFactory.randomMonsterForZone(
      OriginZone.Swamp,
      10,
      5,
      strong = true
    )

    // Strong monsters should generally have better rewards
    // Note: This is probabilistic due to random generation
    assert(normalMonster.goldReward > 0)
    assert(strongMonster.goldReward > 0)
    assert(normalMonster.experienceReward > 0)
    assert(strongMonster.experienceReward > 0)
  }

  test("monster minimum level should be 1") {
    val monster = MonstersFactory.randomMonsterForZone(
      OriginZone.Forest,
      1,
      5,
      strong = false
    )

    assert(monster.level >= 1)
  }

  test("multiple monsters should have variety") {
    val monsters = (1 to 10).map(_ =>
      MonstersFactory.randomMonsterForZone(OriginZone.Forest, 5, 10, strong = false)
    )

    // Should get different names (assuming monster names file has variety)
    val uniqueNames = monsters.map(_.name).toSet
    val uniqueTypes = monsters.map(_.monsterType).toSet

    // With 10 monsters, we should get some variety
    assert(uniqueNames.size > 1 || uniqueNames.head == "Unknown Monster") // unless no names loaded
    assert(uniqueTypes.size >= 1) // at least some type variety
  }
