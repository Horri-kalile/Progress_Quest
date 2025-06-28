package models.player

import scala.util.Random


enum Rarity:
  case Common, Uncommon, Rare, Epic, Legendary

case class ItemNames(items: List[String])

case class Item(name: String, gold: Double, rarity: Rarity)

object ItemFactory:

  private val rarityMultipliers: Map[Rarity, Double] = Map(
    Rarity.Common -> 1.0,
    Rarity.Uncommon -> 1.5,
    Rarity.Rare -> 2.5,
    Rarity.Epic -> 5.0,
    Rarity.Legendary -> 10.0
  )

  private def randomRarity(): Rarity =
    val roll = Random.nextDouble()
    if roll < 0.5 then Rarity.Common
    else if roll < 0.75 then Rarity.Uncommon
    else if roll < 0.9 then Rarity.Rare
    else if roll < 0.98 then Rarity.Epic
    else Rarity.Legendary

  private def createItem(name: String): Item =
    val rarity = randomRarity()
    val baseGold = Random.between(1, 50)
    val gold = baseGold * rarityMultipliers(rarity)
    Item(name, gold, rarity)

  def randomItem(itemNames: List[String]): Item =
    val name = itemNames(Random.nextInt(itemNames.length))
    createItem(name)