package models.player

import scala.util.Random
import models.player.Race.*
import models.player.ClassType.*

/**
 * Represents available fantasy races.
 */
enum Race:
  case Human, Elf, Dwarf, Orc, Gnome, Titan, PandaMan, Gundam

/**
 * Represents RPG class types.
 */
enum ClassType:
  case Warrior, Mage, Poisoner, Cleric, Paladin, Assassin, CowBoy

/**
 * A character identity consisting of race and class.
 *
 * @param race      the character's race
 * @param classType the character's class
 */
case class Identity(race: Race, classType: ClassType)

/**
 * Responsible for applying race and class bonuses to a Player instance.
 */
object PlayerBonusesApplication:

  /**
   * Applies race- and class-specific bonuses to a player.
   *
   * - Race affects HP/MP scaling and certain stat bonuses.
   * - Some races receive starting equipment or additional stats.
   * - Classes may grant bonus HP/MP and a starting skill.
   *
   * @param player The original Player
   * @return A new Player with bonuses applied
   */
  def applyRaceAndClassBonuses(player: Player): Player =
    val equip = EquipmentFactory.generateRandomEquipment(1.0, player.attributes.lucky, player.level).get

    // Race-based modifications
    val (hpMulti, mpMulti, updatedPlayer) = player.identity.race match
      case Human =>
        (1.0, 1.0, player.withEquipment(player.equipment + (equip.slot -> Some(equip))))
      case Elf =>
        (0.6, 1.4, player.withBaseAttributes(player.baseAttributes.copy(dexterity = player.baseAttributes.dexterity + Random.between(1, 5))))
      case Dwarf =>
        (1.5, 0.5, player.withBaseAttributes(player.baseAttributes.copy(constitution = player.baseAttributes.constitution + Random.between(1, 5))))
      case Orc =>
        (1.7, 0.3, player.withBaseAttributes(player.baseAttributes.copy(wisdom = player.baseAttributes.wisdom + Random.between(1, 5))))
      case Gnome =>
        (0.8, 1.2, player.withBaseAttributes(player.baseAttributes.copy(intelligence = player.baseAttributes.intelligence + Random.between(1, 5))))
      case Titan =>
        (1.6, 0.4, player.withBaseAttributes(player.baseAttributes.copy(strength = player.baseAttributes.strength + Random.between(1, 5))))
      case PandaMan =>
        (0.8, 1.3, player.withBaseAttributes(player.baseAttributes.copy(lucky = player.baseAttributes.lucky + Random.between(1, 5))))
      case Gundam =>
        (1.3, 0.7, player.withBaseAttributes(player.baseAttributes.copy(wisdom = player.baseAttributes.wisdom + Random.between(1, 3), strength = player.baseAttributes.strength + Random.between(1, 3))))

    val raceHp = (player.hp * hpMulti).toInt
    val raceMp = (player.mp * mpMulti).toInt

    // Class-based bonuses
    val (classHpBonus, classMpBonus) = player.identity.classType match
      case Mage | Cleric | Paladin | Assassin => (0, 0)
      case Warrior => (Random.between(10, 31), 0)
      case Poisoner => (0, Random.between(5, 16))
      case CowBoy => (Random.between(5, 25), Random.between(5, 10))

    val startingSkills = SkillFactory.generateStartingSkill(player.level, player.identity.classType).toList

    updatedPlayer
      .withHp(raceHp + classHpBonus)
      .withMp(raceMp + classMpBonus)
      .withCurrentHp(raceHp + classHpBonus)
      .withCurrentMp(raceMp + classMpBonus)
      .withSkills(startingSkills)
