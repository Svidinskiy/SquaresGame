package org.example.web.controller;

import org.example.core.SquaresGame;
import org.example.web.dto.BoardDto;
import org.example.web.dto.SimpleMoveDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GameController {

    @PostMapping("/nextMove")
    public ResponseEntity<SimpleMoveDto> nextMove(@RequestBody BoardDto boardDto) {
        try {
            if (boardDto.getSize() <= 2) {
                return ResponseEntity.badRequest().body(
                        new SimpleMoveDto(-1, -1, null, "Invalid board size"));
            }

            String data = boardDto.getData() != null ? boardDto.getData().replaceAll("\\s+", "") : "";
            if (data.length() != boardDto.getSize() * boardDto.getSize()) {
                return ResponseEntity.badRequest().body(
                        new SimpleMoveDto(-1, -1, null, "Invalid board data length"));
            }

            String nextColorStr = boardDto.getNextPlayerColor();
            if (nextColorStr == null || (!nextColorStr.equalsIgnoreCase("w") && !nextColorStr.equalsIgnoreCase("b"))) {
                return ResponseEntity.badRequest().body(
                        new SimpleMoveDto(-1, -1, null, "Invalid player color"));
            }

            SquaresGame game = new SquaresGame();
            char nextPlayer = Character.toUpperCase(nextColorStr.charAt(0));
            game.loadBoard(boardDto.getSize(), data, nextPlayer);

            String status = game.getGameStatus();
            if (!"ACTIVE".equals(status)) {
                String msg = switch (status) {
                    case "DRAW" -> "Game finished. Draw";
                    case "W", "B" -> "Game finished. " + status + " wins!";
                    default -> "Game finished";
                };
                int[][] winningSquare = game.getWinningSquare();
                return ResponseEntity.ok(new SimpleMoveDto(-1, -1, status.toLowerCase(), msg, winningSquare));
            }

            int[] move = game.findNextMove();
            if (move == null) {
                return ResponseEntity.ok(
                        new SimpleMoveDto(-1, -1, null, "No valid moves available"));
            }

            return ResponseEntity.ok(
                    new SimpleMoveDto(move[0], move[1], String.valueOf(Character.toLowerCase(nextPlayer)), "Move found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new SimpleMoveDto(-1, -1, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new SimpleMoveDto(-1, -1, null, "Internal server error: " + e.getMessage()));
        }
    }
}