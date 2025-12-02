package com.ruslan.backend.controllers;

import com.ruslan.backend.aiPlayer.MiniMax;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ruslan.backend.aiPlayer.MiniMax.bestMove;

@RestController
public class GameController {
    GameState gs;

    public static class PlayerDTO {
        public String playerId;
        public List<LineDTO> lines;
        public LineDTO move;
        public int rows;
        public int cols;
        public int difficulty;
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
        if (gs == null) {
            response.put("lines", new ArrayList<>());
            response.put("squares", new ArrayList<>());
            response.put("currentPlayer", 1);
        } else {
            response.put("lines", gs.lines);
            response.put("squares", gs.squares);
            response.put("currentPlayer", gs.currentPlayer);
        }
        return response;
    }

    @PostMapping("/make-move")
    public Map<String, Object> makeMove(@RequestBody PlayerDTO player) {
        if (gs == null) {
            gs = new GameState();
            gs.rows = player.rows;
            gs.cols = player.cols;
            gs.currentPlayer = 1;
            gs.lines = new ArrayList<>();
            gs.squares = new ArrayList<>();
        }

        return applyMove(player.move, gs.currentPlayer, null);
    }

    @PostMapping("/ai-move")
    public Map<String, Object> aiMove(@RequestBody PlayerDTO player) {
        int aiPlayer = 2;

        int depth = player.difficulty > 0 ? Math.min(player.difficulty, 5) : 3;

        long startTime = System.currentTimeMillis();

        MiniMax.AIResult result = MiniMax.bestMove(gs, aiPlayer, depth);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        if (result.bestMove == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "No moves left");
            return response;
        }

        result.logs.add(0, String.format(" Calculation time: %d ms (Depth: %d)", duration, depth));

        return applyMove(result.bestMove, aiPlayer, result.logs);
    }

    @PostMapping("/restart")
    public Map<String, Object> restartGame() {
        gs.lines.clear();
        gs.squares.clear();
        gs.currentPlayer = 1;

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("lines", gs.lines);
        response.put("squares", gs.squares);
        response.put("currentPlayer", gs.currentPlayer);
        return response;
    }

    @PostMapping("/set-size")
    public Map<String, Object> setSize(@RequestBody Map<String, Integer> body) {
        int rows = body.get("rows");
        int cols = body.get("cols");

        if (gs == null) gs = new GameState();
        gs.rows = rows;
        gs.cols = cols;
        gs.lines.clear();
        gs.squares.clear();
        gs.currentPlayer = 1;

        return Map.of(
                "success", true,
                "rows", rows,
                "cols", cols,
                "lines", gs.lines,
                "squares", gs.squares,
                "currentPlayer", gs.currentPlayer
        );
    }

    private Map<String, Object> applyMove(LineDTO move, int playerNumber, List<String> logs) {
        Map<String, Object> response = new HashMap<>();
        move.playerNumber = playerNumber;
        gs.lines.add(move);

        int before = gs.squares.size();
        checkSquares(gs);
        int after = gs.squares.size();

        if (after == before) {
            gs.currentPlayer = gs.currentPlayer == 1 ? 2 : 1;
        }

        int totalSquares = (gs.rows - 1) * (gs.cols - 1);
        String winner = null;
        if (gs.squares.size() == totalSquares) {
            long p1 = gs.squares.stream().filter(s -> s.playerNumber == 1).count();
            long p2 = gs.squares.stream().filter(s -> s.playerNumber == 2).count();
            if (p1 > p2) winner = "player1";
            else if (p2 > p1) winner = "player2";
            else winner = "draw";
        }

        response.put("success", true);
        response.put("lines", gs.lines);
        response.put("squares", gs.squares);
        response.put("currentPlayer", gs.currentPlayer);
        response.put("winner", winner);

        if (logs != null) {
            response.put("aiLogs", logs);
        }

        return response;
    }

    public static void checkSquares(GameState gs) {
        for (int i = 0; i < gs.rows - 1; i++) {
            for (int j = 0; j < gs.cols - 1; j++) {
                final int fi = i;
                final int fj = j;

                boolean alreadyClosed = gs.squares.stream()
                        .anyMatch(sq -> sq.x == fj && sq.y == fi);
                if (alreadyClosed) continue;

                boolean top = gs.lines.stream().anyMatch(l -> lMatches(l, fj, fi, fj + 1, fi));
                boolean bottom = gs.lines.stream().anyMatch(l -> lMatches(l, fj, fi + 1, fj + 1, fi + 1));
                boolean left = gs.lines.stream().anyMatch(l -> lMatches(l, fj, fi, fj, fi + 1));
                boolean right = gs.lines.stream().anyMatch(l -> lMatches(l, fj + 1, fi, fj + 1, fi + 1));

                if (top && bottom && left && right) {
                    SquareDTO sq = new SquareDTO();
                    sq.x = fj;
                    sq.y = fi;
                    sq.playerNumber = gs.currentPlayer;
                    gs.squares.add(sq);
                }
            }
        }
    }

    private static boolean lMatches(LineDTO l, int x1, int y1, int x2, int y2) {
        return (l.x1 == x1 && l.y1 == y1 && l.x2 == x2 && l.y2 == y2) ||
                (l.x1 == x2 && l.y1 == y2 && l.x2 == x1 && l.y2 == y1);
    }
}

