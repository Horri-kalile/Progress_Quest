---
title: Intisar Bel Meddeb
nav_order: 1
parent: Implementazione
---
# Intisar Bel Meddeb


## Controller

Durante l’implementazione del modulo `controller`, mi sono occupata principalmente della logica di combattimento del gioco, della gestione degli eventi e delle missioni, implementando direttamente i controller `PlayerController`, `MonsterController`, `EventController`, `MissionController` e collaborando attivamente allo sviluppo di `CombatController` e `GameController`.

---


### 1 PlayerController

Player controller svolge un ruolo centrale nel flusso del gioco, in quanto si occupa dell'intera manipolazione dello stato del giocatore: combattimento, inventario, equipaggiamento, progressione di livello e gestione delle skill.



## Aspetti implementativi

### Gestione del combattimento
Il metodo `calculatePlayerAttack` calcola i danni in base alla forza del giocatore, agli equipaggiamenti attivi e alle debolezze del nemico. Questo calcolo tiene conto del comportamento del giocatore per modificare dinamicamente l’output tramite `onBattleDamage`.

### Progressione e comportamento
`gainXP` è implementato ricorsivamente con `levelUpLoop` per gestire il caso in cui un giocatore superi più soglie di livello con una singola azione. Comportamenti specifici (es. `Heal`, `FastLeveling`) sono supportati via `behaviorType`.

### Gestione delle risorse
Il controller consente di:
- aggiungere o rimuovere oggetti con `addItem` e `removeItem`
- vendere o perdere oggetti con logiche casuali (`sellRandomItem`, `stealRandomItem`)
- accumulare o spendere oro con `addGold` e `spendGold`

### Equipaggiamento
Tramite `equipmentOn` e `equipmentOff`, il giocatore può equipaggiare o rimuovere elementi nei rispettivi slot. I bonus derivanti dagli equipaggiamenti influenzano direttamente statistiche come forza o intelligenza.

### Skill e mana
L’uso delle skill è gestito da `useSkill`, il quale verifica se il giocatore dispone del mana necessario (`currentMp`) e applica l’effetto (`Physical`, `Magic`, `Healing`) influenzando sia il nemico che il giocatore. Il modulo garantisce coerenza tra le capacità e lo stato del personaggio.

### Gestione del livello
Oltre al normale `levelUp`, è stato implementato anche un `levelDown` con degradazione proporzionale delle statistiche (via `powerDownAttributes`). In aggiunta, il sistema `maybeLearnSkill` permette di apprendere nuove skill in modo probabilistico, basato sulla statistica `lucky` e la configurazione esterna (`GameConfig`).

### Funzionalità di supporto
Metodi come `playerInjured` sono utili per scenari di eventi negativi (es. trappole), dimezzando HP e MP. Inoltre `changeWorld` consente la transizione tra zone nel mondo di gioco.

