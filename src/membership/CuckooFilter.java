package membership;


import hash.MurmurHash3;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import static utils.IntegerUtils.nextPow2;


public class CuckooFilter {
    private static final int MAX_CAPACITY = 1 << 30;
    private static final int DEFAULT_CAPACITY = 1 << 4; // 16
    private static final int NUMBER_OF_FINGERPRINTS_IN_NODE = 8;
    private static final int NUMBER_OF_BITS_IN_FINGERPRINT = 8;
    private static final int MASK_FOR_FINGERPRINT = (1 << NUMBER_OF_BITS_IN_FINGERPRINT) - 1;
    private static final int MAX_LOOP = 20;
    private final int mask, seedForHash, seedForFingerprint;
    private long size;

    // each node contains up to 8 element fingerprints, elements' fingerprints are 8 bits
    private final long[] table;

    // used to identify that node has element with fingerprint 0
    private final BitSet bitset;

    public CuckooFilter() {
        this(DEFAULT_CAPACITY);
    }

    public CuckooFilter(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        if (capacity > MAX_CAPACITY) throw new IllegalArgumentException("Capacity must be less than " + MAX_CAPACITY);
        capacity = nextPow2(capacity);
        this.mask = capacity - 1;
        this.size = 0;
        this.table = new long[capacity];
        this.bitset = new BitSet(capacity);

        Random random = new Random();
        int seed1 = 0, seed2 = 0;
        while (seed1 == 0) seed1 = random.nextInt();
        while (seed2 == 0 || seed2 == seed1) seed2 = random.nextInt();

        this.seedForHash = seed1;
        this.seedForFingerprint = seed2;
    }

    public long size() {
        return size;
    }

    public boolean contains(String key) {
        if (key == null) throw new IllegalArgumentException("Key must not be null");
        byte[] arr = key.getBytes();
        int fingerprint = fingerprint(arr);
        int hash1 = hash(arr);
        int hash2 = hash1 ^ hashFingerprint(fingerprint);
        if (fingerprint == 0) return bitset.get(hash1);
        return contains(table[hash1], fingerprint) || contains(table[hash2], fingerprint);
    }

    private static boolean contains(long val, int fingerprint) {
        for (int i = 0; i < NUMBER_OF_FINGERPRINTS_IN_NODE; i++) {
            if ((val & MASK_FOR_FINGERPRINT) == fingerprint) return true;
            val >>>= NUMBER_OF_BITS_IN_FINGERPRINT;
        }
        return false;
    }

    public boolean add(String key) {
        if (key == null) throw new IllegalArgumentException("Key must not be null");
        if (contains(key)) return false;
        size++;
        byte[] arr = key.getBytes();
        int hash1 = hash(arr);
        int fingerprint = fingerprint(arr);
        int hash2 = hash1 ^ hashFingerprint(fingerprint);

        if (fingerprint == 0) {
            bitset.set(hash1);
            return true;
        }

        int n1 = count(table[hash1]);
        int n2 = count(table[hash2]);

        if (n1 <= n2 && n1 < NUMBER_OF_FINGERPRINTS_IN_NODE) {
            table[hash1] = table[hash1] << NUMBER_OF_BITS_IN_FINGERPRINT | fingerprint;
            return true;
        } else if (n2 < n1) {
            table[hash2] = table[hash2] << NUMBER_OF_BITS_IN_FINGERPRINT | fingerprint;
            return true;
        }

        int loop = 0, tmpFingerprint;
        while (loop < MAX_LOOP) {
            loop++;
            //get last fingerprint in t1
            tmpFingerprint = (int) (table[hash1] >>> (64 - NUMBER_OF_BITS_IN_FINGERPRINT) & MASK_FOR_FINGERPRINT);
            table[hash1] = table[hash1] << NUMBER_OF_BITS_IN_FINGERPRINT | fingerprint;
            if (tmpFingerprint == 0) return true;

            fingerprint = tmpFingerprint;
            hash2 = hash1 ^ hashFingerprint(fingerprint);

            tmpFingerprint = (int) (table[hash2] >>> (64 - NUMBER_OF_BITS_IN_FINGERPRINT) & MASK_FOR_FINGERPRINT);
            table[hash2] = table[hash2] << NUMBER_OF_BITS_IN_FINGERPRINT | fingerprint;
            if (tmpFingerprint == 0) return true;

            fingerprint = tmpFingerprint;
            hash1 = hash2 ^ hashFingerprint(fingerprint);
        }
        throw new RuntimeException("Could not add key " + key + " to CuckooFilter");
    }

    public boolean remove(String key) {
        if (key == null) throw new IllegalArgumentException("Key must not be null");
        byte[] arr = key.getBytes();
        int hash1 = hash(arr);
        int fingerprint = fingerprint(arr);
        int hash2 = hash1 ^ hashFingerprint(fingerprint);

        if (fingerprint == 0) {
            if (bitset.get(hash1)) {
                bitset.clear(hash1);
                size--;
                return true;
            }
            return false;
        }

        return remove(hash1, fingerprint) || remove(hash2, fingerprint);
    }

    public boolean remove(int hash, int fingerprint) {
        long val = table[hash];
        if (val == 0) return false;
        int n = count(val);
        for (int i = 0; i < n; i++) {
            if ((val >>> (NUMBER_OF_FINGERPRINTS_IN_NODE * i) & MASK_FOR_FINGERPRINT) == fingerprint) {
                size--;
                if (i == 0) table[hash] = val >>> NUMBER_OF_BITS_IN_FINGERPRINT;
                else if (i == 7) table[hash] = val & 0x00FFFFFFFFFFFFFFL;
                else {
                    long lo = val << (NUMBER_OF_BITS_IN_FINGERPRINT * (8 - i)) >>> (NUMBER_OF_BITS_IN_FINGERPRINT * (8 - i));
                    long hi = val >>> (NUMBER_OF_BITS_IN_FINGERPRINT * (i + 1)) << (NUMBER_OF_BITS_IN_FINGERPRINT * i);
                    table[hash] = lo | hi;
                }
                return true;
            }
        }
        return false;
    }

    public void clear() {
        bitset.clear();
        Arrays.fill(table, 0);
        size = 0;
    }

    private static int count(long val) {
        int count = 0;
        if ((val & 0xFFFFFFFF00000000L) != 0) count += 4;
        val |= val >>> 32;
        if ((val & 0xFFFF0000L) != 0) count += 2;
        val |= val >>> 16;
        if ((val & 0xFF00L) != 0) count += 1;
        val |= val >>> 8;
        if ((val & 0xFFL) != 0) count += 1;
        return count;
    }

    private int hash(byte[] key) {
        return MurmurHash3.hash32xArray(key, seedForHash) & mask;
    }

    private int fingerprint(byte[] key) {
        return MurmurHash3.hash32xArray(key, seedForFingerprint) & MASK_FOR_FINGERPRINT;
    }

    private int hashFingerprint(int a) {
        return MurmurHash3.hash32x64(a, seedForFingerprint) & mask;
    }
}
