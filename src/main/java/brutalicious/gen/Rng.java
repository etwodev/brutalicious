package brutalicious.gen;

public final class Rng {
    private Rng() {}

    public static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }

    public static long hash(long seed, int a) {
        return mix(seed ^ mix(a * 0x9E3779B97F4A7C15L + 0x632BE59BD9B4E019L));
    }

    public static long hash(long seed, int a, int b) {
        return mix(seed ^ mix(a * 0x9E3779B97F4A7C15L ^ mix(b * 0xC2B2AE3D27D4EB4FL + 0x165667B19E3779F9L)));
    }

    public static long hash(long seed, int a, int b, int c) {
        return mix(hash(seed, a, b) ^ mix(c * 0xD6E8FEB86659FD93L + 0x27D4EB2F165667C5L));
    }

    public static double unit(long h) {
        return (mix(h + 0x2545F4914F6CDD1DL) >>> 11) * 0x1.0p-53;
    }

    public static int range(long h, int lo, int hiInclusive) {
        if (hiInclusive <= lo) return lo;
        return lo + (int) Math.floorMod(mix(h + 0x6A09E667F3BCC909L), (long) (hiInclusive - lo + 1));
    }

    public static boolean chance(long h, double p) {
        return unit(h) < p;
    }

    public static double noise(long seed, double x, double z) {
        int ix = (int) Math.floor(x), iz = (int) Math.floor(z);
        double fx = x - ix, fz = z - iz;
        double sx = fx * fx * (3 - 2 * fx), sz = fz * fz * (3 - 2 * fz);
        double a = unit(hash(seed, ix, iz)), b = unit(hash(seed, ix + 1, iz));
        double c = unit(hash(seed, ix, iz + 1)), d = unit(hash(seed, ix + 1, iz + 1));
        double top = a + (b - a) * sx, bot = c + (d - c) * sx;
        return top + (bot - top) * sz;
    }

    public static double fbm(long seed, double x, double z) {
        return noise(seed, x, z) * 0.65 + noise(seed ^ 0x5DEECE66DL, x * 2.03, z * 2.03) * 0.35;
    }
}
