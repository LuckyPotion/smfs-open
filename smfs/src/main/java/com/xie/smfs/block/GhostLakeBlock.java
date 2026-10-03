package com.xie.smfs.block;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.effect.GhostSuppressionEffect;
import com.xie.smfs.entity.ghost.WaterGhostEntity;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.manager.GoldBlockProtectionManager;
import com.xie.smfs.network.packets.ui.s2c.AberrationPopupS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.ScreenEffectS2CPacket;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModFluids;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostLakeBlock extends FluidBlock {
   private static final Logger LOGGER = LoggerFactory.getLogger(GhostLakeBlock.class);
   private static final String GHOST_LAKE_TIMER_KEY = "GhostLakeTimer";
   private static final int UNLOCK_REQUIRED_TIME = 3600;
   private static final int GHOST_COUNT_THRESHOLD = 4;
   private static final HashSet<PlayerEntity> playersInGhostLake = new HashSet<>();

   public GhostLakeBlock(Settings settings) {
      super(ModFluids.GHOST_LAKE_STILL, settings);
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!world.isClient && entity instanceof LivingEntity livingEntity) {
         livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 40, 0, false, false, false));
         if (!livingEntity.hasStatusEffect(ModEffects.SILENCE)
            && !(livingEntity instanceof WaterGhostEntity)
            && !(
               livingEntity instanceof PlayerEntity player
                  && (PlayerEvents.hasGhostType(player, "water_ghost") || PlayerEvents.hasGhostType(player, "ghost_lake"))
            )) {
            livingEntity.addStatusEffect(new StatusEffectInstance(ModEffects.SILENCE, 60, 0, false, false, true));
         }

         if (livingEntity instanceof PlayerEntity player && !playersInGhostLake.contains(player) && checkBecomeOtherConditions(player)) {
            playersInGhostLake.add(player);
            player.sendMessage(Text.literal("§c厉鬼在体内争夺身体的控制权..."), true);
         }
      }

      super.onEntityCollision(state, world, pos, entity);
   }

   private static boolean checkBecomeOtherConditions(PlayerEntity player) {
      boolean inGoldBlockShelter = GoldBlockProtectionManager.isPlayerInGoldBlockShelter(player);
      boolean hasGhostSuppressionEffect = player.hasStatusEffect(ModEffects.GHOST_SUPPRESSION);
      int ghostCount = PlayerEvents.countOccupiedGhostSlots(player);
      boolean hasAtLeastFourGhosts = ghostCount >= 4;
      return inGoldBlockShelter && hasGhostSuppressionEffect && hasAtLeastFourGhosts;
   }

   private static void handleGhostLakeTimerStatic(PlayerEntity player, World world) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      int currentTimer = playerData.contains("GhostLakeTimer") ? playerData.getInt("GhostLakeTimer") : 0;
      playerData.putInt("GhostLakeTimer", ++currentTimer);
      if (currentTimer % 200 == 0) {
         int remainingTime = 3600 - currentTimer;
         if (remainingTime > 0) {
            int remainingSeconds = remainingTime / 20;
            player.sendMessage(Text.literal("§a剩余浸泡时间 " + remainingSeconds + "秒"), true);
         }
      }

      if (currentTimer >= 3600) {
         playerData.putInt("GhostLakeTimer", 0);
         PlayerEvents.setSpiritAttributes(player, playerData);
         becomeOtherStatic(player);
      }

      PlayerEvents.setSpiritAttributes(player, playerData);
   }

   private static void becomeOtherStatic(PlayerEntity player) {
      GhostSuppressionEffect.safelyRemoveEffect(player);
      if (player instanceof ServerPlayerEntity serverPlayer && AdvancementManager.hasHumanSkinPaper(serverPlayer)) {
         AdvancementManager.checkHumanSkinPaperEnding(serverPlayer);
      } else {
         unlockGhostSlotsStatic(player);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            if (!AdvancementManager.hasAdvancement(serverPlayer, "smfs:become_aberration")) {
               AdvancementManager.unlockAdvancement(serverPlayer, "smfs:become_aberration");
               serverPlayer.sendMessage(Text.literal("§6§l恭喜！你解锁了成就：成为异类§r"));
               serverPlayer.sendMessage(Text.literal("§7你已经成为一种特殊的存在！§r"));
               ScreenEffectS2CPacket.sendGlitch(serverPlayer);
            }

            AberrationPopupS2CPacket.send(serverPlayer);
         }

         player.sendMessage(Text.literal("§a你已经成为异类！"), true);
         LOGGER.info("玩家 {} 成为异类", player.getName().getString());
      }
   }

   private static void unlockGhostSlotsStatic(PlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      NbtCompound ghostSlots = data.getCompound("GhostSlots");
      if (ghostSlots.contains("Slot0")) {
         NbtCompound slot0Data = ghostSlots.getCompound("Slot0");
         slot0Data.putBoolean("unlocked", true);
         ghostSlots.put("Slot0", slot0Data);
      }

      if (ghostSlots.contains("Slot3")) {
         NbtCompound slot3Data = ghostSlots.getCompound("Slot3");
         slot3Data.putBoolean("unlocked", true);
         ghostSlots.put("Slot3", slot3Data);
      }

      data.put("GhostSlots", ghostSlots);
      PlayerEvents.setSpiritAttributes(player, data);
   }

   static {
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         if (!world.isClient()) {
            for (PlayerEntity player : new HashSet<>(playersInGhostLake)) {
               if (player != null && player.isAlive() && player.getWorld() == world) {
                  BlockState blockState = world.getBlockState(player.getBlockPos());
                  BlockState bodyState = world.getBlockState(player.getBlockPos().up());
                  if (!(blockState.getBlock() instanceof GhostLakeBlock) && !(bodyState.getBlock() instanceof GhostLakeBlock)) {
                     playersInGhostLake.remove(player);
                  } else if (checkBecomeOtherConditions(player)) {
                     handleGhostLakeTimerStatic(player, world);
                  } else {
                     playersInGhostLake.remove(player);
                  }
               } else {
                  playersInGhostLake.remove(player);
               }
            }
         }
      });
   }
}
