---
layout: default
title: Processo di sviluppo
nav_order: 2
---

# **Processo di Sviluppo**

Il team ha adottato un processo di sviluppo ispirato alla metodologia **Scrum**, seguendo un approccio **iterativo e incrementale** che garantisce flessibilità, adattamento continuo e miglioramento progressivo del prodotto.  
Lo sviluppo è suddiviso in **sprint** della durata di circa **10 giorni**, ciascuno con obiettivi chiari e task assegnati individualmente.

Sono stati definiti ruoli chiave per coordinare il lavoro e facilitare il processo:

- **Product Owner**: definisce la visione progettuale e le priorità funzionali.
- **Scrum Master**: facilita il processo, organizza i meeting e coordina eventuali adattamenti.
- **Sviluppatore**: implementano funzionalità, scrivono test e validano il codice.

---

## **Organizzazione del Team**

Durante il kick-off iniziale sono stati assegnati i seguenti ruoli:

- **Kalile Horri** – _Product Owner_ & Sviluppatore  
  Responsabile della gestione generale del progetto, definizione degli obiettivi insieme al Scrum Master e supervisione del repository GitHub.

- **Jiahao Guo** – _Scrum Master_ & Sviluppatore  
  Facilita l’attività del team, gestisce la pubblicazione tramite GitHub Pages e la stesura del report. È responsabile dei test automatici e dell'integrazione continua.

- **Intissar Meldebub** – Sviluppatrice  
  Si occupa della progettazione della view e della struttura del gioco, contribuendo allo sviluppo delle funzionalità principali e della strategia di testing.

---

## **Gestione del Lavoro**

### Sprint Planning

All’inizio di ogni sprint il team pianifica:

- Gli obiettivi da raggiungere
- L’assegnazione dei task
- La revisione dello sprint precedente
- L’analisi dell’andamento complessivo del progetto

> Durata tipica dello Sprint Planning: **1–3 ore**

### Task Management

- La gestione operativa avviene tramite **Trello**, dove i task vengono creati, assegnati e monitorati per sprint.
- I task bloccati vengono spostati temporaneamente nella colonna _paused_, in attesa di risoluzione.
- Ogni task è collegato a un **branch Git tematico**, coerente con il contesto di sviluppo (`models`, `view`, `controller`, ecc.).

### Completamento dei Task

Un task è considerato completato solo quando:

- È integrato nel branch `main`
- Ha superato i test automatici previsti
- È stato verificato nel contesto funzionale complessivo

---

## **Comunicazione e Coordinamento**

La comunicazione interna è gestita principalmente da remoto, attraverso due tipologie di meeting:

- **Long Meeting**: incontri strutturati dedicati all’analisi dei progressi, alla valutazione dell’efficacia del processo e alla pianificazione dei cicli successivi.

- **Short Meeting**: sessioni rapide (20–40 minuti) per affrontare problemi urgenti, ridefinire task o rimodellare parte della struttura del gioco.

---

## **Strumenti di Supporto**

Per strutturare e automatizzare il flusso di lavoro, il team utilizza una serie di strumenti fondamentali:

- **Git**: controllo di versione con branch tematici per sviluppo parallelo
- **GitHub**: repository centrale, gestione issue e pubblicazione documentazione tramite **GitHub Pages**
- **Trello**: organizzazione dei task per sprint
- **GitHub Actions**: automazione dei processi di build, test e deployment

Lo schema di versionamento adottato è il **Semantic Versioning** (`MAJOR.MINOR.PATCH`), utile per il rilascio di versioni stabili e tracciabili del gioco.

---

## **Continuous Integration & Deployment**

Per garantire qualità, tracciabilità e velocità nel rilascio, il team ha configurato un sistema di **Integrazione e Deployment Continui (CI/CD)** basato su **GitHub Actions**. Questo processo automatizzato prevede:

- **Test e release** automatici a ogni push o pull request su branch
- **Deploy** automatico della documentazione su **GitHub Pages** a ogni rilascio stabile
- Notifiche sugli esiti della pipeline per un monitoraggio costante

---

## **Workflow Git per Test e Rilascio**

Il team adotta un **Git Workflow** chiaro e scalabile:

1. Branch di sviluppo per ogni elemento (model,view,controller)
2. Merge nel branch `main solo dopo review e con superamento dei test
3. Release su `main` per ogni versione dopo un intervallo di tempo, associato a un tag e descrizione.

Questo approccio garantisce stabilità al codice rilasciato e facilita la collaborazione.

---

## **Code Coverage**

Per monitorare la qualità dei test automatici, viene calcolata la **code coverage**, ovvero la percentuale di codice effettivamente coperta da test.  
L’obiettivo minimo è mantenere una copertura superiore al **50%**, con particolare attenzione alle classi core del modello e della logica di gioco.

Strumenti utilizzati:

- **sbt-coverage** per Scala
- **scalafmt per la formattazione del codice**
- Report generati automaticamente e consultabili nel repository

Questo consente di identificare facilmente le aree meno testate e migliorare la robustezza generale del progetto.

