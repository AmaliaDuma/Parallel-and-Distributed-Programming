import domain.Manager;
import domain.Polynomial;
import mpi.MPI;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Set-up
        MPI.Init(args);
        // Local process index (me)
        int rank = MPI.COMM_WORLD.Rank();
        // Total processes
        int size = MPI.COMM_WORLD.Size();

        System.out.println("Hi from <"+rank+">" + "and size is "+size);
        if (rank == 0) {
            // If master
//            Polynomial a = new Polynomial(3);
//            Polynomial b = new Polynomial(3);

            List<Integer> val = new ArrayList<>();
            for (int i=0; i<10; i++){
                val.add(1);
            }
            Polynomial a = new Polynomial(val);
            Polynomial b = new Polynomial(val);

            System.out.println("First: " + a);
            System.out.println("Second: " + b);
            master(a, b, size);
        } else {
            worker();
//            workerKaratsuba();
        }
        // Finish
        MPI.Finalize();
    }

    private static void master(Polynomial a, Polynomial b, Integer nrProcs) {
        long startTime = System.currentTimeMillis();
        int start, end = 0;
        int len = a.length() / (nrProcs - 1);

        // Split the data and send it to the other processes
        // We start from 1 because we want to keep some coefficients to be processed by master
        for (int i=1; i<nrProcs; i++) {
            start = end;
            end += len;
            if (i == nrProcs - 1) {
                end = a.length();
            }

            /*
              Send data -> we send the whole polynomials as discussed in lecture because we need to reach
            a lot of coefficients -> more convenient to send all

            Arguments: 1 -> object buffer
                       2 -> initial offset in send buffer
                       3 -> count of data that is sent (size)
                       4 -> data type
                       5 -> rank of destination process
                       6 -> tag
            */
            MPI.COMM_WORLD.Send(new Object[]{a}, 0, 1, MPI.OBJECT, i, 0);
            MPI.COMM_WORLD.Send(new Object[]{b}, 0, 1, MPI.OBJECT, i, 0);

            //   Send data -> start & end for which coefficients will be processed
            MPI.COMM_WORLD.Send(new int[]{start}, 0, 1, MPI.INT, i, 0);
            MPI.COMM_WORLD.Send(new int[]{end}, 0, 1, MPI.INT, i, 0);
        }

        Object[] results = new Object[nrProcs-1];
        // Receive data
        for (int i = 1; i < nrProcs; i++) {
            MPI.COMM_WORLD.Recv(results, i - 1, 1, MPI.OBJECT, i, 0);
        }

        Polynomial result = Manager.buildResult(results);
        long endTime = System.currentTimeMillis();
        System.out.println("result:\n" + result.toString());
        System.out.println("time: " + (endTime - startTime) + " ms");
    }

    private static void worker() {
        System.out.println("-----Worker started-----\n");

        Object[] a = new Object[2];
        Object[] b = new Object[2];
        int[] start = new int[1];
        int[] end = new int[1];

        // Receive data
        MPI.COMM_WORLD.Recv(a, 0, 1, MPI.OBJECT, 0, 0);
        MPI.COMM_WORLD.Recv(b, 0, 1, MPI.OBJECT, 0, 0);

        MPI.COMM_WORLD.Recv(start, 0, 1, MPI.INT, 0, 0);
        MPI.COMM_WORLD.Recv(end, 0, 1, MPI.INT, 0, 0);

        Polynomial result = Manager.multiplySimple(a[0], b[0], start[0], end[0]);

        // Send back the result
        MPI.COMM_WORLD.Send(new Object[]{result}, 0, 1, MPI.OBJECT, 0, 0);
    }

    private static void workerKaratsuba() {
        System.out.println("-----Worker started-----\n");

        Object[] a = new Object[2];
        Object[] b = new Object[2];
        int[] start = new int[1];
        int[] end = new int[1];

        // Receive data
        MPI.COMM_WORLD.Recv(a, 0, 1, MPI.OBJECT, 0, 0);
        MPI.COMM_WORLD.Recv(b, 0, 1, MPI.OBJECT, 0, 0);

        MPI.COMM_WORLD.Recv(start, 0, 1, MPI.INT, 0, 0);
        MPI.COMM_WORLD.Recv(end, 0, 1, MPI.INT, 0, 0);

        Polynomial first = (Polynomial) a[0];
        Polynomial second = (Polynomial) b[0];

        for (int i = 0; i < start[0]; i++) {
            first.values.set(i, 0);
        }
        for (int j = end[0]; j < first.values.size(); j++) {
            first.values.set(j, 0);
        }

        Polynomial result = Manager.karatsubaSeq(first, second);

        // Send back the result
        MPI.COMM_WORLD.Send(new Object[]{result}, 0, 1, MPI.OBJECT, 0, 0);
    }
}
