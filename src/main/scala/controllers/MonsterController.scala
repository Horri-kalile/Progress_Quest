package controllers

import models.monster.*
import models.player.*
import util.RandomFunctions

import scala.math.max

object MonsterController:

  def takeDamage(monster: Monster, damage: Int): (Monster, Option[Int]) =
    val damagedMonster = monster.receiveDamage(damage)

    val explosion =
      if damagedMonster.behavior == Explosive && damagedMonster.isDead then Some(damagedMonster.explosionDamage)
      else None

    (damagedMonster, explosion)

  def attackPlayer(monster: Monster, player: Player): Int =
    val baseDamage = monster.attributes.attack + (player.level * 2)
    val bonus = if monster.berserk then (monster.attributes.hp - monster.attributes.currentHp) / 10 else 0
    (baseDamage + bonus - player.attributes.constitution).max(0)

  def heal(monster: Monster, amount: Int): Monster =
    monster.copy(attributes = monster.attributes.copy(hp = monster.attributes.hp + amount))

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



