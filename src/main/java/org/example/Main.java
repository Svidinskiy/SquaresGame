package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        SquaresGame game = new SquaresGame();
        CommandProcessor processor = new CommandProcessor(game);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Starting SquaresGame. Enter commands (HELP for help):");
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            processor.process(command);
        }
        System.out.println("Input closed. Exiting program.");
        scanner.close();
    }
}