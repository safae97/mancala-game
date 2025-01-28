package GameSearch.mancala;

import java.util.*;

public abstract class GameSearch {
    public static final boolean DEBUG = false; // Constante pour activer/désactiver le mode débogage

    /*
     * Remarque : la classe abstraite Position doit également
     * être sous-classée pour écrire un nouveau programme de jeu.
     */
    /*
     * Remarque : la classe abstraite Move doit également être sous-classée.
     */

    public static boolean PROGRAM = false; // Constante pour représenter l'IA
    public static boolean HUMAN = true;   // Constante pour représenter le joueur humain

    // Vérifie si la partie est terminée (ex : plus de mouvements possibles)
    public abstract boolean drawnPosition(Position p);

    // Détermine si un joueur a gagné la partie
    public abstract boolean wonPosition(Position p, boolean player);

    // Évalue la position actuelle pour attribuer un score
    public abstract float positionEvaluation(Position p, boolean player);

    // Affiche l'état actuel du plateau
    public abstract void printPosition(Position p);

    // Renvoie les mouvements possibles pour un joueur
    public abstract Position[] possibleMoves(Position p, boolean player);

    // Effectue un mouvement et renvoie la nouvelle position
    public abstract Position makeMove(Position p, boolean player, Move move);

    // Vérifie si la profondeur maximale de recherche est atteinte
    public abstract boolean reachedMaxDepth(Position p, int depth);

    // Crée un mouvement à partir d'une entrée utilisateur
    public abstract Move createMove();

    // Méthode principale pour l'algorithme Alpha-Beta
    protected List<Object> alphaBeta(int depth, Position p, boolean player) {
        return alphaBetaHelper(depth, p, player, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
    }

    // Méthode récursive pour l'algorithme Alpha-Beta
    protected List<Object> alphaBetaHelper(int depth, Position p, boolean player, float alpha, float beta) {
        // Vérifie si la profondeur maximale ou la fin du jeu est atteinte
        if (reachedMaxDepth(p, depth) || drawnPosition(p)) {
            float eval = positionEvaluation(p, player); // Évalue la position
            return Arrays.asList(eval, null); // Renvoie le score et aucun mouvement
        }

        List<Object> bestMove = new ArrayList<>(); // Stocke le meilleur mouvement
        Position[] moves = possibleMoves(p, player); // Récupère les mouvements possibles
        Arrays.sort(moves, Comparator.comparingDouble(m -> -positionEvaluation((Position) m, player)));

        float bestValue = Float.NEGATIVE_INFINITY;

        for (Position move : moves) {
            // Exploration récursive pour évaluer les mouvements
            List<Object> evalResult = alphaBetaHelper(depth + 1, move, !player, -beta, -alpha);
            float eval = -((Float) evalResult.get(0)); // Inverse l'évaluation pour l'adversaire

            if (eval > bestValue) {
                bestValue = eval;
                bestMove.clear();
                bestMove.add(move); // Enregistre le meilleur mouvement
                bestMove.addAll(evalResult.subList(1, evalResult.size())); // Ajoute les détails restants
            }

            // Met à jour alpha et réalise une coupe (cutoff) si nécessaire
            alpha = Math.max(alpha, eval);
            if (alpha >= beta) {
                break; // Coupe les branches inutiles
            }
        }

        bestMove.add(0, bestValue); // Ajoute le score en premier
        return bestMove;
    }

    // Fonction utilitaire pour récupérer l'index du pit d'un meilleur coup
    private int getPitIndex(MancalaPosition current, MancalaPosition best) {
        for (int i = 0; i < 6; i++) {
            if (!Arrays.equals(current.board, best.board)) {
                return i; // Retourne l'index du pit correspondant au mouvement
            }
        }
        return -1; // Ne devrait pas se produire
    }

    // Fonction pour obtenir l'index du meilleur pit à jouer
    private int getBestMovePitIndex(MancalaPosition current, MancalaPosition best, boolean player) {
        int start = player ? 0 : 7; // Début du côté du joueur actuel
        int end = player ? 5 : 12; // Fin du côté du joueur actuel

        for (int i = start; i <= end; i++) {
            if (current.board[i] > 0) { // Vérifie que le pit n'est pas vide
                MancalaPosition simulatedMove = new MancalaPosition();
                System.arraycopy(current.board, 0, simulatedMove.board, 0, 14);
                makeMove(simulatedMove, player, new MancalaMove(i)); // Simule le mouvement
                if (Arrays.equals(simulatedMove.board, best.board)) {
                    return i; // Retourne l'index correspondant
                }
            }
        }
        return -1; // Aucun mouvement valide trouvé
    }

    // Méthode principale pour jouer une partie
    public void playGame(Position startingPosition, boolean humanPlayFirst, boolean playAgainstComputer) {
        Scanner scanner = new Scanner(System.in);
        MancalaPosition pos = (MancalaPosition) startingPosition;
        boolean currentPlayer = humanPlayFirst; // Détermine le joueur initial
        int remainingHelps = 3; // Nombre d'aides restantes

        while (true) {
            printPosition(pos); // Affiche le plateau

            // Vérifie si la partie est terminée
            if (drawnPosition(pos)) {
                System.out.println("Game Over!");
                System.out.println("Final Scores:");
                System.out.println("Human: " + pos.board[MancalaPosition.HUMAN_MANCALA]);
                System.out.println("Program: " + pos.board[MancalaPosition.PROGRAM_MANCALA]);

                // Affiche le gagnant ou une égalité
                if (pos.board[MancalaPosition.HUMAN_MANCALA] > pos.board[MancalaPosition.PROGRAM_MANCALA]) {
                    System.out.println("Human Wins!");
                } else if (pos.board[MancalaPosition.PROGRAM_MANCALA] > pos.board[MancalaPosition.HUMAN_MANCALA]) {
                    System.out.println("Program Wins!");
                } else {
                    System.out.println("It's a Draw!");
                }
                break; // Fin de la boucle
            }

            // Gestion des tours pour le joueur humain
            if (currentPlayer == HUMAN) {
                System.out.println("Human's turn! (Type 'options' for save/load/quit, or enter help, or enter pit index 0-5)");
                String input = scanner.next();

                // Option d'aide
                if ("help".equalsIgnoreCase(input) && playAgainstComputer) {
                    if (remainingHelps > 0) {
                        remainingHelps--;
                        System.out.println("AI is calculating the best move for you...");
                        try {
                            List<Object> result = alphaBeta(0, pos, HUMAN);
                            MancalaPosition bestMove = (MancalaPosition) result.get(1);

                            if (bestMove != null) {
                                int recommendedPit = getBestMovePitIndex(pos, bestMove, HUMAN);
                                if (recommendedPit != -1) {
                                    System.out.println("AI recommends choosing pit: " + recommendedPit);
                                } else {
                                    System.out.println("No valid moves available.");
                                }
                            } else {
                                System.out.println("No valid moves available.");
                            }
                        } catch (IllegalArgumentException e) {
                            System.out.println("AI encountered an error: " + e.getMessage());
                        }
                        System.out.println("You have " + remainingHelps + " help(s) remaining.");
                    } else {
                        System.out.println("You have used all your helps.");
                    }
                    continue; // Retourner au joueur
                }

                // Options pour sauvegarder, charger ou quitter
                if ("options".equalsIgnoreCase(input)) {
                    System.out.println("Options: (1) Save Game (2) Load Game (3) Quit");
                    int option = scanner.nextInt();
                    switch (option) {
                        case 1:
                            System.out.print("Enter filename to save the game: ");
                            String saveFile = scanner.next();
                            ((Mancala) this).saveGame(pos, saveFile);
                            System.out.println("Game saved.");
                            continue;
                        case 2:
                            System.out.print("Enter filename to load the game: ");
                            String loadFile = scanner.next();
                            MancalaPosition loadedPos = ((Mancala) this).loadGame(loadFile);
                            if (loadedPos != null) {
                                pos = loadedPos;
                                currentPlayer = humanPlayFirst; // Revenir à l'ordre initial
                            }
                            continue;
                        case 3:
                            System.out.println("Exiting the game. Goodbye!");
                            return;
                        default:
                            System.out.println("Invalid option. Continuing the game...");
                    }
                    continue;
                }

                // Gestion du mouvement du joueur
                try {
                    int pit = Integer.parseInt(input);
                    if (pit < 0 || pit > 5 || pos.board[pit] == 0) {
                        System.out.println("Invalid move: The selected pit is empty or out of range. Try again.");
                        continue;
                    }
                    Move move = new MancalaMove(pit);
                    pos = (MancalaPosition) makeMove(pos, HUMAN, move);

                    if (!pos.extraTurn) {
                        currentPlayer = playAgainstComputer ? PROGRAM : !currentPlayer; // Changer de joueur
                    } else {
                        System.out.println("Human gets another turn!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Enter a number between 0 and 5.");
                }
            } else {
                // Tour du programme ou du deuxième joueur
                if (playAgainstComputer) { // Mode contre l'ordinateur
                    System.out.println("Computer's turn!");
                    List<Object> result = alphaBeta(0, pos, PROGRAM);
                    MancalaPosition bestMove = (MancalaPosition) result.get(1);
                    pos = bestMove;

                    if (!pos.extraTurn) {
                        currentPlayer = HUMAN; // Retourner au joueur humain
                    } else {
                        System.out.println("Computer gets another turn!");
                    }
                } else { // Mode Humain vs Humain (Player 2)
                    while (true) { // Boucle pour Player 2
                        try {
                            System.out.println("Player 2's turn! (Type 'options' for save/load/quit, or enter pit index 0-5)");
                            String input = scanner.next();

                            if ("options".equalsIgnoreCase(input)) { // Options pour Player 2
                                System.out.println("Options: (1) Save Game (2) Load Game (3) Quit");
                                int option = scanner.nextInt();
                                switch (option) {
                                    case 1:
                                        System.out.print("Enter filename to save the game: ");
                                        String saveFile = scanner.next();
                                        ((Mancala) this).saveGame(pos, saveFile);
                                        System.out.println("Game saved.");
                                        continue;
                                    case 2:
                                        System.out.print("Enter filename to load the game: ");
                                        String loadFile = scanner.next();
                                        MancalaPosition loadedPos = ((Mancala) this).loadGame(loadFile);
                                        if (loadedPos != null) {
                                            pos = loadedPos;
                                            currentPlayer = humanPlayFirst; // Revenir à l'ordre initial
                                        }
                                        continue;
                                    case 3:
                                        System.out.println("Exiting the game. Goodbye!");
                                        return;
                                    default:
                                        System.out.println("Invalid option. Continuing the game...");
                                }
                                continue;
                            }

                            // Gestion du mouvement du deuxième joueur
                            int pit = Integer.parseInt(input);
                            if (pit < 0 || pit > 5) {
                                System.out.println("Invalid input: Enter a number between 0 and 5.");
                                continue;
                            }

                            int mappedPit = pit + 7; // Côté Player 2
                            if (pos.board[mappedPit] == 0) {
                                System.out.println("Invalid move: The selected pit is empty. Try again.");
                                continue;
                            }

                            Move move = new MancalaMove(mappedPit);
                            pos = (MancalaPosition) makeMove(pos, PROGRAM, move);

                            if (!pos.extraTurn) {
                                currentPlayer = HUMAN; // Retourner au Player 1
                            } else {
                                System.out.println("Player 2 gets another turn!");
                            }
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a valid number between 0 and 5.");
                            scanner.nextLine(); // Consommer la ligne restante
                        }
                    }
                }
            }
        }
    }
}
