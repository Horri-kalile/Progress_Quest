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
   *   - Fight:        30%
   *   - Mission:      30%
   *   - ChangeWorld:  10%
   *   - Training:     10%
   *   - Restore:       5%
   *   - Sell:          5%
   *   - Special:     dynamic, scaled by luck
   *   - GameOver:    dynamic, reduced by luck
   *
   * @param lucky player's lucky attribute
   * @return a random EventType
   */
  def getRandomEventType(lucky: Int): EventType =
    val specialChance = (baseSpecialChance + specialBonusPerLucky * lucky).capped(0.0, maxSpecialChance)
    val gameOverChance = (baseGameOverChance - specialBonusPerLucky * lucky).capped(minGameOverChance, 1.0)
    val x = Random.nextDouble()

    x match
      case v if v < 0.30 => fight
      case v if v < 0.60 => mission
      case v if v < 0.70 => changeWorld
      case v if v < 0.80 => training
      case v if v < 0.85 => restore
      case v if v < 0.90 => sell
      case v if v < 0.90 + specialChance => special
      case v if v < 0.90 + specialChance + gameOverChance => gameOver
      case _ => training // fallback

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