package controllers

import models.monster.Monster
import models.player.Player
import util.GameConfig.maxTurnBattle

import scala.annotation.tailrec
import scala.util.Random

/** Handles turn-based combat logic between a player and a monster.
 */
object CombatController:

  /** Internal reference to last fought monster (for tracking/debugging). */
  private var _lastMonster: Option[Monster] = None

  /** Returns the last monster fought, if any. */
  def lastMonster: Option[Monster] = _lastMonster

  /** Stores a monster as the last monster fought. */
  def setLastMonster(monster: Monster): Unit =
    _lastMonster = Some(monster)

  /** Curried function for messages with actor and target:
   *
   * Example: "Alice attacked Goblin for 5 damage"
   *
   * @param actorName  name of the acting entity (player or monster)
   * @param targetName name of the target entity
   * @param action     the action verb or phrase (e.g. "attacked", "used skill on")
   * @param details    additional detail string (e.g. "for 5 damage")
   * @return formatted message string
   */
  private def actionMessage(actorName: String)(targetName: String)(action: String, details: String): String =
    s"$actorName $action $targetName $details"

  /** Curried function for messages with only actor name:
   *
   * Example: "Alice used a healing skill"
   *
   * @param actorName name of the acting entity
   * @param event     description of the event/action
   * @return formatted message string
   */
  private def actorOnlyMessage(actorName: String)(event: String): String =
    s"$actorName $event"

  /** Curried function for messages with only target name:
   *
   * Example: "Slime was defeated"
   *
   * @param targetName name of the target entity
   * @param event      description of the event/action
   * @return formatted message string
   */
  private def targetOnlyMessage(targetName: String)(event: String): String =
    s"$targetName $event"

  /** Simulates a full turn-based combat between player and monster.
   *
   * @param player
   * the player character
   * @param monster
   * the enemy monster
   * @return
   * a list of (Player state, optional Monster state, log message) for each action
   */
  def simulateFight(player: Player, monster: Monster): List[(Player, Option[Monster], String)] =

    // Partial application of message functions to fix actor/target names
    val playerAct = actionMessage(player.name) _
    val playerOnly = actorOnlyMessage(player.name) _
    val monsterOnly = targetOnlyMessage(monster.name) _

    @tailrec
    def loop(p: Player, m: Monster, acc: List[(Player, Option[Monster], String)], turn: Int)
    : List[(Player, Option[Monster], String)] =
      if !p.isAlive || m.isDead then acc.reverse
      else if turn > maxTurnBattle then
        val msg = monsterOnly("is too exhaustive, better run away.")
        ((p, Some(m), msg) :: acc).reverse
      else
        val acc1 = (p, Some(m), s"Turn $turn:") :: acc

        // Player's turn
        val (pAfterAttack, mAfterAttack, playerLogs) =
          if p.skills.nonEmpty && Random.nextBoolean() && p.currentMp >= 3 then
            val skill = Random.shuffle(p.skills).head
            PlayerController.useSkill(p, skill, m) match
              case (pp, mm, log) => (pp, mm, List(playerOnly(s"tried to use a skill: $log")))
          else
            val dmg = PlayerController.calculatePlayerAttack(p, m)
            val (mDamaged, explosionOpt) = MonsterController.takeDamage(m, dmg)
            val pDamaged = explosionOpt.map(PlayerController.takeDamage(p, _)).getOrElse(p)
            val logs = List(playerAct(m.name)("attacked for", s"$dmg damage.")) ++
              explosionOpt.map(e => monsterOnly(s"exploded for $e damage!"))
            (pDamaged, mDamaged, logs)

        val acc2 = playerLogs.reverse.foldLeft(acc1)((a, log) => (pAfterAttack, Some(mAfterAttack), log) :: a)

        if mAfterAttack.isDead then
          ((pAfterAttack, Some(mAfterAttack), monsterOnly("was defeated!")) :: acc2).reverse
        else
          val (mRegen, regenMsgOpt) = MonsterController.handleRegeneration(mAfterAttack)
          val (dmgToPlayer, attackMsg, mUpdated) = MonsterController.attackPlayer(mRegen, pAfterAttack)
          val pFinal = PlayerController.takeDamage(pAfterAttack, dmgToPlayer)

          val monsterLogs = regenMsgOpt.toList :+ attackMsg
          val acc3 = monsterLogs.reverse.foldLeft(acc2)((a, log) => (pFinal, Some(mUpdated), log) :: a)

          if !pFinal.isAlive then acc3.reverse
          else loop(pFinal, mUpdated, acc3, turn + 1)

    loop(player, monster, Nil, 1)

  /** Handles equipment drop after a monster is defeated. Compares new equipment to existing, either equipping or
   * selling it.
   *
   * @param player
   * the player
   * @param monster
   * the defeated monster
   * @return
   * (updatedPlayer, resultMessage)
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

  /** Handles item drop after a monster is defeated.
   *
   * @param player
   * the player
   * @param monster
   * the defeated monster
   * @return
   * (updatedPlayer, resultMessage)
   */
  def handleItemDrop(player: Player, monster: Monster): (Player, String) =
    MonsterController.getItemReward(monster) match
      case Some(item) =>
        val updated = PlayerController.addItem(player, item)
        (updated, s"You found item: ${item.name}.")
      case None => (player, "No item drop.")
