package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Manager {
    private static int threads = 5;
    private static int max_depth = 4;

    public static Polynomial simpleSeq(Polynomial a, Polynomial b) {
        // Take result size to create and initialize coefficients values
        int sizeResult = a.degree + b.degree + 1;
        List<Integer> values = new ArrayList<>();
        IntStream.range(0, sizeResult).forEach(i -> values.add(0));

        // Multiply the 2 polynomials
        for (int i = 0; i < a.length(); i++) {
            for (int j = 0; j < b.length(); j++) {
                // Index corresponds to the power of x
                int index = i + j;
                int value = a.values.get(i) * b.values.get(j);
                values.set(index, values.get(index) + value);
            }
        }

        // Create new polynomials with the calculated values and return it
        return new Polynomial(values);
    }

    public static Polynomial simpleTh(Polynomial a, Polynomial b) throws InterruptedException {
        // Take result size to create and initialize coefficients values
        int sizeResult = a.degree + b.degree + 1;
        List<Integer> values = new ArrayList<>();
        IntStream.range(0, sizeResult).forEach(i -> values.add(0));

        // Create polynomial where we will store the result
        Polynomial result = new Polynomial(values);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);

        // Get how many computations for each thread
        int step = result.length() / threads;
        if (step == 0) step = 1;

        // Divide the pairs to be computed between the threads
        for (int i = 0; i < result.length(); i += step) {
            Task task = new Task(i, i + step, a, b, result);
            executor.execute(task);
        }

        executor.shutdown();
        executor.awaitTermination(50, TimeUnit.SECONDS);
        return result;

    }

    public static Polynomial karatsubaSeq(Polynomial a, Polynomial b) {
        long startTime = System.currentTimeMillis();

        // Best case, we end recursion
        if (a.degree < 2 || b.degree < 2) {
            return simpleSeq(a, b);
        }

        // Apply the algorithm of karatsuba
        int len = Math.max(a.degree, b.degree) / 2;
        Polynomial lowP1 = new Polynomial(a.values.subList(0, len));
        Polynomial highP1 = new Polynomial(a.values.subList(len, a.length()));
        Polynomial lowP2 = new Polynomial(b.values.subList(0, len));
        Polynomial highP2 = new Polynomial(b.values.subList(len, b.length()));

        Polynomial z1 = karatsubaSeq(lowP1, lowP2);
        Polynomial z2 = karatsubaSeq(Polynomial.add(lowP1, highP1), Polynomial.add(lowP2, highP2));
        Polynomial z3 = karatsubaSeq(highP1, highP2);

        // Calculate the final result
        Polynomial r1 = Polynomial.addZeros(z3, 2 * len);
        Polynomial r2 = Polynomial.addZeros(Polynomial.subtract(Polynomial.subtract(z2, z3), z1), len);
        return Polynomial.add(Polynomial.add(r1, r2), z1);
    }

    public static Polynomial karatsubaTh(Polynomial a, Polynomial b, int currentDepth) throws ExecutionException, InterruptedException {
        if (currentDepth > max_depth) {
            return karatsubaSeq(a, b);
        }

        if (a.degree < 2 || b.degree < 2) {
            return karatsubaSeq(a, b);
        }

        // Apply the algorithm of karatsuba
        int len = Math.max(a.degree, b.degree) / 2;
        Polynomial lowP1 = new Polynomial(a.values.subList(0, len));
        Polynomial highP1 = new Polynomial(a.values.subList(len, a.length()));
        Polynomial lowP2 = new Polynomial(b.values.subList(0, len));
        Polynomial highP2 = new Polynomial(b.values.subList(len, b.length()));

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        Future<Polynomial> f1 = executor.submit(() -> karatsubaTh(lowP1, lowP2, currentDepth + 1));
        Future<Polynomial> f2 = executor.submit(() -> karatsubaTh(Polynomial.add(lowP1, highP1), Polynomial
                .add(lowP2, highP2), currentDepth + 1));
        Future<Polynomial> f3 = executor.submit(() -> karatsubaTh(highP1, highP2, currentDepth + 1));

        executor.shutdown();

        // Get the values from the futures when computation complete
        Polynomial z1 = f1.get();
        Polynomial z2 = f2.get();
        Polynomial z3 = f3.get();

        executor.awaitTermination(60, TimeUnit.SECONDS);

        // Calculate the final result
        Polynomial r1 = Polynomial.addZeros(z3, 2 * len);
        Polynomial r2 = Polynomial.addZeros(Polynomial.subtract(Polynomial.subtract(z2, z3), z1), len);
        return Polynomial.add(Polynomial.add(r1, r2), z1);
    }
}
