---
layout: default
title: Guo jiahao
nav_order: 1
parent: Implementazione
---
# Guo jiahao
Ho sviluppato principalmente il modulo `Player` con i relativi sottocomponenti `Item`, `Equipment`, `Attributes`, `Behavior`, `Identity` e `Skill`. Inoltre, ho realizzato l’interfaccia grafica per la generazione del personaggio (`PlayerGenerationUI`) e il `CombatController`. L’architettura adottata segue i principi di clean code e programmazione funzionale, assicurando una chiara separazione delle responsabilità e una buona flessibilità nell’estensione dei comportamenti dei giocatori. Ho anche fornito supporto e assistenza durante l’implementazione del modulo `GameEvent` da parte di un collega.

## Modello Player

Il modello `Player` è definito tramite un **trait** che espone un'interfaccia completa per rappresentare un giocatore nel gioco. Questa interfaccia include proprietà come nome, identità, livello, esperienza, punti vita e mana, attributi base, inventario, equipaggiamento, abilità, missioni, oro e zona corrente.

L'architettura sfrutta la separazione tra **interfaccia (trait)** e **implementazione concreta (case class privata)**:

- Il **trait `Player`** definisce i metodi pubblici e le proprietà, comprese le funzionalità di logica di gioco (come ricevere danni, guarire, aggiornare attributi) usando metodi immutabili che ritornano nuove istanze.
- La **case class privata `PlayerImpl`**, nascosta all'esterno, implementa concretamente i metodi e mantiene lo stato interno del giocatore. Questo nasconde i dettagli implementativi e permette di modificare l’implementazione senza rompere il codice client.

La creazione degli oggetti Player è gestita da **factory methods** nell’oggetto companion, che validano i parametri di input e applicano regole di business, come l’inizializzazione degli attributi di base e l’applicazione di bonus iniziali tramite strategie comportamentali.

L’immutabilità è garantita da metodi di aggiornamento (`withX`) che generano nuove copie del giocatore con i valori modificati, prevenendo effetti collaterali indesiderati.

L’uso di un **pattern Strategy** per il behavior del giocatore permette di assegnare dinamicamente diverse logiche di comportamento, mantenendo il modello modulare e facilmente estendibile.

```scala
    def withHp(hp: Int): Player

    def withMp(mp: Int): Player

    def withLevel(newLevel: Int): Player

    def withExp(newExp: Int): Player

    def withCurrentHp(newHp: Int): Player

    def withCurrentMp(newMp: Int): Player

    def withBaseAttributes(newAttr: Attributes): Player
    
    /** Validate params, throw exception if invalid */
    private def validateParams(
                                name: String,
                                identity: Identity,
                                level: Int,
                                exp: Int,
                                hp: Int,
                                mp: Int,
                                currentHp: Int,
                                currentMp: Int,
                                baseAttributes: Attributes,
                                behaviorType: BehaviorType,
                                inventory: Map[Item, Int],
                                equipment: Map[EquipmentSlot, Option[Equipment]],
                                skills: List[Skill],
                                missions: List[Mission],
                                gold: Double,
                                currentZone: OriginZone
                              ): Unit =
    require(name.nonEmpty, "Player name cannot be empty")
    require(level > 0, "Level must be positive")
    require(exp >= 0, "Experience cannot be negative")
    require(hp > 0, "HP must be positive")
    require(mp >= 0, "MP cannot be negative")
    require(currentHp >= 0 && currentHp <= hp, "Current HP out of range")
    require(currentMp >= 0 && currentMp <= mp, "Current MP out of range")
    require(gold >= 0, "Gold cannot be negative")
    
    /** Apply factory method to create Player instances */
    def apply(
               name: String,
               identity: Identity,
               level: Int,
               exp: Int,
               hp: Int,
               mp: Int,
               currentHp: Int,
               currentMp: Int,
               baseAttributes: Attributes,
               behaviorType: BehaviorType,
               inventory: Map[Item, Int] = Map.empty,
               equipment: Map[EquipmentSlot, Option[Equipment]] = EquipmentSlot.values.map(_ -> None).toMap,
               skills: List[Skill] = List.empty,
               missions: List[Mission] = List.empty,
               gold: Double,
               currentZone: OriginZone
             ): Player =
    validateParams(name, identity, level, exp, hp, mp, currentHp, currentMp, baseAttributes, behaviorType, inventory,
    equipment, skills, missions, gold, currentZone)
    val raw = PlayerImpl(name, identity, level, exp, hp, mp, currentHp, currentMp, baseAttributes, behaviorType,
    inventory, equipment, skills, missions, gold, currentZone)
    raw.behavior.onGameStart(raw) // Apply behavior bonus when it needs
    
    /** Minimal factory method for convenience, using default level=1, exp=0, gold=0 */
    def apply(
               name: String,
               identity: Identity,
               baseAttributes: Attributes,
               behaviorType: BehaviorType
             ): Player =
    val baseHp = baseAttributes.constitution * 5
    val baseMp = baseAttributes.intelligence * 2
    val level = 1
    val exp = 0
    val gold = 0.0
    val currentZone = OriginZone.Plains
    
    apply(
      name = name,
      identity = identity,
      level = level,
      exp = exp,
      hp = baseHp,
      mp = baseMp,
      currentHp = baseHp,
      currentMp = baseMp,
      baseAttributes = baseAttributes,
      behaviorType = behaviorType,
      inventory = Map.empty,
      equipment = EquipmentSlot.values.map(_ -> None).toMap,
      skills = List.empty,
      missions = List.empty,
      gold = gold,
      currentZone = currentZone
    )
    
    /** Extractor for pattern matching */
    def unapply(p: Player): Option[(String, Identity, Int, Int, Int, Int, Int, Int, Attributes, BehaviorType,
      Map[Item, Int], Map[EquipmentSlot, Option[Equipment]], List[Skill], List[Mission], Double, OriginZone)] =
      Some(
        (
          p.name,
          p.identity,
          p.level,
          p.exp,
          p.hp,
          p.mp,
          p.currentHp,
          p.currentMp,
          p.baseAttributes,
          p.behaviorType,
          p.inventory,
          p.equipment,
          p.skills,
          p.missions,
          p.gold,
          p.currentZone
        )
      )
```

---



