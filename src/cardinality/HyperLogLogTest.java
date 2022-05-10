package cardinality;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.RandomGenerator;

import java.util.stream.IntStream;

class HyperLogLogTest {
    @Test
    @Disabled
    void test() {
        int maxLevel = 11;
        HyperLogLog[] hlls = new HyperLogLog[maxLevel];
        for (int i = 0; i < maxLevel; i++) hlls[i] = new HyperLogLog(i);

        int multiplier = 2;
        for (int i = 0, m = 0, nextM = multiplier; i < 30; i++, m = nextM, nextM *= multiplier) {
            IntStream.range(m, nextM).parallel().mapToObj(t -> RandomGenerator.getRandomString(20)).forEach(a -> {
                for (int j = 0; j < maxLevel; j++) hlls[j].add(a);
            });

            System.out.println("n = " + m);

            for (int k = 0; k < maxLevel; k++) {
                System.out.println(k + " " + Math.abs(hlls[k].getCardinality() - nextM) * 100f / nextM);
            }
        }
    }
}