package models.event

import models.player.Item
import util.MissionLoader

import java.util.UUID
import scala.util.Random

case class MissionData(name: String, description: String)

case class Missions(missions: List[MissionData])

case class Mission(
                    id: String,
                    name: String,
                    description: String,
                    progression: Int = 0,
                    goal: Int = 1,
                    rewardExp: Int,
                    rewardGold: Option[Int] = None,
                    rewardItem: Option[Item] = None
                  ):
  def isCompleted: Boolean = progression >= goal

  def progressed(): Mission =
    if isCompleted then this
    else this.copy(progression = progression + 1)

object MissionFactory:
  private val missions: List[MissionData] = MissionLoader.loadMissions()

  def randomMission(): Mission =
    val mission = Random.shuffle(missions).head
    Mission(
      id = UUID.randomUUID().toString,
      name = mission.name,
      description = mission.description,
      goal = Random.between(1, 4),
      rewardExp = Random.between(50, 201),
      rewardGold = Some(Random.between(10, 51)),
      rewardItem = None // replace with Some(Item.random())
    )
