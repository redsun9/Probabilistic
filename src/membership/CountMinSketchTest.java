package membership;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import utils.RandomGenerator;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CountMinSketchTest {
    private static final int w = 1 << 14;
    private static final int n = 1 << 12;
    private static final int d = 4;
    private static final int tests = 1 << 14;

    private static final double maxErrorRateContainsNonExisting = 0.17;
    private static final double maxErrorRateCountExisting = 0.18;
    private static final double maxErrorRateCountNonExisting = 0.17;

    @Test
    void testAdd() {
        CountMinSketch cms = new CountMinSketch(w, d);
        cms.add("a");

        assertEquals(1, cms.pointQuery("a"));
        assertTrue(cms.contains("a"));

        assertEquals(0, cms.pointQuery("b"));
        assertFalse(cms.contains("b"));

        assertEquals(1, cms.getCount());
    }

    @Test
    void testAddAmount() {
        CountMinSketch cms = new CountMinSketch(w, d);
        cms.add("a", 10);

        assertEquals(10, cms.pointQuery("a"));
        assertTrue(cms.contains("a"));

        assertEquals(0, cms.pointQuery("b"));
        assertFalse(cms.contains("b"));

        assertEquals(10, cms.getCount());
    }

    @Test
    void testCount() {
        CountMinSketch cms = new CountMinSketch(w, d);
        cms.add("a");
        cms.add("b");
        assertEquals(2, cms.getCount());
    }

    @RepeatedTest(100)
    void testErrorRateContainsNonExisting() {
        CountMinSketch cms = new CountMinSketch(w, d);
        Random random = new Random();
        long seed = random.nextLong();

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < n; i++) cms.add(RandomGenerator.getRandomString(20));

        int fails = 0;
        for (int i = 0; i < tests; i++) if (cms.contains(RandomGenerator.getRandomString(20))) fails++;

        double errorRate = (double) fails / tests;
        assertTrue(errorRate <= maxErrorRateContainsNonExisting);
    }

    @RepeatedTest(100)
    void testErrorRateCountExisting() {
        CountMinSketch cms = new CountMinSketch(w, d);
        Random random = new Random();
        long seed = random.nextLong();

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < n; i++) cms.add(RandomGenerator.getRandomString(20));

        RandomGenerator.setSeed(seed);
        int fails = 0;
        for (int i = 0; i < n; i++) fails += cms.pointQuery(RandomGenerator.getRandomString(20)) - 1;

        double errorRate = (double) fails / n;
        assertTrue(errorRate <= maxErrorRateCountExisting);
    }


    @RepeatedTest(100)
    void testErrorRateCountNonExisting() {
        CountMinSketch cms = new CountMinSketch(w, d);
        Random random = new Random();
        long seed = random.nextLong();

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < n; i++) cms.add(RandomGenerator.getRandomString(20));

        int fails = 0;
        for (int i = 0; i < tests; i++) fails += cms.pointQuery(RandomGenerator.getRandomString(20));

        double errorRate = (double) fails / tests;
        assertTrue(errorRate <= maxErrorRateCountNonExisting);
    }


    @RepeatedTest(100)
    void testErrorRateInnerProduct() {
        CountMinSketch cms1 = new CountMinSketch(w, d);
        CountMinSketch cms2 = new CountMinSketch(w, d);
        Random random = new Random();
        long seed = random.nextLong();

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < n; i++) cms1.add(RandomGenerator.getRandomString(20));
        for (int i = 0; i < n; i++) cms2.add(RandomGenerator.getRandomString(20));

        long fails = cms1.innerProduct(cms2);
        double errorRate = (double) fails / tests;
        assertTrue(errorRate <= maxErrorRateCountNonExisting);
    }
}