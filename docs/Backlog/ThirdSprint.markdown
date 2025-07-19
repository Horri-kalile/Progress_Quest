---
layout: default
title: 3° sprint
nav_order: 11
---
# Terzo Sprint – 23/06/2025

Il team ha affrontato il **terzo sprint** con l’obiettivo di completare le funzionalità principali del gioco e ottenere una prima versione **completamente eseguibile** del loop di gioco.  
**Durata della sessione:** 3 ore

---

## Obiettivi dello Sprint

- Completamento e integrazione del modulo **GameEvent**
- GameLoopController funzionante
- GameUi che mostra informazioni sensati
- Collegamento tra interfaccia utente e logica di gioco
- Versione runnabile del gioco.
- Espansione del sistema missioni
- Implementazione del combatController avanzato.
- PlayerGenerationUi con azioni corretti
- Copertura base tramite **test automatici** sui controller ed eventi

---

## Pianificazione & Comunicazione

- Sincronizzazione costante tramite repository GitHub
- Condivisione continua del lavoro.
- Brevi riunioni online focalizzate su bugfix e coordinamento delle merge.

**Scadenza sprint:** 02/07/2025

---

## Assegnazione dei Task

### Kalile
- Completamento di GameUi
- Creazione di specialEventDialog per gestire pop up e interazione per gli eventi speciali.
- Completamento di gameLoopController
- Integrazione degli GameEvent nel gameLoop
- Collegamento tra interfaccia utente e logica di gioco
- Release della versione runnable del gioco


### Guo
- Costruzione e completamento del **modulo GameEvent**, collaborazione con Intissar
- Validazione di eventi e aggiornamento stato player post-evento
- Implementazione combattController avanzato
- Collaborazione per GameUi
- Collegamento tra interfaccia utente e logica di gioco

### Intisar
- Testing e debugging intensivo sui controller di supporto e `GameEvent`
- Implementazione e collaborazione sul modulo gameEvent
- Coordinamento con gli altri controller per il game loop
- Collegamento tra interfaccia utente e logica di gioco


---

## Sprint Review – 03/07/2025

### Risultati Raggiunti:

- Loop di gioco funzionante
- Integrazione tra `Player`, `CombatController`, `GameEvent` e `gameUI`
- Azioni speciali e missioni implementate con aggiornamento stato coerente
- Completamento del modulo `SpecialEventDialog` per eventi inaspettati
- Copertura base dei test automatizzati su eventi e controller
- GameLoop finito
- Release
- GameUi e PlayerGenerationUi completati

---

## Prossimi Step

- Rifinitura dell’esperienza utente e messaggi di gioco
- Maggiore modularizzazione dei controller
- Integrazione di monsterType.
- Aggiunta di ulteriori eventi dinamici e possibilità di scelta
- Estensione della copertura dei test.
- Preparazione alla fase di **refactoring finale e ottimizzazione**
- Ristrutturazione di gameUi in versione piacevole

