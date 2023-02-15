package thread;

import domain.Matrix;
import domain.Pair;

public class KThread extends BaseThread{
    public KThread(Matrix a, Matrix b, Matrix c, int rowStart, int colStart, int sizeTask, int k) {
        super(a, b, c, rowStart, colStart, sizeTask, k);
    }

    @Override
    public void getElemsToCompute() {
        int i = rowStart; int j = colStart;
        int size = sizeTask;
        while (size > 0 && i < c.getRows()) {
            pairs.add(new Pair<>(i, j));
            size--;
            i += (j+k) / c.getCols();
            j += (i+k) / c.getRows();
        }
    }
}
