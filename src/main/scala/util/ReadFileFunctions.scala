package util

import upickle.default.*
import scala.io.Source
import scala.util.Using
import models.event.{MissionData, Missions}
import models.monster.MonsterNameData
import models.player.EquipmentModule.EquipmentSlot
import models.player.ItemModule.ItemNames
import models.player.SkillNameData

/**
 * Loads item name data from a JSON file into a list of strings.
 */
object ItemNameLoader:

  /** Reader/writer for serializing/deserializing item names */
  implicit val rw: ReadWriter[ItemNames] = macroRW

  /**
   * Loads item names from the given file path.
   *
   * @param path File path to items JSON (default: assets/items.json)
   * @return List of item names as strings
   */
  def loadItemNames(path: String = "assets/items.json"): List[String] =
    Using.resource(Source.fromFile(path)) { source =>
      val raw = source.mkString
      val parsed = read[ItemNames](raw)
      parsed.items
    }


/**
 * Loads mission data from a JSON file into a list of MissionData entries.
 * Expected JSON format: { "missions": [ {...}, {...} ] }
 */
object MissionLoader:

  /** Implicit upickle readers/writers for deserialization */
  implicit val missionDataRW: ReadWriter[MissionData] = macroRW
  implicit val missionsRW: ReadWriter[Missions] = macroRW

  /**
   * Loads all missions from the specified JSON file.
   *
   * @param path File path to missions JSON (default: assets/missions.json)
   * @return List of MissionData objects
   */
  def loadMissions(path: String = "assets/missions.json"): List[MissionData] =
    Using.resource(Source.fromFile(path)) { source =>
      val raw = source.mkString
      val parsed = read[Missions](raw)
      parsed.missions
    }


/**
 * Loads equipment names grouped by EquipmentSlot from JSON.
 * Special handling for "Jewelry" key: maps to both Jewelry1 and Jewelry2.
 * Expected format: { Head[...], "Jewelry": [...] }
 */
object EquipmentNameLoader:

  /**
   * Loads equipment names and assigns them to matching equipment slots.
   *
   * @param path File path to equipment JSON (default: assets/equipments.json)
   * @return Map from EquipmentSlot to list of item names
   */
  def loadEquipmentNames(path: String = "assets/equipments.json"): Map[EquipmentSlot, List[String]] =
    Using.resource(Source.fromFile(path)) { source =>
      val raw = source.mkString
      val parsed = read[Map[String, List[String]]](raw)
      parsed.flatMap:
        case ("Jewelry", items) =>
          List(EquipmentSlot.Jewelry1, EquipmentSlot.Jewelry2).map(slot => slot -> items)
        case (name, items) =>
          EquipmentSlot.values.find(_.toString == name).map(_ -> items).toList
    }


/**
 * Loads skill name data from a JSON file into a SkillNameData structure.
 * Expected format: { "physicalSkills": [...], "magicSkills": [...], ... }
 */
object SkillLoader:

  /** Implicit reader/writer for SkillNameData */
  implicit val skillDataRw: ReadWriter[SkillNameData] = macroRW

  /**
   * Loads skill names from JSON and parses into a SkillNameData object.
   *
   * @param path File path to skills JSON (default: assets/skills.json)
   * @return SkillNameData with categorized skill names
   */
  def loadSkillNames(path: String = "assets/skills.json"): SkillNameData =
    Using.resource(Source.fromFile(path)) { source =>
      val raw = source.mkString
      read[SkillNameData](raw)
    }


/**
 * Loads monster names grouped by category from JSON.
 * Expected format: { "Forest": [...], "Volcano": [...], ... }
 */
object MonsterLoader:

  /** Implicit reader/writer for MonsterNameData */
  implicit val monsterDataRw: ReadWriter[MonsterNameData] = macroRW

  /**
   * Loads monster name data from a JSON file.
   *
   * @param path File path to monsters JSON (default: assets/monsters.json)
   * @return Map from zone/type to list of monster names
   */
  def loadMonsters(path: String = "assets/monsters.json"): Map[String, List[String]] =
    Using.resource(Source.fromFile(path)) { source =>
      val raw = source.mkString
      read[Map[String, List[String]]](raw)
    }
