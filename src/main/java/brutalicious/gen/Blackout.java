package brutalicious.gen;

import brutalicious.gen.building.Building;
import brutalicious.gen.building.Mass;

import static brutalicious.gen.CityPlan.*;

public final class Blackout {
    private Blackout() {}

    public static final int BOTTOM = 120, TOP = 123;

    static boolean well(long seed, int x, int z) {
        return Rng.fbm(seed ^ 0xB1AC7L, x / 9.0, z / 9.0) > 0.83;
    }

    static boolean dark(long seed, int x, int z) {
        return Zones.effectiveAt(seed, x, z) == Zones.Zone.DARK;
    }

    static void fill(Column c, Plot plot, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz, int lift) {
        long seed = c.seed;
        Zones.Zone e = Zones.effective(seed, plot, rx, rz, lx, lz, wx, wz, cx, cz);
        if (e == null) {
            if (!(dark(seed, c.x - 1, c.z) || dark(seed, c.x + 1, c.z) || dark(seed, c.x, c.z - 1) || dark(seed, c.x, c.z + 1))) return;
            for (int y = TOP; y > Zones.WALL_TOP + lift && c.isAir(y); y--)
                c.set(y, Math.floorMod(y, 8) == 0 ? B.POLISHED_DEEPSLATE : Palette.MONOLITH.wallAt(seed, c.x, y, c.z, 0.2));
            return;
        }
        if (e != Zones.Zone.DARK) return;
        Building b = (rx || rz) ? null : plot.building;
        int ux = lx - wx, uz = lz - wz;
        Mass pillar = (rx && rz) ? plot.pillar : null;
        boolean well = well(seed, c.x, c.z);
        boolean girder = Math.floorMod(c.x, 32) < 2 || Math.floorMod(c.z, 32) < 2;
        boolean top = false;
        for (int y = BOTTOM - (girder ? 3 : 0); y <= TOP; y++) {
            if (b != null && b.occupied(ux, uz, y - lift)) continue;
            if (pillar != null && pillar.occupied(lx, lz, y - lift)) continue;
            if (!c.isAir(y)) continue;
            if (well) continue;
            c.set(y, y == TOP ? (Math.floorMod(c.x, 16) == 0 || Math.floorMod(c.z, 16) == 0 ? B.POLISHED_ANDESITE : B.GRAY_CONCRETE)
                    : y >= BOTTOM ? B.GRAY_CONCRETE : B.POLISHED_DEEPSLATE);
            if (y == TOP) top = true;
        }
        if (well) {
            boolean rim = !well(seed, c.x + 1, c.z) || !well(seed, c.x - 1, c.z) || !well(seed, c.x, c.z + 1) || !well(seed, c.x, c.z - 1);
            if (rim && Rng.chance(c.hash(BOTTOM, 160), 0.4)) {
                int len = 1 + (int) Math.floorMod(c.hash(BOTTOM, 161), 5L);
                for (int y = BOTTOM; y > BOTTOM - len; y--) if (c.isAir(y)) c.setConnected(y, B.IRON_BARS);
            }
            return;
        }
        if (!girder && Math.floorMod(c.x, 16) == 8 && Math.floorMod(c.z, 16) == 8 && c.isAir(BOTTOM - 1)) {
            int len = 2 + (int) Math.floorMod(c.hash(BOTTOM, 162), 6L);
            int y = BOTTOM - 1;
            for (int i = 0; i < len && c.isAir(y); i++, y--) c.set(y, B.CHAIN);
            if (c.isAir(y)) c.set(y, B.REDSTONE_LAMP);
        }
        if (!top || !c.isAir(TOP + 1)) return;
        int gx = Math.floorDiv(c.x, 40), gz = Math.floorDiv(c.z, 40);
        long h = Rng.hash(seed ^ 0xB1AD0L, gx, gz);
        int dx = c.x - (gx * 40 + 20), dz = c.z - (gz * 40 + 20);
        if (Rng.chance(h, 0.5) && Math.abs(dx) <= 2 && Math.abs(dz) <= 2) {
            boolean shell = Math.abs(dx) == 2 || Math.abs(dz) == 2;
            for (int y = TOP + 1; y <= TOP + 3; y++) c.set(y, shell ? (y == TOP + 2 ? B.IRON_BARS : B.GRAY_CONCRETE) : B.AIR);
            c.set(TOP + 4, B.SMOOTH_STONE_SLAB);
            if (!shell) c.set(TOP, B.IRON_BARS);
        }
    }
}
