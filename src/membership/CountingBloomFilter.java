package membership;

import hash.MurmurHash3;

import java.util.Arrays;
import java.util.BitSet;

import static utils.IntegerUtils.nextPow2;

public class CountingBloomFilter {
    private static final int MAX_CAPACITY = 1 << 30;
    private final int[] counters;
    private final int[] hashes;
    private final BitSet bitSet;
    private final int mask, k;

    public CountingBloomFilter(int size, int numberOfHashFunctions) {
        if (size > MAX_CAPACITY) throw new IllegalArgumentException("Size too large");
        if (size < 0) throw new IllegalArgumentException("Size must be positive");
        if (numberOfHashFunctions < 1) throw new IllegalArgumentException("Hash functions must be positive");

        int size1 = nextPow2(size);
        counters = new int[size1];
        bitSet = new BitSet(size1);

        this.mask = size1 - 1;
        this.k = numberOfHashFunctions;
        this.hashes = new int[k];
    }

    public void add(String key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        byte[] bytes = key.getBytes();
        for (int i = 0; i < k; i++) {
            int hash = hash(bytes, i);
            hashes[i] = hash;
            if (!bitSet.get(hash)) {
                bitSet.set(hash);
                if (counters[hash] != Integer.MAX_VALUE) counters[hash]++;
            }
        }
        for (int hash : hashes) bitSet.clear(hash);
    }

    public boolean contains(String key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        byte[] bytes = key.getBytes();
        for (int i = 0; i < k; i++) {
            int hash = hash(bytes, i);
            if (counters[hash] == 0) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAtLeastTime(String key, int times) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        if (times <= 0) throw new IllegalArgumentException("Times must be positive");
        byte[] bytes = key.getBytes();
        for (int i = 0; i < k; i++) {
            int hash = hash(bytes, i);
            if (counters[hash] < times) {
                return false;
            }
        }
        return true;
    }

    public int getCount(String key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        byte[] bytes = key.getBytes();
        int count = Integer.MAX_VALUE;
        for (int i = 0; i < k; i++) {
            int hash = hash(bytes, i);
            count = Math.min(count, counters[hash]);
        }
        return count;
    }

    public void clear() {
        Arrays.fill(counters, 0);
    }

    private int hash(byte[] s, int i) {
        int hash = MurmurHash3.hash32xArray(s, i);
        return hash & mask;
    }
}
