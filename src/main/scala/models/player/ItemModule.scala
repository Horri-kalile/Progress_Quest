package models.player

import util.ItemNameLoader
import util.GameConfig.{baseDropChance, specialBonusPerLucky}

import scala.util.Random

object ItemModule:

  /** Item rarity levels */
  enum ItemRarity:
    case Common, Uncommon, Rare, Epic, Legendary

  /**
   * Holds a list of possible item names.
   *
   * This is typically used to load and cache predefined names from file or config.
   *
   * @param items list of item name strings
   */
  case class ItemNames(items: List[String])

  /** Item data model */
  case class Item(name: String, gold: Double, rarity: ItemRarity)

  /** Factory trait for polymorphic item creation */
  trait ItemFactory:

    /**
     * Generates a random item based on player luck
     *
     * @param playerLucky luck stat influencing rarity and value
     * @return Some(Item) or None if creation fails (e.g. probabilistic)
     */
    def createRandomItem(playerLucky: Int): Option[Item]

  /** Companion object providing factory methods */
  object ItemFactory:

    /** Creates a factory that always creates an item (100% chance) */
    def alwaysCreate(): ItemFactory = AlwaysCreateFactory()

    /** Creates a factory that creates item based on probability + luck */
    def probBasedCreate(baseChance: Double = baseDropChance): ItemFactory = ProbBasedFactory(baseChance)

  /** Helper trait with shared logic for item factories */
  private trait ItemFactoryHelpers:

    private lazy val prefabNames: List[String] = ItemNameLoader.loadItemNames()

    private val rarityMultipliers: Map[ItemRarity, Double] = Map(
      ItemRarity.Common -> 1.0,
      ItemRarity.Uncommon -> 1.5,
      ItemRarity.Rare -> 2.0,
      ItemRarity.Epic -> 2.5,
      ItemRarity.Legendary -> 3.0
    )

    /** Randomly pick a name from the prefab list */
    protected def randomName(): Option[String] =
      if prefabNames.isEmpty then None else Some(prefabNames(Random.nextInt(prefabNames.length)))

    /** Random rarity influenced by luck */
    private def randomRarity(playerLucky: Int): ItemRarity =
      val roll = Random.nextDouble()
      val bonus = specialBonusPerLucky * playerLucky
      val legendaryThreshold = 0.96 + bonus.min(0.03)

      roll match
        case r if r < 0.50 => ItemRarity.Common
        case r if r < 0.80 => ItemRarity.Uncommon
        case r if r < 0.90 + bonus => ItemRarity.Rare
        case r if r < 0.96 + bonus => ItemRarity.Epic
        case r if r < legendaryThreshold => ItemRarity.Legendary
        case _ => ItemRarity.Common

    /** Creates an item with a name, rarity, and calculated gold value */
    protected def createItem(name: String, playerLucky: Int): Item =
      val rarity = randomRarity(playerLucky)
      val baseGold = Random.between(1, 50)
      val gold = baseGold * rarityMultipliers(rarity)
      Item(name, gold, rarity)

  /** Always creates an item regardless of probability */
  private case class AlwaysCreateFactory() extends ItemFactory with ItemFactoryHelpers:

    override def createRandomItem(playerLucky: Int): Option[Item] =
      for
        name <- randomName()
      yield createItem(name, playerLucky)

  /** Creates item probabilistically based on baseChance + luck */
  private case class ProbBasedFactory(baseChance: Double) extends ItemFactory with ItemFactoryHelpers:

    override def createRandomItem(playerLucky: Int): Option[Item] =
      val chance = (baseChance + playerLucky * specialBonusPerLucky).min(1.0)
      if Random.nextDouble() < chance then
        for name <- randomName()
          yield createItem(name, playerLucky)
      else None
