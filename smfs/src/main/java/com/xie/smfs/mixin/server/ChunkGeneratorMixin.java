package com.xie.smfs.mixin.server;

import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
   private static final int LARGE_STRUCTURE_REFERENCE_RADIUS = 9;

   @ModifyConstant(method = "addStructureReferences", constant = @Constant(intValue = 8))
   private int smfs$expandStructureReferenceRadius(int original) {
      return 9;
   }
}
