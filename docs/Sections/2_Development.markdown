---
layout: default
title: Processo di sviluppo
nav_order: 2
---

# **Processo di Sviluppo**

Il team ha adottato un processo di sviluppo ispirato alla metodologia **Scrum**, con approccio **iterativo e incrementale**, volto a garantire flessibilità, adattamento continuo e miglioramento progressivo del prodotto.  
Lo sviluppo è organizzato in **sprint** della durata di circa **10 giorni**, durante i quali vengono pianificati obiettivi chiari e assegnati i relativi task.

Sono stati identificati i ruoli chiave per coordinare le attività:

- **Product Owner**: definisce la visione del progetto e le priorità.
- **Scrum Master**: facilita il processo, organizzazione meeting e report dei cambiamenti possibili.
- **Sviluppatori**: realizzano le funzionalità, testano e validano il codice.

---

## **Organizzazione del Team**

Durante il meeting iniziale sono stati assegnati i seguenti ruoli:

- **Kalile Horri** – _Product Owner_ & Sviluppatore  
  Responsabile della gestione del progetto, definizione degli obiettivi assieme al scrum master e supervisione del repository GitHub.

- **Jiahao Guo** – _Scrum Master_ & Sviluppatore  
  Facilita le attività del team, gestisce la pubblicazione tramite GitHub Pages e strutturazione del report.

- **Intissar** – _Responsabile Test_ & Sviluppatrice  
  Si occupa dei test automatici e dell'integrazione continua, contribuendo allo sviluppo delle funzionalità principali.

---

## **Gestione del Lavoro**

### Sprint Planning

All’inizio di ogni sprint, il team si riunisce per pianificare:

- Obiettivi dello sprint
- Assegnazione dei task
- Revisione dei risultati dello sprint precedente
- Analisi dell’andamento generale del progetto

> Durata media dello Sprint Planning: **1–2 ore**

### Task Management

- La gestione operativa è affidata a **Trello**, dove i task vengono assegnati, monitorati e categorizzati per sprint.
- I task incompleti o bloccati vengono temporaneamente spostati nella colonna _paused_ in attesa di completamento o revisione.
- Ogni task è collegato a uno specifico branch Git, in base al contesto di lavoro (`models`, `view`, `controller`, ecc.).

### Completion of task

Un task è considerato completato solo quando:

- È stato integrato correttamente in `main`
- Ha superato i test previsti
- È stato verificato nel contesto funzionale generale

---

## **Comunicazione e Coordinamento**

La comunicazione interna si svolge principalmente **da remoto**, attraverso due tipologie di meeting:

- **Long Meeting**: incontri strutturati dedicati all’analisi del lavoro svolto, utili per valutare l’efficacia del processo e identificare task nei cicli successivi.

- **Short Meeting**: brevi sessioni (20–40 minuti) focalizzate sulla risoluzione di problemi imprevisti, ridefinizione dei task o, se necessario, rimodellazione parziale del dominio.

---

## **Strumenti di Supporto**

Per facilitare e strutturare il processo di sviluppo, il team utilizza una serie di strumenti che supportano il versionamento del codice, la collaborazione e l'automazione.

Il controllo di versione è gestito tramite **Git**, con l’utilizzo di **branch tematici** per lo sviluppo parallelo delle diverse componenti del progetto. Questo approccio consente un’organizzazione chiara del lavoro e facilita l’integrazione graduale delle funzionalità.

Il team utilizzerà il **versioning** per il rilascio delle **versioni stabili del gioco**, seguendo lo **schema semantico (Semantic Versioning)** nel formato `MAJOR.MINOR.PATCH`.

La documentazione viene pubblicata tramite **GitHub Pages**, permettendo un accesso rapido e centralizzato alle informazioni chiave del progetto (report, glossario, specifiche, ecc.).

Per l’automazione del flusso di lavoro, viene impiegato **GitHub Actions**, che consente di automatizzare processi di deployment.

Questi strumenti permettono al team di mantenere un processo di sviluppo efficiente, tracciabile e facilmente scalabile.
