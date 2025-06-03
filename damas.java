import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

class Move {
    int x1, y1, x2, y2;

    Move(int x1, int y1, int x2, int y2) {
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
    }
}

public class Checkers {
    static final int BOARD_SIZE = 8;
    static final char EMPTY = '.';
    static final char WHITE = 'w';
    static final char BLACK = 'b';
    static final char WHITE_KING = 'W';
    static final char BLACK_KING = 'B';

    char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
    char player, ai;
    Scanner sc = new Scanner(System.in);

    public Checkers() {
        for (int i = 0; i < BOARD_SIZE; i++)
            Arrays.fill(board[i], EMPTY);
        setupBoard();
    }

    void setupBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if ((i + j) % 2 == 1) {
                    if (i < 3) board[i][j] = BLACK;
                    else if (i > 4) board[i][j] = WHITE;
                }
            }
        }
    }

    void printBoard() {
        System.out.println("  H G F E D C B A");
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.out.print((8 - i) + " ");
            for (int j = BOARD_SIZE - 1; j >= 0; j--) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println(8 - i);
        }
        System.out.println("  H G F E D C B A");
    }

    int[] parseCoord(String coord) {
        int y = Character.toUpperCase(coord.charAt(0)) - 'A';
        int x = 8 - Character.getNumericValue(coord.charAt(1));
        return new int[]{x, y};
    }

    boolean isValidMove(int x1, int y1, int x2, int y2, char turn) {
        if (x2 < 0 || x2 >= BOARD_SIZE || y2 < 0 || y2 >= BOARD_SIZE) return false;
        char piece = board[x1][y1];
        if (board[x2][y2] != EMPTY) return false;
        if (Character.toLowerCase(piece) != turn) return false;

        boolean isKing = Character.isUpperCase(piece);
        int dx = x2 - x1;
        int dy = y2 - y1;

        if (!isKing && Math.abs(dx) == 1 && Math.abs(dy) == 1) {
            if ((turn == WHITE && dx == -1) || (turn == BLACK && dx == 1)) return true;
        } else if (!isKing && Math.abs(dx) == 2 && Math.abs(dy) == 2) {
            int mx = x1 + dx / 2, my = y1 + dy / 2;
            char middle = board[mx][my];
            if (Character.toLowerCase(middle) != turn && middle != EMPTY) return true;
        } else if (isKing) {
            if (Math.abs(dx) == Math.abs(dy)) {
                int steps = Math.abs(dx);
                for (int i = 1; i < steps; i++) {
                    if (board[x1 + i * Integer.signum(dx)][y1 + i * Integer.signum(dy)] != EMPTY)
                        return false;
                }
                return true;
            }
        }
        return false;
    }

    void makeMove(int x1, int y1, int x2, int y2) {
        char piece = board[x1][y1];
        board[x2][y2] = piece;
        board[x1][y1] = EMPTY;

        if (Math.abs(x2 - x1) == 2) {
            int mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
            board[mx][my] = EMPTY;
        }

        if (piece == WHITE && x2 == 0) board[x2][y2] = WHITE_KING;
        if (piece == BLACK && x2 == 7) board[x2][y2] = BLACK_KING;
    }

    List<Move> getAllMoves(char turn) {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (Character.toLowerCase(board[i][j]) == turn) {
                    for (int dx = -BOARD_SIZE; dx <= BOARD_SIZE; dx++) {
                        for (int dy = -BOARD_SIZE; dy <= BOARD_SIZE; dy++) {
                            if (dx != 0 && dy != 0 && Math.abs(dx) == Math.abs(dy)) {
                                int ni = i + dx, nj = j + dy;
                                if (isValidMove(i, j, ni, nj, turn)) {
                                    moves.add(new Move(i, j, ni, nj));
                                }
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    void aiMove() {
        List<Move> moves = getAllMoves(ai);
        if (moves.isEmpty()) {
            System.out.println("\n\tHas ganado, la IA no tiene movimientos!");
            System.exit(0);
        }

        System.out.println("\nLa IA está pensando...");

        // Paralelización estilo "forall"
        Optional<Move> optMove = moves.parallelStream().findAny();

        Move bestMove = optMove.orElse(moves.get(0)); // Fallback por si acaso
        makeMove(bestMove.x1, bestMove.y1, bestMove.x2, bestMove.y2);
        System.out.println("\nLa IA ha jugado.");
    }

    void play() {
        System.out.print("\nElige color (blancas / negras): ");
        String choice = sc.nextLine().toLowerCase();

        player = choice.equals("blancas") ? WHITE : BLACK;
        ai = (player == WHITE) ? BLACK : WHITE;

        if (player == BLACK) aiMove();

        while (true) {
            printBoard();
            System.out.print("\nIngresa tu movimiento (ej. A6 B5): ");
            String from = sc.next();
            String to = sc.next();
            int[] f = parseCoord(from);
            int[] t = parseCoord(to);
            if (!isValidMove(f[0], f[1], t[0], t[1], player)) {
                System.out.println("\nMovimiento no válido, intenta de nuevo.");
                continue;
            }
            makeMove(f[0], f[1], t[0], t[1]);
            aiMove();
        }
    }

    public static void main(String[] args) {
        new Checkers().play();
    }
}
