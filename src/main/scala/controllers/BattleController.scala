package controllers

object BattleController {
  private val BASE_DAMAGE = 10

  def attack(player: Player, monster: Monster): (Player, Monster, String) = {
    val updatedMonster = MonsterController.takeDamage(monster, BASE_DAMAGE)
    val message = s"${player.name} attacks ${monster.name} and deals $BASE_DAMAGE damage"
    (player, updatedMonster, message)
  }

  def takeDamage(player: Player, damage: Int): (Player, String) = {
    val updatedPlayer = PlayerController.takeDamage(player, damage)
    val message =
      if (updatedPlayer.hp <= 0) s"${player.name} has died!"
      else s"${player.name} lost $damage HP (${updatedPlayer.hp} remaining)"
    (updatedPlayer, message)
  }

  def gainXP(player: Player, amount: Int): (Player, String) = {
    val updatedPlayer = PlayerController.gainXP(player, amount)
    val levelUpMsg = if (updatedPlayer.level > player.level) " Level up!" else ""
    (updatedPlayer, s"${player.name} gained $amount XP$levelUpMsg")
  }
}