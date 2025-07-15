package controllers

import models.player.Player
import models.monster.{Monster, MonstersFactory, OriginZone}
import util.RandomFunctions

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
      if !p.isAlive || m.isDead then acc.reverse
      else
        val turnHeader = (p, Some(m), s"Turn $turn:")

        val (pAfterAttack, mAfterAttack, attackLogs) =
          if p.skills.nonEmpty && Random.nextBoolean() && p.currentMp >= 1 then
            val skill = Random.shuffle(p.skills).head
            val (pp, mm, msg) = PlayerController.useSkill(player = p, skill = skill, target = m)
            (pp, mm, List(msg))
          else
            val damage = PlayerController.calculatePlayerAttack(p, m)
            val (damagedM, maybeExplosion) = MonsterController.takeDamage(m, damage)
            val damagedP = maybeExplosion.fold(p)(PlayerController.takeDamage(p, _))
            val logs = List(s"You attacked ${m.name} for $damage.") ++ maybeExplosion.map(d => s"[Explosive] ${m.name} exploded for $d!").toList
            (damagedP, damagedM, logs)

        val accWithHeader = turnHeader :: acc
        val accWithPlayer = attackLogs.reverse.foldLeft(accWithHeader) {
          case (logs, msg) => (pAfterAttack, Some(mAfterAttack), msg) :: logs
        }


        if mAfterAttack.isDead then
          val monsterLog = s"${mAfterAttack.name} was defeated!"
          ((pAfterAttack, None, monsterLog) :: accWithPlayer).reverse
        else
          val (regeneratedM, regenLogOpt) = MonsterController.handleRegeneration(mAfterAttack)
          val monsterDmg = MonsterController.attackPlayer(regeneratedM, pAfterAttack)
          val damagedPlayer = PlayerController.takeDamage(pAfterAttack, monsterDmg)

          val monsterAttackLog = s"${regeneratedM.name} attacked for $monsterDmg."
          val regenLogs = regenLogOpt.toList

          val allLogs = regenLogs :+ monsterAttackLog
          val accWithMonsterLogs = allLogs.reverse.foldLeft(accWithPlayer) {
            case (logsAcc, msg) => (damagedPlayer, Some(regeneratedM), msg) :: logsAcc
          }

          if !damagedPlayer.isAlive then accWithMonsterLogs.reverse
          else loop(damagedPlayer, regeneratedM, accWithMonsterLogs, turn + 1)

    loop(player, monster, Nil, 1)

  def handleEquipDrop(player: Player, monster: Monster): (Player, String) =
    MonsterController.getEquipReward(monster) match
      case Some(newEquip) =>
        val slot = newEquip.slot
        player.equipment.getOrElse(slot, None) match
          case Some(old) if old.value >= newEquip.value =>
            val updated = PlayerController.addGold(player, newEquip.value)
            (updated, s"You found ${newEquip.name}, sold for ${newEquip.value} gold.")
          case _ =>
            val updated = PlayerController.equipmentOn(player, slot, newEquip)
            (updated, s"You equipped: ${newEquip.name} ($slot).")
      case None => (player, "No equipment drop.")

  def handleItemDrop(player: Player, monster: Monster): (Player, String) =
    MonsterController.getItemReward(monster) match
      case Some(item) =>
        val updated = PlayerController.addItem(player, item)
        (updated, s"You found item: ${item.name}.")
      case None => (player, "No item drop.")



