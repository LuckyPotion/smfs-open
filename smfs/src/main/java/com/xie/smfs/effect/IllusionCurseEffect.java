package com.xie.smfs.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

public class IllusionCurseEffect extends StatusEffect implements ICurseEffect {
   public IllusionCurseEffect() {
      super(StatusEffectCategory.field_18272, 9662683);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (entity instanceof ServerPlayerEntity player && !player.method_37908().field_9236) {
         if (!player.method_7325()) {
            player.method_32748(this.createGameModeNbt(GameMode.field_9219));
            player.method_7353(Text.method_43471("effect.smfs.illusion_curse.enter").method_27692(Formatting.field_1064), true);
         }

         if (entity.method_6112(this).method_5584() <= 100) {
            player.method_7353(Text.method_43471("effect.smfs.illusion_curse.warning").method_27692(Formatting.field_1076), true);
         }
      }
   }

   public void method_5562(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof ServerPlayerEntity player && !player.method_37908().field_9236) {
         player.method_32748(this.createGameModeNbt(GameMode.field_9215));
         player.method_7353(Text.method_43471("effect.smfs.illusion_curse.exit").method_27692(Formatting.field_1060), true);
      }

      super.method_5562(entity, attributes, amplifier);
   }

   private NbtCompound createGameModeNbt(GameMode gameMode) {
      NbtCompound nbt = new NbtCompound();
      nbt.method_10569("playerGameType", gameMode.method_8379());
      return nbt;
   }
}
