import java.text.MessageFormat;

public class Operation {
    public int srcId;
    public int destId;
    public int amount;
    public int sn;

    public Operation(int srcId, int destId, int amount, int sn) {
        this.srcId = srcId;
        this.destId = destId;
        this.amount = amount;
        this.sn = sn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operation operation = (Operation) o;
        return srcId == operation.srcId && destId == operation.destId
                && amount == operation.amount && sn == operation.sn;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Operation {0}: {1}$ sent from account: {2} to account: {3}",
                sn, amount, srcId, destId);
    }
}
