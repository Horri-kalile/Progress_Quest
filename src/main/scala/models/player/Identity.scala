package models.player

import scala.util.Random
import models.player.Race.*
import models.player.ClassType.*
import models.player.EquipmentModule.EquipmentFactory

/** Represents available fantasy races. */
enum Race:
  case Human, Elf, Dwarf, Orc, Gnome, Titan, PandaMan, Gundam

/** Represents some RPG class types. */
enum ClassType:
  case Warrior, Mage, Poisoner, Cleric, Paladin, Assassin, CowBoy

/** A character identity consisting of race and class. */
case class Identity(race: Race, classType: ClassType)

/** Player-identity-related modules and strategies encapsulated inside this module object. */
object PlayerBonusesModule:

  /** Trait representing a race-specific bonus application strategy. */
  trait RaceBonusStrategy:
    /** Applies race-specific bonuses to the given player.
     *
     * @param player
     * The player to modify.
     * @return
     * Tuple of (HP multiplier, MP multiplier, updated player with attribute/equipment changes applied)
     */
    def applyBonuses(player: Player): (Double, Double, Player)

  /** Trait representing a class-specific bonus application strategy. */
  trait ClassBonusStrategy:
    /** Applies class-specific HP and MP bonuses.
     *
     * @param player
     * The player to modify (can be used for context if needed)
     * @return
     * Tuple of (HP bonus, MP bonus)
     */
    def applyBonuses(player: Player): (Int, Int)

  /** Factory object to retrieve the appropriate [[RaceBonusStrategy]] for a given [[Race]]. */
  object RaceBonusStrategyFactory:

    def getStrategy(race: Race): RaceBonusStrategy = race match
      case Human => HumanRaceBonus
      case Elf => ElfRaceBonus
      case Dwarf => DwarfRaceBonus
      case Orc => OrcRaceBonus
      case Gnome => GnomeRaceBonus
      case Titan => TitanRaceBonus
      case PandaMan => PandaManRaceBonus
      case Gundam => GundamRaceBonus

  /** Factory object to retrieve the appropriate [[ClassBonusStrategy]] for a given [[ClassType]]. */
  object ClassBonusStrategyFactory:

    def getStrategy(classType: ClassType): ClassBonusStrategy = classType match
      case Mage | Cleric | Paladin | Assassin => NoClassBonus
      case Warrior => WarriorBonus
      case Poisoner => PoisonerBonus
      case CowBoy => CowBoyBonus

  // PRIVATE IMPLEMENTATIONS BELOW

  private object HumanRaceBonus extends RaceBonusStrategy:
    def applyBonuses(player: Player): (Double, Double, Player) =
      val equip = EquipmentFactory.alwaysDrop(player.level).get
      (1.0, 1.0, player.withEquipment(player.equipment + (equip.slot -> Some(equip))))

  private object ElfRaceBonus extends RaceBonusStrategy:
    def applyBonuses(player: Player): (Double, Double, Player) =
      val updated = player.withBaseAttributes(player.baseAttributes.copy(
        dexterity = player.baseAttributes.dexterity + Random.between(1, 5)
      ))
      (0.6, 1.4, updated)

  private object DwarfRaceBonus extends RaceBonusStrategy:
    def applyBonuses(player: Player): (Double, Double, Player) =
      val updated = player.withBaseAttributes(player.baseAttributes.copy(
        constitution = player.baseAttributes.constitution + Random.between(1, 5)
      ))
      (1.5, 0.5, updated)

  private object OrcRaceBonus extends RaceBonusStrategy:
    def applyBonuses(player: Player): (Double, Double, Player) =
      val updated = player.withBaseAttributes(player.baseAttributes.copy(
        wisdom = player.baseAttributes.wisdom + Random.between(1, 5)
      ))
      (1.7, 0.3, updated)

  private object GnomeRaceBonus extends RaceBonusStrategy:
    def applyBonuses(player: Player): (Double, Double, Player) =
      val updated = player.withBaseAttributes(player.baseAttributes.copy(
        intelligence = player.baseAttributes.intelligence + Random.between(1, 5)
      ))
      (0.8, 1.2, updated)

  private object TitanRaceBonus extends RaceBonusStrategy:
    def applyBonuses(player: Player): (Double, Double, Player) =
      val updated = player.withBaseAttributes(player.baseAttributes.copy(
        strength = player.baseAttributes.strength + Random.between(1, 5)
      ))
      (1.6, 0.4, updated)

  private object PandaManRaceBonus extends RaceBonusStrategy:
    def applyBonuses(player: Player): (Double, Double, Player) =
      val updated = player.withBaseAttributes(player.baseAttributes.copy(
        lucky = player.baseAttributes.lucky + Random.between(1, 5)
      ))
      (0.8, 1.3, updated)

  private object GundamRaceBonus extends RaceBonusStrategy:
    def applyBonuses(player: Player): (Double, Double, Player) =
      val updated = player.withBaseAttributes(player.baseAttributes.copy(
        wisdom = player.baseAttributes.wisdom + Random.between(1, 3),
        strength = player.baseAttributes.strength + Random.between(1, 3)
      ))
      (1.3, 0.7, updated)

  private object NoClassBonus extends ClassBonusStrategy:
    def applyBonuses(player: Player): (Int, Int) = (0, 0)

  private object WarriorBonus extends ClassBonusStrategy:
    def applyBonuses(player: Player): (Int, Int) = (Random.between(10, 31), 0)

  private object PoisonerBonus extends ClassBonusStrategy:
    def applyBonuses(player: Player): (Int, Int) = (0, Random.between(5, 16))

  private object CowBoyBonus extends ClassBonusStrategy:
    def applyBonuses(player: Player): (Int, Int) = (Random.between(5, 25), Random.between(5, 10))


/** Public-facing object responsible for applying race and class bonuses to a player. */
object PlayerBonusesApplication:

  import PlayerBonusesModule.*

  /** Applies race- and class-specific bonuses to the given player.
   *
   * Race affects HP/MP multipliers and base attributes.
   * Class grants fixed HP and/or MP bonuses.
   * Also adds starting skills based on class.
   *
   * @param player
   * The original player
   * @return
   * A new player instance with all bonuses applied
   */
  def applyRaceAndClassBonuses(player: Player): Player =
    val raceStrategy = RaceBonusStrategyFactory.getStrategy(player.identity.race)
    val (hpMultiplier, mpMultiplier, playerWithRaceBonuses) = raceStrategy.applyBonuses(player)

    val classStrategy = ClassBonusStrategyFactory.getStrategy(player.identity.classType)
    val (classHpBonus, classMpBonus) = classStrategy.applyBonuses(playerWithRaceBonuses)

    val raceHp = (player.hp * hpMultiplier).toInt
    val raceMp = (player.mp * mpMultiplier).toInt

    val startingSkills = SkillFactory.generateStartingSkill(player.level, player.identity.classType).toList

    playerWithRaceBonuses
      .withHp(raceHp + classHpBonus)
      .withMp(raceMp + classMpBonus)
      .withCurrentHp(raceHp + classHpBonus)
      .withCurrentMp(raceMp + classMpBonus)
      .withSkills(startingSkills)
