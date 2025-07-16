package controllers

import models.monster.Monster
import models.player.Player
import scala.annotation.tailrec
import scala.util.Random

/**
 * Handles turn-based combat logic between a player and a monster.
 */
object CombatController:

  /** Internal reference to last fought monster (for tracking/debugging). */
  private var _lastMonster: Option[Monster] = None

  /** Returns the last monster fought, if any. */
  def lastMonster: Option[Monster] = _lastMonster

  /** Stores a monster as the last monster fought. */
  def setLastMonster(monster: Monster): Unit =
    _lastMonster = Some(monster)

  /**
   * Simulates a full turn-based combat between player and monster.
   *
   * @param player  the player character
   * @param monster the enemy monster
   * @return a list of (Player state, optional Monster state, log message) for each action
   */
  def simulateFight(player: Player, monster: Monster): List[(Player, Option[Monster], String)] =

    @tailrec
    def loop(p: Player, m: Monster, acc: List[(Player, Option[Monster], String)], turn: Int): List[(Player, Option[Monster], String)] =
      if !p.isAlive || m.isDead then acc.reverse
      else
        val turnHeader = (p, Some(m), s"Turn $turn:")

        val (pAfterAttack, mAfterAttack, attackLogs) =
          if p.skills.nonEmpty && Random.nextBoolean() && p.currentMp >= 3 then
            val skill = Random.shuffle(p.skills).head
            val (pp, mm, msg) = PlayerController.useSkill(p, skill, m)
            (pp, mm, List(msg))
          else
            val damage = PlayerController.calculatePlayerAttack(p, m)
            val (damagedM, maybeExplosion) = MonsterController.takeDamage(m, damage)
            val damagedP = maybeExplosion.fold(p)(PlayerController.takeDamage(p, _))
            val logs = List(s"You attacked ${m.name} for $damage.") ++
              maybeExplosion.map(d => s"[Explosive] ${m.name} exploded for $d!").toList
            (damagedP, damagedM, logs)

        val accWithHeader = turnHeader :: acc
        val accWithPlayerLogs = attackLogs.reverse.foldLeft(accWithHeader) {
          case (logs, msg) => (pAfterAttack, Some(mAfterAttack), msg) :: logs
        }

        if mAfterAttack.isDead then
          val monsterLog = s"${mAfterAttack.name} was defeated!"
          ((pAfterAttack, None, monsterLog) :: accWithPlayerLogs).reverse
        else
          val (regeneratedM, regenLogOpt) = MonsterController.handleRegeneration(mAfterAttack)
          val (monsterDmg, monsterAttackLog) = MonsterController.attackPlayer(regeneratedM, pAfterAttack)
          val damagedPlayer = PlayerController.takeDamage(pAfterAttack, monsterDmg)
          val regenLogs = regenLogOpt.toList
          val allLogs = regenLogs :+ monsterAttackLog

          val accWithMonsterLogs = allLogs.reverse.foldLeft(accWithPlayerLogs) {
            case (logs, msg) => (damagedPlayer, Some(regeneratedM), msg) :: logs
          }

          if !damagedPlayer.isAlive then accWithMonsterLogs.reverse
          else loop(damagedPlayer, regeneratedM, accWithMonsterLogs, turn + 1)

    loop(player, monster, Nil, 1)

  /**
   * Handles equipment drop after a monster is defeated.
   * Compares new equipment to existing, either equipping or selling it.
   *
   * @param player  the player
   * @param monster the defeated monster
   * @return (updatedPlayer, resultMessage)
   */
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

  /**
   * Handles item drop after a monster is defeated.
   *
   * @param player  the player
   * @param monster the defeated monster
   * @return (updatedPlayer, resultMessage)
   */
  def handleItemDrop(player: Player, monster: Monster): (Player, String) =
    MonsterController.getItemReward(monster) match
      case Some(item) =>
        val updated = PlayerController.addItem(player, item)
        (updated, s"You found item: ${item.name}.")
      case None => (player, "No item drop.")
