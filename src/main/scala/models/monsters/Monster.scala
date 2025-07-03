package models.monsters

enum MonsterType:
  case Beast, Undead, Humanoid, Dragon, Demon, Elemental

case class Monster(
    name: String,
    level: Int,
    monsterType: MonsterType,
    health: Int,
    damage: Int,
    goldReward: Int,
    experienceReward: Int,
    description: String
)

object Monsters:
  import MonsterType.*

  // Low-level monsters (1-5)
  val rat = Monster("Giant Rat", 1, Beast, 20, 3, 5, 10, "A oversized rodent with sharp teeth")
  val skeleton = Monster("Skeleton Warrior", 2, Undead, 30, 5, 8, 15, "An animated skeleton wielding rusty weapons")
  val goblin = Monster("Cave Goblin", 3, Humanoid, 35, 6, 10, 20, "A small, green-skinned creature with a crude dagger")
  
  // Mid-level monsters (6-10)
  val orc = Monster("Orc Berserker", 6, Humanoid, 60, 12, 25, 40, "A muscular orc wielding a heavy axe")
  val wraith = Monster("Shadow Wraith", 7, Undead, 55, 15, 30, 45, "A ghostly figure that drains life force")
  val fireElemental = Monster("Fire Elemental", 8, Elemental, 70, 18, 35, 50, "A living flame that burns everything in its path")
  
  // High-level monsters (11-15)
  val basilisk = Monster("Stone Basilisk", 11, Beast, 100, 25, 60, 80, "A reptilian creature with a petrifying gaze")
  val demonKnight = Monster("Demon Knight", 13, Demon, 120, 30, 75, 100, "A heavily armored warrior corrupted by demonic power")
  val youngDragon = Monster("Young Forest Dragon", 15, Dragon, 150, 35, 100, 150, "A juvenile dragon protecting its territory")
  
  // Boss monsters (16-20)
  val ancientLich = Monster("Ancient Lich", 18, Undead, 200, 45, 200, 300, "An powerful undead sorcerer")
  val elderDragon = Monster("Elder Frost Dragon", 20, Dragon, 300, 60, 500, 500, "A massive dragon commanding ice and frost")
  val demonLord = Monster("Demon Lord Azrael", 20, Demon, 250, 55, 400, 450, "A powerful demon lord from the abyss")

  // Group monsters by level range for different areas
  val beginnerArea = List(rat, skeleton, goblin)
  val intermediateArea = List(orc, wraith, fireElemental)
  val advancedArea = List(basilisk, demonKnight, youngDragon)
  val bossArea = List(ancientLich, elderDragon, demonLord)

  // All monsters in a single list
  val allMonsters = beginnerArea ++ intermediateArea ++ advancedArea ++ bossArea
