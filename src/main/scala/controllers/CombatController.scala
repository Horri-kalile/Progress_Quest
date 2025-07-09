package controllers

import models.player.Player
import models.monster.{Monster, MonstersFactory, OriginZone}

import scala.util.Random


/**
 * Combat Controller - Handles fighting mechanics
 * TODO: This needs to be implemented to complete the FightEvent
 */
object CombatController:

  /**
   * Simulate a fight between player and monster
   * Returns (updatedPlayer, combatLog)
   */
  def simulateFight(player: Player, monster: Monster): (Player, String) =

    val useSkill = player.skills.nonEmpty && Random.nextBoolean()

    val (playerAfterAction, monsterAfterAction, log1) =
      if useSkill then
        val skill = Random.shuffle(player.skills).head
        val (pAfterSkill, mAfterSkill) = skill.use(player, monster)
        val skillLog =
          if pAfterSkill == player then s"Not enough mana to use ${skill.name}, attack instead."
          else s"You used ${skill.name} on ${monster.name}."
        (pAfterSkill, mAfterSkill.asInstanceOf[Monster], skillLog)
      else
        val baseDamage = PlayerController.calculatePlayerAttack(player)
        val (mAfterAttack, maybeExplosionDmg) = monster.takeDamage(baseDamage)
        val playerAfterExplosion = maybeExplosionDmg.fold(player)(PlayerController.takeDamage(player, _))
        val attackLog = s"You attacked ${monster.name} for $baseDamage physical damage." +
          maybeExplosionDmg.map(dmg => s" ${monster.name} exploded for $dmg damage!").getOrElse("")
        (playerAfterExplosion, mAfterAttack, attackLog)

    // Monster retaliates only if still alive
    val (finalPlayer, log2) =
      if !MonsterController.isAlive(monsterAfterAction) then
        val monsterAttack = MonsterController.attackPlayer(monsterAfterAction, playerAfterAction.level)
        val playerDefense = playerAfterAction.attributes.constitution
        val damageToPlayer = (monsterAttack - playerDefense).max(0)
        val updatedPlayer = PlayerController.takeDamage(playerAfterAction, damageToPlayer)
        (updatedPlayer, s"${monster.name} attacked you for $damageToPlayer damage.")
      else (playerAfterAction, s"${monster.name} was defeated!")

    val fullLog = s"$log1\n$log2"

    (finalPlayer, fullLog)


  /**
   * Generate a random monster for player's level using MonstersFactory
   */
  def getRandomMonster(playerLevel: Int, playerLucky: Int): Monster =
    // Use the factory to get a random monster from a random zone
    val zones = models.monster.OriginZone.values
    val randomZone = zones(Random.nextInt(zones.length))

    MonstersFactory.randomMonsterForZone(randomZone, playerLevel, playerLucky)

  /**
   * Generate a random monster for player's level and zone using MonstersFactory
   */
  def getRandomMonsterForZone(playerLevel: Int, playerLucky: Int, zone: OriginZone): Monster =
    MonstersFactory.randomMonsterForZone(zone, playerLevel, playerLucky)

