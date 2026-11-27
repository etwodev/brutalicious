package brutalicious.gen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.Optional;
import java.util.stream.Stream;

public final class CityBiomeSource extends BiomeSource {
    public static final MapCodec<CityBiomeSource> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Biome.CODEC.fieldOf("sprawl").forGetter(s -> s.sprawl),
            Biome.CODEC.fieldOf("monolith").forGetter(s -> s.monolith),
            Biome.CODEC.fieldOf("undercroft").forGetter(s -> s.undercroft),
            Biome.CODEC.fieldOf("cistern").forGetter(s -> s.cistern),
            Biome.CODEC.optionalFieldOf("reservoir").forGetter(s -> Optional.of(s.reservoir)),
            Biome.CODEC.optionalFieldOf("settlement").forGetter(s -> Optional.of(s.settlement)),
            Biome.CODEC.optionalFieldOf("blackout").forGetter(s -> Optional.of(s.blackout)),
            Biome.CODEC.optionalFieldOf("exclusion").forGetter(s -> Optional.of(s.exclusion))
    ).apply(i, CityBiomeSource::new));

    final Holder<Biome> sprawl, monolith, undercroft, cistern, reservoir, settlement, blackout, exclusion;
    volatile long seed;

    public CityBiomeSource(Holder<Biome> sprawl, Holder<Biome> monolith, Holder<Biome> undercroft, Holder<Biome> cistern,
                           Optional<Holder<Biome>> reservoir, Optional<Holder<Biome>> settlement,
                           Optional<Holder<Biome>> blackout, Optional<Holder<Biome>> exclusion) {
        this.sprawl = sprawl;
        this.monolith = monolith;
        this.undercroft = undercroft;
        this.cistern = cistern;
        this.reservoir = reservoir.orElse(sprawl);
        this.settlement = settlement.orElse(sprawl);
        this.blackout = blackout.orElse(undercroft);
        this.exclusion = exclusion.orElse(sprawl);
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(sprawl, monolith, undercroft, cistern, reservoir, settlement, blackout, exclusion).distinct();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int qx, int qy, int qz, Climate.Sampler sampler) {
        int y = QuartPos.toBlock(qy);
        int x = QuartPos.toBlock(qx), z = QuartPos.toBlock(qz);
        if (y <= CityGen.CISTERN_CEIL) return cistern;
        Zones.Zone zone = Zones.zoneAt(seed, x, z);
        if (zone == Zones.Zone.FLOODED && y >= 36) return reservoir;
        if (zone == Zones.Zone.SURVIVOR && y >= 56) return settlement;
        if (zone == Zones.Zone.DARK && y >= 56 && y <= Blackout.TOP) return blackout;
        if (zone == Zones.Zone.ABANDONED && y >= 56) return exclusion;
        if (y < 56) return undercroft;
        return CityPlan.monolith(seed, x, z) ? monolith : sprawl;
    }
}
