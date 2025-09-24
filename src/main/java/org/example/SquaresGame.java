package org.example;

import java.util.Scanner;

public class SquaresGame {
    private char[][] board;
    private int size;
    private Player[] players;
    private boolean gameStarted;

    static class Player {
        String type;
        char color;

        Player(String type, char color) {
            this.type = type;
            this.color = color;
        }
    }

    public SquaresGame() {
        this.gameStarted = false;
        this.players = new Player[2];
    }

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
            case "HELP":
                if (parts.length != 1) {
                    System.out.println("Некорректная команда: ожидалась только HELP");
                    return;
                }
                printHelp();
                break;
            case "EXIT":
                if (parts.length != 1) {
                    System.out.println("Некорректная команда: ожидалась только EXIT");
                    return;
                }
                System.exit(0);
                break;
            default:
                System.out.println("Некорректная команда: неизвестная команда " + cmd);
        }
    }

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
            this.gameStarted = true;
            System.out.println("Новая игра начата");
        } catch (NumberFormatException e) {
            System.out.println("Некорректная команда: неверный формат числа");
        }
    }

    private boolean isValidType(String type) {
        return type.equals("user") || type.equals("comp");
    }

    private boolean isValidColor(char color) {
        return color == 'W' || color == 'B';
    }

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