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
- **Game Loop (Actor System)**: Coordina l’avanzamento automatico del gioco, gestendo eventi, combattimenti, quest e progressione del personaggio tramite attori Scala/Akka.
- **Modelli (Models)**: Definisce le strutture dati principali (Player, Monster, Inventory, Equipment, Skill, Mondo, ecc.) e le relative logiche di dominio.
- **Messaggi (Messages)**: Rappresenta i messaggi scambiati tra attori per notificare eventi di gioco, aggiornamenti di stato, risultati di combattimento .
- **Gestione Eventi**: Modulo responsabile della generazione casuale di eventi (combattimento, quest, eventi speciali) e della loro risoluzione.

## **Schema Architetturale**

```mermaid
graph TD;
    UI[UI (ScalaFX)] -- invia/mostra eventi --> GameLoop[Game Loop (Actor)]
    GameLoop -- aggiorna stato --> Models[Models]
    GameLoop -- invia messaggi --> UI
    GameLoop -- genera eventi --> Eventi[Gestione Eventi]
    Eventi -- aggiorna --> Models
    Models -- notifica --> UI
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

### Game Loop (Actor System)

- Gestisce il ciclo di gioco automatico (tick periodici).
- Coordina la generazione e la risoluzione degli eventi.
- Gestisce la progressione del personaggio, combattimenti, quest e ricompense.
- Comunica con la UI e aggiorna i modelli.

### Modelli (Models)

- Rappresentano le entità principali: Player, Monster, Mondo, Inventory, Equipment, Skill.
- Incapsulano la logica di dominio (es. calcolo danni, gestione inventario, progressione livelli).

### Messaggi (Messages)

- Definiscono la comunicazione tra attori (es. inizio combattimento, aggiornamento stato, evento completato).
- Permettono la disaccoppiamento tra componenti e la gestione concorrente degli eventi.

### Gestione Eventi

- Genera eventi casuali (combattimento, quest, eventi speciali, allenamento).
- Risolve gli eventi e aggiorna lo stato del gioco di conseguenza.

## **Flusso Principale del Gioco**

1. Il Game Loop attiva periodicamente un tick.
2. Viene generato un evento casuale (combattimento, quest, ecc.).
3. L’evento viene risolto automaticamente (o con input opzionale dell’utente).
4. Il risultato aggiorna i modelli (Player, Inventory, ecc.).
5. La UI viene aggiornata per riflettere il nuovo stato.
6. Il ciclo si ripete fino alla fine della partita (morte del personaggio o chiusura del gioco).
