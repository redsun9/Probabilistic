package utils;

import java.util.Random;

public class RandomGenerator {
    private static final Random random = new Random();

    public static String getRandomString(int length) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) chars[i] = getRandomChar();
        return new String(chars);
    }

    public static char getRandomChar() {
        return (char) ('a' + random.nextInt(26));
    }

    public static int getRandomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static int getRandomInt() {
        return random.nextInt();
    }

    public static double getRandomDouble() {
        return random.nextDouble();
    }

    public static double getRandomDouble(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    public static boolean getRandomBoolean() {
        return random.nextBoolean();
    }

    public static boolean getRandomBoolean(double probability) {
        return random.nextDouble() < probability;
    }

    public static <T> T getRandomElement(T[] array) {
        return array[random.nextInt(array.length)];
    }

    public static <T> T getRandomElement(T[] array, Random random) {
        return array[random.nextInt(array.length)];
    }

    public static <T> T getRandomElement(T[] array, int start, int end) {
        return array[start + random.nextInt(end - start + 1)];
    }

    public static <T> T getRandomElement(T[] array, int start, int end, Random random) {
        return array[start + random.nextInt(end - start + 1)];
    }
}
