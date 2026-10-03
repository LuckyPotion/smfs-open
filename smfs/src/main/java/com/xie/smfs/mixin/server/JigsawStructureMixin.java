package com.xie.smfs.mixin.server;

import com.mojang.serialization.DataResult;
import java.lang.reflect.Field;
import net.minecraft.world.gen.structure.JigsawStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JigsawStructure.class)
public class JigsawStructureMixin {
   @Inject(method = "validate", at = @At("HEAD"), cancellable = true)
   private static void validate(JigsawStructure structure, CallbackInfoReturnable<DataResult<JigsawStructure>> cir) {
      int i = switch (structure.getTerrainAdaptation()) {
         case NONE -> 0;
         case BURY, BEARD_THIN, BEARD_BOX -> 12;
         default -> throw new IncompatibleClassChangeError();
      };

      try {
         Field field = JigsawStructure.class.getDeclaredField("maxDistanceFromCenter");
         field.setAccessible(true);
         int maxDistanceFromCenter = field.getInt(structure);
         if (maxDistanceFromCenter + i > 256) {
            cir.setReturnValue(DataResult.error(() -> "Structure size including terrain adaptation must not exceed 256"));
         } else {
            cir.setReturnValue(DataResult.success(structure));
         }
      } catch (Exception e) {
         e.printStackTrace();
         cir.setReturnValue(DataResult.success(structure));
      }
   }
}
