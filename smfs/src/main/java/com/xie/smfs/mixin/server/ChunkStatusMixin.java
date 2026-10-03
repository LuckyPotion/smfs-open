package com.xie.smfs.mixin.server;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkStatus.class)
public class ChunkStatusMixin {
   private static final int LARGE_STRUCTURE_CHUNK_RADIUS = 9;
   @Shadow
   @Final
   @Mutable
   private static List<ChunkStatus> field_12791;
   @Shadow
   @Final
   @Mutable
   private static IntList field_12788;

   @ModifyConstant(
      method = "<clinit>",
      constant = @Constant(intValue = 8),
      slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=structure_references"), to = @At(value = "CONSTANT", args = "stringValue=biomes"))
   )
   private static int smfs$expandStructureReferenceTaskMargin(int original) {
      return 9;
   }

   @ModifyConstant(
      method = "<clinit>",
      constant = @Constant(intValue = 8),
      slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=features"), to = @At(value = "CONSTANT", args = "stringValue=initialize_light"))
   )
   private static int smfs$expandFeatureTaskMargin(int original) {
      return 9;
   }

   @Inject(method = "<clinit>", at = @At("TAIL"))
   private static void smfs$expandStructureStartDistance(CallbackInfo ci) {
      field_12791 = ImmutableList.builder().addAll(field_12791).add(ChunkStatus.STRUCTURE_STARTS).build();
      field_12788 = smfs$buildStatusToDistance();
   }

   private static IntList smfs$buildStatusToDistance() {
      List<ChunkStatus> orderedStatuses = ChunkStatus.createOrderedList();
      IntArrayList distanceByStatus = new IntArrayList(orderedStatuses.size());
      int distance = 0;

      for (int statusIndex = orderedStatuses.size() - 1; statusIndex >= 0; statusIndex--) {
         while (distance + 1 < field_12791.size() && statusIndex <= field_12791.get(distance + 1).getIndex()) {
            distance++;
         }

         distanceByStatus.add(0, distance);
      }

      return distanceByStatus;
   }
}
