import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Bank {
    public List<Account> accounts;
    public AtomicInteger sn;

    private static final int THREADS = 10;
    private static final int ACCOUNTS = 100;
    private static final int OPERATIONS = 1000;
    private static final int INIT_BALANCE = 5000;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private boolean check = false;

    public Bank() {
        sn = new AtomicInteger();
        int id = 0;
        accounts = new ArrayList<>();
        for (int i = 0; i < ACCOUNTS; ++i){
            accounts.add(new Account(id++, 300));
        }
    }

    public void threadTransfer(int threadId) {
        Random random = new Random();
        for (long op = 0; op < OPERATIONS; ++op){
            int accId1 = random.nextInt(ACCOUNTS);
            int accId2 = random.nextInt(ACCOUNTS);

            if (accId1 == accId2) {
                --op; continue;
            }

            int amount = random.nextInt(INIT_BALANCE / 500);

            lock.readLock().lock();
            int serialNr = sn.getAndIncrement();
            String result = accounts.get(accId1).makeTransfer(accounts.get(accId2), amount, serialNr);
            System.out.println(MessageFormat.format("Thread[{0}]: {1}", threadId, result));
            lock.readLock().unlock();

        }
    }

    public void threadCheck() {
        lock.writeLock().lock();
        System.out.println("Running correctness check");
        AtomicInteger failedAcc = new AtomicInteger();

        accounts.forEach(acc -> {
            if (!acc.checkBalance()) failedAcc.getAndIncrement();

        });

        for (Account acc : accounts) {
            for (Operation op : acc.operations){
                Account destAcc = accounts.get(op.destId);
                if (!destAcc.operations.contains(op)) failedAcc.getAndIncrement();
            }
        }

        if (failedAcc.get() > 0) {
            throw new RuntimeException("Correctness check failed! " + failedAcc.get() + " accounts have failed");
        }
        System.out.println("Finished correctness check");
        lock.writeLock().unlock();
    }

    public void run() {
        var start = LocalDateTime.now();

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            int threadId = i;
            threads.add(new Thread(() -> { this.threadTransfer(threadId);}));
        }
        threads.forEach(Thread::start);


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                threadCheck();
            }
        }, 0, 1);

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        });

        timer.cancel();

        threadCheck();

        var end = LocalDateTime.now();
        var seconds = ChronoUnit.SECONDS.between(start, end);
        System.out.println("Time spent: " + (seconds) + " seconds");
    }
}
