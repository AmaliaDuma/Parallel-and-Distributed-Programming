package domain;

import mpi.MPI;

public class Listener implements Runnable{

    private final DSM dsm;

    public Listener(final DSM dsm) {
        this.dsm = dsm;
    }

    @Override
    public void run() {
        final int me = MPI.COMM_WORLD.Rank();
        System.out.printf("Hello from me<%s>.%n", me);

        while (true) {
            final Object[] buffer = new Object[1];

            // Wait for a notification
            MPI.COMM_WORLD.Recv(buffer, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, MPI.ANY_TAG);

            // Get the message and it's type (subscribe, update or quit)
            final Message message = (Message) buffer[0];
            final Message.Type messageType = (Message.Type) message.getField(Message.Fields.TYPE);

            if (messageType.equals(Message.Type.QUIT)) {
                // If msg is quit we return and stop the listening
                System.out.printf("%s - Received QUIT message.%n", me);
                System.out.printf("%s - Final DSM state: %s.%n", me, dsm);
                return;
            }
            else if (messageType.equals(Message.Type.SUBSCRIBE)) {
                System.out.printf("%s - Received SUBSCRIBE message (Process<%s> -> variable<%s>).%n", me, message.getField(Message.Fields.RANK), message.getField(Message.Fields.VARIABLE));
                // If msg is a subscription we sync it
                dsm.syncSubscription((String) message.getField(Message.Fields.VARIABLE), (int) message.getField(Message.Fields.RANK));
            } else if (messageType.equals(Message.Type.UPDATE)) {
                System.out.printf("%s - Received UPDATE message (variable<%s> -> value<%s>).%n", me, message.getField(Message.Fields.VARIABLE), message.getField(Message.Fields.VALUE));
                dsm.setVariable((String) message.getField(Message.Fields.VARIABLE), message.getField(Message.Fields.VALUE));
            }

        }
    }
}
