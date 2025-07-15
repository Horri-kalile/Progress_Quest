package controllers

import models.player.Player
import models.monster.{Monster, MonstersFactory}
import scala.util.Random

/**
 * Combat Controller - Handles fighting mechanics
 * TODO: This needs to be implemented to complete the FightEvent
 */
object CombatController :
  
  /**
   * Simulate a fight between player and monster
   * Returns (updatedPlayer, combatLog)
   */
  def simulateFight(player: Player, monster: Monster): (Player, String) =
    val playerAttack = calculatePlayerAttack(player)
    val monsterAttack = calculateMonsterAttack(monster)
    
    // Simple combat simulation
    val playerWins = playerAttack > monsterAttack || Random.nextBoolean()
    
    if (playerWins) {
      // Player wins
      val expGain = monster.experienceReward
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
  
  private def calculatePlayerAttack(player: Player): Int =
    // Base attack from level + equipment bonuses
    val baseAttack = player.level * 10
    val equipmentBonus = player.equipment.values.flatten.map(_.statBonus.strength).sum
    baseAttack + equipmentBonus + Random.nextInt(20)
  
  private def calculateMonsterAttack(monster: Monster): Int =
    monster.attributes.attack + Random.nextInt(10)
  
  /**
   * Generate a random monster for player's level using MonstersFactory
   */
  def getRandomMonster(playerLevel: Int): Monster =
    // Use the factory to get a random monster from a random zone
    val zones = models.monster.OriginZone.values
    val randomZone = zones(Random.nextInt(zones.length))
    
    MonstersFactory.randomMonsterForZone(randomZone, playerLevel)
  
  /**
   * Generate a random monster for player's level and zone using MonstersFactory
   */
  def getRandomMonsterForZone(playerLevel: Int, zone: models.monster.OriginZone): Monster =
    MonstersFactory.randomMonsterForZone(zone, playerLevel)

