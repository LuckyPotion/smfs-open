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

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!world.isClient && entity instanceof LivingEntity livingEntity) {
         livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 40, 0, false, false, false));
         if (!livingEntity.hasStatusEffect(ModEffects.SILENCE)
            && !(livingEntity instanceof WaterGhostEntity)
            && !(livingEntity instanceof PlayerEntity player && PlayerEvents.hasGhostType(player, "water_ghost"))) {
            livingEntity.addStatusEffect(new StatusEffectInstance(ModEffects.SILENCE, 60, 0, false, false, true));
         }

         if (livingEntity instanceof PlayerEntity player) {
            PlayerEvents.decreaseAllSlotRevivalDegree(player, 1);
            playersInBloodLake.add(player);
         }
      }

      super.onEntityCollision(state, world, pos, entity);
   }

   private static void handleBloodLakeTimerStatic(PlayerEntity player, World world) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      int currentTimer = playerData.contains("BloodLakeTimer") ? playerData.getInt("BloodLakeTimer") : 0;
      playerData.putInt("BloodLakeTimer", ++currentTimer);
      if (currentTimer >= 12000) {
         playerData.putInt("BloodLakeTimer", 0);
         PlayerEvents.setSpiritAttributes(player, playerData);
         int ghostCount = PlayerEvents.countOccupiedGhostSlots(player);
         if (ghostCount >= 5) {
            giveGhostBloodItemStatic(player);
            player.sendMessage(Text.literal("§a你在鬼血湖中坚持了10分钟，成功驾驭了鬼血！"), true);
         } else {
            player.damage(ModDamageSources.ghost(world), 99999.0F);
            player.sendMessage(Text.literal("§c驾驭的鬼太少，无法承受鬼血的力量！"), true);
         }
      }

      if (currentTimer % 600 == 0) {
         int remainingTime = (12000 - currentTimer) / 20;
         player.sendMessage(Text.literal("§7鬼血湖计时器：" + remainingTime / 60 + "分" + remainingTime % 60 + "秒"), true);
      }
   }

   private static void giveGhostBloodItemStatic(PlayerEntity player) {
      ItemStack ghostBloodItem = new ItemStack(ModItems.GHOST_BLOOD);
      if (!player.getInventory().insertStack(ghostBloodItem)) {
         player.dropItem(ghostBloodItem, false);
      }
   }

   static {
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         if (!world.isClient()) {
            for (PlayerEntity player : new HashSet<>(playersInBloodLake)) {
               if (player != null && player.isAlive() && player.getWorld() == world) {
                  BlockState blockState = world.getBlockState(player.getBlockPos());
                  if (blockState.getBlock() instanceof BloodLakeBlock) {
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
