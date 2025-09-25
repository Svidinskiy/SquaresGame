package org.example.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SquaresGame {
    private SquaresBoard board;
    private Player[] players;
    private int currentPlayerIndex;
    private boolean gameStarted;
    private int[][] winningSquare;
    private final Random random = new Random();

    public SquaresGame() {
        this.players = new Player[2];
        this.currentPlayerIndex = 0;
        this.gameStarted = false;
    }

    public void startNewGame(int size, Player p1, Player p2) {
        if (p1.getColor() == p2.getColor())
            throw new IllegalArgumentException("Players cannot have the same color");
        this.board = new SquaresBoard(size);
        this.players[0] = p1;
        this.players[1] = p2;
        this.currentPlayerIndex = 0;
        this.gameStarted = true;
        this.winningSquare = null;
        System.out.println("New game started");

        handleComputerTurns();
    }

    public void loadBoard(int size, String data, char nextPlayerColor) {
        if (size <= 2) throw new IllegalArgumentException("Size must be > 2");
        if (data.length() != size * size) throw new IllegalArgumentException("Invalid board data length");
        if (nextPlayerColor != 'W' && nextPlayerColor != 'B') throw new IllegalArgumentException("Invalid player color");

        this.board = new SquaresBoard(size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                char cell = data.charAt(i * size + j);
                if (cell != '.' && cell != ' ' && cell != 'W' && cell != 'B' && cell != 'w' && cell != 'b') {
                    throw new IllegalArgumentException("Invalid character in board data: " + cell);
                }
                board.setCell(i, j, cell == ' ' || cell == '.' ? '.' : Character.toUpperCase(cell));
            }
        }

        this.players[0] = new Player("comp", nextPlayerColor);
        this.players[1] = new Player("comp", nextPlayerColor == 'W' ? 'B' : 'W');
        this.currentPlayerIndex = 0;
        this.gameStarted = true;
        this.winningSquare = null;
    }

    public boolean isGameStarted() { return gameStarted; }

    public Player getCurrentPlayer() { return players[currentPlayerIndex]; }

    public void makeMove(int x, int y) {
        if (!gameStarted) throw new IllegalStateException("Game not started");

        if (!board.isInside(x, y))
            throw new IllegalArgumentException("Coordinates out of board");
        if (board.getCell(x, y) != '.')
            throw new IllegalArgumentException("Cell already occupied");

        placePiece(x, y);
        checkGameState();

        if (gameStarted) {
            switchPlayer();
            if (getCurrentPlayer().isComputer()) {
                handleComputerTurns();
            }
        }
    }

    public int[] findNextMove() {
        if (!gameStarted) throw new IllegalStateException("Game not started");

        char myColor = getCurrentPlayer().getColor();
        char oppColor = players[(currentPlayerIndex + 1) % 2].getColor();

        int[] move = findWinningMove(myColor);
        if (move == null) move = findWinningMove(oppColor);
        if (move == null) move = pickRandomMove();

        return move;
    }

    private void placePiece(int x, int y) {
        board.setCell(x, y, getCurrentPlayer().getColor());
        System.out.printf("%c (%d, %d)%n", getCurrentPlayer().getColor(), x, y);
        board.printBoard();
    }

    private void handleComputerTurns() {
        while (gameStarted && getCurrentPlayer().isComputer()) {
            char myColor = getCurrentPlayer().getColor();
            char oppColor = players[(currentPlayerIndex + 1) % 2].getColor();

            int[] move = findWinningMove(myColor);
            if (move == null) move = findWinningMove(oppColor);
            if (move == null) move = pickRandomMove();

            if (move == null) break;

            placePiece(move[0], move[1]);
            checkGameState();
            if (gameStarted) {
                switchPlayer();
            }
        }
    }

    private int[] findWinningMove(char color) {
        List<int[]> cells = new ArrayList<>();
        int size = board.getSize();
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (board.getCell(i, j) == color)
                    cells.add(new int[]{i, j});

        for (int i = 0; i < cells.size(); i++) {
            for (int j = i + 1; j < cells.size(); j++) {
                int x1 = cells.get(i)[0], y1 = cells.get(i)[1];
                int x2 = cells.get(j)[0], y2 = cells.get(j)[1];

                int dx = x2 - x1;
                int dy = y2 - y1;

                int[][] variants = {{-dy, dx}, {dy, -dx}};
                for (int[] v : variants) {
                    int vx = v[0], vy = v[1];
                    int x3 = x1 + vx, y3 = y1 + vy;
                    int x4 = x2 + vx, y4 = y2 + vy;

                    int emptyCount = 0;
                    int[] emptyCell = null;
                    int[][] points = {{x1, y1}, {x2, y2}, {x3, y3}, {x4, y4}};
                    for (int[] p : points) {
                        int px = p[0], py = p[1];
                        if (!board.isInside(px, py)) { emptyCount = -1; break; }
                        char c = board.getCell(px, py);
                        if (c == '.') { emptyCount++; emptyCell = new int[]{px, py}; }
                        else if (c != color) { emptyCount = -1; break; }
                    }
                    if (emptyCount == 1) return emptyCell;
                }
            }
        }
        return null;
    }

    private int[] pickRandomMove() {
        List<int[]> emptyCells = new ArrayList<>();
        int size = board.getSize();
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (board.getCell(i, j) == '.') emptyCells.add(new int[]{i, j});
        if (emptyCells.isEmpty()) return null;
        return emptyCells.get(random.nextInt(emptyCells.size()));
    }

    private void checkGameState() {
        if (checkWinner(getCurrentPlayer().getColor(), true)) {
            System.out.printf("Game finished. %c wins!%n", getCurrentPlayer().getColor());
            if (winningSquare != null) {
                System.out.print("Winning square coordinates: ");
                for (int[] cell : winningSquare)
                    System.out.print("(" + cell[0] + "," + cell[1] + ") ");
                System.out.println();
            }
            gameStarted = false;
        } else if (board.isFull()) {
            System.out.println("Game finished. Draw");
            gameStarted = false;
        }
    }

    private void switchPlayer() { currentPlayerIndex = (currentPlayerIndex + 1) % 2; }

    private boolean checkWinner(char color, boolean saveWinningSquare) {
        List<int[]> cells = new ArrayList<>();
        int size = board.getSize();
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (board.getCell(i, j) == color) cells.add(new int[]{i, j});

        for (int i = 0; i < cells.size(); i++) {
            for (int j = i + 1; j < cells.size(); j++) {
                int x1 = cells.get(i)[0], y1 = cells.get(i)[1];
                int x2 = cells.get(j)[0], y2 = cells.get(j)[1];
                int dx = x2 - x1, dy = y2 - y1;

                int[][] variants = {{-dy, dx}, {dy, -dx}};
                for (int[] v : variants) {
                    int vx = v[0], vy = v[1];
                    int x3 = x1 + vx, y3 = y1 + vy;
                    int x4 = x2 + vx, y4 = y2 + vy;

                    if (board.isInside(x3, y3) && board.isInside(x4, y4)) {
                        if (board.getCell(x3, y3) == color && board.getCell(x4, y4) == color) {
                            if (saveWinningSquare) {
                                winningSquare = new int[][]{{x1, y1}, {x2, y2}, {x3, y3}, {x4, y4}};
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public SquaresBoard getBoard() { return board; }

    public String getGameStatus() {
        if (board == null) return "ACTIVE";
        if (checkWinner('W', false)) return "W";
        if (checkWinner('B', false)) return "B";
        if (board.isFull()) return "DRAW";
        return "ACTIVE";
    }

    public int[][] getWinningSquare() {
        return winningSquare;
    }
}