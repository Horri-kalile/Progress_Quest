package models.world

import models.monster.{Monster, OriginZone}
import util.GameConfig

import scala.util.Random

object World:


  /** Applies buffs if monster is in its home zone */
  def applyZoneBuffs(monster: Monster, currentZone: OriginZone): Monster =
    if monster.originZone != currentZone then return monster

    val attrs = monster.attributes

    val buffedAttributes = currentZone match
      case OriginZone.Forest =>
        val defenseBuff = 1.0 + Random.nextDouble() * GameConfig.maxBuffByZone
        attrs.copy(defense = (attrs.defense * defenseBuff).toInt)

      case OriginZone.Desert =>
        val attackBuff = 1.0 + Random.nextDouble() * GameConfig.maxBuffByZone
        attrs.copy(attack = (attrs.attack * attackBuff).toInt)

      case OriginZone.Volcano =>
        val hpBuff = 1.0 + Random.nextDouble() * GameConfig.maxBuffByZone
        val newHp = (attrs.hp * hpBuff).toInt
        attrs.copy(currentHp = newHp, hp = newHp)

      case OriginZone.Swamp =>
        val physicalBuff = 1.0 + Random.nextDouble() * GameConfig.maxBuffByZone
        val magicBuff = 1.0 + Random.nextDouble() * GameConfig.maxBuffByZone
        attrs.copy(
          weaknessPhysical = attrs.weaknessPhysical * physicalBuff,
          weaknessMagic = attrs.weaknessMagic * magicBuff
        )

      case OriginZone.Plains =>
        attrs // No buffs

    monster.copy(attributes = buffedAttributes)

  /** Provides description of zone effects */
  def getZoneDescription(zone: OriginZone): String = zone match
    case OriginZone.Forest => "A dense forest where monsters have enhanced defense"
    case OriginZone.Desert => "A harsh desert where monsters deal more physical damage"
    case OriginZone.Volcano => "A volcanic region where monsters have increased HP"
    case OriginZone.Swamp => "A mysterious swamp where monsters are more vulnerable to both physical and magical damage"
    case OriginZone.Plains => "Normal plains with no special effects"
