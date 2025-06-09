package models.player

import models.player.Player

import scala.util.Random

enum BehaviorType:
  case Aggressive, Defensive, FastLeveling, TwiceAttack, Heal, Lucky, InvulnerableOnce, OneShotChange

sealed trait Behavior:
  def onGameStart(player: Player): Unit = ()

  def onBattleDamage(player: Player, damage: Int): Int = damage

  def onBattleEnd(player: Player, exp: Int): Int = exp

  def onDamageTaken(player: Player, damage: Int): Int = damage

// Aggressive enhance damage during battle
object Aggressive extends Behavior:
  override def onBattleDamage(player: Player, damage: Int): Int =
    val newDamage = (damage * Random.between(1.1, 2.0)).toInt
    newDamage

// Defensive reduces damage taken during battle
object Defensive extends Behavior:
  override def onDamageTaken(player: Player, damage: Int): Int =
    val reduced = (damage * 0.7).toInt // 30% damage reduction
    reduced

// FastLeveling could grant extra XP at battle end
object FastLeveling extends Behavior:
  override def onBattleEnd(player: Player, exp: Int): Int = (exp * 1.3).toInt // 30% more exp

// DoubleAttack could attack twice
object TwiceAttack extends Behavior:
  override def onBattleDamage(player: Player, damage: Int): Int =
    val newDamage = (damage * Random.between(0.5, 1.5) + damage * Random.between(0.5, 1.5)).toInt
    newDamage

// DoubleHP doubles hp at start game
object Heal extends Behavior:
  override def onBattleEnd(player: Player, exp: Int): Int =
    val heal = (player.maxHP * Random.between(0.1, 0.5)).toInt
    player.hp = player.hp + heal
    exp

// Lucky doubles lucky attribute at game start
object Lucky extends Behavior:
  override def onGameStart(player: Player): Unit =
    val current = player.attributes
    player.attributes = current.copy(lucky = current.lucky * 2)

// InvulnerableOnce could miss once attack
object InvulnerableOnce extends Behavior:
  private var used = false

  override def onDamageTaken(player: Player, damage: Int): Int =
    if !used then
      used = true
      0 // No damage once
    else
      damage

// OneShotChance could instant kill monster
object OneShotChance extends Behavior:
  override def onBattleDamage(player: Player, damage: Int): Int =
    if Random.nextBoolean() then 9999999 else damage


object BehaviorResolver:
  def getBehavior(bt: BehaviorType): Behavior = bt match
    case BehaviorType.Aggressive => Aggressive
    case BehaviorType.Defensive => Defensive
    case BehaviorType.FastLeveling => FastLeveling
    case BehaviorType.TwiceAttack => TwiceAttack
    case BehaviorType.Heal => Heal
    case BehaviorType.Lucky => Lucky
    case BehaviorType.InvulnerableOnce => InvulnerableOnce
    case BehaviorType.OneShotChange => OneShotChance
