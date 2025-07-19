---
layout: default
title: Design
nav_order: 5
---

# Design di Dettaglio

L’interfaccia utente di Progress Quest è progettata con ScalaFX, sfruttando layout a pannelli (BorderPane, HBox, VBox) per una chiara separazione delle sezioni principali: informazioni del player, inventary, equipment, monster, world, skill, mission, event log e combat log.

La generazione del personaggio avviene in una schermata dedicata (`PlayerGenerationUi.scala`), dove l’utente può randomizzare race, classe, comportamento e attributi tramite bottoni interattivi. Una volta confermato, si accede all’interfaccia principale (`GameUI.scala`), organizzata in:

- **Top**: Pannelli per personaggio, equipaggiamento e statistiche.
- **Center**: Inventario, mondo (zona attuale), skill e missione corrente.
- **Bottom**: Diario delle azioni e log dei combattimenti.

Ogni pannello è creato tramite funzioni dedicate, favorendo riuso e chiarezza del codice. Gli eventi utente sono gestiti tramite specialDialog, mentre la maggior parte delle azioni di gioco avviene in modo automatico, secondo la filosofia zero-player.

## Pattern e Scelte di Design

- **MVC**: Separazione tra View (ScalaFX), Model (strutture dati e logica di dominio), Controller (gestione eventi e logica di gioco).
- **Factory/Builder**: Per la generazione casuale di attributes, race, class, behavior, player, monster, item, equipment, skill. (Letti da un prefab di file json)
- **Singleton/Object**: Uso di oggetti Scala per gestire costanti, utility e collezioni degli eventi.

## Diagramma del modello Player
![Player Domain Model](../assets/images/playerModelDiagram.png)

Questo diagramma illustra il modello di dominio principale per l'entità **Player** nel gioco, con particolare attenzione all’esternalizzazione del behavior del giocatore. Vengono inoltre evidenziate le relazioni tra il player e i modelli correlati come identity, attributes, originZone , equipments, skills, items e mission.

---

## Dominio Principale: Player

La classe **Player** rappresenta l'entità principale con le seguenti proprietà chiave:

- **name**: Nome del giocatore.
- **level, exp**: Livello attuale del giocatore e punti esperienza.
- **hp, mp, currentHp, currentMp**: Punti salute e mana (totali e attuali).
- **gold**: Quantità di oro posseduta dal giocatore.
- **baseAttributes**: Statistiche base come forza, destrezza, intelligenza, costituzione, saggezza e fortuna.
- **behaviorType**: Enumerazione che indica la strategia comportamentale del giocatore.
- **identity**: Definisce la razza e la classe del giocatore.
- **inventory**: Collezione di oggetti posseduti dal giocatore.
- **equipment**: Mappatura degli slot di equipaggiamento agli oggetti indossati.
- **skills**: Lista delle abilità del giocatore.
- **missions**: Lista delle missioni assegnate al giocatore.
- **currentZone**: Zona corrente in cui si trova il giocatore nel mondo di gioco.

### Classi di Supporto

- **Identity**: Contiene dettagli come race e class del giocatore.
- **Attributes**: Contiene le statistiche base che influenzano il gameplay.
- **BehaviorType**: Enumerazione dei possibili tipi di comportamento del giocatore (es. Aggressivo, Difensivo, Crescita Rapida, ecc.).

---

## Sistema di behavior

Il behavior del giocatore è esternalizzato tramite l'interfaccia **Strategy** e le sue implementazioni concrete. Questo pattern permette di assegnare dinamicamente diversi comportamenti che influenzano meccaniche di gioco come il danno inflitto, il danno subito, il guadagno di esperienza e i bonus applicati all’avvio del gioco.

- **Interfaccia Strategy**: Definisce metodi da implementare per eventi di gameplay:
    - `onGameStart(player)`
    - `onBattleDamage(player, damage)`
    - `onBattleEnd(value)`
    - `onDamageTaken(player, damage)`

- Le classi concrete (es. AggressiveStrategy, DefensiveStrategy) implementano queste funzioni con la logica specifica del comportamento.
- La classe **BehaviorResolver** si occupa di risolvere il tipo di `BehaviorType` e restituire la strategia corrispondente.

---

## Oggetti ed Equipaggiamento

- **Item**: Rappresenta gli oggetti nell’inventario con attributi come nome, rarità e valore in oro.
- **Equipment**: Oggetti che possono essere equipaggiati e che forniscono bonus agli attributi.
- **EquipmentSlot**: Enumerazione degli slot di equipaggiamento disponibili (es. head, weapon, shield).

---

## Abilità e Missioni

- **Skill**: Abilità del giocatore con proprietà quali costo in mana, livello di potenza e tipo di effetto.
- **Mission**: Missioni assegnati al giocatore.

---

## Relazioni

- Il **Player** aggrega diversi componenti: Identity, Attributes, Skills, Missions, Items ed Equipment.
- L’**Equipment** influisce sugli attributi del giocatore tramite i bonus statistici.
- Il behavior è disaccoppiato dalla classe Player ed è risolto esternamente tramite il `BehaviorResolver`.
- Questa architettura modulare supporta estendibilità e una chiara separazione delle responsabilità.

---


