package view

import models.player.*
import org.scalatest.funsuite.AnyFunSuite

class TestPlayerGenerationUi extends AnyFunSuite:

  test("Race descriptions should match expected text"):
    assert(PlayerGenerationUi.getRaceDescription(Race.Human).contains("Balanced"))
    assert(PlayerGenerationUi.getRaceDescription(Race.Gnome).contains("intelligence"))
    assert(PlayerGenerationUi.getRaceDescription(Race.Gundam).contains("Mechanical"))
    assert(!PlayerGenerationUi.getRaceDescription(Race.Elf).contains("Balanced"))

  test("Class descriptions should match expected text"):
    assert(PlayerGenerationUi.getClassDescription(ClassType.Warrior).contains("more Hp"))
    assert(PlayerGenerationUi.getClassDescription(ClassType.Mage).contains("magic"))


