import models.monster.MonsterAttributes
import org.scalatest.funsuite.AnyFunSuite

class TestMonsterAttributes extends AnyFunSuite:

  test("MonsterAttributes should be created with correct values") {
    val attrs = MonsterAttributes(100, 80, 25, 15, 1.2, 0.8)
    assert(attrs.hp == 100)
    assert(attrs.currentHp == 80)
    assert(attrs.attack == 25)
    assert(attrs.defense == 15)
    assert(attrs.weaknessPhysical == 1.2)
    assert(attrs.weaknessMagic == 0.8)
  }

  test("MonsterAttributes should use default weakness values") {
    val attrs = MonsterAttributes(50, 50, 10, 5)
    assert(attrs.weaknessPhysical == 1.0)
    assert(attrs.weaknessMagic == 1.0)
  }

  test("MonsterAttributes should handle zero and negative values") {
    val attrs = MonsterAttributes(0, 0, 0, 0, 0.0, 0.0)
    assert(attrs.hp == 0)
    assert(attrs.currentHp == 0)
    assert(attrs.attack == 0)
    assert(attrs.defense == 0)
    assert(attrs.weaknessPhysical == 0.0)
    assert(attrs.weaknessMagic == 0.0)
  }

  test("MonsterAttributes copy should modify only specified fields") {
    val original = MonsterAttributes(100, 100, 20, 10, 1.5, 1.2)
    val modified = original.copy(currentHp = 50, attack = 25)

    assert(modified.hp == 100)
    assert(modified.currentHp == 50)
    assert(modified.attack == 25)
    assert(modified.defense == 10)
    assert(modified.weaknessPhysical == 1.5)
    assert(modified.weaknessMagic == 1.2)
  }

  test("MonsterAttributes should handle extreme weakness values") {
    val resistantMonster = MonsterAttributes(100, 100, 15, 20, 0.1, 0.1) // 90% resistance
    val vulnerableMonster = MonsterAttributes(80, 80, 12, 8, 2.5, 3.0) // 150-200% extra damage

    assert(resistantMonster.weaknessPhysical == 0.1)
    assert(resistantMonster.weaknessMagic == 0.1)
    assert(vulnerableMonster.weaknessPhysical == 2.5)
    assert(vulnerableMonster.weaknessMagic == 3.0)
  }

  test("MonsterAttributes equality should work correctly") {
    val attrs1 = MonsterAttributes(100, 85, 20, 15, 1.0, 1.0)
    val attrs2 = MonsterAttributes(100, 85, 20, 15, 1.0, 1.0)
    val attrs3 = MonsterAttributes(100, 85, 20, 15, 1.1, 1.0)

    assert(attrs1 == attrs2)
    assert(attrs1 != attrs3)
  }
