package similarity;

import org.junit.jupiter.api.Test;
import utils.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SimHashTest {

    @Test
    void testForSimHash64() {
        SimHash simHash = new SimHash(2);
        String str1 = "aaa bbb ccc ddd eee fff ggg hhh";
        String str2 = "aaa bbb ddd eee fff ggg hhh";
        String str3 = "bbb ddd aaa eee ggg fff hhh";

        long hash1 = simHash.simHash64(str1);
        long hash2 = simHash.simHash64(str2);
        long hash3 = simHash.simHash64(str3);

        int difference12 = Long.bitCount(hash1 ^ hash2);
        int difference13 = Long.bitCount(hash1 ^ hash3);
        int difference23 = Long.bitCount(hash2 ^ hash3);

        assertTrue(difference12 < 16);
        assertTrue(difference13 > 20);
        assertTrue(difference23 > 20);
    }

    @Test
    void testForSimHash128() {
        SimHash simHash = new SimHash(2);
        String str1 = "aaa bbb ccc ddd eee fff ggg hhh iii jjj";
        String str2 = "aaa bbb ddd eee fff ggg hhh iii jjj";
        String str3 = "bbb ddd aaa eee ggg fff hhh jjj iii";

        long[] hash1 = simHash.simHash128(str1);
        long[] hash2 = simHash.simHash128(str2);
        long[] hash3 = simHash.simHash128(str3);

        int difference12 = Long.bitCount(hash1[0] ^ hash2[0]) + Long.bitCount(hash1[1] ^ hash2[1]);
        int difference13 = Long.bitCount(hash1[0] ^ hash3[0]) + Long.bitCount(hash1[1] ^ hash3[1]);
        int difference23 = Long.bitCount(hash2[0] ^ hash3[0]) + Long.bitCount(hash2[1] ^ hash3[1]);

        assertTrue(difference12 < 40);
        assertTrue(difference13 > 50);
        assertTrue(difference23 > 50);
    }

    @Test
    void testSimilarForSimHash64() {
        SimHash simHash = new SimHash(3);
        int numberOfWords = 100;
        int numberOfTests = 1_000;

        IntStream.range(0, numberOfTests).parallel().forEach(t -> {
            List<String> words = new ArrayList<>(numberOfWords);
            for (int i = 0; i < numberOfWords; i++) words.add(RandomGenerator.getRandomString(10));
            String[] texts = new String[numberOfWords + 1];
            texts[0] = String.join(" ", words);
            for (int i = 0; i < numberOfWords; i++) {
                texts[i + 1] = String.join(" ", words.subList(0, i)) +
                        " " + String.join(" ", words.subList(i + 1, numberOfWords));
            }

            long[] hashes = new long[numberOfWords + 1];
            for (int i = 0; i <= numberOfWords; i++) hashes[i] = simHash.simHash64(texts[i]);

            for (int i = 0; i <= numberOfWords; i++) {
                for (int j = i + 1; j <= numberOfWords; j++) {
                    int difference = Long.bitCount(hashes[i] ^ hashes[j]);
                    assertTrue(difference < 25);
                }
            }
        });
    }

    @Test
    void testSimilarForSimHash128() {
        SimHash simHash = new SimHash(3);
        int numberOfWords = 100;
        int numberOfTests = 1_000;

        IntStream.range(0, numberOfTests).parallel().forEach(t -> {
            List<String> words = new ArrayList<>(numberOfWords);
            for (int i = 0; i < numberOfWords; i++) words.add(RandomGenerator.getRandomString(10));
            String[] texts = new String[numberOfWords + 1];
            texts[0] = String.join(" ", words);
            for (int i = 0; i < numberOfWords; i++) {
                texts[i + 1] = String.join(" ", words.subList(0, i)) +
                        " " + String.join(" ", words.subList(i + 1, numberOfWords));
            }

            long[][] hashes = new long[numberOfWords + 1][2];
            for (int i = 0; i <= numberOfWords; i++) hashes[i] = simHash.simHash128(texts[i]);

            for (int i = 0; i <= numberOfWords; i++) {
                for (int j = i + 1; j <= numberOfWords; j++) {
                    int difference = Long.bitCount(hashes[i][0] ^ hashes[j][0]) + Long.bitCount(hashes[i][1] ^ hashes[j][1]);
                    assertTrue(difference < 35);
                }
            }
        });
    }

    @Test
    void testDifferentForSimHash64() {
        SimHash simHash = new SimHash(3);
        int numberOfWords = 100;
        int numberOfTests = 1_000;

        IntStream.range(0, numberOfTests).parallel().forEach(t -> {
            String[] texts = new String[numberOfWords];
            for (int i = 0; i < numberOfWords; i++) {
                List<String> words = new ArrayList<>(numberOfWords);
                words.add(RandomGenerator.getRandomString(10));
                texts[i] = String.join(" ", words);
            }

            long[] hashes = new long[numberOfWords];
            for (int i = 0; i < numberOfWords; i++) hashes[i] = simHash.simHash64(texts[i]);

            for (int i = 0; i < numberOfWords; i++) {
                for (int j = i + 1; j < numberOfWords; j++) {
                    int difference = Long.bitCount(hashes[i] ^ hashes[j]);
                    assertTrue(difference > 8);
                }
            }
        });
    }

    @Test
    void testDifferentForSimHash128() {
        SimHash simHash = new SimHash(3);
        int numberOfWords = 100;
        int numberOfTests = 1_000;

        IntStream.range(0, numberOfTests).parallel().forEach(t -> {
            String[] texts = new String[numberOfWords];
            for (int i = 0; i < numberOfWords; i++) {
                List<String> words = new ArrayList<>(numberOfWords);
                words.add(RandomGenerator.getRandomString(10));
                texts[i] = String.join(" ", words);
            }

            long[][] hashes = new long[numberOfWords][2];
            for (int i = 0; i < numberOfWords; i++) hashes[i] = simHash.simHash128(texts[i]);

            for (int i = 0; i < numberOfWords; i++) {
                for (int j = i + 1; j < numberOfWords; j++) {
                    int difference = Long.bitCount(hashes[i][0] ^ hashes[j][0]) + Long.bitCount(hashes[i][1] ^ hashes[j][1]);
                    assertTrue(difference > 28);
                }
            }
        });
    }
}