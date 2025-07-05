package controllers

import models.player.Player
import models.monster.{Monster, MonsterFactory}
import scala.util.Random

/**
 * Combat Controller - Handles fighting mechanics
 * TODO: This needs to be implemented to complete the FightEvent
 */
object CombatController {
  
  /**
   * Simulate a fight between player and monster
   * Returns (updatedPlayer, combatLog)
   */
  def simulateFight(player: Player, monster: Monster): (Player, String) = {
    val playerAttack = calculatePlayerAttack(player)
    val monsterAttack = calculateMonsterAttack(monster)
    
    // Simple combat simulation
    val playerWins = playerAttack > monsterAttack || Random.nextBoolean()
    
    if (playerWins) {
      // Player wins
      val expGain = monster.expReward
      val goldGain = monster.goldReward
      val updatedPlayer = PlayerController.gainXP(
        PlayerController.addGold(player, goldGain), 
        expGain
      )
      (updatedPlayer, s"You defeated ${monster.name}! +$expGain EXP, +$goldGain gold")
    } else {
      // Monster wins
      val damage = monsterAttack - playerAttack + Random.nextInt(10)
      val updatedPlayer = PlayerController.takeDamage(player, damage)
      (updatedPlayer, s"${monster.name} dealt $damage damage to you!")
    }
  }
  
  private def calculatePlayerAttack(player: Player): Int = {
    // Base attack from level + equipment bonuses
    val baseAttack = player.level * 10
    val equipmentBonus = player.equipment.values.flatten.map(_.attackBonus).sum
    baseAttack + equipmentBonus + Random.nextInt(20)
  }
  
  private def calculateMonsterAttack(monster: Monster): Int = {
    monster.attributes.attack + Random.nextInt(10)
  }
  
  /**
   * Generate a random monster for player's level
   */
  def getRandomMonster(playerLevel: Int): Monster = {
    // TODO: Implement proper monster selection based on level
    // For now, return a simple monster
    Monster(
      name = "Goblin",
      attributes = models.monster.MonsterAttributes(
        hp = 50 + (playerLevel * 5),
        attack = 15 + (playerLevel * 2),
        defense = 10 + playerLevel,
        level = playerLevel
      ),
      behavior = models.monster.Aggressive,
      originZone = models.monster.OriginZone.Grassland,
      monsterType = models.monster.MonsterType.Beast,
      expReward = 25 + (playerLevel * 5),
      goldReward = 10 + (playerLevel * 2)
    )
  }
}
