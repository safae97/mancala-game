package GameSearch.mancala;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Mancala extends GameSearch {

    // Sauvegarde l'état du jeu dans un fichier
    public void saveGame(MancalaPosition position, String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(position);
            System.out.println("Game saved to " + filename);
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }

    // Charge l'état du jeu à partir d'un fichier
    public MancalaPosition loadGame(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            MancalaPosition position = (MancalaPosition) in.readObject();
            System.out.println("Game loaded from " + filename);
            return position;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load game: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean drawnPosition(Position p) {
        MancalaPosition pos = (MancalaPosition) p;

        // Vérifie si toutes les cases du côté HUMAN sont vides
        boolean humanEmpty = true;
        for (int i = 0; i < 6; i++) {
            if (pos.board[i] > 0) {   //fpr each pit we have grains (not emoty)
                humanEmpty = false;
                break;
            }
        }

        // Vérifie si toutes les cases du côté PROGRAM sont vides
        boolean programEmpty = true;
        for (int i = 7; i < 13; i++) {
            if (pos.board[i] > 0) {
                programEmpty = false;
                break;
            }
        }

        // Si l'un des côtés est vide, transfère les graines restantes au Mancala correspondant
        if (humanEmpty || programEmpty) {
            if (!humanEmpty) { // Si PROGRAM est vide, transfère les graines restantes de HUMAN
                for (int i = 0; i < 6; i++) {
                    pos.board[MancalaPosition.HUMAN_MANCALA] += pos.board[i];
                    pos.board[i] = 0;
                }
            } else { // Si HUMAN est vide, transfère les graines restantes de PROGRAM
                for (int i = 7; i < 13; i++) {
                    pos.board[MancalaPosition.PROGRAM_MANCALA] += pos.board[i];
                    pos.board[i] = 0;
                }
            }
            return true; // Le jeu est terminé
        }

        return false; // Le jeu continue
    }

    @Override
    public boolean wonPosition(Position p, boolean player) {
        MancalaPosition pos = (MancalaPosition) p;

        // Vérifie si le jeu est terminé en utilisant drawnPosition
        if (!drawnPosition(p)) {
            return false; // Pas de gagnant tant que le jeu n'est pas terminé
        }

        // Compare les scores des Mancalas pour déterminer le gagnant
        int humanScore = pos.board[MancalaPosition.HUMAN_MANCALA];
        int programScore = pos.board[MancalaPosition.PROGRAM_MANCALA];
        if (player) {
            return humanScore > programScore; // HUMAN gagne si son score est supérieur
        } else {
            return programScore > humanScore; // PROGRAM gagne si son score est supérieur
        }
    }

    @Override
    public float positionEvaluation(Position p, boolean player) {
        MancalaPosition pos = (MancalaPosition) p;

        // Différence des scores des Mancalas
        int mancalaDifference = pos.board[MancalaPosition.PROGRAM_MANCALA] - pos.board[MancalaPosition.HUMAN_MANCALA];

        // Opportunités de capture
        int capturePotential = 0;
        for (int i = 0; i < 6; i++) {
            if (pos.board[i] == 0 && pos.board[12 - i] > 0) {
                capturePotential += pos.board[12 - i];
            }
        }

        // Réduction des graines proches du Mancala adverse
        int minimizeOpponentStones = 0;
        for (int i = 7; i < 12; i++) {
            minimizeOpponentStones += pos.board[i];
        }

        // Opportunités de tours supplémentaires
        int extraTurnPotential = 0;
        for (int i = 7; i < 13; i++) {
            if (pos.board[i] > 0 && (i + pos.board[i]) % 14 == MancalaPosition.PROGRAM_MANCALA) {
                extraTurnPotential++;
            }
        }

        // Évite de donner des opportunités de capture à l'adversaire
        int preventOpponentCapture = 0;
        for (int i = 7; i < 13; i++) {
            int nextIndex = (i + pos.board[i]) % 14;
            if (pos.board[i] > 0 && nextIndex >= 0 && nextIndex < 6 && pos.board[nextIndex] == 0) {
                int oppositeIndex = 12 - nextIndex;
                preventOpponentCapture -= pos.board[oppositeIndex];
            }
        }

        // Facteur lié à la phase du jeu (début, milieu, fin)
        int totalSeeds = Arrays.stream(pos.board).sum();
        float phaseFactor = totalSeeds > 30 ? 1.0f : totalSeeds > 15 ? 1.5f : 2.0f;

        // Calcul de l'évaluation pondérée
        return phaseFactor * (2.0f * mancalaDifference
                + 1.5f * capturePotential
                + 1.0f * extraTurnPotential
                - 1.0f * minimizeOpponentStones
                - 1.5f * preventOpponentCapture);
    }

    @Override
    public void printPosition(Position p) {
        System.out.println(p.toString());
    }

    @Override
    public Position[] possibleMoves(Position p, boolean player) {
        MancalaPosition pos = (MancalaPosition) p;
        List<ScoredPosition> scoredMoves = new ArrayList<>();
        int start = player ? 0 : 7;
        int end = player ? 5 : 12;

        for (int i = start; i <= end; i++) {
            if (pos.board[i] > 0) {
                // Simule le mouvement en clonant le plateau
                MancalaPosition newPos = new MancalaPosition();
                System.arraycopy(pos.board, 0, newPos.board, 0, 14);

                // Simule le coup
                makeMove(newPos, player, new MancalaMove(i));

                // Évalue la position résultante
                float eval = positionEvaluation(newPos, player);

                // Ajoute des bonus ou des pénalités
                if (newPos.extraTurn) eval += 10.0f; // Bonus pour tour supplémentaire
                if (newPos.board[i] == 0 && newPos.board[12 - i] > 0) eval += 5.0f; // Bonus pour capture
                if (!player && newPos.board[12 - i] > 0) eval -= 5.0f + newPos.board[12 - i];

                scoredMoves.add(new ScoredPosition(newPos, eval));
            }
        }

        // Trie les coups possibles par score décroissant
        scoredMoves.sort((a, b) -> Float.compare(b.score, a.score));
        return scoredMoves.stream()
                .map(m -> m.position)
                .toArray(size -> new Position[size]);
    }

    // Classe interne pour stocker les positions évaluées
    private static class ScoredPosition {
        Position position; // Position résultante
        float score;       // Score associé à la position

        ScoredPosition(Position position, float score) {
            this.position = position;
            this.score = score;
        }
    }

    @Override
    public Position makeMove(Position p, boolean player, Move move) {
        MancalaPosition pos = (MancalaPosition) p;
        MancalaMove m = (MancalaMove) move;
        int[] board = pos.board;
        int index = m.pitIndex;
        int seeds = board[index];

        // Vérifie que le mouvement est valide
        if (seeds <= 0) {
            throw new IllegalArgumentException("Invalid move: The selected pit is empty.");
        }

        board[index] = 0; // Vide le pit sélectionné
        int currentIndex = index;

        // Distribue les graines une à une
        while (seeds > 0) {
            currentIndex = (currentIndex + 1) % 14;

            // Saute le Mancala adverse
            if ((player && currentIndex == MancalaPosition.PROGRAM_MANCALA) ||
                    (!player && currentIndex == MancalaPosition.HUMAN_MANCALA)) {
                continue;
            }

            board[currentIndex]++;
            seeds--;
        }

        // Capture si la dernière graine tombe dans un pit vide du joueur
        if (board[currentIndex] == 1 &&
                ((player && currentIndex >= 0 && currentIndex <= 5) ||
                        (!player && currentIndex >= 7 && currentIndex <= 12))) {
            int oppositeIndex = 12 - currentIndex;
            int mancala = player ? MancalaPosition.HUMAN_MANCALA : MancalaPosition.PROGRAM_MANCALA;

            board[mancala] += board[oppositeIndex] + board[currentIndex];
            board[oppositeIndex] = 0;
            board[currentIndex] = 0;
        }

        // Vérifie si un tour supplémentaire est gagné
        int ownMancala = player ? MancalaPosition.HUMAN_MANCALA : MancalaPosition.PROGRAM_MANCALA;
        pos.extraTurn = (currentIndex == ownMancala);

        // Évalue les captures potentielles adverses
        if (!player) {
            int potentialCapture = 0;
            for (int i = 0; i < 6; i++) {
                if (pos.board[i] == 0 && pos.board[12 - i] > 0) {
                    potentialCapture += pos.board[12 - i];
                }
            }
            pos.scorePenalty = potentialCapture * 2; // Ajoute une pénalité proportionnelle
        }

        return pos;
    }

    @Override
    public boolean reachedMaxDepth(Position p, int depth) {
        MancalaPosition pos = (MancalaPosition) p;
        int totalSeeds = Arrays.stream(pos.board).sum();

        // Définit la profondeur maximale selon la difficulté
        int maxDepth;
        switch (difficulty) {
            case SIMPLE:
                maxDepth = 2;
                break;
            case MEDIUM:
                maxDepth = totalSeeds > 20 ? 6 : 8;
                break;
            case HARD:
                maxDepth = totalSeeds > 20 ? 8 : 12;
                break;
            default:
                maxDepth = 6;
        }

        // Vérifie si la profondeur maximale ou la fin du jeu est atteinte
        return depth >= maxDepth || drawnPosition(p);
    }

    @Override
    public Move createMove() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("Enter pit index (0-5 for HUMAN): ");
            int pit = sc.nextInt();

            // Vérifie que l'index est valide
            if (pit < 0 || pit > 5) {
                System.out.println("Invalid input. Please enter a number between 0 and 5.");
                continue;
            }

            return new MancalaMove(pit);
        }
    }

    private Difficulty difficulty; // Niveau de difficulté

    // Enumération des niveaux de difficulté
    public enum Difficulty {
        SIMPLE, MEDIUM, HARD
    }

    // Définit le niveau de difficulté
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        MancalaPosition initialPosition = new MancalaPosition();
        Mancala game = new Mancala();

        System.out.println("Welcome to Mancala!");
        System.out.println("Choose game mode: ");
        System.out.println("1. Play against the Computer");
        System.out.println("2. Play against another Player");

        int modeChoice = sc.nextInt();
        boolean playAgainstComputer = modeChoice == 1;

        if (playAgainstComputer) {
            System.out.println("Choose difficulty: ");
            System.out.println("1. Simple");
            System.out.println("2. Medium");
            System.out.println("3. Hard");
            int difficultyChoice = sc.nextInt();
            switch (difficultyChoice) {
                case 1:
                    game.setDifficulty(Difficulty.SIMPLE);
                    break;
                case 2:
                    game.setDifficulty(Difficulty.MEDIUM);
                    break;
                case 3:
                    game.setDifficulty(Difficulty.HARD);
                    break;
                default:
                    System.out.println("Invalid choice. Defaulting to Medium.");
                    game.setDifficulty(Difficulty.MEDIUM);
            }
            System.out.println("Starting game against the computer at " + game.difficulty + " difficulty.");
        } else {
            System.out.println("Starting a two-player game.");
        }

        game.playGame(initialPosition, true, playAgainstComputer);
    }
}
