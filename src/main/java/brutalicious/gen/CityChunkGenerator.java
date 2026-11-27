package brutalicious.gen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class CityChunkGenerator extends ChunkGenerator {
    public static final MapCodec<CityChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)
    ).apply(i, CityChunkGenerator::new));

    private volatile long seed;
    private static final ThreadLocal<Column> COLUMN = ThreadLocal.withInitial(Column::new);

    public CityChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSets, RandomState randomState, long seed) {
        setSeed(seed);
        return super.createState(structureSets, randomState, seed);
    }

    public void setSeed(long seed) {
        this.seed = seed;
        if (biomeSource instanceof CityBiomeSource s) s.seed = seed;
    }

    public long seed() {
        return seed;
    }

    public Column column(int x, int z, Column.Sink sink) {
        Column c = COLUMN.get();
        c.reset(seed, x, z, sink);
        CityGen.fill(c);
        c.flush();
        return c;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        int bx = chunk.getPos().getMinBlockX(), bz = chunk.getPos().getMinBlockZ();
        int minY = Math.max(chunk.getMinBuildHeight(), Column.MIN_Y);
        int maxY = Math.min(chunk.getMaxBuildHeight() - 1, Column.MAX_Y);
        Heightmap ocean = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        Column.Sink sink = new Column.Sink() {
            @Override public void blockEntity(CompoundTag tag) { chunk.setBlockEntityNbt(tag); }
            @Override public void postProcess(BlockPos pos) { chunk.markPosForPostprocessing(pos); }
        };
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                Column c = column(bx + lx, bz + lz, sink);
                int top = Integer.MIN_VALUE, floor = Integer.MIN_VALUE;
                for (int y = maxY; y >= minY; y--) {
                    BlockState s = c.states[y - Column.MIN_Y];
                    if (s.isAir()) continue;
                    LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
                    section.setBlockState(lx, y & 15, lz, s, false);
                    if (top == Integer.MIN_VALUE) top = y;
                    if (floor == Integer.MIN_VALUE && Heightmap.Types.OCEAN_FLOOR_WG.isOpaque().test(s)) floor = y;
                }
                if (top != Integer.MIN_VALUE) surface.update(lx, top, lz, c.states[top - Column.MIN_Y]);
                if (floor != Integer.MIN_VALUE) ocean.update(lx, floor, lz, c.states[floor - Column.MIN_Y]);
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
        Column c = column(x, z, null);
        int min = Math.max(level.getMinBuildHeight(), Column.MIN_Y);
        for (int y = Math.min(level.getMaxBuildHeight() - 1, Column.MAX_Y); y >= min; y--) {
            if (type.isOpaque().test(c.states[y - Column.MIN_Y])) return y + 1;
        }
        return level.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        Column c = column(x, z, null);
        int min = level.getMinBuildHeight(), h = level.getHeight();
        BlockState[] out = new BlockState[h];
        for (int i = 0; i < h; i++) {
            int y = min + i;
            out[i] = (y >= Column.MIN_Y && y <= Column.MAX_Y) ? c.states[y - Column.MIN_Y] : B.AIR;
        }
        return new NoiseColumn(min, out);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("Brutalicious: " + CityGen.describe(seed, pos.getX(), pos.getZ()));
    }

    @Override public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {}
    @Override public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {}
    @Override public void spawnOriginalMobs(WorldGenRegion region) {}
    @Override public int getGenDepth() { return Column.HEIGHT; }
    @Override public int getSeaLevel() { return CityGen.GROUND; }
    @Override public int getMinY() { return Column.MIN_Y; }
}
