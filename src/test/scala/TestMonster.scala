import models.monster.{Monster, MonsterAttributes, MonsterBehavior, MonsterType, Aggressive}
import models.world.OriginZone
import org.scalatest.funsuite.AnyFunSuite

class TestMonster extends AnyFunSuite:


  private def createTestMonster(): Monster =
    Monster(
      name = "Test Goblin",
      level = 5,
      monsterType = MonsterType.Beast,
      originZone = OriginZone.Forest,
      attributes = MonsterAttributes(100, 80, 20, 10, 1.0, 1.0),
      goldReward = 25,
      experienceReward = 50,
      itemReward = None,
      equipReward = None,
      behavior = Aggressive,
      description = "A small green creature",
      berserk = false,
      regenerating = false
    )

  test("Monster should be created with correct properties") {
    val monster = createTestMonster()
    assert(monster.name == "Test Goblin")
    assert(monster.level == 5)
    assert(monster.monsterType == MonsterType.Beast)
    assert(monster.originZone == OriginZone.Forest)
    assert(monster.goldReward == 25)
    assert(monster.experienceReward == 50)
    assert(monster.berserk == false)
    assert(monster.regenerating == false)
  }

  test("receiveDamage should reduce current HP correctly") {
    val monster = createTestMonster()
    val damaged = monster.receiveDamage(30)

    assert(damaged.attributes.currentHp == 50) // 80 - 30 = 50
    assert(damaged.attributes.hp == 100) // max HP
    assert(monster.attributes.currentHp == 80) 
  }

  test("receiveDamage should not reduce HP below 0") {
    val monster = createTestMonster()
    val overkilled = monster.receiveDamage(200) // more than current HP

    assert(overkilled.attributes.currentHp == 0)
    assert(!overkilled.isDead == false) // should be dead now
  }

  test("receiveHealing should increase current HP correctly") {
    val monster = createTestMonster().receiveDamage(40) // 80 - 40 = 40 HP
    val healed = monster.receiveHealing(20)

    assert(healed.attributes.currentHp == 60) // 40 + 20 = 60
    assert(healed.attributes.hp == 100) // max HP 
  }

  test("receiveHealing should not exceed maximum HP") {
    val monster = createTestMonster() // 80/100 HP
    val overhealed = monster.receiveHealing(50) // would be 130

    assert(overhealed.attributes.currentHp == 100) // capped at max HP
    assert(overhealed.attributes.hp == 100)
  }

  test("isDead should return correct status") {
    val aliveMonster = createTestMonster()
    val deadMonster = aliveMonster.receiveDamage(200)

    assert(!aliveMonster.isDead)
    assert(deadMonster.isDead)
    assert(deadMonster.attributes.currentHp == 0)
  }

  test("explosionDamage should return attack value") {
    val monster = createTestMonster()
    assert(monster.explosionDamage == 20) // same as attack
  }

  test("Monster copy should work correctly") {
    val original = createTestMonster()
    val modified = original.copy(name = "Super Goblin", level = 10)

    assert(modified.name == "Super Goblin")
    assert(modified.level == 10)
    assert(modified.attributes == original.attributes) 
    assert(original.name == "Test Goblin")
  }

  test("Monster should handle berserk and regenerating flags") {
    val normalMonster = createTestMonster()
    val specialMonster = normalMonster.copy(berserk = true, regenerating = true)

    assert(!normalMonster.berserk)
    assert(!normalMonster.regenerating)
    assert(specialMonster.berserk)
    assert(specialMonster.regenerating)
  }
