import org.scalatest.funsuite.AnyFunSuite
import models.player.EquipmentModule.EquipmentSlot
import models.player.SkillNameData
import models.event.MissionData
import util.{EquipmentNameLoader, ItemNameLoader, MissionLoader, MonsterLoader, SkillLoader}

class TestReadFile extends AnyFunSuite:

  test("ItemNameLoader should load item names from JSON"):
    val items = ItemNameLoader.loadItemNames()
    assert(items.nonEmpty)

  test("MissionLoader should load missions from JSON"):
    val missions: List[MissionData] = MissionLoader.loadMissions()
    assert(missions.nonEmpty)
    assert(missions.head.name.nonEmpty)

  test("EquipmentNameLoader should load equipment names and map to slots"):
    val equipmentMap = EquipmentNameLoader.loadEquipmentNames()
    assert(equipmentMap.contains(EquipmentSlot.Head))
    assert(equipmentMap(EquipmentSlot.Head).nonEmpty)
    assert(equipmentMap.contains(EquipmentSlot.Jewelry1))
    assert(equipmentMap(EquipmentSlot.Jewelry1).nonEmpty)
    assert(equipmentMap.contains(EquipmentSlot.Jewelry2))

  test("SkillLoader should load skill names from JSON"):
    val skills: SkillNameData = SkillLoader.loadSkillNames()
    assert(skills.magic.nonEmpty)
    assert(skills.healing.nonEmpty)
    assert(skills.physical.nonEmpty)

  test("MonsterLoader should load monsters grouped by category"):
    val monsters: Map[String, List[String]] = MonsterLoader.loadMonsters()
    assert(monsters.contains("Forest"))
    assert(monsters("Forest").contains("Wild Boar"))
