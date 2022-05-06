package membership;

import hash.MurmurHash3;

import java.util.Arrays;

import static utils.IntegerUtils.nextPow2;

public class RangeQueryCountMinSketch {
    private static final int MAX_W = 1 << 30;
    private static final int MAX_D = 32;
    private static final double MIN_EPS = 1.0 / MAX_W;
    private static final double MAX_CONFIDENCE = 1 - 1.0 / Math.exp(MAX_D);
    private final int[][][] matrix;
    private final int height, depth, mask;
    private final long minAcceptedValue, maxAcceptedValue, unsignedRange;
    private int count;

    public RangeQueryCountMinSketch(int width, final int depth, final long minAcceptedValue, final long maxAcceptedValue) {
        if (width <= 0) throw new IllegalArgumentException("Width must be positive");
        if (width > MAX_W) throw new IllegalArgumentException("Width must be less than 2^30");
        if (depth <= 0) throw new IllegalArgumentException("Depth must be positive");
        if (depth >= MAX_D) throw new IllegalArgumentException("Depth must be less than 16");
        if (minAcceptedValue >= maxAcceptedValue)
            throw new IllegalArgumentException("Range start must be less than range end");

        this.minAcceptedValue = minAcceptedValue;
        this.maxAcceptedValue = maxAcceptedValue;

        //calculate required height
        long range = maxAcceptedValue - minAcceptedValue;
        long tmpRangeMax = 1;
        int tmpHeight = 1;
        while (Long.compareUnsigned(tmpRangeMax, range) < 0) {
            tmpRangeMax = tmpRangeMax << 1 | 1;
            tmpHeight++;
        }
        this.height = tmpHeight;
        this.unsignedRange = tmpRangeMax;

        this.depth = depth;
        width = nextPow2(width);
        this.mask = width - 1;
        matrix = new int[height][this.depth][width];
    }

    public RangeQueryCountMinSketch(double eps, double confidence, final long minAcceptedValue, final long maxAcceptedValue) {
        if (eps <= 0 || eps >= 1) throw new IllegalArgumentException("eps must be in (0, 1)");
        if (eps < MIN_EPS) throw new IllegalArgumentException("eps must be greater than 2^-30");
        if (confidence <= 0 || confidence >= 1) throw new IllegalArgumentException("conf must be in (0, 1)");
        if (confidence >= MAX_CONFIDENCE) throw new IllegalArgumentException("1-conf must be greater than e^-32");
        if (minAcceptedValue >= maxAcceptedValue)
            throw new IllegalArgumentException("Range start must be less than range end");

        int width = nextPow2((int) Math.ceil(Math.E / eps));
        int depth = (int) Math.ceil(Math.log(1 / (1 - confidence)));

        this.minAcceptedValue = minAcceptedValue;
        this.maxAcceptedValue = maxAcceptedValue;

        //calculate required height
        long range = maxAcceptedValue - minAcceptedValue;
        long tmpRangeMax = 1;
        int tmpHeight = 1;
        while (Long.compareUnsigned(tmpRangeMax, range) < 0) {
            tmpRangeMax = tmpRangeMax << 1 | 1;
            tmpHeight++;
        }
        this.height = tmpHeight;
        this.unsignedRange = tmpRangeMax;

        this.depth = depth;
        width = nextPow2(width);
        this.mask = width - 1;
        matrix = new int[height][this.depth][width];
    }

    public void add(long value) {
        if (value < minAcceptedValue || value > maxAcceptedValue)
            throw new IllegalArgumentException("Value must be within range");
        if (count != Integer.MAX_VALUE) count++;

        //next we will treat the value as an unsigned long
        value -= minAcceptedValue;
        for (int i = 0; i < height; i++) {
            long index = value >>> i;
            for (int j = 0; j < depth; j++) {
                int hash = hash(index, j) & mask;
                if (matrix[i][j][hash] != Integer.MAX_VALUE) matrix[i][j][hash]++;
            }
        }
    }


    public void add(long value, int amount) {
        if (value < minAcceptedValue || value > maxAcceptedValue)
            throw new IllegalArgumentException("Value must be within range");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");

        if (count >= Integer.MAX_VALUE - amount) count = Integer.MAX_VALUE;
        else count += amount;

        value -= minAcceptedValue;
        //next we will treat the value as an unsigned long
        for (int i = 0; i < height; i++) {
            long index = value >>> i;
            for (int j = 0; j < depth; j++) {
                int hash = hash(index, j) & mask;
                if (matrix[i][j][hash] >= Integer.MAX_VALUE - amount) count = Integer.MAX_VALUE;
                else matrix[i][j][hash] += amount;
            }
        }
    }

    public boolean contains(long value) {
        if (value < minAcceptedValue || value > maxAcceptedValue) return false;
        value -= minAcceptedValue;

        // we can only check the first Count-Min sketch, but to reduce false-positive errors we check all of them
        for (int i = 0; i < height; i++) {
            long index = value >>> i;
            for (int j = 0; j < depth; j++) {
                int hash = hash(index, j) & mask;
                if (matrix[i][j][hash] == 0) return false;
            }
        }
        return true;
    }

    public void clear() {
        count = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < depth; j++) {
                Arrays.fill(matrix[i][j], 0);
            }
        }
    }

    public int getCount() {
        return count;
    }

    public int getCount(long value) {
        if (value < minAcceptedValue || value > maxAcceptedValue) return 0;
        value -= minAcceptedValue;
        int count = Integer.MAX_VALUE;

        // we can only check the first Count-Min sketch, but to reduce over-estimation errors we check all of them
        for (int i = 0; i < height; i++) {
            long index = value >>> i;
            for (int j = 0; j < depth; j++) {
                int hash = hash(index, j) & mask;
                if (matrix[i][j][hash] < count) count = matrix[i][j][hash];
            }
        }
        return count;
    }

    public int getCount(long leftInclusive, long rightInclusive) {
        if (leftInclusive > rightInclusive)
            throw new IllegalArgumentException("Left must be less than or equal to right");
        if (rightInclusive < minAcceptedValue || leftInclusive > maxAcceptedValue) return 0;
        if (leftInclusive <= minAcceptedValue && rightInclusive >= maxAcceptedValue) return count;

        if (leftInclusive <= minAcceptedValue) leftInclusive = 0;
        else leftInclusive -= minAcceptedValue;

        rightInclusive -= minAcceptedValue;
        return getCount(leftInclusive, rightInclusive, 0, unsignedRange, height);
    }

    private int getCount(long ql, long qr, long l, long r, int h) {
        if (Long.compareUnsigned(ql, l) <= 0 && Long.compareUnsigned(qr, r) >= 0) {
            long index = l >>> h;
            int ans = Integer.MAX_VALUE;
            for (int j = 0; j < depth; j++) {
                int hash = hash(index, j) & mask;
                if (matrix[h][j][hash] < ans) ans = matrix[h][j][hash];
            }
            return ans;
        } else if (Long.compareUnsigned(ql, r) > 0 || Long.compareUnsigned(qr, l) < 0) return 0;

        long mid = (l + r) >>> 1;
        return getCount(ql, qr, l, mid, h - 1) + getCount(ql, qr, mid + 1, r, h - 1);
    }

    private int hash(long value, int seed) {
        return MurmurHash3.hash32x64(value, seed) & mask;
    }
}
