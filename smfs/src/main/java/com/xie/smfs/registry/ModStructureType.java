package com.xie.smfs.registry;

import com.xie.smfs.structure.CustomJigsawStructure;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.StructureType;

public class ModStructureType {
   public static final StructureType<CustomJigsawStructure> CUSTOM_JIGSAW = (StructureType<CustomJigsawStructure>)Registry.register(
      Registries.STRUCTURE_TYPE, new Identifier("smfs", "custom_jigsaw"), (StructureType)() -> CustomJigsawStructure.CODEC
   );

   public static void registerStructureTypes() {
   }
}
