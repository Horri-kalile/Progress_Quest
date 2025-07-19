---
layout: default
title: Architettura
nav_order: 4
---

# Architettura

L’architettura di Progress Quest è progettata per massimizzare la modularità, la scalabilità e la separazione delle responsabilità tra le componenti principali del sistema.

## **Panoramica dei Componenti Principali**

- **GameUI (ScalaFX)**: Gestisce l’interfaccia grafica, visualizza lo stato del gioco, riceve input opzionali dall’utente e mostra log, inventario, missioni, ecc.
- **Controller**: Coordina il flusso del gioco tra UI, Game Loop e Modelli. Contiene sottocontroller specializzati per player, mostri, battaglie, missioni ed eventi.
- **GameLoopController**: Coordina l’avanzamento automatico del gioco, gestendo eventi, richiamando controller dedicati in base agli eventi e aggiorna al view progressione del player.
- **Modelli (Models)**: Definisce le strutture dati principali (Player, Monster, Inventory, Equipment, Skill, World, ecc.) e le relative logiche di dominio.
- **Gestione Eventi**: Modulo responsabile della generazione casuale di eventi (combattimento, missioni, eventi speciali, etc...) e della loro risoluzione.

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

### Main
- Entry point del programma.

### PlayerGenerationUi
- Creazione del player per poi entrare in gioco.

### GameUi

- Visualizza lo stato del personaggio, inventario, log eventi, missioni, skill, monster, stats, equipments, eventi e combattimenti.
- Riceve input opzionali dall’utente durante eventi speciali.
- Comunica con il GameLoopController per gli aggiornamenti e richieste di interazione.

### GameLoopController

- Il GameLoopController coordina il flusso del gioco tra GameUi e Model.
- Contiene sottocontroller specializzati: PlayerController, MonsterController, CombattController, MissionController, EventController.
- Questi sotticontroller gestiscono le logiche di alto livello, come la transizione tra stati (Player levelUp, Monster attack etc..) e la risoluzione degli eventi.


### Model

- Rappresentano le entità principali: Player, Monster, World, Inventory, Equipment, Skill, Events, Identity, etc...


## **Flusso Principale del Gioco**

1. Il Game Loop attiva il timer degli eventi.
2. Viene generato un evento casuale.
3. L’evento viene risolto automaticamente dal controller(o con input opzionale dell’utente).
4. Il risultato aggiorna i modelli (Player, Inventory, ecc.).
5. La UI viene aggiornata per riflettere il nuovo stato.
6. Il ciclo si ripete fino alla fine della partita (morte del personaggio o chiusura del gioco).
