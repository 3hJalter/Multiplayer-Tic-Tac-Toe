package entity;

import lombok.Getter;

public class Board {
    private static final int X_SIZE = 24;
    private static final int Y_SIZE = 24;
    @Getter
    private final Cell[][] matrix = new Cell[X_SIZE][Y_SIZE];
    private Cell lastMove;

    public Board() {
        initMatrix();
    }

    private void initMatrix() {
        for (int i = 0; i < X_SIZE; i++) {
            for (int j = 0; j < Y_SIZE; j++) {
                matrix[i][j] = new Cell(i, j, CellValue.EMPTY);
            }
        }
    }

    public boolean setMatrix(int x, int y, CellValue value) {
        // If value is the same as last move, return false
        if (lastMove != null && value == lastMove.value) {
            return false;
        }
        // Check if the cell is empty
        if (matrix[x][y].value == CellValue.EMPTY) {
            matrix[x][y].value = value;
            lastMove = matrix[x][y];
            return true;
        }
        return false;
    }

    public EndGameType checkEndGame() {
        // Check with last move
        int x = lastMove.x;
        int y = lastMove.y;
        CellValue lastValue = lastMove.value;
        // Check horizontal
        int count = 0;
        for(int col = 0; col < Y_SIZE; col++){
            Cell cell = matrix[x][col];
            if (cell.value.equals(lastValue)) {
                count++;
                if(count == 5){
                    System.out.println("Horizontal");
                    return EndGameType.WIN;
                }
            }
            else {
                count = 0;
            }
        }

        // Check vertical
        count = 0;
        for(int row = 0; row < X_SIZE; row++){
            Cell cell = matrix[row][y];
            if (cell.value.equals(lastValue)) {
                count++;
                if(count == 5){
                    System.out.println("Vertical");
                    return EndGameType.WIN;
                }
            }
            else {
                count = 0;
            }
        }

        // Left diagonal
        int min = Math.min(x, y);
        int TopI = x - min;
        int TopJ = y - min;
        count = 0;
        for(;TopI < X_SIZE && TopJ < Y_SIZE; TopI++, TopJ++){
            Cell cell = matrix[TopI][TopJ];
            if (cell.value.equals(lastValue)) {
                count++;
                if(count == 5){
                    System.out.println("Left diagonal");
                    return EndGameType.WIN;
                }
            }else {
                count = 0;
            }
        }

        // Right diagonal
        min = Math.min(x, Y_SIZE - y - 1);
        TopI = x - min;
        TopJ = y + min;
        count = 0;
        for(;TopI < X_SIZE && TopJ >= 0; TopI++, TopJ--){
            Cell cell = matrix[TopI][TopJ];
            if (cell.value.equals(lastValue)) {
                count++;
                if(count == 5){
                    System.out.println("Right diagonal");
                    return EndGameType.WIN;
                }
            }else {
                count = 0;
            }
        }

        if(isFull()){
            return EndGameType.DRAW;
        }

        return EndGameType.NONE;
    }

    private boolean isFull() {
        for (Cell[] row : matrix) {
            for (Cell cell : row) {
                if (cell.value == CellValue.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    public static class Cell {
        public int x;
        public int y;
        public CellValue value;
        Cell(int x, int y, CellValue value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }
    }

    public enum CellValue {
        EMPTY,
        X, // First player
        O // Second player
    }

    public enum EndGameType {
        NONE, WIN, DRAW
    }
}
