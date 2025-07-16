package controllers

import controllers.PlayerController.gainXP
import models.event.{Mission, MissionFactory}
import models.player.Player


object MissionController:

  def createRandomMission(player: Player): Mission =
    MissionFactory.randomMission(playerLucky = player.attributes.lucky, playerLevel = player.level)


  /**
   * Adds a mission to the player's mission list.
   *
   * @param player  the player receiving the mission
   * @param mission the mission to add
   * @return updated Player instance with added mission
   */
  def addMission(player: Player, mission: Mission): Player =
    player.withMissions(player.missions :+ mission)


  /**
   * Updates a mission in the player's list and applies rewards if it's completed.
   *
   * @param player the player progressing a mission
   * @param m      the mission to progress
   * @return updated Player instance
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










