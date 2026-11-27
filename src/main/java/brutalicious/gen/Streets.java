package brutalicious.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static brutalicious.gen.CityGen.GROUND;
import static brutalicious.gen.CityPlan.*;

public final class Streets {
    private Streets() {}

    public static final int VIADUCT_X = 99, VIADUCT_Z = 107;

    static void street(Column c, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz) {
        if (CityPlan.plain(c.seed, c.x, c.z)) {
            CityGen.plainGround(c);
            return;
        }
        int depth = c.stairs ? 0 : craterDepth(c.seed, c.x, c.z, 0);
        Zones.Zone zone = Zones.zoneAt(c.seed, c.x, c.z);
        boolean evacuated = zone == Zones.Zone.ABANDONED;
        if (rx && rz) {
            c.set(GROUND, panel(c));
            crater(c, GROUND, depth);
            if (depth == 0) checkpoint(c, lx, lz, wx, wz, cx, cz);
            if (evacuated) drift(c);
            return;
        }
        int a = rx ? lx : lz, w = rx ? wx : wz;
        int along = rx ? c.z : c.x;
        if (a <= 1 || a >= w - 2) {
            boolean curb = a == 1 || a == w - 2;
            c.set(GROUND, curb ? B.POLISHED_ANDESITE : (Rng.chance(c.hash(GROUND, 6), 0.1) ? B.CRACKED_STONE_BRICKS : B.STONE_BRICKS));
            boolean lamp = (a == 1 && Math.floorMod(along, 32) == 16) || (a == w - 2 && Math.floorMod(along, 32) == 0);
            if (lamp) {
                long h = c.hash(GROUND, 7);
                if (Rng.chance(h, evacuated ? 0.75 : 0.25)) return;
                int top = GROUND + 5;
                for (int y = GROUND + 1; y < top; y++) c.setConnected(y, B.ANDESITE_WALL);
                c.set(top, Zones.zoneAt(c.seed, c.x, c.z) == Zones.Zone.SURVIVOR ? B.LANTERN : B.REDSTONE_LAMP);
            }
            crater(c, GROUND, depth);
            return;
        }
        c.set(GROUND, panel(c));
        crater(c, GROUND, depth);
        if (evacuated && !c.stairs) drift(c);
        if (depth != 0 || c.stairs) return;
        if (!major(rx ? cx : cz)) {
            int k = rx ? lz - wz : lx - wx;
            int clear = (rx ? CELL - wz : CELL - wx) - Math.abs(Terrain.segmentDelta(c.seed, cx, cz, rx)) - 9;
            long seg = Rng.hash(c.seed ^ 0x5E6L, cx, cz, rx ? 1 : 2);
            double u = Rng.unit(seg);
            if (u >= 0.32 && u < 0.46 && !evacuated && zone != Zones.Zone.DARK && !hasPipe(c.seed, cx, cz, rx)) {
                int start = 10 + Rng.range(seg >>> 7, 0, 32);
                if (start <= clear) Camp.onStrip(c, a, w, k, start, GROUND, seg);
            }
            if (u >= 0.22 && u < 0.32) {
                int at = 14 + Rng.range(seg >>> 7, 0, 20);
                if (at + 4 <= clear && k >= at && k <= at + 4 && (k - at) % 2 == 0 && a % 2 == 0) {
                    c.set(GROUND + 1, B.SMOOTH_STONE);
                    c.set(GROUND + 2, B.SMOOTH_STONE_SLAB);
                }
            }
        }
        long h = c.hash(GROUND, 9);
        if (Rng.chance(h, 0.002)) c.set(GROUND + 1, B.ANDESITE_SLAB);
    }

    static void drift(Column c) {
        double n = Rng.fbm(c.seed ^ 0xA5D1L, c.x / 7.0, c.z / 7.0);
        if (n > 0.7) c.set(GROUND, B.TUFF);
        else if (n > 0.6) c.set(GROUND, B.GRAVEL);
        if (n > 0.74 && c.isAir(GROUND + 1) && Rng.chance(c.hash(GROUND, 95), 0.25)) c.set(GROUND + 1, B.COBBLE_SLAB);
    }

    static void tankWreck(Column c, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz) {
        if (rx == rz || !major(rx ? cx : cz)) return;
        if (Terrain.segmentDelta(c.seed, cx, cz, rx) != 0) return;
        Zones.Zone zone = Zones.zone(c.seed, cx, cz);
        double p = switch (zone) { case BOMBED -> 0.3; case COMPOUND -> 0.15; case SPACEPORT -> 0.1; default -> 0.025; };
        long h = Rng.hash(c.seed ^ 0x3A1CL, cx, cz, rx ? 1 : 2);
        if (!Rng.chance(h, p)) return;
        int len = rx ? CELL - wz : CELL - wx;
        int ox = rx ? cx * CELL + 8 : cx * CELL + wx + len / 2, oz = rx ? cz * CELL + wz + len / 2 : cz * CELL + 8;
        int rot = (rx ? 0 : 1) + (int) ((h >>> 8) & 1) * 2;
        Voxels w = Models.tank(rot, true);
        if (w.covers(c.x - ox, c.z - oz)) w.apply(c, c.x - ox, c.z - oz, GROUND + 1);
    }

    static void powerLines(Column c, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz, int ground) {
        if (Zones.zone(c.seed, cx, cz) != Zones.Zone.POWER || major(cx) || major(cz)) return;
        int base = GROUND + ground + 1;
        if (rx && rz) {
            Voxels p = Models.PYLON;
            if (p.covers(lx - wx / 2, lz - wz / 2)) p.apply(c, lx - wx / 2, lz - wz / 2, base);
            return;
        }
        int h = Models.PYLON_H;
        if (rx && Math.abs(lx - wx / 2) == 4) c.set(base + h - 2, B.CHAIN_Z);
        if (rz && Math.abs(lz - wz / 2) == 4) c.set(base + h - 5, B.CHAIN_X);
    }

    static void checkpoint(Column c, int lx, int lz, int wx, int wz, int cx, int cz) {
        if (major(cx) || major(cz) || Tier.pillarAt(c.seed, cx, cz)) return;
        long h = Rng.hash(c.seed ^ 0xC4EC7L, cx, cz);
        if (!Rng.chance(h, 0.14)) return;
        int ex = Math.min(lx, wx - 1 - lx), ez = Math.min(lz, wz - 1 - lz);
        int mx = Math.abs(2 * lx - (wx - 1)), mz = Math.abs(2 * lz - (wz - 1));
        boolean gapX = mx <= 1, gapZ = mz <= 1;
        if ((ex == 1 && ez >= 1 && !gapZ) || (ez == 1 && ex >= 1 && !gapX)) {
            c.set(GROUND + 1, B.POLISHED_ANDESITE);
            c.set(GROUND + 2, B.ANDESITE_SLAB);
        } else if ((ex == 0 && !gapZ && ez >= 1) || (ez == 0 && !gapX && ex >= 1)) {
            if (Rng.chance(c.hash(GROUND, 91), 0.7)) c.set(GROUND + 1, ex == 0 ? B.CHAIN_Z : B.CHAIN_X);
        } else if (ex >= 2 && ez >= 2 && Rng.chance(c.hash(GROUND, 92), 0.05)) {
            c.set(GROUND + 1, Rng.chance(c.hash(GROUND, 93), 0.5) ? B.BARREL : B.STUMP);
        }
    }

    public static int[] findRoadCamp(long seed) {
        for (int r = 0; r < 20; r++) for (int cx = -r; cx <= r; cx++) for (int cz = -r; cz <= r; cz++) {
            if (Math.max(Math.abs(cx), Math.abs(cz)) != r || major(cx)) continue;
            long seg = Rng.hash(seed ^ 0x5E6L, cx, cz, 1);
            double u = Rng.unit(seg);
            if (u >= 0.32 && u < 0.46 && !hasPipe(seed, cx, cz, true) && !Tier.present(seed, cx * CELL, cz * CELL)) {
                int start = 10 + Rng.range(seg >>> 7, 0, 32);
                return new int[]{cx * CELL + roadWidth(cx) / 2, cz * CELL + roadWidth(cz) + start + 3};
            }
        }
        return null;
    }

    static boolean hasPipe(long seed, int cx, int cz, boolean xBand) {
        return Rng.chance(Rng.hash(seed ^ 0x919EL, cx, cz, xBand ? 1 : 2), 0.2);
    }

    static BlockState panel(Column c) {
        long h = c.hash(GROUND, 10);
        if (Math.floorMod(c.x, 8) == 0 || Math.floorMod(c.z, 8) == 0) return B.GRAY_CONCRETE;
        double n = Rng.noise(c.seed ^ 0xA5F4L, c.x / 7.0, c.z / 7.0);
        if (n > 0.8) return Rng.chance(h, 0.5) ? B.ANDESITE : B.POLISHED_ANDESITE;
        if (Rng.chance(h, 0.015)) return B.COBBLE;
        return B.LIGHT_GRAY_CONCRETE;
    }

    static final int CRATER_GRID = 22;

    static int craterDepth(long seed, int x, int z, int salt) {
        int gx = Math.floorDiv(x, CRATER_GRID), gz = Math.floorDiv(z, CRATER_GRID);
        int best = 0;
        for (int ax = gx - 1; ax <= gx + 1; ax++) for (int az = gz - 1; az <= gz + 1; az++) {
            long h = Rng.hash(seed ^ 0xC2A7E2L ^ salt, ax, az);
            boolean bombed = Zones.zoneAt(seed, ax * CRATER_GRID + CRATER_GRID / 2, az * CRATER_GRID + CRATER_GRID / 2) == Zones.Zone.BOMBED;
            if (!Rng.chance(h, bombed ? 0.7 : 0.16)) continue;
            int cx = ax * CRATER_GRID + Rng.range(h >>> 5, 3, CRATER_GRID - 4);
            int cz = az * CRATER_GRID + Rng.range(h >>> 13, 3, CRATER_GRID - 4);
            double r = 2.5 + Rng.unit(h >>> 21) * 3 + (bombed ? 2 : 0);
            double d = Math.sqrt((double) (x - cx) * (x - cx) + (double) (z - cz) * (z - cz));
            if (d < r) best = Math.max(best, 1 + (int) ((r - d) * 0.8));
            else if (d < r + 1.3 && best == 0) best = -1;
        }
        return best;
    }

    static void crater(Column c, int y, int depth) {
        if (depth > 0) {
            c.fill(y - depth + 1, y, B.AIR);
            c.set(y - depth, Rng.chance(c.hash(y, 16), 0.5) ? B.GRAVEL : B.COBBLE);
        } else if (depth < 0 && c.isAir(y + 1) && Rng.chance(c.hash(y, 17), 0.45)) {
            c.set(y + 1, Rng.chance(c.hash(y, 18), 0.5) ? B.COBBLE_SLAB : B.GRAVEL);
        }
    }

    static void pipe(Column c, int t, int along, int yc, int r) {
        boolean flange = Math.floorMod(along, 12) == 0;
        double outer = r + (flange ? 0.9 : 0.4);
        if (Math.abs(t) > outer) return;
        for (int y = yc - r - 1; y <= yc + r + 1; y++) {
            double d = Math.sqrt((double) t * t + (double) (y - yc) * (y - yc));
            if (d <= r - 0.6) c.set(y, B.AIR);
            else if (d <= outer) c.set(y, flange ? B.POLISHED_ANDESITE : B.GRAY_CONCRETE);
        }
        if (Math.abs(t) <= 1) c.set(yc - r + 1, B.SMOOTH_STONE_SLAB);
    }

    static void groundPipe(Column c, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz) {
        if (rx == rz) return;
        if (major(rx ? cx : cz)) return;
        if (!hasPipe(c.seed, cx, cz, rx) || Terrain.segmentDelta(c.seed, cx, cz, rx) != 0 || CityPlan.plain(c.seed, c.x, c.z)) return;
        long h = Rng.hash(c.seed ^ 0x919EL, cx, cz, rx ? 1 : 2);
        int a = rx ? lx : lz, w = rx ? wx : wz;
        int along = rx ? c.z : c.x;
        int k = rx ? lz - wz : lx - wx;
        if (Rng.chance(h >>> 9, 0.4)) {
            int gap = 10 + Rng.range(h >>> 17, 0, 30);
            if (k >= gap && k < gap + 6) return;
        }
        pipe(c, a - w / 2, along, GROUND + 4, 3);
    }

    static void viaducts(Column c, boolean rx, boolean rz, int lx, int lz, int cx, int cz, int ground) {
        if (rx && major(cx)) viaduct(c, lx, c.z, VIADUCT_X, cx, true, GROUND + ground);
        if (rz && major(cz)) viaduct(c, lz, c.x, VIADUCT_Z, cz, false, GROUND + ground);
    }

    static void viaduct(Column c, int a, int along, int y, int road, boolean xBand, int base) {
        if (a < 1 || a > 14) return;
        if (base >= y - 5) return;
        int seg = Math.floorDiv(along, 64), k = Math.floorMod(along, 64);
        long hs = Rng.hash(c.seed ^ 0x4177L, road, seg, xBand ? 1 : 2);
        int p = Math.floorMod(along, 32);
        boolean pier = p <= 2;
        boolean ladder = pier && p == 1 && a == 4 && Rng.chance(Rng.hash(c.seed ^ 0x1ADL, road, Math.floorDiv(along, 32)), 0.5);
        boolean gone = false;
        if (Rng.chance(hs, 0.12)) {
            int j = (int) Math.floorMod(Rng.hash(hs, a), 4L);
            gone = k >= 18 + j && k <= 42 - j;
        }
        if (pier && a >= 4 && a <= 11) c.fill(base + 1, y - 4, B.LIGHT_GRAY_CONCRETE);
        if (pier && !gone && a >= 2 && a <= 13) { c.set(y - 3, B.POLISHED_ANDESITE); c.fill(y - 2, y - 1, B.LIGHT_GRAY_CONCRETE); }
        if (!pier && !gone && (a == 2 || a == 13) && c.isAir(y - 1)) c.set(y - 1, B.LIGHT_GRAY_CONCRETE);
        if (ladder) {
            for (int yy = base + 1; yy <= y - 1; yy++) c.set(yy, B.ladder(xBand ? Direction.WEST : Direction.NORTH));
            if (!gone) c.set(y, B.AIR);
            return;
        }
        if (gone) {
            double n = Rng.fbm(hs, a / 3.0, k / 3.0);
            if (n > 0.55) c.set(base + 1, B.COBBLE);
            if (n > 0.7) c.set(base + 2, B.LIGHT_GRAY_CONCRETE);
            return;
        }
        c.fill(y + 1, y + 6, B.AIR);
        c.set(y, B.LIGHT_GRAY_CONCRETE);
        if (Math.floorMod(along, 8) == 0) c.set(y - 1, B.POLISHED_ANDESITE);
        if (a == 1 || a == 14) { c.set(y + 1, B.LIGHT_GRAY_CONCRETE); return; }
        boolean ballast = (a >= 3 && a <= 5) || (a >= 10 && a <= 12);
        if (ballast) c.set(y, B.GRAVEL);
        boolean rail = a == 4 || a == 11;
        if ((a == 7 || a == 8) && Math.floorMod(along, 16) == 0) {
            for (int yy = y + 1; yy <= y + 6; yy++) c.setConnected(yy, B.IRON_BARS);
            return;
        }
        if ((a >= 3 && a <= 12) && Math.floorMod(along, 16) == 0) c.setConnected(y + 6, B.IRON_BARS);
        int track = a <= 7 ? 0 : 1;
        long th = Rng.hash(c.seed ^ 0x7A1EL, road * 3 + track, seg, xBand ? 1 : 2);
        int wk = Math.floorMod(along, 64);
        int start = 8 + Rng.range(th >>> 4, 0, 30);
        int len = 10 + Rng.range(th >>> 12, 0, 12);
        int ca = track == 0 ? 4 : 11;
        if (Rng.chance(th, 0.12) && wk >= start && wk < start + len && Math.abs(a - ca) <= 1) {
            boolean gapBetweenCars = Math.floorMod(wk - start, 11) == 10;
            if (gapBetweenCars) { if (rail) c.set(y + 1, xBand ? B.RAIL_NS : B.RAIL_EW); return; }
            c.set(y + 1, B.DEEPSLATE_TILES);
            c.set(y + 2, Math.abs(a - ca) == 1 ? B.POLISHED_DEEPSLATE : B.AIR);
            c.set(y + 3, B.POLISHED_DEEPSLATE);
            if (Math.abs(a - ca) == 1 && Math.floorMod(wk - start, 11) == 5) c.set(y + 2, B.AIR);
            return;
        }
        if (rail) c.set(y + 1, xBand ? B.RAIL_NS : B.RAIL_EW);
    }
}
