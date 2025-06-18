package controllers

case class Monster(name: String, hp: Int, strength: Int)

object MonsterController {
  
  def createMonster(name: String, hp: Int, strength: Int): Monster = {
    require(hp > 0, "HP must be positive")
    require(strength >= 0, "Strength cannot be negative")
    Monster(name, hp, strength)
  }

  
  def isAlive(monster: Monster): Boolean = monster.hp > 0

  
  def takeDamage(monster: Monster, damage: Int): Monster = {
    require(damage >= 0, "Damage cannot be negative")
    val newHp = monster.hp - damage
    monster.copy(hp = math.max(newHp, 0))
  }

  
  def takeDamageWithMessage(monster: Monster, damage: Int): (Monster, String) = {
    val damagedMonster = takeDamage(monster, damage)
    val message = if (!isAlive(damagedMonster))
      s"${monster.name} has been defeated!"
    else
      s"${monster.name} takes $damage damage! (${damagedMonster.hp} HP remaining)"
    (damagedMonster, message)
  }
}