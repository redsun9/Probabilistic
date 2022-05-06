package prng;

// Has period 2^192-2^32
public class XorWow {
    private int a, b, c, d, e, t;
    private int counter;

    public XorWow(int a, int b, int c, int d, int e) {
        setSeed(a, b, c, d, e);
    }

    public int nextInteger() {
        t = e;
        e = d;
        d = c;
        c = b;
        b = a;
        t ^= t >>> 2;
        t ^= t << 1;
        a = t ^ a ^ (a << 4);
        counter += 362437;
        return a + counter;
    }

    public void setSeed(int a, int b, int c, int d, int e) {
        if ((a | b | c | d) == 0) throw new IllegalArgumentException();
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        counter = 0;
        t = 0;
    }
}
