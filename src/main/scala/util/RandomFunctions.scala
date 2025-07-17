package util

import controllers.PlayerController
import models.event.GameEventModule.EventType
import models.event.GameEventModule.EventType.*
import models.player.EquipmentModule.{Equipment, EquipmentFactory}
import models.player.{Player, SkillFactory}
import util.GameConfig.*

import scala.util.Random

object RandomFunctions:

  /**
   * Extension method to cap a value between a minimum and maximum bound.
   *
   * @return the capped value between min and max
   */
  extension (x: Double)
    def capped(min: Double, max: Double): Double = x.max(min).min(max)

  /**
   * Returns a random [[EventType]] based on base probabilities,
   * adjusted dynamically by the player's `lucky` attribute.
   *
   * ### Probability Overview (base rates):
   * - Fight:        20%
   * - Magic:         5%
   * - Craft:         5%
   * - Power:        10%
   * - Mission:      20%
   * - ChangeWorld:  10%
   * - Training:     10%
   * - Restore:       5%
   * - Sell:          5%
   * - Special:  Dynamic; increases with luck
   * - GameOver: Dynamic; decreases with luck
   *
   * ### Special Notes:
   * - `specialChance` scales *up* with luck
   * - `gameOverChance` scales *down* with luck
   *
   * If none of the above thresholds are matched, falls back to `training`.
   *
   * @param lucky The player's `lucky` attribute (0–100+)
   * @return A randomly chosen [[EventType]] with luck-adjusted weighting
   */
  def getRandomEventType(lucky: Int): EventType =
    val specialChance = (baseSpecialChance + specialBonusPerLucky * lucky).capped(0.0, maxSpecialChance)
    val gameOverChance = (baseGameOverChance - specialBonusPerLucky * lucky).capped(minGameOverChance, 1.0)
    val x = Random.nextDouble()

    x match
      case v if v < 0.20 => fight
      case v if v < 0.25 => magic
      case v if v < 0.30 => craft
      case v if v < 0.40 => power
      case v if v < 0.60 => mission
      case v if v < 0.70 => changeWorld
      case v if v < 0.80 => training
      case v if v < 0.85 => restore
      case v if v < 0.90 => sell
      case v if v < 0.90 + specialChance => special
      case v if v < 0.90 + specialChance + gameOverChance => gameOver
      case _ => training // fallback


  /**
   * Determines whether an item should drop based on player's luck.
   *
   * @param playerLucky player's lucky attribute
   * @return true if drop occurs
   */
  def randomDropFlags(playerLucky: Int): Boolean =
    val bonus = (playerLucky * 0.001).min(0.50) // Max +50% bonus
    val chance = baseDropChance + bonus
    Random.nextDouble() < chance

  /**
   * Used to spawn strong monsters.
   */
  def tryGenerateStrongMonster(): Boolean =
    Random.nextBoolean()

  /**
   * Learns a new skill, or upgrades it if the player already knows it.
   *
   * @param player the player to modify
   * @return a tuple: (updated player, descriptive message)
   */
  def learnOrUpgradeSkill(player: Player): (Player, String) =
    val newSkill = SkillFactory.randomSkill()
    val (updatedPlayer, isNew) = PlayerController.addSkill(player, newSkill)
    val msg =
      if isNew then s"Learned new skill: ${newSkill.name}"
      else s"Upgraded existing skill: ${newSkill.name} to level ${newSkill.poweredUp.powerLevel}"
    (updatedPlayer, msg)



  /**
   * Attempts to equip a new equipment or power up an existing one.
   *
   * @param player        The player to modify
   * @param upgradeChance Probability (0.0 to 1.0) of upgrading existing equipment
   * @return A new Player with equipment changes and a descriptive message
   */
  def forgeOrUpgradeEquipment(player: Player, upgradeChance: Double = 0.5): (Player, String) =
    val newEq = EquipmentFactory.probBased(player.attributes.lucky, player.level)
    val maybeExisting: Option[Equipment] = player.equipment(newEq.get.slot)

    if maybeExisting.isDefined && Random.nextDouble() < upgradeChance then
      val upgraded = EquipmentFactory.powerUpEquip(maybeExisting.get)
      val updatedPlayer = PlayerController.equipmentOn(player, upgraded.slot, upgraded)
      (updatedPlayer, s"Upgraded equipment: ${maybeExisting.get.name} with ${maybeExisting.get.value} ${upgraded.name}, now worth ${upgraded.value}")
    else
      EquipmentFactory.alwaysDrop(player.level) match
        case Some(newEq) =>
          val updatedPlayer = PlayerController.equipmentOn(player, newEq.slot, newEq)
          (updatedPlayer, s"Equipped new item: ${newEq.name}")
        case None =>
          (player, "No equipment was forged.")
