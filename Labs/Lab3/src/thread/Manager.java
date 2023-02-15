package thread;

import domain.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Manager {
    private static final int rows_a = 1000, cols_a = 1000, rows_b = 1000, cols_b = 1000;
    private static final int noThreads = 12;
    private static final boolean threadPoolApproach = false;
    private static final String taskType = "Row";

    public static void run() {
        Matrix a = new Matrix(rows_a, cols_a);
        Matrix b = new Matrix(rows_b, cols_b);

        //System.out.println(a); System.out.println(b);

        float start = System.nanoTime();
        Matrix c = new Matrix(a.getRows(), b.getCols());
        if (threadPoolApproach){
            try {
                runPoolApproach(a, b, c);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            try {
                runListApproach(a, b, c);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.println("Time elapsed: " + (System.nanoTime() - start)/1_000_000_000.0 + " seconds");
    }

    private static void runListApproach(Matrix a, Matrix b, Matrix c) {
        List<Thread> threads = new ArrayList<>();

        if (taskType.equals("Row")) {
            for (int i=0; i<noThreads; i++){
                threads.add(getRowThread(i, a, b, c));
            }
        } else if (taskType.equals("Column")) {
            for (int i=0; i<noThreads; i++) {
                threads.add(getColumnThread(i, a, b, c));
            }
        }
        else {
            for (int i=0; i<noThreads; i++) {
                threads.add(getKThread(i, a, b, c));
            }
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //System.out.println("Final Matrix\n" + c.toString());
    }

    private static void runPoolApproach(Matrix a, Matrix b, Matrix c) {
        ExecutorService service = Executors.newFixedThreadPool(noThreads);

        if (taskType.equals("Row")) {
            for (int i=0; i<noThreads; i++){
                service.submit(getRowThread(i, a, b, c));
            }
        } else if (taskType.equals("Column")) {
            for (int i=0; i<noThreads; i++) {
                service.submit(getColumnThread(i, a, b, c));
            }
        }
        else {
            for (int i=0; i<noThreads; i++) {
                service.submit(getKThread(i, a, b, c));
            }
        }

        service.shutdown();
        try {
            if (!service.awaitTermination(300, TimeUnit.SECONDS)) {
                service.shutdownNow();
            }
            System.out.println("result:\n" + c.toString());
        } catch (InterruptedException ex) {
            service.shutdownNow();
            ex.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }


    private static BaseThread getRowThread(int index, Matrix a, Matrix b, Matrix c) {
        // Splits the element to be computed in the following way:
        //   • consecutive elements, going row after row

        int resSize = c.getRows() * c.getCols();
        // resSize will be the total no of elements we will have in the resulting matrix
        int count = resSize / noThreads;
        // split by the number of threads

        int rowStart = count * index / c.getCols();
        int colStart = count * index % c.getCols();

        if (index == noThreads -1) {
            // if last thread we add the remaining elements in case the result size is not a multiple of the threads no
            count += resSize % noThreads;
        }
        return new RowThread(a, b, c, rowStart, colStart, count);
    }

    private static BaseThread getColumnThread(int index, Matrix a, Matrix b, Matrix c) {
        // Splits the element to be computed in the following way:
        //   • consecutive elements, going column after column

        int resSize = c.getRows() * c.getCols();
        // resSize will be the total no of elements we will have in the resulting matrix
        int count = resSize / noThreads;
        // split by the number of threads

        int rowStart = count * index / c.getRows();
        int colStart = count * index % c.getRows();

        if (index == noThreads -1) {
            // if last thread we add the remaining elements in case the result size is not a multiple of the threads n
            count += resSize % noThreads;
        }
        return new ColumnThread(a, b, c, rowStart, colStart, count);
    }

    private static BaseThread getKThread(int index, Matrix a, Matrix b, Matrix c) {
        // Splits the element to be computed in the following way:
        //   • every k-th element row by row

        int resSize = c.getRows() * c.getCols();
        // resSize will be the total no of elements we will have in the resulting matrix
        int count = resSize / noThreads;
        // split by the number of threads

        if (index < resSize % noThreads)
            count++;

        int rowStart = index / c.getCols();
        int colStart = index % c.getCols();
        return new KThread(a, b, c , rowStart, colStart, count, noThreads);
    }
}
