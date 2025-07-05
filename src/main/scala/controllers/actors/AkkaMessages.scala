package controllers.actors

import models.event.Mission
import models.player.{Equipment, EquipmentSlot, Item, Player}

sealed trait AkkaMessages

case class UpdatePlayerStats(player: Player) extends AkkaMessages

case class UpdateHeroDiary(entries: Seq[String]) extends AkkaMessages

case class UpdateCombatLog(messages: Seq[String]) extends AkkaMessages

case class UpdatePlayerHpMp(hp: Int, mp: Int, maxHp: Int, maxMp: Int) extends AkkaMessages

case class UpdateInventory(items: Map[Item, Int]) extends AkkaMessages

case class UpdateEquipment(equipment: Map[EquipmentSlot, Option[Equipment]]) extends AkkaMessages

case class UpdateMissions(missions: Seq[Mission]) extends AkkaMessages

