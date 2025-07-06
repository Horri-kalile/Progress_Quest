package util

import models.event.EventType
import util.GameConfig.*
import scala.util.Random

object RandomFunctions:
  def getRandomEventType(lucky: Int): EventType =
    val specialChance = (baseSpecialChance + specialBonusPerLucky * lucky).min(maxSpecialChance)
    val gameOverChance = (baseGameOverChance - specialBonusPerLucky * lucky).max(minGameOverChance)

    val x = Random.nextDouble()

    x match
      case v if v < 0.40 => EventType.fight // 40%
      case v if v < 0.70 => EventType.mission // 30%
      case v if v < 0.80 => EventType.training // 10%
      case v if v < 0.85 => EventType.restore // 5%
      case v if v < 0.90 => EventType.sell // 5%
      case v if v < 0.90 + specialChance => EventType.special // dynamic %
      case v if v < 0.90 + specialChance + gameOverChance => EventType.gameOver // dynamic %
      case _ => EventType.fight // fallback, should rarely happen


