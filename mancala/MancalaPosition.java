package GameSearch.mancala;

import java.io.Serializable;
import java.util.Arrays;

// Classe représentant l'état actuel du plateau de jeu Mancala
// Elle est sérialisable pour permettre la sauvegarde et le chargement des parties
public class MancalaPosition extends Position implements Serializable {
    private static final long serialVersionUID = 1L; // Assure la compatibilité de version pour la sérialisation

    public boolean extraTurn = false; // Indique si le joueur bénéficie d'un tour supplémentaire
    public static final int HUMAN_MANCALA = 6; // Index correspondant au Mancala du joueur humain
    public static final int PROGRAM_MANCALA = 13; // Index correspondant au Mancala du programme (IA)
    public int scorePenalty; // Pénalité pour des scénarios spécifiques (ex. opportunités adverses)

    public int[] board = new int[14]; // Tableau représentant le plateau de jeu (14 cases au total)

    @Override
    protected MancalaPosition clone() {
        // Méthode pour créer une copie indépendante (clone) de l'objet actuel
        MancalaPosition copy = new MancalaPosition();
        copy.board = this.board.clone();  // Effectue une copie profonde (deep copy) du tableau board
        copy.extraTurn = this.extraTurn;  // Copie l'état du drapeau extraTurn
        return copy;
    }

    // Constructeur : Initialise le plateau de jeu avec la configuration typique
    public MancalaPosition() {
        // Configuration typique : 4 graines par case, Mancalas initialisés à 0
        Arrays.fill(board, 4); // Remplit chaque case du tableau avec 4 graines
        board[HUMAN_MANCALA] = 0; // Le Mancala du joueur humain commence avec 0 graines
        board[PROGRAM_MANCALA] = 0; // Le Mancala du programme (IA) commence avec 0 graines
    }

    @Override
    public String toString() {
        // Génère une représentation lisible du plateau de jeu sous forme de chaîne de caractères
        StringBuilder sb = new StringBuilder();
        sb.append("PROGRAM: "); // Ligne pour afficher les cases du programme (côté IA)
        for (int i = 12; i >= 7; i--) sb.append(board[i]).append(" "); // Cases du programme (de droite à gauche)
        sb.append("\nMancalas: ").append(board[PROGRAM_MANCALA]).append(" | ").append(board[HUMAN_MANCALA]);
        // Affiche les Mancalas avec les scores actuels
        sb.append("\nHUMAN:    "); // Ligne pour afficher les cases du joueur humain (côté humain)
        for (int i = 0; i <= 5; i++) sb.append(board[i]).append(" "); // Cases du joueur humain (de gauche à droite)
        return sb.toString(); // Retourne la chaîne formatée
    }
}
