import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Buffer {
    private Queue<Integer> queue = new LinkedList<>();
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public Buffer() {}

    public void put(int value) throws InterruptedException{
        lock.lock();
        try{
            while (queue.size() == 2){
                // If queue full, wait for consumer to do his job
                System.out.printf("%s : Buffer is currently full.\n", Thread.currentThread().getName());
                condition.await();
            }
            // Otherwise, produce a new value
            queue.add(value);
            System.out.printf("%s added %d into the queue %n", Thread.currentThread().getName(), value);

            // Signal the changes
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    public Integer get() throws InterruptedException{
        lock.lock();
        try{
            while(queue.size() == 0){
                // If queue is empty wait for producer to do his job
                System.out.printf("%s : Buffer is currently empty.\n", Thread.currentThread().getName());
                condition.await();
            }
            // Otherwise consume a value
            Integer value = queue.poll();
            System.out.printf("%s extracted value %d from the queue \n", Thread.currentThread().getName(), value);

            //Signal the changes
            condition.signal();
            return value;
        } finally {
            lock.unlock();
        }
    }

}
