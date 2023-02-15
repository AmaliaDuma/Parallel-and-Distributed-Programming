package thread;

import domain.Matrix;
import domain.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseThread extends Thread {
    protected List<Pair<Integer, Integer>> pairs;
    protected final Matrix a,b,c;
    protected final int rowStart, colStart, sizeTask; //sizeTask - count of pairs
    protected int k;

    public BaseThread(Matrix a, Matrix b, Matrix c, int rowStart, int colStart, int sizeTask, int k){
        this.a = a;
        this.b = b;
        this.c = c;
        this.rowStart = rowStart;
        this.colStart = colStart;
        this.sizeTask = sizeTask;
        this.k = k;
        this.pairs = new ArrayList<>();
        getElemsToCompute();
    }

    public BaseThread(Matrix a, Matrix b, Matrix c, int rowStart, int colStart, int sizeTask){
        this.a = a;
        this.b = b;
        this.c = c;
        this.rowStart = rowStart;
        this.colStart = colStart;
        this.sizeTask = sizeTask;
        this.pairs = new ArrayList<>();
        getElemsToCompute();
    }

    public abstract void getElemsToCompute();

    @Override
    public void run() {
        for (Pair<Integer, Integer> pair : pairs){
            int i = pair.first;
            int j = pair.second;
            try{
                if (i < a.getRows() && j < b.getCols()){
                    int val = 0;
                    for (int index=0; index < a.getCols(); index++) {
                        val += a.getElement(i, index) * b.getElement(index, j);
                    }
                    c.setElement(i, j, val);
                }
                else{
                    throw new Exception("Row/col out of bounds.");
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
