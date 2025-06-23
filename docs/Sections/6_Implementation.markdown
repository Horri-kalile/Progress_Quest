---
layout: default
title: Implementazione
nav_order: 6
---

# Implementazione

L’implementazione di Progress Quest è stata realizzata in Scala 3, utilizzando ScalaFX per la GUI e SBT come sistema di build. Il progetto è organizzato in cartelle dedicate a view, models, controllers e gestione degli eventi.

Le funzionalità principali sono state sviluppate seguendo un approccio incrementale:

- Il **game loop** automatico è gestito tramite il controller `GameLoopController`, che simula i tick di gioco, la generazione di eventi e la progressione del personaggio.
- La **generazione del personaggio** avviene tramite una schermata dedicata, con randomizzazione di razza, classe, comportamento e attributi.
- Gli **eventi** (combattimenti, quest, eventi speciali) sono gestiti da controller specifici, che si occupano di applicare effetti e aggiornare lo stato del gioco.
- L’**interfaccia utente** è suddivisa in pannelli modulari, ciascuno responsabile di una sezione del gioco (player, equipaggiamento, statistiche, inventario, mondo, skill, missione, diario, combat log).
- Lo stato del gioco viene mantenuto e aggiornato tramite i modelli e i controller, garantendo coerenza tra logica e UI.

Il versionamento del codice è stato gestito tramite Git e GitHub, con branch tematici per le diverse funzionalità e una strategia di merge regolare per integrare le modifiche nel branch principale.
