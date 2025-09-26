package org.example.console;

import org.example.core.Player;
import org.example.core.SquaresGame;

public class CommandProcessor {
    private final SquaresGame game;

    public CommandProcessor(SquaresGame game) {
        this.game = game;
    }

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
            String args = trimmed.substring(4).trim();
            if (!args.matches("\\d+\\s*,\\s*\\w+\\s+\\w\\s*,\\s*\\w+\\s+\\w")) {
                System.out.println("Invalid GAME command format");
                return;
            }
            parts = args.split("\\s*,\\s*");
        } else {
            parts = trimmed.split("\\s+");
            cmd = parts[0].trim().toUpperCase();
        }

        switch (cmd) {
            case "GAME":
                try {
                    int size = Integer.parseInt(parts[0].trim());
                    String[] p1Params = parts[1].trim().split("\\s+");
                    String[] p2Params = parts[2].trim().split("\\s+");

                    String p1Type = p1Params[0];
                    char p1Color = p1Params[1].charAt(0);

                    String p2Type = p2Params[0];
                    char p2Color = p2Params[1].charAt(0);

                    Player p1 = new Player(p1Type, p1Color);
                    Player p2 = new Player(p2Type, p2Color);

                    game.startNewGame(size, p1, p2);
                } catch (Exception e) {
                    System.out.println("Invalid parameters: " + e.getMessage());
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
                    System.out.println("Invalid move format");
                    return;
                }
                try {
                    int x = Integer.parseInt(moveParts[0].trim());
                    int y = Integer.parseInt(moveParts[1].trim());
                    game.makeMove(x, y);
                } catch (Exception e) {
                    System.out.println("Invalid move: " + e.getMessage());
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
                    
                    Examples:
                    GAME 5, user W, comp B
                    GAME 4, comp W, user B
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