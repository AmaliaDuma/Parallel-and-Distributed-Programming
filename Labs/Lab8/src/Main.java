import domain.DSM;
import domain.Listener;
import mpi.MPI;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        MPI.Init(args);
        final DSM dsm = new DSM();
        if (MPI.COMM_WORLD.Rank() == 0) {
            master(dsm);
        } else {
            worker(dsm);
        }
        MPI.Finalize();
    }

    private static void master(final DSM dsm) throws InterruptedException {
        final Thread thread = new Thread(new Listener(dsm));
        thread.start();

        dsm.subscribe("a");
        dsm.subscribe("b");
        dsm.subscribe("c");
        dsm.subscribe("d");

        Thread.sleep(1000);

        dsm.compareAndExchange("a", 0, 27);

        Thread.sleep(1000);

        dsm.compareAndExchange("c", 0, 65);

        Thread.sleep(1000);

        dsm.updateVariable("b", 50);

        Thread.sleep(6000);

        dsm.compareAndExchange("d", 100, 99);

        Thread.sleep(2000);

        dsm.close();
        thread.join();
    }

    private static void worker(final DSM dsm) throws InterruptedException {
        int me = MPI.COMM_WORLD.Rank();
        final Thread thread = new Thread(new Listener(dsm));
        thread.start();
        if (me == 1) {

            Thread.sleep(1000);

            dsm.subscribe("a");
            dsm.subscribe("c");

            Thread.sleep(1000);

            dsm.compareAndExchange("a", 27, 420);
            dsm.compareAndExchange("c", 65, 32);

        } else if (me == 2) {

            Thread.sleep(2000);

            dsm.subscribe("b");

            Thread.sleep(2000);

            dsm.compareAndExchange("b", 50, 100);

            Thread.sleep(1000);

        }
        else {
            Thread.sleep(1000);

            dsm.subscribe("d");

            Thread.sleep(1000);

            dsm.updateVariable("d", 100);

            Thread.sleep(4000);

        }
        thread.join();
    }
}
