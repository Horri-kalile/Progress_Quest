package models.monster

case class MonsterAttributes(hp: Int, currentHp: Int, attack: Int, defense: Int, weaknessPhysical: Double = 1.0, weaknessMagic: Double = 1.0)
