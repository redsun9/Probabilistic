package membership;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import utils.RandomGenerator;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CountingBloomFilterTest {
    private static final int m = 1 << 14;
    private static final int n = 1 << 12;
    private static final int k = 3;
    private static final int tests = 1 << 14;

    private static final double maxErrorRateContainsNonExisting = 0.17;
    private static final double maxErrorRateCountExisting = 0.18;
    private static final double maxErrorRateCountNonExisting = 0.17;

    @Test
    void testAdd() {
        CountingBloomFilter cbf = new CountingBloomFilter(16, 2);
        cbf.add("test");
        assertTrue(cbf.contains("test"));
    }

    @Test
    void testClear() {
        CountingBloomFilter cbf = new CountingBloomFilter(16, 2);
        cbf.add("test");
        cbf.clear();
        assertFalse(cbf.contains("test"));
    }

    @RepeatedTest(100)
    void testContainsExisting() {
        CountingBloomFilter bf = new CountingBloomFilter(m, k);
        Random random = new Random();
        long seed = random.nextLong();

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < n; i++) bf.add(RandomGenerator.getRandomString(20));

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < n; i++) {
            String s = RandomGenerator.getRandomString(20);
            assertTrue(bf.contains(s));
            assertTrue(bf.containsAtLeastTime(s, 1));
            assertTrue(bf.getCount(s) >= 1);
        }
    }

    @RepeatedTest(100)
    void testErrorRateContainsNonExisting() {
        CountingBloomFilter bf = new CountingBloomFilter(m, k);
        Random random = new Random();
        long seed = random.nextLong();

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < n; i++) bf.add(RandomGenerator.getRandomString(20));

        int fails = 0;
        for (int i = 0; i < tests; i++) if (bf.contains(RandomGenerator.getRandomString(20))) fails++;

        double errorRate = (double) fails / tests;
        assertTrue(errorRate <= maxErrorRateContainsNonExisting);
    }


    @RepeatedTest(100)
    void testErrorRateCountExisting() {
        CountingBloomFilter bf = new CountingBloomFilter(m, k);
        Random random = new Random();
        long seed = random.nextLong();

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < n; i++) bf.add(RandomGenerator.getRandomString(20));

        RandomGenerator.setSeed(seed);
        int fails = 0;
        for (int i = 0; i < n; i++) fails += bf.getCount(RandomGenerator.getRandomString(20)) - 1;

        double errorRate = (double) fails / n;
        assertTrue(errorRate <= maxErrorRateCountExisting);
    }


    @RepeatedTest(100)
    void testErrorRateCountNonExisting() {
        CountingBloomFilter bf = new CountingBloomFilter(m, k);
        Random random = new Random();
        long seed = random.nextLong();

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < n; i++) bf.add(RandomGenerator.getRandomString(20));

        int fails = 0;
        for (int i = 0; i < tests; i++) fails += bf.getCount(RandomGenerator.getRandomString(20));

        double errorRate = (double) fails / tests;
        assertTrue(errorRate <= maxErrorRateCountNonExisting);
    }
}