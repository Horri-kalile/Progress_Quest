package controller

import controllers.MonsterController
import models.monster.*
import models.player.*
import models.world.OriginZone
import org.scalatest.funsuite.AnyFunSuite

class TestMonsterController extends AnyFunSuite:

  val baseAttributes: MonsterAttributes = MonsterAttributes(100, 100, 20, 10, 1.2, 1.5)
  val dummyMonster: Monster = Monster(
    name = "Orc",
    level = 5,
    monsterType = MonsterType.Humanoid,
    originZone = OriginZone.Desert,
    attributes = baseAttributes,
    goldReward = 50,
    experienceReward = 30,
    itemReward = None,
    equipReward = None,
    behavior = Defensive,
    description = "A fierce mountain orc.",
    berserk = false,
    regenerating = false
  )

  val player: Player = Player(
    name = "Hero",
    identity = Identity(Race.Human, ClassType.Warrior),
    baseAttributes = Attributes(10, 10, 10, 10, 10, 10),
    behaviorType = Behavior.BehaviorType.Heal
  )

  test("takeDamage reduces monster HP and triggers explosion if explosive and dead"):
    val explosive = dummyMonster.copy(behavior = Explosive, attributes = baseAttributes.copy(currentHp = 10))
    val (damaged, explosion) = MonsterController.takeDamage(explosive, 15)
    assert(damaged.attributes.currentHp == 0)
    assert(explosion.contains(explosive.explosionDamage))

  test("attackPlayer returns damage, message, and handles dodge and berserk"):
    val (damage, message, updated) = MonsterController.attackPlayer(dummyMonster, player)
    assert(damage >= 0)
    assert(message.nonEmpty)
    assert(updated.name == dummyMonster.name)

  test("berserk monster damages itself"):
    val berserk = dummyMonster.copy(berserk = true)
    val (dmg, msg, updated) = MonsterController.attackPlayer(berserk, player)
    assert(msg.contains("Berserk"))
    assert(updated.attributes.currentHp < berserk.attributes.currentHp)

  test("handleRegeneration heals monster if active"):
    val regen = dummyMonster.copy(regenerating = true, attributes = baseAttributes.copy(currentHp = 50))
    val (after, msg) = MonsterController.handleRegeneration(regen)
    assert(after.attributes.currentHp > 50)
    assert(msg.exists(_.contains("recovered")))

  test("heal does not exceed max HP"):
    val damaged = dummyMonster.copy(attributes = baseAttributes.copy(currentHp = 95))
    val healed = MonsterController.heal(damaged, 10)
    assert(healed.attributes.currentHp == 100)

  test("describe returns full string"):
    val description = MonsterController.describe(dummyMonster)
    assert(description.contains(dummyMonster.name))
    assert(description.contains(dummyMonster.originZone.toString))

  test("getMonsterDefenceAndWeakness returns correct tuple"): 
    val (deff, phys, mag) = MonsterController.getMonsterDefenceAndWeakness(dummyMonster)
    assert(deff == baseAttributes.defense)
    assert(phys == baseAttributes.weaknessPhysical)
    assert(mag == baseAttributes.weaknessMagic)

  test("getRandomMonsterForZone returns expected result"):
    val monster = MonsterController.getRandomMonsterForZone(3, 5, OriginZone.Forest)
    assert(monster.originZone == OriginZone.Forest)
    assert(monster.level > 0)
    assert(monster.attributes.hp > 0)

  test("getters return correct rewards"):
    assert(MonsterController.getExpReward(dummyMonster) == 30)
    assert(MonsterController.getGoldReward(dummyMonster) == 50)
    assert(MonsterController.getEquipReward(dummyMonster).isEmpty)
    assert(MonsterController.getItemReward(dummyMonster).isEmpty)
