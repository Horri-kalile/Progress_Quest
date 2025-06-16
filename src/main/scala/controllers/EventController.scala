package controllers

enum Event {
  case GainXP(amount: Int)
  case Heal(amount: Int)
  case TakeDamage(amount: Int)
}

object EventController {
  def applyEvent(player: Player, event: Event): (Player, String) = {
    event match {
      case Event.GainXP(xp) => {
        val updated = PlayerController.gainXP(player, xp)
        (updated, s"${player.name} gains $xp XP")
      }
      case Event.Heal(hp) => {
        val updated = PlayerController.heal(player, hp)
        (updated, s"${player.name} recovers $hp HP")
      }
      case Event.TakeDamage(dmg) => {
        val updated = PlayerController.takeDamage(player, dmg)
        val msg = if (updated.hp <= 0) s"${player.name} has died!" else s"${player.name} loses $dmg HP"
        (updated, msg)
      }
    }
  }
}