package com.ruslan.backend.aiPlayer;

import com.ruslan.backend.controllers.GameController;
import com.ruslan.backend.controllers.GameState;

import java.util.ArrayList;
import java.util.List;

import static com.ruslan.backend.controllers.GameController.checkSquares;

public class MoveGenerator {
    public static List<GameController.LineDTO> getAvailableMoves(GameState gs) {
        List<GameController.LineDTO> moves = new ArrayList<>();

        for (int y = 0; y < gs.rows; y++) {
            for (int x = 0; x < gs.cols - 1; x++) {
                final int fx = x;
                final int fy = y;
                if (!exists(gs, x, y, x + 1, y)) {
                    moves.add(new GameController.LineDTO(){{
                        x1 = fx; y1 = fy; x2 = fx + 1; y2 = fy;
                    }});
                }
            }
        }

        for (int y = 0; y < gs.rows - 1; y++) {
            for (int x = 0; x < gs.cols; x++) {
                final int fx = x;
                final int fy = y;
                if (!exists(gs, x, y, x, y + 1)) {
                    moves.add(new GameController.LineDTO(){{
                        x1 = fx; y1 = fy; x2 = fx; y2 = fy + 1;
                    }});
                }
            }
        }

        return moves;
    }

    public static boolean exists(GameState gs, int x1, int y1, int x2, int y2) {
        return gs.lines.stream().anyMatch(
                l -> (l.x1 == x1 && l.y1 == y1 && l.x2 == x2 && l.y2 == y2)
                        || (l.x1 == x2 && l.y1 == y2 && l.x2 == x1 && l.y2 == y1)
        );
    }

    public static  GameState simulate(GameState gs, GameController.LineDTO move, int player) {
        GameState ns = gs.copy();

        GameController.LineDTO nm = new GameController.LineDTO();
        nm.x1 = move.x1;
        nm.y1 = move.y1;
        nm.x2 = move.x2;
        nm.y2 = move.y2;
        nm.playerNumber = player;

        ns.lines.add(nm);

        int before = ns.squares.size();
        checkSquares(ns);
        int after = ns.squares.size();

        if (after == before)
            ns.currentPlayer = 3 - player;
        else
            ns.currentPlayer = player;

        return ns;
    }
}
