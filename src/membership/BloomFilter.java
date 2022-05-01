package membership;

import hash.MurmurHash3;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

public class BloomFilter {
    private static final int MAX_SIZE = 1 << 30;
    private static final int MAX_HASH_FUNCTIONS = 32;
    private final int mask;
    private final BitSet bits;
    private final int nbHash;

    public BloomFilter(int capacity, double errorRate) {
        if (errorRate <= 0 || errorRate >= 1) throw new IllegalArgumentException("errorRate must be between 0 and 1");
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        long k = Math.round(-Math.log(errorRate) / Math.log(2));
        long m = Math.round(capacity * k / Math.log(2));
        if (m > MAX_SIZE) throw new IllegalArgumentException("Impossible to satisfy the error rate");
        if(k>MAX_HASH_FUNCTIONS) throw new IllegalArgumentException("Too many hash functions needed");
        int size = nextPow2((int) m);
        this.mask = size - 1;
        this.bits = new BitSet(size);
        this.nbHash = (int) k;
    }

    public BloomFilter(int bits, int nbHash) {
        if (bits > MAX_SIZE) throw new IllegalArgumentException("bits too big");
        int size = nextPow2(bits);
        this.mask = size - 1;
        this.bits = new BitSet(size);
        this.nbHash = nbHash;
    }

    public void add(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < nbHash; i++) {
            bits.set(hash(bytes, i));
        }
    }

    public boolean contains(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < nbHash; i++) {
            if (!bits.get(hash(bytes, i))) {
                return false;
            }
        }
        return true;
    }

    private int hash(byte[] s, int i) {
        int hash = MurmurHash3.hash32xArray(s, i);
        return hash & mask;
    }

    private static int nextPow2(int n) {
        if ((n & (n - 1)) == 0) return n;
        else return Integer.highestOneBit(n) << 1;
    }
}
