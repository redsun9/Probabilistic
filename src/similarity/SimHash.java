package similarity;

import hash.MurmurHash3;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SimHash {
    private final int shingleSize;

    public SimHash(int shingleSize) {
        this.shingleSize = shingleSize;
    }

    public short simHash16(String text) {
        byte[] arr = prepareText(text);
        int[] counters = new int[16];
        List<int[]> positions = findPositions(arr);
        int numberOfShingles = positions.size();

        for (int[] pos : positions) {
            int hash = MurmurHash3.hash32xArray(arr, pos[0], pos[1] - pos[0]);
            updateCounters(counters, (short) (hash ^ (hash >>> 16)));
        }

        short result = 0;
        for (int i = 0; i < 16; i++) if (counters[i] * 2 > numberOfShingles) result |= 1 << i;
        return result;
    }


    public int simHash32(String text) {
        byte[] arr = prepareText(text);
        int[] counters = new int[32];
        List<int[]> positions = findPositions(arr);
        int numberOfShingles = positions.size();

        for (int[] pos : positions) {
            int hash = MurmurHash3.hash32xArray(arr, pos[0], pos[1] - pos[0]);
            updateCounters(counters, hash);
        }

        int result = 0;
        for (int i = 0; i < 32; i++) if (counters[i] * 2 > numberOfShingles) result |= 1 << i;
        return result;
    }

    public long simHash64(String text) {
        byte[] arr = prepareText(text);
        int[] counters = new int[64];
        List<int[]> positions = findPositions(arr);
        int numberOfShingles = positions.size();

        for (int[] pos : positions) {
            long[] hash = MurmurHash3.hash128xArray(arr, pos[0], pos[1] - pos[0]);
            updateCounters(counters, hash[0] ^ hash[1]);
        }

        long result = 0;
        for (int i = 0; i < 64; i++) if (counters[i] * 2 > numberOfShingles) result |= 1L << i;
        return result;
    }

    public long[] simHash128(String text) {
        byte[] arr = prepareText(text);
        int[] counters = new int[128];
        List<int[]> positions = findPositions(arr);
        int numberOfShingles = positions.size();

        for (int[] pos : positions) {
            long[] hash = MurmurHash3.hash128xArray(arr, pos[0], pos[1] - pos[0]);
            updateCounters(counters, hash);
        }

        long[] result = new long[2];
        for (int i = 0; i < 64; i++) {
            if (counters[i] * 2 > numberOfShingles) result[0] |= 1L << i;
            if (counters[i + 64] * 2 > numberOfShingles) result[1] |= 1L << i;
        }
        return result;
    }

    private static byte[] prepareText(String text) {
        return text.replaceAll("\\W+", " ").trim().getBytes(StandardCharsets.UTF_8);
    }

    private List<int[]> findPositions(byte[] arr) {
        int n = arr.length;
        List<int[]> ans = new ArrayList<>();
        for (int l = 0, r = 0, c = 0; r <= n; r++) {
            if (r < n && arr[r] == ' ') c++;
            if (c == shingleSize || r == n) {
                ans.add(new int[]{l, r});
                l++;
                while (l != n && arr[l] != ' ') l++;
                l++;
                c--;
            }
        }
        return ans;
    }

    private static void updateCounters(int[] counters, long[] hash) {
        for (int i = 0; i < 64; i++) counters[i] += (hash[0] >>> i & 1);
        for (int i = 0; i < 64; i++) counters[i + 64] += (hash[1] >>> i & 1);
    }

    private static void updateCounters(int[] counters, long hash) {
        for (int i = 0; i < 64; i++) counters[i] += (hash >>> i & 1);
    }

    private static void updateCounters(int[] counters, int hash) {
        for (int i = 0; i < 32; i++) counters[i] += (hash >>> i & 1);
    }

    private static void updateCounters(int[] counters, short hash) {
        for (int i = 0; i < 16; i++) counters[i] += (hash >>> i & 1);
    }
}
