package model

import models.player.ItemModule.*
import org.scalatest.funsuite.AnyFunSuite

class TestItem extends AnyFunSuite:
  val AlwaysCreate: ItemFactory = ItemFactory.alwaysCreate()

  test("AlwaysCreateFactory always returns an item"):
    val result = AlwaysCreate.createRandomItem(10)
    assert(result.isDefined)
    val item = result.get
    assert(item.name.nonEmpty)
    assert(item.gold > 0)
    assert(ItemRarity.values.contains(item.rarity))

  test("ProbBasedFactory returns item with 100% baseChance"):
    val result = AlwaysCreate.createRandomItem(0)
    assert(result.isDefined)

  test("ProbBasedFactory returns None with 0% baseChance and 0 luck"):
    val ProbBasedCreate: ItemFactory = ItemFactory.probBasedCreate(0.0)
    val result = ProbBasedCreate.createRandomItem(0)
    assert(result.isEmpty)

  test("ProbBasedFactory returns more Item than None with higher prob and lucky"):
    val factory = ItemFactory.probBasedCreate()
    val highLuckResults = (1 to 1000).map(_ => factory.createRandomItem(1000))
    val lowLuckResults = (1 to 1000).map(_ => factory.createRandomItem(1))
    val highLuckSuccesses = highLuckResults.count(_.isDefined)
    val lowLuckSuccesses = lowLuckResults.count(_.isDefined)
    println(highLuckSuccesses)
    println(lowLuckSuccesses)
    assert(highLuckSuccesses > lowLuckSuccesses)

  test("Higher luck increases chance of rare items (probabilistically)"):
    val lowRarities = (1 to 1000).flatMap(_ => AlwaysCreate.createRandomItem(0)).map(_.rarity)
    val highRarities = (1 to 1000).flatMap(_ => AlwaysCreate.createRandomItem(1000)).map(_.rarity)

    def countRare(rarities: Seq[ItemRarity]) =
      rarities.count(r => r.ordinal == ItemRarity.Rare.ordinal)

    val lowCount = countRare(lowRarities)
    val highCount = countRare(highRarities)
    println(highCount)
    println(lowCount)
    assert(highCount >= lowCount)

  test("AlwaysCreateFactory returns Legendary sometimes with high luck"):
    val factory = ItemFactory.alwaysCreate()
    val items = (1 to 1000).flatMap(_ => AlwaysCreate.createRandomItem(1000))
    val hasLegendary = items.exists(_.rarity == ItemRarity.Legendary)
    assert(hasLegendary)
