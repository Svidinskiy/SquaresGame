package org.example;

import java.util.Scanner;

public class SquaresGame {

    private char[][] board;
    private int size;
    private Player[] players;
    private int currentPlayer;
    private boolean gameStarted;

    static class Player {
        String type;
        char color;
        Player(String type, char color) { this.type = type; this.color = color; }
    }

    public void processCommand(String command) {
        if (command.trim().isEmpty()) return;
        if (command.toUpperCase().startsWith("GAME")) {
            String[] parts = command.substring(4).trim().split("\\s*,\\s*");
            if (parts.length != 3) {
                System.out.println("Некорректная команда: ожидалось 3 параметра для GAME");
                return;
            }
            handleGameCommand(parts);
        }

    }

    private void handleGameCommand(String[] parts) {
        try {
            int newSize = Integer.parseInt(parts[0].trim());
            if (newSize <= 2) { System.out.println("Размер доски > 2"); return; }

            String[] p1Params = parts[1].trim().split("\\s+");
            String[] p2Params = parts[2].trim().split("\\s+");

            String p1Type = p1Params[0], p2Type = p2Params[0];
            char p1Color = p1Params[1].charAt(0), p2Color = p2Params[1].charAt(0);

            this.size = newSize;
            this.board = new char[size][size];
            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++)
                    board[i][j] = '.';

            this.players = new Player[2];
            this.players[0] = new Player(p1Type, p1Color);
            this.players[1] = new Player(p2Type, p2Color);
            this.currentPlayer = 0;
            this.gameStarted = true;
            System.out.println("Новая игра начата");
        } catch (NumberFormatException e) {
            System.out.println("Некорректный формат числа");
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
