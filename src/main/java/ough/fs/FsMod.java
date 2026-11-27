package ough.fs;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.GenerationStep;

public class FsMod implements ModInitializer {
	public static final String MODID = "fs";

	public static final ResourceLocation OVERWORLDQUARTZ_ID = id("overworldquartz");

	public static final Block OVERWORLDQUARTZ = Registry.register(BuiltInRegistries.BLOCK, OVERWORLDQUARTZ_ID,
			new DropExperienceBlock(UniformInt.of(2, 5), BlockBehaviour.Properties.of().strength(3f, 6f).requiresCorrectToolForDrops()));

	public static final Item OVERWORLDQUARTZ_ITEM = Registry.register(BuiltInRegistries.ITEM, OVERWORLDQUARTZ_ID,
			new BlockItem(OVERWORLDQUARTZ, new Item.Properties()));

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	@Override
	public void onInitialize() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> entries.accept(OVERWORLDQUARTZ_ITEM));


		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES,
				ResourceKey.create(Registries.PLACED_FEATURE, OVERWORLDQUARTZ_ID));
	}
}
