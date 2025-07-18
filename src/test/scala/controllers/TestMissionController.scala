package controllers

import models.event.Mission
import models.player.*
import models.player.Behavior.BehaviorType
import org.scalatest.funsuite.AnyFunSuite

class MissionControllerTest extends AnyFunSuite {

  
  val freshPlayer: Player = Player(
    name = "TestHero",
    identity = Identity(Race.Human, ClassType.Warrior),
    baseAttributes = Attributes(5, 5, 5, 5, 5, 5),
    behaviorType = BehaviorType.Heal
  )

  test("createRandomMission generates a valid mission") {
    val player = freshPlayer
      .withLevel(3)
      .withBaseAttributes(freshPlayer.baseAttributes.copy(lucky = 5))

    val mission = MissionController.createRandomMission(player)

    assert(mission.name.nonEmpty)
    assert(mission.description.nonEmpty)
    assert(mission.rewardExp > 0)
    assert(mission.rewardGold > 0)
    assert(mission.goal >= 1 && mission.goal <= 3)
  }

  test("addMission adds mission to player's mission list") {
    val mission = Mission("1", "Test Mission", "Test Description", rewardExp = 100, rewardGold = 50)
    val updatedPlayer = MissionController.addMission(freshPlayer, mission)

    assert(updatedPlayer.missions.contains(mission))
  }

  test("progressMission increments mission progression without completion") {
    val mission = Mission("1", "Ongoing", "Do something", progression = 0, goal = 2, rewardExp = 50, rewardGold = 10)
    val playerWithMission = freshPlayer.withMissions(List(mission))

    val updatedPlayer = MissionController.progressMission(playerWithMission, mission)
    val updated = updatedPlayer.missions.find(_.id == mission.id).get

    assert(updated.progression == 1)
    assert(!updated.isCompleted)
  }

  test("progressMission completes mission and gives rewards") {
    val item = Item("Potion", 20.0, Rarity.Common)

    val mission = Mission(
      id = "1",
      name = "Complete me",
      description = "Finish it",
      progression = 2,
      goal = 3,
      rewardExp = 100,
      rewardGold = 50,
      rewardItem = Some(item)
    )

    val playerWithMission = freshPlayer.withMissions(List(mission))
    val updatedPlayer = MissionController.progressMission(playerWithMission, mission)

   
    assert(!updatedPlayer.missions.exists(_.id == mission.id))

   
    assert(updatedPlayer.inventory.contains(item))

   
    assert(updatedPlayer.gold >= 50)

   
  }

}
