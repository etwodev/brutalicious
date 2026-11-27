package brutalicious.gen;

import brutalicious.gen.building.Building;
import brutalicious.gen.building.Mass;

import static brutalicious.gen.CityPlan.*;

public final class Roof {
    private Roof() {}

    public static final int FLOOR = 60;
    public static final int TOP = Building.floorY(FLOOR);
    public static final int BOTTOM = TOP - 3;
    public static final int BEAM = BOTTOM - 3;

    public static boolean present(long seed, int x, int z) {
        double n = Rng.fbm(seed ^ 0x2007L, x / 760.0, z / 760.0);
        double edge = (Rng.noise(seed ^ 0x2008L, x / 7.0, z / 7.0) - 0.5) * 0.04;
        return n + edge > 0.62;
    }

    static boolean hole(long seed, int x, int z) {
        return Rng.fbm(seed ^ 0x2009L, x / 14.0, z / 14.0) > 0.8;
    }

    public static boolean megaAt(long seed, int cx, int cz) {
        if (major(cx) || major(cz) || Math.floorMod(cx, 3) != 0 || Math.floorMod(cz, 3) != 0) return false;
        return present(seed, cx * CELL + 5, cz * CELL + 5);
    }

    static void fill(Column c, Plot plot, boolean rx, boolean rz, int lx, int lz, int wx, int wz, int cx, int cz, int lift) {
        if (!present(c.seed, c.x, c.z)) return;
        Building b = (rx || rz) ? null : plot.building;
        int ux = lx - wx, uz = lz - wz;
        Mass pillar = (rx && rz) ? plot.pillar : null;
        boolean hole = hole(c.seed, c.x, c.z);
        boolean beam = Math.floorMod(c.x, 8) < 2 || Math.floorMod(c.z, 8) < 2;
        boolean girder = Math.floorMod(c.x, 32) < 3 || Math.floorMod(c.z, 32) < 3;
        boolean placedTop = false;
        for (int y = BEAM - (girder ? 3 : 0); y <= TOP; y++) {
            if (b != null && b.occupied(ux, uz, y - lift)) continue;
            if (pillar != null && pillar.occupied(lx, lz, y - lift)) continue;
            if (!c.isAir(y)) continue;
            if (y >= BOTTOM) {
                if (!hole) { c.set(y, y == TOP ? surface(c) : B.GRAY_CONCRETE); if (y == TOP) placedTop = true; }
            } else if ((beam || girder) && !hole) {
                c.set(y, B.GRAY_CONCRETE);
            }
        }
        if (hole) {
            boolean rim = !hole(c.seed, c.x + 1, c.z) || !hole(c.seed, c.x - 1, c.z) || !hole(c.seed, c.x, c.z + 1) || !hole(c.seed, c.x, c.z - 1);
            if (rim && Rng.chance(c.hash(BOTTOM, 41), 0.35)) {
                int len = 1 + (int) Math.floorMod(c.hash(BOTTOM, 42), 6L);
                for (int y = BOTTOM; y > BOTTOM - len; y--) if (c.isAir(y)) c.setConnected(y, B.IRON_BARS);
            }
            return;
        }
        if (!placedTop || !c.isAir(TOP + 1)) return;
        topside(c);
    }

    static net.minecraft.world.level.block.state.BlockState surface(Column c) {
        if (Math.floorMod(c.x, 12) == 0 || Math.floorMod(c.z, 12) == 0) return B.POLISHED_ANDESITE;
        return Rng.chance(c.hash(TOP, 43), 0.04) ? B.ANDESITE : B.GRAY_CONCRETE;
    }

    static void topside(Column c) {
        int gx = Math.floorDiv(c.x, 24), gz = Math.floorDiv(c.z, 24);
        long h = Rng.hash(c.seed ^ 0x200AL, gx, gz);
        int ox = gx * 24 + 12, oz = gz * 24 + 12;
        int dx = c.x - ox, dz = c.z - oz;
        double u = Rng.unit(h);
        if (u < 0.25) {
            if (Math.floorMod(dx, 3) == 0 && Math.floorMod(dz, 3) == 0 && Math.abs(dx) <= 6 && Math.abs(dz) <= 6) {
                int hgt = 4 + (int) Math.floorMod(Rng.hash(h, dx, dz), 14L);
                for (int y = TOP + 1; y <= TOP + hgt; y++) c.setConnected(y, B.IRON_BARS);
                if (Rng.chance(Rng.hash(h, dx, dz, 1), 0.3)) c.set(TOP + hgt + 1, B.END_ROD);
            }
        } else if (u < 0.37) {
            Voxels d = Models.dish((int) (h >>> 8));
            if (d.covers(dx, dz)) d.apply(c, dx, dz, TOP + 1);
        } else if (u < 0.55) {
            if (Math.abs(dx) <= 3 && Math.abs(dz) <= 2) {
                boolean shell = Math.abs(dx) == 3 || Math.abs(dz) == 2;
                for (int y = TOP + 1; y <= TOP + 3; y++) c.set(y, shell ? (dx == -3 && dz == 0 && y <= TOP + 2 ? B.AIR : (y == TOP + 2 && dz == 2 ? B.TINTED_GLASS : B.LIGHT_GRAY_CONCRETE)) : B.AIR);
                c.set(TOP + 4, B.SMOOTH_STONE);
                if (!shell) Decor.console(c, TOP + 1, dx + 3, dz + 2, h);
            }
        } else if (u < 0.68) {
            if (Math.abs(dx) <= 3 && Math.abs(dz) <= 3) Camp.fill(c, dx, dz, TOP, h);
        }
    }
}
