---
layout: default
title: Kalile horri
nav_order: 1
parent: Implementazione
---
# Implementazione - Kalile Horri

## Monster Model

**Descrizione**  
Ho sviluppato il modulo Monster con l'obiettivo di rappresentare in maniera dettagliata e flessibile i mostri presenti nel mondo di gioco. Ogni mostro è un'entità con caratteristiche uniche legate a statistiche di combattimento, tipo, zona di origine, ricompense e comportamenti speciali. Il design del modulo è stato pensato per essere estensibile e facilmente integrabile con altri sistemi del gioco come il World, il sistema di ricompense (Item ed Equipment) e (MonsterBehavior). La generazione dei mostri avviene in modo procedurale tramite una factory (MonstersFactory) che tiene conto del livello del giocatore, della zona e della fortuna, rendendo ogni incontro variabile e bilanciato.

<details>
<summary><u>Full details</u></summary>

### Aspetti implementativi

- Utilizzo di una enum per rappresentare i diversi tipi di mostri (Beast, Undead, Dragon, ecc.), ognuno con modificatori specifici alle statistiche.
- Classe Monster come entità centrale, composta da attributi base (MonsterAttributes) e stati speciali (berserk, regenerating), oltre a informazioni di contesto come la zona di origine e il comportamento.
- Implementazione di una factory (MonstersFactory) per la generazione dinamica di mostri, con logiche per:
  - Scelta casuale del nome da una lista caricata tramite MonsterLoader
  - Scala delle statistiche in base al livello del giocatore e alla difficoltà (strong)
  - Applicazione di modificatori legati al tipo (applyTypeModifiers) e al comportamento (MonsterBehavior)
  - Calcolo delle ricompense (oro, esperienza, oggetti)
- Comportamenti modulari definiti tramite il trait MonsterBehavior con implementazioni come Aggressive, Defensive, Berserk, Regenerating, ecc. Questi comportamenti modificano direttamente le statistiche o gli stati del mostro.

### Funzionalità principali

| Componente/Metodo                        | Descrizione                                                                 |
| ---------------------------------------- | --------------------------------------------------------------------------- |
| **MonsterType**                          | Enum dei tipi di mostro (Beast, Undead, Humanoid, Dragon, Demon, Elemental) |
| **MonsterAttributes**                    | Statistiche di combattimento e vulnerabilità                                |
| **Monster.receiveDamage**                | Applica danno al mostro, riducendo gli HP                                   |
| **Monster.receiveHealing**               | Cura il mostro, aumentando gli HP                                           |
| **Monster.explosionDamage**              | Calcola il danno da esplosione o attacco speciale                           |
| **Monster.isDead**                       | Verifica se il mostro è sconfitto                                           |
| **MonsterBehavior**                      | Comportamenti speciali che modificano attributi o stato del mostro          |
| **MonstersFactory.randomMonsterForZone** | Genera un mostro casuale per una zona e livello specifici                   |
| **MonstersFactory.scaleLevel**           | Calcola il livello del mostro in base al giocatore e alla difficoltà        |
| **MonstersFactory.generateAttributes**   | Genera attributi di combattimento scalati                                   |
| **MonstersFactory.applyTypeModifiers**   | Applica modifiche agli attributi in base al tipo di mostro                  |
| **MonstersFactory.generateRewards**      | Calcola le ricompense (oro, esperienza, oggetti, equipaggiamento)           |

</details>

## World Model

**Descrizione**  
Il modulo World gestisce la logica del mondo di gioco, con un focus specifico sulle zone di origine dei mostri (OriginZone) e sugli effetti ambientali che influenzano le dinamiche di combattimento. Ogni zona rappresenta un bioma con caratteristiche distinte, come foreste, deserti o vulcani, e può conferire buff ai mostri che vi appartengono. Il modulo permette anche di gestire le transizioni tra zone, garantendo varietà e dinamismo durante l'esplorazione del mondo da parte del giocatore.
L'interazione tra World e Monster permette la creazione di mostri ambientati e bilanciati in funzione del contesto in cui vengono generati.

<details>
<summary><u>Full details</u></summary>

### Aspetti implementativi

- Definizione dell'enumerazione OriginZone che rappresenta le diverse aree del mondo (Forest, Swamp, Desert, Volcano, Plains), ognuna con caratteristiche ambientali uniche.
- La funzione applyZoneBuffs(monster, currentZone) applica modificatori alle statistiche del mostro solo se si trova nella sua zona di origine:
  - Forest: aumenta la difesa
  - Desert: aumenta l'attacco
  - Volcano: aumenta gli HP
  - Swamp: riduce le vulnerabilità ai danni (fisici e magici)
  - Plains: nessun effetto speciale
- La funzione randomWorld(currentZone) garantisce la transizione verso una nuova zona casuale diversa da quella attuale.
- La funzione getZoneDescription(zone) fornisce descrizioni testuali leggibili e utili per l'interfaccia utente, spiegando gli effetti di ogni zona.

### Funzionalità principali

| Componente/Metodo            | Descrizione                                                                     |
| ---------------------------- | ------------------------------------------------------------------------------- |
| **OriginZone**               | Enum che rappresenta le zone del mondo (Forest, Swamp, Desert, Volcano, Plains) |
| **World.randomWorld**        | Restituisce una nuova zona casuale diversa da quella attuale                    |
| **World.applyZoneBuffs**     | Applica buff ambientali ai mostri che si trovano nella loro zona di origine     |
| **World.getZoneDescription** | Restituisce una descrizione testuale degli effetti ambientali di una zona       |

</details>

## GameUi

**Descrizione**  
Ho sviluppato il modulo GameUi per gestire l'interfaccia grafica principale del gioco.  
Questo modulo si occupa di visualizzare tutte le informazioni rilevanti per il giocatore: statistiche, equipaggiamento, inventario, abilità, missioni, log degli eventi e dei combattimenti, dettagli sui mostri incontrati e informazioni sulla zona attuale.  
Il design è stato pensato per essere reattivo, modulare e facilmente aggiornabile in tempo reale, offrendo una user experience chiara e coinvolgente.

<details>
<summary><u>Full details</u></summary>

### Aspetti implementativi

GameUi è strutturato come un oggetto che gestisce la finestra principale del gioco tramite una griglia di pannelli, ciascuno dedicato a una sezione specifica (statistiche, inventario, mondo, abilità, missioni, diario, log combattimenti, info mostro).  
L'interfaccia si adatta dinamicamente alle dimensioni dello schermo e aggiorna i dati in tempo reale in risposta agli eventi di gioco.

- Utilizzo di layout flessibili (BorderPane, VBox, HBox, GridPane) per organizzare i contenuti.
- Aggiornamento automatico delle informazioni tramite metodi come `updatePlayerInfo`, `addCombatLog`, `addEventLog`, `updateMonsterInfo`.
- Gestione dello stato interno (player, messaggi, mostro corrente) per garantire coerenza tra logica e visualizzazione.
- Implementazione di funzionalità interattive come la barra di progresso animata nel diario dell'eroe e la schermata di game over con possibilità di restart.
- Modularità dei pannelli per facilitare l'estensione e la manutenzione dell'interfaccia.

### Funzionalità principali

| Componente/Metodo            | Descrizione                                                         |
| ---------------------------- | ------------------------------------------------------------------- |
| **open**                     | Apre la finestra principale del gioco e visualizza tutti i pannelli |
| **updatePlayerInfo**         | Aggiorna le informazioni del giocatore nella UI                     |
| **addCombatLog**             | Aggiunge un messaggio al log dei combattimenti                      |
| **addEventLog**              | Aggiunge un messaggio al diario degli eventi                        |
| **updateMonsterInfo**        | Aggiorna il pannello con le informazioni sul mostro attuale         |
| **showGameOverWithRestart**  | Mostra la schermata di game over con opzione di restart             |
| **resetData**                | Resetta lo stato interno della UI quando si riavvia il gioco        |
| **createRoot**               | Crea la struttura principale della UI con tutti i pannelli          |
| **createHeroDiaryPanel**     | Crea il pannello diario con barra di progresso animata              |
| **createCombatLogContent**   | Visualizza il log dei combattimenti                                 |
| **createMonsterInfoContent** | Visualizza i dettagli del mostro incontrato                         |

</details>

## GameController

**Descrizione**  
Ho sviluppato il modulo GameController per gestire il flusso principale del gioco e la logica degli eventi.  
Questo controller coordina l'interazione tra i modelli di gioco (giocatore, mostri, eventi) e l'interfaccia utente, gestendo il ciclo di gioco automatico, la sequenza dei combattimenti, la progressione degli eventi e lo stato globale della partita.  
Il design permette di avviare, fermare e riavviare il gioco, oltre a gestire in modo reattivo gli aggiornamenti della UI e le transizioni tra gli stati.

<details>
<summary><u>Full details</u></summary>

### Aspetti implementativi

GameController è strutturato come oggetto singleton che mantiene lo stato corrente del giocatore e gestisce il timer del ciclo di gioco.  
Il ciclo principale (`startGameLoop`) attiva eventi a intervalli regolari, alternando combattimenti, esplorazioni e altri eventi casuali.  
La logica di combattimento è gestita tramite sequenze di passi (`showFightStepsSequentially`), con aggiornamenti graduali della UI per migliorare la presentazione.  
Il controller si occupa anche della gestione del game over, offrendo la possibilità di riavviare la partita tramite una callback che riapre la schermata di creazione del personaggio e la UI di gioco.

- Gestione dello stato del giocatore e del timer del gioco.
- Attivazione automatica di eventi tramite intervalli temporizzati.
- Coordinamento tra moduli di combattimento, eventi e interfaccia grafica.
- Aggiornamento reattivo della UI in base allo stato del gioco.
- Gestione del game over e restart tramite callback.
- Supporto per il triggering manuale di eventi (utile per test/debug).

### Funzionalità principali

| Componente/Metodo              | Descrizione                                                       |
| ------------------------------ | ----------------------------------------------------------------- |
| **startGame**                  | Avvia una nuova partita con il giocatore selezionato              |
| **stopGame**                   | Ferma il ciclo di gioco e cancella il timer                       |
| **startGameLoop**              | Avvia il ciclo principale che genera eventi a intervalli regolari |
| **triggerRandomEvent**         | Attiva un evento casuale e aggiorna lo stato del gioco            |
| **showFightStepsSequentially** | Visualizza i passi del combattimento con aggiornamenti graduali   |
| **handleGameOver**             | Gestisce la fine della partita e offre la possibilità di restart  |
| **updateUI**                   | Aggiorna la UI con lo stato corrente del giocatore                |
| **getCurrentPlayer**           | Restituisce il giocatore attuale                                  |
| **isRunning**                  | Indica se il ciclo di gioco è attivo                              |
| **triggerEvent**               | Attiva manualmente un evento specifico (per test/debug)           |

</details>
