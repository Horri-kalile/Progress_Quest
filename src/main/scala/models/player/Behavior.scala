package models.player

import scala.util.Random

object Behavior:

  /** Enum to represent behavior types */
  enum BehaviorType:
    case Aggressive, Defensive, FastLeveling, TwiceAttack, Heal, Lucky, InvulnerableOnce, OneShotChance

  /** Trait for behavior strategy */
  trait Strategy:
    def onGameStart(player: Player): Player = player

    def onBattleDamage(player: Player, damage: Int): Int = damage

    def onBattleEnd(amount: Int): Int = amount

    def onDamageTaken(player: Player, damage: Int): Int = damage

  /** Public API to get a behavior by type */
  given Conversion[BehaviorType, Strategy] with
    def apply(bt: BehaviorType): Strategy = bt match
      case BehaviorType.Aggressive => Aggressive()
      case BehaviorType.Defensive => Defensive()
      case BehaviorType.FastLeveling => FastLeveling()
      case BehaviorType.TwiceAttack => TwiceAttack()
      case BehaviorType.Heal => Heal()
      case BehaviorType.Lucky => Lucky()
      case BehaviorType.InvulnerableOnce => InvulnerableOnce()
      case BehaviorType.OneShotChance => OneShotChance()

  /** Aggressive: Increases damage */
  private case class Aggressive() extends Strategy:
    override def onBattleDamage(player: Player, damage: Int): Int =
      (damage * Random.between(1.1, 1.3)).toInt

  /** Defensive: Reduces damage taken */
  private case class Defensive() extends Strategy:
    override def onDamageTaken(player: Player, damage: Int): Int =
      (damage * Random.between(0.7, 0.9)).toInt

  /** FastLeveling: Extra XP */
  private case class FastLeveling() extends Strategy:
    override def onBattleEnd(amount: Int): Int =
      (amount * Random.between(0.1, 0.5)).toInt

  /** TwiceAttack: Double hit */
  private case class TwiceAttack() extends Strategy:
    override def onBattleDamage(player: Player, damage: Int): Int =
      val d1 = damage * Random.between(0.5, 1.5)
      val d2 = damage * Random.between(0.5, 1.5)
      (d1 + d2).toInt

  /** Heal after battle */
  private case class Heal() extends Strategy:
    override def onBattleEnd(amount: Int): Int =
      (amount * Random.between(0.1, 0.5)).toInt


  /** Lucky: Doubled luck stat */
  private case class Lucky() extends Strategy:
    override def onGameStart(player: Player): Player =
      val newAttr = player.baseAttributes.copy(lucky = player.baseAttributes.lucky * 2)
      player.withBaseAttributes(newAttr)

  /** Invulnerable once */
  private case class InvulnerableOnce() extends Strategy:
    private var used = false

    override def onDamageTaken(player: Player, damage: Int): Int =
      if !used then
        used = true
        0
      else damage

  /** One shot chance */
  private case class OneShotChance() extends Strategy:
    override def onBattleDamage(player: Player, damage: Int): Int =
      if Random.nextBoolean() then 9999999 else damage
