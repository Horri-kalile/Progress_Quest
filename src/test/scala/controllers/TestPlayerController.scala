import models.player.*
import models.monster.*
import models.world.OriginZone
import controllers.PlayerController
import org.scalatest.funsuite.AnyFunSuite

class TestPlayerController extends AnyFunSuite {


  val baseAttributes = Attributes(
    strength = 10, constitution = 10, dexterity = 10,
    intelligence = 10, wisdom = 10, lucky = 10
  )


  def freshPlayer: Player = Player(
    name = "TestHero",
    identity = Identity(Race.Human, ClassType.Warrior),
    baseAttributes = baseAttributes,
    behaviorType = Behavior.BehaviorType.Heal
  )

  test("calculatePlayerAttack returns correct damage with no equipment") {
    val player = freshPlayer.withLevel(5)

    val monsterAttributes = MonsterAttributes(
      hp = 100, currentHp = 100, attack = 10, defense = 5,
      weaknessPhysical = 1.0, weaknessMagic = 1.0
    )

    val monster = Monster(
      name = "Goblin",
      level = 3,
      monsterType = MonsterType.Humanoid,
      originZone = OriginZone.Forest,
      attributes = monsterAttributes,
      goldReward = 10,
      experienceReward = 20,
      itemReward = None,
      equipReward = None,
      behavior = Defensive,
      description = "A test goblin."
    )

    val damage = PlayerController.calculatePlayerAttack(player, monster)
    assert(damage == 10)
  }

  test("takeDamage reduces HP correctly") {
    val player = freshPlayer.withCurrentHp(100)
    val damaged = PlayerController.takeDamage(player, 40)
    assert(damaged.currentHp == 60)
  }

  test("heal increases HP up to max") {
    val player = freshPlayer.withHp(100).withCurrentHp(50)
    val healed = PlayerController.heal(player, 30)
    assert(healed.currentHp == 80)
  }

  test("gainXP without special behavior increases XP") {
    val player = freshPlayer.withLevel(1).withExp(50)
    val result = PlayerController.gainXP(player, 30)
    assert(result.exp == 80)
    assert(result.level == 1)
  }


  test("addItem adds items correctly") {
    val potion = Item("Potion", 10.0, Rarity.Common)
    val playerWithItem = PlayerController.addItem(freshPlayer, potion)
    assert(playerWithItem.inventory(potion) == 1)
  }
  test("consumeItem removes correct quantity") {
    val potion = Item("Potion", gold = 5.0, rarity = Rarity.Common)
    val withItems = PlayerController.addItem(freshPlayer, potion, 3)
    val updated = PlayerController.consumeItem(withItems, potion, 2)

    assert(updated.inventory(potion) == 1)
  }

  test("equipmentOn equips item to correct slot") {
    val eq = Equipment("Iron Sword", EquipmentSlot.Weapon, statBonus = baseAttributes, value = 100)
    val updated = PlayerController.equipmentOn(freshPlayer, EquipmentSlot.Weapon, eq)
    assert(updated.equipment(EquipmentSlot.Weapon).contains(eq))
  }

  test("equipmentOff unequips item from slot") {
    val eq = Equipment("Iron Sword", EquipmentSlot.Weapon, statBonus = baseAttributes, value = 100)
    val withEq = PlayerController.equipmentOn(freshPlayer, EquipmentSlot.Weapon, eq)
    val removed = PlayerController.equipmentOff(withEq, EquipmentSlot.Weapon)
    assert(removed.equipment(EquipmentSlot.Weapon).isEmpty)
  }

  test("addGold increases player gold") {
    val rich = freshPlayer.withGold(100)
    val updated = PlayerController.addGold(rich, 50)
    assert(updated.gold == 150)
  }

  test("changeWorld sets new OriginZone correctly") {
    val initialZone = OriginZone.Forest
    val newZone = OriginZone.Volcano

    val player = freshPlayer.withCurrentZone(initialZone)
    val updatedPlayer = PlayerController.changeWorld(player, newZone)

    assert(updatedPlayer.currentZone == newZone)
  }
  test("spendGold reduces gold if player has enough") {
    val player = freshPlayer.withGold(100)
    val updated = PlayerController.spendGold(player, 40)

    assert(updated.gold == 60)
  }

  test("spendGold does not change gold if not enough") {
    val player = freshPlayer.withGold(30)
    val updated = PlayerController.spendGold(player, 50)

    assert(updated.gold == 30)
  }

  test("addSkill powers up existing GenericSkill") {
    val baseSkill = GenericSkill("Slash", SkillEffectType.Physical, manaCost = 10, baseMultiplier = 1.0, powerLevel = 1)
    val player = freshPlayer.withSkills(List(baseSkill))

    val updated = PlayerController.addSkill(player, baseSkill)

    val upgradedSkill = updated.skills.find(_.name == "Slash").get
    assert(upgradedSkill.isInstanceOf[GenericSkill])
    assert(upgradedSkill.asInstanceOf[GenericSkill].powerLevel == 2)
  }
  test("levelDownAndDecreaseStats decreases level but not below 1") {
    val player = freshPlayer.withLevel(5)
    val updated = PlayerController.levelDownAndDecreaseStats(player, 3)
    assert(updated.level == 2)

    val lowerBounded = PlayerController.levelDownAndDecreaseStats(player, 10)
    assert(lowerBounded.level == 1)
  }
  test("levelUp increases level, resets XP, boosts HP/MP and attributes") {
    val player = freshPlayer.withLevel(3).withHp(100).withMp(50).withExp(100)

    val leveledUp = PlayerController.levelUp(player)

    assert(leveledUp.level == 4)
    assert(leveledUp.exp == 0)
    assert(leveledUp.hp > 100)
    assert(leveledUp.mp > 50)
    assert(leveledUp.currentHp == leveledUp.hp)
    assert(leveledUp.currentMp == leveledUp.mp)
    assert(leveledUp.baseAttributes.total > player.baseAttributes.total)
  }
  test("levelDown reduces level, resets XP, decreases HP/MP and attributes") {
    val player = freshPlayer.withLevel(5).withHp(100).withMp(50).withExp(120)

    val leveledDown = PlayerController.levelDown(player)

    assert(leveledDown.level == 4)
    assert(leveledDown.exp == 0)
    assert(leveledDown.hp < 100)
    assert(leveledDown.mp < 50)
    assert(leveledDown.currentHp == leveledDown.hp)
    assert(leveledDown.currentMp == leveledDown.mp)
    assert(leveledDown.baseAttributes.total < player.baseAttributes.total)
  }
  test("gainXP indirectly triggers maybeLearnSkill if luck is high") {
    val base = freshPlayer.withSkills(Nil).withExp(0).withLevel(5)

    val maybeLearned = (1 to 30).view
      .map(_ => PlayerController.gainXP(base, 500))
      .find(_.skills.nonEmpty)

    assert(
      maybeLearned.nonEmpty,
      "Expected at least one skill to be learned after multiple attempts"
    )
  }


  test("sellRandomItem removes item and increases gold") {
    val item = Item("Potion", 10.0, Rarity.Common)
    val playerWithItems = PlayerController.addItem(freshPlayer.withGold(0), item, 5)

    val (updatedPlayer, message) = PlayerController.sellRandomItem(playerWithItems)

    assert(updatedPlayer.gold > 0)
    assert(updatedPlayer.inventory.getOrElse(item, 0) < 5)
    assert(message.contains("Potion"))
  }
  test("stealRandomItem removes 1 quantity of some item") {
    val itemA = Item("Potion", 10.0, Rarity.Common)
    val itemB = Item("Elixir", 20.0, Rarity.Uncommon)

    val player = freshPlayer
      .withInventory(Map(itemA -> 2, itemB -> 1))

    val (afterSteal, message) = PlayerController.stealRandomItem(player)

    // Check that the quantity of one of the items was reduced by 1
    val expectedOptions = List(
      Map(itemA -> 1, itemB -> 1), // If Potion was stolen
      Map(itemA -> 2) // If Elixir was stolen
    )
    assert(expectedOptions.contains(afterSteal.inventory))

    // Check that the message mentions one of the two items
    assert(message.contains("Potion") || message.contains("Elixir"))
  }
  test("playerInjured halves current HP and MP") {
    val player = freshPlayer
      .withCurrentHp(80)
      .withCurrentMp(60)

    val injured = PlayerController.playerInjured(player)

    // HP and MP should each be divided by 2
    assert(injured.currentHp == 40)
    assert(injured.currentMp == 30)
  }
  test("Physical skill deals correct damage and reduces MP") {
    val player = freshPlayer
      .withBaseAttributes(freshPlayer.baseAttributes.copy(strength = 10))
      .withCurrentMp(50)

    val skill = GenericSkill(
      name = "Slash",
      effectType = SkillEffectType.Physical,
      manaCost = 10,
      baseMultiplier = 1.0,
      powerLevel = 1
    )

    val monster = MonstersFactory.randomMonsterForZone(
      zone = OriginZone.Plains,
      playerLevel = player.level,
      playerLucky = player.attributes.lucky
    )

    val originalHp = monster.attributes.currentHp

    val (updatedPlayer, damagedMonster, message) = PlayerController.useSkill(player, skill, monster)

    assert(updatedPlayer.currentMp == 40, "MP should decrease by manaCost (10)")
    assert(damagedMonster.attributes.currentHp < originalHp, "Monster should have taken damage")
    assert(message.contains("Slash"), "Feedback message should mention skill name")
  }


}
