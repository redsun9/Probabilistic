package hash;

public final class MurmurHash3 {
    /**
     * A default seed to use for the murmur hash algorithm.
     * Has the value {@code 104729}.
     */
    public static final int DEFAULT_SEED = 104729;

    // Constants for 32-bit variant
    private static final int C1_32 = 0xcc9e2d51;
    private static final int C2_32 = 0x1b873593;
    private static final int R1_32 = 15;
    private static final int R2_32 = 13;
    private static final int M_32 = 5;
    private static final int N_32 = 0xe6546b64;

    // Constants for 128-bit variant
    private static final long C1 = 0x87c37b91114253d5L;
    private static final long C2 = 0x4cf5ad432745937fL;
    private static final int R1 = 31;
    private static final int R2 = 27;
    private static final int R3 = 33;
    private static final int M = 5;
    private static final int N1 = 0x52dce729;
    private static final int N2 = 0x38495ab5;

    private MurmurHash3() {
    }

    public static int hash32x128(final long data1, final long data2) {
        return hash32x128(data1, data2, DEFAULT_SEED);
    }

    public static int hash32x128(final long data1, final long data2, final int seed) {
        int hash = seed;
        final long r0 = Long.reverseBytes(data1);
        final long r1 = Long.reverseBytes(data2);

        hash = mix32((int) r0, hash);
        hash = mix32((int) (r0 >>> 32), hash);
        hash = mix32((int) (r1), hash);
        hash = mix32((int) (r1 >>> 32), hash);

        hash ^= Long.BYTES * 2;
        return fmix32(hash);
    }


    public static int hash32x64(final long data) {
        return hash32x64(data, DEFAULT_SEED);
    }

    public static int hash32x64(final long data, final int seed) {
        int hash = seed;
        final long r0 = Long.reverseBytes(data);

        hash = mix32((int) r0, hash);
        hash = mix32((int) (r0 >>> 32), hash);

        hash ^= Long.BYTES;
        return fmix32(hash);
    }


    public static int hash32xArray(final byte[] data) {
        return hash32xArray(data, DEFAULT_SEED);
    }

    public static int hash32xArray(final byte[] data, int seed) {
        return hash32xArray(data, 0, data.length, seed);
    }

    public static int hash32xArray(final byte[] data, final int offset, final int length, final int seed) {
        int hash = seed;
        final int nblocks = length >> 2;

        // body
        for (int i = 0; i < nblocks; i++) {
            final int index = offset + (i << 2);
            final int k = getLittleEndianInt(data, index);
            hash = mix32(k, hash);
        }

        // tail
        final int index = offset + (nblocks << 2);
        int k1 = 0;
        switch (offset + length - index) {
            case 3:
                k1 ^= (data[index + 2] & 0xff) << 16;
            case 2:
                k1 ^= (data[index + 1] & 0xff) << 8;
            case 1:
                k1 ^= (data[index] & 0xff);

                // mix functions
                k1 *= C1_32;
                k1 = Integer.rotateLeft(k1, R1_32);
                k1 *= C2_32;
                hash ^= k1;
        }

        hash ^= length;
        return fmix32(hash);
    }


    public static long[] hash128xArray(final byte[] data) {
        return hash128xArray(data, 0, data.length, DEFAULT_SEED);
    }

    public static long[] hash128xArray(final byte[] data, final int offset, final int length, final int seed) {
        // Use an unsigned 32-bit integer as the seed
        return hash128x64Internal(data, offset, length, seed & 0xffffffffL);
    }

    private static long[] hash128x64Internal(final byte[] data, final int offset, final int length, final long seed) {
        long h1 = seed;
        long h2 = seed;
        final int nblocks = length >> 4;

        // body
        for (int i = 0; i < nblocks; i++) {
            final int index = offset + (i << 4);
            long k1 = getLittleEndianLong(data, index);
            long k2 = getLittleEndianLong(data, index + 8);

            // mix functions for k1
            k1 *= C1;
            k1 = Long.rotateLeft(k1, R1);
            k1 *= C2;
            h1 ^= k1;
            h1 = Long.rotateLeft(h1, R2);
            h1 += h2;
            h1 = h1 * M + N1;

            // mix functions for k2
            k2 *= C2;
            k2 = Long.rotateLeft(k2, R3);
            k2 *= C1;
            h2 ^= k2;
            h2 = Long.rotateLeft(h2, R1);
            h2 += h1;
            h2 = h2 * M + N2;
        }

        // tail
        long k1 = 0;
        long k2 = 0;
        final int index = offset + (nblocks << 4);
        switch (offset + length - index) {
            case 15:
                k2 ^= ((long) data[index + 14] & 0xff) << 48;
            case 14:
                k2 ^= ((long) data[index + 13] & 0xff) << 40;
            case 13:
                k2 ^= ((long) data[index + 12] & 0xff) << 32;
            case 12:
                k2 ^= ((long) data[index + 11] & 0xff) << 24;
            case 11:
                k2 ^= ((long) data[index + 10] & 0xff) << 16;
            case 10:
                k2 ^= ((long) data[index + 9] & 0xff) << 8;
            case 9:
                k2 ^= data[index + 8] & 0xff;
                k2 *= C2;
                k2 = Long.rotateLeft(k2, R3);
                k2 *= C1;
                h2 ^= k2;

            case 8:
                k1 ^= ((long) data[index + 7] & 0xff) << 56;
            case 7:
                k1 ^= ((long) data[index + 6] & 0xff) << 48;
            case 6:
                k1 ^= ((long) data[index + 5] & 0xff) << 40;
            case 5:
                k1 ^= ((long) data[index + 4] & 0xff) << 32;
            case 4:
                k1 ^= ((long) data[index + 3] & 0xff) << 24;
            case 3:
                k1 ^= ((long) data[index + 2] & 0xff) << 16;
            case 2:
                k1 ^= ((long) data[index + 1] & 0xff) << 8;
            case 1:
                k1 ^= data[index] & 0xff;
                k1 *= C1;
                k1 = Long.rotateLeft(k1, R1);
                k1 *= C2;
                h1 ^= k1;
        }

        // finalization
        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        return new long[]{h1, h2};
    }


    private static long getLittleEndianLong(final byte[] data, final int index) {
        return (((long) data[index] & 0xff)) |
                (((long) data[index + 1] & 0xff) << 8) |
                (((long) data[index + 2] & 0xff) << 16) |
                (((long) data[index + 3] & 0xff) << 24) |
                (((long) data[index + 4] & 0xff) << 32) |
                (((long) data[index + 5] & 0xff) << 40) |
                (((long) data[index + 6] & 0xff) << 48) |
                (((long) data[index + 7] & 0xff) << 56);
    }

    private static int getLittleEndianInt(final byte[] data, final int index) {
        return ((data[index] & 0xff)) |
                ((data[index + 1] & 0xff) << 8) |
                ((data[index + 2] & 0xff) << 16) |
                ((data[index + 3] & 0xff) << 24);
    }

    private static int mix32(int k, int hash) {
        k *= C1_32;
        k = Integer.rotateLeft(k, R1_32);
        k *= C2_32;
        hash ^= k;
        return Integer.rotateLeft(hash, R2_32) * M_32 + N_32;
    }

    private static int fmix32(int hash) {
        hash ^= (hash >>> 16);
        hash *= 0x85ebca6b;
        hash ^= (hash >>> 13);
        hash *= 0xc2b2ae35;
        hash ^= (hash >>> 16);
        return hash;
    }

    private static long fmix64(long hash) {
        hash ^= (hash >>> 33);
        hash *= 0xff51afd7ed558ccdL;
        hash ^= (hash >>> 33);
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= (hash >>> 33);
        return hash;
    }
}
