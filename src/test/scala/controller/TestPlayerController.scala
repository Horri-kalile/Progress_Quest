package controller

import models.player.*
import models.player.EquipmentModule.{Equipment, EquipmentSlot}
import models.player.ItemModule.{Item, ItemRarity}
import models.world.OriginZone
import controllers.PlayerController
import org.scalatest.funsuite.AnyFunSuite

class TestPlayerController extends AnyFunSuite:

  val baseAttr: Attributes = Attributes(10, 10, 10, 10, 10, 10)

  def freshPlayer: Player = Player(
    name = "Hero",
    identity = Identity(Race.Human, ClassType.Warrior),
    baseAttributes = baseAttr,
    behaviorType = Behavior.BehaviorType.Heal
  )

  test("Player takes damage and HP is reduced"):
    val player = freshPlayer.withCurrentHp(100)
    val damaged = PlayerController.takeDamage(player, 30)
    assert(damaged.currentHp == 70)

  test("Player heals and HP does not exceed max"):
    val player = freshPlayer.withHp(100).withCurrentHp(80)
    val healed = PlayerController.heal(player, 30)
    assert(healed.currentHp == 100)

  test("gainXP triggers healing for Heal behavior"):
    val player = freshPlayer.withHp(100).withCurrentHp(40).withExp(0).withLevel(1)
    val gained = PlayerController.gainXP(player, 50)
    assert(gained.currentHp > 40)

  test("levelUp increases level and stats"):
    val player = freshPlayer.withHp(100).withMp(50).withLevel(1).withExp(100)
    val leveled = PlayerController.levelUp(player)
    assert(leveled.level == 2)
    assert(leveled.hp > 100)
    assert(leveled.mp > 50)

  test("levelDown decreases level and stats"):
    val player = freshPlayer.withHp(100).withMp(60).withLevel(5)
    val down = PlayerController.levelDown(player)
    assert(down.level == 4)
    assert(down.hp < 100)
    assert(down.mp < 60)

  test("addItem and inventory updates correctly"):
    val item = Item("Potion", 10.0, ItemRarity.Common)
    val updated = PlayerController.addItem(freshPlayer, item, 2)
    assert(updated.inventory(item) == 2)

  test("equipmentOn puts equipment in correct slot"):
    val eq = Equipment("Sword", EquipmentSlot.Weapon, baseAttr, 100)
    val equipped = PlayerController.equipmentOn(freshPlayer, EquipmentSlot.Weapon, eq)
    assert(equipped.equipment(EquipmentSlot.Weapon).contains(eq))

  test("equipmentOff removes equipment from slot"):
    val eq = Equipment("Sword", EquipmentSlot.Weapon, baseAttr, 100)
    val equipped = PlayerController.equipmentOn(freshPlayer, EquipmentSlot.Weapon, eq)
    val removed = PlayerController.equipmentOff(equipped, EquipmentSlot.Weapon)
    assert(removed.equipment(EquipmentSlot.Weapon).isEmpty)

  test("addGold increases player gold"):
    val player = freshPlayer.withGold(50)
    val updated = PlayerController.addGold(player, 30)
    assert(updated.gold == 80)

  test("spendGold reduces gold if enough"):
    val player = freshPlayer.withGold(100)
    val updated = PlayerController.spendGold(player, 40)
    assert(updated.gold == 60)

  test("spendGold does nothing if not enough gold"):
    val player = freshPlayer.withGold(20)
    val updated = PlayerController.spendGold(player, 50)
    assert(updated.gold == 20)

  test("playerInjured halves current HP and MP"):
    val player = freshPlayer.withCurrentHp(80).withCurrentMp(60)
    val injured = PlayerController.playerInjured(player)
    assert(injured.currentHp == 40)
    assert(injured.currentMp == 30)

  test("changeWorld updates player zone"):
    val newZone = OriginZone.Volcano
    val updated = PlayerController.changeWorld(freshPlayer, newZone)
    assert(updated.currentZone == newZone)
