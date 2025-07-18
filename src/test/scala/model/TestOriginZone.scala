package model

import models.world.OriginZone
import org.scalatest.funsuite.AnyFunSuite

class TestOriginZone extends AnyFunSuite:

  test("OriginZone should have all expected zones") {
    val zones = OriginZone.values.toSet
    val expectedZones = Set(
      OriginZone.Forest,
      OriginZone.Swamp,
      OriginZone.Desert,
      OriginZone.Volcano,
      OriginZone.Plains
    )

    assert(zones == expectedZones)
    assert(zones.size == 5)
  }

  test("OriginZone values should be accessible") {
    assert(OriginZone.Forest != null)
    assert(OriginZone.Swamp != null)
    assert(OriginZone.Desert != null)
    assert(OriginZone.Volcano != null)
    assert(OriginZone.Plains != null)
  }

  test("OriginZone should support equality comparison") {
    assert(OriginZone.Forest == OriginZone.Forest)
    assert(OriginZone.Desert == OriginZone.Desert)
    assert(OriginZone.Forest != OriginZone.Desert)
    assert(OriginZone.Swamp != OriginZone.Volcano)
  }

  test("OriginZone should have string representation") {
    assert(OriginZone.Forest.toString == "Forest")
    assert(OriginZone.Swamp.toString == "Swamp")
    assert(OriginZone.Desert.toString == "Desert")
    assert(OriginZone.Volcano.toString == "Volcano")
    assert(OriginZone.Plains.toString == "Plains")
  }

  test("OriginZone.values should return all zones") {
    val allZones = OriginZone.values
    assert(allZones.length == 5)
    assert(allZones.contains(OriginZone.Forest))
    assert(allZones.contains(OriginZone.Swamp))
    assert(allZones.contains(OriginZone.Desert))
    assert(allZones.contains(OriginZone.Volcano))
    assert(allZones.contains(OriginZone.Plains))
  }

  test("OriginZone should support pattern matching") {
    def classifyZone(zone: OriginZone): String = zone match
      case OriginZone.Forest => "Natural"
      case OriginZone.Swamp => "Wetland"
      case OriginZone.Desert => "Arid"
      case OriginZone.Volcano => "Volcanic"
      case OriginZone.Plains => "Grassland"

    assert(classifyZone(OriginZone.Forest) == "Natural")
    assert(classifyZone(OriginZone.Swamp) == "Wetland")
    assert(classifyZone(OriginZone.Desert) == "Arid")
    assert(classifyZone(OriginZone.Volcano) == "Volcanic")
    assert(classifyZone(OriginZone.Plains) == "Grassland")
  }
