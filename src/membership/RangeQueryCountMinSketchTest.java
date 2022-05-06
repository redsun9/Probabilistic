package membership;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import prng.XorShiftN;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RangeQueryCountMinSketchTest {
    private static final int m = 1 << 14;
    private static final int n = 1 << 12;
    private static final int k = 3;
    private static final int tests = 1 << 14;
    private static final int rangeLog = 20;
    private static final long minAcceptedValue = 0;
    private static final long maxAcceptedValue = minAcceptedValue + (1 << rangeLog) - 1;

    private static final double maxErrorRateContainsNonExisting = 0.17;
    private static final double maxErrorRateCountExisting = 0.18;
    private static final double maxErrorRateCountNonExisting = 0.17;
    private static final double maxErrorRateRangeExisting = 0.18;
    private static final double maxErrorRateRangeNonExisting = 0.017;

    @Test
    void testClear() {
        RangeQueryCountMinSketch cms = new RangeQueryCountMinSketch(m, k, 0, 16);
        cms.add(1);
        cms.clear();
        assertEquals(0, cms.getCount());
        assertEquals(0, cms.getCount(1));
        assertEquals(0, cms.getCount(1, 1));
        assertEquals(0, cms.getCount(0, 2));
    }

    @Test
    void testRangeQueryExisting() {
        RangeQueryCountMinSketch cms = new RangeQueryCountMinSketch(m, k, 0, n - 1);
        for (int i = 0; i < n; i++) cms.add(i);

        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                assertTrue(cms.getCount(i, j) >= j - i + 1);
            }
        }
    }

    @RepeatedTest(100)
    void testContainsExisting() {
        RangeQueryCountMinSketch cms = new RangeQueryCountMinSketch(m, k, minAcceptedValue, maxAcceptedValue);
        Random random = new Random();
        int seed = 0;
        while ((seed & ((1 << rangeLog) - 1)) == 0) seed = random.nextInt();

        XorShiftN xorShiftN = new XorShiftN(seed, rangeLog);
        for (int i = 0; i < n; i++) cms.add(minAcceptedValue + xorShiftN.nextInteger());

        xorShiftN.setSeed(seed);
        for (int i = 0; i < n; i++) {
            long key = minAcceptedValue + xorShiftN.nextInteger();
            assertTrue(cms.contains(key));
            assertTrue(cms.getCount(key) >= 1);
        }
    }

    @RepeatedTest(100)
    void testErrorRateContainsNonExisting() {
        RangeQueryCountMinSketch cms = new RangeQueryCountMinSketch(m, k, minAcceptedValue, maxAcceptedValue);
        Random random = new Random();
        int seed = 0;
        while ((seed & ((1 << rangeLog) - 1)) == 0) seed = random.nextInt();

        XorShiftN xorShiftN = new XorShiftN(seed, rangeLog);
        for (int i = 0; i < n; i++) cms.add(minAcceptedValue + xorShiftN.nextInteger());

        int fails = 0;
        for (int i = 0; i < tests; i++) if (cms.contains(maxAcceptedValue + xorShiftN.nextInteger())) fails++;

        double errorRate = (double) fails / tests;
        assertTrue(errorRate <= maxErrorRateContainsNonExisting);
    }

    @RepeatedTest(100)
    void testErrorRateCountExisting() {
        RangeQueryCountMinSketch cms = new RangeQueryCountMinSketch(m, k, minAcceptedValue, maxAcceptedValue);
        Random random = new Random();
        int seed = 0;
        while ((seed & ((1 << rangeLog) - 1)) == 0) seed = random.nextInt();

        XorShiftN xorShiftN = new XorShiftN(seed, rangeLog);
        for (int i = 0; i < n; i++) cms.add(minAcceptedValue + xorShiftN.nextInteger());

        int fails = 0;
        xorShiftN.setSeed(seed);
        for (int i = 0; i < tests; i++) fails += cms.getCount(minAcceptedValue + xorShiftN.nextInteger()) - 1;

        double errorRate = (double) fails / tests;
        assertTrue(errorRate <= maxErrorRateCountExisting);
    }

    @RepeatedTest(100)
    void testErrorRateCountNonExisting() {
        RangeQueryCountMinSketch cms = new RangeQueryCountMinSketch(m, k, minAcceptedValue, maxAcceptedValue);
        Random random = new Random();
        int seed = 0;
        while ((seed & ((1 << rangeLog) - 1)) == 0) seed = random.nextInt();

        XorShiftN xorShiftN = new XorShiftN(seed, rangeLog);
        for (int i = 0; i < n; i++) cms.add(minAcceptedValue + xorShiftN.nextInteger());

        int fails = 0;
        for (int i = 0; i < tests; i++) fails += cms.getCount(maxAcceptedValue + xorShiftN.nextInteger());

        double errorRate = (double) fails / tests;
        assertTrue(errorRate <= maxErrorRateCountNonExisting);
    }

    @Test
    void testErrorRateRangeExisting() {
        RangeQueryCountMinSketch cms = new RangeQueryCountMinSketch(m, k, 0, n - 1);
        for (int i = 0; i < n; i++) cms.add(i);

        int fails = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                fails += cms.getCount(i, j) / (double) (j - i + 1) - 1;
            }
        }

        double errorRate = fails / ((double) n * (n - 1) / 2);
        assertTrue(errorRate <= maxErrorRateRangeExisting);
    }

    @Test
    void testErrorRateRangeNonExisting() {
        RangeQueryCountMinSketch cms = new RangeQueryCountMinSketch(m, k, 0, 2 * n - 1);
        for (int i = 0; i < n; i++) cms.add(i);

        long fails = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                fails += cms.getCount(n + i, n + j);
            }
        }

        double errorRate = fails / ((double) n * (n - 1) / 2) / n;
        assertTrue(errorRate <= maxErrorRateRangeNonExisting);
    }
}