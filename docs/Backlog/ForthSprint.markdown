---
layout: default
title: 4° sprint
nav_order: 12
---

# Quarto Sprint – 04/07/2025

Nel **quarto sprint**, il team si è concentrato sul consolidamento delle funzionalità implementate, sull’aggiunta di elementi finali al gameplay, e sul miglioramento dell’esperienza utente in vista del completamento del progetto.  
**Durata della sessione:** 3 ore

---

## Obiettivi dello Sprint

- Ristrutturazione della **GameUI** per un’interfaccia più chiara e navigabile
- Estensione dei comportamenti `MonsterType` e diversificazione nemici
- Integrazione completa tra eventi, combattimento e missioni
- Aggiunta di ulteriori eventi dinamici e possibilità di scelta
- Completamento finale dei **controller**
- Fase di **refactoring e cleanup** del codice

---

## Pianificazione & Comunicazione

- Verifiche incrociate per evitare sovrapposizioni
- Definizione di una lista delle priorità da seguire

**Scadenza sprint:** 14/07/2025

---

## Assegnazione dei Task

### Kalile
- Miglioramenti visivi all’interfaccia di `SpecialEventDialog`
- Fix di bug legati alla visualizzazione delle scelte evento
- Testing dei flussi completi del loop di gioco
- Fase di **refactoring e cleanup** del codice

### Guo
- Introduzione e bilanciamento dei `MonsterType` nei combattimenti
- Refactoring e cleanup del modulo `Player`
- Aggiunta di ulteriori eventi dinamici e possibilità di scelta
- Ottimizzazione del modulo utils

### Intisar
- Debugging di problemi logici su eventi
- Completamento finale dei **controller**
- Fase di **refactoring e cleanup** del codice
- Ristrutturazione di GameUI con elementi più chiari

---

## Sprint Review – 15/07/2025

### Risultati Raggiunti:

- Interfaccia utente rifinita e usabile
- Eventi speciali migliorati con pop-up e scelte leggibili
- Statistiche dei nemici differenziati in base al monsterType
- Game loop robusto e coerente tra missioni, reward e stato player
- Refactoring avviato con miglioramenti a modularità e leggibilità
- SpecialEventDialog migliorati
- Test completati su tutti i controller principali

---

## Ultimi Giorni – Fino al 19/07/2025

Negli ultimi giorni prima della consegna, il team ha lavorato intensamente su debugging e preparazione del materiale finale.

### Attività principali:

- Debugging approfondito su edge case legati a eventi, drop e combattimenti.
- È emerso un problema critico: il file JAR, che nelle fasi precedenti risultava funzionante, non era più eseguibile. L’errore era legato alla configurazione del javaFx in module path e impostazione nel `build.sbt` e all'inclusione di coverage, che durante il release doveva essere disattivato.
- La risoluzione ha richiesto un giorno aggiuntivo, con numerosi test e modifiche per garantire che il JAR finale fosse eseguibile da terminale.
- Completamento e revisione del report, con aggiornamenti finali nelle sezioni su architettura, testing e sviluppo.
