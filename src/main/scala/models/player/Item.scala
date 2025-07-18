package models.player

import scala.util.Random
import util.ItemNameLoader
import util.GameConfig.specialBonusPerLucky

/**
 * Represents the rarity level of an item.
 */
enum Rarity:
  case Common, Uncommon, Rare, Epic, Legendary

/**
 * Represents an in-game item with name, value in gold, and rarity.
 *
 * @param name   the name of the item
 * @param gold   the item's gold value (sell price)
 * @param rarity the item's rarity level
 */
case class Item(name: String, gold: Double, rarity: Rarity)

/**
 * Holds a list of possible item names.
 *
 * This is typically used to load and cache predefined names from file or config.
 *
 * @param items list of item name strings
 */
case class ItemNames(items: List[String])

/**
 * Factory object for generating random in-game [[Item]]s.
 *
 * Item rarity and value are influenced by the player's luck stat.
 */
object ItemFactory:

  /** List of all preloaded item names used during random generation. */
  private val preFabItems: List[String] = ItemNameLoader.loadItemNames()

  /** Multiplier applied to base item gold value based on its rarity. */
  private val rarityMultipliers: Map[Rarity, Double] = Map(
    Rarity.Common -> 1.0,
    Rarity.Uncommon -> 1.5,
    Rarity.Rare -> 2.0,
    Rarity.Epic -> 2.5,
    Rarity.Legendary -> 3.0
  )

  /**
   * Determines a random item rarity, weighted by base probabilities and luck bonus.
   *
   * The player's luck increases chances for rarer items. Probability thresholds:
   * - Common:     < 50%
   * - Uncommon:   < 80%
   * - Rare:       < 90% + luck bonus
   * - Epic:       < 96% + luck bonus
   * - Legendary:  up to a maximum of 99%
   *
   * @param playerLucky the player's luck attribute (used to shift rarity odds)
   * @return the generated [[Rarity]] for the item
   */
  private def randomRarity(playerLucky: Int): Rarity =
    val roll = Random.nextDouble()
    val bonus = specialBonusPerLucky * playerLucky
    val legendaryThreshold = 0.96 + bonus.min(0.03) // max 3% bonus chance for legendary

    roll match
      case r if r < 0.50 => Rarity.Common
      case r if r < 0.80 => Rarity.Uncommon
      case r if r < 0.90 + bonus => Rarity.Rare
      case r if r < 0.96 + bonus => Rarity.Epic
      case r if r < legendaryThreshold => Rarity.Legendary
      case _ => Rarity.Common

  /**
   * Creates a new [[Item]] instance with a random gold value and rarity.
   *
   * @param name        the item's name (randomly selected externally)
   * @param playerLucky the luck value of the player
   * @return a new item with value and rarity based on player luck
   */
  private def createItem(name: String, playerLucky: Int): Item =
    val rarity = randomRarity(playerLucky)
    val baseGold = Random.between(1, 50)
    val gold = baseGold * rarityMultipliers(rarity)
    Item(name, gold, rarity)

  /**
   * Generates a fully randomized [[Item]] using player luck to influence rarity.
   *
   * A name is selected from the preloaded item pool, then value and rarity are calculated.
   *
   * @param playerLucky the luck stat of the player
   * @return a new randomly generated item
   */
  def randomItem(playerLucky: Int): Item =
    val name = preFabItems(Random.nextInt(preFabItems.length))
    createItem(name, playerLucky)
