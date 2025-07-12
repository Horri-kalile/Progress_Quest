package controllers

import models.player.Player
import models.monster.{Monster, MonstersFactory, OriginZone}

import scala.annotation.tailrec
import scala.util.Random


/**
 * Combat Controller - Handles fighting mechanics
 * TODO: This needs to be implemented to complete the FightEvent
 */
object CombatController:
  private var _lastMonster: Option[Monster] = None

  def lastMonster: Option[Monster] = _lastMonster

  def setLastMonster(monster: Monster): Unit =
    _lastMonster = Some(monster)

  /**
   * Simulate a fight between player and monster
   * Returns (updatedPlayer, combatLog)
   */
  def simulateFight(player: Player, monster: Monster): List[(Player, Option[Monster], String)] =

    @tailrec
    def loop(p: Player, m: Monster, acc: List[(Player, Option[Monster], String)], turn: Int): List[(Player, Option[Monster], String)] =
      if !PlayerController.isAlive(p) || !MonsterController.isAlive(m) then acc.reverse
      else
        val turnHeader = (p, Some(m), s"Turn $turn:")

        val useSkill = p.skills.nonEmpty && Random.nextBoolean()
        val (p1, m1, playerLog) =
          if useSkill then
            val skill = Random.shuffle(p.skills).head
            val (pp, mm) = skill.use(p, m)
            (pp, mm.asInstanceOf[Monster], s"You used ${skill.name}.")
          else
            val damage = PlayerController.calculatePlayerAttack(p)
            val (mm, maybeExplosion) = MonsterController.takeDamage(m, damage)
            val pp = maybeExplosion.fold(p)(PlayerController.takeDamage(p, _))
            val log = s"You attacked ${m.name} for $damage." + maybeExplosion.map(d => s" ${m.name} exploded for $d!").getOrElse("")
            (pp, mm, log)

        val accWithTurnHeader = turnHeader :: acc
        val accWithPlayer = (p1, Some(m1), playerLog) :: accWithTurnHeader

        if !MonsterController.isAlive(m1) then
          val monsterLog = s"${m1.name} was defeated!"
          val accWithMonster = (p1, None, monsterLog) :: accWithPlayer
          accWithMonster.reverse
        else
          val dmg = MonsterController.attackPlayer(m1, p1.level)
          val defence = p1.attributes.constitution
          val finalDmg = (dmg - defence).max(0)
          val p2 = PlayerController.takeDamage(p1, finalDmg)
          val monsterLog = s"${m1.name} attacked for $finalDmg."

          val accWithMonster = (p2, Some(m1), monsterLog) :: accWithPlayer

          if !PlayerController.isAlive(p2) then accWithMonster.reverse
          else loop(p2, m1, accWithMonster, turn + 1)

    loop(player, monster, Nil, 1)


  /**
   * Generate a random monster for player's level using MonstersFactory
   */
  def getRandomMonster(playerLevel: Int, playerLucky: Int): Monster =
    // Use the factory to get a random monster from a random zone
    val zones = models.monster.OriginZone.values
    val randomZone = zones(Random.nextInt(zones.length))

    MonstersFactory.randomMonsterForZone(randomZone, playerLevel, playerLucky)

  /**
   * Generate a random monster for player's level and zone using MonstersFactory
   */
  def getRandomMonsterForZone(playerLevel: Int, playerLucky: Int, zone: OriginZone): Monster =
    MonstersFactory.randomMonsterForZone(zone, playerLevel, playerLucky)

