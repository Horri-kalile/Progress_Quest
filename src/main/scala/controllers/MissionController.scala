package controllers

import models.event.{Mission, MissionFactory}
import models.player.Player
import models.player.Item
import controllers.PlayerController


object MissionController{

 def createRandomMission(): Mission = {
    MissionFactory.randomMission()
  }


  def progressMission(mission: Mission): Mission = {
    mission.progressed()
  }

 
  def isCompleted(mission: Mission): Boolean = {
    mission.isCompleted
  }

 
  def completeMission(player: Player, mission: Mission): Player = {
    var updatedPlayer = player.gainExp(mission.rewardExp)

    mission.rewardGold.foreach { gold =>
      updatedPlayer = updatedPlayer.earnGold(gold)
    }

    mission.rewardItem.foreach { item =>
      updatedPlayer = PlayerController.addItem(updatedPlayer, item)

    }

    updatedPlayer
  } 
}



