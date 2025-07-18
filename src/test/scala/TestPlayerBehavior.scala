import models.player.{Attributes, Behavior, ClassType, Identity, Player, Race}
import models.player.Behavior.*
import models.player.Behavior.BehaviorType.*
import models.world.OriginZone.Desert
import org.scalatest.funsuite.AnyFunSuite

class TestPlayerBehavior extends AnyFunSuite:
  def basePlayer(race: Race, behaviorType: BehaviorType, classType: ClassType): Player =
    Player(
      name = "Player",
      identity = Identity(race, classType),
      level = 1,
      exp = 0,
      baseAttributes = Attributes(5, 5, 5, 5, 5, 5),
      equipment = Map.empty,
      inventory = Map.empty,
      behaviorType = behaviorType,
      gold = 0,
      hp = 100,
      mp = 50,
      currentHp = 100,
      currentMp = 50,
      skills = List.empty,
      missions = List.empty,
      currentZone = Desert
    )

  val baseDamage = 100
  val baseExp = 1000

  test("Aggressive increases battle damage by 10% to 30%"):
    val player: Player = basePlayer(Race.Human, BehaviorType.Aggressive, ClassType.Warrior)
    val dmg = player.behavior.onBattleDamage(player, baseDamage)
    println(baseDamage)
    println(dmg)
    assert(dmg >= (baseDamage * 1.1).toInt && dmg <= (baseDamage * 1.3).toInt)

  test("Defensive reduces incoming damage by 10% to 30%"):
    val player: Player = basePlayer(Race.Human, BehaviorType.Defensive, ClassType.Warrior)
    val dmgTaken = player.behavior.onDamageTaken(player, baseDamage)
    assert(dmgTaken >= (baseDamage * 0.7).toInt && dmgTaken <= (baseDamage * 0.9).toInt)

  test("FastLeveling increases battle end XP by 10% to 50%"):
    val player: Player = basePlayer(Race.Human, BehaviorType.FastLeveling, ClassType.Warrior)
    val expBonus = player.behavior.onBattleEnd(baseExp)
    assert(expBonus >= (baseExp * 0.1).toInt && expBonus <= (baseExp * 0.5).toInt)

  test("TwiceAttack deals 2 attacks each between 50% and 150% damage"):
    val player: Player = basePlayer(Race.Human, BehaviorType.TwiceAttack, ClassType.Warrior)
    val dmg = player.behavior.onBattleDamage(player, baseDamage)
    assert(dmg >= (baseDamage * 1.0).toInt && dmg <= (baseDamage * 3.0).toInt)

  test("Heal restores 10% to 50% of XP as HP after battle"):
    val player: Player = basePlayer(Race.Human, BehaviorType.Heal, ClassType.Warrior)
    val heal = player.behavior.onBattleEnd(baseExp)
    assert(heal >= (baseExp * 0.1).toInt && heal <= (baseExp * 0.5).toInt)

  test("Lucky doubles player's lucky attribute on game start"):
    val player: Player = basePlayer(Race.Human, BehaviorType.Lucky, ClassType.Warrior)
    val updated = player.behavior.onGameStart(player)
    assert(updated.baseAttributes.lucky == player.baseAttributes.lucky * 2)

  test("MoreDodge increases dexterity by 50 to 100 on game start"):
    val player: Player = basePlayer(Race.Human, BehaviorType.MoreDodge, ClassType.Warrior)
    val updated = player.behavior.onGameStart(player)
    val diff = updated.baseAttributes.dexterity - player.baseAttributes.dexterity
    assert(diff >= 50 && diff <= 100)

  test("OneShotChance sometimes deals massive damage"):
    val player: Player = basePlayer(Race.Human, BehaviorType.OneShotChance, ClassType.Warrior)
    val damages = (1 to 1000).map(_ => player.behavior.onBattleDamage(player, baseDamage))
    assert(damages.contains(99 * player.level))
    assert(damages.contains(baseDamage))

  import Behavior.given

  test("Aggressive maps to Aggressive strategy"):
    val strategy = summon[Conversion[Behavior.BehaviorType, Behavior.Strategy]].apply(Aggressive)
    assert(strategy.getClass.getSimpleName.contains("Aggressive"))

  test("Defensive maps to Defensive strategy"):
    val strategy = summon[Conversion[Behavior.BehaviorType, Behavior.Strategy]].apply(Defensive)
    assert(strategy.getClass.getSimpleName.contains("Defensive"))

  test("FastLeveling maps to FastLeveling strategy"):
    val strategy = summon[Conversion[Behavior.BehaviorType, Behavior.Strategy]].apply(FastLeveling)
    assert(strategy.getClass.getSimpleName.contains("FastLeveling"))

  test("TwiceAttack maps to TwiceAttack strategy"):
    val strategy = summon[Conversion[Behavior.BehaviorType, Behavior.Strategy]].apply(TwiceAttack)
    assert(strategy.getClass.getSimpleName.contains("TwiceAttack"))

  test("Heal maps to Heal strategy"):
    val strategy = summon[Conversion[Behavior.BehaviorType, Behavior.Strategy]].apply(Heal)
    assert(strategy.getClass.getSimpleName.contains("Heal"))

  test("Lucky maps to Lucky strategy"):
    val strategy = summon[Conversion[Behavior.BehaviorType, Behavior.Strategy]].apply(Lucky)
    assert(strategy.getClass.getSimpleName.contains("Lucky"))

  test("MoreDodge maps to DexterityBoost strategy"):
    val strategy = summon[Conversion[Behavior.BehaviorType, Behavior.Strategy]].apply(MoreDodge)
    assert(strategy.getClass.getSimpleName.contains("DexterityBoost"))

  test("OneShotChance maps to OneShotChance strategy"):

    val strategy = summon[Conversion[Behavior.BehaviorType, Behavior.Strategy]].apply(OneShotChance)
    assert(strategy.getClass.getSimpleName.contains("OneShotChance"))
