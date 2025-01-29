# Mancala Game Application

## 🌟 Project Overview

This project involves developing a **Java application** that implements the traditional African board game **Mancala**. The game features adversarial search algorithms as covered in the course, providing players with both strategic challenges and opportunities for learning AI concepts.

### 🎯 Objectives

- **Implement the rules and principles of Mancala:**
  - The board consists of two rows of six pits, with a large pit ("store") at each end.
  - Players aim to collect the most stones in their own store by the end of the game.
  - Stones are distributed strategically to maximize opportunities and block the opponent.

- **Key Features:**
  - Allowing players to request hints (limited number of times).
  - Adjusting the game's complexity when playing against the machine.
  - Implementing three heuristics (gains maximazation , losses minimization and Prevention of missed opportunities).
  - Using Alpha Beta Pruning Algorithm for decesio making.
  - Enable two-player mode or player vs. machine mode.
  - Provide options to save and resume saved games.

---

## 🛠️ Features

### Gameplay
- **Two Modes:**
  - Player vs. Player
  - Player vs. Machine

- **Rules:**
  - Players pick stones from their pits and distribute them counterclockwise.
  - Stones can be added to the player's store but not to the opponent's store.
  - Capturing is possible if the last stone lands in an empty pit on the player's side.
  - Bonus turns are granted if the last stone lands in the player's store.

### AI and Heuristics
- **Strategies:**
  - Define strategies for the machine and hint system.
  - Implement alpha-beta pruning with an enhanced `alphaBetaHelper(...)` method.

- **Heuristics:**
  - Propose and implement at least two heuristics to guide gameplay.

### Persistence
- Save the current game state.
- Resume and replay saved games.

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK 11 or higher)**
- **Maven**

### Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/safae97/mancala-game.git
   cd mancala-game
   ```

2. **Build the Project**:
   ```bash
   mvn clean install
   ```

3. **Run the Application**:
   ```bash
   mvn exec:java -Dexec.mainClass="GameSearch.mancala.MancalaMenuUI"
   ```

---

## 📝 Game Rules

1. At each turn, a player chooses one of their pits containing stones.
2. The player picks up all stones from the selected pit and distributes them counterclockwise.
3. If the last stone lands in an empty pit on the player's side, and the opposite pit has stones, the player captures all those stones.
4. If the last stone lands in the player's store, they get an extra turn.
5. The game ends when all pits on one side are empty.
6. The player with the most stones in their store wins.

---


## 📑 Report Section
For a detailed explanation of the project, including design decisions, implementation details, and demonstration please refer to the [Project Report](./rapport_mancala.pdf).

---

 ## 🤝 Collaboration
This project is a collaborative effort between me and  [@fatimazahrae03](https://github.com/fatimazahrae03). Both contributed to the design, development, and implementation of the Mancala game, ensuring a robust and enjoyable application.


---

Happy Coding and Gaming! 🎮

