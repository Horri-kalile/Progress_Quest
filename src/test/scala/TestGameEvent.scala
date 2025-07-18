import controllers.{CombatController, MonsterController, PlayerController}
import models.event.GameEventModule.*
import models.player.{Attributes, ClassType, Identity, Player, Race}
import models.monster.Monster
import models.world.OriginZone.Plains
import models.player.Behavior.BehaviorType
import models.player.EquipmentModule.{Equipment, EquipmentFactory, EquipmentSlot}
import models.player.ItemModule.{Item, ItemFactory}
import org.scalatest.funsuite.AnyFunSuite

class TestGameEvent extends AnyFunSuite:
  val identity: Identity = Identity(Race.Human, ClassType.Warrior)
  val attributes: Attributes = Attributes(10, 10, 10, 10, 10, 10)
  val player: Player = Player("Healer", identity, attributes, BehaviorType.Aggressive)
  val monster: Monster = MonsterController.getRandomMonsterForZone(player.level, player.attributes.lucky, player.currentZone)
  val item: Item = ItemFactory.alwaysCreate().createRandomItem(player.attributes.lucky).get
  val equipment: Equipment = EquipmentFactory.alwaysDrop(player.level).get

  private def equipRandomGear(player: Player): Player =
    EquipmentSlot.values.foldLeft(player)((p, slot) =>
      EquipmentFactory.alwaysDrop(p.level) match
        case Some(equipment) if equipment.slot == slot =>
          PlayerController.equipmentOn(p, slot, equipment)
        case _ => p
    )

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

  test("MagicEvent grants new skill or upgrades existing one"):
    val (updated, messages, _) = GameEventFactory.executeEvent(EventType.magic, player)
    assert(messages.exists(_.toLowerCase.contains("skill")))

  test("CraftEvent equips or upgrades equipment"):
    val (updated, messages, _) = GameEventFactory.executeEvent(EventType.craft, player)
    assert(messages.exists(msg => msg.toLowerCase.contains("equipped") || msg.toLowerCase.contains("upgraded") || msg.toLowerCase.contains("no equipment")))

  // Special Events
  test("Special Case 0 - Blessing or curse"):
    val (updated, messages, _) = GameEventFactory.testSpecialCase(player, 0)
    assert(updated.level != player.level || messages.exists(_.toLowerCase.contains("ignored")))

  test("Special Case 1 - Loot equipment"):
    val starting = equipRandomGear(player)
    val (updated, messages, _) = GameEventFactory.testSpecialCase(starting, 1)
    val keywords = List("loot", "equipped", "sold", "no loot")
    assert(messages.exists(msg => keywords.exists(kw => msg.toLowerCase.contains(kw))))


  test("Special Case 2 - Fight deadly monster"):
    val (updated, messages, _) = GameEventFactory.testSpecialCase(player, 2)
    assert(updated.exp == 0 || updated.gold >= player.gold || messages.exists(_.toLowerCase.contains("escaped")))

  test("Special Case 3 - Hidden dungeon item"):
    val (updated, messages, _) = GameEventFactory.testSpecialCase(player, 3)
    assert(updated.inventory.nonEmpty || messages.exists(_.toLowerCase.contains("ignored")))

  test("Special Case 4 - Trap"):
    val (updated, messages, _) = GameEventFactory.testSpecialCase(player, 4)
    assert(updated.currentHp <= player.currentHp || messages.exists(_.toLowerCase.contains("escaped")))

  test("Special Case 5 - Help villagers"):
    val (updated, messages, _) = GameEventFactory.testSpecialCase(player, 5)
    assert(updated.exp > player.exp || messages.exists(_.toLowerCase.contains("ignored")))

  test("Special Case 6 - Deadly trap or buff"):
    val (updated, messages, _) = GameEventFactory.testSpecialCase(player, 6)
    assert(
      updated.currentHp == 0 ||
        updated.hp > player.hp ||
        messages.exists(_.toLowerCase.contains("backed away"))
    )

  test("Special Case 7 - Theft"):
    val withItem = PlayerController.addItem(player, item)
    val (after, messages, _) = GameEventFactory.testSpecialCase(withItem, 7)
    assert(after.inventory.size <= withItem.inventory.size)
    assert(messages.exists(_.toLowerCase.contains("stolen")))
