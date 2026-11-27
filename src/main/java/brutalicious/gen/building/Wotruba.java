package brutalicious.gen.building;

import brutalicious.gen.B;
import brutalicious.gen.Column;
import brutalicious.gen.Decor;
import brutalicious.gen.Palette;
import brutalicious.gen.Rng;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;

import static brutalicious.gen.building.Building.GROUND;

public final class Wotruba implements Building {
    final long seed;
    final int cx, cz;
    final int[][] boxes;
    final Palette pal;
    static final int HW = 8, HD = 6, HH = 16;

    public Wotruba(long seed, int px, int pz, Palette pal) {
        this.seed = seed;
        this.pal = pal;
        this.cx = px / 2;
        this.cz = pz / 2;
        int n = 46;
        boxes = new int[n][];
        for (int i = 0; i < n; i++) {
            long h = Rng.hash(seed, 100 + i);
            int w = Rng.range(h, 1, 3), d = Rng.range(h >>> 6, 1, 3), t = Rng.range(h >>> 12, 2, 7);
            int bx = cx + Rng.range(h >>> 18, -HW - 1, HW + 1), bz = cz + Rng.range(h >>> 26, -HD - 1, HD + 1);
            int by = GROUND + 1 + Rng.range(h >>> 34, 0, HH + 4);
            boxes[i] = new int[]{bx - w, bz - d, bx + w, bz + d, by, by + t};
        }
    }

    @Override public String name() { return "wotruba_chapel"; }

    @Override public int floors() { return 4; }

    @Override
    public void fill(Column c, int ux, int uz) {
        int dx = ux - cx, dz = uz - cz;
        if ((Math.abs(dx) > HW + 3 || Math.abs(dz) > HD + 3) && ux > 1 && uz > 1 && ux < 2 * cx - 1 && uz < 2 * cz - 1) {
            graveyard(c, ux, uz);
            return;
        }
        boolean env = Math.abs(dx) <= HW && Math.abs(dz) <= HD;
        boolean cavity = Math.abs(dx) <= HW - 2 && Math.abs(dz) <= HD - 2;
        if (env) {
            for (int y = GROUND + 1; y <= GROUND + HH; y++) {
                if (Rng.chance(c.hash(y, 51), 0.12)) continue;
                c.set(y, pal.wallAt(c.seed, c.x, y, c.z, 0.15));
            }
        }
        for (int[] b : boxes) {
            if (ux >= b[0] && ux <= b[2] && uz >= b[1] && uz <= b[3]) {
                for (int y = b[4]; y <= b[5]; y++) c.set(y, (y == b[5]) ? pal.trim : pal.wallAt(c.seed, c.x, y, c.z, 0.15));
            }
        }
        if (cavity) {
            c.fill(GROUND + 1, GROUND + HH - 2, B.AIR);
            c.set(GROUND, B.SMOOTH_STONE);
            interior(c, dx, dz);
        }
        if (dz >= -1 && dz <= 1 && dx < -HW + 3 && dx >= -HW - 4) c.fill(GROUND + 1, GROUND + 3, B.AIR);
    }

    void graveyard(Column c, int ux, int uz) {
        long h = c.hash(GROUND, 56);
        c.set(GROUND, Rng.chance(h, 0.7) ? B.GRAVEL : B.ANDESITE);
        boolean marker = Math.floorMod(ux, 3) == 1 && Math.floorMod(uz, 4) == 1;
        if (marker && !Rng.chance(h >>> 8, 0.15)) {
            c.set(GROUND + 1, Rng.chance(h >>> 12, 0.8) ? B.STONE_BRICKS : B.CRACKED_STONE_BRICKS);
            if (Rng.chance(h >>> 16, 0.06)) c.set(GROUND + 2, B.rot16(B.SKULL, (int) (h >>> 24)));
        } else if (Math.floorMod(uz, 4) == 2 && marker == false && Math.floorMod(ux, 3) == 1 && Rng.chance(h >>> 20, 0.5)) {
            c.set(GROUND, B.GRAVEL);
        } else if (Rng.chance(h >>> 32, 0.004)) {
            c.fill(GROUND + 1, GROUND + 5, B.SPRUCE_LOG);
        }
    }

    void interior(Column c, int dx, int dz) {
        int y = GROUND + 1;
        if (dx == HW - 3 && Math.abs(dz) <= 1) {
            c.set(y, B.CHISELED_STONE_BRICKS);
            c.set(y + 1, B.CANDLE.setValue(CandleBlock.CANDLES, 1 + (int) Math.floorMod(c.hash(y, 52), 4L)));
            return;
        }
        if (dx <= HW - 6 && dx >= -HW + 3 && (dx & 1) == 0 && dz != 0 && Math.abs(dz) <= HD - 3) {
            c.set(y, B.stair(Blocks.SPRUCE_STAIRS, Direction.WEST));
            if (Rng.chance(c.hash(y, 53), 0.18)) c.set(y + 1, B.rot16(B.SKULL, 12 + (int) Math.floorMod(c.hash(y, 54), 3L) - 1));
            return;
        }
        if (dz == 0 && Rng.chance(c.hash(y, 55), 0.15)) c.set(y, B.GRAY_CARPET);
    }
}
