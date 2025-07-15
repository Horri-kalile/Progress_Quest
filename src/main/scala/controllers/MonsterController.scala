package controllers

import models.monster._
import scala.math.max

object MonsterController:


  def isAlive(monster: Monster): Boolean = monster.attributes.hp > 0


  def takeDamage(monster: Monster, damage: Int): Monster =
    val newHp = max(0, monster.attributes.hp - damage)
    monster.copy(attributes = monster.attributes.copy(hp = newHp))


  def heal(monster: Monster, amount: Int): Monster =
    monster.copy(attributes = monster.attributes.copy(hp = monster.attributes.hp + amount))


  def describe(monster: Monster): String =
    s"${monster.name} (Level ${monster.level}) - ${monster.monsterType} from ${monster.originZone}: ${monster.description}"


  def applyBehavior(monster: Monster): Monster =
    monster.behavior.apply(monster)
    monster


  def createMonsterForPlayerZone(playerLevel: Int, zone: OriginZone): Monster =
    MonstersFactory.randomMonsterForZone(zone, playerLevel)

