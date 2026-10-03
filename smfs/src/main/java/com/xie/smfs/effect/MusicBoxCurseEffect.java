package com.xie.smfs.effect;

import com.xie.smfs.item.EerieMusicBoxItem;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.util.InstantKillUtil;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class MusicBoxCurseEffect extends StatusEffect implements ICurseEffect {
   private static final Set<UUID> SAFE_REMOVAL_PLAYERS = new HashSet<>();

   public MusicBoxCurseEffect() {
      super(StatusEffectCategory.HARMFUL, 14423100);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return false;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
   }

   public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      super.onApplied(entity, attributes, amplifier);
   }

   public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof ServerPlayerEntity player && !entity.getWorld().isClient()) {
         if (SAFE_REMOVAL_PLAYERS.contains(player.getUuid())) {
            SAFE_REMOVAL_PLAYERS.remove(player.getUuid());
            this.closeMusicBox(player);
         } else {
            this.closeMusicBox(player);
            InstantKillUtil.executeInstantKillIgnoreMusicBox(player);
         }
      }

      super.onRemoved(entity, attributes, amplifier);
   }

   private void closeMusicBox(PlayerEntity player) {
      boolean foundMusicBox = false;
      ItemStack mainHandStack = player.getMainHandStack();
      ItemStack offHandStack = player.getOffHandStack();
      if (mainHandStack.getItem() instanceof EerieMusicBoxItem musicBox) {
         musicBox.setMusicBoxOpen(mainHandStack, false);
         foundMusicBox = true;
      } else if (offHandStack.getItem() instanceof EerieMusicBoxItem musicBox) {
         musicBox.setMusicBoxOpen(offHandStack, false);
         foundMusicBox = true;
      }

      if (!foundMusicBox) {
         for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() instanceof EerieMusicBoxItem musicBox) {
               musicBox.setMusicBoxOpen(stack, false);
               foundMusicBox = true;
               break;
            }
         }
      }

      player.getWorld()
         .playSound(null, player.getX(), player.getY(), player.getZ(), (SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.PLAYERS, 1.0F, 0.5F);
      player.sendMessage(Text.translatable("item.smfs.eerie_music_box.closed"), true);
   }

   public boolean isInstant() {
      return false;
   }

   public String getTranslationKey() {
      return "effect.smfs.music_box_curse";
   }

   public static void safelyRemoveEffect(PlayerEntity player) {
      if (player != null && !player.getWorld().isClient() && player.hasStatusEffect(ModEffects.MUSIC_BOX_CURSE)) {
         SAFE_REMOVAL_PLAYERS.add(player.getUuid());
         player.removeStatusEffect(ModEffects.MUSIC_BOX_CURSE);
      }
   }
}
