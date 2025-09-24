package org.example.core;

// Класс доски, отвечает за хранение клеток и проверку заполненности
public class SquaresBoard {
    private final char[][] board;
    private final int size;

    public SquaresBoard(int size) {
        if (size <= 2) throw new IllegalArgumentException("Size must be > 2");
        this.size = size;
        board = new char[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                board[i][j] = '.';
    }

    public int getSize() {
        return size;
    }

    public char getCell(int x, int y) {
        if (!isInside(x, y)) throw new IllegalArgumentException("Coordinates out of bounds");
        return board[x][y];
    }

    public void setCell(int x, int y, char color) {
        if (!isInside(x, y)) throw new IllegalArgumentException("Coordinates out of bounds");
        board[x][y] = color;
    }

    public boolean isInside(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    public boolean isFull() {
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (board[i][j] == '.') return false;
        return true;
    }

    public void printBoard() {
        System.out.println("Current board state:");
        int width = Integer.toString(size - 1).length();
        System.out.print(" ".repeat(width + 2));
        for (int j = 0; j < size; j++)
            System.out.print(j + " ");
        System.out.println();

        for (int i = 0; i < size; i++) {
            System.out.printf("%" + width + "d ", i);
            for (int j = 0; j < size; j++)
                System.out.print(board[i][j] + " ");
            System.out.println();
        }
    }
}