package util

import models.event.GameEventModule.EventType
import models.event.GameEventModule.EventType.*
import util.GameConfig.*

import scala.util.Random

object RandomFunctions:

  /**
   * Extension method to cap a value between a minimum and maximum bound.
   *
   * @return the capped value between min and max
   */
  extension (x: Double)
    def capped(min: Double, max: Double): Double = x.max(min).min(max)

  /**
   * Generates a random [[EventType]] based on player's luck.
   *
   * - Base chances:
   *   - Fight:        25%
   *   - Mission:      25%
   *   - ChangeWorld:  10%
   *   - Training:     10%
   *   - Restore:       5%
   *   - Sell:          5%
   *   - Special:      15% + luck bonus (INCREASED!)
   *   - GameOver:     5% - luck bonus
   *
   * @param lucky player's lucky attribute
   * @return a random EventType
   */
  def getRandomEventType(lucky: Int): EventType =
    // INCREASED base special chance from dynamic to fixed 15%
    val baseSpecialChance = 0.15 // 15% base chance
    val luckyBonus = (lucky * 0.01).capped(0.0, 0.10) // Max +10% from luck
    val specialChance = (baseSpecialChance + luckyBonus).capped(0.15, 0.25) // 15-25% range
    
    // Reduced gameOver chance and make it decrease with luck
    val baseGameOverChance = 0.05 // 5% base
    val gameOverChance = (baseGameOverChance - luckyBonus * 0.5).capped(0.01, 0.05) // 1-5% range
    
    val x = Random.nextDouble()

    x match
      case v if v < 0.25 => fight                                    // 25%
      case v if v < 0.50 => mission                                  // 25%
      case v if v < 0.60 => changeWorld                              // 10%
      case v if v < 0.70 => training                                 // 10%
      case v if v < 0.75 => restore                                  // 5%
      case v if v < 0.80 => sell                                     // 5%
      case v if v < 0.80 + specialChance => special                  // 15-25% (MUCH HIGHER!)
      case v if v < 0.80 + specialChance + gameOverChance => gameOver // 1-5%
      case _ => training // fallback for remaining %

  /**
   * Determines whether an item should drop based on player's luck.
   *
   * @param playerLucky player's lucky attribute
   * @return true if drop occurs
   */
  def randomDropFlags(playerLucky: Int): Boolean =
    val bonus = (playerLucky * 0.001).min(0.50) // Max +50% bonus
    val chance = baseDropChance + bonus
    Random.nextDouble() < chance

  /**
   * Used to spawn strong monsters.
   */
  def tryGenerateStrongMonster(): Boolean =
    Random.nextBoolean()