package brutalicious.gen.building;

import brutalicious.gen.B;
import brutalicious.gen.Column;
import brutalicious.gen.Decor;
import brutalicious.gen.Models;
import brutalicious.gen.Palette;
import brutalicious.gen.Rng;
import brutalicious.gen.Voxels;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

import static brutalicious.gen.building.Building.GROUND;

public final class Power implements Building {
    public enum Kind { STATION, SUBSTATION, SOLAR }

    final Kind kind;
    final long seed;
    final int px, pz;
    final Palette pal;
    static final int HALL_TOP = GROUND + 22, BOILER_TOP = GROUND + 44;

    public Power(Kind kind, long seed, int px, int pz) {
        this.kind = kind;
        this.seed = seed;
        this.px = px;
        this.pz = pz;
        this.pal = Rng.chance(Rng.hash(seed, 61), 0.5) ? Palette.BETON : Palette.BARBICAN;
    }

    @Override public String name() { return kind == Kind.STATION ? "power_station" : kind.name().toLowerCase(); }

    @Override
    public boolean occupied(int ux, int uz, int y) {
        return switch (kind) {
            case STATION -> y <= GROUND + 95 && ux >= 2 && ux <= px - 3 && uz >= 2 && uz <= pz - 3;
            case SUBSTATION, SOLAR -> y <= GROUND + 12;
        };
    }

    @Override
    public void fill(Column c, int ux, int uz) {
        switch (kind) {
            case STATION -> station(c, ux, uz);
            case SUBSTATION -> substation(c, ux, uz);
            case SOLAR -> solar(c, ux, uz);
        }
    }

    void station(Column c, int ux, int uz) {
        int x0 = 3, x1 = px - 4;
        int hz0 = 3, hz1 = 22;
        int bz0 = 23, bz1 = 36;
        if (ux < x0 || ux > x1 || uz < hz0) {
            return;
        }
        if (uz > bz1) {
            for (int[] ch : new int[][]{{px / 3, bz1 + 6}, {2 * px / 3, bz1 + 6}}) chimney(c, ux, uz, ch[0], ch[1], GROUND + 90);
            return;
        }
        boolean hall = uz <= hz1;
        int z0 = hall ? hz0 : bz0, z1 = hall ? hz1 : bz1, top = hall ? HALL_TOP : BOILER_TOP;
        int e = Math.min(Math.min(ux - x0, x1 - ux), Math.min(uz - z0, z1 - uz));
        c.set(GROUND, pal.floor);
        if (e == 0) {
            boolean ns = uz == z0 || uz == z1;
            int a = ns ? ux - x0 : uz - z0;
            for (int y = GROUND + 1; y <= top; y++) {
                boolean door = hall && uz == z0 && Math.abs(ux - (x0 + x1) / 2) <= 2 && y <= GROUND + 6;
                boolean band = y >= top - 6 && y <= top - 2 && Math.floorMod(a, 4) != 0;
                boolean pier = Math.floorMod(a, 8) == 0;
                c.set(y, door ? B.AIR : pier ? pal.trim : band ? (Rng.chance(c.hash(y, 110), 0.5) ? B.AIR : B.TINTED_GLASS) : pal.wallAt(c.seed, c.x, y, c.z, 0.25));
            }
            c.set(top + 1, pal.parapet);
            return;
        }
        c.set(top, pal.trim);
        if (hall) {
            int mid = (hz0 + hz1) / 2, u = ux - x0;
            int set = Math.floorDiv(u - 2, 14), su = Math.floorMod(u - 2, 14);
            if (Math.abs(uz - mid) <= 3 && su <= 10 && set >= 0 && x0 + 2 + set * 14 + 10 <= x1 - 1) {
                c.set(GROUND + 1, pal.trim);
                int dy = uz - mid;
                for (int y = GROUND + 2; y <= GROUND + 8; y++) {
                    double d = Math.sqrt(dy * dy + (y - (GROUND + 5)) * (y - (GROUND + 5)));
                    boolean casing = su <= 7 ? d <= 3.4 : (Math.abs(dy) <= 2 && y <= GROUND + 7);
                    if (casing) c.set(y, su <= 7 ? (su % 3 == 0 ? B.POLISHED_DEEPSLATE : B.GRAY_CONCRETE) : B.POLISHED_ANDESITE);
                }
            }
            if (uz == hz0 + 1 || uz == hz1 - 1) c.set(top - 3, B.SMOOTH_STONE_SLAB);
            if (Math.floorMod(ux - x0, 40) == 17 || Math.floorMod(ux - x0, 40) == 18) c.set(top - 2, B.POLISHED_DEEPSLATE);
            if (uz >= hz1 - 4) {
                c.set(GROUND + 8, pal.floor);
                if (uz == hz1 - 4) for (int y = GROUND + 9; y <= GROUND + 11; y++) c.set(y, y == GROUND + 10 ? B.TINTED_GLASS : pal.wall);
                else {
                    Decor.console(c, GROUND + 9, ux - x0, uz - (hz1 - 4), Rng.hash(seed, 0xC1));
                    if (ux == x1 - 2 && uz == hz1 - 1) for (int y = GROUND + 1; y <= GROUND + 8; y++) c.set(y, B.ladder(Direction.WEST));
                }
            }
            if (Rng.chance(c.hash(GROUND, 111), 0.002)) Decor.container(c, GROUND + 1, B.BARREL, Decor.LOOT_INDUSTRIAL);
        } else {
            int bx = Math.floorMod(ux - x0, 10);
            if (bx >= 3 && bx <= 7 && uz >= bz0 + 3 && uz <= bz1 - 3) c.fill(GROUND + 1, top - 6, bx == 5 ? B.POLISHED_DEEPSLATE : B.GRAY_CONCRETE);
            else if (e == 1) for (int y = GROUND + 8; y < top; y += 8) c.set(y, B.SMOOTH_STONE_SLAB_TOP);
            if (ux == x0 + 1 && uz == bz0 + 1) for (int y = GROUND + 1; y < top; y++) c.set(y, B.ladder(Direction.EAST));
        }
    }

    void chimney(Column c, int ux, int uz, int ccx, int ccz, int top) {
        double d = Math.sqrt((ux - ccx) * (ux - ccx) + (uz - ccz) * (uz - ccz));
        if (ux == ccx + 5 && uz == ccz) { for (int y = GROUND + 2; y <= top; y++) c.set(y, B.ladder(Direction.EAST)); return; }
        if (d > 4.4) return;
        c.set(GROUND, pal.floor);
        for (int y = GROUND + 1; y <= top; y++) {
            if (d > 3.2) c.set(y, y > top - 8 ? B.POLISHED_DEEPSLATE : (Math.floorMod(y, 12) == 0 ? pal.trim : pal.wallAt(c.seed, c.x, y, c.z, 0.2)));
            else c.set(y, B.AIR);
        }
    }

    void substation(Column c, int ux, int uz) {
        c.set(GROUND, B.GRAVEL);
        boolean edge = ux == 2 || uz == 2 || ux == px - 3 || uz == pz - 3;
        if (edge) {
            if (!(uz == pz - 3 && Math.abs(ux - px / 2) <= 1)) for (int y = GROUND + 1; y <= GROUND + 3; y++) c.setConnected(y, B.IRON_BARS);
            return;
        }
        int gx = Math.floorMod(ux - 6, 10), gz = Math.floorMod(uz - 6, 10);
        int tx = ux - gx + 2, tz = uz - gz + 2;
        if (ux >= 4 && uz >= 4 && ux <= px - 6 && uz <= pz - 6) {
            Voxels t = Models.TRANSFORMER;
            if (t.covers(ux - tx, uz - tz)) t.apply(c, ux - tx, uz - tz, GROUND + 1);
            if (gz == 6 && Math.floorMod(ux, 6) == 0) for (int y = GROUND + 1; y <= GROUND + 9; y++) c.setConnected(y, B.IRON_BARS);
            if (gz == 6) c.set(GROUND + 10, B.POLISHED_ANDESITE);
            if (Math.abs(gz - 6) == 1 && Math.floorMod(ux, 2) == 0) c.set(GROUND + 9, B.END_ROD);
        }
        if (ux >= px - 12 && ux <= px - 5 && uz >= 4 && uz <= 9) {
            boolean shell = ux == px - 12 || ux == px - 5 || uz == 4 || uz == 9;
            for (int y = GROUND + 1; y <= GROUND + 4; y++) {
                if (y == GROUND + 4) c.set(y, pal.trim);
                else if (shell) c.set(y, ux == px - 12 && uz == 6 && y <= GROUND + 2 ? B.AIR : pal.wallAt(c.seed, c.x, y, c.z, 0.2));
                else c.set(y, B.AIR);
            }
            if (!shell) Decor.console(c, GROUND + 1, ux, uz - 4, Rng.hash(seed, 0xC2));
        }
    }

    void solar(Column c, int ux, int uz) {
        c.set(GROUND, Rng.chance(c.hash(GROUND, 112), 0.1) ? B.GRAVEL : B.SMOOTH_STONE);
        if (ux < 2 || uz < 2 || ux > px - 3 || uz > pz - 3) return;
        int row = Math.floorMod(uz - 2, 5);
        if (row <= 1) {
            if (row == 0 && Math.floorMod(ux, 4) == 0) c.set(GROUND + 1, B.POLISHED_ANDESITE);
            boolean broken = Rng.chance(c.hash(GROUND, 113), 0.08);
            if (!broken) c.set(GROUND + 2, B.stair(Blocks.POLISHED_DEEPSLATE_STAIRS, row == 0 ? Direction.NORTH : Direction.SOUTH));
            if (row == 1 && Math.floorMod(ux, 4) == 0) c.set(GROUND + 1, B.POLISHED_ANDESITE);
        }
    }
}
