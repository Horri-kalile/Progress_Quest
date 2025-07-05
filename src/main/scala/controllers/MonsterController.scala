package controllers

import models.monster._
import scala.math.max

object MonsterController:


  def isAlive(monster: Monster): Boolean = monster.attributes.hp > 0


  def takeDamage(monster: Monster, damage: Int): Monster = {
    val newHp = max(0, monster.attributes.hp - damage)
    monster.copy(attributes = monster.attributes.copy(hp = newHp))
  }


  def heal(monster: Monster, amount: Int): Monster = {
    monster.copy(attributes = monster.attributes.copy(hp = monster.attributes.hp + amount))
  }


  def describe(monster: Monster): String = {
    s"${monster.name} (Level ${monster.level}) - ${monster.monsterType} from ${monster.originZone}: ${monster.description}"
  }


  def applyBehavior(monster: Monster): Monster = {
    monster.behavior.apply(monster)
    monster
  }


  def createMonster(
                     name: String,
                     level: Int,
                     monsterType: MonsterType,
                     originZone: OriginZone,
                     attributes: MonsterAttributes,
                     goldReward: Int,
                     experienceReward: Int,
                     behavior: MonsterBehavior,
                     description: String
                   ): Monster = {

    require(name.nonEmpty, "Monster name must not be empty")
    require(level > 0, "Level must be positive")
    require(attributes.hp > 0, "Monster must have positive HP")
    require(goldReward >= 0, "Gold reward cannot be negative")
    require(experienceReward >= 0, "Experience reward cannot be negative")
    require(description.nonEmpty, "Description must not be empty")

    Monster(
      name = name,
      level = level,
      monsterType = monsterType,
      originZone = originZone,
      attributes = attributes,
      goldReward = goldReward,
      experienceReward = experienceReward,
      behavior = behavior,
      description = description
    )

  }
