package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Manager {
    private static int threads = 5;
    private static int max_depth = 4;

    public static Polynomial multiplySimple(Object o, Object o1, int begin, int end) {
        Polynomial p = (Polynomial) o;
        Polynomial q = (Polynomial) o1;
        Polynomial result = Polynomial.buildEmptyPolynomial(p.degree*2 + 1);
        for (int i = begin; i < end; i++) {
            for (int j = 0; j < q.values.size(); j++) {
                result.values.set(i + j, result.values.get(i + j) + p.values.get(i) * q.values.get(j));
            }
        }
        return result;
    }

    public static Polynomial buildResult(Object[] polynomials) {
        int degree = ((Polynomial) polynomials[0]).degree;
        Polynomial result = Polynomial.buildEmptyPolynomial(degree+1);
        for (int i = 0; i < result.values.size(); i++) {
            for (Object polynomial : polynomials) {
                result.values.set(i, result.values.get(i) + ((Polynomial) polynomial).values.get(i));
            }
        }
        return result;
    }

    public static Polynomial simpleSeq(Polynomial a, Polynomial b) {
        // Take result size to create and initialize `coefficients` values
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

    public static Polynomial karatsubaSeq(Polynomial a, Polynomial b) {
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
}
