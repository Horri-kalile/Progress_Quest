package models.player

enum Race:
  case Human, Elf, Dwarf, Orc, Gnome, Titan, PandaMan, Gundam

enum ClassType:
  case Warrior, Mage, Poisoner, Cleric, Paladin, Voodoo_Princess, CowBoy

case class Identity(name: String, race: Race, classType: ClassType)