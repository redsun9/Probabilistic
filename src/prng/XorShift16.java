package prng;

public class XorShift16 {
    private short a;

    public XorShift16(short a) {
        setSeed(a);
    }

    public short nextShort() {
        return a ^= (a << 3) ^ (a >>> 13);
    }

    public void setSeed(short a) {
        if (a == 0) throw new IllegalArgumentException();
        this.a = a;
    }
}
