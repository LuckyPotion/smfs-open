package com.xie.smfs.mixin.server;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.ChunkRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkRegion.class)
public interface ChunkRegionAccessor {
   @Accessor("world")
   ServerWorld getWorld();
}
