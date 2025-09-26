package org.example.core;

import java.util.*;

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

        // 1 Немедленный выигрыш
        int[] move = findImmediateWin(myColor);
        if (move != null) {
            return move;
        }

        // 2 Блокировка выигрыша противника
        move = findImmediateWin(oppColor);
        if (move != null) {
            return move;
        }

        // 3 Создание двойных угроз
        move = findDoubleThreat(myColor, oppColor);
        if (move != null) {
            return move;
        }

        // 4 Стратегический ход
        move = findStrategicMove(myColor, oppColor);
        if (move != null) {
            return move;
        }

        return findWeightedRandomMove();
    }

    private int[] findStrategicMove(char myColor, char oppColor) {
        int bestScore = -1;
        int[] bestMove = null;
        int size = board.getSize();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (board.getCell(x, y) != '.') continue;

                int score = evaluateMoveStrategic(x, y, myColor, oppColor);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = new int[]{x, y};
                }
            }
        }

        return bestMove;
    }

    private int evaluateMoveStrategic(int x, int y, char myColor, char oppColor) {
        int score = 0;
        int size = board.getSize();

        int center = size / 2;
        int distanceFromCenter = Math.abs(x - center) + Math.abs(y - center);
        score += (size - distanceFromCenter) * 3;

        // Потенциал создания квадратов 2x2
        for (int dx = -1; dx <= 0; dx++) {
            for (int dy = -1; dy <= 0; dy++) {
                int startX = x + dx;
                int startY = y + dy;

                if (startX >= 0 && startX < size-1 && startY >= 0 && startY < size-1) {
                    int myCount = 0, oppCount = 0;

                    for (int i = startX; i <= startX+1; i++) {
                        for (int j = startY; j <= startY+1; j++) {
                            char c = board.getCell(i, j);
                            if (c == myColor) myCount++;
                            else if (c == oppColor) oppCount++;
                        }
                    }

                    if (myCount == 3 && oppCount == 0) score += 100;
                    if (myCount == 2 && oppCount == 0) score += 20;
                    if (oppCount == 2 && myCount == 0) score += 15;
                    if (myCount == 1 && oppCount == 0) score += 5;
                }
            }
        }

        return score;
    }

    private int[] findImmediateWin(char color) {
        int size = board.getSize();

        // Проверка квадратов 2x2
        for (int x = 0; x < size - 1; x++) {
            for (int y = 0; y < size - 1; y++) {
                int[][] square = {{x, y}, {x+1, y}, {x, y+1}, {x+1, y+1}};
                int emptyCount = 0;
                int[] emptyCell = null;
                boolean valid = true;

                for (int[] cell : square) {
                    char c = board.getCell(cell[0], cell[1]);
                    if (c == '.') {
                        emptyCount++;
                        emptyCell = cell;
                    } else if (c != color) {
                        valid = false;
                        break;
                    }
                }

                if (valid && emptyCount == 1) return emptyCell;
            }
        }

        List<int[]> cells = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board.getCell(i, j) == color) cells.add(new int[]{i, j});
            }
        }

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

                    int emptyCount = 0;
                    int[] emptyCell = null;
                    int[][] points = {{x1, y1}, {x2, y2}, {x3, y3}, {x4, y4}};
                    boolean validSquare = true;

                    for (int[] p : points) {
                        int px = p[0], py = p[1];
                        if (!board.isInside(px, py)) {
                            validSquare = false;
                            break;
                        }
                        char c = board.getCell(px, py);
                        if (c == '.') {
                            emptyCount++;
                            emptyCell = new int[]{px, py};
                        } else if (c != color) {
                            validSquare = false;
                            break;
                        }
                    }
                    if (validSquare && emptyCount == 1) return emptyCell;
                }
            }
        }
        return null;
    }

    private int[] findDoubleThreat(char myColor, char oppColor) {
        int size = board.getSize();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (board.getCell(x, y) != '.') continue;

                board.setCell(x, y, myColor);
                int threatCount = 0;

                for (int i = 0; i < size - 1; i++) {
                    for (int j = 0; j < size - 1; j++) {
                        int myCount = 0;
                        boolean valid = true;
                        for (int di = 0; di <= 1; di++) {
                            for (int dj = 0; dj <= 1; dj++) {
                                char c = board.getCell(i + di, j + dj);
                                if (c == '.') {
                                } else if (c != myColor) {
                                    valid = false;
                                } else {
                                    myCount++;
                                }
                            }
                        }
                        if (valid && myCount == 3) threatCount++;
                    }
                }

                board.setCell(x, y, '.');

                if (threatCount >= 2) return new int[]{x, y};
            }
        }
        return null;
    }

    private int[] findWeightedRandomMove() {
        List<int[]> emptyCells = new ArrayList<>();
        int size = board.getSize();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (board.getCell(x, y) == '.') {
                    int weight = calculateCellWeight(x, y, size);
                    for (int i = 0; i < weight; i++) {
                        emptyCells.add(new int[]{x, y});
                    }
                }
            }
        }

        return emptyCells.isEmpty() ? null : emptyCells.get(random.nextInt(emptyCells.size()));
    }

    private int calculateCellWeight(int x, int y, int size) {
        int weight = 1;

        int center = size / 2;
        int distanceFromCenter = Math.abs(x - center) + Math.abs(y - center);
        weight += (size - distanceFromCenter);

        // Небольшой случайный элемент
        weight += random.nextInt(3);

        return Math.max(1, weight);
    }

    private void placePiece(int x, int y) {
        board.setCell(x, y, getCurrentPlayer().getColor());
        System.out.printf("%c (%d, %d)%n", getCurrentPlayer().getColor(), x, y);
        board.printBoard();
    }

    private void handleComputerTurns() {
        while (gameStarted && getCurrentPlayer().isComputer()) {
            int[] move = findNextMove();
            if (move == null) break;

            placePiece(move[0], move[1]);
            checkGameState();
            if (gameStarted) switchPlayer();
        }
    }

    private void checkGameState() {
        if (checkWinner(getCurrentPlayer().getColor(), true)) {
            if (winningSquare != null) {
                for (int[] cell : winningSquare) System.out.print("(" + cell[0] + "," + cell[1] + ") ");
                System.out.println();
            }
            gameStarted = false;
        } else if (board.isFull()) {
            gameStarted = false;
        }
    }

    private void switchPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % 2;
    }

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
                            if (saveWinningSquare) winningSquare = new int[][]{{x1, y1}, {x2, y2}, {x3, y3}, {x4, y4}};
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

    public int[][] getWinningSquare() { return winningSquare; }
}