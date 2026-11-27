package brutalicious.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

import static brutalicious.gen.CityGen.GROUND;
import static brutalicious.gen.CityPlan.*;

public final class Terrain {
    private Terrain() {}

    public static final int SPLIT = 40;
    static final int FLIGHT_START = 6;

    public static int cellH(long seed, int cx, int cz) {
        Zones.Zone z = Zones.zone(seed, cx, cz);
        if (z == Zones.Zone.FLOODED) return -4;
        if (z == Zones.Zone.SPACEPORT) return 0;
        if (z == Zones.Zone.DARK) return 0;
        double n = Rng.fbm(seed ^ 0x7E44A1L, cx / 4.5, cz / 4.5);
        int steps = (int) Math.round((n - 0.5) * 12);
        steps += switch (z) {
            case COMPOUND, NECROPOLIS -> 2;
            case INDUSTRIAL, POWER -> -1;
            case SURVIVOR -> 1;
            default -> 0;
        };
        return Math.max(-2, Math.min(6, steps)) * brutalicious.gen.building.Building.FLOOR_H;
    }

    public static int segmentDelta(long seed, int cx, int cz, boolean xBand) {
        int h1 = cellH(seed, cx, cz);
        return (xBand ? cellH(seed, cx, cz + 1) : cellH(seed, cx + 1, cz)) - h1;
    }

    public static int[] spec(long seed, Plot plot, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz) {
        int[] wall = Zones.wallSpec(seed, plot, rx, rz, lx, lz, wx, wz, cx, cz);
        if (wall != null) return new int[]{wall[0], wall[1], wall[2], 0};
        if (!rx && !rz) return flat(cellH(seed, cx, cz));
        int ecx = cx, ecz = cz;
        boolean wX = rx && Zones.wallX(seed, cx, cz), wZ = rz && Zones.wallZ(seed, cx, cz);
        if (rx && Math.floorMod(cx, Zones.REGION) == 0 && lx - wx / 2 < -3 && (wX || (rz && Zones.throughXAt(seed, cx, cz)))) ecx = cx - 1;
        if (rz && Math.floorMod(cz, Zones.REGION) == 0 && lz - wz / 2 < -3 && (wZ || (rx && Zones.throughZAt(seed, cx, cz)))) ecz = cz - 1;
        if (rx && rz) return flat(cellH(seed, ecx, ecz));
        if (Zones.zone(seed, ecx, ecz) == Zones.Zone.FLOODED) return flat(cellH(seed, ecx, ecz));

        boolean xBand = rx;
        int a = xBand ? lx : lz, w = xBand ? wx : wz;
        int k = xBand ? lz - wz : lx - wx, len = CELL - (xBand ? wz : wx);
        int h1 = cellH(seed, ecx, ecz);
        int h2 = xBand ? cellH(seed, ecx, cz + 1) : cellH(seed, cx + 1, ecz);
        Direction fwd = xBand ? Direction.SOUTH : Direction.EAST;
        int dh = h2 - h1;
        if (dh != 0 && k >= len - Math.abs(dh)) {
            int i = k - (len - Math.abs(dh));
            return dh > 0 ? stair(h1 + i, fwd) : stair(h1 - i - 1, fwd.getOpposite());
        }
        boolean walled = xBand ? wX : wZ;
        if (!walled && a <= 1) {
            int hp = xBand ? cellH(seed, cx - 1, cz) : cellH(seed, cx, cz - 1);
            int dp = hp - h1;
            if (dp != 0) {
                int k0 = FLIGHT_START;
                if (k >= k0 && k < k0 + Math.abs(dp)) {
                    int i = k - k0;
                    return dp > 0 ? stair(h1 + i, fwd) : stair(h1 - i - 1, fwd.getOpposite());
                }
                if (k >= k0 + Math.abs(dp)) return flat(hp);
            }
        }
        return flat(h1);
    }

    static int[] flat(int h) { return new int[]{h, h, h, 0}; }

    static int[] stair(int shift, Direction up) { return new int[]{shift, shift, shift, 1 + up.get2DDataValue()}; }

    static int groundAt(long seed, int x, int z) {
        int cx = Math.floorDiv(x, CELL), cz = Math.floorDiv(z, CELL);
        int lx = Math.floorMod(x, CELL), lz = Math.floorMod(z, CELL);
        int wx = roadWidth(cx), wz = roadWidth(cz);
        Plot p = plot(seed, cx, cz);
        int[] s = spec(seed, p, lx < wx, lz < wz, lx, lz, wx, wz, cx, cz);
        if (s[1] != s[2] || Zones.wallSpec(seed, p, lx < wx, lz < wz, lx, lz, wx, wz, cx, cz) != null) return Integer.MAX_VALUE;
        return s[0] + (s[3] != 0 ? 1 : 0);
    }

    static void dress(Column c, int[] s, boolean wallColumn) {
        int g = s[0];
        if (s[3] != 0) {
            Direction up = Direction.from2DDataValue(s[3] - 1);
            c.set(GROUND + 1 + g, B.stair(Blocks.POLISHED_ANDESITE_STAIRS, up));
            c.fill(GROUND + 2 + g, GROUND + 6 + g, B.AIR);
            return;
        }
        if (wallColumn) return;
        long seed = c.seed;
        int low = Integer.MAX_VALUE;
        int[][] nb = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : nb) low = Math.min(low, groundAt(seed, c.x + d[0], c.z + d[1]));
        if (low == Integer.MAX_VALUE || low >= g - 1) return;
        Palette pal = Palette.BETON;
        for (int y = GROUND + low; y < GROUND + g; y++) c.set(y, Math.floorMod(y, 4) == 3 ? pal.trim : pal.wallAt(seed, c.x, y, c.z, 0.25));
        if (c.isAir(GROUND + g + 1) && !c.isAir(GROUND + g)) c.set(GROUND + g + 1, pal.parapet);
    }
}
