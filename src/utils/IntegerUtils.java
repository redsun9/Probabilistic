package utils;

public class IntegerUtils {
    private IntegerUtils() {
    }

    public static int nextPow2(int n) {
        if ((n & (n - 1)) == 0) return n;
        else return Integer.highestOneBit(n) << 1;
    }
}
