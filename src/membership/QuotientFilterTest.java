package membership;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import prng.XorShift32;
import utils.RandomGenerator;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class QuotientFilterTest {
    private static final int numberOfTests = 1_000;
    private static final int numberOfElements = 4_000;
    private static final int bitsForFilter = 12;
    private static final int capacity = 1 << bitsForFilter;
    private static final double maxErrorRate = 0.05;

    @Test
    void testAddAlreadyExisting() {
        IntStream.rangeClosed(1, numberOfTests).parallel().forEach(t -> {
            QuotientFilter<Integer> filter = new QuotientFilter<>(bitsForFilter);
            XorShift32 random = new XorShift32(t);
            for (int i = 0; i < numberOfElements; i++) filter.add(random.nextInteger());
            random = new XorShift32(t);
            for (int i = 0; i < numberOfElements; i++) assertFalse(filter.add(random.nextInteger()));
        });
    }

    @Test
    void testContainsAlreadyExisting() {
        IntStream.rangeClosed(1, numberOfTests).parallel().forEach(t -> {
            QuotientFilter<Integer> filter = new QuotientFilter<>(bitsForFilter);
            XorShift32 random = new XorShift32(t);
            for (int i = 0; i < numberOfElements; i++) filter.add(random.nextInteger());
            random = new XorShift32(t);
            for (int i = 0; i < numberOfElements; i++) assertTrue(filter.contains(random.nextInteger()));
        });
    }

    @Test
    void testErrorRateForContainsNonExisting() {
        IntStream.rangeClosed(1, numberOfTests).parallel().forEach(t -> {
            QuotientFilter<Integer> filter = new QuotientFilter<>(bitsForFilter);
            XorShift32 random = new XorShift32(t);
            for (int i = 0; i < numberOfElements; i++) filter.add(random.nextInteger());
            int fails = 0;
            for (int i = numberOfElements; i < numberOfElements * 2; i++)
                if (filter.contains(random.nextInteger())) fails++;
            assertTrue(fails < numberOfElements * maxErrorRate);
        });
    }

    @Test
    void testErrorRateForAdd() {
        IntStream.rangeClosed(1, numberOfTests).parallel().forEach(t -> {
            QuotientFilter<Integer> filter = new QuotientFilter<>(bitsForFilter);
            XorShift32 random = new XorShift32(t);
            int fails = 0;
            for (int i = 0; i < numberOfElements; i++) if (!filter.add(random.nextInteger())) fails++;
            assertTrue(fails < numberOfElements * maxErrorRate);
        });
    }

    @Test
    void testErrorRateForSize() {
        IntStream.rangeClosed(1, numberOfTests).parallel().forEach(t -> {
            QuotientFilter<Integer> filter = new QuotientFilter<>(bitsForFilter);
            XorShift32 random = new XorShift32(t);
            for (int i = 0; i < numberOfElements; i++) filter.add(random.nextInteger());
            assertTrue(filter.size() >= numberOfElements - numberOfElements * maxErrorRate);
            assertTrue(filter.size() <= numberOfElements);
        });
    }

    @Test
    void testFull() {
        IntStream.rangeClosed(1, numberOfTests).parallel().forEach(t -> {
            QuotientFilter<Integer> filter = new QuotientFilter<>(bitsForFilter);
            XorShift32 random = new XorShift32(t);
            int counter = 0;
            while (!filter.isFull()) if (filter.add(random.nextInteger())) counter++;
            assertTrue(filter.isFull());
            assertEquals(counter, filter.size());
            assertTrue(counter >= capacity);
            assertEquals(capacity, filter.size());
        });
    }

    @Test
    void testClear() {
        IntStream.rangeClosed(1, numberOfTests).parallel().forEach(t -> {
            QuotientFilter<Integer> filter = new QuotientFilter<>(bitsForFilter);
            XorShift32 random = new XorShift32(t);
            for (int i = 0; i < numberOfElements; i++) filter.add(random.nextInteger());
            filter.clear();
            assertTrue(filter.isEmpty());
            assertFalse(filter.isFull());
            assertEquals(0, filter.size());
        });
    }

    @Test
    void testCapacity() {
        IntStream.rangeClosed(1, 20).parallel().forEach(t -> {
            QuotientFilter<Integer> filter = new QuotientFilter<>(t);
            assertEquals(1 << t, filter.capacity());
        });
    }

    @Test
    @Disabled
    void testLargeFilter() {
        int numberOfStrings = 100_000_000;
        int maxNumberOfBits = 27;
        int seed = 0;
        QuotientFilter<String> filter = new QuotientFilter<>(maxNumberOfBits);
        RandomGenerator.setSeed(seed);
        for (int i = 0; i < numberOfStrings; i++) filter.add(RandomGenerator.getRandomString(10));
        double errorRate = 1.0 - (double) filter.size() / numberOfStrings;
        assertTrue(errorRate < 0.015);

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < numberOfStrings; i++) assertTrue(filter.contains(RandomGenerator.getRandomString(10)));

        RandomGenerator.setSeed(seed);
        for (int i = 0; i < numberOfStrings; i++) assertFalse(filter.add(RandomGenerator.getRandomString(10)));
    }
}