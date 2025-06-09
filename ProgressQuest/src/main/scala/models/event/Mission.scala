package models.event

import models.player.Item

case class Mission(name: String, description: String, rewardExp: Int, rewardGold: Option[Int], rewardItem: Option[Item] = None)
