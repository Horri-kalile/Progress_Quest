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
## Modello Behavior

Descrizione del Modello Behavior

Il package `Behavior` definisce strategie di behavior per il giocatore, influenzando meccaniche come danni, attacco, skill e attributi iniziali.

---

Enumerazione `BehaviorType`

Tipi di behavior disponibili:

- `Aggressive`: aumenta del 10-30% il danno inflitto
- `Defensive`: riduce del 10-30% il danno subito
- `FastLeveling`: aumenta del 10-50% l’esperienza guadagnata
- `TwiceAttack`: effettua un doppio attacco con danni variabili
- `Heal`: converte parte dell’XP in guarigione dopo la battaglia
- `Lucky`: raddoppia la fortuna iniziale del giocatore
- `MoreDodge`: incrementa casualmente la destrezza per migliorare la schivata
- `OneShotChance`: 25% di possibilità di colpo letale (danno = 99 × livello giocatore)

---

Trait `Strategy`

Interfaccia che definisce i metodi per modificare il comportamento del giocatore durante il gioco:

- `onGameStart(player)`: modifica gli attributi o lo stato all’inizio del gioco
- `onBattleDamage(player, damage)`: modifica il danno in uscita durante il combattimento
- `onBattleEnd(value)`: modifica i punti esperienza o le ricompense dopo la battaglia
- `onDamageTaken(player, damage)`: modifica il danno subito dal giocatore

Le implementazioni concrete di questo trait definiscono le logiche specifiche di ciascun behavior.

---

Conversione e Implementazioni

Ogni valore di `BehaviorType` viene convertito automaticamente nella corrispondente strategia tramite una conversione implicita (`given Conversion`), permettendo una risoluzione modulare e dinamica del behavior.

Le strategie private (`Aggressive`, `Defensive`, `Lucky`, `OneShotChance`, ecc.) sovrascrivono i metodi del trait per applicare le modifiche specifiche relative a danni, attributi o esperienza.

```scala
  trait Strategy:
    /** Called once when the game starts. Can modify player attributes or state. */
    def onGameStart(player: Player): Player = player

    /** Modifies the outgoing damage dealt by the player during combat. */
    def onBattleDamage(player: Player, damage: Int): Int = damage

    /** Modifies experience points or rewards earned after a battle ends. */
    def onBattleEnd(value: Int): Int = value

    /** Modifies the incoming damage when the player is attacked. */
    def onDamageTaken(player: Player, damage: Int): Int = damage

    /** Converts a BehaviorType enum to its corresponding Strategy implementation. */
    given Conversion[BehaviorType, Strategy] with
        def apply(bt: BehaviorType): Strategy = bt match
        case BehaviorType.Aggressive => Aggressive()
        case BehaviorType.Defensive => Defensive()
        case BehaviorType.FastLeveling => FastLeveling()
        case BehaviorType.TwiceAttack => TwiceAttack()
        case BehaviorType.Heal => Heal()
        case BehaviorType.Lucky => Lucky()
        case BehaviorType.MoreDodge => DexterityBoost() 
        case BehaviorType.OneShotChance => OneShotChance()
    
    /** Aggressive: Increases the player's outgoing damage by 10% to 30%.
     */
    private case class Aggressive() extends Strategy:
      override def onBattleDamage(player: Player, damage: Int): Int =
        (damage * Random.between(1.1, 1.3)).toInt
```
---

## Modello Identity
Descrizione del Modello Identity

Il modello Identity definisce meccanismi per l’applicazione di bonus specifici di razza e classe al giocatore, influenzando attributi, HP, MP, equipaggiamento e abilità iniziali.

---

Enumerazioni fondamentali

- `Race`: definisce le razze disponibili (Human, Elf, Dwarf, Orc, Gnome, Titan, PandaMan, Gundam).
- `ClassType`: definisce alcune classi RPG (Warrior, Mage, Poisoner, Cleric, Paladin, Assassin, CowBoy).
- `Identity`: combina `Race` e `ClassType` per rappresentare l’identità del personaggio.

---

Modulo PlayerBonusesModule

Contiene le strategie per l’applicazione dei bonus di razza e classe tramite:

- **RaceBonusStrategy**: applica bonus specifici di razza, restituendo un moltiplicatore per HP, MP e un nuovo `Player` con modifiche ad attributi o equipaggiamento.
- **ClassBonusStrategy**: applica bonus fissi di HP e MP legati alla classe del giocatore.

Factory di Strategie

- `RaceBonusStrategyFactory`: restituisce la strategia corretta per la razza del giocatore.
- `ClassBonusStrategyFactory`: restituisce la strategia corretta per la classe del giocatore.

---

Implementazioni private

Ogni razza modifica attributi specifici (es. Elf aumenta destrezza, Dwarf costituzione, Titan forza, ecc.) e può influenzare i moltiplicatori di HP e MP. Alcune razze assegnano anche equipaggiamenti iniziali (es. Human).

Le classi forniscono bonus HP e MP variabili o nulli (es. Warrior bonus HP, Poisoner bonus MP, Mage nessun bonus).

---

Applicazione dei bonus

L’oggetto `PlayerBonusesApplication` gestisce l’applicazione combinata dei bonus di razza e classe:

1. Ottiene la strategia di razza e applica i bonus al giocatore.
2. Ottiene la strategia di classe e applica i bonus di HP/MP.
3. Calcola i nuovi valori di HP e MP moltiplicati e aggiunti ai bonus.
4. Aggiunge le skill iniziali appropriate alla classe e livello.
5. Restituisce un nuovo `Player` aggiornato con tutti i bonus e le abilità.

```scala
object RaceBonusStrategyFactory:

    def getStrategy(race: Race): RaceBonusStrategy = race match
      case Human => HumanRaceBonus
      case Elf => ElfRaceBonus
      case Dwarf => DwarfRaceBonus
      case Orc => OrcRaceBonus
      case Gnome => GnomeRaceBonus
      case Titan => TitanRaceBonus
      case PandaMan => PandaManRaceBonus
      case Gundam => GundamRaceBonus

  /** Factory object to retrieve the appropriate [[ClassBonusStrategy]] for a given [[ClassType]]. */
  object ClassBonusStrategyFactory:

    def getStrategy(classType: ClassType): ClassBonusStrategy = classType match
      case Mage | Cleric | Paladin | Assassin => NoClassBonus
      case Warrior => WarriorBonus
      case Poisoner => PoisonerBonus
      case CowBoy => CowBoyBonus

    def applyRaceAndClassBonuses(player: Player): Player =
        val raceStrategy = RaceBonusStrategyFactory.getStrategy(player.identity.race)
        val (hpMultiplier, mpMultiplier, playerWithRaceBonuses) = raceStrategy.applyBonuses(player)
        
        val classStrategy = ClassBonusStrategyFactory.getStrategy(player.identity.classType)
        val (classHpBonus, classMpBonus) = classStrategy.applyBonuses(playerWithRaceBonuses)
        
        val raceHp = (player.hp * hpMultiplier).toInt
        val raceMp = (player.mp * mpMultiplier).toInt
        
        val startingSkills = SkillFactory.generateStartingSkill(player.level, player.identity.classType).toList
        
        playerWithRaceBonuses
          .withHp(raceHp + classHpBonus)
          .withMp(raceMp + classMpBonus)
          .withCurrentHp(raceHp + classHpBonus)
          .withCurrentMp(raceMp + classMpBonus)
          .withSkills(startingSkills)
```
---




## Controller CombatController
Descrizione del CombatController

Il `CombatController` è responsabile della gestione del combattimento a turni tra un giocatore (`Player`) e un mostro (`Monster`).

---
Responsabilità principali

- Simulare il combattimento a turni tra giocatore e mostro, gestendo attacchi, uso di abilità, rigenerazione, danni e stati di morte.
- Tracciare il mostro con cui si è combattuto per ultime (utile per debug o statistiche).
- Gestire la logica di drop di equipaggiamenti e oggetti una volta sconfitto il mostro, decidendo se equipaggiare o vendere un equipment sostituito da quello migliore.

---

Funzionalità principali

Stato e tracciamento

- Variabile privata `_lastMonster`: memorizza l’ultimo mostro affrontato.
- Metodi `lastMonster` e `setLastMonster`: permettono di leggere e impostare questo valore.

---

Generazione di messaggi di log

- Funzioni private currificate per creare messaggi di azione, eventi con player e mostro, o solo player o solo mostro.
- Questi messaggi aiutano a tracciare passo passo cosa accade durante il combattimento (es. "Player attacked Slime for 5 damage").

---

Simulazione del combattimento (`simulateFight`)

- Riceve un giocatore e un mostro e restituisce una lista di tuple `(Player, Option[Monster], String)`, contenente lo stato del giocatore, dello (eventuale) mostro e messaggi di log per ogni azione.
- La simulazione è ricorsiva e termina se:
    - Il giocatore muore (`!p.isAlive`),
    - Il mostro muore (`m.isDead`),
    - O viene superato un limite massimo di turni (`maxTurnBattle`).
- Per ogni turno:
    1. Viene aggiunto un messaggio di inizio turno.
    2. Il giocatore attacca:
        - Usa una skill se ha MP e abilità disponibili (scelta casuale).
        - Altrimenti effettua un attacco base calcolato.
        - Il mostro subisce danni, può esplodere causando danni secondari al giocatore.
    3. Se il mostro è morto, si registra la sconfitta.
    4. Il mostro rigenera eventualmente salute.
    5. Il mostro attacca il giocatore, che subisce danni.
    6. Se il giocatore muore, la simulazione termina.
    7. Altrimenti si passa al turno successivo.

---

### Gestione drop

- `handleEquipDrop`:
    - Verifica se il mostro lascia un equipaggiamento.
    - Se il nuovo oggetto è migliore di quello equipaggiato, il giocatore lo equipaggia, altrimenti lo vende ottenendo oro.
- `handleItemDrop`:
    - Verifica se il mostro lascia un item.
    - Lo aggiunge all’inventario del giocatore.
    - Esso potrà essere venduto al sellEvent, ottenedo gold per poi usarli al powerUp Event (Che potenzia in modo randomico gli attributes del player)


```scala
 /** Internal reference to last fought monster (for tracking/debugging). */
  private var _lastMonster: Option[Monster] = None

  /** Returns the last monster fought, if any. */
  def lastMonster: Option[Monster] = _lastMonster

  /** Stores a monster as the last monster fought. */
  def setLastMonster(monster: Monster): Unit =
    _lastMonster = Some(monster)
  private def actionMessage(actorName: String)(targetName: String)(action: String, details: String): String =
    s"$actorName $action $targetName $details"

    def simulateFight(player: Player, monster: Monster): List[(Player, Option[Monster], String)] =
    
        // Partial application of message functions to fix actor/target names
        val playerAct = actionMessage(player.name) _
        val playerOnly = actorOnlyMessage(player.name) _
        val monsterOnly = targetOnlyMessage(monster.name) _
        
        @tailrec
        def loop(p: Player, m: Monster, acc: List[(Player, Option[Monster], String)], turn: Int)
        : List[(Player, Option[Monster], String)] =
          if !p.isAlive || m.isDead then acc.reverse
          else if turn > maxTurnBattle then
        val msg = monsterOnly("is too exhaustive, better run away.")
        ((p, Some(m), msg) :: acc).reverse
        else
        val acc1 = (p, Some(m), s"Turn $turn:") :: acc
        
        // Player's turn
        val (pAfterAttack, mAfterAttack, playerLogs) =
          if p.skills.nonEmpty && Random.nextBoolean() && p.currentMp >= 3 then
        val skill = Random.shuffle(p.skills).head
        PlayerController.useSkill(p, skill, m) match
        case (pp, mm, log) => (pp, mm, List(playerOnly(s"tried to use a skill: $log")))
        else
        val dmg = PlayerController.calculatePlayerAttack(p, m)
        val (mDamaged, explosionOpt) = MonsterController.takeDamage(m, dmg)
        val pDamaged = explosionOpt.map(PlayerController.takeDamage(p, _)).getOrElse(p)
        val logs = List(playerAct(m.name)("attacked for", s"$dmg damage.")) ++
          explosionOpt.map(e => monsterOnly(s"exploded for $e damage!"))
        (pDamaged, mDamaged, logs)
        
        val acc2 = playerLogs.reverse.foldLeft(acc1)((a, log) => (pAfterAttack, Some(mAfterAttack), log) :: a)
        
        if mAfterAttack.isDead then
          ((pAfterAttack, Some(mAfterAttack), monsterOnly("was defeated!")) :: acc2).reverse
        else
        val (mRegen, regenMsgOpt) = MonsterController.handleRegeneration(mAfterAttack)
        val (dmgToPlayer, attackMsg, mUpdated) = MonsterController.attackPlayer(mRegen, pAfterAttack)
        val pFinal = PlayerController.takeDamage(pAfterAttack, dmgToPlayer)
        
        val monsterLogs = regenMsgOpt.toList :+ attackMsg
        val acc3 = monsterLogs.reverse.foldLeft(acc2)((a, log) => (pFinal, Some(mUpdated), log) :: a)
        
        if !pFinal.isAlive then acc3.reverse
        else loop(pFinal, mUpdated, acc3, turn + 1)
        
    loop(player, monster, Nil, 1)
    def handleEquipDrop(player: Player, monster: Monster): (Player, String) =
      MonsterController.getEquipReward(monster) match
        case Some(newEquip) =>
        val slot = newEquip.slot
        player.equipment.getOrElse(slot, None) match
        case Some(old) if old.value >= newEquip.value =>
        val updated = PlayerController.addGold(player, newEquip.value)
        (updated, s"You found ${newEquip.name}, sold for ${newEquip.value} gold.")
        case _ =>
        val updated = PlayerController.equipmentOn(player, slot, newEquip)
        (updated, s"You equipped: ${newEquip.name} ($slot).")
        case None => (player, "No equipment drop.")

    def handleItemDrop(player: Player, monster: Monster): (Player, String) =
      MonsterController.getItemReward(monster) match
        case Some(item) =>
        val updated = PlayerController.addItem(player, item)
        (updated, s"You found item: ${item.name}.")
        case None => (player, "No item drop.")

  
```
---




