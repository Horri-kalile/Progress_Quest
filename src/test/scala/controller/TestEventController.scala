package controller

import controllers.{CombatController, EventController, MonsterController}
import org.scalatest.funsuite.AnyFunSuite
import models.event.GameEventModule.EventType
import models.player.{Attributes, ClassType, Identity, Player, Race}
import models.player.Behavior.BehaviorType

class TestEventController extends AnyFunSuite:


  def freshPlayer: Player =
    val identity = Identity(Race.Human, ClassType.Warrior)
    val attributes = Attributes(10, 10, 10, 10, 10, 10)
    Player("TestHero", identity, attributes, BehaviorType.Heal)

  test("runEvent handles restore event") {
    val wounded = freshPlayer.withCurrentHp(5).withCurrentMp(3)
    val (restored, messages, _) = EventController.runEvent(EventType.restore, wounded)

    assert(messages.exists(_.toLowerCase.contains("recover")), "Expected restoration message")
    assert(restored.currentHp > wounded.currentHp, "HP should be restored")
    assert(restored.currentMp > wounded.currentMp, "MP should be restored")
  }

  test("runEvent handles training event"):
    val player = freshPlayer
    val (trained, messages, _) = EventController.runEvent(EventType.training, player)

    assert(messages.exists(_.toLowerCase.contains("training")), "Expected training message")
    assert(trained.exp > player.exp, "Player should gain experience")


  test("runEvent handles mission event"):
    val player = freshPlayer
    val (updated, messages, _) = EventController.runEvent(EventType.mission, player)

    assert(messages.exists(_.toLowerCase.contains("mission")), "Expected mission-related message")
    assert(updated.activeMissions.nonEmpty, "Player should have at least one active mission")


  test("runEvent handles fight event with a defeated monster"):
    val player = freshPlayer
    val monster = MonsterController.getRandomMonsterForZone(player.level, player.attributes.lucky, player.currentZone)
    val deadMonster = monster.copy(attributes = monster.attributes.copy(currentHp = 0))

    CombatController.setLastMonster(deadMonster)

    val (updated, messages, _) = EventController.runEvent(EventType.fight, player)
    assert(messages.exists(_.toLowerCase.contains("won")) || messages.exists(_.toLowerCase.contains("xp")), "Expected fight resolution message")
    assert(updated.exp > player.exp || updated.gold > player.gold, "Player should gain XP or gold")

