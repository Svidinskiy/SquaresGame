import org.example.console.CommandProcessor;
import org.example.core.Player;
import org.example.core.SquaresBoard;
import org.example.core.SquaresGame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class SquaresGameTest {
    private SquaresGame game;
    private CommandProcessor processor;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        game = new SquaresGame();
        processor = new CommandProcessor(game);
        System.setOut(new PrintStream(outContent));
    }

    // Проверяет запуск новой игры с корректными параметрами
    @Test
    void testStartNewGame() {
        processor.process("GAME 3, user W, user B");
        assertTrue(game.isGameStarted());
        assertEquals("New game started\n", normalizeOutput(outContent.toString()));
    }

    // Проверяет обработку некорректных команд GAME
    @Test
    void testInvalidGameCommand() {
        processor.process("GAME 2, user W, user B");
        assertFalse(game.isGameStarted());
        assertEquals("Incorrect command\n", normalizeOutput(outContent.toString()));

        outContent.reset();
        processor.process("GAME 3, foo W, user B");
        assertFalse(game.isGameStarted());
        assertEquals("Incorrect command\n", normalizeOutput(outContent.toString()));

        outContent.reset();
        processor.process("GAME 3, user W, user W");
        assertFalse(game.isGameStarted());
        assertEquals("Incorrect command\n", normalizeOutput(outContent.toString()));
    }

    // Проверяет попытку сделать ход до старта игры
    @Test
    void testMoveBeforeGameStart() {
        processor.process("MOVE 0, 0");
        assertEquals("Game not started\n", normalizeOutput(outContent.toString()));
    }

    // Проверяет корректный пользовательский ход
    @Test
    void testValidUserMove() {
        processor.process("GAME 3, user W, user B");
        outContent.reset();
        processor.process("MOVE 0, 0");
        String expected =
                "W (0, 0)\n" +
                        "Current board state:\n" +
                        "   0 1 2\n" +
                        "0 W . .\n" +
                        "1 . . .\n" +
                        "2 . . .\n";
        assertEquals(expected, normalizeOutput(outContent.toString()));
        assertEquals('W', game.getBoard().getCell(0, 0));
    }

    // Проверяет обработку некорректных ходов
    @Test
    void testInvalidMove() {
        processor.process("GAME 3, user W, user B");
        outContent.reset();
        processor.process("MOVE 3, 0");
        assertEquals("Incorrect command\n", normalizeOutput(outContent.toString()));

        outContent.reset();
        processor.process("MOVE 0, 0");
        outContent.reset();
        processor.process("MOVE 0, 0");
        assertEquals("Incorrect command\n", normalizeOutput(outContent.toString()));
    }

    // Проверяет корректное определение победы игрока
    @Test
    void testWinCondition() {
        processor.process("GAME 3, user W, user B");
        outContent.reset();
        processor.process("MOVE 0, 0"); // W
        processor.process("MOVE 2, 2"); // B
        processor.process("MOVE 0, 1"); // W
        processor.process("MOVE 2, 1"); // B
        processor.process("MOVE 1, 0"); // W
        processor.process("MOVE 2, 0"); // B
        processor.process("MOVE 1, 1"); // W

        String expectedOutput = """
W (0, 0)
Current board state:
   0 1 2 
0 W . . 
1 . . . 
2 . . . 
B (2, 2)
Current board state:
   0 1 2 
0 W . . 
1 . . . 
2 . . B 
W (0, 1)
Current board state:
   0 1 2 
0 W W . 
1 . . . 
2 . . B 
B (2, 1)
Current board state:
   0 1 2 
0 W W . 
1 . . . 
2 . B B 
W (1, 0)
Current board state:
   0 1 2 
0 W W . 
1 W . . 
2 . B B 
B (2, 0)
Current board state:
   0 1 2 
0 W W . 
1 W . . 
2 B B B 
W (1, 1)
Current board state:
   0 1 2 
0 W W . 
1 W W . 
2 B B B 
Game finished. W wins!
Winning square coordinates: (0,0) (0,1) (1,0) (1,1) 
""";

        assertEquals(expectedOutput, normalizeOutput(outContent.toString()));
        assertFalse(game.isGameStarted());
    }

    // Проверяет корректное определение ничьей
    @Test
    void testDrawCondition() {
        processor.process("GAME 3, user W, user B");
        outContent.reset();
        int[][] moves = {{0, 0}, {0, 1}, {1, 0}, {0, 2}, {2, 0}, {1, 1}, {1, 2}, {2, 1}, {2, 2}};
        for (int[] move : moves) {
            processor.process("MOVE " + move[0] + ", " + move[1]);
        }
        String output = normalizeOutput(outContent.toString());
        assertTrue(output.contains("Game finished. Draw\n"));
        assertFalse(game.isGameStarted());
    }

    // Проверяет автоматический ход компьютера
    @Test
    void testComputerMove() {
        processor.process("GAME 3, comp W, user B");
        String output = normalizeOutput(outContent.toString());
        assertTrue(output.startsWith("New game started\nW ("));
        boolean hasW = false;
        SquaresBoard board = game.getBoard();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board.getCell(i, j) == 'W') {
                    hasW = true;
                    break;
                }
            }
        }
        assertTrue(hasW, "Board should have at least one 'W' after computer move");
    }

    // Проверяет игру между двумя компьютерами
    @Test
    void testCompVsComp() {
        processor.process("GAME 3, comp W, comp B");
        String output = normalizeOutput(outContent.toString());
        assertTrue(output.contains("New game started"));
        assertTrue(output.contains("Game finished"));
        assertFalse(game.isGameStarted());
    }

    // Проверяет вывод команды HELP
    @Test
    void testHelpCommand() {
        processor.process("HELP");
        String expected =
                "Available commands:\n" +
                        "GAME N, U1, U2 - start a new game\n" +
                        "  N: board size (> 2)\n" +
                        "  U1, U2: player parameters (TYPE C)\n" +
                        "    TYPE: 'user' or 'comp'\n" +
                        "    C: color ('W' or 'B')\n" +
                        "MOVE X, Y - make a move\n" +
                        "EXIT - exit program\n" +
                        "HELP - show this help message\n";
        assertEquals(expected, normalizeOutput(outContent.toString()));
    }

    // Проверяет валидацию параметров игрока
    @Test
    void testPlayerValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Player("foo", 'W'));
        assertThrows(IllegalArgumentException.class, () -> new Player("user", 'Z'));
        Player p = new Player("USER", 'w');
        assertEquals("user", p.getType());
        assertEquals('W', p.getColor());
    }

    // Проверяет базовую валидацию доски и установку клеток
    @Test
    void testBoardValidation() {
        assertThrows(IllegalArgumentException.class, () -> new SquaresBoard(2));
        SquaresBoard board = new SquaresBoard(3);
        assertThrows(IllegalArgumentException.class, () -> board.getCell(3, 0));
        assertThrows(IllegalArgumentException.class, () -> board.setCell(0, 3, 'W'));
        assertFalse(board.isFull());
        board.setCell(0, 0, 'W');
        assertEquals('W', board.getCell(0, 0));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private String normalizeOutput(String output) {
        return output.replace("\r\n", "\n")
                .lines()
                .map(String::stripTrailing)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("") + "\n";
    }
}
