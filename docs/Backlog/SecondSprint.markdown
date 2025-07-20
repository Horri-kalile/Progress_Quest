---
layout: default
title: 2° sprint
nav_order: 10
---
# Secondo Sprint – 12/06/2025

Il team ha avviato il **secondo sprint** con l’obiettivo di estendere la base logica del progetto e avvicinarsi alla prima versione eseguibile.  
**Durata della sessione:** 2 ore

---

## Obiettivi dello Sprint

- Consolidamento delle strutture dati introdotte nello sprint precedente.
- Prototipazione gameloopController
- Implementazione delle **prime logiche di interazione** tra player e monster.
- Introduzione di un **controller per il combattimento** con sistema base di reward.
- Integrazione del **pattern Strategy** per gestire comportamenti dinamici dei giocatori.
- Prototipazione iniziale della `PlayerSelectionUi` e **gestione eventi base**.
- Stesura dei **primi test automatici** per moduli chiave.

---

## Pianificazione & Comunicazione

- Coordinamento online con confronto diretto sulle strutture da sviluppare.
- Comunicazione continua tramite short-meeting e sessioni brevi focalizzate su refactoring e test.

**Scadenza sprint:** 21/06/2025

---

## Assegnazione dei Task

### Kalile
- Sviluppo avanzato dei moduli per mostri
- Prototipazione GameloopController
- Ottimizzazione di GameUi

### Guo
- Completamento del modello `Player` e sottostrutture: `Item`, `Equipment`, `Attributes`, `Identity`, `Behavior`, `Skill`, effettuando miglioramenti.
- Integrazione del **sistema strategico** per la fase di applicazione del Behavior
- Prototipazione iniziale della **PlayerSelectionUi**
- Supporto all’implementazione del modulo **GameEvent**
- Collaborazione e implementazione di **combatController**


### Intisar
- Collaborazione e implementazione di **combatController**
- Implementazione del modulo **GameEvent**
- Implementazione delle **prime logiche di interazione** tra player e monster.

---

## Sprint Review – 22/06/2025

### Risultati Raggiunti:

- Modello `Player` completato.
- Modello `Monster` completato.
- **Comportamenti dinamici** tramite Strategy di behavior integrati nel gioco
- Sistema di combattimento base funzionante con gestione del danno e delle ricompense
- Prototipo iniziale della UI testuale completato
- Prototipo gameloopController completato.
- Prime funzioni di test automatici implementate con ScalaTest.

---

## Prossimi Step

- Introduzione completa degli **Eventi di gioco (GameEvent)**
- Affinamento del **game loop**
- Integrazione di nuove azioni e missioni tramite eventi
- Estensione della copertura dei **test automatici**
- Costruzione dei meccanismi di **validazione e attuazione** delle decisioni
