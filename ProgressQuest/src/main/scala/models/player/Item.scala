package models.player

case class Item(name: String, applyTo: Player => Unit)
