import models.event.GameEventModule.EventType
import org.scalatest.funsuite.AnyFunSuite
import util.RandomFunctions

class TestRandomFunctions extends AnyFunSuite:
  private val lucky = 500
  private val iterations = 1000
  test("getRandomEventType produces more specials with high luck"):
    val lowLuckSpecials = (1 to iterations).count(_ => RandomFunctions.getRandomEventType(0) == EventType.special)
    val highLuckSpecials = (1 to iterations).count(_ => RandomFunctions.getRandomEventType(lucky) == EventType.special)
    assert(highLuckSpecials > lowLuckSpecials)

  test("getRandomEventType produces fewer gameOver with high luck"):
    val lowLuckDeaths = (1 to iterations).count(_ => RandomFunctions.getRandomEventType(0) == EventType.gameOver)
    val highLuckDeaths = (1 to iterations).count(_ => RandomFunctions.getRandomEventType(lucky) == EventType.gameOver)
    assert(highLuckDeaths < lowLuckDeaths)

  test("randomDropFlags returns true more often with higher luck"):
    val lowLuckDrops = (1 to iterations).count(_ => RandomFunctions.randomDropFlags(0))
    val highLuckDrops = (1 to iterations).count(_ => RandomFunctions.randomDropFlags(lucky))
    assert(highLuckDrops > lowLuckDrops)

  test("tryGenerateStrongMonster returns roughly 50% true"):
    val results = (1 to iterations).map(_ => RandomFunctions.tryGenerateStrongMonster())
    val trues = results.count(identity)
    val ratio = trues.toDouble / results.size
    assert(ratio > 0.4 && ratio < 0.6)

  test("capped extension clamps values correctly"):
    import RandomFunctions.capped
    assert(5.0.capped(0.0, 10.0) == 5.0)
    assert((-1.0).capped(0.0, 10.0) == 0.0)
    assert(100.0.capped(0.0, 10.0) == 10.0)
