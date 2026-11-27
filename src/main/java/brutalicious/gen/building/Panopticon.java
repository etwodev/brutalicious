package brutalicious.gen.building;

import brutalicious.gen.B;
import brutalicious.gen.Column;
import brutalicious.gen.Decor;
import brutalicious.gen.Palette;
import brutalicious.gen.Rng;
import net.minecraft.core.Direction;

import static brutalicious.gen.building.Building.GROUND;
import static brutalicious.gen.building.Building.floorY;

public final class Panopticon implements Building {
    static final int TIERS = 4;
    final long seed;
    final int cx, cz;
    final double ro, ri;
    final Palette pal = Palette.MONOLITH;
    final int towerTop;

    public Panopticon(long seed, int px, int pz) {
        this.seed = seed;
        cx = px / 2;
        cz = pz / 2;
        ro = Math.min(px, pz) / 2.0 - 2.5;
        ri = ro - 9;
        towerTop = floorY(TIERS + 3);
    }

    @Override public String name() { return "panopticon"; }

    @Override public int floors() { return TIERS; }

    double d(int ux, int uz) {
        return Math.sqrt((double) (ux - cx) * (ux - cx) + (double) (uz - cz) * (uz - cz));
    }

    @Override
    public boolean occupied(int ux, int uz, int y) {
        double d = d(ux, uz);
        return (d <= ro + 0.5 && d >= ri - 3 && y <= floorY(TIERS)) || (d <= 4.5 && y <= towerTop + 1);
    }

    @Override
    public void fill(Column c, int ux, int uz) {
        double d = d(ux, uz);
        double ang = Math.atan2(uz - cz, ux - cx);
        int cell = (int) Math.floor((ang + Math.PI) / (2 * Math.PI) * 36);
        double frac = (ang + Math.PI) / (2 * Math.PI) * 36 - cell;
        boolean entrance = ux < cx && Math.abs(uz - cz) <= 1;
        boolean ladderSpot = uz == cz && ux > cx && Math.abs(d - (ri - 1.5)) < 0.6;

        if (d <= 4.5) { tower(c, ux, uz, d); return; }
        if (d < ri - 3 || d > ro + 0.5) {
            if (d < ri - 3) c.set(GROUND, B.GRAVEL);
            return;
        }
        for (int f = 0; f <= TIERS; f++) {
            int F = floorY(f);
            c.set(F, pal.floor);
            if (f == TIERS) {
                if (d > ro - 0.7) c.set(F + 1, pal.parapet);
                continue;
            }
            for (int r = 1; r <= 3; r++) {
                int y = F + r;
                if (d > ro - 1) {
                    boolean slit = r == 2 && frac > 0.4 && frac < 0.6;
                    c.set(y, slit || (entrance && f == 0) ? B.AIR : pal.wallAt(c.seed, c.x, y, c.z, 0.2));
                } else if (d > ri + 0.5) {
                    boolean partition = frac < 0.12;
                    if (entrance && f == 0) c.set(y, r <= 3 ? B.AIR : pal.wall);
                    else c.set(y, partition ? pal.wall : B.AIR);
                    if (!partition && r == 1 && !(entrance && f == 0)) {
                        long h = c.hash(y, 60);
                        if (d > ro - 2 && frac > 0.4 && frac < 0.7 && Rng.chance(h, 0.6)) c.set(y, B.LIGHT_GRAY_CARPET);
                        else if (d < ri + 2 && frac > 0.7 && Rng.chance(h, 0.25)) c.set(y, B.CAULDRON);
                        else if (Rng.chance(h, 0.004)) Decor.container(c, y, B.BARREL, Decor.LOOT_MILITARY);
                    }
                } else if (d > ri - 0.5) {
                    boolean missing = Rng.chance(Rng.hash(seed, f * 64 + cell), 0.25);
                    if (entrance && f == 0) c.set(y, B.AIR);
                    else if (frac < 0.12) c.set(y, pal.wall);
                    else c.set(y, missing ? B.AIR : B.IRON_BARS);
                    if (!missing && frac >= 0.12) c.setConnected(y, B.IRON_BARS);
                } else {
                    boolean rail = d < ri - 2.4 && f > 0 && r == 1;
                    c.set(y, rail ? B.IRON_BARS : B.AIR);
                    if (rail) c.setConnected(y, B.IRON_BARS);
                    if (ladderSpot) c.set(y, B.ladder(Direction.WEST));
                }
            }
            if (ladderSpot && f > 0) c.set(F, B.ladder(Direction.WEST));
            if (f > 0 && d < ri - 0.5 && d >= ri - 3 && !(ladderSpot)) c.set(F, pal.slabTop);
        }
    }

    void tower(Column c, int ux, int uz, double d) {
        int roomFloor = towerTop - 4;
        for (int y = GROUND + 1; y <= towerTop + 1; y++) {
            boolean wall = d > 2.6;
            if (y == towerTop + 1) { if (d > 3.6) c.set(y, pal.parapet); continue; }
            if (y == towerTop) { c.set(y, pal.trim); continue; }
            if (wall) {
                boolean glazed = y > roomFloor && y < towerTop;
                boolean door = y <= GROUND + 2 && ux > cx && uz == cz;
                c.set(y, door ? B.AIR : glazed ? B.TINTED_GLASS : pal.wallAt(c.seed, c.x, y, c.z, 0.1));
                continue;
            }
            if (ux == cx && uz == cz) { c.set(y, y > roomFloor ? B.AIR : pal.trim); continue; }
            if (ux == cx + 1 && uz == cz) { c.set(y, y <= roomFloor ? B.ladder(Direction.EAST) : B.AIR); continue; }
            c.set(y, y == roomFloor ? pal.floor : B.AIR);
        }
        c.set(GROUND, pal.floor);
    }
}
