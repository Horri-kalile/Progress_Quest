---
layout: default
title: Design
nav_order: 5
---

# Design di Dettaglio

L’interfaccia utente di Progress Quest è progettata con ScalaFX, sfruttando layout a pannelli (BorderPane, HBox, VBox) per una chiara separazione delle sezioni principali: informazioni del personaggio, inventario, mondo, skill, missioni, diario e combat log.

La generazione del personaggio avviene in una schermata dedicata (`PlayerGenerationUi.scala`), dove l’utente può randomizzare razza, classe, comportamento e attributi tramite bottoni interattivi. Una volta confermato, si accede all’interfaccia principale (`UI.scala`), organizzata in:

- **Top**: Pannelli per personaggio, equipaggiamento e statistiche.
- **Center**: Inventario, mondo (zona attuale), skill e missione corrente.
- **Bottom**: Diario delle azioni e log dei combattimenti.

Ogni pannello è creato tramite funzioni dedicate, favorendo riuso e chiarezza del codice. Gli eventi utente sono gestiti tramite `onAction` di ScalaFX, mentre la maggior parte delle azioni di gioco avviene in modo automatico, secondo la filosofia zero-player.

## Pattern e Scelte di Design

- **MVC**: Separazione tra View (ScalaFX), Model (strutture dati e logica di dominio), Controller (gestione eventi e logica di gioco).
- **Factory/Builder**: Per la generazione casuale di attributi, razze, classi e comportamenti del personaggio.
- **Singleton/Object**: Uso di oggetti Scala per gestire costanti, utility e collezioni di mostri/eventi.

## Estendibilità e Manutenibilità

La struttura modulare e l’uso di pattern come MVC e Factory rendono il sistema facilmente estendibile: nuove funzionalità possono essere aggiunte creando nuovi pannelli o estendendo i modelli e i controller esistenti. La separazione delle responsabilità facilita la manutenzione e l’evoluzione del progetto.
