package brutalicious.gen;

import static brutalicious.gen.CityPlan.*;

public final class Footings {
    private Footings() {}

    public static final int FLOOR = -58, CEIL = -43;

    public static boolean present(long seed, int cx, int cz) {
        return Rng.chance(Rng.hash(seed ^ 0xF007L, cx, cz), 0.7);
    }

    static void fill(Column c) {
        long seed = c.seed;
        int cx = Math.floorDiv(c.x, CELL), cz = Math.floorDiv(c.z, CELL);
        if (!present(seed, cx, cz)) return;
        int dx = Math.floorMod(c.x, 32) - 16, dz = Math.floorMod(c.z, 32) - 16;
        int d = Math.max(Math.abs(dx), Math.abs(dz));
        double sculk = Rng.fbm(seed ^ 0xF00CL, c.x / 13.0, c.z / 13.0);
        c.set(FLOOR, sculk > 0.6 ? B.SCULK : Math.floorMod(c.x, 8) == 0 || Math.floorMod(c.z, 8) == 0 ? B.POLISHED_DEEPSLATE : B.DEEPSLATE_TILES);
        for (int y = FLOOR + 1; y < CEIL; y++) {
            int half = y <= FLOOR + 2 ? 4 : y >= CEIL - 5 ? 3 + (y - (CEIL - 6)) : 3;
            if (d <= half) c.set(y, Math.floorMod(y, 6) == 0 || d == half ? B.POLISHED_DEEPSLATE : B.DEEPSLATE_BRICKS);
            else c.set(y, B.AIR);
        }
        if (d <= 4) return;
        int y = FLOOR + 1;
        long h = c.hash(y, 180);
        double u = Rng.unit(h);
        if (sculk > 0.6 && u < 0.006) c.set(y, B.SCULK_SHRIEKER);
        else if (sculk > 0.6 && u < 0.012) c.set(y, B.SCULK_SENSOR);
        else if (u < 0.0005) Decor.container(c, y, B.CHEST, Decor.LOOT_FOOTINGS);
        else if (u < 0.0017) Decor.spawner(c, y, Decor.pick(h >>> 30, "minecraft:skeleton", "minecraft:zombie", "minecraft:cave_spider", "minecraft:creeper"));
        else if (u < 0.0040) c.set(y, B.BONE_BLOCK);
        else if (u < 0.0050) c.set(y, B.rot16(B.SKULL, (int) (h >>> 20)));
        if (Rng.chance(h >>> 9, 0.002)) {
            int len = 3 + (int) Math.floorMod(h >>> 24, 10L);
            for (int k = 1; k <= len; k++) c.set(CEIL - k, B.CHAIN);
        }
        long ch = Rng.hash(seed ^ 0xF0CAL, cx, cz);
        int ox = cx * CELL + 32, oz = cz * CELL + 32;
        if (Rng.chance(ch, 0.12) && Math.abs(c.x - ox) <= 3 && Math.abs(c.z - oz) <= 3) Camp.fill(c, c.x - ox, c.z - oz, FLOOR, ch);
    }
}
