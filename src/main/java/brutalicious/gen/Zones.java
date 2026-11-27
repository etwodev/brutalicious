package brutalicious.gen;

import brutalicious.gen.building.Building;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static brutalicious.gen.CityGen.GROUND;
import static brutalicious.gen.CityPlan.*;

public final class Zones {
    private Zones() {}

    public enum Zone { CITY, COMPOUND, INDUSTRIAL, FLOODED, BOMBED, NECROPOLIS, SPACEPORT, POWER, SURVIVOR, DARK, ABANDONED }

    public static final int REGION = 12;
    public static final int WALL_TOP = Building.floorY(8);
    public static final int FLOOD = GROUND + 12;
    static final int CURTAIN = CityGen.TUNNEL_FLOOR - 7;

    public static Zone zone(long seed, int cx, int cz) {
        int rx = Math.floorDiv(cx, REGION), rz = Math.floorDiv(cz, REGION);
        if (rx >= -1 && rx <= 0 && rz >= -1 && rz <= 0) return Zone.CITY;
        double u = Rng.unit(Rng.hash(seed ^ 0x20E5L, rx, rz));
        if (u < 0.08) return Zone.COMPOUND;
        if (u < 0.17) return Zone.INDUSTRIAL;
        if (u < 0.23) return Zone.FLOODED;
        if (u < 0.29) return Zone.BOMBED;
        if (u < 0.33) return Zone.NECROPOLIS;
        if (u < 0.38) return Zone.SPACEPORT;
        if (u < 0.44) return Zone.POWER;
        if (u < 0.51) return Zone.DARK;
        if (u < 0.59) return Zone.ABANDONED;
        if (u < 0.60) return Zone.SURVIVOR;
        return Zone.CITY;
    }

    public static Zone zoneAt(long seed, int x, int z) {
        return zone(seed, Math.floorDiv(x, CELL), Math.floorDiv(z, CELL));
    }

    public static boolean centrepiece(long seed, int cx, int cz) {
        Zone z = zone(seed, cx, cz);
        if (z == Zone.CITY) return false;
        int rx = Math.floorDiv(cx, REGION), rz = Math.floorDiv(cz, REGION);
        long h = Rng.hash(seed ^ 0xCE47L, rx, rz);
        return cx == rx * REGION + 3 + (int) Math.floorMod(h, (long) (REGION - 6)) && cz == rz * REGION + 3 + (int) Math.floorMod(h >>> 16, (long) (REGION - 6));
    }

    public static int height(long seed, int cx, int cz) {
        return Terrain.cellH(seed, cx, cz);
    }

    static boolean differ(long seed, int ax, int az, int bx, int bz) {
        return differ(zone(seed, ax, az), zone(seed, bx, bz));
    }

    static boolean differ(Zone a, Zone b) {
        return a != b && (a != Zone.CITY || b != Zone.CITY);
    }

    public static boolean wallX(long seed, int cx, int cz) {
        return Math.floorMod(cx, REGION) == 0 && differ(seed, cx - 1, cz, cx, cz);
    }

    public static boolean wallZ(long seed, int cx, int cz) {
        return Math.floorMod(cz, REGION) == 0 && differ(seed, cx, cz - 1, cx, cz);
    }

    public static boolean throughXAt(long seed, int cx, int cz) { return throughX(seed, cx, cz); }

    public static boolean throughZAt(long seed, int cx, int cz) { return throughZ(seed, cx, cz); }

    static boolean throughX(long seed, int cx, int cz) { return wallX(seed, cx, cz) || wallX(seed, cx, cz - 1); }

    static boolean throughZ(long seed, int cx, int cz) { return wallZ(seed, cx, cz) || wallZ(seed, cx - 1, cz); }

    public static boolean towerAt(long seed, int cx, int cz) {
        if (major(cx) || major(cz)) return false;
        return throughX(seed, cx, cz) || throughZ(seed, cx, cz);
    }

    public static int[] towerDoorFloors(long seed, int cx, int cz) {
        int tower = range(height(seed, cx - 1, cz - 1), height(seed, cx, cz - 1), height(seed, cx - 1, cz), height(seed, cx, cz))[0];
        java.util.TreeSet<Integer> floors = new java.util.TreeSet<>();
        if (wallX(seed, cx, cz)) floors.add(Math.max(height(seed, cx - 1, cz), height(seed, cx, cz)));
        if (wallX(seed, cx, cz - 1)) floors.add(Math.max(height(seed, cx - 1, cz - 1), height(seed, cx, cz - 1)));
        if (wallZ(seed, cx, cz)) floors.add(Math.max(height(seed, cx, cz - 1), height(seed, cx, cz)));
        if (wallZ(seed, cx - 1, cz)) floors.add(Math.max(height(seed, cx - 1, cz - 1), height(seed, cx - 1, cz)));
        if (floors.isEmpty()) floors.add(tower);
        return floors.descendingSet().stream().limit(2).mapToInt(h -> 8 + (h - tower) / Building.FLOOR_H).toArray();
    }

    static int wetSide(Zone low, Zone high) {
        return low == Zone.FLOODED ? -1 : high == Zone.FLOODED ? 1 : 0;
    }

    public static boolean gateAt(long seed, int cx, int cz, boolean xBand) { return gate(seed, cx, cz, xBand); }

    static boolean gate(long seed, int cx, int cz, boolean xBand) {
        return Rng.chance(Rng.hash(seed ^ 0x6A7EL, cx, cz, xBand ? 1 : 2), 0.45);
    }

    static boolean core(int t) { return t >= -3 && t <= 2; }

    static void walls(Column c, Plot plot, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz) {
        long seed = c.seed;
        if (rx && rz) {
            if (plot.watchtower != null) {
                plot.watchtower.fill(c, lx, lz);
                if (throughX(seed, cx, cz) || throughZ(seed, cx, cz)) c.fill(CURTAIN, GROUND - 1, B.STONE);
                return;
            }
            if (throughX(seed, cx, cz)) {
                int skip = Integer.MIN_VALUE;
                wallCell(c, lx, wx, 0, 0, false, true, wetX(seed, cx, cz) != 0, 0, skip);
            }
            if (throughZ(seed, cx, cz)) {
                int skip = Integer.MIN_VALUE;
                wallCell(c, lz, wz, 0, 0, false, false, wetZ(seed, cx, cz) != 0, 0, skip);
            }
            return;
        }
        if (rx && wallX(seed, cx, cz)) {
            int wet = wetSide(zone(seed, cx - 1, cz), zone(seed, cx, cz));
            wallCell(c, lx, wx, lz - wz, CELL - wz, wet == 0 && gate(seed, cx, cz, true), true, wet != 0, -wet, Integer.MIN_VALUE);
        }
        if (rz && wallZ(seed, cx, cz)) {
            int wet = wetSide(zone(seed, cx, cz - 1), zone(seed, cx, cz));
            wallCell(c, lz, wz, lx - wx, CELL - wx, wet == 0 && gate(seed, cx, cz, false), false, wet != 0, -wet, Integer.MIN_VALUE);
        }
    }

    static int wetX(long seed, int cx, int cz) {
        int a = wallX(seed, cx, cz) ? wetSide(zone(seed, cx - 1, cz), zone(seed, cx, cz)) : 0;
        int b = wallX(seed, cx, cz - 1) ? wetSide(zone(seed, cx - 1, cz - 1), zone(seed, cx, cz - 1)) : 0;
        return a != 0 ? a : b;
    }

    static int wetZ(long seed, int cx, int cz) {
        int a = wallZ(seed, cx, cz) ? wetSide(zone(seed, cx, cz - 1), zone(seed, cx, cz)) : 0;
        int b = wallZ(seed, cx - 1, cz) ? wetSide(zone(seed, cx - 1, cz - 1), zone(seed, cx - 1, cz)) : 0;
        return a != 0 ? a : b;
    }

    static void wallCell(Column c, int a, int w, int k, int len, boolean gate, boolean xBand, boolean sealed, int drySide, int viaduct) {
        int t = a - w / 2;
        int along = xBand ? c.z : c.x;
        Palette pal = Palette.MONOLITH;
        int mid = len / 2;
        boolean ladderSpot = sealed && drySide != 0 && len > 0 && k == mid && t == (drySide < 0 ? -4 : 3);
        if (ladderSpot) {
            Direction face = xBand ? (drySide < 0 ? Direction.WEST : Direction.EAST) : (drySide < 0 ? Direction.NORTH : Direction.SOUTH);
            for (int y = GROUND + 1; y <= WALL_TOP + 1; y++) c.set(y, B.ladder(face));
            return;
        }
        boolean wetLadder = sealed && drySide != 0 && len > 0 && k == mid && t == (drySide < 0 ? 3 : -4);
        if (wetLadder) {
            Direction face = xBand ? (drySide < 0 ? Direction.EAST : Direction.WEST) : (drySide < 0 ? Direction.SOUTH : Direction.NORTH);
            c.fill(GROUND + 1, GROUND + 2, pal.trim);
            for (int y = FLOOD + 1; y <= WALL_TOP + 1; y++) c.set(y, B.ladder(face));
            return;
        }
        boolean foot = t == -4 || t == 3;
        if (foot) {
            c.fill(GROUND + 1, GROUND + 2, pal.trim);
            if (Math.floorMod(along, 16) == 0) c.fill(GROUND + 1, WALL_TOP - 6, pal.wallAt(c.seed, c.x, GROUND + 3, c.z, 0.2));
            return;
        }
        if (!core(t)) return;
        if (sealed) c.fill(CURTAIN, GROUND, B.STONE);
        int g0 = mid - 2, g1 = mid + 1;
        boolean opening = gate && k >= g0 && k <= g1;
        for (int y = GROUND + 1; y <= WALL_TOP; y++) {
            if (viaduct != Integer.MIN_VALUE && y >= viaduct - 1 && y <= viaduct + 6) continue;
            if (opening && y <= GROUND + 6) { c.set(y, B.AIR); continue; }
            c.set(y, Math.floorMod(y, 8) == 0 ? pal.trim : pal.wallAt(c.seed, c.x, y, c.z, 0.25));
        }
        if (viaduct != Integer.MIN_VALUE) c.set(viaduct + 7, pal.trim);
        if (opening) {
            c.setConnected(GROUND + 6, B.IRON_BARS);
            if (k == g0 && t == -1) {
                Direction face = xBand ? Direction.SOUTH : Direction.EAST;
                for (int y = GROUND + 1; y <= WALL_TOP; y++) c.set(y, B.ladder(face));
            }
        }
        if (t == -3 || t == 2) {
            boolean ladderGap = sealed && drySide != 0 && len > 0 && k == mid;
            if (ladderGap) c.set(WALL_TOP + 1, B.AIR);
            else if (Math.floorMod(along, 3) != 0) c.fill(WALL_TOP + 1, WALL_TOP + 2, pal.parapet);
            else c.set(WALL_TOP + 1, xBand ? B.CHAIN_Z : B.CHAIN_X);
        }
    }

    static boolean wet(long seed, Column c, Plot plot, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz) {
        return effective(seed, plot, rx, rz, lx, lz, wx, wz, cx, cz) == Zone.FLOODED;
    }

    public static Zone effective(long seed, Plot plot, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz) {
        if (rx && rz && plot.watchtower != null) return null;
        int ecx = cx, ecz = cz;
        if (rx) {
            int t = lx - wx / 2;
            if (core(t) && throughXHere(seed, cx, cz, rz)) return null;
            if (Math.floorMod(cx, REGION) == 0 && t < -3) ecx = cx - 1;
        }
        if (rz) {
            int t = lz - wz / 2;
            if (core(t) && throughZHere(seed, cx, cz, rx)) return null;
            if (Math.floorMod(cz, REGION) == 0 && t < -3) ecz = cz - 1;
        }
        return zone(seed, ecx, ecz);
    }

    public static Zone effectiveAt(long seed, int x, int z) {
        int cx = Math.floorDiv(x, CELL), cz = Math.floorDiv(z, CELL);
        int lx = Math.floorMod(x, CELL), lz = Math.floorMod(z, CELL);
        int wx = roadWidth(cx), wz = roadWidth(cz);
        return effective(seed, plot(seed, cx, cz), lx < wx, lz < wz, lx, lz, wx, wz, cx, cz);
    }

    static boolean throughXHere(long seed, int cx, int cz, boolean intersection) {
        return intersection ? throughX(seed, cx, cz) : wallX(seed, cx, cz);
    }

    static boolean throughZHere(long seed, int cx, int cz, boolean intersection) {
        return intersection ? throughZ(seed, cx, cz) : wallZ(seed, cx, cz);
    }

    static void flood(Column c) {
        for (int y = CURTAIN + 1; y <= FLOOD; y++) {
            BlockState s = c.get(y);
            if (s.isAir() || s.is(net.minecraft.world.level.block.Blocks.COBWEB) || s.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock)
                c.set(y, B.WATER);
        }
    }

    static int[] range(int... hs) {
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
        for (int h : hs) { lo = Math.min(lo, h); hi = Math.max(hi, h); }
        return new int[]{hi, lo, hi};
    }

    public static int[] wallSpec(long seed, Plot plot, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz) {
        if (rx && rz) {
            boolean coreX = throughX(seed, cx, cz) && core(lx - wx / 2);
            boolean coreZ = throughZ(seed, cx, cz) && core(lz - wz / 2);
            if (plot.watchtower != null || coreX || coreZ)
                return range(height(seed, cx - 1, cz - 1), height(seed, cx, cz - 1), height(seed, cx - 1, cz), height(seed, cx, cz));
            return null;
        }
        if (rx && wallX(seed, cx, cz) && core(lx - wx / 2)) return range(height(seed, cx - 1, cz), height(seed, cx, cz));
        if (rz && wallZ(seed, cx, cz) && core(lz - wz / 2)) return range(height(seed, cx, cz - 1), height(seed, cx, cz));
        return null;
    }

    static void afterShift(Column c, int[] sh, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz) {
        long seed = c.seed;
        if (sh[1] < sh[2]) {
            for (int y = GROUND + sh[1] - 2; y <= GROUND + sh[2]; y++) c.set(y, Palette.MONOLITH.wallAt(seed, c.x, y, c.z, 0.25));
        }
        if (rx && !rz && wallX(seed, cx, cz))
            boundary(c, lx - wx / 2, lz - wz, CELL - wz, cx, cz, zone(seed, cx - 1, cz), zone(seed, cx, cz),
                    height(seed, cx - 1, cz), height(seed, cx, cz), true);
        if (rz && !rx && wallZ(seed, cx, cz))
            boundary(c, lz - wz / 2, lx - wx, CELL - wx, cx, cz, zone(seed, cx, cz - 1), zone(seed, cx, cz),
                    height(seed, cx, cz - 1), height(seed, cx, cz), false);
    }

    static void boundary(Column c, int t, int k, int len, int cx, int cz, Zone za, Zone zb, int ha, int hb, boolean xBand) {
        int wet = wetSide(za, zb);
        int mid = len / 2, hi = Math.max(ha, hb);
        Direction up = xBand ? Direction.SOUTH : Direction.EAST;
        if (wet != 0) {
            int dry = -wet;
            int dryT = dry < 0 ? -4 : 3, wetT = dry < 0 ? 3 : -4;
            if (k != mid) return;
            if (t == dryT) {
                Direction face = xBand ? (dry < 0 ? Direction.WEST : Direction.EAST) : (dry < 0 ? Direction.NORTH : Direction.SOUTH);
                int h = dry < 0 ? ha : hb;
                for (int y = GROUND + 1 + h; y <= WALL_TOP + 1 + hi; y++) c.set(y, B.ladder(face));
            } else if (t == wetT) {
                Direction face = xBand ? (dry < 0 ? Direction.EAST : Direction.WEST) : (dry < 0 ? Direction.SOUTH : Direction.NORTH);
                int h = dry < 0 ? hb : ha;
                for (int y = FLOOD + 1 + h; y <= WALL_TOP + 1 + hi; y++) c.set(y, B.ladder(face));
            }
            return;
        }
        if (ha == hb || !gate(c.seed, cx, cz, xBand)) return;
        boolean lowA = ha < hb;
        if (lowA ? (t != -5 && t != -4) : (t != 3 && t != 4)) return;
        int dh = Math.abs(ha - hb), lowG = GROUND + Math.min(ha, hb);
        int g0 = mid - 2, g1 = mid + 1, start = Math.max(0, g0 - dh), span = g0 - start;
        Palette pal = Palette.MONOLITH;
        if (k >= start && k < g0) {
            int i = k - start, y = lowG + 1 + (i * dh) / span;
            c.fill(lowG + 1, y - 1, pal.wallAt(c.seed, c.x, y, c.z, 0.2));
            c.set(y, B.stair(net.minecraft.world.level.block.Blocks.POLISHED_ANDESITE_STAIRS, up));
            c.fill(y + 1, y + 10, B.AIR);
        } else if (k >= g0 && k <= g1) {
            c.fill(lowG + 1, lowG + dh, pal.floor);
            c.fill(lowG + dh + 1, lowG + dh + 6, B.AIR);
        } else if (k == g1 + 1 && t == (lowA ? -5 : 4)) {
            c.fill(lowG + 1, lowG + dh + 1, pal.trim);
        }
    }
}
