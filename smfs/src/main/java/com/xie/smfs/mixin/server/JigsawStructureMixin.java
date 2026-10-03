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
      int i = switch (structure.method_42701()) {
         case field_28922 -> 0;
         case field_28923, field_38431, field_38432 -> 12;
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
