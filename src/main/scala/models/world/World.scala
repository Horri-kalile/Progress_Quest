package models.world

import models.monster.{Monster, OriginZone}
import scala.util.Random

class World(val currentZone: OriginZone):
  private val MaxBuff = 0.30 // 30% maximum buff

  def applyZoneBuffs(monster: Monster): Unit =
    if monster.originZone == currentZone then
      currentZone match
        case OriginZone.Forest =>
          // Defensive buff (up to 30%)
          val defenseBuff = (1.0 + (Random.nextDouble() * MaxBuff))
          monster.attributes.copy(
            defense = (monster.attributes.defense * defenseBuff).toInt
          )

        case OriginZone.Desert =>
          // Physical damage buff (up to 30%)
          val attackBuff = (1.0 + (Random.nextDouble() * MaxBuff))
          monster.attributes.copy(
            attack = (monster.attributes.attack * attackBuff).toInt
          )

        case OriginZone.Volcano =>
          // HP buff (up to 30%)
          val hpBuff = (1.0 + (Random.nextDouble() * MaxBuff))
          monster.attributes.copy(
            hp = (monster.attributes.hp * hpBuff).toInt
          )

        case OriginZone.Swamp =>
          // Apply buffs to both physical and magic weaknesses (up to 30%)
          val physicalWeaknessBuff = (1.0 + (Random.nextDouble() * MaxBuff))
          val magicWeaknessBuff = (1.0 + (Random.nextDouble() * MaxBuff))
          monster.attributes.copy(
            weaknessPhysical = monster.attributes.weaknessPhysical * physicalWeaknessBuff,
            weaknessMagic = monster.attributes.weaknessMagic * magicWeaknessBuff
          )

        case OriginZone.Plains =>
          // No buffs applied in plains
          ()

  def getZoneDescription: String =
    currentZone match
      case OriginZone.Forest => "A dense forest where monsters have enhanced defense"
      case OriginZone.Desert => "A harsh desert where monsters deal more physical damage"
      case OriginZone.Volcano => "A volcanic region where monsters have increased HP"
      case OriginZone.Swamp => "A mysterious swamp where monsters are more vulnerable to both physical and magical damage"
      case OriginZone.Plains => "Normal plains with no special effects"
