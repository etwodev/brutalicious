package brutalicious.gen;

import net.minecraft.world.level.block.state.BlockState;

public final class Camp {
    private Camp() {}

    public static final String LOOT = "brutalicious:chests/camp";

    static final String[] TENT = {
            "TRT..WW",
            "TRT...W",
            "TRT.S..",
            "...SFS.",
            "BB..S.K",
            "B......",
            "..b....",
    };
    static final String[] LEAN_TO = {
            "PxxxP..",
            "xbbxx.W",
            "xCbBx.W",
            "PxxOP..",
            ".......",
            "..SFS..",
            "...S.KB",
    };
    static final String[] CACHE = {
            ".......",
            ".BBB...",
            ".BxB.b.",
            ".BBB.b.",
            "...S...",
            "..SFS..",
            "...K...",
    };

    public static void fill(Column c, int dx, int dz, int base, long h) {
        if (Math.abs(dx) > 3 || Math.abs(dz) > 3) return;
        if (c.get(base).isAir() || !c.isAir(base + 1)) return;
        int i = dx + 3, j = dz + 3, rot = (int) (h & 3);
        int a, b;
        switch (rot) {
            case 1 -> { a = j; b = 6 - i; }
            case 2 -> { a = 6 - i; b = 6 - j; }
            case 3 -> { a = 6 - j; b = i; }
            default -> { a = i; b = j; }
        }
        double u = Rng.unit(h >>> 3);
        String[] map = u < 0.45 ? TENT : u < 0.8 ? LEAN_TO : CACHE;
        char k = map[b].charAt(a);
        BlockState canvas = switch ((int) Math.floorMod(h >>> 9, 3L)) { case 0 -> B.GRAY_WOOL; default -> B.LIGHT_GRAY_WOOL; };
        BlockState roll = canvas == B.GRAY_WOOL ? B.LIGHT_GRAY_CARPET : B.GRAY_CARPET;
        int y = base + 1;
        long cell = c.hash(y, 90);
        boolean roofed = map == LEAN_TO && b <= 3 && a <= 4;
        switch (k) {
            case 'T' -> c.set(y, canvas);
            case 'R' -> { c.set(y, roll); c.set(y + 1, canvas); }
            case 'S' -> c.set(y, B.STUMP);
            case 'F' -> { c.set(y, B.CAMPFIRE_OUT); c.set(base, B.COBBLE); }
            case 'B' -> {
                if (Rng.chance(cell, 0.2)) Decor.container(c, y, B.BARREL, LOOT);
                else c.set(y, B.BARREL);
                if (map == CACHE && Rng.chance(cell >>> 7, 0.5) && c.isAir(y + 1)) c.set(y + 1, B.BARREL);
            }
            case 'K' -> c.set(y, B.WATER_CAULDRON);
            case 'W' -> { c.set(y, B.SPRUCE_LOG); if (Rng.chance(cell, 0.5)) c.set(y + 1, B.SPRUCE_LOG); }
            case 'b' -> c.set(y, roll);
            case 'P' -> { c.setConnected(y, B.SPRUCE_FENCE); c.setConnected(y + 1, B.SPRUCE_FENCE); }
            case 'C' -> c.set(y, B.CRAFTING_TABLE);
            case 'O' -> c.set(y, B.FURNACE);
            case 'x' -> { if (map == CACHE) c.set(y, canvas); }
            default -> {}
        }
        if (roofed) c.set(y + 2, canvas);
    }

    public static boolean onStrip(Column c, int across, int width, int along, int start, int base, long h) {
        int dx = across - width / 2, dz = along - start - 3;
        if (Math.abs(dx) > 3 || dz < -3 || dz > 3) return false;
        fill(c, dx, dz, base, h);
        return true;
    }
}
