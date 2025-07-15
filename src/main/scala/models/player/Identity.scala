package models.player

import scala.util.Random

enum Race:
  case Human, Elf, Dwarf, Orc, Gnome, Titan, PandaMan, Gundam

enum ClassType:
  case Warrior, Mage, Poisoner, Cleric, Paladin, Assassin, CowBoy

case class Identity(race: Race, classType: ClassType)


object PlayerBonusesApplication:
  /**
   * Applies race and class bonuses to player's HP, MP, and starting skills.
   * - Race affects HP and MP multipliers.
   * - Certain classes get random flat bonuses to HP and MP.
   * - Starting skill is generated based on class type.
   *
   * @param player The player instance to apply bonuses to.
   * @return A new Player instance with updated HP, MP, current HP/MP, and skills.
   */
  def applyRaceAndClassBonuses(player: Player): Player =
    val possibleEquipment = EquipmentFactory.generateRandomEquipment(1.0, player.attributes.lucky, player.level).get
    val (hpMulti, mpMulti, updatedPlayer) = player.identity.race match
      case Race.Human => (1.0, 1.0, player.withEquipment(player.equipment + (possibleEquipment.slot -> Some(possibleEquipment))))
      case Race.Elf => (0.6, 1.4, player.withBaseAttributes(player.baseAttributes.copy(lucky = player.baseAttributes.dexterity + Random.between(1, 5))))
      case Race.Dwarf => (1.5, 0.5, player.withBaseAttributes(player.baseAttributes.copy(constitution = player.baseAttributes.constitution + Random.between(1, 5))))
      case Race.Orc => (1.7, 0.3, player.withBaseAttributes(player.baseAttributes.copy(wisdom = player.baseAttributes.wisdom + Random.between(1, 5))))
      case Race.Gnome => (0.8, 1.2, player.withBaseAttributes(player.baseAttributes.copy(intelligence = player.baseAttributes.intelligence + Random.between(1, 5))))
      case Race.Titan => (1.9, 0.1, player.withBaseAttributes(player.baseAttributes.copy(strength = player.baseAttributes.strength + Random.between(1, 5))))
      case Race.Gundam => (1.3, 0.7, player.withBaseAttributes(player.baseAttributes.copy(constitution = player.baseAttributes.constitution + Random.between(1, 5))))
      case Race.PandaMan => (0.8, 1.2, player.withBaseAttributes(player.baseAttributes.copy(wisdom = player.baseAttributes.wisdom + Random.between(1, 3), strength = player.baseAttributes.strength + Random.between(1, 3))))

    val raceHp = (player.hp * hpMulti).toInt
    val raceMp = (player.mp * mpMulti).toInt
    val classType = player.identity.classType

    val (classHpBonus, classMpBonus) = classType match
      case ClassType.Mage => (0, 0)
      case ClassType.Cleric => (0, 0)
      case ClassType.Paladin => (0, 0)
      case ClassType.Assassin => (0, 0)
      case ClassType.Warrior => (Random.between(10, 31), 0)
      case ClassType.Poisoner => (0, Random.between(5, 16))
      case ClassType.CowBoy => (Random.between(10, 31), Random.between(5, 16))

    val startingSkill = SkillFactory.generateStartingSkill(player.level, classType).toList

    updatedPlayer.withHp(raceHp + classHpBonus)
      .withMp(raceMp + classMpBonus)
      .withCurrentHp(raceHp + classHpBonus)
      .withCurrentMp(raceMp + classMpBonus)
      .withSkills(startingSkill)
