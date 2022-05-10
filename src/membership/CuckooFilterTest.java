package membership;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import utils.RandomGenerator;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CuckooFilterTest {

    @Test
    void testAdd() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        assertTrue(cuckooFilter.add("test"));
    }

    @Test
    void testContains() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        cuckooFilter.add("test");
        assertTrue(cuckooFilter.contains("test"));
    }

    @Test
    void testRemoveExisting() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        cuckooFilter.add("test");
        assertTrue(cuckooFilter.remove("test"));
    }

    @Test
    void testContainsAfterRemove() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        cuckooFilter.add("test");
        cuckooFilter.remove("test");
        assertFalse(cuckooFilter.contains("test"));
    }

    @Test
    void testRemoveNonExisting() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        cuckooFilter.add("test");
        assertFalse(cuckooFilter.remove("test2"));
        assertTrue(cuckooFilter.contains("test"));
    }

    @Test
    void testAddAfterRemove() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        cuckooFilter.add("test");
        cuckooFilter.remove("test");
        assertTrue(cuckooFilter.add("test"));
    }

    @Test
    void testAddExisting() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        cuckooFilter.add("test");
        assertFalse(cuckooFilter.add("test"));
    }

    @Test
    void testSize() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        cuckooFilter.add("test");
        assertEquals(1, cuckooFilter.size());
    }

    @Test
    void testSizeAfterRemove() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        cuckooFilter.add("test");
        cuckooFilter.remove("test");
        assertEquals(0, cuckooFilter.size());
    }

    @Test
    void testSizeAfterAddExisting() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        cuckooFilter.add("test");
        cuckooFilter.add("test");
        assertEquals(1, cuckooFilter.size());
    }

    @Test
    void testSizeAfterAddNonExisting() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        cuckooFilter.add("test");
        cuckooFilter.add("test2");
        assertEquals(2, cuckooFilter.size());
    }

    @Test
    void testSizeAfterRemoveNonExisting() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        cuckooFilter.add("test");
        cuckooFilter.remove("test2");
        assertEquals(1, cuckooFilter.size());
    }

    @Test
    void testSizeAfterRemoveExisting() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        cuckooFilter.add("test");
        cuckooFilter.remove("test");
        assertEquals(0, cuckooFilter.size());
    }

    @Test
    void testSizeAfterClear() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        cuckooFilter.add("test");
        cuckooFilter.clear();
        assertEquals(0, cuckooFilter.size());
    }

    @Test
    void testContainsAfterClear() {
        CuckooFilter cuckooFilter = new CuckooFilter(16);
        cuckooFilter.add("test");
        cuckooFilter.clear();
        assertFalse(cuckooFilter.contains("test"));
    }

    @Test
    void testErrorRateForAdding() {
        int capacity = 1 << 10;
        CuckooFilter cuckooFilter = new CuckooFilter(capacity);
        Random random = new Random();
        long seed = random.nextLong();

        RandomGenerator.setSeed(seed);
        int fails = 0;
        for (int i = 0; i < capacity; i++) if (!cuckooFilter.add(RandomGenerator.getRandomString(10))) fails++;

        double errorRate = (double) fails / capacity;
        assertTrue(errorRate < 0.01);
    }

    @Test
    void testContainsExisting() {
        int capacity = 1 << 10;
        CuckooFilter cuckooFilter = new CuckooFilter(capacity);

        Random random = new Random();
        long seed = random.nextLong();

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < capacity; i++) cuckooFilter.add(RandomGenerator.getRandomString(10));

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < capacity; i++) assertTrue(cuckooFilter.contains(RandomGenerator.getRandomString(10)));
    }

    @RepeatedTest(1_000)
    void testErrorRateForContainsNonExisting() {
        int capacity = 1 << 10;
        int tests = 1 << 12;
        CuckooFilter cuckooFilter = new CuckooFilter(capacity);

        Random random = new Random();
        long seed = random.nextLong();

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < capacity; i++) cuckooFilter.add(RandomGenerator.getRandomString(10));

        int fails = 0;
        for (int i = 0; i < tests; i++) if (cuckooFilter.contains(RandomGenerator.getRandomString(10))) fails++;

        double errorRate = (double) fails / tests;
        System.out.println(errorRate);
        assertTrue(errorRate < 0.017);
    }

    @RepeatedTest(1_000)
    void testMaxLoad() {
        int capacity = 1 << 10;
        CuckooFilter cuckooFilter = new CuckooFilter(capacity);
        Random random = new Random();
        long seed = random.nextLong();

        RandomGenerator.setSeed(seed);
        int counter = 0;

        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                cuckooFilter.add(RandomGenerator.getRandomString(10));
                counter++;
            }
        } catch (Throwable e) {
            double maxLoad = (double) counter / capacity / 8;
            System.out.println(maxLoad);
            assertTrue(maxLoad >= 0.75);
        }
    }

    @RepeatedTest(1000)
    void testErrorRateForSize() {
        int capacity = 1 << 10;
        int tests = capacity * 7;
        CuckooFilter cuckooFilter = new CuckooFilter(capacity);
        Random random = new Random();
        long seed = random.nextLong();

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < tests; i++) cuckooFilter.add(RandomGenerator.getRandomString(10));

        double errorRate = 1.0 - (double) cuckooFilter.size() / tests;
        System.out.println(errorRate);
        assertTrue(errorRate < 0.04);
    }
}