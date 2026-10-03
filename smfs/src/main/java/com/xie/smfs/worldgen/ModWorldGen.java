package com.xie.smfs.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

public class ModWorldGen {
   public static final RegistryKey<PlacedFeature> DEFILED_ORE_PLACED_KEY = RegistryKey.method_29179(
      RegistryKeys.field_41245, new Identifier("smfs", "defiled_ore")
   );

   public static void register() {
      BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), Feature.field_13176, DEFILED_ORE_PLACED_KEY);
   }
}
