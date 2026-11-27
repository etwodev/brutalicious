package brutalicious.gen;

import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Voxels {
    private Map<Long, BlockState> build = new HashMap<>();
    private Map<Long, int[]> columns;
    private final List<BlockState> states = new ArrayList<>();
    public int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
    public int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

    static long key3(int x, int y, int z) {
        return ((long) (x + (1 << 20)) << 42) | ((long) (y + (1 << 20)) << 21) | (z + (1 << 20));
    }

    static long key2(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    public Voxels set(int x, int y, int z, BlockState s) {
        build.put(key3(x, y, z), s);
        return this;
    }

    public Voxels box(int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++)
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) set(x, y, z, s);
        return this;
    }

    public Voxels hollow(int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
        box(x0, y0, z0, x1, y1, z1, s);
        if (x1 - x0 >= 2 && y1 - y0 >= 2 && z1 - z0 >= 2) box(x0 + 1, y0 + 1, z0 + 1, x1 - 1, y1 - 1, z1 - 1, B.AIR);
        return this;
    }

    public Voxels cylinder(int cx, int cz, int y0, int y1, double r, BlockState s, boolean hollow) {
        int ri = (int) Math.ceil(r + 0.5);
        for (int x = -ri; x <= ri; x++) for (int z = -ri; z <= ri; z++) {
            double d = Math.sqrt(x * x + z * z);
            if (d > r + 0.5) continue;
            boolean shell = d > r - 0.6;
            for (int y = y0; y <= y1; y++) set(cx + x, y, cz + z, (hollow && !shell) ? B.AIR : s);
        }
        return this;
    }

    public Voxels cylinderX(int x0, int x1, int cy, int cz, double r, BlockState s, boolean hollow) {
        int ri = (int) Math.ceil(r + 0.5);
        for (int y = -ri; y <= ri; y++) for (int z = -ri; z <= ri; z++) {
            double d = Math.sqrt(y * y + z * z);
            if (d > r + 0.5) continue;
            boolean shell = d > r - 0.6;
            for (int x = x0; x <= x1; x++) set(x, cy + y, cz + z, (hollow && !shell) ? B.AIR : s);
        }
        return this;
    }

    public Voxels sphere(int cx, int cy, int cz, double r, BlockState s, boolean hollow) {
        int ri = (int) Math.ceil(r + 0.5);
        for (int x = -ri; x <= ri; x++) for (int y = -ri; y <= ri; y++) for (int z = -ri; z <= ri; z++) {
            double d = Math.sqrt(x * x + y * y + z * z);
            if (d > r + 0.5) continue;
            set(cx + x, cy + y, cz + z, (hollow && d <= r - 0.6) ? B.AIR : s);
        }
        return this;
    }

    public Voxels line(double x0, double y0, double z0, double x1, double y1, double z1, BlockState s) {
        double len = Math.max(Math.abs(x1 - x0), Math.max(Math.abs(y1 - y0), Math.abs(z1 - z0)));
        int n = (int) Math.ceil(len * 2) + 1;
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            set((int) Math.round(x0 + (x1 - x0) * t), (int) Math.round(y0 + (y1 - y0) * t), (int) Math.round(z0 + (z1 - z0) * t), s);
        }
        return this;
    }

    public Voxels ellipsoid(int cx, int cy, int cz, double rx, double ry, double rz, BlockState s) {
        for (int x = (int) -Math.ceil(rx); x <= Math.ceil(rx); x++)
            for (int y = (int) -Math.ceil(ry); y <= Math.ceil(ry); y++)
                for (int z = (int) -Math.ceil(rz); z <= Math.ceil(rz); z++) {
                    double q = (x * x) / (rx * rx) + (y * y) / (ry * ry) + (z * z) / (rz * rz);
                    if (q <= 1.0) set(cx + x, cy + y, cz + z, s);
                }
        return this;
    }

    public Voxels rotated(int quarter) {
        int q = Math.floorMod(quarter, 4);
        Rotation rot = switch (q) { case 1 -> Rotation.CLOCKWISE_90; case 2 -> Rotation.CLOCKWISE_180; case 3 -> Rotation.COUNTERCLOCKWISE_90; default -> Rotation.NONE; };
        Voxels v = new Voxels();
        build.forEach((k, s) -> {
            int x = (int) (k >>> 42) - (1 << 20), y = (int) ((k >>> 21) & 0x1FFFFF) - (1 << 20), z = (int) (k & 0x1FFFFF) - (1 << 20);
            int nx = x, nz = z;
            switch (q) { case 1 -> { nx = -z; nz = x; } case 2 -> { nx = -x; nz = -z; } case 3 -> { nx = z; nz = -x; } default -> {} }
            v.set(nx, y, nz, s.rotate(rot));
        });
        return v;
    }

    public Voxels toppled() {
        Voxels v = new Voxels();
        int[] lo = {Integer.MAX_VALUE};
        build.forEach((k, s) -> lo[0] = Math.min(lo[0], (int) (k >>> 42) - (1 << 20)));
        build.forEach((k, s) -> {
            int x = (int) (k >>> 42) - (1 << 20), y = (int) ((k >>> 21) & 0x1FFFFF) - (1 << 20), z = (int) (k & 0x1FFFFF) - (1 << 20);
            if (s.isAir()) return;
            v.set(y, x - lo[0], z, s);
        });
        return v;
    }

    public Voxels damaged(long seed, double share) {
        Voxels v = new Voxels();
        build.forEach((k, s) -> {
            if (!s.isAir() && Rng.chance(Rng.mix(seed ^ k), share)) return;
            v.build.put(k, s);
        });
        return v;
    }

    public Voxels freeze() {
        Map<Long, List<int[]>> tmp = new HashMap<>();
        Map<BlockState, Integer> index = new HashMap<>();
        build.forEach((k, s) -> {
            int x = (int) (k >>> 42) - (1 << 20), y = (int) ((k >>> 21) & 0x1FFFFF) - (1 << 20), z = (int) (k & 0x1FFFFF) - (1 << 20);
            int si = index.computeIfAbsent(s, st -> { states.add(st); return states.size() - 1; });
            tmp.computeIfAbsent(key2(x, z), kk -> new ArrayList<>()).add(new int[]{y, si});
            minX = Math.min(minX, x); maxX = Math.max(maxX, x);
            minZ = Math.min(minZ, z); maxZ = Math.max(maxZ, z);
            minY = Math.min(minY, y); maxY = Math.max(maxY, y);
        });
        columns = new HashMap<>();
        tmp.forEach((k, l) -> {
            int[] a = new int[l.size() * 2];
            for (int i = 0; i < l.size(); i++) { a[2 * i] = l.get(i)[0]; a[2 * i + 1] = l.get(i)[1]; }
            columns.put(k, a);
        });
        build = null;
        return this;
    }

    public boolean covers(int x, int z) {
        return columns.containsKey(key2(x, z));
    }

    public void apply(Column c, int x, int z, int base) {
        int[] a = columns.get(key2(x, z));
        if (a == null) return;
        for (int i = 0; i < a.length; i += 2) c.set(base + a[i], states.get(a[i + 1]));
    }

    public int top(int x, int z) {
        int[] a = columns.get(key2(x, z));
        int t = Integer.MIN_VALUE;
        if (a != null) for (int i = 0; i < a.length; i += 2) if (!states.get(a[i + 1]).isAir()) t = Math.max(t, a[i]);
        return t;
    }
}
