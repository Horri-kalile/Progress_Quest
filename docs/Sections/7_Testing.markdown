---
layout: default
title: Testing
nav_order: 7
---

# Testing

Abbiamo adottato un approccio **agile** per il nostro progetto, lavorando in modo flessibile e iterativo per definire i requisiti e adattarci rapidamente ai cambiamenti. Questo metodo ci ha permesso di esplorare e chiarire le funzionalità principali durante i primi backlog.

Successivamente, a partire dal terzo backlog, ci siamo spostati verso un approccio più **incrementale**, concentrandoci sullo sviluppo e rilascio di funzionalità complete a piccoli passi. Per esempio, abbiamo prima implementato un sistema di combattimento base per avere una versione funzionante del gioco, e poi abbiamo continuato a migliorarlo con funzionalità avanzate in incrementi successivi.

### Copertura del Codice e Integrazione CI

La copertura è stata applicata tramite un workflow GitHub Actions che esegue test, utilizzando SBT per pulire, testare e generare report, includendo l'utilizzo di Codecov per tracciare linee e codici non coperte. Questo è stato molto utile per avere una visualizzazione grafica dei test fatti.

### Codice

I test comportamentali sono stati implementati con **ScalaTest**, **AnyFunSpec** e **Matchers**.

---

## Test sul modulo model `GameEvent`

```scala
test("RestoreEvent fully restores the player"):
  val damaged = player.withCurrentHp(5)
  val (restored, _, _) = GameEventFactory.executeEvent(EventType.restore, damaged)
  assert(restored.currentHp == restored.hp)

test("TrainingEvent gives player XP"):
  val (trained, _, _) = GameEventFactory.executeEvent(EventType.training, player)
  assert(trained.exp > player.exp)

test("ChangeWorldEvent updates player zone"):
  val (updated, _, _) = GameEventFactory.executeEvent(EventType.changeWorld, player)
  assert(updated.currentZone != Plains)

test("GameOverEvent sets HP to 0"):
  val (dead, _, _) = GameEventFactory.executeEvent(EventType.gameOver, player)
  assert(dead.currentHp == 0)

test("SellEvent sells items and may power up"):
  val playerWithItem = PlayerController.addItem(player, item).withGold(0)
  val (updated, _, _) = GameEventFactory.executeEvent(EventType.sell, playerWithItem)
  assert(updated.gold >= 0)

test("MissionEvent creates a mission or progresses existing one"):
  val (updated, _, _) = GameEventFactory.executeEvent(EventType.mission, player)
  assert(updated.activeMissions.nonEmpty)

test("FightEvent awards XP and gold"):
  CombatController.setLastMonster(monster.copy(attributes = monster.attributes.copy(currentHp = 0)))
  val (updated, _, _) = GameEventFactory.executeEvent(EventType.fight, player)
  assert(updated.gold > player.gold)
  assert(updated.exp > player.exp)
```
---

## Test sul modulo view `SpecialDialog`

```scala
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
```
---
## Test sul modulo controller `CombatController`

```scala
package controller

import controllers.CombatController
import models.monster.{Aggressive, Monster, MonsterAttributes, MonsterType}
import models.player.{Attributes, Behavior, ClassType, EquipmentModule, Identity, ItemModule, Player, Race}
import models.world.OriginZone
import org.scalatest.funsuite.AnyFunSuite

class TestCombatController extends AnyFunSuite:

  val baseAttributes: Attributes = Attributes(10, 10, 10, 10, 10, 10)

  def freshPlayer: Player = Player(
    name = "Hero",
    identity = Identity(Race.Human, ClassType.Warrior),
    baseAttributes = baseAttributes,
    behaviorType = Behavior.BehaviorType.Heal
  ).withHp(100).withCurrentHp(100).withMp(50).withCurrentMp(50)

  val testMonster: Monster = Monster(
    name = "TestBeast",
    level = 2,
    monsterType = MonsterType.Beast,
    originZone = OriginZone.Forest,
    attributes = MonsterAttributes(hp = 50, currentHp = 50, attack = 5, defense = 2, weaknessPhysical = 1.0,
      weaknessMagic = 1.0),
    goldReward = 10,
    experienceReward = 15,
    itemReward = Some(ItemModule.Item("Herb", 5.0, ItemModule.ItemRarity.Common)),
    equipReward = Some(EquipmentModule.Equipment("Claw", EquipmentModule.EquipmentSlot.Weapon, baseAttributes, 10)),
    behavior = Aggressive,
    description = "A test beast"
  )

  test("simulateFight returns combat log and updates states"):
    val results = CombatController.simulateFight(freshPlayer, testMonster)
    assert(results.nonEmpty)
    assert(results.exists(_._3.contains("attacked")) || results.exists(_._3.contains("defeated")))

  test("handleEquipDrop equips better item"):
    val (updated, msg) = CombatController.handleEquipDrop(freshPlayer, testMonster)
    assert(msg.contains("equipped") || msg.contains("sold"))

  test("handleItemDrop adds item to inventory"):
    val (updated, msg) = CombatController.handleItemDrop(freshPlayer, testMonster)
    assert(msg.contains("found item") || msg.contains("No item"))

  test("setLastMonster and get lastMonster works"):
    CombatController.setLastMonster(testMonster)
    assert(CombatController.lastMonster.contains(testMonster))
```