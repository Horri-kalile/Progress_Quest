package model

import models.monster.*
import models.world.OriginZone
import org.scalatest.funsuite.AnyFunSuite

class TestMonsterBehavior extends AnyFunSuite:

  private def createTestMonster(): Monster =
    Monster(
      name = "Test Monster",
      level = 5,
      monsterType = MonsterType.Beast,
      originZone = OriginZone.Forest,
      attributes = MonsterAttributes(100, 100, 20, 10, 1.0, 1.0),
      goldReward = 25,
      experienceReward = 50,
      itemReward = None,
      equipReward = None,
      behavior = Aggressive,
      description = "A test monster for unit testing",
      berserk = false,
      regenerating = false
    )

  test("Aggressive behavior should increase attack by 25%") {
    val monster = createTestMonster()
    val aggressive = Aggressive.apply(monster)

    val expectedAttack = (20 * 1.25).toInt // 25
    assert(aggressive.attributes.attack == expectedAttack)
    assert(aggressive.attributes.hp == monster.attributes.hp)
    assert(aggressive.attributes.defense == monster.attributes.defense)
  }

  test("Defensive behavior should increase defense by 25%") {
    val monster = createTestMonster()
    val defensive = Defensive.apply(monster)

    val expectedDefense = (10 * 1.25).toInt // 12
    assert(defensive.attributes.defense == expectedDefense)
    assert(defensive.attributes.attack == monster.attributes.attack)
    assert(defensive.attributes.hp == monster.attributes.hp)
  }

  test("MoreHp behavior should increase HP by 25%") {
    val monster = createTestMonster()
    val moreHp = MoreHp.apply(monster)

    val expectedHp = (100 * 1.25).toInt // 125
    assert(moreHp.attributes.hp == expectedHp)
    assert(moreHp.attributes.currentHp == expectedHp)
    assert(moreHp.attributes.attack == monster.attributes.attack)
    assert(moreHp.attributes.defense == monster.attributes.defense)
  }

  test("Berserk behavior should set berserk flag to true") {
    val monster = createTestMonster()
    val berserk = Berserk.apply(monster)

    assert(berserk.berserk == true)
    assert(monster.berserk == false)
    // All attributes should remain the same
    assert(berserk.attributes == monster.attributes)
  }

  test("OneShot behavior should double attack power") {
    val monster = createTestMonster()
    val oneShot = OneShot.apply(monster)

    val expectedAttack = 20 * 2 // 40
    assert(oneShot.attributes.attack == expectedAttack)
    assert(oneShot.attributes.hp == monster.attributes.hp)
    assert(oneShot.attributes.defense == monster.attributes.defense)
  }

  test("Explosive behavior should return monster unchanged") {
    val monster = createTestMonster()
    val explosive = Explosive.apply(monster)

    // Should be exactly the same
    assert(explosive == monster)
    assert(explosive.attributes == monster.attributes)
    assert(explosive.berserk == monster.berserk)
    assert(explosive.regenerating == monster.regenerating)
  }

  test("Regenerating behavior should set regenerating flag to true") {
    val monster = createTestMonster()
    val regenerating = Regenerating.apply(monster)

    assert(regenerating.regenerating == true)
    assert(monster.regenerating == false)
    // All attributes should remain the same
    assert(regenerating.attributes == monster.attributes)
  }

  test("MonsterBehavior.randomBehavior should return valid behavior") {
    val behavior1 = MonsterBehavior.randomBehavior
    val behavior2 = MonsterBehavior.randomBehavior
    val behavior3 = MonsterBehavior.randomBehavior

    // Fix: Use pattern matching instead of Set.contains
    def isValidBehavior(behavior: MonsterBehavior): Boolean = behavior match
      case Aggressive | Defensive | MoreHp | Berserk | OneShot | Explosive | Regenerating => true
      case _ => false

    assert(isValidBehavior(behavior1))
    assert(isValidBehavior(behavior2))
    assert(isValidBehavior(behavior3))
  }

  test("randomBehavior should produce different behaviors over multiple calls") {
    val behaviors = (1 to 20).map(_ => MonsterBehavior.randomBehavior.toString).toSet

    // With 20 calls and 7 behaviors, we should get some variety
    // Using toString to avoid type issues
    assert(behaviors.size > 1, "Should generate different behaviors over multiple calls")
  }

  test("Behaviors should not modify original monster") {
    val original = createTestMonster()
    val originalAttack = original.attributes.attack
    val originalDefense = original.attributes.defense
    val originalHp = original.attributes.hp
    val originalBerserk = original.berserk
    val originalRegen = original.regenerating

    // Apply all behaviors
    Aggressive.apply(original)
    Defensive.apply(original)
    MoreHp.apply(original)
    Berserk.apply(original)
    OneShot.apply(original)
    Explosive.apply(original)
    Regenerating.apply(original)

    // Original should be unchanged
    assert(original.attributes.attack == originalAttack)
    assert(original.attributes.defense == originalDefense)
    assert(original.attributes.hp == originalHp)
    assert(original.berserk == originalBerserk)
    assert(original.regenerating == originalRegen)
  }

  test("Behavior combinations should work correctly") {
    val monster = createTestMonster()

    // Apply multiple behaviors in sequence
    val modified = Aggressive.apply(
      Defensive.apply(
        MoreHp.apply(
          Berserk.apply(monster)
        )
      )
    )

    // Should have all modifications
    assert(modified.attributes.attack == (20 * 1.25).toInt) // Aggressive
    assert(modified.attributes.defense == (10 * 1.25).toInt) // Defensive
    assert(modified.attributes.hp == (100 * 1.25).toInt) // MoreHp
    assert(modified.berserk == true) // Berserk
  }
