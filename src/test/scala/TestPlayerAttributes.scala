import models.player.Attributes
import models.player.EquipmentModule.EquipmentSlot
import org.scalatest.funsuite.AnyFunSuite

class TestPlayerAttributes extends AnyFunSuite:

  test("Addition of two Attributes should sum each field") {
    val a1 = Attributes(1, 2, 3, 4, 5, 6)
    val a2 = Attributes(6, 5, 4, 3, 2, 1)
    val result = a1 + a2
    assert(result == Attributes(7, 7, 7, 7, 7, 7))
  }

  test("Total should be sum of all attributes") {
    val attr = Attributes(2, 2, 2, 2, 2, 2)
    assert(attr.total == 12)
  }

  test("incrementRandomAttributes should increase total or stay same") {
    val original = Attributes(5, 5, 5, 5, 5, 5)
    val incremented = original.incrementRandomAttributes()
    assert(incremented.total >= original.total)
  }

  test("decrementRandomAttributes should decrease total or stay same") {
    val original = Attributes(5, 5, 5, 5, 5, 5)
    val decremented = original.decrementRandomAttributes()
    assert(decremented.total <= original.total)
  }

  test("random generates attributes in expected ranges") {
    val a = Attributes.random()
    assert(a.strength >= 10 && a.strength <= 15)
    assert(a.constitution >= 5 && a.constitution <= 15)
    assert(a.lucky >= 5 && a.lucky <= 15)
  }

  test("biasedFor returns attributes influenced by equipment slot") {
    val attrs = Attributes.biasedFor(EquipmentSlot.Weapon, 5)
    assert(attrs.strength >= 0) // strength weight = 2.0
    assert(attrs.total > 0)
  }
