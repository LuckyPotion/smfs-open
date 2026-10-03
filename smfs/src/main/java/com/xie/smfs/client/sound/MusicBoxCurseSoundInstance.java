package com.xie.smfs.client.sound;

import com.xie.smfs.registry.ModSounds;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;

public class MusicBoxCurseSoundInstance extends MovingSoundInstance {
   private final PlayerEntity player;

   public MusicBoxCurseSoundInstance(PlayerEntity player) {
      super(ModSounds.BACKGROUND_EERIE_MUSIC_BOX, SoundCategory.RECORDS, SoundInstance.createRandom());
      this.player = player;
      this.repeat = true;
      this.repeatDelay = 0;
      this.volume = 0.7F;
      this.pitch = 1.0F;
      this.x = player.getX();
      this.y = player.getY();
      this.z = player.getZ();
   }

   public void tick() {
      if (this.player != null && !this.player.isRemoved() && this.player.isAlive()) {
         this.x = this.player.getX();
         this.y = this.player.getY();
         this.z = this.player.getZ();
      } else {
         this.setDone();
      }
   }
}
