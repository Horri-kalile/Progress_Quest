import controllers.{CombatController, MonsterController, PlayerController}
import models.event.GameEventModule.*
import models.player.{Attributes, ClassType, Identity, Item, ItemFactory, Player, Race}
import models.monster.Monster
import models.world.OriginZone.Plains
import models.player.Behavior.BehaviorType
import models.player.EquipmentModule.{Equipment, EquipmentFactory, EquipmentSlot}
import org.scalatest.funsuite.AnyFunSuite

class TestGameEvent extends AnyFunSuite:
  val identity: Identity = Identity(Race.Human, ClassType.Warrior)
  val attributes: Attributes = Attributes(10, 10, 10, 10, 10, 10)
  val player: Player = Player("Healer", identity, attributes, BehaviorType.Aggressive)
  val monster: Monster = MonsterController.getRandomMonsterForZone(player.level, player.attributes.lucky, player.currentZone)
  val item: Item = ItemFactory.randomItem(player.attributes.lucky)
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
    val (restored, msgs, _) = GameEventFactory.executeEvent(EventType.restore, damaged)
    assert(restored.currentHp == restored.hp)

  test("TrainingEvent gives player XP"):
    val (trained, msgs, _) = GameEventFactory.executeEvent(EventType.training, player)
    assert(trained.exp > player.exp)

  test("ChangeWorldEvent updates player zone"):
    val (updated, msgs, _) = GameEventFactory.executeEvent(EventType.changeWorld, player)
    assert(updated.currentZone != Plains)

  test("GameOverEvent sets HP to 0"):
    val (dead, msgs, _) = GameEventFactory.executeEvent(EventType.gameOver, player)
    assert(dead.currentHp == 0)

  test("SellEvent sells items and may power up"):
    val playerWithItem = PlayerController.addItem(player, item).withGold(0)
    val (updated, msgs, _) = GameEventFactory.executeEvent(EventType.sell, playerWithItem)
    assert(updated.gold >= 0)

  test("MissionEvent creates a mission or progresses existing one"):
    val (updated, msgs, _) = GameEventFactory.executeEvent(EventType.mission, player)
    assert(updated.activeMissions.nonEmpty)

  test("FightEvent awards XP and gold"):
    CombatController.setLastMonster(monster.copy(attributes = monster.attributes.copy(currentHp = 0)))
    val (updated, msgs, _) = GameEventFactory.executeEvent(EventType.fight, player)
    assert(updated.gold > player.gold)
    assert(updated.exp > player.exp)

  test("Case 0 - Blessing or curse: level changes"):
    val leveledUpPlayer = player.withLevel(10)
    val (updated, messages, _) = GameEventFactory.testSpecialCase(leveledUpPlayer, 0)
    assert(updated.level != leveledUpPlayer.level)

  test("Case 1 - Loot equipment: equip, sell, or no loot"):
    var startingPlayer = player
    for _ <- 1 to 100 do
      startingPlayer = equipRandomGear(startingPlayer)

    val startingGold = startingPlayer.gold

    val (updatedPlayer, messages, _) = GameEventFactory.testSpecialCase(startingPlayer, 1)

    val lootMessage = messages.find(_.toLowerCase.contains("looted"))
    val equipMessage = messages.find(_.toLowerCase.contains("equipped"))
    val soldMessage = messages.find(_.toLowerCase.contains("sold"))
    val noLootMessage = messages.find(_.toLowerCase.contains("no loot"))
    println(startingPlayer.gold)
    println(updatedPlayer.gold)
    if noLootMessage.isDefined then
      // Case 1: No loot was found
      assert(updatedPlayer == startingPlayer, "Player should remain unchanged if no loot was found.")
    else
      // Case 2: Equipment was generated
      assert(lootMessage.isDefined, "Expected loot message when equipment is found.")

      if equipMessage.isDefined then
        assert(updatedPlayer.equipment.values.flatten.nonEmpty, "Expected at least one equipped item.")

      if soldMessage.isDefined then
        assert(updatedPlayer.gold > startingGold, "Expected gold to increase if equipment was sold.")


  test("Case 2 - Game over by monster"):
    val (updated, messages, _) = GameEventFactory.testSpecialCase(player, 2)
    assert(updated.exp == 0 || updated.gold >= player.gold)

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

  test("Case 6 - Death by trap or increased hp or mp"):
    val (updated, messages, _) = GameEventFactory.testSpecialCase(player, 6)
    assert(updated.currentHp == 0 || updated.hp > player.hp || messages.exists(_.toLowerCase.contains("backed away.")))
    assert(messages.exists(_.toLowerCase.contains("trap")))

  test("Case 7 - Item stolen"):
    val withItem = PlayerController.addItem(player, item)
    val (afterTheft, messages, _) = GameEventFactory.testSpecialCase(withItem, 7)
    assert(afterTheft.inventory.size <= withItem.inventory.size)
    assert(messages.exists(_.toLowerCase.contains("stolen")))
