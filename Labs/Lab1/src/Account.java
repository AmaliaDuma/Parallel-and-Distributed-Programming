import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Account {
    public int id;
    public int balance;
    public int initialBalance;
    public List<Operation> operations;
    public Lock lock;

    public Account(int id, int initialBalance) {
        this.id = id;
        this.balance = initialBalance;
        this.initialBalance = initialBalance;
        this.operations = new ArrayList<>();
        this.lock = new ReentrantLock();
    }


    public boolean checkBalance(){
        int balance = initialBalance;
        for (Operation op : operations){
            if (op.srcId == id) balance -= op.amount;
            else balance += op.amount;
        }
        return balance == this.balance;
    }

    public String makeTransfer(Account dest, int amount, int sn){
        if (id == dest.id) return "Accounts are the same.";

        if (id < dest.id){
            lock.lock();
            dest.lock.lock();
        } else {
            dest.lock.lock();
            lock.lock();
        }

        if (amount > balance){
            if (id < dest.id){
                dest.lock.unlock();
                lock.unlock();
            } else {
                lock.unlock();
                dest.lock.unlock();
            }
            return "Invalid transfer, not enough money.";
        }

        balance -= amount;
        dest.balance += amount;
        var operation = new Operation(id, dest.id, amount, sn);
        operations.add(operation);
        dest.operations.add(operation);

        if (id < dest.id){
            dest.lock.unlock();
            lock.unlock();
        } else {
            lock.unlock();
            dest.lock.unlock();
        }

        return operation.toString();
    }
}
