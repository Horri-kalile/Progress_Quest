package controllers

import models.event.{Mission, MissionFactory}
import models.player.Player
import models.player.Item
import controllers.PlayerController


object MissionController:

  def createRandomMission(): Mission =
    MissionFactory.randomMission()


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
   * Updates a mission in the player's list if progressed.
   *
   * @param player the player
   * @param m      the mission to progress
   * @return updated Player instance
   */
  def progressMission(player: Player, m: Mission): Player =
    val updated = player.missions.map(m1 => if m1.id == m.id then m1.progressed() else m1)
    player.withMissions(updated)







