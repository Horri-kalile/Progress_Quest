package util

import models.event.{EventFactory, EventType}
import models.player.Player

import scala.util.Random

object RandomFunctions:

  def triggerRandomEvent(player: Player): Unit =
    val randomEventType = Random.shuffle(EventType.values.toList).head
    EventFactory.executeEvent(randomEventType, player)

