package brutalicious.gen;

import net.minecraft.core.Direction;

import static brutalicious.gen.CityGen.GROUND;
import static brutalicious.gen.CityPlan.*;

public final class DeepWorks {
    private DeepWorks() {}

    static final int AXIS = -1;
    static final double R = 6.5;
    static final int FLOOR = -6;
    static final int HALL_TOP = 8;

    static void fill(Column c, boolean rx, boolean rz, int lx, int lz, int cx, int cz) {
        boolean bx = rx && major(cx), bz = rz && major(cz);
        if (bx && bz) { junction(c, lx, lz); return; }
        if (bx) bore(c, lx, c.z, true, cx, Math.floorDiv(c.z, CELL));
        if (bz) bore(c, lz, c.x, false, cz, Math.floorDiv(c.x, CELL));
    }

    static void bore(Column c, int a, int along, boolean xBand, int road, int seg) {
        double t = a - 7.5;
        long hs = Rng.hash(c.seed ^ 0xDEE9L, road, seg, xBand ? 1 : 2);
        int k = Math.floorMod(along, CELL);
        boolean rib = Math.floorMod(along, 6) == 0;
        for (int y = AXIS - 9; y <= AXIS + 9; y++) {
            double dy = y - AXIS, d = Math.sqrt(t * t + dy * dy);
            if (d <= R) c.set(y, y <= FLOOR ? (Math.abs(t) <= 5 ? B.SMOOTH_STONE : B.DEEPSLATE_TILES) : B.AIR);
            else if (d <= R + 1.3) c.set(y, rib ? B.DEEPSLATE_TILES : B.POLISHED_DEEPSLATE);
        }
        boolean ballast = (a >= 3 && a <= 5) || (a >= 10 && a <= 12);
        if (ballast) c.set(FLOOR, B.GRAVEL);
        boolean rail = a == 4 || a == 11;
        int y = FLOOR + 1;
        if (Rng.chance(hs, 0.08) && k >= 20 && k <= 36) {
            int kk = k - 20;
            for (int yy = FLOOR + 1; yy <= AXIS + 6; yy++) {
                double d = Math.sqrt(t * t + (yy - AXIS) * (yy - AXIS));
                if (kk == 0) { if (d <= 5.8) c.set(yy, (Math.abs(t) < 0.6 || Math.abs(yy - AXIS) < 1) ? B.POLISHED_BLACKSTONE : B.IRON_BARS); }
                else if (d <= 5.8 && d > 4.8) c.set(yy, kk % 4 == 0 ? B.POLISHED_BLACKSTONE : B.GRAY_CONCRETE);
                else if (d <= 1.5) c.set(yy, B.POLISHED_DEEPSLATE);
            }
            if (kk == 8 && Math.abs(t) < 1) Decor.container(c, FLOOR + 1, B.CHEST, Decor.LOOT_DEEP);
            return;
        }
        int track = (int) Math.floorMod(hs >>> 4, 2L), ca = track == 0 ? 4 : 11;
        if (Rng.chance(hs >>> 8, 0.4) && k >= 6 && k <= 50 && Math.abs(a - ca) <= 1) {
            int w = k - 6;
            if (Math.floorMod(w, 12) == 11) { if (rail) c.set(y, xBand ? B.RAIL_NS : B.RAIL_EW); return; }
            boolean side = Math.abs(a - ca) == 1;
            c.set(y, B.DEEPSLATE_TILES);
            c.set(y + 1, side && Math.floorMod(w, 12) != 5 ? B.POLISHED_DEEPSLATE : B.AIR);
            c.set(y + 2, side ? B.TINTED_GLASS : B.AIR);
            c.set(y + 3, B.POLISHED_DEEPSLATE);
            if (!side && Math.floorMod(w, 12) == 2 && Rng.chance(Rng.hash(hs, w), 0.25)) Decor.container(c, y + 1, B.CHEST, Decor.LOOT_DEEP);
            return;
        }
        if (rail && !Rng.chance(c.hash(y, 140), 0.06)) c.set(y, xBand ? B.RAIL_NS : B.RAIL_EW);
        int v = Math.floorMod(along, 96);
        if (a <= 1 && v >= 46 && v <= 50) {
            c.set(FLOOR, B.DEEPSLATE_TILES);
            c.fill(FLOOR + 1, FLOOR + 3, B.AIR);
            c.set(FLOOR + 4, B.DEEPSLATE_TILES);
            if (a == 0 && v != 48) Decor.container(c, FLOOR + 1, B.CHEST, Decor.LOOT_VAULT);
            if (a == 1 && v == 48) Decor.spawner(c, FLOOR + 3, "minecraft:skeleton");
            return;
        }
        if ((a == 1 || a == 14) && Math.floorMod(along + (a == 1 ? 0 : 12), 24) == 0) {
            long sh = c.hash(y, 141);
            Decor.spawner(c, y, Decor.pick(sh, "minecraft:zombie", "minecraft:skeleton", "minecraft:cave_spider", "minecraft:spider", "minecraft:creeper"));
        }
        double s = Rng.fbm(c.seed ^ 0x5C0BL, c.x / 9.0, c.z / 9.0);
        if (s > 0.62 && !ballast) c.set(FLOOR, B.SCULK);
        if (s > 0.62 && !ballast && c.isAir(y) && Rng.chance(c.hash(y, 142), 0.02)) c.set(y, B.SCULK_SHRIEKER);
    }

    static void junction(Column c, int lx, int lz) {
        boolean pillar = (lx <= 1 || lx >= 14) && (lz <= 1 || lz >= 14);
        c.set(FLOOR, B.DEEPSLATE_TILES);
        for (int y = FLOOR + 1; y <= HALL_TOP; y++) c.set(y, pillar ? B.POLISHED_DEEPSLATE : B.AIR);
        c.set(HALL_TOP + 1, B.DEEPSLATE_TILES);
        if (pillar) return;
        double s = Rng.fbm(c.seed ^ 0x5C0CL, c.x / 6.0, c.z / 6.0);
        if (s > 0.45) c.set(FLOOR, B.SCULK);
        long h = c.hash(FLOOR, 143);
        if (s > 0.45 && Rng.chance(h, 0.05)) c.set(FLOOR + 1, B.SCULK_SHRIEKER);
        else if (s > 0.6 && Rng.chance(h >>> 9, 0.01)) c.set(FLOOR + 1, B.SCULK_CATALYST);
        int low = Footings.present(c.seed, Math.floorDiv(c.x, CELL), Math.floorDiv(c.z, CELL)) ? Footings.FLOOR + 1 : CityGen.CISTERN_FLOOR + 1;
        if (lx == 12 && lz == 11) c.fill(low, GROUND - 1, B.POLISHED_DEEPSLATE);
        if (lx == 12 && lz == 12) {
            for (int y = low; y < GROUND; y++) c.set(y, B.ladder(Direction.SOUTH));
            c.set(GROUND, B.IRON_TRAPDOOR);
        }
    }

    static void headframe(Column c, int lx, int lz, int cx, int cz, int ground) {
        if (!major(cx) || !major(cz)) return;
        int dx = lx - 12, dz = lz - 12;
        if (Math.abs(dx) > 1 || Math.abs(dz) > 1 || (dx == 0 && dz == 0)) return;
        int base = GROUND + ground;
        boolean corner = Math.abs(dx) == 1 && Math.abs(dz) == 1;
        for (int y = base + 1; y <= base + 9; y++) if (corner || y == base + 9) c.setConnected(y, B.IRON_BARS);
        if (dx == 0 && dz == 1) c.set(base + 10, B.POLISHED_DEEPSLATE);
    }
}
