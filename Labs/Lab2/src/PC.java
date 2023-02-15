import java.util.ArrayList;
import java.util.List;

public class PC {
    private final Buffer buffer;

    // Producer parts
    List<Integer> v1 = new ArrayList<>();
    List<Integer> v2 = new ArrayList<>();

    // Consumer parts
    private Integer sum;

    public PC(Buffer buffer){
        this.buffer = buffer;
        this.sum = 0;
    }

    public void produce(){
        for (int i=0; i<v1.size(); ++i){
            try{
                System.out.printf("Producer: Sending %d * %d = %d\n", v1.get(i), v2.get(i), v1.get(i) * v2.get(i));
                buffer.put(v1.get(i) * v2.get(i));
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public void consume(){
        for (int i=0; i<v1.size(); ++i){
            try{
                sum += buffer.get();
                System.out.printf("Consumer: Sum is %d\n", sum);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        System.out.printf("Final sum is %d\n", sum);
    }

    public void run(){
//        v1.add(1);
//        v1.add(2);
//        v1.add(3);
//        v1.add(4);
//        v1.add(5);
        for(int i=0; i<1000; i++){
            v1.add(1);
            v2.add(1);
        }

//        v2.add(2);
//        v2.add(2);
//        v2.add(2);
//        v2.add(2);
//        v2.add(2);

        Thread producer = new Thread(this::produce, "Producer");
        Thread consumer = new Thread(this::consume, "Consumer");

        producer.start();
        consumer.start();

        try{
            producer.join();
            consumer.join();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
