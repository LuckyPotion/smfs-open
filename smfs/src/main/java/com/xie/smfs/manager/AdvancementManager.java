package com.xie.smfs.manager;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.data.GhostChildData;
import com.xie.smfs.data.PlayerGhostChildManager;
import com.xie.smfs.effect.SpiritAttributes;
import com.xie.smfs.item.GoldenPistolItem;
import com.xie.smfs.network.packets.ui.s2c.OpenHumanSkinPaperEndingS2CPacket;
import com.xie.smfs.registry.ModItems;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancementManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(AdvancementManager.class);
   public static final String BECOME_GOD_ADVANCEMENT = "smfs:become_god";
   public static final String END_GHOST_ERA_ADVANCEMENT = "smfs:end_ghost_era";
   public static final String END_GHOST_ERA_NO_DEATH_ADVANCEMENT = "smfs:end_ghost_era_no_death";
   public static final String TAME_GHOST_ADVANCEMENT = "smfs:tame_ghost";
   public static final String GHOST_HUNTER_ADVANCEMENT = "smfs:ghost_hunter";
   public static final String DUAL_GOLDEN_GUNS_ADVANCEMENT = "smfs:dual_golden_guns";
   public static final String BECOME_ABERRATION_ADVANCEMENT = "smfs:become_aberration";

   public static boolean isGhostEntityType(EntityType<?> entityType) {
      try {
         if (!GhostSpawnManager.getSpawnedGhostTypes().contains(entityType)) {
            return false;
         }

         String entityTypeName = entityType.toString().toLowerCase();
         return !entityTypeName.contains("yezhen")
            && !entityTypeName.contains("fengquan")
            && !entityTypeName.contains("caoyang")
            && !entityTypeName.contains("fangshimin")
            && !entityTypeName.contains("yanli")
            && !entityTypeName.contains("lileping")
            && !entityTypeName.contains("wangxiaoming")
            && !entityTypeName.contains("chen_doctor")
            && !entityTypeName.contains("lijun")
            && !entityTypeName.contains("zhaokaiming")
            && !entityTypeName.contains("npc1")
            && !entityTypeName.contains("npc2")
            && !entityTypeName.contains("npc3")
            && !entityTypeName.contains("npc4")
            && !entityTypeName.contains("npc5")
            && !entityTypeName.contains("npc6")
            && !entityTypeName.contains("ghost_slave")
            && !entityTypeName.contains("player_ghost");
      } catch (Exception e) {
         LOGGER.warn("检查实体类型 {} 是否为鬼实体类型时出错: {}", entityType, e.getMessage());
         return false;
      }
   }

   public static void checkAndUnlockEndGhostEra(ServerPlayerEntity player) {
      if (player != null && player.getServer() != null) {
         Set<EntityType<?>> allGhostTypes = GhostSpawnManager.getSpawnedGhostTypes();
         Set<EntityType<?>> lockedGhostTypes = GhostSpawnManager.getLockedGhostTypes();
         Set<EntityType<?>> filteredGhostTypes = new HashSet<>();

         for (EntityType<?> ghostType : allGhostTypes) {
            if (isGhostEntityType(ghostType)) {
               filteredGhostTypes.add(ghostType);
            }
         }

         LOGGER.debug("成就检查 - 所有鬼类型数量: {}, 锁死鬼类型数量: {}, 过滤后鬼类型数量: {}", allGhostTypes.size(), lockedGhostTypes.size(), filteredGhostTypes.size());
         boolean allGhostsLocked = filteredGhostTypes.isEmpty() || lockedGhostTypes.containsAll(filteredGhostTypes);
         if (allGhostsLocked) {
            unlockAdvancement(player, "smfs:end_ghost_era");
            LOGGER.info("玩家 {} 解锁成就: 终结灵异时代", player.getName().getString());
            player.sendMessage(Text.literal("§6§l恭喜！你解锁了成就：终结灵异时代§r"));
            player.sendMessage(Text.literal("§7将所有厉鬼全部驱逐！§r"));
            int deathCount = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS));
            if (deathCount == 0 && !hasAdvancement(player, "smfs:end_ghost_era_no_death")) {
               unlockAdvancement(player, "smfs:end_ghost_era_no_death");
               LOGGER.info("玩家 {} 解锁成就: 一命终结灵异时代", player.getName().getString());
               player.sendMessage(Text.literal("§6§l恭喜！你解锁了成就：一命终结灵异时代§r"));
               player.sendMessage(Text.literal("§7在未死亡的情况下终结所有灵异！§r"));
            }
         } else {
            int lockedCount = 0;

            for (EntityType<?> ghostType : filteredGhostTypes) {
               if (lockedGhostTypes.contains(ghostType)) {
                  lockedCount++;
               }
            }

            LOGGER.debug("成就进度: {}/{}", lockedCount, filteredGhostTypes.size());
         }
      }
   }

   public static void unlockAdvancement(ServerPlayerEntity player, String advancementId) {
      if (player != null && advancementId != null) {
         try {
            PlayerAdvancementTracker tracker = player.getAdvancementTracker();
            Advancement advancement = player.getServer().getAdvancementLoader().get(new Identifier(advancementId));
            if (advancement != null) {
               AdvancementProgress progress = tracker.getProgress(advancement);
               if (!progress.isDone()) {
                  for (String criterion : progress.getUnobtainedCriteria()) {
                     tracker.grantCriterion(advancement, criterion);
                  }

                  LOGGER.debug("已解锁成就: {}", advancementId);
               }
            } else {
               LOGGER.warn("成就未找到: {}", advancementId);
            }
         } catch (Exception e) {
            LOGGER.error("解锁成就失败: {}", advancementId, e);
         }
      }
   }

   public static boolean hasAdvancement(ServerPlayerEntity player, String advancementId) {
      if (player != null && advancementId != null) {
         try {
            PlayerAdvancementTracker tracker = player.getAdvancementTracker();
            Advancement advancement = player.getServer().getAdvancementLoader().get(new Identifier(advancementId));
            if (advancement != null) {
               return tracker.getProgress(advancement).isDone();
            }
         } catch (Exception e) {
            LOGGER.error("检查成就失败: {}", advancementId, e);
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean hasAnyGodAdvancement(MinecraftServer server) {
      if (server == null) {
         return false;
      }

      for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
         if (hasAdvancement(player, "smfs:become_god")) {
            return true;
         }
      }

      return false;
   }

   public static void checkAndUnlockBecomeGod(ServerPlayerEntity player) {
      if (player != null && player.getServer() != null) {
         if (!hasAdvancement(player, "smfs:become_god")) {
            unlockAdvancement(player, "smfs:become_god");
            inheritGhostChildAttributes(player);
            unlockAdditionalGhostSlots(player);
            upgradeAllGhostSlotsToMaxLevel(player);
            player.sendMessage(Text.literal("§6§l恭喜！你解锁了成就：成神§r"));
            player.sendMessage(Text.literal("§7你与鬼童融合，成为了超越人类的存在！§r"));
            LOGGER.info("玩家 {} 解锁成就: 成神", player.getName().getString());
            checkHumanSkinPaperEnding(player);
         }
      }
   }

   public static void checkHumanSkinPaperEnding(ServerPlayerEntity player) {
      if (player != null) {
         ServerPlayNetworking.send(player, OpenHumanSkinPaperEndingS2CPacket.ID, OpenHumanSkinPaperEndingS2CPacket.create());
      }
   }

   public static boolean hasHumanSkinPaper(ServerPlayerEntity player) {
      if (player == null) {
         return false;
      }

      for (ItemStack stack : player.getInventory().main) {
         if (stack.isOf(ModItems.HUMAN_SKIN_PAPER)) {
            return true;
         }
      }

      for (ItemStack stack : player.getInventory().armor) {
         if (stack.isOf(ModItems.HUMAN_SKIN_PAPER)) {
            return true;
         }
      }

      return player.getOffHandStack().isOf(ModItems.HUMAN_SKIN_PAPER);
   }

   private static void inheritGhostChildAttributes(ServerPlayerEntity player) {
      try {
         GhostChildData ghostChildData = PlayerGhostChildManager.getGhostChildData(player);
         if (ghostChildData != null) {
            int ghostChildMaxSpirit = ghostChildData.getMaxSpiritPower();
            int ghostChildResistance = ghostChildData.getSpiritResistance();
            int ghostChildDamage = ghostChildData.getSpiritDamage();
            PlayerEvents.addSpiritAttribute(player, SpiritAttributes.MAX_SPIRIT, ghostChildMaxSpirit);
            PlayerEvents.addSpiritAttribute(player, SpiritAttributes.SPIRIT_RESISTANCE, ghostChildResistance);
            PlayerEvents.addSpiritAttribute(player, SpiritAttributes.SPIRIT_DAMAGE, ghostChildDamage);
         }
      } catch (Exception e) {
         LOGGER.warn("继承鬼童属性失败: {}", e.getMessage());
      }
   }

   private static void unlockAdditionalGhostSlots(ServerPlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      NbtCompound ghostSlots = data.getCompound("GhostSlots");
      boolean anyChanged = false;

      for (int i = 6; i <= 9; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (!slotData.getBoolean("unlocked")) {
               slotData.putBoolean("unlocked", true);
               ghostSlots.put(slotKey, slotData);
               anyChanged = true;
            }
         }
      }

      if (anyChanged) {
         data.put("GhostSlots", ghostSlots);
         PlayerEvents.setSpiritAttributes(player, data);
         LOGGER.debug("玩家 {} 解锁了额外的鬼槽位（6-9）", player.getName().getString());
      }
   }

   private static void upgradeAllGhostSlotsToMaxLevel(ServerPlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      NbtCompound ghostSlots = data.getCompound("GhostSlots");
      boolean anyChanged = false;

      for (int i = 0; i <= 9; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied") && slotData.getBoolean("unlocked")) {
               int currentLevel = slotData.getInt("level");
               if (currentLevel < 10) {
                  slotData.putInt("level", 10);
                  slotData.putInt("revivalDegree", 0);
                  ghostSlots.put(slotKey, slotData);
                  anyChanged = true;
                  PlayerEvents.recalculateSpiritAttributesForSlot(player, i, currentLevel, 10);
               }
            }
         }
      }

      if (anyChanged) {
         data.put("GhostSlots", ghostSlots);
         PlayerEvents.setSpiritAttributes(player, data);
         LOGGER.info("玩家 {} 成神后所有厉鬼已升至10级，复苏归零", player.getName().getString());
      }
   }

   public static void onGhostTypeLocked(ServerPlayerEntity player, EntityType<?> ghostType) {
      if (player != null && player.getServer() != null) {
         checkAndUnlockEndGhostEra(player);
      }
   }

   public static void checkAndUnlockTameGhost(ServerPlayerEntity player) {
      if (player != null && player.getServer() != null) {
         if (!hasAdvancement(player, "smfs:tame_ghost")) {
            try {
               int tamedCount = PlayerEvents.countOccupiedGhostSlots(player);
               if (tamedCount > 0) {
                  unlockAdvancement(player, "smfs:tame_ghost");
                  player.sendMessage(Text.literal("§6§l恭喜！你解锁了成就：驾驭厉鬼§r"));
                  player.sendMessage(Text.literal("§7你成功驾驭了第一个厉鬼，成为了一名驭鬼者！§r"));
               }
            } catch (Exception e) {
               LOGGER.error("检查驾驭厉鬼成就时出错: {}", e.getMessage(), e);
            }
         }
      }
   }

   public static void checkAndUnlockGhostHunter(ServerPlayerEntity player) {
      if (player != null && player.getServer() != null) {
         if (!hasAdvancement(player, "smfs:ghost_hunter")) {
            unlockAdvancement(player, "smfs:ghost_hunter");
            player.sendMessage(Text.literal("§6§l恭喜！你解锁了成就：大昌市抓鬼人§r"));
            player.sendMessage(Text.literal("§7你成功使用黄金容器捕捉了一个鬼！§r"));
         }
      }
   }

   public static void checkAndUnlockDualGoldenGuns(ServerPlayerEntity player) {
      if (player != null && player.getServer() != null) {
         if (!hasAdvancement(player, "smfs:dual_golden_guns")) {
            try {
               boolean mainHandHasGoldenPistol = player.getMainHandStack().getItem() instanceof GoldenPistolItem;
               boolean offHandHasGoldenPistol = player.getOffHandStack().getItem() instanceof GoldenPistolItem;
               if (mainHandHasGoldenPistol && offHandHasGoldenPistol) {
                  unlockAdvancement(player, "smfs:dual_golden_guns");
                  player.sendMessage(Text.literal("§6§l恭喜！你解锁了成就：双持金枪客§r"));
                  player.sendMessage(Text.literal("§7你感觉自己帅呆了！§r"));
               }
            } catch (Exception e) {
               LOGGER.error("检查双持金枪客成就时出错: {}", e.getMessage(), e);
            }
         }
      }
   }

   public static void checkAllPlayersAdvancements(MinecraftServer server) {
      if (server != null) {
         for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            checkAndUnlockEndGhostEra(player);
            checkAndUnlockTameGhost(player);
            checkAndUnlockDualGoldenGuns(player);
         }
      }
   }
}
