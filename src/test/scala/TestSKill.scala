
import models.player.*
import org.scalatest.funsuite.AnyFunSuite

class TestSKill extends AnyFunSuite:

  test("Generate a starting skill for Mage should return a Magic skill"):
    val maybeSkill = SkillFactory.generateStartingSkill(3, ClassType.Mage)
    assert(maybeSkill.nonEmpty)
    assert(maybeSkill.get.effectType == SkillEffectType.Magic)


  test("Random skill generation returns a valid skill with expected range"):
    val skill = SkillFactory.randomSkill()
    assert(skill.manaCost >= 4 && skill.manaCost <= 12)
    assert(skill.baseMultiplier >= 0.6 && skill.baseMultiplier <= 1.5)


  test("Powered up skill increases power level"):
    val skill = GenericSkill("Test", SkillEffectType.Magic, 10, 1.5)
    val upgraded = skill.poweredUp
    assert(upgraded.powerLevel == skill.powerLevel + 1)

