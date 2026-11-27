package brutalicious.gen.building;

import brutalicious.gen.B;
import brutalicious.gen.Column;
import brutalicious.gen.Decor;
import brutalicious.gen.Models;
import brutalicious.gen.Palette;
import brutalicious.gen.Rng;
import brutalicious.gen.Voxels;

import static brutalicious.gen.building.Building.GROUND;

public final class Spaceport implements Building {
    public enum Kind { LAUNCH_PAD, TANK_FARM, TRACKING, ASSEMBLY }

    final Kind kind;
    final long seed;
    final int px, pz, cx, cz;
    final Palette pal = Palette.BETON;
    final int rot, variant;
    final boolean fallen;
    static final int PAD = GROUND + 4;

    public Spaceport(Kind kind, long seed, int px, int pz) {
        this.kind = kind;
        this.seed = seed;
        this.px = px;
        this.pz = pz;
        this.cx = px / 2;
        this.cz = pz / 2;
        long h = Rng.hash(seed, 51);
        rot = Rng.range(h, 0, 3);
        variant = Rng.range(h >>> 4, 0, 1);
        fallen = kind == Kind.LAUNCH_PAD && Rng.chance(h >>> 8, 0.3);
    }

    @Override public String name() { return kind.name().toLowerCase(); }

    @Override public int floors() { return 0; }

    @Override
    public boolean occupied(int ux, int uz, int y) {
        return switch (kind) {
            case LAUNCH_PAD -> !fallen && Math.abs(ux - cx) <= 13 && Math.abs(uz - cz) <= 13 && y <= PAD + 75;
            case ASSEMBLY -> ux >= 3 && ux <= px - 4 && uz >= 3 && uz <= pz - 4 && y <= GROUND + 72;
            case TRACKING -> Math.abs(ux - cx) <= 11 && Math.abs(uz - cz) <= 11 && y <= GROUND + 30;
            case TANK_FARM -> y <= GROUND + 20;
        };
    }

    @Override
    public void fill(Column c, int ux, int uz) {
        switch (kind) {
            case LAUNCH_PAD -> pad(c, ux, uz);
            case TANK_FARM -> tanks(c, ux, uz);
            case TRACKING -> tracking(c, ux, uz);
            case ASSEMBLY -> assembly(c, ux, uz);
        }
    }

    void pad(Column c, int ux, int uz) {
        int dx = ux - cx, dz = uz - cz;
        boolean ns = (rot & 1) == 0;
        int across = ns ? dx : dz, along = ns ? dz : dx;
        c.set(GROUND, Rng.chance(c.hash(GROUND, 101), 0.2) ? B.POLISHED_DEEPSLATE : B.LIGHT_GRAY_CONCRETE);
        boolean deck = Math.abs(dx) <= 9 && Math.abs(dz) <= 9;
        boolean trench = Math.abs(across) <= 3;
        if (trench && Math.abs(along) <= 22) {
            int floor = GROUND - 8 + (Math.abs(along) <= 2 ? 4 - Math.abs(along) : 0);
            c.fill(floor + 1, GROUND, B.AIR);
            c.set(floor, B.POLISHED_DEEPSLATE);
        }
        if (deck) {
            boolean hole = Math.abs(dx) <= 2 && Math.abs(dz) <= 2;
            for (int y = GROUND + 1; y <= PAD; y++) {
                boolean arch = trench && y < PAD;
                c.set(y, hole || arch ? B.AIR : (y == PAD ? pal.floor : pal.wallAt(c.seed, c.x, y, c.z, 0.2)));
            }
            if (!hole && (Math.abs(dx) == 9 || Math.abs(dz) == 9)) c.setConnected(PAD + 1, B.IRON_BARS);
        }
        if (Math.abs(dx) == 10 && Math.abs(dz) >= 4 && Math.abs(dz) <= 7) {
            int step = Math.abs(dz) - 3;
            c.fill(GROUND + 1, GROUND + step, pal.floor);
        }
        if (fallen) {
            Voxels r = Models.fallenRocket(rot);
            int ox = ux - (cx - 22), oz = uz - cz;
            if (r.covers(ox, oz)) r.apply(c, ox, oz, GROUND + 1);
        } else {
            Voxels r = Models.rocket(variant, rot);
            if (r.covers(dx, dz)) r.apply(c, dx, dz, PAD + 1);
        }
        if (Math.abs(Math.abs(dx) - 18) == 0 && Math.abs(Math.abs(dz) - 18) == 0) for (int y = GROUND + 1; y <= GROUND + 40; y++) c.setConnected(y, B.IRON_BARS);
    }

    void tanks(Column c, int ux, int uz) {
        c.set(GROUND, B.SMOOTH_STONE);
        int[][] spots = {{px / 4 + 1, pz / 4 + 1}, {3 * px / 4 - 2, pz / 4 + 1}, {px / 4 + 1, 3 * pz / 4 - 2}, {3 * px / 4 - 2, 3 * pz / 4 - 2}};
        for (int[] s : spots) {
            int dx = ux - s[0], dz = uz - s[1];
            if (Models.SPHERE_TANK.covers(dx, dz)) Models.SPHERE_TANK.apply(c, dx, dz, GROUND + 1);
            int ring = Math.max(Math.abs(dx), Math.abs(dz));
            if (ring == 10) c.set(GROUND + 1, pal.parapet);
        }
        if (Math.abs(uz - pz / 2) <= 1 && ux > 2 && ux < px - 3) {
            int t = uz - pz / 2;
            if (t == 0) c.set(GROUND + 9, B.BASALT_X);
            else c.set(GROUND + 8, pal.trim);
            if (Math.floorMod(ux, 8) == 0) c.fill(GROUND + 1, GROUND + 7, pal.trim);
        }
        if (Rng.chance(c.hash(GROUND, 102), 0.0015)) Decor.container(c, GROUND + 1, B.BARREL, Decor.LOOT_INDUSTRIAL);
    }

    void tracking(Column c, int ux, int uz) {
        int dx = ux - cx, dz = uz - cz;
        c.set(GROUND, Math.floorMod(ux, 6) == 0 || Math.floorMod(uz, 6) == 0 ? B.GRAY_CONCRETE : B.SMOOTH_STONE);
        Voxels d = Models.dish(rot);
        if (d.covers(dx, dz)) d.apply(c, dx, dz, GROUND + 1);
        int hx = dx + 14, hz = dz;
        if (Math.abs(hz) <= 4 && hx >= -4 && hx <= 4) {
            boolean shell = Math.abs(hz) == 4 || Math.abs(hx) == 4;
            for (int y = GROUND + 1; y <= GROUND + 4; y++) {
                if (y == GROUND + 4) c.set(y, pal.trim);
                else if (shell) c.set(y, (y == GROUND + 2 && hx == 4) ? B.TINTED_GLASS : (hx == -4 && hz == 0 && y <= GROUND + 2) ? B.AIR : pal.wallAt(c.seed, c.x, y, c.z, 0.2));
                else c.set(y, B.AIR);
            }
            if (!shell) Decor.console(c, GROUND + 1, hx, hz, c.hash(GROUND, 103));
        }
    }

    void assembly(Column c, int ux, int uz) {
        int x0 = 3, z0 = 3, x1 = px - 4, z1 = pz - 4, top = GROUND + 70;
        if (ux < x0 || ux > x1 || uz < z0 || uz > z1) return;
        int e = Math.min(Math.min(ux - x0, x1 - ux), Math.min(uz - z0, z1 - uz));
        boolean ns = uz == z0 || uz == z1;
        c.set(GROUND, pal.floor);
        if (e == 0) {
            int a = ns ? ux - x0 : uz - z0, span = ns ? x1 - x0 : z1 - z0;
            boolean door = ns && Math.abs(a - span / 2) <= 6;
            for (int y = GROUND + 1; y <= top; y++) {
                if (door && y < top - 6) { c.set(y, Math.abs(a - span / 2) <= 3 ? B.AIR : pal.trim); continue; }
                boolean rib = Math.floorMod(a, 6) == 0;
                c.set(y, rib ? pal.trim : (Math.floorMod(y, 16) == 0 ? B.TINTED_GLASS : pal.wallAt(c.seed, c.x, y, c.z, 0.25)));
            }
            c.set(top + 1, pal.parapet);
            return;
        }
        c.set(top, pal.trim);
        if (e <= 3) for (int y = GROUND + 16; y < top; y += 16) c.set(y, B.SMOOTH_STONE_SLAB_TOP);
        if (e == 3) for (int y = GROUND + 17; y < top; y += 16) c.setConnected(y, B.IRON_BARS);
        if (ux == x0 + 1 && uz == z0 + 2) for (int y = GROUND + 1; y < top; y++) c.set(y, B.ladder(net.minecraft.core.Direction.SOUTH));
        int dx = ux - (x0 + x1) / 2, dz = uz - (z0 + z1) / 2;
        Voxels r = Models.rocket(1, rot);
        if (r.covers(dx, dz)) {
            r.apply(c, dx, dz, GROUND + 4);
            c.fill(GROUND + 1, GROUND + 3, pal.trim);
        }
        if (Rng.chance(c.hash(GROUND, 104), 0.0015)) Decor.container(c, GROUND + 1, B.BARREL, Decor.LOOT_INDUSTRIAL);
    }
}
