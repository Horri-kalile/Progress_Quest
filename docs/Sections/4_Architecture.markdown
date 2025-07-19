---
layout: default
title: Architettura
nav_order: 4
---

# Architettura

L’architettura di Progress Quest è progettata per massimizzare la modularità, la scalabilità e la separazione delle responsabilità tra le componenti principali del sistema. Il progetto segue un approccio a componenti, sfruttando il modello ad attori per la gestione della concorrenza e l’automazione del ciclo di gioco.

## **Panoramica dei Componenti Principali**

- **UI (ScalaFX)**: Gestisce l’interfaccia grafica, visualizza lo stato del gioco, riceve input opzionali dall’utente e mostra log, inventario, missioni, ecc.
- **Controller**: Coordina il flusso del gioco tra UI, Game Loop e Modelli. Contiene sottocontroller specializzati per player, mostri, battaglie, missioni ed eventi.
- **Game Loop**: Coordina l’avanzamento automatico del gioco, gestendo eventi, combattimenti, quest e progressione del personaggio tramite attori Scala.
- **Modelli (Models)**: Definisce le strutture dati principali (Player, Monster, Inventory, Equipment, Skill, World, ecc.) e le relative logiche di dominio.
- **Gestione Eventi**: Modulo responsabile della generazione casuale di eventi (combattimento, missioni, eventi speciali) e della loro risoluzione.

## **Schema Architetturale**

```mermaid
stateDiagram
    [*] --> Main: Game Starts
    Main --> PlayerGenerationUI: Initialize Player Setup
    PlayerGenerationUI --> GameLoopController: Player Ready
    GameLoopController --> GameUI: Start Game Loop
    GameLoopController --> Model: Update Game State
    Model --> GameLoopController: Return Updated Data
    GameLoopController --> GameUI: Render Updated State and Wait for Input if special event occurred
    GameUI --> GameLoopController: Send User Input

```

## **Descrizione dei Componenti**

### UI (ScalaFX)

- Visualizza lo stato del personaggio, inventario, log eventi, missioni e combattimenti.
- Riceve input opzionali dall’utente durante eventi speciali.
- Comunica con il Game Loop tramite messaggi e aggiornamenti.

### Controller

- Coordina il flusso del gioco tra UI, Game Loop e Modelli.
- Contiene sottocontroller specializzati: PlayerController, MonsterController, BattleController, MissionController, EventController, GameLoopController.
- Gestisce la logica di alto livello, come la transizione tra stati (esplorazione, combattimento, quest) e la risoluzione degli eventi.
- Implementa la gestione degli eventi (generazione e risoluzione), integrandosi con il Game Loop e la UI.

### Game Loop 

- Gestisce il ciclo di gioco automatico (round periodici).
- Coordina la generazione e la risoluzione degli eventi.
- Gestisce la progressione del personaggio, combattimenti, missioni e ricompense.
- Comunica con la UI e aggiorna i modelli.

### Modelli (Models)

- Rappresentano le entità principali: Player, Monster, Mondo, Inventory, Equipment, Skill.
- Incapsulano la logica di dominio (es. calcolo danni, gestione inventario, progressione livelli).


### Gestione Eventi

- Genera eventi casuali (combattimento, missioni, eventi speciali, allenamento, etc...).
- Risolve gli eventi e aggiorna lo stato del gioco di conseguenza.

## **Flusso Principale del Gioco**

1. Il Game Loop attiva il timer degli eventi.
2. Viene generato un evento casuale.
3. L’evento viene risolto automaticamente (o con input opzionale dell’utente).
4. Il risultato aggiorna i modelli (Player, Inventory, ecc.).
5. La UI viene aggiornata per riflettere il nuovo stato.
6. Il ciclo si ripete fino alla fine della partita (morte del personaggio o chiusura del gioco).
