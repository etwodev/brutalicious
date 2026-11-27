package brutalicious.gen.building;

import brutalicious.gen.B;
import brutalicious.gen.Column;
import brutalicious.gen.Decor;
import brutalicious.gen.Palette;
import brutalicious.gen.Rng;
import net.minecraft.core.Direction;

import static brutalicious.gen.building.Building.GROUND;

public final class Hochbunker implements Building {
    record Tower(int x, int z, double r0, int h, boolean cone) {}

    final Tower[] towers;
    final Palette pal;
    final boolean winkel;

    public Hochbunker(long seed, int px, int pz, Palette pal) {
        this.pal = pal;
        long h = Rng.hash(seed, 21);
        winkel = Rng.chance(h, 0.55);
        if (winkel) {
            int n = Rng.range(h >>> 4, 2, 4);
            int[][] spots = {{px / 4 + 1, pz / 4 + 1}, {3 * px / 4 - 1, 3 * pz / 4 - 1}, {3 * px / 4 - 1, pz / 4 + 1}, {px / 4 + 1, 3 * pz / 4 - 1}};
            towers = new Tower[n];
            for (int i = 0; i < n; i++) {
                long t = Rng.hash(h, i);
                towers[i] = new Tower(spots[i][0], spots[i][1], 6 + Rng.unit(t) * 2, Rng.range(t >>> 8, 30, 42), true);
            }
        } else {
            double r = Math.min(14, Math.min(px, pz) / 2.0 - 7);
            towers = new Tower[]{new Tower(px / 2, pz / 2, r, Rng.range(h >>> 8, 26, 36), false)};
        }
    }

    @Override public String name() { return winkel ? "winkel_towers" : "hochbunker"; }

    @Override public int floors() { return 0; }

    static double radius(Tower t, int y) {
        double rel = (y - GROUND) / (double) t.h;
        if (rel < 0 || rel > 1) return -1;
        if (t.cone) return t.r0 * Math.pow(1 - rel, 0.85);
        int capStart = t.h - 8;
        if (y - GROUND < capStart) return t.r0;
        return t.r0 * (1 - (y - GROUND - capStart) / 10.0);
    }

    static double dist(Tower t, int ux, int uz) {
        return Math.sqrt((double) (ux - t.x) * (ux - t.x) + (double) (uz - t.z) * (uz - t.z));
    }

    @Override
    public boolean occupied(int ux, int uz, int y) {
        for (Tower t : towers) if (dist(t, ux, uz) <= radius(t, y) + 0.5) return true;
        return false;
    }

    @Override
    public void fill(Column c, int ux, int uz) {
        for (Tower t : towers) {
            double d = dist(t, ux, uz);
            if (d > t.r0 + 0.5) continue;
            boolean pillar = ux == t.x && uz == t.z, ladder = ux == t.x + 1 && uz == t.z;
            c.set(GROUND, pal.floor);
            double sector = Math.atan2(uz - t.z, ux - t.x);
            int sec = (int) Math.floor((sector + Math.PI) / (Math.PI / 6));
            for (int y = GROUND + 1; y <= GROUND + t.h + 1; y++) {
                double r = radius(t, y);
                if (r < 0 || d > r + 0.5) continue;
                boolean wall = d > r - 2.2 || r < 3;
                int lvl = Math.floorMod(y - GROUND, 4);
                if (wall) {
                    boolean door = ux < t.x && Math.abs(uz - t.z) <= 1 && y <= GROUND + 3;
                    boolean vent = lvl == 2 && Math.floorMod(sec, 3) == 0 && d > r - 1 && y < GROUND + t.h - 6;
                    c.set(y, (door || vent) ? B.AIR : pal.wallAt(c.seed, c.x, y, c.z, 0.15));
                    continue;
                }
                if (pillar) { c.set(y, pal.trim); continue; }
                if (ladder) { c.set(y, B.ladder(Direction.EAST)); continue; }
                if (lvl == 0) { c.set(y, pal.floor); continue; }
                c.set(y, B.AIR);
                if (lvl == 1 && !c.isAir(y - 1)) Decor.floorItem(c, y, Decor.RoomKind.BUNKER, 0.1);
            }
        }
    }
}
