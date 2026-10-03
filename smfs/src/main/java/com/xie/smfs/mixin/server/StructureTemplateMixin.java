package com.xie.smfs.mixin.server;

import com.xie.smfs.worldgen.GhostSpawnProcessor;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {
   @Inject(method = "place", at = @At("RETURN"))
   private void onPlaceComplete(
      ServerWorldAccess world,
      BlockPos pos,
      BlockPos pivot,
      StructurePlacementData placementData,
      Random random,
      int flags,
      CallbackInfoReturnable<Boolean> cir
   ) {
      if (cir.getReturnValue()) {
         for (StructureProcessor processor : placementData.getProcessors()) {
            if (processor instanceof GhostSpawnProcessor gsp) {
               gsp.finalize(world);
            }
         }
      }
   }
}
