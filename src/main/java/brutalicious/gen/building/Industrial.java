package brutalicious.gen.building;

import brutalicious.gen.B;
import brutalicious.gen.Column;
import brutalicious.gen.Decor;
import brutalicious.gen.Palette;
import brutalicious.gen.Rng;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static brutalicious.gen.building.Building.GROUND;
import static brutalicious.gen.building.Building.floorY;

public final class Industrial implements Building {
    public enum Kind { COOLING_TOWER, FACTORY, SILOS, STACKS }

    final Kind kind;
    final long seed;
    final int px, pz, cx, cz;
    final Palette pal;
    final int H;
    final double rB, rW, y0, cc;
    final int craneX, craneZ;
    final Mass tower;
    final int[] fill = new int[6];

    static final int WALL_TOP = GROUND + 16;
    static final int SILO_TOP = floorY(9);

    public Industrial(Kind kind, long seed, int px, int pz) {
        this.kind = kind;
        this.seed = seed;
        this.px = px;
        this.pz = pz;
        this.cx = px / 2;
        this.cz = pz / 2;
        long h = Rng.hash(seed, 41);
        this.pal = kind == Kind.COOLING_TOWER ? Palette.BETON : Rng.chance(h, 0.5) ? Palette.STONE : Palette.BARBICAN;
        H = Rng.range(h >>> 4, 56, 72);
        rB = Math.min(21, Math.min(px, pz) / 2.0 - 3);
        rW = rB * 0.62;
        y0 = H * 0.72;
        cc = y0 / Math.sqrt((rB / rW) * (rB / rW) - 1);
        craneX = 3 + Rng.range(h >>> 12, 6, px - 12);
        craneZ = cz + Rng.range(h >>> 20, -6, 6);
        for (int i = 0; i < 6; i++) fill[i] = Rng.range(Rng.hash(h, i), 1, 12);
        if (kind == Kind.SILOS) {
            Mass t = new Mass(seed ^ 0x5170L, px, pz, 10, 0);
            t.rectAll(px - 12, cz - 4, px - 4, cz + 4);
            t.pal = pal;
            t.decay = 0.15;
            t.facadeLow = t.facadeHigh = Mass.Facade.SLIT;
            t.layout = Mass.Layout.OPEN;
            t.kind = Decor.RoomKind.DEPOT;
            t.deckDoorFloor = 9;
            t.centreCore();
            tower = t;
        } else if (kind == Kind.STACKS) {
            Mass t = new Mass(seed ^ 0xB011L, px, pz, 3, 1);
            t.rectAll(3, cz + 3, px - 4, pz - 4);
            t.pal = pal;
            t.decay = 0.3;
            t.facadeLow = t.facadeHigh = Mass.Facade.RIBBON;
            t.layout = Mass.Layout.STORAGE;
            t.kind = Decor.RoomKind.DEPOT;
            t.centreCore();
            tower = t;
        } else {
            tower = null;
        }
    }

    @Override public String name() { return kind.name().toLowerCase(); }

    @Override public int floors() { return kind == Kind.SILOS ? 9 : 0; }

    static double dist(int ax, int az, int bx, int bz) {
        return Math.sqrt((double) (ax - bx) * (ax - bx) + (double) (az - bz) * (az - bz));
    }

    @Override
    public boolean occupied(int ux, int uz, int y) {
        int t = y - GROUND;
        return switch (kind) {
            case COOLING_TOWER -> t >= 1 && t <= H && dist(ux, uz, cx, cz) <= coolR(t) + 1;
            case FACTORY -> (inFactory(ux, uz) && y <= WALL_TOP + 7) || chimneyOccupied(ux, uz, y, 3 + 4, 3 + 4, GROUND + 70);
            case SILOS -> y <= SILO_TOP + 4 && (siloAt(ux, uz) != null || gallery(ux, uz) || tower.occupied(ux, uz, y));
            case STACKS -> tower.occupied(ux, uz, y) || stackAt(ux, uz, y);
        };
    }

    @Override
    public void fill(Column c, int ux, int uz) {
        switch (kind) {
            case COOLING_TOWER -> cooling(c, ux, uz);
            case FACTORY -> factory(c, ux, uz);
            case SILOS -> silos(c, ux, uz);
            case STACKS -> stacks(c, ux, uz);
        }
    }

    double coolR(int t) {
        double k = (t - y0) / cc;
        return rW * Math.sqrt(1 + k * k);
    }

    void cooling(Column c, int ux, int uz) {
        double d = dist(ux, uz, cx, cz);
        if (d > rB + 1.5) return;
        int sector = (int) Math.floor((Math.atan2(uz - cz, ux - cx) + Math.PI) / (Math.PI / 12));
        if (d < rB - 3) {
            c.set(GROUND, B.WATER);
            c.set(GROUND - 1, B.SMOOTH_STONE);
        }
        for (int t = 1; t <= H; t++) {
            int y = GROUND + t;
            double r = coolR(t);
            if (Math.abs(d - r) <= 0.85) {
                boolean leg = t > 6 || Math.floorMod(sector + t / 2, 3) == 0;
                c.set(y, leg ? (t >= H - 1 ? pal.trim : pal.wallAt(c.seed, c.x, y, c.z, 0.15)) : B.AIR);
            } else if (d < r - 0.85 && t == 8 && (Math.floorMod(ux + uz, 3) != 0)) {
                c.set(y, pal.slabTop);
            }
        }
    }

    static boolean chimneyOccupied(int ux, int uz, int y, int ccx, int ccz, int top) {
        return y <= top + 1 && dist(ux, uz, ccx, ccz) <= 4.2;
    }

    void chimney(Column c, int ux, int uz, int ccx, int ccz, int top) {
        double d = dist(ux, uz, ccx, ccz);
        if (ux == ccx + 4 && uz == ccz) {
            for (int y = GROUND + 2; y <= top; y++) c.set(y, B.ladder(Direction.EAST));
            return;
        }
        if (d <= 4.2 && d > 3.4) { c.set(GROUND + 1, pal.trim); return; }
        if (d > 3.4) return;
        for (int y = GROUND + 1; y <= top; y++) {
            if (d > 2.2) c.set(y, y > top - 6 ? B.POLISHED_DEEPSLATE : (Math.floorMod(y, 12) == 0 ? pal.trim : pal.wallAt(c.seed, c.x, y, c.z, 0.2)));
            else c.set(y, B.AIR);
        }
        if (d > 2.2 && ux == ccx - 3 && uz == ccz) c.fill(GROUND + 1, GROUND + 2, B.AIR);
    }

    boolean inFactory(int ux, int uz) {
        return ux >= 3 && ux <= px - 4 && uz >= 3 && uz <= pz - 4;
    }

    static final BlockState[] MACHINES = {B.BLAST_FURNACE, B.SMOKER, B.STONECUTTER, B.GRINDSTONE, B.ANVIL, B.FURNACE};

    void factory(Column c, int ux, int uz) {
        int x0 = 3, z0 = 3, x1 = px - 4, z1 = pz - 4;
        int chx = x0 + 4, chz = z0 + 4;
        if (dist(ux, uz, chx, chz) <= 4.2 || (ux == chx + 4 && uz == chz)) {
            if (inFactory(ux, uz)) c.set(GROUND, pal.floor);
            chimney(c, ux, uz, chx, chz, GROUND + 70);
            return;
        }
        if (!inFactory(ux, uz)) return;
        int e = Math.min(Math.min(ux - x0, x1 - ux), Math.min(uz - z0, z1 - uz));
        int xm = Math.floorMod(ux - x0, 8);
        int roof = WALL_TOP + 1 + (xm * 6) / 8;
        c.set(GROUND, pal.floor);
        if (xm == 7) {
            for (int y = WALL_TOP + 1; y <= WALL_TOP + 6; y++) c.set(y, Rng.chance(c.hash(y, 50), 0.5) ? B.AIR : B.GRAY_GLASS);
            c.set(WALL_TOP + 7, pal.trim);
        } else {
            c.set(roof, Rng.chance(c.hash(roof, 51), 0.06) ? B.AIR : pal.trim);
        }
        if (e == 0) {
            boolean ns = uz == z0 || uz == z1;
            int a = ns ? ux - x0 : uz - z0;
            int mid = ns ? (x1 - x0) / 2 : (z1 - z0) / 2;
            for (int y = GROUND + 1; y <= WALL_TOP; y++) {
                boolean door = !ns && Math.abs(a - mid) <= 2 && y <= GROUND + 6;
                boolean band = y >= WALL_TOP - 5 && y <= WALL_TOP - 2 && Math.floorMod(a, 4) != 0;
                c.set(y, door ? B.AIR : band ? (Rng.chance(c.hash(y, 52), 0.6) ? B.AIR : B.TINTED_GLASS) : pal.wallAt(c.seed, c.x, y, c.z, 0.3));
            }
            return;
        }
        int u = ux - x0, v = uz - z0;
        if (u % 8 == 0 && v % 12 == 0) { c.fill(GROUND + 1, WALL_TOP, B.POLISHED_DEEPSLATE); return; }
        if (uz == z0 + 1 || uz == z1 - 1) c.set(WALL_TOP - 3, B.SMOOTH_STONE_SLAB);
        if (ux == craneX || ux == craneX + 1) c.set(WALL_TOP - 2, B.POLISHED_DEEPSLATE);
        if (ux == craneX && uz == craneZ) {
            for (int y = WALL_TOP - 8; y <= WALL_TOP - 3; y++) c.set(y, B.CHAIN);
            c.set(WALL_TOP - 9, B.IRON_BLOCK);
        }
        int row = Math.floorMod(v, 12);
        if (row == 5 || row == 6) {
            int m = u % 8;
            if (m >= 2 && m <= 6) c.set(GROUND + 1, B.BASALT_X);
            else if (m == 1 || m == 7) c.set(GROUND + 1, MACHINES[(int) Math.floorMod(Rng.hash(seed, ux, uz), (long) MACHINES.length)]);
        } else if (Rng.chance(c.hash(GROUND, 53), 0.004)) {
            Decor.container(c, GROUND + 1, B.BARREL, Decor.LOOT_INDUSTRIAL);
        }
    }

    int[] siloAt(int ux, int uz) {
        int z0 = (pz - 22) / 2;
        for (int i = 0; i < 3; i++) for (int j = 0; j < 2; j++) {
            int sx = 8 + 11 * i, sz = z0 + 5 + 11 * j;
            if (dist(ux, uz, sx, sz) <= 5.4) return new int[]{sx, sz, i, j};
        }
        return null;
    }

    boolean gallery(int ux, int uz) {
        return ux >= 3 && ux <= px - 12 && Math.abs(uz - cz) <= 3;
    }

    void silos(Column c, int ux, int uz) {
        tower.fill(c, ux, uz);
        int[] s = siloAt(ux, uz);
        if (s != null) {
            double d = dist(ux, uz, s[0], s[1]);
            c.set(GROUND, pal.floor);
            for (int y = GROUND + 1; y < SILO_TOP; y++) {
                if (d > 4.2) {
                    boolean hatch = s[2] == 0 && ux < s[0] && uz == s[1] && y <= GROUND + 3;
                    c.set(y, hatch ? B.AIR : (Math.floorMod(y, 10) == 0 ? pal.trim : pal.wallAt(c.seed, c.x, y, c.z, 0.2)));
                } else {
                    c.set(y, y <= GROUND + fill[s[2] * 2 + s[3]] ? B.HAY : B.AIR);
                }
            }
            if (!gallery(ux, uz)) c.set(SILO_TOP, pal.trim);
        }
        if (gallery(ux, uz)) {
            boolean overBin = s != null && dist(ux, uz, s[0], s[1]) < 1.5;
            c.set(SILO_TOP, overBin ? B.AIR : pal.floor);
            boolean wall = Math.abs(uz - cz) == 3 || ux == 3;
            for (int y = SILO_TOP + 1; y <= SILO_TOP + 3; y++)
                c.set(y, wall ? (y == SILO_TOP + 2 && Math.floorMod(ux, 3) == 0 ? B.AIR : pal.wallAt(c.seed, c.x, y, c.z, 0.2)) : B.AIR);
            c.set(SILO_TOP + 4, pal.trim);
            if (!wall && Rng.chance(c.hash(SILO_TOP, 54), 0.006)) Decor.container(c, SILO_TOP + 1, B.BARREL, Decor.LOOT_INDUSTRIAL);
        }
    }

    int stackTop(int i) {
        return GROUND + 70 + Rng.range(Rng.hash(seed, 60 + i), 0, 30);
    }

    boolean stackAt(int ux, int uz, int y) {
        for (int i = 0; i < 3; i++) {
            int sx = px / 4 + i * (px / 4);
            if (chimneyOccupied(ux, uz, y, sx, cz - 8, stackTop(i))) return true;
        }
        return false;
    }

    void stacks(Column c, int ux, int uz) {
        tower.fill(c, ux, uz);
        for (int i = 0; i < 3; i++) {
            int sx = px / 4 + i * (px / 4);
            if (dist(ux, uz, sx, cz - 8) <= 4.2 || (ux == sx + 4 && uz == cz - 8)) chimney(c, ux, uz, sx, cz - 8, stackTop(i));
        }
    }
}
