package domain;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import mpi.MPI;

public class DSM {

    private final Map<String, Set<Integer>> subscribers;
    private final Map<String, Object> variables;

    public DSM() {
        // Initialize variables (= nr of processes)
        variables = new ConcurrentHashMap<>();
        variables.put("a", 0);
        variables.put("b", 0);
        variables.put("c", 0);
        variables.put("d", 0);

        // Initialize a map for each variable to hold the subscribers
        subscribers = new ConcurrentHashMap<>();
        subscribers.put("a", new HashSet<>());
        subscribers.put("b", new HashSet<>());
        subscribers.put("c", new HashSet<>());
        subscribers.put("d", new HashSet<>());
    }

    // Send a message to all processes
    private void sendAll(Message message) {
        // For every process running
        for (int i=0; i<MPI.COMM_WORLD.Size(); i++) {
            if (MPI.COMM_WORLD.Rank() == i && !message.getField(Message.Fields.TYPE).equals(Message.Type.QUIT)) {
                // If i = crt process rank and the message is not Quit, we continue
                continue;
            }
            // Else send the message to all processes
            MPI.COMM_WORLD.Send(new Object[]{message}, 0, 1, MPI.OBJECT, i, 0);
        }
    }

    public void sendToSubscribers(String variable, Message message) {
        for (int i=0; i<MPI.COMM_WORLD.Size(); i++) {
            if (MPI.COMM_WORLD.Rank() == i || !subscribers.get(variable).contains(i)) {
                continue;
            }
            MPI.COMM_WORLD.Send(new Object[]{message}, 0, 1, MPI.OBJECT, i, 0);
        }
    }

    public void syncSubscription(final String variable, final int rank) {
        subscribers.get(variable).add(rank);
    }

    public void subscribe(String variable) {
        // Add the crt process to the subscribers set
        subscribers.get(variable).add(MPI.COMM_WORLD.Rank());

        final Message message = new Message(Message.Type.SUBSCRIBE);
        message.setField(Message.Fields.VARIABLE, variable);
        message.setField(Message.Fields.RANK, MPI.COMM_WORLD.Rank());

        sendAll(message);
    }

    public synchronized void setVariable(String variable, Object value) {
        if (variables.containsKey(variable)) {
            variables.put(variable, value);
        }
    }

    public synchronized void updateVariable(String variable, Object value) {
        // Set variable
        setVariable(variable, value);

        // Send notification to subscribers
        final Message message = new Message(Message.Type.UPDATE);
        message.setField(Message.Fields.VARIABLE, variable);
        message.setField(Message.Fields.VALUE, value);

        sendToSubscribers(variable, message);
    }

    public synchronized void compareAndExchange(String variable, Object oldValue, Object newValue) {
        if (oldValue.equals(variables.get(variable))) {
            updateVariable(variable, newValue);
        }
    }

    public void close() {
        sendAll(new Message(Message.Type.QUIT));
    }

    @Override
    public String toString() {
        return "DSM{" +
                "subscribers=" + subscribers +
                ", variables=" + variables +
                '}';
    }
}
