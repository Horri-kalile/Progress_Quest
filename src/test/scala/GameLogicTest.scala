import org.scalatest.funsuite.AnyFunSuite
import models.player.*
import models.monster.*
import models.event.*
import controllers.{MissionController, PlayerController}
import models.player.Behavior.BehaviorType

class GameLogicTest extends AnyFunSuite:

  val identity: Identity = Identity(Race.Human, ClassType.Warrior)
  val attributes: Attributes = Attributes(10, 10, 10, 10, 10, 10)

  test("Player behavior: Heal behavior restores HP after battle") {
    val player = Player("Healer", identity, attributes, BehaviorType.Heal)
      .withCurrentHp(5)
    val afterBattle = PlayerController.gainXP(player, 20)
    assert(afterBattle.currentHp > 5)
  }

  test("Player attacks Monster and deals damage") {
    val player = Player("Attacker", identity, attributes, BehaviorType.Aggressive)
    val monster = MonstersFactory.randomMonsterForZone(player.currentZone, player.level, player.attributes.lucky)
    val damage = PlayerController.calculatePlayerAttack(player, monster)
    val monsterAfter = monster.receiveDamage(damage)
    assert(monsterAfter.attributes.currentHp < monster.attributes.currentHp)
  }

  test("Player takes damage and HP is reduced") {
    val player = Player("Tank", identity, attributes, Behavior.BehaviorType.Defensive)
    val damaged = PlayerController.takeDamage(player, 10)
    assert(damaged.currentHp < player.currentHp)
  }

  test("Player heals and HP increases") {
    val player = Player("Cleric", identity, attributes, Behavior.BehaviorType.Aggressive)
      .withCurrentHp(5)
    val healed = PlayerController.heal(player, 20)
    assert(healed.currentHp > 5)
  }

  test("Player adds item to inventory") {
    val player = Player("Collector", identity, attributes, Behavior.BehaviorType.Aggressive)
    val item = ItemFactory.randomItem(attributes.lucky)
    val updated = PlayerController.addItem(player, item, 2)
    assert(updated.inventory.getOrElse(item, 0) == 2)
  }

  test("Player equips an equipment") {
    val player = Player("Warrior", identity, attributes, Behavior.BehaviorType.Aggressive)
    val item = EquipmentFactory.generateRandomEquipment(1.0, attributes.lucky, player.level).get
    val equipped = PlayerController.equipmentOn(player, item.slot, item)
    assert(equipped.equipment(item.slot).contains(item))
  }

  test("Player state after level up includes reset HP and MP") {
    val player = Player("Leveler", identity, attributes, Behavior.BehaviorType.Aggressive)
    val leveled = PlayerController.gainXP(player, 500)
    assert(leveled.level > player.level)
    assert(leveled.currentHp == leveled.hp)
    assert(leveled.currentMp == leveled.mp)
  }

  test("Player state after a battle with Heal behavior") {
    val player = Player("BattleHealer", identity, attributes, Behavior.BehaviorType.Heal)
      .withCurrentHp(10)
    val postBattle = PlayerController.gainXP(player, 50)
    assert(postBattle.currentHp > 10)
  }

  test("Mission progresses") {
    val mission = MissionFactory.randomMission(10, 1)
    val player = Player("Missioner", identity, attributes, Behavior.BehaviorType.Aggressive)
      .withMissions(List(mission))
    val progressed = MissionController.progressMission(player, mission)
    if mission.goal == 1 then
      assert(progressed.missions.forall(_.id != mission.id)) // Removed if completed
    else
      assert(progressed.missions.exists(_.id == mission.id))
  }

  test("Player after event power up") {
    val player = Player("Strong", identity, attributes, Behavior.BehaviorType.Defensive)
    val updated = PlayerController.levelUp(player)
    assert(updated.level != player.level)
  }
