package membership;

import hash.MurmurHash3;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import static utils.IntegerUtils.nextPow2;

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

    public void add(String key) {
        if (key == null) throw new IllegalArgumentException("key cannot be null");
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < nbHash; i++) {
            bits.set(hash(bytes, i));
        }
    }

    public boolean contains(String key) {
        if (key == null) throw new IllegalArgumentException("key cannot be null");
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
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


}
