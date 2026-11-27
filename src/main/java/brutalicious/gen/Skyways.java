package brutalicious.gen;

import brutalicious.gen.building.Building;
import brutalicious.gen.building.Mass;

import static brutalicious.gen.CityPlan.*;

public final class Skyways {
    private Skyways() {}

    public static final int FLOOR_F = 38;
    public static final int FLOOR = Building.floorY(FLOOR_F);
    public static final int CEIL = FLOOR + 5;

    static boolean lineX(int cx) { return Math.floorMod(cx, 3) == 0; }

    static boolean lineZ(int cz) { return Math.floorMod(cz, 3) == 0; }

    static boolean fallen(long seed, int cx, int cz, boolean xBand) {
        return Rng.chance(Rng.hash(seed ^ 0x5CAFL, cx, cz, xBand ? 1 : 2), 0.11);
    }

    static void fill(Column c, Plot plot, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz, int lift) {
        if (!(rx && lineX(cx)) && !(rz && lineZ(cz))) return;
        if (!Roof.present(c.seed, c.x, c.z)) return;
        int tX = lx - wx / 2, tZ = lz - wz / 2;
        int t, along, k;
        boolean xBand;
        if (rx && rz) {
            Mass pillar = plot.pillar;
            if (pillar != null && pillar.occupied(lx, lz, FLOOR + 2 - lift)) return;
            boolean inX = lineX(cx) && Math.abs(tX) <= 3, inZ = lineZ(cz) && Math.abs(tZ) <= 3;
            if (!inX && !inZ) return;
            int ax = Math.abs(tX), az = Math.abs(tZ), a;
            boolean bothLines = lineX(cx) && lineZ(cz);
            if (bothLines) a = (ax == 3 && az == 3) ? 3 : (ax <= 3 && az <= 3) ? 0 : ax <= 3 ? ax : az;
            else a = inX ? ax : az;
            gallery(c, a, 0, inX && !bothLines ? c.z : c.x);
            return;
        }
        xBand = rx;
        t = xBand ? tX : tZ;
        if (Math.abs(t) > 3) return;
        k = xBand ? lz - wz : lx - wx;
        along = xBand ? c.z : c.x;
        if (fallen(c.seed, cx, cz, xBand) && k >= 18 && k <= 30) {
            if ((k == 18 || k == 30) && Rng.chance(c.hash(FLOOR, 190), 0.5)) {
                int len = 2 + (int) Math.floorMod(c.hash(FLOOR, 191), 10L);
                for (int y = FLOOR; y > FLOOR - len; y--) if (c.isAir(y)) c.setConnected(y, B.IRON_BARS);
            }
            return;
        }
        gallery(c, Math.abs(t), k, along);
        if (Math.abs(t) == 3 && Math.floorMod(along, 16) == 0) {
            for (int y = CEIL + 1; y < Roof.BOTTOM && c.isAir(y); y++) c.set(y, B.CHAIN);
        }
    }

    static void gallery(Column c, int a, int k, int along) {
        c.set(FLOOR, a == 3 ? B.GRAY_CONCRETE : B.SMOOTH_STONE);
        c.set(CEIL, B.GRAY_CONCRETE);
        boolean rib = Math.floorMod(along, 8) == 0;
        if (a == 3) {
            for (int y = FLOOR + 1; y < CEIL; y++) {
                boolean window = !rib && (y == FLOOR + 2 || y == FLOOR + 3);
                if (window) c.setConnected(y, Rng.chance(c.hash(y, 192), 0.7) ? B.AIR : B.GRAY_GLASS);
                else c.set(y, rib ? B.POLISHED_ANDESITE : B.LIGHT_GRAY_CONCRETE);
            }
            if (rib) c.set(CEIL + 1, B.POLISHED_ANDESITE);
            return;
        }
        for (int y = FLOOR + 1; y < CEIL; y++) c.set(y, B.AIR);
        if (a == 2) c.set(CEIL - 1, B.GRAY_CONCRETE);
        if (a == 0 && Math.floorMod(along, 6) == 0) c.set(CEIL, B.REDSTONE_LAMP);
        long h = c.hash(FLOOR, 193);
        if (a == 2 && Math.floorMod(along, 12) == 6) c.set(FLOOR + 1, B.SMOOTH_STONE_SLAB);
        else if (Rng.chance(h, 0.0012)) Decor.container(c, FLOOR + 1, B.BARREL, Decor.LOOT_APARTMENT);
        else if (Rng.chance(h >>> 16, 0.0008)) Decor.spawner(c, FLOOR + 1, Decor.pick(h >>> 30, "minecraft:skeleton", "minecraft:zombie"));
        else if (Rng.chance(h >>> 40, 0.004)) c.set(FLOOR + 1, B.COBBLE_SLAB);
    }
}
