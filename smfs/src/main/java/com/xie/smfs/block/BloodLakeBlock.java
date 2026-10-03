package com.xie.smfs.block;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.ghost.WaterGhostEntity;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModFluids;
import com.xie.smfs.registry.ModItems;
import java.util.HashSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndWorldTick;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BloodLakeBlock extends FluidBlock {
   private static final String BLOOD_LAKE_TIMER_KEY = "BloodLakeTimer";
   private static final int BLOOD_LAKE_TIMER_THRESHOLD = 12000;
   private static final int GHOST_COUNT_THRESHOLD = 5;
   private static final HashSet<PlayerEntity> playersInBloodLake = new HashSet<>();

   public BloodLakeBlock(Settings settings) {
      super(ModFluids.BLOOD_LAKE_STILL, settings);
   }

   public void method_9548(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!world.field_9236 && entity instanceof LivingEntity livingEntity) {
         livingEntity.method_6092(new StatusEffectInstance(StatusEffects.field_5923, 40, 0, false, false, false));
         if (!livingEntity.method_6059(ModEffects.SILENCE)
            && !(livingEntity instanceof WaterGhostEntity)
            && !(livingEntity instanceof PlayerEntity player && PlayerEvents.hasGhostType(player, "water_ghost"))) {
            livingEntity.method_6092(new StatusEffectInstance(ModEffects.SILENCE, 60, 0, false, false, true));
         }

         if (livingEntity instanceof PlayerEntity player) {
            PlayerEvents.decreaseAllSlotRevivalDegree(player, 1);
            playersInBloodLake.add(player);
         }
      }

      super.method_9548(state, world, pos, entity);
   }

   private static void handleBloodLakeTimerStatic(PlayerEntity player, World world) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      int currentTimer = playerData.method_10545("BloodLakeTimer") ? playerData.method_10550("BloodLakeTimer") : 0;
      playerData.method_10569("BloodLakeTimer", ++currentTimer);
      if (currentTimer >= 12000) {
         playerData.method_10569("BloodLakeTimer", 0);
         PlayerEvents.setSpiritAttributes(player, playerData);
         int ghostCount = PlayerEvents.countOccupiedGhostSlots(player);
         if (ghostCount >= 5) {
            giveGhostBloodItemStatic(player);
            player.method_7353(Text.method_43470("§a你在鬼血湖中坚持了10分钟，成功驾驭了鬼血！"), true);
         } else {
            player.method_5643(ModDamageSources.ghost(world), 99999.0F);
            player.method_7353(Text.method_43470("§c驾驭的鬼太少，无法承受鬼血的力量！"), true);
         }
      }

      if (currentTimer % 600 == 0) {
         int remainingTime = (12000 - currentTimer) / 20;
         player.method_7353(Text.method_43470("§7鬼血湖计时器：" + remainingTime / 60 + "分" + remainingTime % 60 + "秒"), true);
      }
   }

   private static void giveGhostBloodItemStatic(PlayerEntity player) {
      ItemStack ghostBloodItem = new ItemStack(ModItems.GHOST_BLOOD);
      if (!player.method_31548().method_7394(ghostBloodItem)) {
         player.method_7328(ghostBloodItem, false);
      }
   }

   static {
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         if (!world.method_8608()) {
            for (PlayerEntity player : new HashSet<>(playersInBloodLake)) {
               if (player != null && player.method_5805() && player.method_37908() == world) {
                  BlockState blockState = world.method_8320(player.method_24515());
                  if (blockState.method_26204() instanceof BloodLakeBlock) {
                     handleBloodLakeTimerStatic(player, world);
                  } else {
                     playersInBloodLake.remove(player);
                  }
               } else {
                  playersInBloodLake.remove(player);
               }
            }
         }
      });
   }
}
