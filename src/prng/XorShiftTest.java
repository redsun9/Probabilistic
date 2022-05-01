package prng;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XorShiftTest {
    @Test
    void testXorShift8() {
        XorShift8 fast = new XorShift8((byte) 1);
        XorShift8 slow = new XorShift8((byte) 1);
        long period = 1;
        while (true) {
            fast.nextByte();
            if (fast.nextByte() == slow.nextByte()) break;
            period++;
        }
        assertEquals((1L << 8) - 1, period);
    }

    @Test
    void testXorShift16() {
        XorShift16 fast = new XorShift16((short) 1);
        XorShift16 slow = new XorShift16((short) 1);
        long period = 1;
        while (true) {
            fast.nextShort();
            if (fast.nextShort() == slow.nextShort()) break;
            period++;
        }
        assertEquals((1L << 16) - 1, period);
    }

    @Test
    @Disabled
    void testXorShift32() {
        XorShift32 fast = new XorShift32(1);
        XorShift32 slow = new XorShift32(1);
        long period = 1;
        while (true) {
            fast.nextInteger();
            if (fast.nextInteger() == slow.nextInteger()) break;
            period++;
        }
        assertEquals((1L << 32) - 1, period);
    }

    @Test
    void testLowXorShiftN() {
        IntStream.rangeClosed(4, 16).parallel().forEach(XorShiftTest::testXorShiftN);
    }

    @Test
    @Disabled
    void testHighXorShiftN() {
        IntStream.rangeClosed(17, 31).parallel().forEach(XorShiftTest::testXorShiftN);
    }

    private static void testXorShiftN(int bits) {
        XorShiftN fast = new XorShiftN(1, bits);
        XorShiftN slow = new XorShiftN(1, bits);
        long period = 1;
        while (true) {
            fast.nextInteger();
            if (fast.nextInteger() == slow.nextInteger()) break;
            period++;
        }
        assertEquals((1L << bits) - 1, period);
    }
}