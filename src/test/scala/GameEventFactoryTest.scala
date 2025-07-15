
import controllers.{CombatController, MonsterController, PlayerController}
import models.event.GameEventModule.*
import models.player.{Attributes, ClassType, Equipment, EquipmentFactory, Identity, Item, ItemFactory, Player, Race}
import models.monster.Monster
import models.monster.OriginZone.Plains
import models.player.Behavior.BehaviorType
import org.scalatest.funsuite.AnyFunSuite


class GameEventFactoryTest extends AnyFunSuite:
  val identity: Identity = Identity(Race.Human, ClassType.Warrior)
  val attributes: Attributes = Attributes(10, 10, 10, 10, 10, 10)
  val player: Player = Player("Healer", identity, attributes, BehaviorType.Aggressive)
  val monster: Monster = MonsterController.getRandomMonsterForZone(player.level, player.attributes.lucky, player.currentZone)
  val item: Item = ItemFactory.randomItem(player.attributes.lucky)
  val equipment: Equipment = EquipmentFactory.generateRandomEquipment(1.0, player.attributes.lucky, player.level).get

  test("RestoreEvent fully restores the player"):
    val damaged = player.withCurrentHp(5)
    val (restored, msgs, _) = GameEventFactory.executeEvent(EventType.restore, damaged)
    assert(restored.currentHp == restored.hp)


  test("TrainingEvent gives player XP"):
    val (trained, msgs, _) = GameEventFactory.executeEvent(EventType.training, player)
    assert(trained.exp > 0)


  test("ChangeWorldEvent updates player zone"):
    val (updated, msgs, _) = GameEventFactory.executeEvent(EventType.changeWorld, player)
    assert(updated.currentZone != Plains)


  test("GameOverEvent sets HP to 0"):
    val (dead, msgs, _) = GameEventFactory.executeEvent(EventType.gameOver, player)
    assert(dead.currentHp == 0)


  test("SellEvent sells items and may power up"):
    val updatedPlayer = PlayerController.addItem(player, item).withGold(0)
    val (updated, msgs, _) = GameEventFactory.executeEvent(EventType.sell, player)
    assert(updated.gold >= 0)


  test("MissionEvent creates a mission or progresses existing one"):
    val (updated, msgs, _) = GameEventFactory.executeEvent(EventType.mission, player)
    assert(updated.activeMissions.nonEmpty)


  test("FightEvent awards XP and gold"):
    CombatController.setLastMonster(monster)
    val (updated, msgs, _) = GameEventFactory.executeEvent(EventType.fight, player)
    assert(updated.gold > 0)
    assert(updated.exp > 0)

  test("Case 0 - Blessing or curse: level changes"):
    val leveledUpPlayer = player.withLevel(10)
    val (updated, messages, _) = GameEventFactory.testSpecialCase(leveledUpPlayer, 0)
    assert(updated.level != player.level)

  test("Case 1 - Loot equipment: equip or sell depending on value"):
    val playerWithEquip = PlayerController.equipmentOn(player, equipment.slot, equipment)
    val (updated, messages, _) = GameEventFactory.testSpecialCase(playerWithEquip, 1)
    assert(messages.exists(msg => msg.contains("equipped") || msg.contains("sold") || msg.contains("but")))
    assert(updated.gold >= 0)

  test("Case 2 - Game over by monster"):
    val (dead, messages, _) = GameEventFactory.testSpecialCase(player, 2)
    assert(dead.currentHp == 0)
    assert(messages.exists(_.toLowerCase.contains("game over")))

  test("Case 3 - Found a new item"):
    val (updated, messages, _) = GameEventFactory.testSpecialCase(player, 3)
    assert(updated.inventory.nonEmpty)
    assert(messages.exists(_.toLowerCase.contains("item")))

  test("Case 4 - Trap: HP and MP halved"):
    val (afterTrap, messages, _) = GameEventFactory.testSpecialCase(player, 4)
    assert(afterTrap.currentHp <= player.currentHp)
    assert(afterTrap.currentMp <= player.currentMp)
    assert(messages.exists(_.toLowerCase.contains("trap")))

  test("Case 5 - Help villagers and gain EXP"):
    val (updated, messages, _) = GameEventFactory.testSpecialCase(player, 5)
    assert(updated.exp > player.exp || updated.level > player.level)
    assert(messages.exists(_.toLowerCase.contains("villagers")))

  test("Case 6 - Death by trap"):
    val (dead, messages, _) = GameEventFactory.testSpecialCase(player, 6)
    assert(dead.currentHp == 0)
    assert(messages.exists(_.toLowerCase.contains("trap")))
    assert(messages.exists(_.toLowerCase.contains("game over")))

  test("Case 7 - Item stolen"):
    val withItem = PlayerController.addItem(player, item)
    println(withItem)
    val (afterTheft, messages, _) = GameEventFactory.testSpecialCase(withItem, 7)
    assert(afterTheft.inventory.size <= withItem.inventory.size - 1)
    assert(messages.exists(_.toLowerCase.contains("stolen")))
