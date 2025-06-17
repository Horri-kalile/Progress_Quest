package controllers
case class Mission(description: String, xpReward: Int)

enum MissionDecision:
  case Accept, Refuse


object MissionController {
  def start(player: Player, mission: Mission): (Player, String) = {
    (player, s"${player.name} starts the mission: ${mission.description}")
  }

  def complete(player: Player, mission: Mission): (Player, String) = {
    val updatedPlayer = PlayerController.gainXP(player, mission.xpReward) 
    (updatedPlayer, s"${player.name} completed the mission: ${mission.description}")
  }

  def proposeMission(player: Player, mission: Mission): (Player, MissionDecision) = {
    val accepted = scala.util.Random.nextBoolean()
    val decision = if accepted then MissionDecision.Accept else MissionDecision.Refuse
    println(s"${player.name} ${if accepted then "accepte" else "refuse"} la mission : ${mission.description}")
    (player, decision)
  }

}
