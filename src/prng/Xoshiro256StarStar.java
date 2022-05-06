package prng;

// Has a period of 2^256-1
@SuppressWarnings("DuplicatedCode")
public class Xoshiro256StarStar {
    private long a, b, c, d;

    public Xoshiro256StarStar(long[] arr) {
        setSeed(arr);
    }

    public long nextLong() {
        long s1 = b * 5;
        long ans = ((s1 << 7) | (s1 >>> 57)) * 9;
        long t = s1 << 17;

        c ^= a;
        d ^= b;
        b ^= c;
        a ^= d;

        c ^= t;
        d = (d << 45) | (d >>> 19);

        return ans;
    }

    public void setSeed(long[] arr) {
        if (arr.length < 4) throw new IllegalArgumentException();
        long t = 0;
        for (int i = 0; i < 4; i++) t |= arr[i];
        if (t == 0) throw new IllegalArgumentException();
        this.a = arr[0];
        this.b = arr[1];
        this.c = arr[2];
        this.d = arr[3];
    }
}
