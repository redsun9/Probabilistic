package membership;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import utils.RandomGenerator;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BloomFilterTest {
    @RepeatedTest(100)
    void testErrorRate() {
        int m = 1 << 14, n = 1 << 12, k = 3, tests = 1 << 14;
        double errorRateMax = 0.17;
        BloomFilter bf = new BloomFilter(m, k);
        testFilter(bf, n, tests, errorRateMax);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "1000;0.2;10000",
            "1000;0.1;20000",
            "1000;0.05;50000",

            "10000;0.2;10000",
            "10000;0.1;20000",
            "10000;0.05;50000",
            "10000;0.02;100000",
            "10000;0.01;200000",

            "100000;0.01;200000",
            "100000;0.02;500000",
    }, delimiterString = ";")
    void testParameterChoosing(int n, double errorRate, int tests) {
        BloomFilter bf = new BloomFilter(n, errorRate);
        testFilter(bf, n, tests, errorRate * 1.1);
    }

    private static void testFilter(BloomFilter bf, int n, int tests, double errorRateMax) {
        HashSet<String> set = new HashSet<>();
        for (int i = 0; i < n; i++) {
            String s = RandomGenerator.getRandomString(20);
            bf.add(s);
            set.add(s);
        }

        for (String s : set) {
            assertTrue(bf.contains(s));
        }

        int fails = 0;

        for (int i = 0; i < tests; i++) {
            String s = RandomGenerator.getRandomString(20);
            if (bf.contains(s) != set.contains(s)) fails++;
        }

        double errorRate = (double) fails / tests;
        System.out.println("n = " + n);
        System.out.println("error rate: " + errorRate);
        System.out.println("max: " + errorRateMax);
        assertTrue(errorRate <= errorRateMax);
    }
}