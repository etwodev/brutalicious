package brutalicious.gen;

import brutalicious.gen.building.Building;
import brutalicious.gen.building.Mass;

import static brutalicious.gen.CityPlan.*;

public final class Tier {
    private Tier() {}

    public static final int FLOOR = 20;
    public static final int TOP = Building.floorY(FLOOR);
    public static final int BOTTOM = TOP - 3;
    public static final int BEAM = BOTTOM - 2;

    public static boolean present(long seed, int x, int z) {
        double n = Rng.fbm(seed ^ 0x7135L, x / 560.0, z / 560.0);
        double edge = (Rng.noise(seed ^ 0x7136L, x / 6.0, z / 6.0) - 0.5) * 0.04;
        return n + edge > 0.585;
    }

    static boolean hole(long seed, int x, int z) {
        return Rng.fbm(seed ^ 0x7137L, x / 12.0, z / 12.0) > 0.79;
    }

    public static boolean pillarAt(long seed, int cx, int cz) {
        if (major(cx) || major(cz)) return false;
        int w = roadWidth(cx);
        return present(seed, cx * CELL + w / 2, cz * CELL + w / 2);
    }

    static void fill(Column c, Plot plot, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz, int lift) {
        if (!present(c.seed, c.x, c.z)) return;
        Building b = (rx || rz) ? null : plot.building;
        int ux = lx - wx, uz = lz - wz;
        Mass pillar = (rx && rz) ? plot.pillar : null;
        boolean hole = hole(c.seed, c.x, c.z);
        boolean beam = Math.floorMod(c.x, 16) < 2 || Math.floorMod(c.z, 16) < 2;
        boolean placedTop = false;
        for (int y = BEAM; y <= TOP; y++) {
            if (b != null && b.occupied(ux, uz, y - lift)) continue;
            if (pillar != null && pillar.occupied(lx, lz, y - lift)) continue;
            if (!c.isAir(y)) continue;
            if (y >= BOTTOM) {
                if (!hole) { c.set(y, y == TOP ? surface(c) : B.LIGHT_GRAY_CONCRETE); if (y == TOP) placedTop = true; }
            } else if (beam && !hole) {
                c.set(y, B.LIGHT_GRAY_CONCRETE);
            }
        }
        if (hole) {
            boolean edgeOfHole = !hole(c.seed, c.x + 1, c.z) || !hole(c.seed, c.x - 1, c.z) || !hole(c.seed, c.x, c.z + 1) || !hole(c.seed, c.x, c.z - 1);
            if (edgeOfHole && Rng.chance(c.hash(BOTTOM, 31), 0.3)) {
                int len = 1 + (int) Math.floorMod(c.hash(BOTTOM, 32), 4L);
                for (int y = BOTTOM; y > BOTTOM - len; y--) if (c.isAir(y)) c.setConnected(y, B.IRON_BARS);
            }
            return;
        }
        if (!placedTop || !c.isAir(TOP + 1)) return;
        int depth = Streets.craterDepth(c.seed, c.x, c.z, 0x71E2);
        Streets.crater(c, TOP, Math.min(depth, 4));
        if (depth != 0) return;
        topside(c, rx, rz, lx, lz, wx, wz, cx, cz);
    }

    static net.minecraft.world.level.block.state.BlockState surface(Column c) {
        if (Math.floorMod(c.x, 8) == 0 || Math.floorMod(c.z, 8) == 0) return B.GRAY_CONCRETE;
        return Rng.chance(c.hash(TOP, 33), 0.03) ? B.ANDESITE : B.LIGHT_GRAY_CONCRETE;
    }

    static void topside(Column c, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz) {
        if (rx == rz) return;
        int a = rx ? lx : lz, w = rx ? wx : wz;
        int along = rx ? c.z : c.x;
        long h = Rng.hash(c.seed ^ 0x71E3L, cx, cz, rx ? 1 : 2);
        double u = Rng.unit(h);
        if (u < 0.5) {
            int t = a - w / 2;
            if (Math.abs(t) <= 1) c.set(TOP, B.GRAVEL);
            if (t == 0) c.set(TOP + 1, rx ? B.RAIL_NS : B.RAIL_EW);
            if (Math.abs(t) == 3 && Math.floorMod(along, 24) == 0) {
                for (int y = TOP + 1; y <= TOP + 4; y++) c.setConnected(y, B.IRON_BARS);
                c.set(TOP + 5, B.REDSTONE_LAMP);
            }
        } else if (u < 0.8) {
            Streets.pipe(c, a - w / 2, along, TOP + 4, 3);
        } else if (u < 0.93) {
            int k = rx ? lz - wz : lx - wx;
            Camp.onStrip(c, a, w, k, 8 + Rng.range(h >>> 11, 0, 34), TOP, h);
        }
    }
}
