package com.ruslan.backend.aiPlayer;

import com.ruslan.backend.controllers.GameController;
import com.ruslan.backend.controllers.GameState;

import static com.ruslan.backend.aiPlayer.MoveGenerator.*;

public class Evaluator {
    public static int evaluate(GameState gs, int player) {
        int mySquares = (int) gs.squares.stream().filter(s -> s.playerNumber == player).count();
        int enemySquares = (int) gs.squares.stream().filter(s -> s.playerNumber != player).count();

        int score = (mySquares - enemySquares) * 100;

        int dangerMyMoves = countBadMoves(gs, player);
        int dangerEnemyMoves = countBadMoves(gs, 3 - player);

        score -= dangerMyMoves * 15;
        score += dangerEnemyMoves * 15;

        score += countPotentialSquares(gs, player) * 3;
        score -= countPotentialSquares(gs, 3 - player) * 3;

        return score;
    }
    private static int countBadMoves(GameState gs, int player) {
        int count = 0;

        for (GameController.LineDTO m : getAvailableMoves(gs)) {
            GameState ns = simulate(gs, m, player);
            int newSquares = ns.squares.size() - gs.squares.size();
            if (newSquares == 0) {
                if (createsThirdSide(gs, m)) count++;
            }
        }

        return count;
    }

    private static boolean createsThirdSide(GameState gs, GameController.LineDTO move) {
        int x1 = move.x1, y1 = move.y1;
        int x2 = move.x2, y2 = move.y2;

        int affected = 0;

        for (int dy = -1; dy <= 0; dy++) {
            for (int dx = -1; dx <= 0; dx++) {
                int sx = Math.min(x1, x2);
                int sy = Math.min(y1, y2);

                int x = sx + dx;
                int y = sy + dy;

                if (x < 0 || y < 0 || x >= gs.cols - 1 || y >= gs.rows - 1) continue;

                int sides = 0;

                if (exists(gs, x, y, x + 1, y)) sides++;
                if (exists(gs, x, y + 1, x + 1, y + 1)) sides++;
                if (exists(gs, x, y, x, y + 1)) sides++;
                if (exists(gs, x + 1, y, x + 1, y + 1)) sides++;

                if (sides == 2) return true;
            }
        }

        return false;
    }

    private static int countPotentialSquares(GameState gs, int player) {
        int count = 0;

        for (int i = 0; i < gs.rows - 1; i++) {
            for (int j = 0; j < gs.cols - 1; j++) {

                boolean top = exists(gs, j, i, j+1, i);
                boolean bottom = exists(gs, j, i+1, j+1, i+1);
                boolean left = exists(gs, j, i, j, i+1);
                boolean right = exists(gs, j+1, i, j+1, i+1);

                int sides = (top?1:0) + (bottom?1:0) + (left?1:0) + (right?1:0);

                if (sides == 2) count++;
            }
        }

        return count;
    }
}
