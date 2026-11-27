package brutalicious.gen.building;

import brutalicious.gen.B;
import brutalicious.gen.Column;
import brutalicious.gen.Decor;
import brutalicious.gen.Palette;
import brutalicious.gen.Rng;
import net.minecraft.core.Direction;

import static brutalicious.gen.building.Building.GROUND;

public final class Monument implements Building {
    public enum Kind { WATER_TOWER, CONGRESS, SPOMENIK, TV_TOWER }

    final Kind kind;
    final long seed;
    final int px, pz, cx, cz;
    final Palette pal;
    final int h1;
    final double tankR;

    public Monument(long seed, int px, int pz, Palette pal) {
        this(seed, px, pz, pal, null);
    }

    public Monument(long seed, int px, int pz, Palette pal, Kind forced) {
        this.seed = seed;
        this.px = px;
        this.pz = pz;
        this.pal = pal;
        long h = Rng.hash(seed, 31);
        double u = Rng.unit(h);
        kind = forced != null ? forced : u < 0.3 ? Kind.WATER_TOWER : u < 0.52 ? Kind.CONGRESS : u < 0.75 ? Kind.SPOMENIK : Kind.TV_TOWER;
        cx = kind == Kind.CONGRESS ? px / 2 - 4 : px / 2;
        cz = pz / 2;
        h1 = kind == Kind.WATER_TOWER ? Rng.range(h >>> 8, 34, 50) : Rng.range(h >>> 8, 26, 36);
        tankR = 9 + Rng.unit(h >>> 16) * 3;
    }

    @Override public String name() { return kind.name().toLowerCase(); }

    double d(int ux, int uz) {
        return Math.sqrt((double) (ux - cx) * (ux - cx) + (double) (uz - cz) * (uz - cz));
    }

    @Override
    public boolean occupied(int ux, int uz, int y) {
        return switch (kind) {
            case WATER_TOWER -> d(ux, uz) <= towerR(y) + 0.5;
            case CONGRESS -> (d(ux, uz) <= 17.5 && y <= GROUND + 21) || pylon(ux, uz) && y <= GROUND + 46;
            case SPOMENIK -> y <= wingTop(ux) && Math.abs(uz - cz) <= 4;
            case TV_TOWER -> d(ux, uz) <= tvR(y - GROUND) + 0.5;
        };
    }

    @Override
    public void fill(Column c, int ux, int uz) {
        switch (kind) {
            case WATER_TOWER -> waterTower(c, ux, uz);
            case CONGRESS -> congress(c, ux, uz);
            case SPOMENIK -> spomenik(c, ux, uz);
            case TV_TOWER -> tvTower(c, ux, uz);
        }
    }

    double towerR(int y) {
        int t = y - GROUND;
        double rs = 3.5;
        if (t < 1) return -1;
        if (t <= h1) return rs;
        if (t <= h1 + 8) return rs + (t - h1) * (tankR - rs) / 8.0;
        if (t <= h1 + 15) return tankR;
        if (t == h1 + 16) return tankR;
        return -1;
    }

    void waterTower(Column c, int ux, int uz) {
        double d = d(ux, uz);
        if (d > tankR + 0.5) return;
        boolean pillar = ux == cx && uz == cz, ladder = ux == cx + 1 && uz == cz;
        int top = GROUND + h1 + 16;
        for (int y = GROUND + 1; y <= top + 1; y++) {
            double r = towerR(y);
            int t = y - GROUND;
            if (y == top + 1) {
                if (d > tankR - 1 && d <= tankR + 0.5) c.set(y, pal.parapet);
                if (ladder) c.set(y - 1, B.AIR);
                continue;
            }
            if (r < 0 || d > r + 0.5) continue;
            if (y == top) { c.set(y, ladder ? B.AIR : pal.trim); continue; }
            boolean shell = d > r - 1.6;
            if (shell) {
                boolean door = t <= 3 && ux < cx && Math.abs(uz - cz) <= 0;
                boolean slit = t > 4 && t < h1 && Math.floorMod(t, 8) == 4 && Math.abs(ux - cx) + Math.abs(uz - cz) > 2 && (ux == cx || uz == cz);
                c.set(y, door || slit ? B.AIR : pal.wallAt(c.seed, c.x, y, c.z, 0.2));
                continue;
            }
            if (pillar) { c.set(y, pal.trim); continue; }
            if (ladder) { c.set(y, B.ladder(Direction.EAST)); continue; }
            boolean tank = t > h1 + 4 && t <= h1 + 13;
            if (t == h1 + 4 && d > 1.5) { c.set(y, pal.floor); continue; }
            if (tank && d <= 1.5) { c.set(y, pal.wall); continue; }
            if (tank && t <= h1 + 12 && d > 1.5) { c.set(y, B.WATER); continue; }
            c.set(y, B.AIR);
        }
        if (d <= tankR) c.set(GROUND, pal.floor);
    }

    boolean pylon(int ux, int uz) {
        return ux >= cx + 18 && ux <= cx + 22 && Math.abs(uz - cz) <= 3;
    }

    void congress(Column c, int ux, int uz) {
        if (pylon(ux, uz)) {
            boolean face = ux == cx + 18 || ux == cx + 22;
            int top = GROUND + 46;
            for (int y = GROUND + 1; y <= top; y++) {
                boolean star = face && Math.abs(uz - cz) <= 1 && y >= top - 7 && y <= top - 5;
                boolean slit = face && uz == cz && y < top - 9 && Math.floorMod(y, 6) == 0;
                c.set(y, star ? B.TINTED_GLASS : slit ? B.AIR : pal.wallAt(c.seed, c.x, y, c.z, 0.15));
            }
            return;
        }
        double d = d(ux, uz);
        if (d > 17.5) return;
        int plinth = d <= 15.5 ? 3 : d <= 16.5 ? 2 : 1;
        c.fill(GROUND + 1, GROUND + plinth, pal.floor);
        int base = GROUND + 3;
        double R = 13.5;
        int wallTop = base + 14;
        if (d <= R + 0.5) {
            boolean wall = d > R - 1.5;
            boolean door = ux < cx && Math.abs(uz - cz) <= 1;
            for (int y = base + 1; y <= wallTop; y++) {
                if (wall) {
                    boolean band = y >= wallTop - 3 && y <= wallTop - 1 && Math.floorMod((int) Math.round(Math.atan2(uz - cz, ux - cx) * 12 / Math.PI), 2) == 0;
                    c.set(y, (door && y <= base + 3) || band ? (band ? B.TINTED_GLASS : B.AIR) : pal.wallAt(c.seed, c.x, y, c.z, 0.2));
                } else {
                    c.set(y, B.AIR);
                }
            }
            if (!wall) {
                int ring = (int) d;
                if (ring >= 5) {
                    int step = Math.min(6, (ring - 5) / 1);
                    c.fill(base + 1, base + step / 2, pal.floor);
                    if ((step & 1) == 1) c.set(base + step / 2 + 1, pal.slabBottom);
                } else if (ring == 0) {
                    c.set(base + 1, B.CHISELED_STONE_BRICKS);
                }
                if (d <= R - 2 && Math.floorMod(ux - cx, 5) == 0 && Math.floorMod(uz - cz, 5) == 0 && d > 4) c.set(wallTop - 1, B.REDSTONE_LAMP);
            }
        }
        if (d <= 17.0) c.set(wallTop + 1, pal.trim);
        if (d <= 17.0 && d > 16.0) c.set(wallTop + 2, pal.parapet);
        if (d <= 11) c.set(wallTop + 2, pal.wall);
        if (d <= 6) c.set(wallTop + 3, pal.wall);
    }

    int podAt() { return 80 + (int) Math.floorMod(seed, 30L); }

    double tvR(int t) {
        int p = podAt();
        if (t < 1) return -1;
        if (t < p) return 3.5;
        if (t < p + 3) return 3.5 + (t - p) * 2.0;
        if (t < p + 8) return 9.5;
        if (t < p + 11) return 9.5 - (t - p - 7) * 2.0;
        if (t < p + 30) return 2.5;
        if (t < p + 46) return 0.4;
        return -1;
    }

    void tvTower(Column c, int ux, int uz) {
        double d = d(ux, uz);
        int p = podAt();
        boolean pillar = ux == cx && uz == cz, ladder = ux == cx + 1 && uz == cz;
        if (d <= 9.5) c.set(GROUND, pal.floor);
        for (int t = 1; t < p + 46; t++) {
            int y = GROUND + t;
            double r = tvR(t);
            if (r < 0 || d > r + 0.5) continue;
            if (t >= p + 30) { c.setConnected(y, B.IRON_BARS); if (t == p + 45) c.set(y, B.REDSTONE_LAMP); continue; }
            if (t >= p + 11) { c.set(y, pal.wallAt(c.seed, c.x, y, c.z, 0.1)); continue; }
            boolean shell = d > r - 1.6;
            if (t == p + 3 && !shell && !ladder) { c.set(y, pal.floor); continue; }
            if (t == p + 10 && !ladder) { c.set(y, pal.trim); continue; }
            if (shell) {
                boolean door = t <= 3 && ux < cx && uz == cz;
                boolean glazed = t >= p + 4 && t <= p + 6;
                c.set(y, door ? B.AIR : glazed ? B.TINTED_GLASS : pal.wallAt(c.seed, c.x, y, c.z, 0.15));
                continue;
            }
            if (pillar && t < p + 3) { c.set(y, pal.trim); continue; }
            if (ladder && t < p + 3) { c.set(y, B.ladder(Direction.EAST)); continue; }
            c.set(y, B.AIR);
            if (t == p + 4 && d > 3 && Rng.chance(c.hash(y, 80), 0.05)) c.set(y, B.SMOOTH_STONE_SLAB);
        }
    }

    static final int GAP = 4, LEAN = 3;

    int wingLen() { return Math.min(cx - 6, 18); }

    int wingTop(int ux) {
        int dx = Math.abs(ux - cx);
        for (int y = GROUND + h1; y > GROUND; y--) if (inWing(dx, y)) return y;
        return GROUND;
    }

    boolean inWing(int dx, int y) {
        double t = (y - GROUND) / (double) h1;
        if (t <= 0 || t > 1) return false;
        double inner = GAP - LEAN * t * t;
        double outer = GAP + wingLen() * (1 - Math.pow(t, 0.6));
        return dx >= inner && dx <= Math.max(inner + 1, outer);
    }

    void spomenik(Column c, int ux, int uz) {
        int dz = Math.abs(uz - cz), dx = Math.abs(ux - cx);
        if (dz <= 1) c.set(GROUND, B.POLISHED_DEEPSLATE);
        if (dx == 0 && dz == 0) c.set(GROUND + 1, B.CAMPFIRE_OUT);
        for (int y = GROUND + 1; y <= GROUND + h1; y++) {
            if (!inWing(dx, y)) continue;
            double t = (y - GROUND) / (double) h1;
            double half = 2.6 - 1.6 * t + (Math.floorMod(ux, 3) == 0 ? 0.6 : 0);
            if (dz <= half) c.set(y, pal.wallAt(c.seed, c.x, y, c.z, 0.1));
        }
    }
}
