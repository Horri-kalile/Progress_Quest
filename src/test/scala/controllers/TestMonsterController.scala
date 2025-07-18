package test

import controllers.MonsterController
import models.monster.{Aggressive, Monster, MonsterAttributes, MonsterBehavior, MonsterType}
import models.player.{Attributes, Behavior, ClassType, Identity, Player, Race}
import models.world.OriginZone
import org.scalatest.funsuite.AnyFunSuite

class TestMonsterController extends AnyFunSuite {

  val dummyMonster = Monster(
    name = "Slime",
    level = 3,
    monsterType = MonsterType.Beast,
    originZone = OriginZone.Plains,
    attributes = MonsterAttributes(hp = 100, currentHp = 100, attack = 10, defense = 5, weaknessPhysical = 1.2, weaknessMagic = 1.3),
    goldReward = 25,
    experienceReward = 10,
    itemReward = None,
    equipReward = None,
    behavior = Aggressive,
    description = "A weak but annoying slime.",
    berserk = false,
    regenerating = false
  )
  val freshPlayer: Player = Player(
    name = "TestHero",
    identity = Identity(Race.Human, ClassType.Warrior),
    baseAttributes = Attributes(strength = 5, intelligence = 5, constitution = 5, dexterity = 5, wisdom = 5, lucky = 5),
    behaviorType = Behavior.BehaviorType.Heal
  )


  test("Monster takes damage and returns updated instance") {
    val (damaged, explosion) = MonsterController.takeDamage(dummyMonster, 30)
    assert(damaged.attributes.currentHp == 70)
    assert(explosion.isEmpty)
  }

  test("attackPlayer should deal damage or be dodged based on player's dexterity") {
    val monster = dummyMonster.copy(berserk = false)
    val (damage, message, updatedMonster) = MonsterController.attackPlayer(monster, freshPlayer)

    assert(damage >= 0)
    assert(message.contains("attacked") || message.contains("dodged"))
    assert(updatedMonster == monster || updatedMonster.attributes.currentHp <= monster.attributes.currentHp)
  }
  test("attackPlayer should cause self-damage if monster is berserk") {
    val berserkMonster = dummyMonster.copy(berserk = true, attributes = dummyMonster.attributes.copy(currentHp = 50))
    val (damage, message, updatedMonster) = MonsterController.attackPlayer(berserkMonster, freshPlayer)

    assert(damage >= 1)
    assert(message.contains("Berserk"))
    assert(updatedMonster.attributes.currentHp < berserkMonster.attributes.currentHp)
  }
  test("handleRegeneration should heal the monster if regenerating") {
    val regeneratingMonster = dummyMonster.copy(regenerating = true, attributes = dummyMonster.attributes.copy(currentHp = 20))
    val (updatedMonster, maybeMessage) = MonsterController.handleRegeneration(regeneratingMonster)

    assert(updatedMonster.attributes.currentHp >= 20)
    assert(updatedMonster.attributes.currentHp <= updatedMonster.attributes.hp)
    assert(maybeMessage.isDefined)
    assert(maybeMessage.get.contains("recovered"))
  }
  test("handleRegeneration should do nothing if monster is not regenerating") {
    val (updated, msg) = MonsterController.handleRegeneration(dummyMonster)
    assert(updated == dummyMonster)
    assert(msg.isEmpty)
  }


  test("Monster heals properly") {
    val damaged = dummyMonster.copy(attributes = dummyMonster.attributes.copy(currentHp = 50))
    val healed = MonsterController.heal(damaged, 30)
    assert(healed.attributes.currentHp == 80)
  }

  test("Monster cannot heal beyond max HP") {
    val damaged = dummyMonster.copy(attributes = dummyMonster.attributes.copy(currentHp = 95))
    val healed = MonsterController.heal(damaged, 20)
    assert(healed.attributes.currentHp == 100)
  }


  test("describe should return monster description string") {
    val desc = MonsterController.describe(dummyMonster)
    assert(desc.contains(dummyMonster.name))
    assert(desc.contains(dummyMonster.monsterType.toString))
    assert(desc.contains(dummyMonster.originZone.toString))
    assert(desc.contains(dummyMonster.description))
  }
 
  test("getExpReward should return monster's XP reward") {
    val exp = MonsterController.getExpReward(dummyMonster)
    assert(exp == dummyMonster.experienceReward)
  }
  test("getItemReward should return monster's item reward") {
    val item = MonsterController.getItemReward(dummyMonster)
    assert(item == dummyMonster.itemReward)
  }
  test("getEquipReward should return monster's equipment reward") {
    val equip = MonsterController.getEquipReward(dummyMonster)
    assert(equip == dummyMonster.equipReward)
  }
  test("getGoldReward should return monster's gold reward") {
    val gold = MonsterController.getGoldReward(dummyMonster)
    assert(gold == dummyMonster.goldReward)
  }
  test("getMonsterDefenceAndWeakness should return correct tuple") {
    val (defense, physical, magical) = MonsterController.getMonsterDefenceAndWeakness(dummyMonster)
    assert(defense == dummyMonster.attributes.defense)
    assert(physical == dummyMonster.attributes.weaknessPhysical)
    assert(magical == dummyMonster.attributes.weaknessMagic)
  }


  test("getRandomMonsterForZone returns a valid monster for player level and zone") {
    val zone = OriginZone.Forest
    val playerLevel = 5
    val playerLucky = 3

    val monster = MonsterController.getRandomMonsterForZone(playerLevel, playerLucky, zone)

    assert(monster.level >= 1) // Level should never be below 1
    assert(monster.originZone == zone)
    assert(monster.attributes.hp > 0)
    assert(monster.goldReward >= 0)
    assert(monster.experienceReward >= 0)
    assert(monster.name.nonEmpty)
  }

}
