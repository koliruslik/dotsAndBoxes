package com.ruslan.backend.controllers;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    public int currentPlayer;
    public List<GameController.LineDTO> lines;
    public List<GameController.SquareDTO> squares;
    public int rows;
    public int cols;

    public GameState(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.currentPlayer = 1;
        this.lines = new ArrayList<>();
        this.squares = new ArrayList<>();
    }

    public GameState() {}

    public GameState copy() {
        GameState g = new GameState();
        g.currentPlayer = this.currentPlayer;

        g.lines = new ArrayList<>();
        for (GameController.LineDTO l : this.lines) {
            GameController.LineDTO nl = new GameController.LineDTO();
            nl.x1 = l.x1;
            nl.y1 = l.y1;
            nl.x2 = l.x2;
            nl.y2 = l.y2;
            nl.playerNumber = l.playerNumber;
            g.lines.add(nl);
        }

        g.squares = new ArrayList<>();
        for (GameController.SquareDTO s : this.squares) {
            GameController.SquareDTO ns = new GameController.SquareDTO();
            ns.x = s.x;
            ns.y = s.y;
            ns.playerNumber = s.playerNumber;
            g.squares.add(ns);
        }

        g.rows = this.rows;
        g.cols = this.cols;

        return g;
    }
}
