package org.example.console;

import org.example.core.Player;
import org.example.core.SquaresGame;

// Отвечает за разбор команд и их передачу движку
public class CommandProcessor {
    private final SquaresGame game;

    public CommandProcessor(SquaresGame game) { this.game = game; }

    public void process(String command) {
        if (command == null || command.trim().isEmpty()) {
            System.out.println("Incorrect command");
            return;
        }

        String trimmed = command.trim();
        String cmd;
        String[] parts;

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
                if (parts.length != 3) { System.out.println("Incorrect command"); return; }
                try {
                    int size = Integer.parseInt(parts[0].trim());
                    String[] p1Params = parts[1].trim().split("\\s+");
                    String[] p2Params = parts[2].trim().split("\\s+");
                    Player p1 = new Player(p1Params[0], p1Params[1].charAt(0));
                    Player p2 = new Player(p2Params[0], p2Params[1].charAt(0));
                    game.startNewGame(size, p1, p2);
                } catch (Exception e) {
                    System.out.println("Incorrect command");
                }
                break;
            case "MOVE":
                if (!game.isGameStarted()) {
                    System.out.println("Game not started");
                    return;
                }
                String args = trimmed.substring(4).trim();
                args = args.replace(",", " ");
                String[] moveParts = args.split("\\s+");
                if (moveParts.length != 2) {
                    System.out.println("Incorrect command");
                    return;
                }
                try {
                    int x = Integer.parseInt(moveParts[0].trim());
                    int y = Integer.parseInt(moveParts[1].trim());
                    game.makeMove(x, y);
                } catch (Exception e) {
                    System.out.println("Incorrect command");
                }
                break;
            case "HELP":
                String helpText = """
                    Available commands:
                    GAME N, U1, U2 - start a new game
                      N: board size (> 2)
                      U1, U2: player parameters (TYPE C)
                        TYPE: 'user' or 'comp'
                        C: color ('W' or 'B')
                    MOVE X, Y - make a move
                    EXIT - exit program
                    HELP - show this help message
                    """;
                System.out.print(helpText.stripTrailing());
                break;
            case "EXIT":
                System.exit(0);
                break;
            default:
                System.out.println("Incorrect command");
        }
    }
}
