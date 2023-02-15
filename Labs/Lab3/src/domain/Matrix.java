package domain;

import java.util.Random;
import java.util.stream.IntStream;

public class Matrix {
    private final int rows, cols;
    private int[][] matrix;

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.matrix = new int[rows][cols];

        fillMatrix();
    }

    private void fillMatrix(){
        Random random = new Random();
        IntStream.range(0, rows).forEach(i -> {
            IntStream.range(0, cols).forEach(j -> {
                matrix[i][j] = 1;
            });
        });
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public int getElement(int row, int col) {
        return matrix[row][col];
    }

    public void setElement(int row, int col, int val) {
        matrix[row][col] = val;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        IntStream.range(0, rows).forEach(i -> {
            IntStream.range(0, cols).forEach(j -> {
                builder.append(matrix[i][j]).append(" ");
            });
            builder.append("\n");
        });
        return builder.toString();
    }
}
