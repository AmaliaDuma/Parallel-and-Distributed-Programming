package thread;

import domain.Matrix;
import domain.Pair;

public class ColumnThread extends BaseThread{
    public ColumnThread(Matrix a, Matrix b, Matrix c, int rowStart, int colStart, int sizeTask) {
        super(a, b, c, rowStart, colStart, sizeTask);
    }

    @Override
    public void getElemsToCompute() {
        int i = rowStart; int j = colStart;
        int size = sizeTask;
        while (size > 0 && i < c.getRows() && j < c.getCols()) {
            pairs.add(new Pair<>(i, j));
            i++; size--;
            if (i == c.getCols()) {
                i = 0; j++;
            }
        }
    }
}
