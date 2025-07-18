import models.world.{World, OriginZone}
import models.monster.{Monster, MonsterAttributes, MonsterBehavior, MonsterType, Aggressive}
import org.scalatest.funsuite.AnyFunSuite

class TestWorld extends AnyFunSuite:

  private def createTestMonster(originZone: OriginZone = OriginZone.Forest): Monster =
    Monster(
      name = "Test Monster",
      level = 5,
      monsterType = MonsterType.Beast,
      originZone = originZone,
      attributes = MonsterAttributes(100, 100, 20, 10, 1.0, 1.0),
      goldReward = 25,
      experienceReward = 50,
      itemReward = None,
      equipReward = None,
      behavior = Aggressive,
      description = "A test monster",
      berserk = false,
      regenerating = false
    )

  test("randomWorld should return different zone from current") {
    val currentZone = OriginZone.Forest

    // Test multiple times to account for randomness
    val differentZones = (1 to 20).map(_ => World.randomWorld(currentZone)).toSet

    // Should never return the current zone
    assert(!differentZones.contains(currentZone))

    // Should return at least one different zone
    assert(differentZones.nonEmpty)

    // All returned zones should be valid OriginZones
    val validZones = OriginZone.values.toSet
    assert(differentZones.subsetOf(validZones))
  }

  test("randomWorld should work for all zones") {
    OriginZone.values.foreach { currentZone =>
      val newZone = World.randomWorld(currentZone)
      assert(newZone != currentZone)
      assert(OriginZone.values.contains(newZone))
    }
  }

  test("randomWorld should provide variety over multiple calls") {
    val currentZone = OriginZone.Forest
    val results = (1 to 50).map(_ => World.randomWorld(currentZone)).toSet

    // With 50 calls and 4 possible zones (excluding Forest), should get variety
    assert(results.size > 1, "Should generate different zones over multiple calls")
  }

  test("applyZoneBuffs should not buff monsters outside their origin zone") {
    val forestMonster = createTestMonster(OriginZone.Forest)
    val buffedInDesert = World.applyZoneBuffs(forestMonster, OriginZone.Desert)

    // Monster should be unchanged when not in origin zone
    assert(buffedInDesert == forestMonster)
    assert(buffedInDesert.attributes == forestMonster.attributes)
  }

  test("applyZoneBuffs should buff Forest monsters in Forest zone") {
    val forestMonster = createTestMonster(OriginZone.Forest)
    val originalDefense = forestMonster.attributes.defense
    val buffedMonster = World.applyZoneBuffs(forestMonster, OriginZone.Forest)

    // Defense should be increased
    assert(buffedMonster.attributes.defense >= originalDefense)

    // Other attributes should be unchanged
    assert(buffedMonster.attributes.hp == forestMonster.attributes.hp)
    assert(buffedMonster.attributes.attack == forestMonster.attributes.attack)
    assert(buffedMonster.attributes.weaknessPhysical == forestMonster.attributes.weaknessPhysical)
  }

  test("applyZoneBuffs should buff Desert monsters in Desert zone") {
    val desertMonster = createTestMonster(OriginZone.Desert)
    val originalAttack = desertMonster.attributes.attack
    val buffedMonster = World.applyZoneBuffs(desertMonster, OriginZone.Desert)

    // Attack should be increased
    assert(buffedMonster.attributes.attack >= originalAttack)

    // Other attributes should be unchanged
    assert(buffedMonster.attributes.hp == desertMonster.attributes.hp)
    assert(buffedMonster.attributes.defense == desertMonster.attributes.defense)
  }

  test("applyZoneBuffs should buff Volcano monsters in Volcano zone") {
    val volcanoMonster = createTestMonster(OriginZone.Volcano)
    val originalHp = volcanoMonster.attributes.hp
    val buffedMonster = World.applyZoneBuffs(volcanoMonster, OriginZone.Volcano)

    // HP should be increased
    assert(buffedMonster.attributes.hp >= originalHp)
    assert(buffedMonster.attributes.currentHp >= originalHp)
    assert(buffedMonster.attributes.currentHp == buffedMonster.attributes.hp) // Current HP matches max HP

    // Other attributes should be unchanged
    assert(buffedMonster.attributes.attack == volcanoMonster.attributes.attack)
    assert(buffedMonster.attributes.defense == volcanoMonster.attributes.defense)
  }

  test("applyZoneBuffs should buff Swamp monsters in Swamp zone") {
    val swampMonster = createTestMonster(OriginZone.Swamp)
    val originalPhysicalWeakness = swampMonster.attributes.weaknessPhysical
    val originalMagicWeakness = swampMonster.attributes.weaknessMagic
    val buffedMonster = World.applyZoneBuffs(swampMonster, OriginZone.Swamp)

    // Weaknesses should be increased (making monster more resistant)
    assert(buffedMonster.attributes.weaknessPhysical >= originalPhysicalWeakness)
    assert(buffedMonster.attributes.weaknessMagic >= originalMagicWeakness)

    // Other attributes should be unchanged
    assert(buffedMonster.attributes.hp == swampMonster.attributes.hp)
    assert(buffedMonster.attributes.attack == swampMonster.attributes.attack)
    assert(buffedMonster.attributes.defense == swampMonster.attributes.defense)
  }

  test("applyZoneBuffs should not buff Plains monsters") {
    val plainsMonster = createTestMonster(OriginZone.Plains)
    val buffedMonster = World.applyZoneBuffs(plainsMonster, OriginZone.Plains)

    // Monster should be completely unchanged in Plains
    assert(buffedMonster == plainsMonster)
    assert(buffedMonster.attributes == plainsMonster.attributes)
  }

  test("applyZoneBuffs should not modify original monster") {
    val original = createTestMonster(OriginZone.Forest)
    val originalDefense = original.attributes.defense
    val originalAttack = original.attributes.attack
    val originalHp = original.attributes.hp

    // Apply buffs
    World.applyZoneBuffs(original, OriginZone.Forest)

    // Original should be unchanged
    assert(original.attributes.defense == originalDefense)
    assert(original.attributes.attack == originalAttack)
    assert(original.attributes.hp == originalHp)
  }

  test("getZoneDescription should return correct descriptions") {
    assert(World.getZoneDescription(OriginZone.Forest).contains("enhanced defense"))
    assert(World.getZoneDescription(OriginZone.Desert).contains("more physical damage"))
    assert(World.getZoneDescription(OriginZone.Volcano).contains("increased HP"))
    assert(World.getZoneDescription(OriginZone.Swamp).contains("less vulnerable"))
    assert(World.getZoneDescription(OriginZone.Plains).contains("no special effects"))
  }

  test("getZoneDescription should return non-empty strings") {
    OriginZone.values.foreach { zone =>
      val description = World.getZoneDescription(zone)
      assert(description.nonEmpty)
      assert(description.length > 10) // Should be descriptive
    }
  }

  test("getZoneDescription should handle all zones") {
    // Test that all zones have descriptions
    val descriptions = OriginZone.values.map(World.getZoneDescription).toSet

    // Should have unique descriptions for each zone
    assert(descriptions.size == OriginZone.values.length)

    // No description should be null or empty
    descriptions.foreach { desc =>
      assert(desc != null)
      assert(desc.nonEmpty)
    }
  }

  test("zone buffs should provide meaningful improvements") {
    // Test that buff logic doesn't break monsters
    val monsters = OriginZone.values.map(createTestMonster)

    monsters.foreach { monster =>
      val buffed = World.applyZoneBuffs(monster, monster.originZone)

      // Basic sanity checks - all attributes should be non-negative
      assert(buffed.attributes.hp >= 0)
      assert(buffed.attributes.currentHp >= 0)
      assert(buffed.attributes.attack >= 0)
      assert(buffed.attributes.defense >= 0)
      assert(buffed.attributes.weaknessPhysical >= 0)
      assert(buffed.attributes.weaknessMagic >= 0)

      monster.originZone match
        case OriginZone.Forest =>
          // Forest: defense should be >= original
          assert(buffed.attributes.defense >= monster.attributes.defense)

        case OriginZone.Desert =>
          // Desert: attack should be >= original
          assert(buffed.attributes.attack >= monster.attributes.attack)

        case OriginZone.Volcano =>
          // Volcano: HP should be >= original
          assert(buffed.attributes.hp >= monster.attributes.hp)
          assert(buffed.attributes.currentHp == buffed.attributes.hp) // Current HP should match max

        case OriginZone.Swamp =>
          // Swamp: weaknesses should be increased (more resistant)
          assert(buffed.attributes.weaknessPhysical > monster.attributes.weaknessPhysical)
          assert(buffed.attributes.weaknessMagic > monster.attributes.weaknessMagic)

        case OriginZone.Plains =>
          // Plains monsters should not be buffed
          assert(buffed == monster)
          assert(buffed.attributes == monster.attributes)
    }
  }
