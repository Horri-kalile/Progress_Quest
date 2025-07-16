package models.player

import scala.util.Random

/**
 * Represents a player's core RPG attributes.
 *
 * @param strength     affects physical damage
 * @param constitution affects defense
 * @param dexterity    affects dodge rate
 * @param intelligence affects magic damage
 * @param wisdom       affects experience gain
 * @param lucky        affects loot drop rates and special events
 */
case class Attributes(strength: Int, constitution: Int, dexterity: Int, intelligence: Int, wisdom: Int, lucky: Int):
  /** Returns the total of all attribute values. */
  def total: Int = strength + constitution + dexterity + intelligence + wisdom + lucky

  /** Sum another [[Attributes]] instance. */
  def +(other: Attributes): Attributes =
    Attributes(
      strength + other.strength,
      constitution + other.constitution,
      dexterity + other.dexterity,
      intelligence + other.intelligence,
      wisdom + other.wisdom,
      lucky + other.lucky
    )


object Attributes:
  /** List of attribute field names. */
  val attributeNames: Seq[String] = Seq("strength", "constitution", "dexterity", "intelligence", "wisdom", "lucky")

  /** Map of +1 increment by attribute name. */
  private val increments: Map[String, Attributes] = attributeNames.map(n => n -> singleAttribute(n, 1)).toMap

  /** Map of -1 decrement by attribute name. */
  private val decrements: Map[String, Attributes] = attributeNames.map(n => n -> singleAttribute(n, -1)).toMap

  /** Generates a random [[Attributes]] instance with base values. */
  def random(): Attributes =
    Attributes(
      strength = Random.between(10, 16),
      constitution = Random.between(5, 16),
      dexterity = Random.between(5, 16),
      intelligence = Random.between(5, 16),
      wisdom = Random.between(5, 16),
      lucky = Random.between(5, 16)
    )

  /** Generates attribute bonuses weighted by equipment slot and player level. */
  def biasedFor(slot: EquipmentSlot, playerLevel: Int): Attributes =
    val weights: List[Double] = slot match
      case EquipmentSlot.Weapon => List(2.0, 0.0, 1.0, 1.0, 1.0, 1.0)
      case EquipmentSlot.Shield => List(0.0, 2.0, 0.8, 0.5, 1.0, 1.0)
      case EquipmentSlot.Body => List(1.5, 1.8, 0.8, 0.6, 0.0, 0.8)
      case EquipmentSlot.Gauntlets => List(1.6, 1.0, 1.4, 0.5, 0.5, 1.0)
      case EquipmentSlot.Shoes => List(0.0, 0.0, 1.8, 0.8, 0.8, 1.5)
      case EquipmentSlot.Head => List(0.0, 1.0, 0.8, 1.5, 1.5, 1.0)
      case EquipmentSlot.Jewelry1 | EquipmentSlot.Jewelry2 =>
        List(0.5, 0.5, 0.8, 1.5, 1.5, 1.5)

    generateWithWeights(weights, playerLevel)

  // === Private Helpers ===

  private def singleAttribute(name: String, value: Int): Attributes = name match
    case "strength" => Attributes(value, 0, 0, 0, 0, 0)
    case "constitution" => Attributes(0, value, 0, 0, 0, 0)
    case "dexterity" => Attributes(0, 0, value, 0, 0, 0)
    case "intelligence" => Attributes(0, 0, 0, value, 0, 0)
    case "wisdom" => Attributes(0, 0, 0, 0, value, 0)
    case "lucky" => Attributes(0, 0, 0, 0, 0, value)

  private def biasedValue(weight: Double, max: Int): Int =
    val base = Random.nextDouble() * weight * max
    math.max(0, base.round.toInt)

  private def generateWithWeights(weights: List[Double], playerLevel: Int): Attributes =
    val maxStat = playerLevel + 2
    val values = weights.map(w => biasedValue(w, maxStat))
    val List(str, con, dex, int, wis, luck) = values
    Attributes(str, con, dex, int, wis, luck)

  def getIncrements: Map[String, Attributes] = increments

  def getDecrements: Map[String, Attributes] = decrements


/** Extension methods for random mutations. */
extension (attr: Attributes)

  /** Randomly increments a subset of attributes. */
  def incrementRandomAttributes(): Attributes =
    val selected = Random.shuffle(Attributes.attributeNames).take(Random.between(1, Attributes.attributeNames.size + 1))
    selected.foldLeft(attr)(_ + Attributes.getIncrements(_))

  /** Randomly decrements a subset of attributes. */
  def decrementRandomAttributes(): Attributes =
    val selected = Random.shuffle(Attributes.attributeNames).take(Random.between(1, Attributes.attributeNames.size + 1))
    selected.foldLeft(attr)(_ + Attributes.getDecrements(_))
