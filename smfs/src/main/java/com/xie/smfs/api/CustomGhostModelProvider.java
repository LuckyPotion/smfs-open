package com.xie.smfs.api;

import net.minecraft.util.Identifier;

public interface CustomGhostModelProvider {
   Identifier getModelPath();

   Identifier getTexturePath();

   Identifier getAnimationPath();

   float getScale();
}
