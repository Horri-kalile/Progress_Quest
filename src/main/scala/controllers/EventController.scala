package controllers

import models.event.*
import models.event.GameEventModule.{EventType, GameEventFactory}
import models.player.Player
import models.monster.Monster

object EventController:

  /** Executes an event of the given type for the specified player.
    *
    * @param eventType
    *   the chosen event type (fight, mission, training, etc.)
    * @param player
    *   the current state of the player
    * @return
    *   the updated player, the generated messages, and an optional encountered monster
    */
  def runEvent(eventType: EventType, player: Player): (Player, List[String], Option[Monster]) =
    GameEventFactory.executeEvent(eventType, player)
