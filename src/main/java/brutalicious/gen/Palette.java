package brutalicious.gen;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class Palette {
    public final String name;
    public final BlockState wall, wallAlt, stain, trim, floor, slabBottom, slabTop, window, parapet;

    Palette(String name, Block wall, Block wallAlt, Block stain, Block trim, Block floor, Block slab, Block window, Block parapet) {
        this.name = name;
        this.wall = wall.defaultBlockState();
        this.wallAlt = wallAlt.defaultBlockState();
        this.stain = stain.defaultBlockState();
        this.trim = trim.defaultBlockState();
        this.floor = floor.defaultBlockState();
        this.slabBottom = B.slab(slab, false);
        this.slabTop = B.slab(slab, true);
        this.window = window.defaultBlockState();
        this.parapet = parapet.defaultBlockState();
    }

    public static final Palette BETON = new Palette("beton", Blocks.LIGHT_GRAY_CONCRETE, Blocks.SMOOTH_STONE, Blocks.ANDESITE,
            Blocks.GRAY_CONCRETE, Blocks.SMOOTH_STONE, Blocks.SMOOTH_STONE_SLAB, Blocks.GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_CONCRETE);
    public static final Palette BARBICAN = new Palette("barbican", Blocks.POLISHED_ANDESITE, Blocks.ANDESITE, Blocks.GRAY_CONCRETE,
            Blocks.STONE_BRICKS, Blocks.SMOOTH_STONE, Blocks.POLISHED_ANDESITE_SLAB, Blocks.BLACK_STAINED_GLASS, Blocks.POLISHED_ANDESITE);
    public static final Palette RAW = new Palette("raw", Blocks.SMOOTH_STONE, Blocks.STONE, Blocks.ANDESITE,
            Blocks.POLISHED_ANDESITE, Blocks.SMOOTH_STONE, Blocks.SMOOTH_STONE_SLAB, Blocks.TINTED_GLASS, Blocks.SMOOTH_STONE);
    public static final Palette MONOLITH = new Palette("monolith", Blocks.GRAY_CONCRETE, Blocks.ANDESITE, Blocks.POLISHED_DEEPSLATE,
            Blocks.POLISHED_DEEPSLATE, Blocks.DEEPSLATE_TILES, Blocks.DEEPSLATE_TILE_SLAB, Blocks.TINTED_GLASS, Blocks.GRAY_CONCRETE);
    public static final Palette STONE = new Palette("stone", Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.ANDESITE,
            Blocks.SMOOTH_STONE, Blocks.SMOOTH_STONE, Blocks.STONE_BRICK_SLAB, Blocks.GRAY_STAINED_GLASS, Blocks.STONE_BRICKS);
    public static final Palette ALEXANDRA = new Palette("alexandra", Blocks.WHITE_CONCRETE, Blocks.SMOOTH_STONE, Blocks.LIGHT_GRAY_CONCRETE,
            Blocks.SMOOTH_STONE, Blocks.SMOOTH_STONE, Blocks.SMOOTH_STONE_SLAB, Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.WHITE_CONCRETE);

    public static final Palette[] ALL = {BETON, BARBICAN, RAW, MONOLITH, STONE, ALEXANDRA};
    public static final Palette[] GRIM = {MONOLITH, RAW, BARBICAN};

    public BlockState wallAt(long seed, int x, int y, int z, double decay) {
        long col = Rng.hash(seed ^ 0x51A1L, x, z);
        long h = Rng.hash(seed ^ 0x7A11L, x, y, z);
        if ((col & 7) == 0 && (h & 3) != 0) return stain;
        double u = Rng.unit(h);
        if (u < 0.07 + decay * 0.15) return wallAlt;
        if (u > 0.985 - decay * 0.04) return stain;
        return wall;
    }
}
