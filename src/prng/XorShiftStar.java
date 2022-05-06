package prng;

// has a period of 2^64-1
public class XorShiftStar {
    private long a;

    public XorShiftStar(long a) {
        setSeed(a);
    }

    public long nextLong() {
        a ^= a >>> 12;
        a ^= a << 25;
        a ^= a >>> 27;
        return a * 0x2545F4914F6CDD1DL;
    }

    public int nextInt() {
        return ((int) (nextLong() >>> 32));
    }

    public void setSeed(long a) {
        if (a == 0) throw new IllegalArgumentException();
        this.a = a;
    }
}
