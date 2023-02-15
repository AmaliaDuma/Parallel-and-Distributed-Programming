import domain.Manager;
import domain.Polynomial;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        List<Integer> val = new ArrayList<>();
        for (int i=0; i<10; i++){
            val.add(1);
        }
        Polynomial a = new Polynomial(val);
        Polynomial b = new Polynomial(val);


        System.out.println("First: " + a);
        System.out.println("Second: " + b);
        System.out.println("\n");

        simpleSeq(a, b);
        simpleTh(a, b);
        karatsubaSeq(a, b);
        karatsubaTh(a, b);
    }

    private static void simpleSeq(Polynomial a, Polynomial b) {
        long startTime = System.currentTimeMillis();

        Polynomial result = Manager.simpleSeq(a, b);
        System.out.println(result);

        long endTime = System.currentTimeMillis();
        System.out.println("Simple sequential multiplication");
        System.out.println("time: " + (endTime - startTime) + " ms.\n");
    }

    private static void simpleTh(Polynomial a, Polynomial b) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        Polynomial result = Manager.simpleTh(a, b);
        System.out.println(result);

        long endTime = System.currentTimeMillis();
        System.out.println("Simple parallel multiplication");
        System.out.println("time: " + (endTime - startTime) + " ms.\n");
    }

    private static void karatsubaSeq(Polynomial a, Polynomial b) {
        long startTime = System.currentTimeMillis();

        Polynomial result = Manager.karatsubaSeq(a, b);
        System.out.println(result);

        long endTime = System.currentTimeMillis();
        System.out.println("Karatsuba sequential multiplication");
        System.out.println("time: " + (endTime - startTime) + " ms.\n");
    }

    private static void karatsubaTh(Polynomial a, Polynomial b) throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();

        Polynomial result = Manager.karatsubaTh(a, b, 1);
        System.out.println(result);

        long endTime = System.currentTimeMillis();
        System.out.println("Karatsuba parallel multiplication");
        System.out.println("time: " + (endTime - startTime) + " ms.\n");
    }
}
