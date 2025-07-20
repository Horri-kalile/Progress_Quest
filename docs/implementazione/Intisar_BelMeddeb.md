---
layout: default
title: Intisar Bel Meddeb
nav_order: 1
parent: Implementazione
---
# Intisar Bel Meddeb






Il mio contributo al progetto si è concentrato sull’implementazione dei controller principali (`PlayerController`, `MonsterController`, `MissionController`, `EventController`).

Ho anche collaborato allo sviluppo dei moduli `GameEventModule` e `Mission`, contribuendo alla gestione degli eventi e alla generazione delle missioni.

Il lavoro è stato svolto seguendo principi di **modularità**, **immutabilità** e **separazione delle responsabilità**.


---
# Controller: PlayerController

## Descrizione
Il `PlayerController` gestisce l'intera logica legata all'interazione del giocatore con il mondo di gioco, i combattimenti, l’equipaggiamento, l’inventario e la progressione del personaggio.  
Le sue responsabilità spaziano dalla gestione dell’esperienza e del livello, fino all'applicazione di danni, guarigione, acquisizione di oggetti e utilizzo delle abilità.

---

## Pattern principali

- **Modularità**  
  Ogni funzione è immutabile e restituisce una nuova istanza aggiornata di `Player`, rispettando i principi della programmazione funzionale.

- **Tail Recursion**  
  Utilizzata per gestire il loop di level-up evitando stack overflow (`levelUpLoop`).

- **Pattern Matching**  
  Usato per trattare comportamenti dinamici legati alle strategie (`behaviorType`) e gestire abilità diverse (`useSkill`).

- **Encapsulation**  
  Le operazioni critiche (come l'incremento dell'inventario o la modifica dell’oro) sono incapsulate in metodi autonomi, migliorando la riusabilità.


## Comportamento dettagliato

### calculatePlayerAttack(player, monster)
Calcola il danno inflitto da un giocatore a un mostro combinando i seguenti elementi:

- **Forza base** del giocatore  
- **Bonus da equipaggiamento** (somma degli slot)  
- **Livello** del giocatore  
- **Difesa** e **debolezza** del mostro (fisica)  

#### Formula finale:
  ```text
  ((strength + equipmentBonus + level - defense) * physicalMultiplier).max(1)

```
### Aspetto tecnico
Il metodo integra il modificatore di danno `onBattleDamage` definito all'interno del comportamento (`Behavior`) del giocatore, rendendo il sistema flessibile per differenti stili di gioco.

### `gainXP(player, xpGained)`
Gestisce l’acquisizione di esperienza e il potenziale level-up del personaggio.  
Supporta due comportamenti speciali:

- **Heal**: cura al termine della battaglia  
- **FastLeveling**: bonus esperienza in base al comportamento  

Utilizza ricorsione **tail-recursive** per gestire level-up multipli consecutivi.

#### Step principali:

1. Calcolo **bonus da comportamento**
2. Applicazione della **cura**
3. Ricorsione tramite `levelUpLoop`
4. Tentativo di apprendimento **skill** con **probabilità configurabile**
### `useSkill(player, skill, monster)`
Consente l’uso di una **Skill attiva**, sottraendo il **mana** richiesto e applicando l'effetto della skill.

#### Tipi di effetto supportati:

- **Physical**: infligge danni fisici → basati su forza + equipaggiamento
- **Magic**: infligge danni magici → basati su intelligenza
- **Healing**: cura il giocatore → basato su saggezza

#### Output: una tripla (giocatore aggiornato, mostro aggiornato, messaggio descrittivo)
Usa pattern **matching** per distinguere i comportamenti in modo chiaro e tip-safe.

## Aspetti Implementativi

- **Separazione netta tra effetti collaterali** e aggiornamenti allo stato del giocatore.
- **Nessuna mutazione diretta**: ogni operazione restituisce una nuova istanza immutabile.
- **Probabilità configurabili** tramite `GameConfig` per l’apprendimento di nuove skill.
- **Supporto a stili di gioco multipli** tramite il pattern `BehaviorType`, iniettato al momento della creazione del personaggio.
- **Gestione completa dell’esperienza utente** in caso di eventi casuali (*furto*, *vendita*, *infortunio*), con messaggi testuali immediati.

# Controller: MonsterController

## Descrizione
Il `MonsterController` è responsabile della **logica interna** che regola:

- Il **comportamento dei mostri** nel sistema di combattimento
- La **generazione dinamica** degli avversari
- Il **calcolo dei danni**
- La gestione delle **abilità speciali** (es. *berserk*, *esplosione*, *rigenerazione*)
- La gestione delle **ricompense** ottenibili alla loro sconfitta
## Pattern principali

- **Comportamenti condizionali**  
  Uso estensivo di **pattern matching** per modellare comportamenti speciali dei mostri  
  (*Explosive*, *Berserk*, *Regenerating*).

- **Funzioni pure e immutabilità**  
  Ogni operazione restituisce una **nuova istanza** di `Monster`, senza modificare lo stato esistente.

- **Randomizzazione controllata**  
  Utilizzo di `Random` per simulare eventi **probabilistici**, come **danni bonus** o **rigenerazioni**,  
  con soglie definite tramite configurazioni (`GameConfig`, `RandomFunctions`).

- **Composizione modulare**  
  Logica di **combattimento** e **generazione dei mostri** separata da `MonstersFactory` e modelli dedicati.

## Comportamento dettagliato

### `attackPlayer(monster, player)`
Gestisce l’attacco di un mostro contro un giocatore, calcolando:

- Danno base=  ```text monster.attack + player.level * 2```
- Possibilità di schivata:  ```text dodgeChance = min(player.dexterity * dodgeBonus, maxDodgeChance)```
- Se il mostro è in stato berserk, viene applicato un bonus al danno e un’autolesione come effetto collaterale

#### Output: una tripla (danno inflitto, messaggio descrittivo, mostro aggiornato)
Comportamenti speciali gestiti tramite rami separati e messaggi testuali dinamici.

### `takeDamage(monster, damage)`
Applica il **danno** al mostro e verifica se il tipo di comportamento è **Explosive**.  
In tal caso, se il mostro **muore**, restituisce anche il **danno da esplosione** da infliggere al giocatore.

#### Aspetto tecnico:

- Restituisce una tupla
  ```text (mostro aggiornato, danno esplosivo opzionale)```
- Garantisce un comportamento implicito ma letale se trascurato dal lato player


### `handleRegeneration(monster)`
Gestisce la rigenerazione automatica per i mostri con comportamento `Regenerating`. Se attiva e il mostro è vivo, cura una quantità casuale tra: ```text 1 e 2 * livello```

**Messaggio prodotto**: notifica testuale del recupero HP.

Comportamento utile per aumentare la difficoltà in modo dinamico.



## Aspetti Implementativi

- **Encapsulamento dei comportamenti speciali** (`Explosive`, `Berserk`, `Regenerating`)  
  direttamente nei metodi core del controller, senza disperdere la logica nel modello.

- **Separazione completa** tra descrizione (`describe`) e logica di gioco, debugging e interfacce utente.

- **Ricompense modulari** accessibili tramite metodi pubblici, indipendenti dalla logica di combattimento.

- **Probabilità e valori configurabili** esternamente tramite GameConfig, migliorando la flessibilità nel tuning del gioco.

# Controller: MissionController

## Descrizione
Il MissionController gestisce la logica di generazione, assegnazione, avanzamento e completamento delle missioni nel gioco. Agisce come intermediario tra il `Player` e il `MissionFactory`, garantendo che ogni missione sia coerente con il profilo del giocatore (livello e fortuna) e applicando correttamente le ricompense previste al completamento.

## Pattern principali

- **Contesto dinamico:** la generazione di missioni avviene sulla base degli attributi del giocatore (`lucky`, `level`), offrendo difficoltà e ricompense bilanciate

- **Manipolazione immutabile dello stato:**  tutte le operazioni restituiscono un nuovo `Player`, con missioni aggiornate o premi applicati.
 

- **Pipeline di ricompense:** in caso di completamento, le ricompense vengono applicate in sequenza (gold, XP, item) tramite i metodi del `PlayerController`.
 
- **Separazione delle responsabilità:**  la logica di generazione è delegata a `MissionFactory`, mentre il controller si occupa solo dell'interazione con il `Player`.

## Comportamento dettagliato

### `createRandomMission(player)`
Crea una nuova missione basata su:
- **Fortuna** (`player.attributes.lucky`): influenza la qualità delle ricompense.
 
- **Livello** (`player.level`): influisce sulla difficoltà dell’obiettivo.

La generazione è gestita tramite `MissionFactory`, che assicura varietà e adeguatezza della missione.

### `addMission(player, mission)`
Aggiunge una missione alla lista attuale del giocatore.
La lista delle missioni non ha limiti imposti, ma la gestione della saturazione può essere implementata a livello di gameplay.

-  Output `Player` aggiornato con la missione in coda.
-  Metodo utile per inserire missioni provenienti da eventi, NPC o reward.

 ### `progressMission(player, mission)`
Metodo centrale della logica missione. Gestisce:

1. **Avanzamento** della missione  (`progressed()`).
2. **Verifica** dello stato completato (`isCompleted`).
3. **Applicazione** ricompense, nel seguente ordine:
 
 - **Oro**:`addGold`
 - **Esperienza**: `gainXP`
 - **Oggetti opzionali**: `addItem` (se `Some(item)`)

Se la missione è completata, viene **rimossa dalla lista attiva**. L’intera operazione è trasparente e atomica dal punto di vista del giocatore.

## Aspetti Implementativi

- **Design reattivo**: il controller reagisce dinamicamente all’evoluzione della missione, senza mutare direttamente i dati, ma creando nuove istanze coerenti.

- **Architettura modulare**: `MissionController` non si occupa della logica interna delle missioni (progresso, completamento, reward), ma si appoggia alle API esposte da `Mission` e `MissionFactory`.

- **Scalabilità**: l’approccio utilizzato permette di estendere facilmente con nuove tipologie di missioni o ricompense, mantenendo l'interfaccia pubblica invariata.

- **Efficienza**: l’uso di `map` e `filterNot` sulla lista delle missioni garantisce operazioni precise e performanti, anche con missioni multiple attive  

# Controller: EventController

## Descrizione

L’`EventController` si occupa dell’esecuzione degli eventi di gioco dinamici per un determinato `Player`, sulla base della tipologia dell’evento. Agisce come livello di coordinamento tra il giocatore e il modulo `GameEventFactory`, delegando a quest’ultimo la logica interna di esecuzione e garantendo un’interfaccia coerente verso il resto del sistema.

## Pattern principali

- **Delegation**: la logica dettagliata dell’evento (combattimento, missione, allenamento...) è completamente incapsulata in `GameEventFactory`.

- **Modularità**: aggiungere nuove tipologie di eventi è possibile semplicemente estendendo il tipo  `EventType` e aggiornando il `GameEventFactory`, senza modificare il controller.

- **Design orientato agli effetti**: l’output dell’evento include modifiche al `Player`, messaggi descrittivi, ed eventualmente un `Monster` (solo in caso di combattimento o eventi che ne     richiedano la comparsa).

- **Tripla restituzione**: l'interfaccia restituisce una tupla tripla per garantire separazione tra stato del gioco (Player), messaggistica e interazioni extra (es. mostro generato).
 
## Comportamento dettagliato

### `runEvent(eventType: EventType, player: Player)`
Metodo principale ed unico del controller.



### Input:

- `eventType`: specifica la tipologia dell’evento  da eseguire (es. `Fight`, `Mission`, `Training`, ecc.).

- `player`: stato attuale del giocatore che partecipa all’evento.

### Output:

- `player`: giocatore aggiornato dopo l’evento.

- `List[String]`: messaggi descrittivi per il log o l’interfaccia.

- `Option[Monster]`: mostro eventualmente incontrato (solo per eventi di combattimento).

Questo metodo rappresenta un punto d’ingresso uniforme per l’interazione con qualsiasi evento, permettendo di mantenere il sistema modulare e aperto all’estensione.


## Aspetti Implementativi

- L’implementazione utilizza direttamente `GameEventFactory.executeEvent`, funzione che si occupa dell’intera orchestrazione dell’evento scelto.

- Non viene effettuato alcun controllo specifico a livello di controller, rendendolo un **thin controller** con responsabilità di puro dispatch.

- La decisione di ritornare un `Option[Monster]` anche per eventi non combattivi permette di mantenere un'interfaccia coerente, semplificando l'integrazione lato `GameController`.

- L’approccio facilita anche l’uso nel testing: essendo privo di stato e dipendenze interne, il comportamento è completamente deterministico in funzione del `GameEventFactory`.

# Module: GameEventModule

## Descrizione:
`GameEventModule` gestisce tutti gli eventi principali del gioco, come combattimenti, missioni, cambi di zona, potenziamenti o eventi speciali. Ogni evento è definito come effetto puro: modifica lo stato del `Player`, produce messaggi e opzionalmente coinvolge un `Monster`.

## Componenti principali:

- `EventType`: Enum dei tipi di eventi (fight, mission, restore, training, craft, magic, ecc.).

- `GameEvent`: Trait che rappresenta un evento generico con metodo `action`.

- `GameEventFactory`: Risolve e gestisce l’esecuzione degli eventi a partire dal tipo (`EventType`)

## Eventi principali:

- **FightEvent**: Gestisce le ricompense dopo un combattimento (XP, oro, loot). Se il mostro non è morto, l’evento termina senza premi.

- **MissionEvent**: Se ci sono missioni attive, ne progredisce una. Altrimenti, ne assegna una nuova.

- **RestoreEvent**: Ripristina completamente HP/MP del giocatore.

- **TrainingEvent**: Fornisce EXP in base al livello del giocatore.

- **SellEvent / PowerUpEvent**: Vendita di oggetti o potenziamento statistiche, se il giocatore ha abbastanza oro.

- **CraftEvent / MagicEvent**: Permette di forgiare o potenziare equipaggiamenti/abilità.

- **SpecialEvent**: Evento casuale tra 8 scenari possibili (*trappole, dungeon, ladri, mostri...*).

- **GameOverEvent**: Termina il gioco impostando `HP = 0`.

## Caratteristiche tecniche:

- Architettura modulare e ad eventi.

- Tutti gli eventi sono **immutabili** e restituendo un nuovo stato del `Player`.

- I messaggi sono **descrittivi** e utili per la UI.

- Uso di Option[Monster] solo quando l’evento lo richiede.

# Module: Mission

## Descrizione:
Il modulo Mission gestisce il sistema delle missioni del gioco, dalla definizione di missioni statiche (`MissionData`), alla generazione dinamica (`MissionFactory`), fino alla rappresentazione e progressione delle missioni assegnate al giocatore (`Mission`).


## Componenti principali:

- `MissionData`: Rappresenta un template di missione (*nome, descrizione*). I dati sono caricati da file tramite `MissionLoader`.

- `Missions`: Contenitore per una lista di `MissionData`.

- `Mission`: Istanza concreta assegnata al giocatore. Ogni missione ha:
  - `id` univoco  
  - `progression` e `goal` per monitorare lo stato  
  - Ricompense: `rewardExp`, `rewardGold`, `rewardItem` (`Option`)

## Funzionalità:

- `progressed()`: Avanza la progressione della missione di 1 passo, se non già completata.

- `isCompleted`: Verifica se la missione è terminata (`progression >= goal`).

- `complete`: Imposta direttamente la missione come completata (utile per test/debug).

## MissionFactory:

- **`randomMission(playerLucky, playerLevel)`**

   - Seleziona una missione casuale da `MissionLoader`.

    - Calcola XP e oro in base al livello del giocatore.

     - Aggiunge eventualmente un oggetto (`Item`) in base alla fortuna**.

     - Genera una missione con obiettivo randomico (`goal ∈ [1, 3]`).
 
## Caratteristiche tecniche:

- Design **immutabile** per tutte le entità missione.

- Separazione tra **template statici** (file) e **istanze dinamiche** (gioco).

- Le ricompense sono **scalabili** e **probabilistiche** (oggetto opzionale basato sulla fortuna).

- Sistema adatto sia a missioni testuali che meccaniche (es. uccidi X mostri, trova oggetto...).



