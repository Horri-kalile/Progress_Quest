package controllers

case class Mission(description: String, xpReward: Int)  

object MissionController {
  def start(player: Player, mission: Mission): (Player, String) = {
    (player, s"${player.name} starts the mission: ${mission.description}")
  }

  def complete(player: Player, mission: Mission): (Player, String) = {
    val updatedPlayer = PlayerController.gainXP(player, mission.xpReward) 
    (updatedPlayer, s"${player.name} completed the mission: ${mission.description}")
  }
}
