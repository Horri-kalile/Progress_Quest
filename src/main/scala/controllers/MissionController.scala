package controllers

import models.event.{Mission, MissionFactory}
import models.player.Player
import models.player.Item
import controllers.PlayerController


object MissionController:

  /**
   * Generates a new random mission for the given player.
   *
   * The mission is influenced by the player's luck and level to ensure appropriate difficulty and rewards.
   *
   * @param player the player for whom the mission is generated
   * @return a new randomized [[Mission]] instance
   */
  def createRandomMission(player: Player): Mission =
    MissionFactory.randomMission(playerLucky = player.attributes.lucky, playerLevel = player.level)

  /**
   * Adds a mission to the player's mission list.
   *
   * The mission is appended to the current active mission list.
   *
   * @param player  the player receiving the mission
   * @param mission the mission to add
   * @return updated [[Player]] instance with the mission added
   */
  def addMission(player: Player, mission: Mission): Player =
    player.withMissions(player.missions :+ mission)

  /**
   * Progresses a mission for the player and checks for completion.
   *
   * If the mission becomes completed after progression, rewards (gold, XP, optional item) are granted.
   * The completed mission is then removed from the mission list.
   *
   * @param player the player progressing a mission
   * @param m      the mission to progress
   * @return updated [[Player]] instance with mission progress and possible rewards applied
   */
  def progressMission(player: Player, m: Mission): Player =
    // Update missions: progress the target one
    val updatedMissions = player.missions.map:
      case mission if mission.id == m.id => mission.progressed()
      case other => other

    // Check if the mission is now completed
    val maybeCompleted = updatedMissions.find(_.id == m.id).filter(_.isCompleted)

    maybeCompleted match
      case Some(completed) =>
        val withGold = PlayerController.addGold(player, completed.rewardGold)
        val withXP = PlayerController.gainXP(withGold, completed.rewardExp)
        val withItem = completed.rewardItem match
          case Some(item) => PlayerController.addItem(withXP, item)
          case None => withXP
        withItem.withMissions(updatedMissions.filterNot(_.id == m.id))

      case None =>
        player.withMissions(updatedMissions)
