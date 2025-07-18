package models.player

import util.GameConfig.oneShotChance
import scala.util.Random

/**
 * Defines player behavior strategies that affect various gameplay mechanics such as:
 * - Combat damage modifications
 * - Experience gain bonuses
 * - Damage reduction or mitigation
 * - Starting attribute boosts
 */
object Behavior:

  /** Enumeration of all possible player behavior types. */
  enum BehaviorType:
    case Aggressive, Defensive, FastLeveling, TwiceAttack, Heal, Lucky, MoreDodge, OneShotChance

  /**
   * Trait defining a behavior strategy.
   * Override the relevant hooks to alter player mechanics during gameplay.
   */
  trait Strategy:
    /** Called once when the game starts. Can modify player attributes or state. */
    def onGameStart(player: Player): Player = player

    /** Modifies the outgoing damage dealt by the player during combat. */
    def onBattleDamage(player: Player, damage: Int): Int = damage

    /** Modifies experience points or rewards earned after a battle ends. */
    def onBattleEnd(value: Int): Int = value

    /** Modifies the incoming damage when the player is attacked. */
    def onDamageTaken(player: Player, damage: Int): Int = damage

  /** Converts a BehaviorType enum to its corresponding Strategy implementation. */
  given Conversion[BehaviorType, Strategy] with
    def apply(bt: BehaviorType): Strategy = bt match
      case BehaviorType.Aggressive => Aggressive()
      case BehaviorType.Defensive => Defensive()
      case BehaviorType.FastLeveling => FastLeveling()
      case BehaviorType.TwiceAttack => TwiceAttack()
      case BehaviorType.Heal => Heal()
      case BehaviorType.Lucky => Lucky()
      case BehaviorType.MoreDodge => DexterityBoost()
      case BehaviorType.OneShotChance => OneShotChance()

  /**
   * Aggressive: Increases the player's outgoing damage by 10% to 30%.
   */
  private case class Aggressive() extends Strategy:
    override def onBattleDamage(player: Player, damage: Int): Int =
      (damage * Random.between(1.1, 1.3)).toInt

  /**
   * Defensive: Reduces incoming damage taken by 10% to 30%.
   */
  private case class Defensive() extends Strategy:
    override def onDamageTaken(player: Player, damage: Int): Int =
      (damage * Random.between(0.7, 0.9)).toInt

  /**
   * FastLeveling: Grants an extra 10% to 50% experience gain after battles.
   */
  private case class FastLeveling() extends Strategy:
    override def onBattleEnd(amount: Int): Int =
      (amount * Random.between(0.1, 0.5)).toInt

  /**
   * TwiceAttack: Simulates a double attack each dealing 50% to 150% of base damage.
   */
  private case class TwiceAttack() extends Strategy:
    override def onBattleDamage(player: Player, damage: Int): Int =
      val d1 = damage * Random.between(0.5, 1.5)
      val d2 = damage * Random.between(0.5, 1.5)
      (d1 + d2).toInt

  /**
   * Heal: Restores 10% to 50% of XP as HP after battle ends.
   */
  private case class Heal() extends Strategy:
    override def onBattleEnd(amount: Int): Int =
      (amount * Random.between(0.1, 0.5)).toInt

  /**
   * Lucky: Doubles the player's luck attribute at game start.
   */
  private case class Lucky() extends Strategy:
    override def onGameStart(player: Player): Player =
      val newAttr = player.baseAttributes.copy(lucky = player.baseAttributes.lucky * 2)
      player.withBaseAttributes(newAttr)

  /**
   * DexterityBoost: Increases the player's dexterity attribute by a random amount at game start.
   * This boost affects dodge chance during battle.
   */
  private case class DexterityBoost() extends Strategy:
    override def onGameStart(player: Player): Player =
      val dexBoost = Random.between(50, 100)
      val newAttr = player.baseAttributes.copy(dexterity = player.baseAttributes.dexterity + dexBoost)
      player.withBaseAttributes(newAttr)

  /**
   * OneShotChance: Grants a 25% chance to instantly defeat an enemy by dealing massive damage.
   * If triggered, damage = 99 × player level; otherwise normal damage applies.
   */
  private case class OneShotChance() extends Strategy:
    override def onBattleDamage(player: Player, damage: Int): Int =
      if Random.nextDouble() < oneShotChance then 99 * player.level else damage
