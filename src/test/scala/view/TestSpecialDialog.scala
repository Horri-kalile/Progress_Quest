package view

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterAll

class TestSpecialDialog extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  override def beforeAll(): Unit =
    SpecialEventDialog.testModeResult = Some(true)
    SpecialEventDialog.isTestMode = true

  override def afterAll(): Unit =
    SpecialEventDialog.testModeResult = None
    SpecialEventDialog.isTestMode = false


  "SpecialEventDialog" should :

    "return Some(true) from blessing/curse dialog" in {
      SpecialEventDialog.showBlessingCurseDialog() shouldBe Some(true)
    }


    "return Some(true) from powerful monster dialog" in {
      SpecialEventDialog.showPowerfulMonsterDialog() shouldBe Some(true)
    }

    "return Some(true) from hidden dungeon dialog" in {
      SpecialEventDialog.showHiddenDungeonDialog() shouldBe Some(true)
    }

    "return Some(true) from villager help dialog" in {
      SpecialEventDialog.showVillagerHelpDialog() shouldBe Some(true)
    }

    "return Some(true) from deadly trap dialog" in {
      SpecialEventDialog.showGameOverTrapDialog() shouldBe Some(true)
    }

    "not crash on dungeon trap dialog" in {
      noException should be thrownBy SpecialEventDialog.showDungeonTrapDialog()
    }

    "not crash on monster defeat info dialog" in {
      noException should be thrownBy SpecialEventDialog.showGameOverMonsterDefeatDialog()
    }

    "not crash on theft dialog" in {
      noException should be thrownBy SpecialEventDialog.showTheftDialog()
    }

    "return Some(false) from blessing/curse dialog" in {
      SpecialEventDialog.testModeResult = Some(false)
      SpecialEventDialog.showBlessingCurseDialog() shouldBe Some(false)
    }

