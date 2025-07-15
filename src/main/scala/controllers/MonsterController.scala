package controllers

import models.monster.*
import models.player.*
import util.RandomFunctions

import scala.util.Random

object MonsterController:

  def takeDamage(monster: Monster, damage: Int): (Monster, Option[Int]) =
    val damagedMonster = monster.receiveDamage(damage)

    val explosion =
      if damagedMonster.behavior == Explosive && damagedMonster.isDead then Some(damagedMonster.explosionDamage)
      else None

    (damagedMonster, explosion)

  def attackPlayer(monster: Monster, player: Player): Int =
    val baseDamage = monster.attributes.attack + (player.level * 2)
    if monster.berserk then
      val bonus: Int = Random.between(1, 5 + monster.attributes.attack)
      monster.copy(attributes = monster.attributes.copy(currentHp = (monster.attributes.currentHp - bonus).min(0)))
      (baseDamage + bonus - player.attributes.constitution).max(1)
    else
      (baseDamage - player.attributes.constitution).max(1)

  def handleRegeneration(monster: Monster): (Monster, Option[String]) =
    if monster.regenerating && !monster.isDead then
      val healAmount = Random.between(monster.level, 3 * monster.level)
      val healed = monster.receiveHealing(healAmount)
      (healed, Some(s"[Regenerating] ${monster.name} recovered $healAmount HP."))
    else (monster, None)


  def heal(monster: Monster, amount: Int): Monster =
    monster.copy(attributes = monster.attributes.copy(currentHp = (monster.attributes.currentHp + amount).min(monster.attributes.hp)))

  def describe(monster: Monster): String = {
    s"${monster.name} (Level ${monster.level}) - ${monster.monsterType} from ${monster.originZone}: ${monster.description}"
  }

  def getEquipReward(monster: Monster): Option[Equipment] =
    monster.equipReward

  def getItemReward(monster: Monster): Option[Item] =
    monster.itemReward

  def getExpReward(monster: Monster): Int =
    monster.experienceReward

  def getGoldReward(monster: Monster): Int =
    monster.goldReward

  def getMonsterDefenceAndWeakness(monster: Monster): (Int, Double, Double) =
    (monster.attributes.defense, monster.attributes.weaknessPhysical, monster.attributes.weaknessMagic)

  /**
   * Generate a random monster for player's level and zone using MonstersFactory
   */
  def getRandomMonsterForZone(playerLevel: Int, playerLucky: Int, zone: OriginZone): Monster =
    MonstersFactory.randomMonsterForZone(zone, playerLevel, playerLucky, RandomFunctions.tryGenerateStrongMonster())

