package domain;

public class Task implements Runnable{
    private final int start;
    private final int end;
    private final Polynomial a;
    private final Polynomial b;
    private final Polynomial result;

    public Task(int start, int end, Polynomial a, Polynomial b, Polynomial result) {
        this.start = start;
        this.end = end;
        this.a = a;
        this.b = b;
        this.result = result;
    }

    @Override
    public void run() {
        for (int index = start; index < end; index++) {
            // No more values to compute
            if (index > result.length()) {
                return;
            }
            // Get all pairs that we add to obtain the value of a result coefficient
            for (int j = 0; j <= index; j++) {
                if (j < a.length() && (index - j) < b.length()) {
                    int value = a.values.get(j) * b.values.get(index - j);
                    result.values.set(index, result.values.get(index) + value);
                }
            }
        }
    }
}
