package brutalicious.gen.building;

import brutalicious.gen.B;
import brutalicious.gen.Camp;
import brutalicious.gen.Column;
import brutalicious.gen.Decor;
import brutalicious.gen.Models;
import brutalicious.gen.Voxels;
import brutalicious.gen.Palette;
import brutalicious.gen.Rng;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import static brutalicious.gen.building.Building.GROUND;
import static brutalicious.gen.building.Building.W;
import static brutalicious.gen.building.Building.E;

public final class Plaza implements Building {
    public enum Kind { CIVIC, SUNKEN, PARK, PIT, ALLOTMENT, STATION, LIDO, STADIUM, PARADE, CEMETERY, GROUND_ZERO, VACANT, FARM, GREENHOUSE, MARKET }

    public static final int PIT_BASE = -33;
    static final int S = 14;
    static final int P = 4 * (S - 1), Q = P / 2;

    final Kind kind;
    final long seed;
    final int px, pz;
    final Palette pal;
    final int sculptX, sculptZ;
    final int[][] sculpture;
    public int wreck;

    public Plaza(Kind kind, long seed, int px, int pz, Palette pal) {
        this.kind = kind;
        this.seed = seed;
        this.px = px;
        this.pz = pz;
        this.pal = pal;
        this.sculptX = px / 2;
        this.sculptZ = pz / 2;
        int n = Rng.range(Rng.hash(seed, 1), 3, 6);
        sculpture = new int[n][];
        int y = GROUND + 1;
        for (int i = 0; i < n; i++) {
            long h = Rng.hash(seed, 10 + i);
            int w = Rng.range(h, 1, 3), d = Rng.range(h >>> 8, 1, 3), t = Rng.range(h >>> 16, 2, 4);
            int ox = Rng.range(h >>> 24, -2, 2), oz = Rng.range(h >>> 32, -2, 2);
            sculpture[i] = new int[]{sculptX + ox - w, sculptZ + oz - d, sculptX + ox + w, sculptZ + oz + d, y, y + t - 1};
            y += t;
        }
    }

    @Override public String name() { return "plaza_" + kind.name().toLowerCase(); }

    @Override public boolean needsCistern() { return kind == Kind.PIT; }

    @Override
    public int basementReach(int side) {
        return kind == Kind.STATION && (side == W || side == E) ? 4 : -1;
    }

    @Override
    public boolean occupied(int ux, int uz, int y) {
        return kind == Kind.STADIUM && y <= GROUND + 16 && stadiumN(ux, uz) <= 1.56;
    }

    @Override
    public void fill(Column c, int ux, int uz) {
        switch (kind) {
            case CIVIC -> { civic(c, ux, uz); if (Rng.chance(Rng.hash(seed, 0xCA1), 0.4)) Camp.fill(c, ux - 7, uz - 7, GROUND, Rng.hash(seed, 0xCA2)); }
            case ALLOTMENT -> allotment(c, ux, uz);
            case STATION -> station(c, ux, uz);
            case LIDO -> { lido(c, ux, uz); lidoBoards(c, ux, uz); }
            case STADIUM -> stadium(c, ux, uz);
            case PARADE -> parade(c, ux, uz);
            case CEMETERY -> cemetery(c, ux, uz);
            case GROUND_ZERO -> groundZero(c, ux, uz);
            case VACANT -> vacant(c, ux, uz);
            case FARM -> farm(c, ux, uz);
            case GREENHOUSE -> greenhouse(c, ux, uz);
            case MARKET -> market(c, ux, uz);
            case SUNKEN -> sunken(c, ux, uz);
            case PARK -> park(c, ux, uz);
            case PIT -> pit(c, ux, uz);
        }
    }

    void civic(Column c, int ux, int uz) {
        for (int[] b : sculpture) {
            if (ux >= b[0] && ux <= b[2] && uz >= b[1] && uz <= b[3]) c.fill(b[4], b[5], pal.wallAt(c.seed, c.x, b[4], c.z, 0.2));
        }
        int gx = Math.floorMod(ux - 4, 12), gz = Math.floorMod(uz - 4, 12);
        if (gx <= 2 && gz <= 2 && ux > 2 && uz > 2 && ux < px - 3 && uz < pz - 3 && Math.abs(ux - sculptX) > 6) {
            boolean rim = gx != 1 || gz != 1;
            if (rim) c.set(GROUND + 1, pal.trim);
            else {
                c.set(GROUND + 1, B.GRAVEL);
                if (Rng.chance(c.hash(GROUND, 21), 0.3)) c.fill(GROUND + 2, GROUND + 4, B.SPRUCE_LOG);
            }
        }
        if (Math.floorMod(uz, 12) == 9 && Math.floorMod(ux, 6) < 3 && ux > 3 && ux < px - 4 && c.isAir(GROUND + 1))
            c.set(GROUND + 1, B.SMOOTH_STONE_SLAB);
    }

    void sunken(Column c, int ux, int uz) {
        int r = Math.max(Math.abs(ux - px / 2), Math.abs(uz - pz / 2));
        int rings = Math.min(px, pz) / 2 - 3;
        if (r > rings) return;
        int depth = Math.min(8, (rings - r) / 2 + 1);
        int floor = GROUND - depth;
        c.fill(floor + 1, GROUND, B.AIR);
        c.set(floor, (r & 1) == 0 ? pal.trim : pal.floor);
        if (depth == 8 && Rng.chance(c.hash(floor, 22), 0.04)) c.set(floor + 1, B.rot16(B.SKULL, (int) c.hash(floor, 23)));
        if (r == rings && (ux == px / 2 || uz == pz / 2)) c.set(GROUND + 1, B.AIR);
        else if (r == rings) c.setConnected(GROUND + 1, B.IRON_BARS);
    }

    void allotment(Column c, int ux, int uz) {
        if (ux < 2 || uz < 2 || ux > px - 3 || uz > pz - 3) return;
        int ax = Math.floorMod(ux - 2, 10), az = Math.floorMod(uz - 2, 8);
        long ph = Rng.hash(seed ^ 0xA110L, Math.floorDiv(ux - 2, 10), Math.floorDiv(uz - 2, 8));
        if (ax == 9 || az == 7) { c.set(GROUND, B.GRAVEL); return; }
        c.set(GROUND - 1, B.DIRT);
        if (ax == 0 || az == 0) {
            c.set(GROUND, B.COARSE_DIRT);
            if (!(ax == 0 && az == 3)) c.setConnected(GROUND + 1, B.SPRUCE_FENCE);
            return;
        }
        boolean shed = Rng.chance(ph, 0.45) && ax >= 6 && az >= 1 && az <= 3;
        if (shed) {
            c.set(GROUND, B.SMOOTH_STONE);
            boolean wall = ax == 6 || ax == 8 || az == 1 || az == 3;
            boolean door = ax == 6 && az == 2;
            if (wall && !door) c.fill(GROUND + 1, GROUND + 2, B.LIGHT_GRAY_CONCRETE);
            if (!wall && Rng.chance(ph >>> 5, 0.6)) Decor.container(c, GROUND + 1, B.BARREL, Camp.LOOT);
            c.set(GROUND + 3, B.SMOOTH_STONE_SLAB);
            return;
        }
        long h = c.hash(GROUND, 97);
        if (Rng.chance(ph >>> 9, 0.35)) {
            c.set(GROUND, Rng.chance(h, 0.6) ? B.COARSE_DIRT : B.DIRT);
            if (Rng.chance(h >>> 7, 0.3)) c.set(GROUND + 1, Rng.chance(h >>> 13, 0.5) ? B.DEAD_BUSH : B.SHORT_GRASS);
            return;
        }
        if (ax == 1 && az == 1) { c.set(GROUND, B.SMOOTH_STONE); c.set(GROUND + 1, B.WATER_CAULDRON); return; }
        if (ax == 1 && az == 2 && Rng.chance(ph >>> 3, 0.5)) { c.set(GROUND, B.SMOOTH_STONE); c.set(GROUND + 1, B.COMPOSTER); return; }
        if ((ax & 1) == 1) {
            c.set(GROUND, B.FARMLAND);
            if (Rng.chance(h, 0.75)) c.set(GROUND + 1, B.crop(h >>> 5));
        } else {
            c.set(GROUND, B.COARSE_DIRT);
        }
    }

    void park(Column c, int ux, int uz) {
        if (ux < 2 || uz < 2 || ux > px - 3 || uz > pz - 3) return;
        if (Rng.chance(Rng.hash(seed, 0xCA3), 0.45) && Math.abs(ux - (px - 8)) <= 3 && Math.abs(uz - (pz - 8)) <= 3) {
            c.set(GROUND, B.COARSE_DIRT);
            Camp.fill(c, ux - (px - 8), uz - (pz - 8), GROUND, Rng.hash(seed, 0xCA4));
            return;
        }
        double pond = Rng.fbm(seed ^ 0xB0D, ux / 9.0, uz / 9.0);
        c.set(GROUND - 1, B.DIRT);
        c.set(GROUND - 2, B.DIRT);
        if (pond > 0.66) {
            c.set(GROUND, B.WATER);
            c.set(GROUND - 1, pond > 0.72 ? B.WATER : B.DIRT);
            return;
        }
        long h = c.hash(GROUND, 31);
        c.set(GROUND, Rng.chance(h, 0.85) ? B.GRASS : B.MOSS);
        if (Rng.chance(h >>> 8, 0.25)) c.set(GROUND + 1, Rng.chance(h >>> 16, 0.7) ? B.SHORT_GRASS : B.FERN);
        int gx = Math.floorDiv(ux, 9), gz = Math.floorDiv(uz, 9);
        for (int ax = gx - 1; ax <= gx + 1; ax++) {
            for (int az = gz - 1; az <= gz + 1; az++) {
                long t = Rng.hash(seed ^ 0x7EE, ax, az);
                if (!Rng.chance(t, 0.55)) continue;
                int tx = ax * 9 + Rng.range(t >>> 4, 2, 6), tz = az * 9 + Rng.range(t >>> 12, 2, 6);
                if (tx < 4 || tz < 4 || tx > px - 5 || tz > pz - 5) continue;
                if (Rng.fbm(seed ^ 0xB0D, tx / 9.0, tz / 9.0) > 0.6) continue;
                int hgt = Rng.range(t >>> 20, 5, 8);
                boolean dead = Rng.chance(t >>> 28, 0.3);
                int dx = Math.abs(ux - tx), dz = Math.abs(uz - tz);
                if (dx == 0 && dz == 0) {
                    c.fill(GROUND + 1, GROUND + hgt, dead ? B.SPRUCE_LOG : B.OAK_LOG);
                    c.set(GROUND, B.DIRT);
                } else if (!dead) {
                    int top = GROUND + hgt;
                    for (int y = top - 3; y <= top + 1; y++) {
                        int rad = y >= top ? 1 : 2;
                        if (dx <= rad && dz <= rad && !(dx == rad && dz == rad && Rng.chance(Rng.hash(t, ux, y, uz), 0.6))
                                && c.isAir(y)) c.set(y, B.OAK_LEAVES.setValue(BlockStateProperties.PERSISTENT, true));
                    }
                }
            }
        }
    }

    void station(Column c, int ux, int uz) {
        int F = Building.floorY(-3);
        int c0 = pz / 2, hx0 = 4, hx1 = px - 5, hz0 = c0 - 8, hz1 = c0 + 8;
        int dz = uz - c0;
        int sx = px / 2 - 6, sz0 = c0 - 11, sz1 = c0 - 9;
        BlockState tile = B.SMOOTH_STONE;
        if (ux >= sx && ux <= sx + 12 && uz >= sz0 - 1 && uz <= sz1 + 1) {
            int i = ux - sx;
            boolean side = uz == sz0 - 1 || uz == sz1 + 1;
            int step = GROUND - Math.min(i, 11);
            if (side) {
                c.fill(F + 1, GROUND - 1, tile);
                if (i <= 3) c.setConnected(GROUND + 1, B.IRON_BARS);
            } else {
                if (i <= 11) c.set(step, B.stair(net.minecraft.world.level.block.Blocks.STONE_BRICK_STAIRS, net.minecraft.core.Direction.WEST));
                else c.set(step, B.SMOOTH_STONE);
                c.fill(step + 1, i <= 3 ? GROUND + 4 : GROUND - 1, B.AIR);
                if (i > 3) c.set(GROUND, B.SMOOTH_STONE);
            }
        }
        if (ux >= sx - 1 && ux <= sx + 5 && uz >= sz0 - 1 && uz <= sz1 + 1) {
            boolean corner = (ux == sx - 1 || ux == sx + 5) && (uz == sz0 - 1 || uz == sz1 + 1);
            if (corner) c.fill(GROUND + 1, GROUND + 4, pal.trim);
            c.set(GROUND + 5, pal.trim);
        }
        if (ux >= hx0 && ux <= hx1 && uz >= hz0 && uz <= hz1) {
            boolean wall = ux == hx0 || ux == hx1 || uz == hz0 || uz == hz1;
            int ceil = Math.abs(dz) <= 3 ? F + 9 : F + 8;
            c.set(F, B.SMOOTH_STONE);
            for (int y = F + 1; y < ceil; y++) c.set(y, wall ? (y == F + 3 ? B.GRAY_CONCRETE : tile) : B.AIR);
            c.set(ceil, pal.trim);
            boolean track = Math.abs(dz) >= 4 && Math.abs(dz) <= 6;
            if (wall) {
                if ((ux == hx0 || ux == hx1) && track) c.fill(F + 1, F + 4, B.AIR);
                if (uz == hz0 && ux >= sx + 10 && ux <= sx + 12) c.fill(F + 2, F + 4, B.AIR);
                return;
            }
            if (track) {
                if (Math.abs(dz) == 5) c.set(F + 1, B.RAIL_EW);
                long th = Rng.hash(seed, 0x7A1);
                int start = hx0 + 3 + Rng.range(th, 0, 8), len = 20;
                if (dz > 0 && ux >= start && ux < start + len && Math.floorMod(ux - start, 11) != 10) {
                    c.set(F + 1, B.DEEPSLATE_TILES);
                    boolean sideWall = Math.abs(dz) != 5;
                    c.set(F + 2, sideWall && Math.floorMod(ux - start, 11) != 5 ? B.POLISHED_DEEPSLATE : B.AIR);
                    c.set(F + 3, sideWall ? B.TINTED_GLASS : B.AIR);
                    c.set(F + 4, B.POLISHED_DEEPSLATE);
                }
                return;
            }
            c.set(F + 1, Math.abs(dz) == 3 || Math.abs(dz) == 7 ? B.WHITE_CONCRETE_BLOCK : B.SMOOTH_STONE);
            int col = Math.floorMod(ux - hx0, 6);
            if (Math.abs(dz) == 2 && col == 3) c.fill(F + 2, ceil - 1, pal.trim);
            else if (dz == 0 && col == 0) c.set(F + 2, B.SMOOTH_STONE_SLAB);
            else if (dz == 0 && col == 3) c.set(ceil - 1, B.REDSTONE_LAMP);
            else if (Rng.chance(c.hash(F + 2, 70), 0.003)) Decor.container(c, F + 2, B.BARREL, Decor.LOOT_TUNNEL);
            return;
        }
        if ((ux < hx0 || ux > hx1) && Math.abs(dz) >= 4 && Math.abs(dz) <= 6) {
            c.set(F, B.SMOOTH_STONE);
            c.fill(F + 1, F + 4, (ux == 0 || ux == px - 1) ? B.COBBLE : B.AIR);
            if (Math.abs(dz) == 5 && ux > 0 && ux < px - 1) c.set(F + 1, B.RAIL_EW);
            c.set(F + 5, pal.trim);
        }
    }

    void lido(Column c, int ux, int uz) {
        int bx0 = 6, bx1 = px - 9, bz0 = 8, bz1 = pz - 10, c0 = pz / 2;
        if (ux >= bx0 && ux <= bx1 && uz >= bz0 && uz <= bz1) {
            int depth = 1 + (ux - bx0) * 4 / (bx1 - bx0 + 1);
            int floor = GROUND - depth;
            boolean edge = ux == bx0 || ux == bx1 || uz == bz0 || uz == bz1;
            if (edge) { c.set(GROUND, B.WHITE_CONCRETE_BLOCK); return; }
            c.fill(floor + 1, GROUND, B.AIR);
            c.set(floor, Math.floorMod(uz - bz0, 5) == 0 ? B.GRAY_CONCRETE : B.LIGHT_GRAY_CONCRETE);
            if (depth == 4) c.set(floor + 1, B.WATER);
            if (ux == bx1 - 1 && uz == bz0 + 1) for (int y = floor + 1; y <= GROUND; y++) c.set(y, B.ladder(net.minecraft.core.Direction.SOUTH));
            return;
        }
        if (ux >= bx1 + 1 && ux <= bx1 + 3 && Math.abs(uz - c0) <= 1) {
            c.fill(GROUND + 1, GROUND + 15, pal.wallAt(c.seed, c.x, GROUND + 1, c.z, 0.2));
            return;
        }
        if (ux == bx1 + 4 && uz == c0) { for (int y = GROUND + 1; y <= GROUND + 15; y++) c.set(y, B.ladder(net.minecraft.core.Direction.EAST)); return; }
        if (uz > bz1 + 1 && uz < pz - 2 && ux >= bx0 && ux <= bx1) {
            int row = uz - bz1 - 1;
            c.fill(GROUND + 1, GROUND + row, pal.floor);
        }
    }

    void lidoBoards(Column c, int ux, int uz) {
        int bx1 = px - 9, c0 = pz / 2;
        if (ux >= bx1 - 4 && ux <= bx1 && Math.abs(uz - c0) <= 1)
            for (int h : new int[]{5, 10, 15}) if (ux >= bx1 - h / 3 - 1) c.set(GROUND + h, pal.slabBottom);
    }

    double stadiumN(int ux, int uz) {
        double x = (ux - px / 2) / 14.0, z = (uz - pz / 2) / 9.0;
        return Math.sqrt(x * x + z * z);
    }

    void stadium(Column c, int ux, int uz) {
        double n = stadiumN(ux, uz);
        int dz = uz - pz / 2, dx = ux - px / 2;
        if (Math.abs(dx) == 19 && Math.abs(dz) == 13) {
            for (int y = GROUND + 1; y <= GROUND + 28; y++) c.setConnected(y, B.IRON_BARS);
            c.set(GROUND + 29, B.REDSTONE_LAMP);
            return;
        }
        if (n <= 1.0) {
            c.set(GROUND, n > 0.95 || dx == 0 ? B.WHITE_CONCRETE_BLOCK : B.GRAVEL);
            return;
        }
        if (n > 1.56) return;
        int row = (int) ((n - 1.0) * 22);
        int top = GROUND + Math.min(12, row);
        boolean vom = Math.abs(dz) <= 1 && Math.abs(dx) > 10;
        boolean back = n > 1.48;
        c.fill(GROUND + 1, back ? GROUND + 14 : top, pal.floor);
        if (!back) c.set(top + 1, pal.slabBottom);
        if (vom) c.fill(GROUND + 1, GROUND + 3, B.AIR);
        if (dz < 0 && n >= 1.05) c.set(GROUND + 15, pal.trim);
        if (!back && !vom && Rng.chance(c.hash(top, 71), 0.004)) c.set(top + 1, B.rot16(B.SKULL, (int) c.hash(top, 72)));
    }

    void cemetery(Column c, int ux, int uz) {
        if (ux < 1 || uz < 1 || ux > px - 2 || uz > pz - 2) return;
        int gx = Math.floorMod(ux - 1, 12), gz = Math.floorMod(uz - 1, 12);
        long h = c.hash(GROUND, 81);
        if (gx == 0 || gz == 0) { c.set(GROUND, B.GRAVEL); return; }
        if (gx <= 4 && gz <= 4 && gx >= 1 && gz >= 1) {
            long mh = Rng.hash(seed ^ 0x3A05L, Math.floorDiv(ux - 1, 12), Math.floorDiv(uz - 1, 12));
            if (Rng.chance(mh, 0.4)) {
                boolean shell = gx == 1 || gx == 4 || gz == 1 || gz == 4;
                boolean door = gx == 1 && (gz == 2 || gz == 3);
                c.set(GROUND, pal.floor);
                if (shell && !door) c.fill(GROUND + 1, GROUND + 3, pal.wallAt(c.seed, c.x, GROUND + 1, c.z, 0.2));
                else if (!shell && gx == 3 && Rng.chance(mh >>> 9, 0.5)) Decor.container(c, GROUND + 1, B.CHEST, Decor.LOOT_ARCHIVE);
                c.set(GROUND + 4, pal.trim);
                return;
            }
        }
        c.set(GROUND, Rng.chance(h, 0.85) ? B.GRAVEL : B.ANDESITE);
        if (Math.floorMod(ux, 2) == 1 && Math.floorMod(uz, 3) == 1 && !Rng.chance(h >>> 8, 0.12)) {
            c.set(GROUND + 1, Rng.chance(h >>> 12, 0.85) ? B.STONE_BRICKS : B.CRACKED_STONE_BRICKS);
        } else if (Rng.chance(h >>> 20, 0.003)) {
            c.fill(GROUND + 1, GROUND + 5, B.SPRUCE_LOG);
        }
    }

    void farm(Column c, int ux, int uz) {
        if (ux < 2 || uz < 2 || ux > px - 3 || uz > pz - 3) return;
        c.set(GROUND - 1, B.DIRT);
        if (ux == 2 || uz == 2 || ux == px - 3 || uz == pz - 3) {
            c.set(GROUND, B.COARSE_DIRT);
            if (!(uz == pz - 3 && Math.abs(ux - px / 2) <= 1)) c.setConnected(GROUND + 1, B.OAK_FENCE);
            return;
        }
        if (ux >= px - 10 && uz <= 9) {
            boolean shell = ux == px - 10 || ux == px - 4 || uz == 3 || uz == 9;
            c.set(GROUND, B.SMOOTH_STONE);
            for (int y = GROUND + 1; y <= GROUND + 3; y++) c.set(y, shell && !(ux == px - 10 && uz == 6 && y <= GROUND + 2) ? B.LIGHT_GRAY_CONCRETE : B.AIR);
            c.set(GROUND + 4, B.SMOOTH_STONE_SLAB);
            if (!shell && Rng.chance(c.hash(GROUND, 131), 0.12)) Decor.container(c, GROUND + 1, B.BARREL, Decor.LOOT_FARM);
            if (!shell && ux == px - 6 && uz == 5) c.set(GROUND + 3, B.LANTERN_HANGING);
            return;
        }
        int m = Math.floorMod(uz, 9);
        if (m == 4) { c.set(GROUND, B.WATER); return; }
        if (m == 0) { c.set(GROUND, B.COARSE_DIRT); return; }
        c.set(GROUND, B.FARMLAND.setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, 7));
        long h = c.hash(GROUND, 132);
        if (Rng.chance(h, 0.85)) c.set(GROUND + 1, B.crop(Rng.hash(seed, Math.floorDiv(uz, 9), Math.floorDiv(ux, 7)) ^ (h & 0x700)));
        if (ux == px / 2 && Math.floorMod(uz, 18) == 6) {
            c.set(GROUND, B.COARSE_DIRT);
            c.setConnected(GROUND + 1, B.OAK_FENCE);
            c.set(GROUND + 2, B.HAY);
            c.set(GROUND + 3, net.minecraft.world.level.block.Blocks.CARVED_PUMPKIN.defaultBlockState());
        }
    }

    void greenhouse(Column c, int ux, int uz) {
        int x0 = 3, z0 = 3, x1 = px - 4, z1 = pz - 4, top = GROUND + 7;
        if (ux < x0 || ux > x1 || uz < z0 || uz > z1) return;
        boolean edge = ux == x0 || ux == x1 || uz == z0 || uz == z1;
        boolean frame = Math.floorMod(ux - x0, 6) == 0 || Math.floorMod(uz - z0, 6) == 0;
        if (edge) {
            boolean door = uz == z1 && Math.abs(ux - (x0 + x1) / 2) <= 1;
            c.set(GROUND, B.SMOOTH_STONE);
            for (int y = GROUND + 1; y <= top; y++) c.set(y, door && y <= GROUND + 3 ? B.AIR : frame || y == GROUND + 1 ? B.LIGHT_GRAY_CONCRETE : B.GLASS);
            return;
        }
        c.set(top, frame ? B.LIGHT_GRAY_CONCRETE : B.GLASS);
        c.set(GROUND - 1, B.DIRT);
        int m = Math.floorMod(ux - x0, 6);
        if (m == 3) { c.set(GROUND, Math.floorMod(uz, 4) == 0 ? B.WATER : B.SMOOTH_STONE); return; }
        if (m == 0) { c.set(GROUND, B.SMOOTH_STONE); if (Math.floorMod(uz, 6) == 0) c.set(top - 1, B.LANTERN_HANGING); return; }
        c.set(GROUND, B.FARMLAND.setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, 7));
        long h = c.hash(GROUND, 133);
        if (Rng.chance(h, 0.9)) c.set(GROUND + 1, B.crop(Rng.hash(seed, ux / 6, uz / 5) ^ (h & 0x700)));
    }

    void market(Column c, int ux, int uz) {
        if (ux < 2 || uz < 2 || ux > px - 3 || uz > pz - 3) return;
        int sx = Math.floorMod(ux - 3, 9), sz = Math.floorMod(uz - 3, 9);
        long stall = Rng.hash(seed, Math.floorDiv(ux - 3, 9), Math.floorDiv(uz - 3, 9));
        if (sx <= 5 && sz <= 4 && ux < px - 4 && uz < pz - 4 && Rng.chance(stall, 0.75)) {
            boolean post = (sx == 0 || sx == 5) && (sz == 0 || sz == 4);
            if (post) { for (int y = GROUND + 1; y <= GROUND + 3; y++) c.setConnected(y, B.OAK_FENCE); c.set(GROUND + 4, B.LIGHT_GRAY_WOOL); return; }
            c.set(GROUND + 4, Rng.chance(c.hash(GROUND, 134), 0.15) ? B.AIR : B.LIGHT_GRAY_CARPET);
            if (sz == 1) {
                c.set(GROUND + 1, B.POLISHED_ANDESITE);
                long h = c.hash(GROUND, 135);
                if (Rng.chance(h, 0.25)) c.set(GROUND + 2, B.POTTED_FERN);
                else if (Rng.chance(h >>> 8, 0.2)) c.set(GROUND + 2, B.CANDLE);
            } else if (sz == 3 && Rng.chance(c.hash(GROUND, 136), 0.4)) {
                if (Rng.chance(c.hash(GROUND, 137), 0.15)) Decor.container(c, GROUND + 1, B.BARREL, Decor.LOOT_FARM);
                else c.set(GROUND + 1, B.BARREL);
            }
            return;
        }
        if (sx == 7 && sz == 7) {
            for (int y = GROUND + 1; y <= GROUND + 3; y++) c.setConnected(y, B.ANDESITE_WALL);
            c.set(GROUND + 4, B.LANTERN);
        }
    }

    void vacant(Column c, int ux, int uz) {
        if (wreck != 0) {
            if (wreck == 1) {
                for (int i = 0; i < 4; i++) {
                    long h = Rng.hash(seed ^ 0x7A4EL, i);
                    if (i == 3 && Rng.chance(h, 0.5)) break;
                    int ox = 10 + Rng.range(h >>> 4, 0, Math.max(1, px - 20)), oz = 10 + Rng.range(h >>> 20, 0, Math.max(1, pz - 20));
                    Voxels t = Models.tank((int) ((h >>> 40) & 3), true);
                    if (t.covers(ux - ox, uz - oz)) t.apply(c, ux - ox, uz - oz, GROUND + 1);
                }
            } else {
                Voxels w = Models.fallenRocket((int) (seed & 1) * 2);
                int ox = px / 2 - 19, oz = pz / 2;
                if (w.covers(ux - ox, uz - oz)) w.apply(c, ux - ox, uz - oz, GROUND + 1);
            }
            double n = Rng.fbm(seed ^ 0x2B1FL, ux / 4.0, uz / 4.0);
            if (n > 0.6 && c.isAir(GROUND + 1)) c.set(GROUND + 1, Rng.chance(c.hash(GROUND, 89), 0.5) ? B.COBBLE : B.GRAVEL);
            return;
        }
        double u = Rng.unit(Rng.hash(seed, 0x7AC));
        int cx = px / 2, cz = pz / 2;
        if (u < 0.45) return;
        if (u < 0.72) {
            int s = 6 + Rng.range(Rng.hash(seed, 0x7AD), 0, 6);
            if (ux < s || uz < s || ux > px - 1 - s || uz > pz - 1 - s) return;
            c.set(GROUND + 1, Rng.chance(c.hash(GROUND, 86), 0.04) ? B.AIR : pal.floor);
            if (Math.floorMod(ux - s, 6) == 0 && Math.floorMod(uz - s, 6) == 0) {
                long h = c.hash(GROUND, 87);
                int hgt = Rng.range(h, 1, 9);
                c.fill(GROUND + 2, GROUND + 1 + hgt, pal.wallAt(c.seed, c.x, GROUND + 2, c.z, 0.3));
                if (Rng.chance(h >>> 8, 0.6)) for (int y = GROUND + 2 + hgt; y <= GROUND + 3 + hgt + Rng.range(h >>> 16, 0, 2); y++) c.setConnected(y, B.IRON_BARS);
            }
            return;
        }
        if (u < 0.86) {
            int dx = Math.abs(ux - cx), dz = Math.abs(uz - cz);
            int hgt = 8 + (int) Math.floorMod(seed, 12L);
            if (dx > 2 || dz > 2) return;
            boolean shell = dx == 2 || dz == 2;
            for (int y = GROUND + 1; y <= GROUND + hgt; y++) {
                if (shell) c.set(y, (Math.floorMod(y, 3) == 0 && dx + dz == 2) ? B.IRON_BARS : pal.wallAt(c.seed, c.x, y, c.z, 0.2));
                else c.set(y, B.AIR);
            }
            if (!shell) c.fill(GROUND - 12, GROUND, B.AIR);
            c.set(GROUND + hgt + 1, pal.trim);
            return;
        }
        double n = Rng.fbm(seed ^ 0x2B1EL, ux / 5.0, uz / 5.0);
        int hgt = (int) ((n - 0.45) * 8);
        for (int i = 1; i <= hgt; i++) c.set(GROUND + i, Rng.chance(c.hash(GROUND + i, 88), 0.5) ? B.COBBLE : B.GRAVEL);
    }

    void groundZero(Column c, int ux, int uz) {
        double dx = ux - px / 2.0, dz = uz - pz / 2.0, d = Math.sqrt(dx * dx + dz * dz);
        double R = Math.min(px, pz) / 2.0 - 3;
        if (ux == px / 2 + 5 && uz == pz / 2 - 3 || (Math.abs(ux - px / 2 - 5) <= 1 && Math.abs(uz - pz / 2 + 3) <= 1)) {
            c.fill(GROUND - 13, GROUND + 18, pal.wallAt(c.seed, c.x, GROUND, c.z, 0.5));
            return;
        }
        if (d < R) {
            int depth = (int) Math.round(14 * Math.sqrt(1 - (d / R) * (d / R)));
            c.fill(GROUND - depth + 1, GROUND + 4, B.AIR);
            c.set(GROUND - depth, Rng.chance(c.hash(GROUND, 82), 0.6) ? B.GRAVEL : B.COBBLED_DEEPSLATE);
            if (Rng.chance(c.hash(GROUND, 83), 0.03)) c.set(GROUND - depth + 1, B.COBBLE);
        } else if (d < R + 4) {
            int hgt = (int) Math.round(3 * (1 - Math.abs(d - R - 1.5) / 2.5) + Rng.unit(c.hash(GROUND, 84)) * 1.5);
            for (int i = 1; i <= hgt; i++) c.set(GROUND + i, Rng.chance(c.hash(GROUND + i, 85), 0.5) ? B.COBBLE : B.GRAVEL);
        }
    }

    void parade(Column c, int ux, int uz) {
        int cx = px / 2, cz = pz / 2;
        c.set(GROUND, (Math.floorMod(ux, 4) == 0 || Math.floorMod(uz, 4) == 0) ? B.POLISHED_ANDESITE : B.SMOOTH_STONE);
        if (uz >= 3 && uz <= 7 && Math.abs(ux - cx) <= 9) {
            c.fill(GROUND + 1, GROUND + (uz - 2), pal.floor);
            if (uz == 7) c.set(GROUND + 6, pal.parapet);
            return;
        }
        if (uz == pz - 5 && Math.floorMod(ux - cx, 6) == 0 && Math.abs(ux - cx) <= 18) {
            for (int y = GROUND + 1; y <= GROUND + 16; y++) c.setConnected(y, B.IRON_BARS);
            return;
        }
        if (Math.abs(ux - cx) <= 1 && Math.abs(uz - cz) <= 1) c.fill(GROUND + 1, GROUND + 2, pal.trim);
    }

    void pit(Column c, int ux, int uz) {
        int sx0 = px / 2 - S / 2, sz0 = pz / 2 - S / 2;
        int i = ux - sx0, j = uz - sz0;
        boolean inner = i >= 0 && i < S && j >= 0 && j < S;
        boolean lining = !inner && i >= -1 && i <= S && j >= -1 && j <= S;
        civicGround(c, ux, uz, sx0, sz0);
        if (lining) {
            c.fill(-16, GROUND, pal.wallAt(c.seed, c.x, 0, c.z, 0.3));
            for (int y = -15; y < GROUND; y += 7) c.set(y, pal.trim);
            int gap = topGapK();
            int k = nearestRingK(i, j);
            if (Math.abs(k - gap) <= 2 || Math.abs(k - gap - P) <= 2 || Math.abs(k - gap + P) <= 2) {
                c.set(GROUND + 1, B.AIR);
            } else {
                c.setConnected(GROUND + 1, B.IRON_BARS);
            }
            return;
        }
        if (!inner) return;
        c.fill(PIT_BASE + 1, GROUND + 4, B.AIR);
        c.set(PIT_BASE, B.DEEPSLATE_TILES);
        boolean ring = i == 0 || j == 0 || i == S - 1 || j == S - 1;
        if (ring) {
            int k = ringK(i, j);
            int layer = Math.floorMod(k / 2 + 1, Q);
            BlockState slab = (k & 1) == 0 ? pal.slabBottom : pal.slabTop;
            for (int y = PIT_BASE + 1; y <= GROUND; y++) {
                if (Math.floorMod(y - PIT_BASE, Q) == layer) c.set(y, slab);
            }
        } else if (Rng.chance(c.hash(PIT_BASE, 41), 0.05)) {
            c.set(PIT_BASE + 1, B.rot16(B.SKULL, (int) c.hash(PIT_BASE, 42)));
        }
    }

    void civicGround(Column c, int ux, int uz, int sx0, int sz0) {
        if (Rng.chance(c.hash(GROUND, 43), 0.05)) c.set(GROUND, B.COBBLE);
    }

    static int ringK(int i, int j) {
        int m = S - 1;
        if (j == 0) return i;
        if (i == m) return m + j;
        if (j == m) return 2 * m + (m - i);
        return 3 * m + (m - j);
    }

    static int nearestRingK(int i, int j) {
        return ringK(Math.max(0, Math.min(S - 1, i)), Math.max(0, Math.min(S - 1, j)));
    }

    static int topGapK() {
        int m = Math.floorMod(GROUND - PIT_BASE, Q);
        return 2 * Math.floorMod(m - 1, Q);
    }
}
