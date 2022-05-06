package prng;

public class XorShift8 {
    private byte a;

    public XorShift8(byte a) {
        setSeed(a);
    }

    public byte nextByte() {
        return a ^= (a >> 3) ^ (a << 4);
    }

    public void setSeed(byte a) {
        if (a == 0) throw new IllegalArgumentException();
        this.a = a;
    }
}
