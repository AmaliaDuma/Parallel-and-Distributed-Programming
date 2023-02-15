package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Polynomial implements Serializable {
    public List<Integer> values;
    public int degree;

    public Polynomial(List<Integer> values) {
        this.values = values;
        this.degree = values.size() - 1;
    }

    public Polynomial(Integer degree){
        this.degree = degree;
        values = new ArrayList<>();

        Random random = new Random();
        for (int i=0; i<=degree; i++) values.add(random.nextInt(10));
    }


    public static Polynomial add(Polynomial a, Polynomial b) {
        // Get the smaller and bigger degree of the polynomials
        int minDegree = Math.min(a.degree, b.degree);
        int maxDegree = Math.max(a.degree, b.degree);

        List<Integer> values = new ArrayList<>(maxDegree + 1);

        // For the same degree present in both polynomials, we add the values
        for (int i=0; i<= minDegree; i++) {
            values.add(a.values.get(i) + b.values.get(i));
        }

        // If one polynomial has a bigger degree, we add the remaining values to result
        if (a.degree > b.degree){
            addRemainingVal(a, minDegree, maxDegree, values);
        } else{
            addRemainingVal(b, minDegree, maxDegree, values);
        }

        return new Polynomial(values);
    }

    private static void addRemainingVal(Polynomial a, int minDegree, int maxDegree, List<Integer> values) {
        if (minDegree != maxDegree){
            for (int i=minDegree+1; i<=maxDegree; i++){
                values.add(a.values.get(i));
            }
        }
    }

    public static Polynomial addZeros(Polynomial polynomial, int offset) {
        List<Integer> values = IntStream.range(0, offset).mapToObj(i -> 0).collect(Collectors.toList());
        values.addAll(polynomial.values);
        return new Polynomial(values);
    }

    public static Polynomial subtract(Polynomial a, Polynomial b) {
        // Get the smaller and bigger degree of the polynomials
        int minDegree = Math.min(a.degree, b.degree);
        int maxDegree = Math.max(a.degree, b.degree);

        List<Integer> values = new ArrayList<>(maxDegree + 1);

        // For the same degree present in both polynomials, we subtract the values
        for (int i=0; i<= minDegree; i++) {
            values.add(a.values.get(i) - b.values.get(i));
        }

        //   If one polynomial has a bigger degree, we add the remaining values to result since we don't subtract
        // anything from those values
        if (a.degree > b.degree){
            addRemainingVal(a, minDegree, maxDegree, values);
        } else{
            addRemainingVal(b, minDegree, maxDegree, values);
        }

        // Remove coefficients values starting from bigger power if value is 0
        int i = values.size() - 1;
        while (values.get(i) == 0 && i > 0) {
            values.remove(i);
            i--;
        }

        return new Polynomial(values);
    }

    public static Polynomial buildEmptyPolynomial(int degree){
        List<Integer> zeros = IntStream.range(0, degree).mapToObj(i -> 0).collect(Collectors.toList());
        return new Polynomial(zeros);
    }


    public int length() {
        return values.size();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(values.get(0)).append(" +");
        int power = 1;
        for (int i = 1; i <= this.degree; i++) {
            if (values.get(i) == 0) {
                power++;
                continue;
            }
            str.append(" ").append(values.get(i)).append("x^").append(power).append(" +");
            power++;
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }
}
