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

   public void method_9548(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!world.field_9236 && entity instanceof LivingEntity livingEntity) {
         livingEntity.method_6092(new StatusEffectInstance(StatusEffects.field_5923, 40, 0, false, false, false));
         if (!livingEntity.method_6059(ModEffects.SILENCE)
            && !(livingEntity instanceof WaterGhostEntity)
            && !(
               livingEntity instanceof PlayerEntity player
                  && (PlayerEvents.hasGhostType(player, "water_ghost") || PlayerEvents.hasGhostType(player, "ghost_lake"))
            )) {
            livingEntity.method_6092(new StatusEffectInstance(ModEffects.SILENCE, 60, 0, false, false, true));
         }

         if (livingEntity instanceof PlayerEntity player && !playersInGhostLake.contains(player) && checkBecomeOtherConditions(player)) {
            playersInGhostLake.add(player);
            player.method_7353(Text.method_43470("§c厉鬼在体内争夺身体的控制权..."), true);
         }
      }

      super.method_9548(state, world, pos, entity);
   }

   private static boolean checkBecomeOtherConditions(PlayerEntity player) {
      boolean inGoldBlockShelter = GoldBlockProtectionManager.isPlayerInGoldBlockShelter(player);
      boolean hasGhostSuppressionEffect = player.method_6059(ModEffects.GHOST_SUPPRESSION);
      int ghostCount = PlayerEvents.countOccupiedGhostSlots(player);
      boolean hasAtLeastFourGhosts = ghostCount >= 4;
      return inGoldBlockShelter && hasGhostSuppressionEffect && hasAtLeastFourGhosts;
   }

   private static void handleGhostLakeTimerStatic(PlayerEntity player, World world) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      int currentTimer = playerData.method_10545("GhostLakeTimer") ? playerData.method_10550("GhostLakeTimer") : 0;
      playerData.method_10569("GhostLakeTimer", ++currentTimer);
      if (currentTimer % 200 == 0) {
         int remainingTime = 3600 - currentTimer;
         if (remainingTime > 0) {
            int remainingSeconds = remainingTime / 20;
            player.method_7353(Text.method_43470("§a剩余浸泡时间 " + remainingSeconds + "秒"), true);
         }
      }

      if (currentTimer >= 3600) {
         playerData.method_10569("GhostLakeTimer", 0);
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
               serverPlayer.method_43496(Text.method_43470("§6§l恭喜！你解锁了成就：成为异类§r"));
               serverPlayer.method_43496(Text.method_43470("§7你已经成为一种特殊的存在！§r"));
               ScreenEffectS2CPacket.sendGlitch(serverPlayer);
            }

            AberrationPopupS2CPacket.send(serverPlayer);
         }

         player.method_7353(Text.method_43470("§a你已经成为异类！"), true);
         LOGGER.info("玩家 {} 成为异类", player.method_5477().getString());
      }
   }

   private static void unlockGhostSlotsStatic(PlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      NbtCompound ghostSlots = data.method_10562("GhostSlots");
      if (ghostSlots.method_10545("Slot0")) {
         NbtCompound slot0Data = ghostSlots.method_10562("Slot0");
         slot0Data.method_10556("unlocked", true);
         ghostSlots.method_10566("Slot0", slot0Data);
      }

      if (ghostSlots.method_10545("Slot3")) {
         NbtCompound slot3Data = ghostSlots.method_10562("Slot3");
         slot3Data.method_10556("unlocked", true);
         ghostSlots.method_10566("Slot3", slot3Data);
      }

      data.method_10566("GhostSlots", ghostSlots);
      PlayerEvents.setSpiritAttributes(player, data);
   }

   static {
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         if (!world.method_8608()) {
            for (PlayerEntity player : new HashSet<>(playersInGhostLake)) {
               if (player != null && player.method_5805() && player.method_37908() == world) {
                  BlockState blockState = world.method_8320(player.method_24515());
                  BlockState bodyState = world.method_8320(player.method_24515().method_10084());
                  if (!(blockState.method_26204() instanceof GhostLakeBlock) && !(bodyState.method_26204() instanceof GhostLakeBlock)) {
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
