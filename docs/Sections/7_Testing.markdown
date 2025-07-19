---
layout: default
title: Testing
nav_order: 7
---

# Testing

Abbiamo adottato un approccio **agile** per il nostro progetto, lavorando in modo flessibile e iterativo per definire i requisiti e adattarci rapidamente ai cambiamenti. Questo metodo ci ha permesso di esplorare e chiarire le funzionalità principali durante i primi backlog.

Successivamente, a partire dal terzo backlog, ci siamo spostati verso un approccio più **incrementale**, concentrandoci sullo sviluppo e rilascio di funzionalità complete a piccoli passi. Per esempio, abbiamo prima implementato un sistema di combattimento base per avere una versione funzionante del gioco, e poi abbiamo continuato a migliorarlo con funzionalità avanzate in incrementi successivi.

### Copertura del Codice e Integrazione CI

La copertura è stata applicata tramite un workflow GitHub Actions che esegue test, utilizzando SBT per pulire, testare e generare report, includendo l'utilizzo di Codecov per tracciare linee e codici non coperte. Questo è stato molto utile per avere una visualizzazione grafica dei test fatti.

### Codice

I test comportamentali sono stati implementati con **ScalaTest**, **AnyFunSpec** e **Matchers** per asserzioni leggibili.

Ecco un esempio dai test della view, che verifica inizializzazione e rendering senza eccezioni, questo test si concentra su comportamenti osservabili in ambienti con interfaccia grafica, saltando test GUI se in ambienti headless per gestire vincoli CI:

```scala
"SwingView" should "initialize with the correct panels and buttons" in {
  val state = SituationGenerator.kickOff(Score.init())
  val view = new SwingView(state)
  noException should be thrownBy view.render(state)
}

