package prng;

import java.util.Arrays;

// has period of 2^1024-1
public class XorShift1024 {
    private long[] arr;
    private int index;

    public XorShift1024(long[] arr) {
        setSeed(arr);
    }

    public long nextLong() {
        long s = arr[index++];
        long t = arr[index &= 15];
        t ^= t << 31;
        t ^= t >>> 11;
        t ^= s ^ (s >>> 30);
        arr[index] = t;
        return t * 1181783497276652981L;
    }

    public void setSeed(long[] arr) {
        if (arr.length < 16) throw new IllegalArgumentException();
        long t = 0;
        for (int i = 0; i < 16; i++) t |= arr[i];
        if (t == 0) throw new IllegalArgumentException();
        this.arr = Arrays.copyOf(arr, 16);
    }
}
