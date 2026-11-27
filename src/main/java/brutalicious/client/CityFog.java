package brutalicious.client;

import brutalicious.Brutalicious;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;

public final class CityFog {
    private CityFog() {}

    static final ResourceKey<Biome> SPRAWL = key("concrete_sprawl");
    static final ResourceKey<Biome> MONOLITH = key("monolith_district");
    static final ResourceKey<Biome> UNDERCROFT = key("undercroft");
    static final ResourceKey<Biome> CISTERN = key("cistern");
    static final ResourceKey<Biome> RESERVOIR = key("reservoir");
    static final ResourceKey<Biome> SETTLEMENT = key("settlement");
    static final ResourceKey<Biome> BLACKOUT = key("blackout");
    static final ResourceKey<Biome> EXCLUSION = key("exclusion_zone");

    static ResourceKey<Biome> key(String path) {
        return ResourceKey.create(Registries.BIOME, Brutalicious.id(path));
    }

    private static float current = -1;
    private static long lastNanos;

    public static void apply(Camera camera, FogRenderer.FogMode mode, float farPlane) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || camera.getFluidInCamera() != FogType.NONE) return;
        if (camera.getEntity() instanceof net.minecraft.world.entity.LivingEntity le
                && (le.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS) || le.hasEffect(net.minecraft.world.effect.MobEffects.DARKNESS))) return;

        Holder<Biome> biome = mc.level.getBiome(BlockPos.containing(camera.getPosition()));
        float target;
        if (biome.is(SPRAWL)) target = 88f;
        else if (biome.is(MONOLITH)) target = 52f;
        else if (biome.is(RESERVOIR)) target = 64f;
        else if (biome.is(SETTLEMENT)) target = 110f;
        else if (biome.is(EXCLUSION)) target = 58f;
        else if (biome.is(BLACKOUT)) target = 18f;
        else if (biome.is(UNDERCROFT)) target = 30f;
        else if (biome.is(CISTERN)) target = 22f;
        else target = farPlane;
        target = Math.min(target, farPlane);

        long now = System.nanoTime();
        if (mode == FogRenderer.FogMode.FOG_TERRAIN) {
            float dt = current < 0 ? 1f : Math.min(1f, (now - lastNanos) / 1.5e9f);
            current = current < 0 ? target : current + (target - current) * dt;
            lastNanos = now;
        }
        float end = current < 0 ? target : current;
        if (end >= farPlane - 0.5f) return;

        RenderSystem.setShaderFogEnd(Math.min(RenderSystem.getShaderFogEnd(), end));
        RenderSystem.setShaderFogStart(mode == FogRenderer.FogMode.FOG_SKY ? 0f : Math.min(RenderSystem.getShaderFogStart(), end * 0.15f));
    }
}
