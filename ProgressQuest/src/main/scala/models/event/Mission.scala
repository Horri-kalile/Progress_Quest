package models.event

import models.player.Item

import java.util.UUID
import scala.util.Random

case class Mission(
                    id: String,
                    name: String,
                    description: String,
                    progression: Int = 0,
                    goal: Int = 1,
                    rewardExp: Int,
                    rewardGold: Option[Int] = None,
                    rewardItem: Option[Item] = None
                  ):
  def isCompleted: Boolean = progression >= goal

  def progressed(): Mission =
    if isCompleted then this
    else this.copy(progression = progression + 1)

object MissionFactory:

  private val namesAndDescriptions = List(
    "Goblin Hunt" -> "Eliminate goblins near the village.",
    "Wolf Trouble" -> "Deal with wolves threatening livestock.",
    "Lost Relic" -> "Retrieve a lost relic from nearby ruins.",
    "Bandit Menace" -> "Drive off bandits harassing travelers.",
    "Herb Gathering" -> "Collect rare herbs from the forest.",
    "Cave Mystery" -> "Explore the mysterious cave in the hills.",
    "Protect the Caravan" -> "Escort a trade caravan safely.",
    "Ancient Puzzle" -> "Solve the ruins’ magical puzzle.",
    "Ghost Problem" -> "Put restless spirits to peace.",
    "Hunter's Request" -> "Track and trap a dangerous beast.",
    "Scout the Area" -> "Map out nearby terrain for the guard.",
    "Farm Assistance" -> "Help with urgent farm labor.",
    "Mysterious Glow" -> "Investigate strange light from the woods.",
    "Hidden Cache" -> "Uncover a smuggler’s hidden stash.",
    "Messenger Duty" -> "Deliver an urgent message to the capital.",
    "Festival Prep" -> "Help prepare for the annual village festival.",
    "Mine Safety" -> "Check on missing miners.",
    "Missing Person" -> "Search for a missing child.",
    "Magical Leakage" -> "Seal a breach in the arcane barrier.",
    "Treant Attack" -> "Drive away angry treants from the orchard."
  )

  def random(): Mission =
    val (name, description) = Random.shuffle(namesAndDescriptions).head
    Mission(
      id = UUID.randomUUID().toString,
      name = name,
      description = description,
      goal = Random.between(1, 4),
      rewardExp = Random.between(50, 201),
      rewardGold = Some(Random.between(10, 51)),
      rewardItem = ??? /*if Random.nextBoolean() then Some(/*Item.random()*/) else None*/
    )