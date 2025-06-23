package models.player

enum Rarity:
  case Common, Uncommon, Rare, Epic, Legendary

case class Item(name: String, gold: Double, rarity: Rarity)
