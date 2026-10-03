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
      super(StatusEffectCategory.field_18272, 14423100);
   }

   public boolean method_5552(int duration, int amplifier) {
      return false;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
   }

   public void method_5555(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      super.method_5555(entity, attributes, amplifier);
   }

   public void method_5562(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof ServerPlayerEntity player && !entity.method_37908().method_8608()) {
         if (SAFE_REMOVAL_PLAYERS.contains(player.method_5667())) {
            SAFE_REMOVAL_PLAYERS.remove(player.method_5667());
            this.closeMusicBox(player);
         } else {
            this.closeMusicBox(player);
            InstantKillUtil.executeInstantKillIgnoreMusicBox(player);
         }
      }

      super.method_5562(entity, attributes, amplifier);
   }

   private void closeMusicBox(PlayerEntity player) {
      boolean foundMusicBox = false;
      ItemStack mainHandStack = player.method_6047();
      ItemStack offHandStack = player.method_6079();
      if (mainHandStack.method_7909() instanceof EerieMusicBoxItem musicBox) {
         musicBox.setMusicBoxOpen(mainHandStack, false);
         foundMusicBox = true;
      } else if (offHandStack.method_7909() instanceof EerieMusicBoxItem musicBox) {
         musicBox.setMusicBoxOpen(offHandStack, false);
         foundMusicBox = true;
      }

      if (!foundMusicBox) {
         for (int i = 0; i < player.method_31548().method_5439(); i++) {
            ItemStack stack = player.method_31548().method_5438(i);
            if (stack.method_7909() instanceof EerieMusicBoxItem musicBox) {
               musicBox.setMusicBoxOpen(stack, false);
               foundMusicBox = true;
               break;
            }
         }
      }

      player.method_37908()
         .method_43128(
            null,
            player.method_23317(),
            player.method_23318(),
            player.method_23321(),
            (SoundEvent)SoundEvents.field_14793.comp_349(),
            SoundCategory.field_15248,
            1.0F,
            0.5F
         );
      player.method_7353(Text.method_43471("item.smfs.eerie_music_box.closed"), true);
   }

   public boolean method_5561() {
      return false;
   }

   public String method_5567() {
      return "effect.smfs.music_box_curse";
   }

   public static void safelyRemoveEffect(PlayerEntity player) {
      if (player != null && !player.method_37908().method_8608() && player.method_6059(ModEffects.MUSIC_BOX_CURSE)) {
         SAFE_REMOVAL_PLAYERS.add(player.method_5667());
         player.method_6016(ModEffects.MUSIC_BOX_CURSE);
      }
   }
}
