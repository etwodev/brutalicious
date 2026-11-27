package brutalicious;

import brutalicious.debug.PreviewRenderer;
import brutalicious.gen.CityBiomeSource;
import brutalicious.gen.CityChunkGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Brutalicious implements ModInitializer {
    public static final String MOD_ID = "brutalicious";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        OverworldQuartz.init();
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, id("city"), CityChunkGenerator.CODEC);
        Registry.register(BuiltInRegistries.BIOME_SOURCE, id("city"), CityBiomeSource.CODEC);

        String preview = System.getProperty("brutalicious.preview");
        if (preview != null) ServerLifecycleEvents.SERVER_STARTED.register(server -> PreviewRenderer.run(server, preview));
    }
}
