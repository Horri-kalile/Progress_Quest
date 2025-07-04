package models.player

import scala.util.Random

enum Race:
  case Human, Elf, Dwarf, Orc, Gnome, Titan, PandaMan, Gundam

enum ClassType:
  case Warrior, Mage, Poisoner, Cleric, Paladin, Assassin, CowBoy

case class Identity(race: Race, classType: ClassType)


object PlayerBonusesApplication:
  def applyRaceAndClassBonuses(player: Player): Player =
    val (hpMulti, mpMulti) = player.identity.race match
      case Race.Human => (1.0, 1.0)
      case Race.Elf => (0.5, 1.5)
      case Race.Dwarf => (1.5, 0.5)
      case Race.Orc => (1.7, 0.3)
      case Race.Gnome => (0.8, 1.2)
      case Race.Titan => (2.0, 0.4)
      case Race.PandaMan => (1.2, 0.8)
      case Race.Gundam => (1.3, 1.3)

    val raceHp = (player.hp * hpMulti).toInt
    val raceMp = (player.mp * mpMulti).toInt
    val classType = player.identity.classType

    val (classHpBonus, classMpBonus) = classType match
      case ClassType.Mage => (0, 0)
      case ClassType.Cleric => (0, 0)
      case ClassType.Paladin => (0, 0)
      case ClassType.Assassin => (0, 0)
      case _ => (Random.between(10, 31), Random.between(5, 16))

    val skillOpt = SkillFactory.generateStartingSkill(classType)

    player.copy(
      hp = raceHp + classHpBonus,
      mp = raceMp + classMpBonus,
      currentHp = raceHp + classHpBonus,
      currentMp = raceMp + classMpBonus,
      skills = skillOpt.toList
    )