---
title: Intisar Bel Meddeb
nav_order: 1
parent: Implementazione
---
# Intisar Bel Meddeb




desc

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

### `calculatePlayerAttack(player, monster)`
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
## Altre funzionalità supportate

- `addItem`, `removeItem`: gestione dell’inventario e delle quantità
- `addGold`, `spendGold`: modifica del bilancio d’oro
- `addSkill`: apprendimento di skill uniche o potenziamento di quelle già presenti
- `equipmentOn` / `equipmentOff`: gestione dell’equipaggiamento per slot specifico
- `changeWorld`: modifica della zona del mondo in cui si trova il giocatore
- `levelUp` / `levelDown`: modifiche a HP/MP e statistiche con randomness controllato
- `playerInjured`, `stealRandomItem`, `sellRandomItem`: eventi casuali legati alla perdita o alla vendita di oggetti
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
Gestisce la rigenerazione automatica per i mostri con comportamento `Regenerating`.

Se attiva e il mostro è vivo, cura una quantità casuale tra: ```text 1 e 2 * livello```

  **Messaggio prodotto**: notifica testuale del recupero HP.
Comportamento utile per aumentare la difficoltà in modo dinamico.

## Altre funzionalità supportate

- `heal`: funzione diretta per rigenerare HP, usata internamente o da eventi di supporto.

- `getEquipReward`, `getItemReward`: restituiscono oggetti o equipaggiamenti come ricompensa per la sconfitta del mostro.

- `getExpReward`, `getGoldReward`: calcolano le ricompense in esperienza e oro, basate sul profilo del mostro.

- `getMonsterDefenceAndWeakness`: funzione di supporto usata dal `PlayerController` per i calcoli di danno.

- `getRandomMonsterForZone`: genera un mostro adatto a livello e zona del giocatore, bilanciando casualità e difficoltà.  
  Supporta la generazione di mostri rari o potenziati tramite:    ```text RandomFunctions.tryGenerateStrongMonster() ```

## Aspetti Implementativi

- **Encapsulamento dei comportamenti speciali** (`Explosive`, `Berserk`, `Regenerating`)  
  direttamente nei metodi core del controller, senza disperdere la logica nel modello.

- **Separazione completa** tra descrizione (`describe`) e logica di gioco, debugging e interfacce utente.

- **Ricompense modulari** accessibili tramite metodi pubblici, indipendenti dalla logica di combattimento.

- **Probabilità e valori configurabili** esternamente tramite GameConfig, migliorando la flessibilità nel tuning del gioco.





