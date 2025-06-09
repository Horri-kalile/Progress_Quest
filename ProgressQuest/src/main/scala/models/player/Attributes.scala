package models.player

import scala.annotation.targetName
import scala.util.Random

// === Attributes ===
/*strength -> more dmg, constitution -> more defence, dexterity -> probability to dodge attack, intelligence -> mana, wisdom -> how many hp restore after a fight, lucky -> probability for a special event/drop rate, */
case class Attributes(strength: Int, constitution: Int, dexterity: Int, intelligence: Int, wisdom: Int, lucky: Int):

  def add(other: Attributes): Attributes = this + other

  def total: Int =
    strength + constitution + dexterity + intelligence + wisdom + lucky

  def +(other: Attributes): Attributes = Attributes(
    strength + other.strength,
    constitution + other.constitution,
    dexterity + other.dexterity,
    intelligence + other.intelligence,
    wisdom + other.wisdom,
    lucky + other.lucky
  )

object Attributes:
  def random(): Attributes =
    Attributes(
      strength = Random.between(5, 16),
      constitution = Random.between(5, 16),
      dexterity = Random.between(5, 16),
      intelligence = Random.between(5, 16),
      wisdom = Random.between(5, 16),
      lucky = Random.between(5, 16)
    )

  //max is player level
  private def biasedValue(weight: Double = 1.0, min: Int = 1, max: Int): Int =
    val base = scala.util.Random.nextDouble() * weight * max
    math.min(max, math.max(min, base.round.toInt))

  private def generateWithWeights(weights: List[Double], playerLevel: Int): Attributes =
    val List(str, con, dex, int, wis, luck)  = weights.map(w => biasedValue(w, max = playerLevel))
    Attributes(str, con, dex, int, wis, luck)


  def biasedFor(slot: EquipmentSlot, playerLevel: Int): Attributes =
    val weights = slot match
      case EquipmentSlot.Weapon => List(2.0, 1.0, 1.0, 1.0, 1.0, 1.0)
      case EquipmentSlot.Shield => List(1.0, 2.0, 0.8, 0.5, 1.0, 1.0)
      case EquipmentSlot.Body => List(1.5, 1.8, 0.8, 0.6, 0.6, 0.8)
      case EquipmentSlot.Gauntlets => List(1.6, 1.0, 1.4, 0.5, 0.5, 1.0)
      case EquipmentSlot.Shoes => List(0.8, 0.8, 1.8, 0.8, 0.8, 1.5)
      case EquipmentSlot.Head => List(0.6, 1.0, 0.8, 1.5, 1.5, 1.0)
      case EquipmentSlot.Jewelry1 | EquipmentSlot.Jewelry2 =>
        List(0.5, 0.5, 0.8, 1.5, 1.5, 1.5)

    generateWithWeights(weights, playerLevel)

