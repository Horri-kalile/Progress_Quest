---
layout: default
title: Introduzione
nav_order: 1
---
# Introduzione
Progress Quest è una replica in Scala dell’omonimo zero-player game, sviluppata come progetto didattico per il corso Programming Paradigms and Development (Università di Bologna, sede di Cesena). Il sistema automatizza il ciclo di gioco – missioni, combattimenti, bottini ed eventi – tramite meccaniche stocastiche, modellando un gameplay passivo. A differenza dell’originale, integra un modulo opzionale di interazione ispirato a Godville, che consente decisioni utente in momenti critici, mantenendo l’architettura automatizzata di base.

---
## G**lossario dei termini**

- **Player**:  
  Il personaggio principale gestito dal sistema. Possiede statistiche (es. forza, difesa, intelligenza), HP (punti vita), MP (punti mana), livello, gold, **inventario**, **equipaggiamento**, **skill** e **behavior**.  
  Le **skill** possono essere di tipo fisico o magico, e vengono utilizzate automaticamente in combattimento. Il **behavior** definisce il passivo speciale, come *OneShot*, che consente di eliminare un mostro con un colpo solo.

- **World**:  
  Ambiente speciale in cui si svolge il gioco. Può applicare buff/potenziamenti (es. aumento danno dei mostri, rigenerazione, ecc.) che alterano le condizioni dei **monster**.

- **Monster**:  
  Entità ostili generate durante gli eventi. Ogni mostro ha statistiche, HP, **behavior** e può essere debole o resistente a **skill** fisiche o magiche.  

- **Event**:  
  Nucleo del ciclo di gioco, generato in modo casuale. Gli eventi possono essere:  
  - **Combattimento**  
  - **Evento speciale**  
  - **Allenamento**  
  - **Quest**  
  Ogni evento altera lo stato del player e guida l’avanzamento automatico.

- **Inventory**:  
  Raccolta di materiali ottenuti da eventi e quest. I materiali possono essere venduti per ottenere gold, che può essere speso per migliorare le statistiche del **player**.

- **Equipment**:  
  Oggetti equipaggiati dal player (armi, armature, accessori) che modificano direttamente le statistiche e influenzano l’esito dei combattimenti. Possono essere ottenuti tramite quest o eventi.

- **Quest**:  
  Missioni da completare attraverso una sequenza di azioni automatiche.  
  Una quest può richiedere più fasi e fornisce ricompense come gold, EXP, equipment e materiali di vendita.

- **Behavior**:  
  Caratteristica associata a player o mostri che altera le loro abilità in battaglia.  
  - *Esempi player*: OneShot – possibilità di sconfiggere un nemico con un solo colpo.  
  - *Esempi mostro*: DoubleHP.  
  I behavior aggiungono variabilità e profondità al sistema.

- **Interazione**:  
  Meccanica attivata solo durante **eventi speciali**, in cui al giocatore viene richiesto di effettuare una scelta che può influenzare drasticamente l’esito del gioco.  
  *Esempio*:  
  “È comparso Red Goblin: combatti o scappa?”
    Gli esiti possono essere *good ending* / *bad ending*.
  - Good ending → +5 livelli  
  - Bad ending → morte istantanea  

