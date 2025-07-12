package controllers

import models.monster.*
import models.player.*
import util.RandomFunctions

import scala.math.max

object MonsterController:

  def isAlive(monster: Monster): Boolean = monster.attributes.currentHp > 0

  def takeDamage(monster: Monster, damage: Int): (Monster, Option[Int]) =
    monster.takeDamage(damage)

  def attackPlayer(monster: Monster, playerLevel: Int): Int =
    val baseDamage = monster.attributes.attack + (playerLevel * 2)
    val bonus = if monster.berserk then (monster.attributes.hp - monster.attributes.currentHp) / 10 else 0
    (baseDamage + bonus).max(0)

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

  def applyBehavior(monster: Monster): Monster =
    monster.behavior.apply(monster)
    monster


  def createMonster(player: Player,
                    name: String,
                    level: Int,
                    monsterType: MonsterType,
                    originZone: OriginZone,
                    attributes: MonsterAttributes,
                    goldReward: Int,
                    experienceReward: Int,
                    itemReward: Option[Item],
                    equipReward: Option[Equipment],
                    behavior: MonsterBehavior,
                    description: String
                   ): Monster =

    require(name.nonEmpty, "Monster name must not be empty")
    require(level > 0, "Level must be positive")
    require(attributes.hp > 0, "Monster must have positive HP")
    require(goldReward >= 0, "Gold reward cannot be negative")
    require(experienceReward >= 0, "Experience reward cannot be negative")
    require(description.nonEmpty, "Description must not be empty")

    MonstersFactory.randomMonsterForZone(player.currentZone, player.level, player.attributes.lucky, RandomFunctions.tryGenerateStrongMonster())

