package membership;

import hash.MurmurHash3;

import static utils.IntegerUtils.nextPow2;

public class CountMinSketch {
    private static final int MAX_W = 1 << 30;
    private static final int MAX_D = 32;
    private static final double MIN_EPS = 1.0 / MAX_W;
    private static final double MAX_CONFIDENCE = 1 - 1.0 / Math.exp(MAX_D);
    private final int[][] matrix;
    private final int width, depth, mask;
    private int count;

    public CountMinSketch(int width, int depth) {
        if (width <= 0) throw new IllegalArgumentException("Width must be positive");
        if (width > MAX_W) throw new IllegalArgumentException("Width must be less than 2^30");
        if (depth <= 0) throw new IllegalArgumentException("Depth must be positive");
        if (depth >= MAX_D) throw new IllegalArgumentException("Depth must be less than 16");
        this.width = nextPow2(width);
        this.mask = this.width - 1;
        this.depth = depth;
        matrix = new int[depth][width];
    }

    public CountMinSketch(double eps, double confidence) {
        if (eps <= 0 || eps >= 1) throw new IllegalArgumentException("eps must be in (0, 1)");
        if (eps < MIN_EPS) throw new IllegalArgumentException("eps must be greater than 2^-30");
        if (confidence <= 0 || confidence >= 1) throw new IllegalArgumentException("conf must be in (0, 1)");
        if (confidence >= MAX_CONFIDENCE) throw new IllegalArgumentException("1-conf must be greater than e^-32");

        this.width = nextPow2((int) Math.ceil(Math.E / eps));
        this.mask = this.width - 1;
        this.depth = (int) Math.ceil(Math.log(1 / (1 - confidence)));
        matrix = new int[depth][width];
    }

    public void add(String key) {
        if (key == null) throw new IllegalArgumentException("Key must not be null");
        if (count != Integer.MAX_VALUE) count++;
        byte[] bytes = key.getBytes();
        for (int i = 0; i < depth; i++) {
            int hash = hash(bytes, i);
            if (matrix[i][hash] != Integer.MAX_VALUE) matrix[i][hash]++;
        }
    }

    public void add(String key, int amount) {
        if (key == null) throw new IllegalArgumentException("Key must not be null");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");

        if (count >= Integer.MAX_VALUE - amount) count = Integer.MAX_VALUE;
        else count += amount;

        byte[] bytes = key.getBytes();
        for (int i = 0; i < depth; i++) {
            int hash = hash(bytes, i);
            if (matrix[i][hash] >= Integer.MAX_VALUE - amount) count = Integer.MAX_VALUE;
            else matrix[i][hash] += amount;
        }
    }

    public boolean contains(String key) {
        if (key == null) throw new IllegalArgumentException("Key must not be null");
        byte[] bytes = key.getBytes();
        for (int i = 0; i < depth; i++) {
            int hash = hash(bytes, i);
            if (matrix[i][hash] == 0) return false;
        }
        return true;
    }

    public int pointQuery(String key) {
        if (key == null) throw new IllegalArgumentException("Key must not be null");
        byte[] bytes = key.getBytes();
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < depth; i++) {
            int hash = hash(bytes, i);
            min = Math.min(min, matrix[i][hash]);
        }
        return min;
    }

    public long innerProduct(CountMinSketch other) {
        if (other == null) throw new IllegalArgumentException("Other must not be null");
        if (other.width != width || other.depth != depth)
            throw new IllegalArgumentException("Sketches must have the same width and depth");
        long min = Long.MAX_VALUE;
        for (int i = 0; i < depth; i++) {
            long sum = 0;
            int[] a = matrix[i];
            int[] b = other.matrix[i];
            for (int j = 0; j < width; j++) sum += (long) a[j] * b[j];
            min = Math.min(min, sum);
        }
        return min;
    }

    public int getCount() {
        return count;
    }

    private int hash(byte[] bytes, int i) {
        return MurmurHash3.hash32xArray(bytes, i) & mask;
    }
}
