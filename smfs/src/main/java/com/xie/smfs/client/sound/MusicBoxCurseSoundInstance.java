package com.xie.smfs.client.sound;

import com.xie.smfs.registry.ModSounds;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;

public class MusicBoxCurseSoundInstance extends MovingSoundInstance {
   private final PlayerEntity player;

   public MusicBoxCurseSoundInstance(PlayerEntity player) {
      super(ModSounds.BACKGROUND_EERIE_MUSIC_BOX, SoundCategory.field_15247, SoundInstance.method_43221());
      this.player = player;
      this.field_5446 = true;
      this.field_5451 = 0;
      this.field_5442 = 0.7F;
      this.field_5441 = 1.0F;
      this.field_5439 = player.method_23317();
      this.field_5450 = player.method_23318();
      this.field_5449 = player.method_23321();
   }

   public void method_16896() {
      if (this.player != null && !this.player.method_31481() && this.player.method_5805()) {
         this.field_5439 = this.player.method_23317();
         this.field_5450 = this.player.method_23318();
         this.field_5449 = this.player.method_23321();
      } else {
         this.method_24876();
      }
   }
}
