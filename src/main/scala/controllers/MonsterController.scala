package controllers

import models.monster.*
import models.player.*
import models.player.EquipmentModule.Equipment
import models.player.ItemModule.Item
import models.world.OriginZone
import util.GameConfig.{dodgeBonusByDexterity, maxDodgeChance}
import util.RandomFunctions

import scala.util.Random

object MonsterController:

  def takeDamage(monster: Monster, damage: Int): (Monster, Option[Int]) =
    val damagedMonster = monster.receiveDamage(damage)

    val explosion =
      if damagedMonster.behavior == Explosive && damagedMonster.isDead then Some(damagedMonster.explosionDamage)
      else None

    (damagedMonster, explosion)

  def attackPlayer(monster: Monster, player: Player): (Int, String, Monster) =
    val baseDamage = monster.attributes.attack + (player.level * 2)

    // Dodge chance based on dexterity
    val dodgeChance = (player.attributes.dexterity * dodgeBonusByDexterity).min(maxDodgeChance)
    val didDodge = Random.nextDouble() < dodgeChance

    if didDodge then
      (0, s"${player.name} dodged the attack!", monster)
    else if monster.berserk then
      val bonus = Random.between(1, 5 + monster.attributes.attack)
      val selfDamage = bonus
      val updatedMonster = monster.copy(attributes = monster.attributes.copy(currentHp = (monster.attributes.currentHp - selfDamage).max(0)))
      val damage = (baseDamage + bonus - player.attributes.constitution).max(1)
      (damage, s"[Berserk] ${monster.name} attacked for $damage and lost $selfDamage HP!", updatedMonster)
    else
      val damage = (baseDamage - player.attributes.constitution).max(1)
      (damage, s"${monster.name} attacked for $damage.", monster)


  def handleRegeneration(monster: Monster): (Monster, Option[String]) =
    if monster.regenerating && !monster.isDead then
      val healAmount = Random.between(1, 2 * monster.level)
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

