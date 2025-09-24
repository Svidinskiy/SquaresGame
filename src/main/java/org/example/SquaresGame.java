package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class SquaresGame {
    private char[][] board;
    private int size;
    private Player[] players;
    private int currentPlayer;
    private boolean gameStarted;
    private final Random random = new Random();
    private int[][] winningSquare = null;

    static class Player {
        String type;
        char color;

        Player(String type, char color) {
            this.type = type;
            this.color = color;
        }
    }

    // Конструктор класса игры
    public SquaresGame() {
        this.gameStarted = false;
        this.currentPlayer = 0;
        this.players = new Player[2];
    }

    // Обрабатывает введённые пользователем команды
    public void processCommand(String command) {
        if (command.trim().isEmpty()) return;

        String cmd;
        String[] parts;

        String trimmed = command.trim();
        if (trimmed.toUpperCase().startsWith("GAME")) {
            cmd = "GAME";
            String argsPart = trimmed.substring(4).trim();
            parts = argsPart.split("\\s*,\\s*");
        } else {
            parts = trimmed.split("\\s+");
            cmd = parts[0].trim().toUpperCase();
        }

        switch (cmd) {
            case "GAME":
                if (parts.length != 3) {
                    System.out.println("Некорректная команда: ожидалось 3 параметра для GAME");
                    return;
                }
                handleGameCommand(parts);
                break;
            case "MOVE":
                if (parts.length != 3) {
                    System.out.println("Некорректная команда: ожидалось 3 части для MOVE");
                    return;
                }
                handleMoveCommand(parts);
                break;
            case "EXIT":
                if (parts.length != 1) {
                    System.out.println("Некорректная команда: ожидалась только EXIT");
                    return;
                }
                System.exit(0);
                break;
            case "HELP":
                if (parts.length != 1) {
                    System.out.println("Некорректная команда: ожидалась только HELP");
                    return;
                }
                printHelp();
                break;
            default:
                System.out.println("Некорректная команда: неизвестная команда " + cmd);
        }
    }

    // Обрабатывает команду начала новой игры
    private void handleGameCommand(String[] parts) {
        try {
            int newSize = Integer.parseInt(parts[0].trim());
            if (newSize <= 2) {
                System.out.println("Некорректная команда: размер доски должен быть > 2");
                return;
            }

            String[] p1Params = parts[1].trim().split("\\s+");
            String[] p2Params = parts[2].trim().split("\\s+");

            if (p1Params.length != 2 || p2Params.length != 2) {
                System.out.println("Некорректная команда: параметры игроков заданы неверно");
                return;
            }

            String p1Type = p1Params[0], p2Type = p2Params[0];
            char p1Color = p1Params[1].charAt(0), p2Color = p2Params[1].charAt(0);

            if (!isValidType(p1Type) || !isValidType(p2Type) ||
                    !isValidColor(p1Color) || !isValidColor(p2Color) ||
                    p1Color == p2Color) {
                System.out.println("Некорректная команда: недопустимые цвета или типы игроков");
                return;
            }

            if (gameStarted) {
                System.out.println("Игра в процессе. Текущая игра будет сброшена.");
            }

            this.size = newSize;
            this.board = new char[size][size];
            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++)
                    board[i][j] = '.';

            this.players[0] = new Player(p1Type, p1Color);
            this.players[1] = new Player(p2Type, p2Color);
            this.currentPlayer = 0;
            this.gameStarted = true;
            System.out.println("Новая игра начата");

            if (players[0].type.equals("comp")) makeComputerMove();

        } catch (NumberFormatException e) {
            System.out.println("Некорректная команда: неверный формат числа");
        }
    }

    // Проверяет, корректен ли тип игрока
    private boolean isValidType(String type) {
        return type.equals("user") || type.equals("comp");
    }

    // Проверяет, корректен ли цвет игрока
    private boolean isValidColor(char color) {
        return color == 'W' || color == 'B';
    }

    // Обрабатывает команду хода игрока
    private void handleMoveCommand(String[] parts) {
        if (!gameStarted) {
            System.out.println("Некорректная команда: игра еще не начата");
            return;
        }
        try {
            int x = Integer.parseInt(parts[1].replace(",", "").trim());
            int y = Integer.parseInt(parts[2].replace(",", "").trim());

            if (players[currentPlayer].type.equals("comp")) {
                System.out.println("Некорректная команда: сейчас ход компьютера");
                return;
            }

            makeMove(x, y);

        } catch (NumberFormatException e) {
            System.out.println("Некорректная команда: неверные координаты");
        }
    }

    // Выполняет ход игрока
    private void makeMove(int x, int y) {
        if (!isInside(x, y)) {
            System.out.println("Некорректная команда: координаты вне доски");
            return;
        }
        if (board[x][y] != '.') {
            System.out.println("Некорректная команда: клетка уже занята");
            return;
        }

        char color = players[currentPlayer].color;
        board[x][y] = color;
        System.out.printf("%c (%d, %d)%n", color, x, y);
        printBoard();

        if (checkWinner(color)) {
            System.out.printf("Игра окончена. Победили %c%n", color);
            if (winningSquare != null) {
                System.out.print("Координаты выигрышного квадрата: ");
                for (int[] cell : winningSquare) {
                    System.out.print("(" + cell[0] + "," + cell[1] + ") ");
                }
                System.out.println();
            }
            gameStarted = false;
            return;
        }

        if (isBoardFull()) {
            System.out.println("Игра окончена. Ничья");
            gameStarted = false;
            return;
        }

        currentPlayer = (currentPlayer + 1) % 2;
        if (players[currentPlayer].type.equals("comp")) makeComputerMove();
    }

    // Выполняет ход компьютера с поиском выигрышного хода или случайного
    private void makeComputerMove() {
        char myColor = players[currentPlayer].color;
        char oppColor = players[(currentPlayer + 1) % 2].color;

        int[] move = findWinningMove(myColor);
        if (move == null) {
            move = findWinningMove(oppColor);
        }
        if (move == null) {
            List<int[]> emptyCells = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (board[i][j] == '.') {
                        emptyCells.add(new int[]{i, j});
                    }
                }
            }
            if (!emptyCells.isEmpty()) {
                move = emptyCells.get(random.nextInt(emptyCells.size()));
            }
        }

        if (move != null) {
            makeMove(move[0], move[1]);
        }
    }

    // Ищет потенциальный выигрышный ход для указанного цвета
    private int[] findWinningMove(char color) {
        List<int[]> cells = new ArrayList<>();
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (board[i][j] == color) cells.add(new int[]{i, j});

        for (int i = 0; i < cells.size(); i++) {
            for (int j = i + 1; j < cells.size(); j++) {
                int x1 = cells.get(i)[0], y1 = cells.get(i)[1];
                int x2 = cells.get(j)[0], y2 = cells.get(j)[1];

                int dx = x2 - x1;
                int dy = y2 - y1;

                int[][] variants = { {-dy, dx}, {dy, -dx} };
                for (int[] v : variants) {
                    int vx = v[0], vy = v[1];
                    int x3 = x1 + vx, y3 = y1 + vy;
                    int x4 = x2 + vx, y4 = y2 + vy;

                    int emptyCount = 0;
                    int[] emptyCell = null;
                    int[][] points = {{x1,y1},{x2,y2},{x3,y3},{x4,y4}};
                    for (int[] p : points) {
                        int px = p[0], py = p[1];
                        if (!isInside(px, py)) {
                            emptyCount = -1;
                            break;
                        }
                        if (board[px][py] == '.') {
                            emptyCount++;
                            emptyCell = new int[]{px, py};
                        } else if (board[px][py] != color) {
                            emptyCount = -1;
                            break;
                        }
                    }
                    if (emptyCount == 1) {
                        return emptyCell;
                    }
                }
            }
        }
        return null;
    }

    // Проверяет, создал ли игрок выигрышный квадрат
    private boolean checkWinner(char color) {
        List<int[]> cells = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == color) {
                    cells.add(new int[]{i, j});
                }
            }
        }

        for (int i = 0; i < cells.size(); i++) {
            for (int j = i + 1; j < cells.size(); j++) {
                int x1 = cells.get(i)[0], y1 = cells.get(i)[1];
                int x2 = cells.get(j)[0], y2 = cells.get(j)[1];

                int dx = x2 - x1;
                int dy = y2 - y1;

                int[][] variants = { {-dy, dx}, {dy, -dx} };
                for (int[] v : variants) {
                    int vx = v[0], vy = v[1];
                    int x3 = x1 + vx, y3 = y1 + vy;
                    int x4 = x2 + vx, y4 = y2 + vy;

                    if (isInside(x3, y3) && isInside(x4, y4)) {
                        if (board[x3][y3] == color && board[x4][y4] == color) {
                            winningSquare = new int[][]{{x1, y1}, {x2, y2}, {x3, y3}, {x4, y4}};
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // Проверяет, находится ли координата внутри границ доски
    private boolean isInside(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    // Проверяет, заполнена ли вся доска
    private boolean isBoardFull() {
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (board[i][j] == '.') return false;
        return true;
    }

    // Выводит справку
    private void printHelp() {
        System.out.println("""
            Доступные команды:
            GAME N, U1, U2 - начать новую игру
              N: размер доски (> 2)
              U1, U2: параметры игроков (TYPE C)
                TYPE: 'user' или 'comp'
                C: цвет ('W' или 'B')
            MOVE X, Y - сделать ход в координаты (X, Y)
            EXIT - выход из программы
            HELP - показать это описание
            """);
    }

    // Печатает текущее состояние доски
    private void printBoard() {
        System.out.println("Текущее состояние доски:");
        int width = Integer.toString(size - 1).length();
        System.out.print(" ".repeat(width + 2));
        for (int j = 0; j < size; j++)
            System.out.print(j + " ");
        System.out.println();

        for (int i = 0; i < size; i++) {
            System.out.printf("%" + width + "d ", i);
            for (int j = 0; j < size; j++)
                System.out.print(board[i][j] + " ");
            System.out.println();
        }
    }

    public static void main(String[] args) {
        SquaresGame game = new SquaresGame();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Запуск игры SquaresGame. Введите команды (HELP для справки):");
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            game.processCommand(command);
        }
        System.out.println("Ввод закрыт. Завершение работы.");
        scanner.close();
    }
}