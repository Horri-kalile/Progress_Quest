package controllers

case class Player(name: String, hp: Int, xp: Int, level: Int)

object PlayerController {
  def isAlive(player: Player): Boolean = player.hp > 0

  def takeDamage(player: Player, damage: Int): Player = {
    val newHp = player.hp - damage
    player.copy(hp = if (newHp < 0) 0 else newHp)
  }

  def heal(player: Player, amount: Int): Player = {
    player.copy(hp = player.hp + amount)
  }

  def gainXP(player: Player, xp: Int): Player = {
    val totalXp = player.xp + xp
    if (totalXp >= 100)
      player.copy(xp = totalXp - 100, level = player.level + 1)
    else
      player.copy(xp = totalXp)
  }
}