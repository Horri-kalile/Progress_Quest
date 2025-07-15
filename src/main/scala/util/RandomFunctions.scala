package util

import models.event.GameEventModule.EventType
import models.monster.OriginZone
import util.GameConfig.*

import scala.util.Random

object RandomFunctions:
  def getRandomEventType(lucky: Int): EventType =
    val specialChance = (baseSpecialChance + specialBonusPerLucky * lucky).min(maxSpecialChance)
    val gameOverChance = (baseGameOverChance - specialBonusPerLucky * lucky).max(minGameOverChance)

    val x = Random.nextDouble()

    x match
      case v if v < 0.30 => EventType.fight // 30%
      case v if v < 0.60 => EventType.mission // 30%
      case v if v < 0.70 => EventType.changeWorld // 10%
      case v if v < 0.80 => EventType.training // 10%
      case v if v < 0.85 => EventType.restore // 5%
      case v if v < 0.90 => EventType.sell // 5%
      case v if v < 0.90 + specialChance => EventType.special // dynamic %
      case v if v < 0.90 + specialChance + gameOverChance => EventType.gameOver // dynamic %
      case _ => EventType.fight // fallback, should rarely happen


  def randomDropFlags(playerLucky: Int): Boolean =
    val bonus = (playerLucky * 0.001).min(0.50) // max +50% bonus per type
    val chance = baseDropChance + bonus
    val dropItem = Random.nextDouble() < chance

    dropItem

  def tryGenerateStrongMonster(): Boolean =
    Random.nextBoolean()


