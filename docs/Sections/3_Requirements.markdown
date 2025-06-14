---
layout: default
title: Requisiti
nav_order: 3
---
# **Requisiti**

## Funzionali

1. **Avanzamento automatico del personaggio**  
   Il sistema gestisce la progressione del player senza intervento diretto, attraverso eventi casuali.

2. **Generazione casuale di eventi**  
   Gli eventi vengono generati in modo pseudo-casuale e possono includere:
   - Combattimenti
   - Eventi speciali
   - Allenamento
   - Riposo
   - Quest

3. **Combattimento automatizzato e comportamenti dinamici**  
   Il combattimento avviene in modo automatico.  
   Player e mostri possono avere **behavior** specifici che alterano il flusso del combattimento.

4. **Sistema di quest**  
   Le quest sono composte da sequenze di azioni automatiche.  
   Offrono ricompense come **gold**, **esperienza (EXP)** e **equipment**.

5. **Inventario e risorse**  
   Il player raccoglie **materiali** che possono essere venduti per ottenere **gold**, usato per migliorare le statistiche.

6. **Gestione dell’equipaggiamento**  
   Gli oggetti equipaggiati influenzano direttamente le statistiche del personaggio e vengono ottenuti tramite eventi o quest.

7. **Skill e vulnerabilità**  
   Il player dispone di **skill** fisiche e magiche.  
   I mostri possono essere vulnerabili o resistenti a uno dei due tipi.

8. **Interazione opzionale**  
   Durante eventi speciali, il giocatore può compiere **scelte cruciali** (opzionali) con esiti casuali.  
   *Esempio*: “È comparso Red Goblin: fight or not?”  
   - Esito positivo: +5 livelli  
   - Esito negativo: morte del personaggio

9. **Gameplay infinito**
    Il gioco è strutturato come endless, ovvero senza una fine prestabilita.
    La partita termina esclusivamente in caso di morte del personaggio, condizione che può verificarsi durante eventi casuali o combattimenti particolarmente critici.

---

## Requisiti Non Funzionali

- **Facilità d’uso**  
  Il sistema deve essere semplice da avviare, senza richiedere configurazioni complesse o input continui.

- **Esperienza bilanciata e coinvolgente**  
  Il gioco deve offrire una progressione chiara, gratificante e ben bilanciata tra automatismo, casualità e ricompense.

- **Interfaccia utente comprensibile**  
  L’interfaccia grafica deve mostrare chiaramente lo stato del personaggio, gli eventi in corso e i risultati, senza ambiguità.

---

## Implementazione

- **Linguaggio**: Scala (versione 3.x)  
- **IDE**: IntelliJ IDEA 
- **Sistema di build**: sbt  
- **Testing**: ScalaTest 

### Controllo versione

- Utilizzo di **Git** con repository su **GitHub**
- Branch principali:
  - `main`: versione stabile
  - `report`: aggiornamento docs
  - `models`: modelli del gioco
  - `view` : interfaccia del gioco
  - `controller` : gestione ambiente di gioco

### Documentazione

- Pubblicazione della documentazione tramite **GitHub Pages**
- Utilizzo di Jekyll per la generazione della documentazione e dello stile.
