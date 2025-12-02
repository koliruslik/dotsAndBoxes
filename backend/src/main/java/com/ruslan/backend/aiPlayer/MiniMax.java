package com.ruslan.backend.aiPlayer;

import com.ruslan.backend.controllers.GameController;
import com.ruslan.backend.controllers.GameState;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.ruslan.backend.aiPlayer.Evaluator.evaluate;
import static com.ruslan.backend.aiPlayer.MoveGenerator.getAvailableMoves;
import static com.ruslan.backend.aiPlayer.MoveGenerator.simulate;

public class MiniMax {

    public static class AIResult {
        public GameController.LineDTO bestMove;
        public List<String> logs;

        public AIResult(GameController.LineDTO bestMove, List<String> logs) {
            this.bestMove = bestMove;
            this.logs = logs;
        }
    }

    public static class SearchStats {
        public int nodesVisited = 0;
        public int cutoffs = 0;
    }


    public static AIResult bestMove(GameState gs, int aiPlayer, int depth) {
        GameController.LineDTO best = null;
        int bestValue = Integer.MIN_VALUE;
        List<String> logs = new ArrayList<>();
        SearchStats stats = new SearchStats();

        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        List<GameController.LineDTO> moves = getAvailableMoves(gs);
        if (moves.isEmpty()) return new AIResult(null, logs);

        best = moves.get(0);

        logs.add(String.format("️ SETTINGS: Depth %d | Mode: %s",
                depth, (depth >= 4 ? "Hard" : (depth == 3 ? "Medium" : "Easy"))));
        logs.add(String.format(" Root moves to analyze: %d", moves.size()));
        logs.add("--------------------------------");

        int moveCounter = 1;
        for (GameController.LineDTO move : moves) {
            GameState next = simulate(gs, move, aiPlayer);
            stats.nodesVisited++;

            int moveValue = minimax(next, depth - 1, alpha, beta, aiPlayer, stats);

            String moveDesc = String.format("[%d,%d -> %d,%d]", move.x1, move.y1, move.x2, move.y2);
            logs.add(String.format("%d) %s = %d", moveCounter++, moveDesc, moveValue));

            if (moveValue > bestValue) {
                bestValue = moveValue;
                best = move;
                logs.add(String.format("    New Best: %d", bestValue));
            }

            if (bestValue > alpha) {
                alpha = bestValue;
            }
        }

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);

        logs.add("--------------------------------");
        logs.add(" COMPLEXITY STATS:");
        logs.add(String.format("   Nodes visited: %s", nf.format(stats.nodesVisited)));
        logs.add(String.format("   Pruning cutoffs: %s", nf.format(stats.cutoffs)));
        logs.add(String.format(" SELECTED: [%d,%d -> %d,%d]",
                best.x1, best.y1, best.x2, best.y2));

        return new AIResult(best, logs);
    }

    public static int minimax(GameState gs, int depth, int alpha, int beta, int aiPlayer, SearchStats stats) {
        List<GameController.LineDTO> moves = getAvailableMoves(gs);

        if (depth == 0 || moves.isEmpty()) {
            return evaluate(gs, aiPlayer);
        }

        boolean isMax = (gs.currentPlayer == aiPlayer);

        if (isMax) {
            int maxEval = Integer.MIN_VALUE;
            for (GameController.LineDTO move : moves) {
                GameState next = simulate(gs, move, gs.currentPlayer);
                stats.nodesVisited++;

                int eval = minimax(next, depth - 1, alpha, beta, aiPlayer, stats);

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);

                if (beta <= alpha) {
                    stats.cutoffs++;
                    break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (GameController.LineDTO move : moves) {
                GameState next = simulate(gs, move, gs.currentPlayer);
                stats.nodesVisited++;

                int eval = minimax(next, depth - 1, alpha, beta, aiPlayer, stats);

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);

                if (beta <= alpha) {
                    stats.cutoffs++;
                    break;
                }
            }
            return minEval;
        }
    }
}
