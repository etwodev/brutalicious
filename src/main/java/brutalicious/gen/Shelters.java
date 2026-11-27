package brutalicious.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

import static brutalicious.gen.CityPlan.*;

public final class Shelters {
    private Shelters() {}

    public static final int FLOOR = 19, CEIL = 24;

    public static boolean present(long seed, int cx, int cz) {
        if (Zones.zone(seed, cx, cz) == Zones.Zone.FLOODED) return false;
        return Rng.fbm(seed ^ 0x5E17E7L, cx / 3.0, cz / 3.0) > 0.47;
    }

    static boolean hall(long seed, int cx, int cz) {
        return present(seed, cx, cz) || present(seed, cx, cz - 1) || present(seed, cx - 1, cz);
    }

    static void lining(Column c) {
        for (int y = FLOOR; y <= CEIL; y++) c.set(y, Math.floorMod(y, 5) == 4 ? B.GRAY_CONCRETE : B.POLISHED_ANDESITE);
    }

    static void street(Column c, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz) {
        long seed = c.seed;
        int tX = lx - wx / 2, tZ = lz - wz / 2;
        if (rx && rz) {
            if (!hall(seed, cx, cz)) return;
            int ax = Math.abs(tX), az = Math.abs(tZ);
            if (ax <= 3 && az <= 3) {
                if (ax == 3 && az == 3) { c.fill(FLOOR, CEIL, B.POLISHED_DEEPSLATE); return; }
                open(c, 0, 99, false);
                if (tX == 0 && tZ == 0) c.set(CEIL, B.REDSTONE_LAMP);
                return;
            }
            if (ax <= 2) open(c, tX, c.z, false);
            else if (az <= 2) open(c, tZ, c.x, false);
            else if (ax == 3 || az == 3) lining(c);
            return;
        }
        if (!present(seed, cx, cz)) return;
        int t = rx ? tX : tZ;
        int k = rx ? lz - wz : lx - wx, along = rx ? c.z : c.x;
        if (Math.abs(t) == 3) {
            if (t == 3 && roomDoor(rx, k)) { lining(c); open(c, t, 99, true); }
            else lining(c);
            return;
        }
        if (Math.abs(t) > 3) {
            if (t > 3 && roomDoor(rx, k)) open(c, t, 99, true);
            return;
        }
        boolean bulk = Math.floorMod(along, 16) == 8;
        if (bulk && (Math.abs(t) == 2)) { c.fill(FLOOR, CEIL, B.GRAY_CONCRETE); return; }
        open(c, t, along, false);
        if (bulk) { c.set(CEIL - 1, B.GRAY_CONCRETE); if (t == 1) c.fill(FLOOR + 1, FLOOR + 3, B.IRON_BARS); }
        long h = c.hash(FLOOR, 170);
        if (Math.abs(t) == 2 && Math.floorMod(along, 6) == 3 && !bulk) c.set(FLOOR + 1, B.SMOOTH_STONE_SLAB);
        else if (Rng.chance(h, 0.0015)) Decor.spawner(c, FLOOR + 1, Decor.pick(h >>> 12, "minecraft:zombie", "minecraft:skeleton", "minecraft:spider"));
        else if (Rng.chance(h >>> 20, 0.002)) c.set(FLOOR + 1, B.rot16(B.SKULL, (int) (h >>> 40)));
    }

    static void hatch(Column c, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz) {
        if (!rx || rz || major(cx) || !present(c.seed, cx, cz) || Zones.wallX(c.seed, cx, cz)) return;
        if (lx != wx / 2 - 2 || lz != wz + 20) return;
        if (!Rng.chance(Rng.hash(c.seed ^ 0x5E1A7L, cx, cz), 0.6)) return;
        if (CityGen.tunnelStyle(c.seed, cx, cz, true) != CityGen.Tunnel.SERVICE) return;
        for (int y = FLOOR + 1; y <= CityGen.TUNNEL_FLOOR; y++) c.set(y, B.ladder(Direction.EAST));
    }

    static void open(Column c, int t, int along, boolean low) {
        c.set(FLOOR, B.SMOOTH_STONE);
        int top = low ? FLOOR + 3 : CEIL - 1;
        for (int y = FLOOR + 1; y <= top; y++) c.set(y, B.AIR);
        if (low) { c.set(FLOOR + 4, B.GRAY_CONCRETE); return; }
        if (Math.abs(t) == 2) c.set(CEIL - 1, B.GRAY_CONCRETE);
        if (t == 0 && Math.floorMod(along, 8) == 0) c.set(CEIL, B.REDSTONE_LAMP);
        else c.set(CEIL, B.GRAY_CONCRETE);
    }

    static final int[] ROOMS = {12, 32};

    static boolean roomDoor(boolean xBand, int k) {
        for (int r : ROOMS) if (k == (xBand ? r : r + 4)) return true;
        return false;
    }

    static void plot(Column c, Plot p, int ux, int uz) {
        if (!present(c.seed, p.cx, p.cz)) return;
        if (ux <= 8) for (int r : ROOMS) if (Math.abs(uz - r) <= 6) { room(c, p, ux, uz - r, r, true); return; }
        if (uz <= 8) for (int r : ROOMS) if (Math.abs(ux - (r + 4)) <= 6) { room(c, p, uz, ux - (r + 4), r, false); return; }
    }

    static void room(Column c, Plot p, int d, int w, int r, boolean west) {
        long h = Rng.hash(c.seed ^ 0x5E100L, p.cx, p.cz, r * 2 + (west ? 1 : 0));
        if (d == 0 || d == 8 || Math.abs(w) == 6) {
            lining(c);
            if (d == 0 && w == 0) open(c, 0, 99, true);
            return;
        }
        open(c, 0, 99, true);
        c.set(FLOOR + 4, B.GRAY_CONCRETE);
        if (d == 4 && w == 0) c.set(FLOOR + 4, B.REDSTONE_LAMP);
        int kind = (int) Math.floorMod(h, 5L);
        long ch = c.hash(FLOOR, 171);
        switch (kind) {
            case 0, 1 -> {
                if (Math.abs(w) == 5 && d != 4) { c.set(FLOOR + 1, B.SPRUCE_SLAB); c.set(FLOOR + 3, B.SPRUCE_SLAB_TOP); if (d % 3 == 1) c.fill(FLOOR + 1, FLOOR + 2, B.SPRUCE_FENCE); }
                else if (Math.abs(w) == 4 && Rng.chance(ch, 0.05)) c.set(FLOOR + 1, B.GRAY_CARPET);
            }
            case 2 -> {
                if (Math.abs(w) >= 4 && d >= 2 && d <= 6) {
                    if (Rng.chance(ch, 0.12)) Decor.container(c, FLOOR + 1, B.BARREL, Decor.LOOT_TUNNEL);
                    else c.set(FLOOR + 1, B.BARREL);
                    if (Rng.chance(ch >>> 9, 0.5)) c.set(FLOOR + 2, B.BARREL);
                }
            }
            case 3 -> {
                if (Math.abs(w) == 5 && d <= 6) {
                    Decor.bed(c, FLOOR + 1, true, west ? Direction.EAST : Direction.SOUTH, d % 2 == 0);
                } else if (w == 0 && d == 7) c.set(FLOOR + 1, B.CAULDRON);
                else if (w == 2 && d == 7) c.set(FLOOR + 1, B.BREWING_STAND);
                else if (w == -2 && d == 7 && Rng.chance(ch, 0.4)) Decor.container(c, FLOOR + 1, B.CHEST, Decor.LOOT_HOSPITAL);
            }
            default -> {
                if (d >= 6) Decor.console(c, FLOOR + 1, w + 6, d, h);
                else if (Math.abs(w) <= 1 && d >= 2 && d <= 4) { c.set(FLOOR + 1, B.POLISHED_ANDESITE); c.set(FLOOR + 2, Rng.chance(ch, 0.5) ? B.GRAY_CARPET : B.d(Blocks.BLACK_CARPET)); }
                else if (w == -5 && d == 1 && Rng.chance(ch, 0.5)) Decor.container(c, FLOOR + 1, B.CHEST, Decor.LOOT_MILITARY);
            }
        }
    }
}
