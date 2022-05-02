package membership;

import java.util.Arrays;
import java.util.Objects;

public class QuotientFilter<K> {
    private static final int MAX_BITS = 27;
    private static final byte maskR = 0b11111;
    private static final byte occupationByte = (byte) (1 << 5);
    private static final byte continuationByte = (byte) (1 << 6);
    private static final byte shiftByte = (byte) (1 << 7);
    private static final byte elementMask = maskR | continuationByte | shiftByte;

    private static final int shiftQ = 5;
    private final byte[] t;
    private final int maskQ;
    private int size;

    public QuotientFilter(int bits) {
        if (bits < 0) throw new IllegalArgumentException("Bits must be positive");
        if (bits > MAX_BITS) throw new IllegalArgumentException("Too many bits");
        t = new byte[1 << bits];
        maskQ = (1 << bits) - 1;
    }

    public boolean contains(K key) {
        int hash = hash(key); // get hash, hash function's implementation can be changed
        int fq = getQuotient(hash); //get index of the run
        byte fr = getRemainder(hash); //get remainder to check if the key is in the run

        // check existence of corresponding run
        if ((t[fq] & occupationByte) != occupationByte) return false;

        // find start of the cluster
        fq = findStartOfRun(fq);

        //check elements in the run
        if ((t[fq] & maskR) == fr) return true; //if the remainder is equal in the start of run, we found the key
        fq = (fq + 1) & maskQ;
        while ((t[fq] & continuationByte) == continuationByte) { //while the run is not finished
            if ((t[fq] & maskR) == fr)
                return true; //if the remainder in continued slot of the run is equal, we found the key
            fq = (fq + 1) & maskQ; //go to the next slot
        }
        return false;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return t.length;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == t.length;
    }

    public void clear() {
        Arrays.fill(t, (byte) 0);
        size = 0;
    }

    public boolean add(K key) {
        if (size == t.length) {
            if (contains(key)) return false;
            else throw new RuntimeException("QuotientFilter is full");
        }
        int hash = hash(key);
        int fq = getQuotient(hash);
        byte fr = getRemainder(hash);

        // check existence of corresponding run
        if ((t[fq] & occupationByte) != occupationByte) {
            t[fq] |= occupationByte;
            size++;
            // if canonical slot is empty, create new run in it
            if (t[fq] == 0) {
                t[fq] |= fr;
            } else {
                //if canonical slot is occupied, find the position to place the run
                fq = findStartOfRun(fq);

                //if the slot is occupied with another run, we need to move them to the next slot
                int prevValue = t[fq], nextValue;
                t[fq] = (byte) (prevValue & occupationByte | fr | shiftByte);
                while ((prevValue & elementMask) != 0) {
                    fq = (fq + 1) & maskQ;
                    nextValue = t[fq];
                    t[fq] = (byte) (prevValue & elementMask | shiftByte | nextValue & occupationByte);
                    prevValue = nextValue;
                }
            }
            return true;
        } else {
            fq = findStartOfRun(fq);
            byte shift = (byte) (t[fq] & shiftByte);
            if ((t[fq] & maskR) == fr) return false; //if the remainder is equal in the start of run, we found the key
            fq = (fq + 1) & maskQ;
            while ((t[fq] & continuationByte) == continuationByte) { //while the run is not finished
                if ((t[fq] & maskR) == fr)
                    return false; //if the remainder in continued slot of the run is equal, we found the key
                fq = (fq + 1) & maskQ; //go to the next slot
            }

            // we didn't find the key, so we need to add it
            size++;
            int prevValue = t[fq], nextValue;
            t[fq] = (byte) (prevValue & occupationByte | fr | shift | continuationByte);
            //if slot wasn't empty, we need to shift elements
            while ((prevValue & elementMask) != 0) {
                fq = (fq + 1) & maskQ;
                nextValue = t[fq];
                t[fq] = (byte) (prevValue & elementMask | shiftByte | nextValue & occupationByte);
                prevValue = nextValue;
            }
            return true;
        }
    }

    private int findStartOfRun(int fq) {
        // find start of the cluster
        int counter = 1;
        while ((t[fq] & shiftByte) == shiftByte) { //while not meet start of the cluster
            if ((t[fq] & occupationByte) == occupationByte) counter++; //count the number of chains in cluster
            fq = (fq - 1) & maskQ; //go to the previous slot
        }

        //find the start of the run
        while (counter != 0) {
            if ((t[fq] & continuationByte) == 0) { //decrease counter if we meet a start of run
                if (--counter == 0) break; //if counter is 0, we found the run
            }
            fq = (fq + 1) & maskQ; //go to the next slot
        }
        return fq;
    }

    private int hash(Object key) {
        return Objects.hashCode(key);
    }

    private int getQuotient(int hash) {
        return hash >>> shiftQ & maskQ;
    }

    private byte getRemainder(int hash) {
        return (byte) (hash & maskR);
    }
}
