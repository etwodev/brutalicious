package brutalicious.gen;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.SlabType;

public final class B {
    private B() {}

    static BlockState d(Block b) { return b.defaultBlockState(); }

    public static final BlockState AIR = d(Blocks.AIR);
    public static final BlockState WATER = d(Blocks.WATER);
    public static final BlockState BEDROCK = d(Blocks.BEDROCK);
    public static final BlockState STONE = d(Blocks.STONE);
    public static final BlockState DEEPSLATE = d(Blocks.DEEPSLATE);
    public static final BlockState TUFF = d(Blocks.TUFF);
    public static final BlockState ANDESITE = d(Blocks.ANDESITE);
    public static final BlockState GRAVEL = d(Blocks.GRAVEL);
    public static final BlockState COBBLE = d(Blocks.COBBLESTONE);
    public static final BlockState COBBLED_DEEPSLATE = d(Blocks.COBBLED_DEEPSLATE);
    public static final BlockState DEEPSLATE_TILES = d(Blocks.DEEPSLATE_TILES);
    public static final BlockState CRACKED_DEEPSLATE_TILES = d(Blocks.CRACKED_DEEPSLATE_TILES);
    public static final BlockState POLISHED_DEEPSLATE = d(Blocks.POLISHED_DEEPSLATE);
    public static final BlockState DEEPSLATE_BRICKS = d(Blocks.DEEPSLATE_BRICKS);
    public static final BlockState SMOOTH_STONE = d(Blocks.SMOOTH_STONE);
    public static final BlockState STONE_BRICKS = d(Blocks.STONE_BRICKS);
    public static final BlockState CRACKED_STONE_BRICKS = d(Blocks.CRACKED_STONE_BRICKS);
    public static final BlockState MOSSY_STONE_BRICKS = d(Blocks.MOSSY_STONE_BRICKS);
    public static final BlockState POLISHED_ANDESITE = d(Blocks.POLISHED_ANDESITE);
    public static final BlockState LIGHT_GRAY_CONCRETE = d(Blocks.LIGHT_GRAY_CONCRETE);
    public static final BlockState GRAY_CONCRETE = d(Blocks.GRAY_CONCRETE);
    public static final BlockState BLACK_CONCRETE = d(Blocks.BLACK_CONCRETE);
    public static final BlockState WHITE_CONCRETE = d(Blocks.WHITE_CONCRETE);
    public static final BlockState CYAN_TERRACOTTA = d(Blocks.CYAN_TERRACOTTA);
    public static final BlockState LIGHT_GRAY_TERRACOTTA = d(Blocks.LIGHT_GRAY_TERRACOTTA);
    public static final BlockState YELLOW_TERRACOTTA = d(Blocks.YELLOW_TERRACOTTA);
    public static final BlockState COARSE_DIRT = d(Blocks.COARSE_DIRT);
    public static final BlockState DIRT = d(Blocks.DIRT);
    public static final BlockState GRASS = d(Blocks.GRASS_BLOCK);
    public static final BlockState MOSS = d(Blocks.MOSS_BLOCK);
    public static final BlockState MOSS_CARPET = d(Blocks.MOSS_CARPET);
    public static final BlockState DEAD_BUSH = d(Blocks.DEAD_BUSH);
    public static final BlockState SHORT_GRASS = d(Blocks.SHORT_GRASS);
    public static final BlockState FERN = d(Blocks.FERN);
    public static final BlockState COBWEB = d(Blocks.COBWEB);
    public static final BlockState IRON_BARS = d(Blocks.IRON_BARS);
    public static final BlockState CHAIN = d(Blocks.CHAIN);
    public static final BlockState REDSTONE_LAMP = d(Blocks.REDSTONE_LAMP);
    public static final BlockState SHROOMLIGHT = d(Blocks.SHROOMLIGHT);
    public static final BlockState RED_GLASS = d(Blocks.RED_STAINED_GLASS);
    public static final BlockState TINTED_GLASS = d(Blocks.TINTED_GLASS);
    public static final BlockState GRAY_GLASS = d(Blocks.GRAY_STAINED_GLASS);
    public static final BlockState BLACK_GLASS = d(Blocks.BLACK_STAINED_GLASS);
    public static final BlockState LIGHT_GRAY_GLASS = d(Blocks.LIGHT_GRAY_STAINED_GLASS);
    public static final BlockState DARK_OAK_PLANKS = d(Blocks.DARK_OAK_PLANKS);
    public static final BlockState SPRUCE_PLANKS = d(Blocks.SPRUCE_PLANKS);
    public static final BlockState BARREL = d(Blocks.BARREL);
    public static final BlockState CHEST = d(Blocks.CHEST);
    public static final BlockState SPAWNER = d(Blocks.SPAWNER);
    public static final BlockState CAULDRON = d(Blocks.CAULDRON);
    public static final BlockState BOOKSHELF = d(Blocks.BOOKSHELF);
    public static final BlockState CHISELED_BOOKSHELF = d(Blocks.CHISELED_BOOKSHELF);
    public static final BlockState POTTED_DEAD_BUSH = d(Blocks.POTTED_DEAD_BUSH);
    public static final BlockState SKULL = d(Blocks.SKELETON_SKULL);
    public static final BlockState SCULK = d(Blocks.SCULK);
    public static final BlockState SCULK_SENSOR = d(Blocks.SCULK_SENSOR);
    public static final BlockState SCULK_SHRIEKER = d(Blocks.SCULK_SHRIEKER).setValue(BlockStateProperties.CAN_SUMMON, true);
    public static final BlockState RED_CARPET = d(Blocks.RED_CARPET);
    public static final BlockState GRAY_CARPET = d(Blocks.GRAY_CARPET);
    public static final BlockState SOUL_LANTERN_HANGING = d(Blocks.SOUL_LANTERN).setValue(LanternBlock.HANGING, true);
    public static final BlockState SOUL_LANTERN = d(Blocks.SOUL_LANTERN);
    public static final BlockState CANDLE = d(Blocks.CANDLE);
    public static final BlockState CANDLE_LIT = d(Blocks.CANDLE).setValue(BlockStateProperties.LIT, true);
    public static final BlockState CHISELED_STONE_BRICKS = d(Blocks.CHISELED_STONE_BRICKS);
    public static final BlockState OAK_LOG = d(Blocks.OAK_LOG);
    public static final BlockState OAK_LEAVES = d(Blocks.OAK_LEAVES);
    public static final BlockState SPRUCE_LOG = d(Blocks.SPRUCE_LOG);
    public static final BlockState ANDESITE_WALL = d(Blocks.ANDESITE_WALL);
    public static final BlockState IRON_BLOCK = d(Blocks.IRON_BLOCK);
    public static final BlockState RAIL_NS = d(Blocks.RAIL).setValue(BlockStateProperties.RAIL_SHAPE, RailShape.NORTH_SOUTH);
    public static final BlockState RAIL_EW = d(Blocks.RAIL).setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);
    public static final BlockState BASALT_X = d(Blocks.POLISHED_BASALT).setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
    public static final BlockState BASALT_Z = d(Blocks.POLISHED_BASALT).setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
    public static final BlockState MANHOLE = d(Blocks.SPRUCE_TRAPDOOR).setValue(TrapDoorBlock.HALF, Half.TOP);
    public static final BlockState IRON_TRAPDOOR = d(Blocks.IRON_TRAPDOOR).setValue(TrapDoorBlock.HALF, Half.TOP);
    public static final BlockState SMOOTH_STONE_SLAB = slab(Blocks.SMOOTH_STONE_SLAB, false);
    public static final BlockState COBBLE_SLAB = slab(Blocks.COBBLESTONE_SLAB, false);
    public static final BlockState ANDESITE_SLAB = slab(Blocks.ANDESITE_SLAB, false);
    public static final BlockState STALACTITE = d(Blocks.POINTED_DRIPSTONE)
            .setValue(BlockStateProperties.VERTICAL_DIRECTION, Direction.DOWN)
            .setValue(BlockStateProperties.DRIPSTONE_THICKNESS, net.minecraft.world.level.block.state.properties.DripstoneThickness.TIP);
    public static final BlockState COAL_ORE = d(Blocks.COAL_ORE), IRON_ORE = d(Blocks.IRON_ORE), COPPER_ORE = d(Blocks.COPPER_ORE),
            GOLD_ORE = d(Blocks.GOLD_ORE), LAPIS_ORE = d(Blocks.LAPIS_ORE), REDSTONE_ORE = d(Blocks.REDSTONE_ORE),
            DS_IRON_ORE = d(Blocks.DEEPSLATE_IRON_ORE), DS_GOLD_ORE = d(Blocks.DEEPSLATE_GOLD_ORE), DS_REDSTONE_ORE = d(Blocks.DEEPSLATE_REDSTONE_ORE),
            DS_LAPIS_ORE = d(Blocks.DEEPSLATE_LAPIS_ORE), DS_DIAMOND_ORE = d(Blocks.DEEPSLATE_DIAMOND_ORE), DS_COAL_ORE = d(Blocks.DEEPSLATE_COAL_ORE);

    public static BlockState slab(Block b, boolean top) {
        return b.defaultBlockState().setValue(SlabBlock.TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
    }

    public static final BlockState ICE = d(Blocks.ICE);
    public static final BlockState SMOOTH_STONE_SLAB_TOP = slab(Blocks.SMOOTH_STONE_SLAB, true);
    public static final BlockState END_ROD = d(Blocks.END_ROD), LEVER = d(Blocks.LEVER), JUKEBOX = d(Blocks.JUKEBOX),
            NOTE_BLOCK = d(Blocks.NOTE_BLOCK), LANTERN = d(Blocks.LANTERN), LANTERN_HANGING = d(Blocks.LANTERN).setValue(LanternBlock.HANGING, true),
            GLASS = d(Blocks.GLASS), STONE_BUTTON = d(Blocks.STONE_BUTTON), DAYLIGHT_DETECTOR = d(Blocks.DAYLIGHT_DETECTOR),
            BLACK_GLASS_PANE = d(Blocks.BLACK_STAINED_GLASS_PANE), OAK_FENCE = d(Blocks.OAK_FENCE), WHITE_CARPET = d(Blocks.WHITE_CARPET),
            SCULK_CATALYST = d(Blocks.SCULK_CATALYST), SCULK_VEIN = d(Blocks.SCULK_VEIN);

    public static BlockState facing(Block b, Direction d) {
        return b.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, d);
    }
    public static final BlockState RED_TERRACOTTA = d(Blocks.RED_TERRACOTTA), WHITE_TERRACOTTA = d(Blocks.WHITE_TERRACOTTA),
            LIGHT_BLUE_TERRACOTTA = d(Blocks.LIGHT_BLUE_TERRACOTTA), GRAY_TERRACOTTA = d(Blocks.GRAY_TERRACOTTA);
    public static final BlockState BREWING_STAND = d(Blocks.BREWING_STAND), ANVIL = d(Blocks.ANVIL), GRINDSTONE = d(Blocks.GRINDSTONE),
            STONECUTTER = d(Blocks.STONECUTTER), BLAST_FURNACE = d(Blocks.BLAST_FURNACE), SMOKER = d(Blocks.SMOKER),
            POLISHED_BLACKSTONE = d(Blocks.POLISHED_BLACKSTONE), WHITE_CONCRETE_BLOCK = d(Blocks.WHITE_CONCRETE), CALCITE = d(Blocks.CALCITE),
            STONE_BRICK_STAIRS = d(Blocks.STONE_BRICK_STAIRS), DEEPSLATE_TILE_STAIRS = d(Blocks.DEEPSLATE_TILE_STAIRS);
    public static final BlockState CAMPFIRE_OUT = d(Blocks.CAMPFIRE).setValue(BlockStateProperties.LIT, false);
    public static final BlockState STUMP = d(Blocks.STRIPPED_SPRUCE_LOG);
    public static final BlockState GRAY_WOOL = d(Blocks.GRAY_WOOL);
    public static final BlockState LIGHT_GRAY_WOOL = d(Blocks.LIGHT_GRAY_WOOL), GREEN_WOOL = d(Blocks.GREEN_WOOL), BROWN_WOOL = d(Blocks.BROWN_WOOL);
    public static final BlockState BROWN_CARPET = d(Blocks.BROWN_CARPET), LIGHT_GRAY_CARPET = d(Blocks.LIGHT_GRAY_CARPET), GREEN_CARPET = d(Blocks.GREEN_CARPET);
    public static final BlockState PACKED_MUD = d(Blocks.PACKED_MUD);
    public static final BlockState MUD_BRICK_SLAB = slab(Blocks.MUD_BRICK_SLAB, false);
    public static final BlockState CHAIN_X = d(Blocks.CHAIN).setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
    public static final BlockState CHAIN_Z = d(Blocks.CHAIN).setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
    public static final BlockState WATER_CAULDRON = d(Blocks.WATER_CAULDRON).setValue(BlockStateProperties.LEVEL_CAULDRON, 3);
    public static final BlockState FARMLAND = d(Blocks.FARMLAND);
    public static final BlockState CRAFTING_TABLE = d(Blocks.CRAFTING_TABLE);
    public static final BlockState FURNACE = d(Blocks.FURNACE);
    public static final BlockState COMPOSTER = d(Blocks.COMPOSTER);
    public static final BlockState SPRUCE_FENCE = d(Blocks.SPRUCE_FENCE);
    public static final BlockState HAY = d(Blocks.HAY_BLOCK);
    public static final BlockState POTTED_FERN = d(Blocks.POTTED_FERN);
    public static final BlockState SPRUCE_SLAB = slab(Blocks.SPRUCE_SLAB, false);
    public static final BlockState SPRUCE_SLAB_TOP = slab(Blocks.SPRUCE_SLAB, true);
    public static final BlockState WATER_SOURCE = d(Blocks.WATER);

    public static BlockState crop(long h) {
        Block[] crops = {Blocks.WHEAT, Blocks.POTATOES, Blocks.CARROTS};
        Block b = crops[(int) Math.floorMod(h, 3L)];
        return b.defaultBlockState().setValue(BlockStateProperties.AGE_7, (int) Math.floorMod(h >>> 8, 8L));
    }

    public static BlockState snow(int layers) {
        return Blocks.SNOW.defaultBlockState().setValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS, Math.max(1, Math.min(8, layers)));
    }

    public static BlockState ladder(Direction facing) {
        return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing);
    }

    public static BlockState stair(Block b, Direction facing) {
        return b.defaultBlockState().setValue(StairBlock.FACING, facing);
    }

    public static BlockState rot16(BlockState s, int rot) {
        return s.setValue(BlockStateProperties.ROTATION_16, rot & 15);
    }

    public static final BlockState BONE_BLOCK = d(Blocks.BONE_BLOCK), MUD = d(Blocks.MUD), MAGMA = d(Blocks.MAGMA_BLOCK),
            COAL_BLOCK = d(Blocks.COAL_BLOCK), BLACK_CARPET = d(Blocks.BLACK_CARPET), SNOW_BLOCK = d(Blocks.SNOW_BLOCK),
            FLOWER_POT = d(Blocks.FLOWER_POT), BASALT = d(Blocks.BASALT), BLACKSTONE = d(Blocks.BLACKSTONE),
            CHISELED_BOOKSHELF_EMPTY = d(Blocks.CHISELED_BOOKSHELF), POLISHED_ANDESITE_STAIRS_N = stair(Blocks.POLISHED_ANDESITE_STAIRS, Direction.NORTH),
            DEEPSLATE_BRICK_WALL = d(Blocks.DEEPSLATE_BRICK_WALL), IRON_DOOR_LOWER = d(Blocks.IRON_DOOR),
            BELL_CEILING = d(Blocks.BELL).setValue(net.minecraft.world.level.block.BellBlock.ATTACHMENT, net.minecraft.world.level.block.state.properties.BellAttachType.CEILING),
            OBSERVER_N = facing(Blocks.OBSERVER, Direction.NORTH), DISPENSER_N = facing(Blocks.DISPENSER, Direction.NORTH),
            DROPPER_N = facing(Blocks.DROPPER, Direction.NORTH), RAIL_STRAIGHT = d(Blocks.RAIL);

    public static BlockState stairs(Block b, Direction up, boolean top) {
        return stair(b, up).setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM);
    }
}
