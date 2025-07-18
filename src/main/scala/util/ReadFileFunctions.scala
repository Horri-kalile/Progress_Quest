package util

import upickle.default.*

import scala.io.Source
import scala.util.Using
import models.event.{MissionData, Missions}
import models.monster.MonsterNameData
import models.player.EquipmentModule.EquipmentSlot
import models.player.ItemModule.ItemNames
import models.player.{SkillNameData}

object ItemNameLoader:
  implicit val rw: ReadWriter[ItemNames] = macroRW

  def loadItemNames(path: String = "assets/items.json"): List[String] =
    Using.resource(Source.fromFile(path)) { source =>
      val raw = source.mkString
      val parsed = read[ItemNames](raw)
      parsed.items
    }


object MissionLoader:
  implicit val missionDataRW: ReadWriter[MissionData] = macroRW
  implicit val missionsRW: ReadWriter[Missions] = macroRW

  def loadMissions(path: String = "assets/missions.json"): List[MissionData] =
    Using.resource(Source.fromFile(path)) { source =>
      val raw = source.mkString
      val parsed = read[Missions](raw)
      parsed.missions
    }


object EquipmentNameLoader:
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


object SkillLoader:
  implicit val skillDataRw: ReadWriter[SkillNameData] = macroRW

  def loadSkillNames(path: String = "assets/skills.json"): SkillNameData =
    Using.resource(Source.fromFile(path)) { source =>
      val raw = source.mkString
      read[SkillNameData](raw)
    }

object MonsterLoader:
  implicit val monsterDataRw: ReadWriter[MonsterNameData] = macroRW

  def loadMonsters(path: String = "assets/monsters.json"): Map[String, List[String]] =
    Using.resource(Source.fromFile(path)) { source =>
      val raw = source.mkString
      read[Map[String, List[String]]](raw)
    }