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
      super(StatusEffectCategory.HARMFUL, 9662683);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (entity instanceof ServerPlayerEntity player && !player.getWorld().isClient) {
         if (!player.isSpectator()) {
            player.setGameMode(this.createGameModeNbt(GameMode.SPECTATOR));
            player.sendMessage(Text.translatable("effect.smfs.illusion_curse.enter").formatted(Formatting.DARK_PURPLE), true);
         }

         if (entity.getStatusEffect(this).getDuration() <= 100) {
            player.sendMessage(Text.translatable("effect.smfs.illusion_curse.warning").formatted(Formatting.LIGHT_PURPLE), true);
         }
      }
   }

   public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof ServerPlayerEntity player && !player.getWorld().isClient) {
         player.setGameMode(this.createGameModeNbt(GameMode.SURVIVAL));
         player.sendMessage(Text.translatable("effect.smfs.illusion_curse.exit").formatted(Formatting.GREEN), true);
      }

      super.onRemoved(entity, attributes, amplifier);
   }

   private NbtCompound createGameModeNbt(GameMode gameMode) {
      NbtCompound nbt = new NbtCompound();
      nbt.putInt("playerGameType", gameMode.getId());
      return nbt;
   }
}
