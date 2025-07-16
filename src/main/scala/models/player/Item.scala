package models.player

import scala.util.Random
import util.ItemNameLoader
import util.GameConfig.specialBonusPerLucky

enum Rarity:
  case Common, Uncommon, Rare, Epic, Legendary

case class ItemNames(items: List[String])

case class Item(name: String, gold: Double, rarity: Rarity)

object ItemFactory:
  private val preFabItems: List[String] = ItemNameLoader.loadItemNames()
  private val rarityMultipliers: Map[Rarity, Double] = Map(
    Rarity.Common -> 1.0,
    Rarity.Uncommon -> 1.5,
    Rarity.Rare -> 2.0,
    Rarity.Epic -> 2.5,
    Rarity.Legendary -> 3.0
  )

  private def randomRarity(playerLucky: Int): Rarity =
    val roll = Random.nextDouble()
    val bonus = specialBonusPerLucky * playerLucky
    roll match
      case common if common < 0.5 => Rarity.Common
      case uncommon if uncommon < 0.80 => Rarity.Uncommon
      case rare if rare < 0.90 + bonus => Rarity.Rare
      case epic if epic < 0.96 + bonus => Rarity.Epic
      case legendary if legendary < 0.96 + bonus.min(0.03) => Rarity.Legendary
      case _ => Rarity.Common


  private def createItem(name: String, playerLucky: Int): Item =
    val rarity = randomRarity(playerLucky)
    val baseGold = Random.between(1, 50)
    val gold = baseGold * rarityMultipliers(rarity)
    Item(name, gold, rarity)

  def randomItem(playerLucky: Int): Item =
    val name = preFabItems(Random.nextInt(preFabItems.length))
    createItem(name, playerLucky)