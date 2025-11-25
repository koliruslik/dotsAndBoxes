package com.ruslan.backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GameController {
    private int currentPlayer = 1;
    private final List<LineDTO> lines = new ArrayList<>();
    private final List<SquareDTO> squares = new ArrayList<>();

    public static class PlayerDTO {
        public String playerId;
        public List<LineDTO> lines;
        public LineDTO move;
        public int rows;
        public int cols;
    }

    public static class LineDTO {
        public int x1, y1, x2, y2;
        public int playerNumber;
    }

    public static class SquareDTO {
        public int x;
        public int y;
        public int playerNumber;
    }

    @GetMapping("/get-state")
    public Map<String, Object> getState() {
        Map<String, Object> response = new HashMap<>();
        response.put("lines", lines);
        response.put("squares", squares);
        response.put("currentPlayer", currentPlayer);
        return response;
    }

    @PostMapping("/make-move")
    public Map<String, Object> makeMove(@RequestBody PlayerDTO player) {
        Map<String, Object> response = new HashMap<>();

        player.move.playerNumber = currentPlayer;
        lines.add(player.move);

        checkSquares(lines, currentPlayer, player.rows, player.cols);

        currentPlayer = currentPlayer == 1 ? 2 : 1;

        response.put("success", true);
        response.put("lines", lines);
        response.put("squares", squares);
        return response;
    }
    @PostMapping("/restart")
    public Map<String, Object> restartGame() {
        lines.clear();
        squares.clear();
        currentPlayer = 1;

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("lines", lines);
        response.put("squares", squares);
        response.put("currentPlayer", currentPlayer);
        return response;
    }

    private void checkSquares(List<LineDTO> lines, int playerNumber, int rows, int cols) {
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < cols - 1; j++) {
                final int fi = i;
                final int fj = j;

                boolean alreadyClosed = squares.stream()
                        .anyMatch(sq -> sq.x == fj && sq.y == fi);
                if (alreadyClosed) continue;

                boolean top = lines.stream().anyMatch(l -> lMatches(l, fj, fi, fj + 1, fi));
                boolean bottom = lines.stream().anyMatch(l -> lMatches(l, fj, fi + 1, fj + 1, fi + 1));
                boolean left = lines.stream().anyMatch(l -> lMatches(l, fj, fi, fj, fi + 1));
                boolean right = lines.stream().anyMatch(l -> lMatches(l, fj + 1, fi, fj + 1, fi + 1));

                if (top && bottom && left && right) {
                    SquareDTO sq = new SquareDTO();
                    sq.x = fj;
                    sq.y = fi;
                    sq.playerNumber = playerNumber;
                    squares.add(sq);
                }
            }
        }
    }

    private boolean lMatches(LineDTO l, int x1, int y1, int x2, int y2) {
        return (l.x1 == x1 && l.y1 == y1 && l.x2 == x2 && l.y2 == y2) ||
                (l.x1 == x2 && l.y1 == y2 && l.x2 == x1 && l.y2 == y1);
    }
}

