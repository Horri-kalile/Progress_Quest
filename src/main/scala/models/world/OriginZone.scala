package models.world

/** Enumeration of world zones where monsters can originate and be encountered.
  *
  * OriginZone defines the different biomes and regions within the game world where specific types of monsters naturally
  * spawn. Each zone has distinct environmental characteristics that influence monster types, behaviors, and combat
  * modifiers.
  *
  * Zones are used by:
  *   - Monster generation system to determine appropriate creature types
  *   - World system to apply environmental buffs and debuffs
  */
enum OriginZone:
  /** Forest zone - temperate woodland environment.
    */
  case Forest

  /** Swamp zone - wetland environment with murky waters.
    */
  case Swamp

  /** Desert zone - arid environment with extreme heat.
    */
  case Desert

  /** Volcano zone - volcanic environment with lava and extreme heat.
    */
  case Volcano

  /** Plains zone - open grassland environment.
    */
  case Plains
