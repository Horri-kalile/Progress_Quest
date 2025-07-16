package models.event

import models.player.{Item, ItemFactory}
import util.MissionLoader

import java.util.UUID
import scala.util.Random

/**
 * Data used to define mission templates (loaded from file).
 */
case class MissionData(name: String, description: String)

/**
 * Container of loaded mission data
 */
case class Missions(missions: List[MissionData])

/**
 * Represents a single mission assigned to a player.
 */
case class Mission(
                    id: String,
                    name: String,
                    description: String,
                    progression: Int = 0,
                    goal: Int = 1,
                    rewardExp: Int,
                    rewardGold: Int,
                    rewardItem: Option[Item] = None
                  ):

  /** Whether the mission is completed. */
  def isCompleted: Boolean = progression >= goal

  /** Progresses the mission by one step, if not already completed. */
  def progressed(): Mission =
    if isCompleted then this
    else copy(progression = progression + 1)

  /** Mark as fully completed (for test/debug or reward preview). */
  def complete: Mission = copy(progression = goal)


object MissionFactory:

  private val missions: List[MissionData] = MissionLoader.loadMissions()

  /**
   * Generate a random mission with scaling rewards.
   *
   * @param playerLucky how lucky the player is (affects item reward)
   * @param playerLevel used to scale reward XP/Gold
   * @return a new randomized Mission
   */
  def randomMission(playerLucky: Int, playerLevel: Int): Mission =
    val mission = Random.shuffle(missions).head
    val rewardExp = Random.between(50, 101) * playerLevel
    val rewardGold = Random.between(10, 51) * playerLevel
    val rewardItem = Option.when(Random.nextBoolean())(ItemFactory.randomItem(playerLucky))

    Mission(
      id = UUID.randomUUID().toString,
      name = mission.name,
      description = mission.description,
      goal = Random.between(1, 4),
      rewardExp = rewardExp,
      rewardGold = rewardGold,
      rewardItem = rewardItem
    )
