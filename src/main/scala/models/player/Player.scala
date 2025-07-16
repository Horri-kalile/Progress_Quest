package models.player

import models.event.Mission
import models.player.Behavior.*
import models.world.OriginZone

/**
 * The main Player trait.
 * Behavior logic is resolved from the BehaviorType.
 * All updates return new Player instances to keep immutability.
 */
trait Player:

  def name: String

  def identity: Identity

  def level: Int

  def exp: Int

  def hp: Int

  def mp: Int

  def currentHp: Int

  def currentMp: Int

  def baseAttributes: Attributes

  def behaviorType: BehaviorType

  def inventory: Map[Item, Int]

  def equipment: Map[EquipmentSlot, Option[Equipment]]

  def skills: List[Skill]

  def missions: List[Mission]

  def gold: Double

  def currentZone: OriginZone

  /** The behavior logic instance resolved from type */
  def behavior: Strategy = summon[Strategy](using behaviorType)

  /** Current stats including equipment bonuses */
  def attributes: Attributes =
    equipment.values.flatten.map(_.statBonus).foldLeft(baseAttributes)(_ + _)

  /** Is the player alive (has HP)? */
  def isAlive: Boolean = currentHp > 0

  /** Inventory item count */
  def inventorySize: Int = inventory.size

  /** Is inventory empty */
  def emptyInventory: Boolean = inventory.isEmpty

  // ===== Update methods =====
  def withHp(hp: Int): Player

  def withMp(mp: Int): Player

  def withLevel(newLevel: Int): Player

  def withExp(newExp: Int): Player

  def withCurrentHp(newHp: Int): Player

  def withCurrentMp(newMp: Int): Player

  def withBaseAttributes(newAttr: Attributes): Player

  def withInventory(newInventory: Map[Item, Int]): Player

  def withEquipment(newEquipment: Map[EquipmentSlot, Option[Equipment]]): Player

  def withSkills(newSkills: List[Skill]): Player

  def withMissions(newMissions: List[Mission]): Player

  def withGold(newGold: Double): Player

  def withCurrentZone(newZone: OriginZone): Player

  def withBehaviorType(newBehaviorType: BehaviorType): Player

  // ===== Player logic methods using update methods =====

  def receiveDamage(amount: Int): Player =
    val dmg = behavior.onDamageTaken(this, amount)
    withCurrentHp((currentHp - dmg).max(0))

  def receiveHealing(amount: Int): Player =
    withCurrentHp((currentHp + amount).min(hp))

  def restore(): Player = withCurrentHp(hp).withCurrentMp(mp)

  def equipmentList: Iterable[Equipment] = equipment.values.flatten

  def activeMissions: List[Mission] = missions.filterNot(_.isCompleted)

  def powerUpAttributes(): Player =
    withBaseAttributes(baseAttributes.incrementRandomAttributes())

  def powerDownAttributes(): Player =
    withBaseAttributes(baseAttributes.decrementRandomAttributes())

/** Companion object for Player trait */
object Player:

  import models.player.Behavior.given

  /** Validate params, throw exception if invalid */
  private def validateParams(
                              name: String,
                              identity: Identity,
                              level: Int,
                              exp: Int,
                              hp: Int,
                              mp: Int,
                              currentHp: Int,
                              currentMp: Int,
                              baseAttributes: Attributes,
                              behaviorType: BehaviorType,
                              inventory: Map[Item, Int],
                              equipment: Map[EquipmentSlot, Option[Equipment]],
                              skills: List[Skill],
                              missions: List[Mission],
                              gold: Double,
                              currentZone: OriginZone
                            ): Unit =
    require(name.nonEmpty, "Player name cannot be empty")
    require(level > 0, "Level must be positive")
    require(exp >= 0, "Experience cannot be negative")
    require(hp > 0, "HP must be positive")
    require(mp >= 0, "MP cannot be negative")
    require(currentHp >= 0 && currentHp <= hp, "Current HP out of range")
    require(currentMp >= 0 && currentMp <= mp, "Current MP out of range")
    require(gold >= 0, "Gold cannot be negative")

  /** Apply factory method to create Player instances */
  def apply(
             name: String,
             identity: Identity,
             level: Int,
             exp: Int,
             hp: Int,
             mp: Int,
             currentHp: Int,
             currentMp: Int,
             baseAttributes: Attributes,
             behaviorType: BehaviorType,
             inventory: Map[Item, Int] = Map.empty,
             equipment: Map[EquipmentSlot, Option[Equipment]] = EquipmentSlot.values.map(_ -> None).toMap,
             skills: List[Skill] = List.empty,
             missions: List[Mission] = List.empty,
             gold: Double,
             currentZone: OriginZone
           ): Player =
    validateParams(name, identity, level, exp, hp, mp, currentHp, currentMp, baseAttributes, behaviorType, inventory, equipment, skills, missions, gold, currentZone)
    val raw = PlayerImpl(name, identity, level, exp, hp, mp, currentHp, currentMp, baseAttributes, behaviorType, inventory, equipment, skills, missions, gold, currentZone)
    raw.behavior.onGameStart(raw) // Apply behavior bonus when it needs

  /** Minimal factory method for convenience, using default level=1, exp=0, gold=0 */
  def apply(
             name: String,
             identity: Identity,
             baseAttributes: Attributes,
             behaviorType: BehaviorType
           ): Player =
    val baseHp = baseAttributes.constitution * 5
    val baseMp = baseAttributes.intelligence * 2
    val level = 1
    val exp = 0
    val gold = 0.0
    val currentZone = OriginZone.Plains

    apply(
      name = name,
      identity = identity,
      level = level,
      exp = exp,
      hp = baseHp,
      mp = baseMp,
      currentHp = baseHp,
      currentMp = baseMp,
      baseAttributes = baseAttributes,
      behaviorType = behaviorType,
      inventory = Map.empty,
      equipment = EquipmentSlot.values.map(_ -> None).toMap,
      skills = List.empty,
      missions = List.empty,
      gold = gold,
      currentZone = currentZone
    )

  /** Extractor for pattern matching */
  def unapply(p: Player): Option[(String, Identity, Int, Int, Int, Int, Int, Int, Attributes, BehaviorType, Map[Item, Int], Map[EquipmentSlot, Option[Equipment]], List[Skill], List[Mission], Double, OriginZone)] =
    Some(
      (
        p.name,
        p.identity,
        p.level,
        p.exp,
        p.hp,
        p.mp,
        p.currentHp,
        p.currentMp,
        p.baseAttributes,
        p.behaviorType,
        p.inventory,
        p.equipment,
        p.skills,
        p.missions,
        p.gold,
        p.currentZone
      )
    )

  /** Private implementation case class hidden from public */
  private case class PlayerImpl(
                                 name: String,
                                 identity: Identity,
                                 level: Int,
                                 exp: Int,
                                 hp: Int,
                                 mp: Int,
                                 currentHp: Int,
                                 currentMp: Int,
                                 baseAttributes: Attributes,
                                 behaviorType: BehaviorType,
                                 inventory: Map[Item, Int],
                                 equipment: Map[EquipmentSlot, Option[Equipment]],
                                 skills: List[Skill],
                                 missions: List[Mission],
                                 gold: Double,
                                 currentZone: OriginZone
                               ) extends Player:

    override def withHp(hp: Int): Player = copy(hp = hp)

    override def withMp(mp: Int): Player = copy(mp = mp)

    override def withLevel(newLevel: Int): Player = copy(level = newLevel)

    override def withExp(newExp: Int): Player = copy(exp = newExp)

    override def withCurrentHp(newHp: Int): Player = copy(currentHp = newHp)

    override def withCurrentMp(newMp: Int): Player = copy(currentMp = newMp)

    override def withBaseAttributes(newAttr: Attributes): Player = copy(baseAttributes = newAttr)

    override def withInventory(newInventory: Map[Item, Int]): Player = copy(inventory = newInventory)

    override def withEquipment(newEquipment: Map[EquipmentSlot, Option[Equipment]]): Player = copy(equipment = newEquipment)

    override def withSkills(newSkills: List[Skill]): Player = copy(skills = newSkills)

    override def withMissions(newMissions: List[Mission]): Player = copy(missions = newMissions)

    override def withGold(newGold: Double): Player = copy(gold = newGold)

    override def withCurrentZone(newZone: OriginZone): Player = copy(currentZone = newZone)

    override def withBehaviorType(newBehaviorType: BehaviorType): Player = copy(behaviorType = newBehaviorType)

    override def behavior: Strategy = behaviorType
