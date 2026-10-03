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
      if (player != null && player.method_5682() != null) {
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
            LOGGER.info("玩家 {} 解锁成就: 终结灵异时代", player.method_5477().getString());
            player.method_43496(Text.method_43470("§6§l恭喜！你解锁了成就：终结灵异时代§r"));
            player.method_43496(Text.method_43470("§7将所有厉鬼全部驱逐！§r"));
            int deathCount = player.method_14248().method_15025(Stats.field_15419.method_14956(Stats.field_15421));
            if (deathCount == 0 && !hasAdvancement(player, "smfs:end_ghost_era_no_death")) {
               unlockAdvancement(player, "smfs:end_ghost_era_no_death");
               LOGGER.info("玩家 {} 解锁成就: 一命终结灵异时代", player.method_5477().getString());
               player.method_43496(Text.method_43470("§6§l恭喜！你解锁了成就：一命终结灵异时代§r"));
               player.method_43496(Text.method_43470("§7在未死亡的情况下终结所有灵异！§r"));
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
            PlayerAdvancementTracker tracker = player.method_14236();
            Advancement advancement = player.method_5682().method_3851().method_12896(new Identifier(advancementId));
            if (advancement != null) {
               AdvancementProgress progress = tracker.method_12882(advancement);
               if (!progress.method_740()) {
                  for (String criterion : progress.method_731()) {
                     tracker.method_12878(advancement, criterion);
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
            PlayerAdvancementTracker tracker = player.method_14236();
            Advancement advancement = player.method_5682().method_3851().method_12896(new Identifier(advancementId));
            if (advancement != null) {
               return tracker.method_12882(advancement).method_740();
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

      for (ServerPlayerEntity player : server.method_3760().method_14571()) {
         if (hasAdvancement(player, "smfs:become_god")) {
            return true;
         }
      }

      return false;
   }

   public static void checkAndUnlockBecomeGod(ServerPlayerEntity player) {
      if (player != null && player.method_5682() != null) {
         if (!hasAdvancement(player, "smfs:become_god")) {
            unlockAdvancement(player, "smfs:become_god");
            inheritGhostChildAttributes(player);
            unlockAdditionalGhostSlots(player);
            upgradeAllGhostSlotsToMaxLevel(player);
            player.method_43496(Text.method_43470("§6§l恭喜！你解锁了成就：成神§r"));
            player.method_43496(Text.method_43470("§7你与鬼童融合，成为了超越人类的存在！§r"));
            LOGGER.info("玩家 {} 解锁成就: 成神", player.method_5477().getString());
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

      for (ItemStack stack : player.method_31548().field_7547) {
         if (stack.method_31574(ModItems.HUMAN_SKIN_PAPER)) {
            return true;
         }
      }

      for (ItemStack stack : player.method_31548().field_7548) {
         if (stack.method_31574(ModItems.HUMAN_SKIN_PAPER)) {
            return true;
         }
      }

      return player.method_6079().method_31574(ModItems.HUMAN_SKIN_PAPER);
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
      NbtCompound ghostSlots = data.method_10562("GhostSlots");
      boolean anyChanged = false;

      for (int i = 6; i <= 9; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.method_10545(slotKey)) {
            NbtCompound slotData = ghostSlots.method_10562(slotKey);
            if (!slotData.method_10577("unlocked")) {
               slotData.method_10556("unlocked", true);
               ghostSlots.method_10566(slotKey, slotData);
               anyChanged = true;
            }
         }
      }

      if (anyChanged) {
         data.method_10566("GhostSlots", ghostSlots);
         PlayerEvents.setSpiritAttributes(player, data);
         LOGGER.debug("玩家 {} 解锁了额外的鬼槽位（6-9）", player.method_5477().getString());
      }
   }

   private static void upgradeAllGhostSlotsToMaxLevel(ServerPlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      NbtCompound ghostSlots = data.method_10562("GhostSlots");
      boolean anyChanged = false;

      for (int i = 0; i <= 9; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.method_10545(slotKey)) {
            NbtCompound slotData = ghostSlots.method_10562(slotKey);
            if (slotData.method_10577("occupied") && slotData.method_10577("unlocked")) {
               int currentLevel = slotData.method_10550("level");
               if (currentLevel < 10) {
                  slotData.method_10569("level", 10);
                  slotData.method_10569("revivalDegree", 0);
                  ghostSlots.method_10566(slotKey, slotData);
                  anyChanged = true;
                  PlayerEvents.recalculateSpiritAttributesForSlot(player, i, currentLevel, 10);
               }
            }
         }
      }

      if (anyChanged) {
         data.method_10566("GhostSlots", ghostSlots);
         PlayerEvents.setSpiritAttributes(player, data);
         LOGGER.info("玩家 {} 成神后所有厉鬼已升至10级，复苏归零", player.method_5477().getString());
      }
   }

   public static void onGhostTypeLocked(ServerPlayerEntity player, EntityType<?> ghostType) {
      if (player != null && player.method_5682() != null) {
         checkAndUnlockEndGhostEra(player);
      }
   }

   public static void checkAndUnlockTameGhost(ServerPlayerEntity player) {
      if (player != null && player.method_5682() != null) {
         if (!hasAdvancement(player, "smfs:tame_ghost")) {
            try {
               int tamedCount = PlayerEvents.countOccupiedGhostSlots(player);
               if (tamedCount > 0) {
                  unlockAdvancement(player, "smfs:tame_ghost");
                  player.method_43496(Text.method_43470("§6§l恭喜！你解锁了成就：驾驭厉鬼§r"));
                  player.method_43496(Text.method_43470("§7你成功驾驭了第一个厉鬼，成为了一名驭鬼者！§r"));
               }
            } catch (Exception e) {
               LOGGER.error("检查驾驭厉鬼成就时出错: {}", e.getMessage(), e);
            }
         }
      }
   }

   public static void checkAndUnlockGhostHunter(ServerPlayerEntity player) {
      if (player != null && player.method_5682() != null) {
         if (!hasAdvancement(player, "smfs:ghost_hunter")) {
            unlockAdvancement(player, "smfs:ghost_hunter");
            player.method_43496(Text.method_43470("§6§l恭喜！你解锁了成就：大昌市抓鬼人§r"));
            player.method_43496(Text.method_43470("§7你成功使用黄金容器捕捉了一个鬼！§r"));
         }
      }
   }

   public static void checkAndUnlockDualGoldenGuns(ServerPlayerEntity player) {
      if (player != null && player.method_5682() != null) {
         if (!hasAdvancement(player, "smfs:dual_golden_guns")) {
            try {
               boolean mainHandHasGoldenPistol = player.method_6047().method_7909() instanceof GoldenPistolItem;
               boolean offHandHasGoldenPistol = player.method_6079().method_7909() instanceof GoldenPistolItem;
               if (mainHandHasGoldenPistol && offHandHasGoldenPistol) {
                  unlockAdvancement(player, "smfs:dual_golden_guns");
                  player.method_43496(Text.method_43470("§6§l恭喜！你解锁了成就：双持金枪客§r"));
                  player.method_43496(Text.method_43470("§7你感觉自己帅呆了！§r"));
               }
            } catch (Exception e) {
               LOGGER.error("检查双持金枪客成就时出错: {}", e.getMessage(), e);
            }
         }
      }
   }

   public static void checkAllPlayersAdvancements(MinecraftServer server) {
      if (server != null) {
         for (ServerPlayerEntity player : server.method_3760().method_14571()) {
            checkAndUnlockEndGhostEra(player);
            checkAndUnlockTameGhost(player);
            checkAndUnlockDualGoldenGuns(player);
         }
      }
   }
}
