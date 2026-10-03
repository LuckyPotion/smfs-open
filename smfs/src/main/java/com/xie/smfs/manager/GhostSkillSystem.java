package com.xie.smfs.manager;

import com.xie.smfs.block.GhostLakeBlock;
import com.xie.smfs.client.util.GhostShadowHeadCameraManager;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.item.BaseGhostEyeItem;
import com.xie.smfs.network.ClientModNetwork;
import com.xie.smfs.network.packets.skills.c2s.ClientBlockGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientBlockGhostVSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientBoxGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientCandyGhostJSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientClothesGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientConsumeShardC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientCropGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientCryingGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientDeathSightGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientDitouGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientDoorGhostJSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientFogGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientFoodGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientFoodGhostVSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientFuneralMusicGhostGSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientFuneralMusicGhostNSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientFuneralMusicGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientFuneralMusicGhostVSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGanshiBrideGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostBloodSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDomainFireIgniteAllC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDomainFireIgniteBlocksC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDomainFireIgniteC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDomainMoveEntityC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDomainRemoveBuffsC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDomainTeleportC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDreamSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDreamVSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostEyeJSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostFireJSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostLakeGSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostLakeNSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostLakeSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostLakeVSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostMerchantSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostOfficerGSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostOfficerRestartC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostOfficerSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostOfficerVSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostPressureSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostShadowHeadJSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostWindSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGiantShadowGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGongGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGraveEarthGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientJumpGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientLostGhostBlindC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientLostGhostRestartC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientLostGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientLostGhostSlowC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientMineralGhostJSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientPlagueGhostJSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientPuppetGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientQiaomenGhostGSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientQiaomenGhostNSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientQiaomenGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientQiaomenGhostVSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientScapegoatGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientShadowGhostJSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientSilentGhostJSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientSneakGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientStepGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientSuonaGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientTaitouGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientTrashGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientVillagerGhostGSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientVillagerGhostNSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientVillagerGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientVillagerGhostVSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientWaterGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientWishGhostGSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientWishGhostJSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientWishGhostNSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientWishGhostVSkillC2SPacket;
import com.xie.smfs.registry.ModEffects;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostSkillSystem {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostSkillSystem");
   public static final String SKILL_TYPE_GHOST_EYE = "silent_ghost_eye";
   public static final String SKILL_TYPE_GHOST_FIRE = "ghost_fire";
   public static final String SKILL_TYPE_TAITOU_GHOST = "taitou_ghost";
   public static final String SKILL_TYPE_DITOU_GHOST = "ditou_ghost";
   public static final String SKILL_TYPE_BOX_GHOST = "box_ghost";
   public static final String SKILL_TYPE_DEATH_SIGHT_GHOST = "death_sight_ghost";
   public static final String SKILL_TYPE_GHOST_MERCHANT = "ghost_merchant";
   public static final String SKILL_TYPE_JUMP_GHOST = "jump_ghost";
   public static final String SKILL_TYPE_FOG_GHOST = "fog_ghost";
   public static final String SKILL_TYPE_BLOCK_GHOST = "block_ghost";
   public static final String SKILL_TYPE_FOOD_GHOST = "food_ghost";
   public static final String SKILL_TYPE_VILLAGER_GHOST = "villager_ghost";
   public static final String SKILL_TYPE_LOST_GHOST = "lost_ghost";
   public static final String SKILL_TYPE_QIAOMEN_GHOST = "qiaomen_ghost";
   public static final String SKILL_TYPE_CROP_GHOST = "crop_ghost";
   public static final String SKILL_TYPE_STEP_GHOST = "step_ghost";
   public static final String SKILL_TYPE_GRAVE_EARTH_GHOST = "grave_earth_ghost";
   public static final String SKILL_TYPE_GHOST_LAKE = "ghost_lake";
   public static final String SKILL_TYPE_TRASH_GHOST = "trash_ghost";
   public static final String SKILL_TYPE_WATER_GHOST = "water_ghost";
   public static final String SKILL_TYPE_GIANT_SHADOW_GHOST = "giant_shadow_ghost";
   public static final String SKILL_TYPE_GANSHI_BRIDE_GHOST = "ganshi_bride_ghost";
   public static final String SKILL_TYPE_CRYING_GHOST = "crying_ghost";
   public static final String SKILL_TYPE_SUONA_GHOST = "suona_ghost";
   public static final String SKILL_TYPE_GONG_GHOST = "gong_ghost";
   public static final String SKILL_TYPE_GHOST_PRESSURE = "ghost_pressure";
   public static final String SKILL_TYPE_GHOST_WIND = "ghost_wind";
   public static final String SKILL_TYPE_GHOST_BLOOD = "ghost_blood";
   public static final String SKILL_TYPE_WISH_GHOST = "wish_ghost";
   public static final String SKILL_TYPE_SCAPEGOAT_GHOST = "scapegoat_ghost";
   public static final String SKILL_TYPE_CANDY_GHOST = "candy_ghost";
   public static final String SKILL_TYPE_SILENT_GHOST = "silent_ghost";
   public static final String SKILL_TYPE_GHOST_OFFICER = "ghost_officer";
   public static final String SKILL_TYPE_PLAGUE_GHOST = "plague_ghost";
   public static final String SKILL_TYPE_MINERAL_GHOST = "mineral_ghost";
   public static final String SKILL_TYPE_SHADOW_GHOST = "shadow_ghost";
   public static final String SKILL_TYPE_COMPLETE_SHADOW_GHOST = "complete_shadow_ghost";
   public static final String SKILL_TYPE_DOOR_GHOST = "door_ghost";
   public static final String SKILL_TYPE_SNEAK_GHOST = "sneak_ghost";
   public static final String SKILL_TYPE_CLOTHES_GHOST = "clothes_ghost";
   public static final String SKILL_TYPE_PUPPET_GHOST = "puppet_ghost";
   public static final String SKILL_TYPE_FUNERAL_MUSIC_GHOST = "funeral_music_ghost";
   public static final String SKILL_TYPE_GHOST_DREAM = "ghost_dream";
   public static final String SKILL_TYPE_GHOST_SHADOW_HEAD = "ghost_shadow_head";
   private static final Map<String, Runnable> nSkillHandlerMap = new HashMap<>();
   private static final Map<String, Runnable> gSkillHandlerMap = new HashMap<>();
   private static final Map<String, Runnable> vSkillHandlerMap = new HashMap<>();
   private static final Map<String, Runnable> jSkillHandlerMap = new HashMap<>();

   private static float getAberrationCooldownMultiplier(PlayerEntity player) {
      return player instanceof ServerPlayerEntity serverPlayer && AdvancementManager.hasAdvancement(serverPlayer, "smfs:become_aberration") ? 0.75F : 1.0F;
   }

   public static void handleNKeySkill(PlayerEntity player) {
      String currentSkillType = getCurrentSkillType(player);
      if (player.method_6059(ModEffects.SILENCE) && !currentSkillType.equals("funeral_music_ghost")) {
         player.method_7353(Text.method_43470("§c体内的鬼陷入沉寂"), true);
      } else if (player.method_6059(ModEffects.DREAM)) {
         player.method_7353(Text.method_43470("§c体内的鬼陷入沉寂"), true);
      } else if (GhostDomainManager.isSkillOnCooldown(player, "ngv_shared_cooldown")) {
         long remainingTicks = GhostDomainManager.getSkillCooldownRemaining(player, "ngv_shared_cooldown");
         double remainingSeconds = remainingTicks / 20.0;
         player.method_7353(Text.method_43470("§c技能正在冷却中，剩余时间：" + String.format("%.2f", remainingSeconds) + "秒"), true);
      } else if (isHoldingShardItem(player)
         || currentSkillType.equals("lost_ghost")
         || currentSkillType.equals("wish_ghost")
         || currentSkillType.equals("funeral_music_ghost")
         || currentSkillType.equals("ghost_lake")
         || currentSkillType.equals("ghost_shadow_head")
         || currentSkillType.equals("complete_shadow_ghost")
         || GhostDomainManager.isGhostDomainActive(player)) {
         handleDefaultNSkill(currentSkillType, player);
         if (isHoldingShardItem(player)) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.method_10814(currentSkillType);
            ClientPlayNetworking.send(ClientConsumeShardC2SPacket.ID, buf);
         }

         float multiplier = getAberrationCooldownMultiplier(player);
         int cooldownTicks = Math.round(60.0F * multiplier);
         GhostDomainManager.setSkillCooldown(player, "ngv_shared_cooldown", cooldownTicks);
         LOGGER.debug("玩家 {} 使用N键技能，设置NGV共享冷却{}秒", player.method_5477().getString(), cooldownTicks / 20.0);
      }
   }

   public static void handleGKeySkill(PlayerEntity player) {
      String currentSkillType = getCurrentSkillType(player);
      if (player.method_6059(ModEffects.SILENCE) && !currentSkillType.equals("funeral_music_ghost")) {
         player.method_7353(Text.method_43470("§c体内的鬼陷入沉寂"), true);
      } else if (player.method_6059(ModEffects.DREAM)) {
         player.method_7353(Text.method_43470("§c体内的鬼陷入沉寂"), true);
      } else {
         if (currentSkillType.equals("funeral_music_ghost")) {
            if (GhostDomainManager.isSkillOnCooldown(player, "funeral_music_ghost_g_skill")) {
               long remainingTicks = GhostDomainManager.getSkillCooldownRemaining(player, "funeral_music_ghost_g_skill");
               double remainingSeconds = remainingTicks / 20.0;
               player.method_7353(Text.method_43470("§c技能正在冷却中，剩余时间：" + String.format("%.1f", remainingSeconds) + "秒"), true);
               LOGGER.warn("玩家 {} 尝试使用丧乐鬼G键技能但冷却中，剩余时间：{}秒", player.method_5477().getString(), String.format("%.1f", remainingSeconds));
               return;
            }
         } else if (GhostDomainManager.isSkillOnCooldown(player, "ngv_shared_cooldown")) {
            long remainingTicks = GhostDomainManager.getSkillCooldownRemaining(player, "ngv_shared_cooldown");
            double remainingSeconds = remainingTicks / 20.0;
            player.method_7353(Text.method_43470("§c技能正在冷却中，剩余时间：" + String.format("%.1f", remainingSeconds) + "秒"), true);
            return;
         }

         if (isHoldingShardItem(player)
            || currentSkillType.equals("lost_ghost")
            || currentSkillType.equals("wish_ghost")
            || currentSkillType.equals("funeral_music_ghost")
            || currentSkillType.equals("ghost_lake")
            || currentSkillType.equals("ghost_shadow_head")
            || currentSkillType.equals("complete_shadow_ghost")
            || GhostDomainManager.isGhostDomainActive(player)) {
            handleDefaultGSkill(currentSkillType, player);
            if (isHoldingShardItem(player)) {
               PacketByteBuf buf = PacketByteBufs.create();
               buf.method_10814(currentSkillType);
               ClientPlayNetworking.send(ClientConsumeShardC2SPacket.ID, buf);
            }

            float multiplier = getAberrationCooldownMultiplier(player);
            if (currentSkillType.equals("funeral_music_ghost")) {
               int cooldownTicks = Math.round(100.0F * multiplier);
               GhostDomainManager.setSkillCooldown(player, "funeral_music_ghost_g_skill", cooldownTicks);
               LOGGER.debug("玩家 {} 使用丧乐鬼G键技能，设置单独冷却{}秒", player.method_5477().getString(), cooldownTicks / 20.0);
            } else {
               int cooldownTicks = Math.round(60.0F * multiplier);
               GhostDomainManager.setSkillCooldown(player, "ngv_shared_cooldown", cooldownTicks);
               LOGGER.debug("玩家 {} 使用G键技能，设置NGV共享冷却{}秒", player.method_5477().getString(), cooldownTicks / 20.0);
            }
         }
      }
   }

   public static void handleVKeySkill(PlayerEntity player) {
      String currentSkillType = getCurrentSkillType(player);
      if (player.method_6059(ModEffects.SILENCE) && !currentSkillType.equals("funeral_music_ghost")) {
         player.method_7353(Text.method_43470("§c体内的鬼陷入沉寂"), true);
      } else if (player.method_6059(ModEffects.DREAM) && !currentSkillType.equals("ghost_dream")) {
         player.method_7353(Text.method_43470("§c体内的鬼陷入沉寂"), true);
      } else {
         if (currentSkillType.equals("funeral_music_ghost")) {
            if (GhostDomainManager.isSkillOnCooldown(player, "funeral_music_ghost_v_skill")) {
               long remainingTicks = GhostDomainManager.getSkillCooldownRemaining(player, "funeral_music_ghost_v_skill");
               double remainingSeconds = remainingTicks / 20.0;
               player.method_7353(Text.method_43470("§c技能正在冷却中，剩余时间：" + String.format("%.1f", remainingSeconds) + "秒"), true);
               LOGGER.warn("玩家 {} 尝试使用丧乐鬼V键技能但冷却中，剩余时间：{}秒", player.method_5477().getString(), String.format("%.1f", remainingSeconds));
               return;
            }
         } else if (GhostDomainManager.isSkillOnCooldown(player, "ngv_shared_cooldown")) {
            long remainingTicks = GhostDomainManager.getSkillCooldownRemaining(player, "ngv_shared_cooldown");
            double remainingSeconds = remainingTicks / 20.0;
            player.method_7353(Text.method_43470("§c技能正在冷却中，剩余时间：" + String.format("%.1f", remainingSeconds) + "秒"), true);
            return;
         }

         if (isHoldingShardItem(player)
            || currentSkillType.equals("lost_ghost")
            || currentSkillType.equals("wish_ghost")
            || currentSkillType.equals("funeral_music_ghost")
            || currentSkillType.equals("ghost_lake")
            || currentSkillType.equals("ghost_dream")
            || currentSkillType.equals("ghost_shadow_head")
            || currentSkillType.equals("complete_shadow_ghost")
            || GhostDomainManager.isGhostDomainActive(player)) {
            handleDefaultVSkill(currentSkillType, player);
            if (isHoldingShardItem(player)) {
               PacketByteBuf buf = PacketByteBufs.create();
               buf.method_10814(currentSkillType);
               ClientPlayNetworking.send(ClientConsumeShardC2SPacket.ID, buf);
            }

            float multiplier = getAberrationCooldownMultiplier(player);
            if (currentSkillType.equals("funeral_music_ghost")) {
               int cooldownTicks = Math.round(100.0F * multiplier);
               GhostDomainManager.setSkillCooldown(player, "funeral_music_ghost_v_skill", cooldownTicks);
               LOGGER.debug("玩家 {} 使用丧乐鬼V键技能，设置单独冷却{}秒", player.method_5477().getString(), cooldownTicks / 20.0);
            } else {
               int cooldownTicks = Math.round(60.0F * multiplier);
               GhostDomainManager.setSkillCooldown(player, "ngv_shared_cooldown", cooldownTicks);
               LOGGER.debug("玩家 {} 使用V键技能，设置NGV共享冷却{}秒", player.method_5477().getString(), cooldownTicks / 20.0);
            }
         }
      }
   }

   public static void handleJKeySkill(PlayerEntity player) {
      String currentSkillType = getCurrentSkillType(player);
      if (player.method_6059(ModEffects.SILENCE) && !currentSkillType.equals("funeral_music_ghost")) {
         player.method_7353(Text.method_43470("§c体内的鬼陷入沉寂"), true);
      } else if (player.method_6059(ModEffects.DREAM)) {
         player.method_7353(Text.method_43470("§c体内的鬼陷入沉寂"), true);
      } else if (MainGhostManager.hasMainGhost(player) || isHoldingShardItem(player)) {
         String skillType = getCurrentSkillType(player);
         handleDefaultJSkill(skillType, player);
         if (isHoldingShardItem(player)) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.method_10814(skillType);
            ClientPlayNetworking.send(ClientConsumeShardC2SPacket.ID, buf);
         }
      }
   }

   private static boolean isHoldingShardItem(PlayerEntity player) {
      for (Hand hand : Hand.values()) {
         ItemStack stack = player.method_5998(hand);
         if (stack.method_7909() instanceof BaseGhostEyeItem && BaseGhostEyeItem.isShard(stack)) {
            return true;
         }
      }

      return false;
   }

   private static String getCurrentSkillType(PlayerEntity player) {
      for (Hand hand : Hand.values()) {
         ItemStack heldStack = player.method_5998(hand);
         if (heldStack.method_7909() instanceof BaseGhostEyeItem item && BaseGhostEyeItem.isShard(heldStack)) {
            return item.getGhostType();
         }
      }

      if (MainGhostManager.hasMainGhost(player)) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         String ghostType = PlayerEvents.getGhostTypeInSlot(player, mainSlot);
         if (ghostType != null && !ghostType.isEmpty()) {
            return ghostType;
         }

         ItemStack mainGhostItem = PlayerEvents.getGhostSlotItem(player, mainSlot);
         if (!mainGhostItem.method_7960()) {
            String itemId = Registries.field_41178.method_10221(mainGhostItem.method_7909()).method_12832();
            LOGGER.debug("通过物品ID获取技能类型: {}", itemId);
            return itemId;
         }
      }

      return "silent_ghost_eye";
   }

   private static void handleGhostEyeNSkill(PlayerEntity player) {
      ClientPlayNetworking.send(ClientGhostDomainRemoveBuffsC2SPacket.ID, PacketByteBufs.empty());
   }

   private static void handleGhostFireNSkill(PlayerEntity player) {
      ClientPlayNetworking.send(ClientGhostDomainFireIgniteBlocksC2SPacket.ID, PacketByteBufs.empty());
   }

   private static void handleLostGhostNSkill(PlayerEntity player) {
      if (MainGhostManager.hasMainGhost(player)) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int currentLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         if (currentLevel < 7) {
            LOGGER.warn("玩家 {} 尝试使用遗忘鬼N键技能但等级不足: {}", player.method_5477().getString(), currentLevel);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c复苏程度不足"), true);
            }
         } else {
            ClientPlayNetworking.send(ClientLostGhostRestartC2SPacket.ID, PacketByteBufs.empty());
         }
      }
   }

   private static void handleGhostOfficerNSkill(PlayerEntity player) {
      if (MainGhostManager.hasMainGhost(player)) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int rawLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         int currentLevel = GhostDomainManager.getEffectiveSkillLevel(player, rawLevel);
         if (currentLevel < 7) {
            LOGGER.warn("玩家 {} 尝试使用鬼差N键技能但等级不足: {}", player.method_5477().getString(), currentLevel);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470(GhostDomainManager.getInsufficientLevelMessage(player)), true);
            }
         } else {
            ClientPlayNetworking.send(ClientGhostOfficerRestartC2SPacket.ID, PacketByteBufs.empty());
         }
      }
   }

   private static void handleQiaomenGhostNSkill(PlayerEntity player) {
      if (MainGhostManager.hasMainGhost(player)) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int rawLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         int currentLevel = GhostDomainManager.getEffectiveSkillLevel(player, rawLevel);
         if (currentLevel < 7) {
            LOGGER.warn("玩家 {} 尝试使用敲门鬼N键技能但等级不足: {}", player.method_5477().getString(), currentLevel);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470(GhostDomainManager.getInsufficientLevelMessage(player)), true);
            }
         } else {
            ClientPlayNetworking.send(ClientQiaomenGhostNSkillC2SPacket.ID, PacketByteBufs.empty());
         }
      }
   }

   private static void handleVillagerGhostNSkill(PlayerEntity player) {
      ClientPlayNetworking.send(ClientVillagerGhostNSkillC2SPacket.ID, PacketByteBufs.empty());
   }

   private static void handleVillagerGhostGSkill(PlayerEntity player) {
      ClientPlayNetworking.send(ClientVillagerGhostGSkillC2SPacket.ID, PacketByteBufs.empty());
   }

   private static void handleVillagerGhostVSkill(PlayerEntity player) {
      ClientPlayNetworking.send(ClientVillagerGhostVSkillC2SPacket.ID, PacketByteBufs.empty());
   }

   private static void handleOtherGhostNSkill(PlayerEntity player) {
   }

   private static void handleGhostEyeGSkill(PlayerEntity player) {
      ClientPlayNetworking.send(ClientGhostDomainMoveEntityC2SPacket.ID, PacketByteBufs.empty());
   }

   private static void handleGhostFireGSkill(PlayerEntity player) {
      ClientPlayNetworking.send(ClientGhostDomainFireIgniteAllC2SPacket.ID, PacketByteBufs.empty());
   }

   private static void handleLostGhostGSkill(PlayerEntity player) {
      if (MainGhostManager.hasMainGhost(player)) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int currentLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         if (currentLevel < 3) {
            LOGGER.warn("玩家 {} 尝试使用遗忘鬼G键技能但等级不足: {}", player.method_5477().getString(), currentLevel);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c复苏程度不足"), true);
            }
         } else {
            ClientPlayNetworking.send(ClientLostGhostBlindC2SPacket.ID, PacketByteBufs.empty());
         }
      }
   }

   private static void handleQiaomenGhostGSkill(PlayerEntity player) {
      if (MainGhostManager.hasMainGhost(player)) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int rawLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         int currentLevel = GhostDomainManager.getEffectiveSkillLevel(player, rawLevel);
         if (currentLevel < 4) {
            LOGGER.warn("玩家 {} 尝试使用敲门鬼G键技能但等级不足: {}", player.method_5477().getString(), currentLevel);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470(GhostDomainManager.getInsufficientLevelMessage(player)), true);
            }
         } else {
            LOGGER.debug("玩家 {} 执行敲门鬼G键技能：生成门圈", player.method_5477().getString());
            ClientPlayNetworking.send(ClientQiaomenGhostGSkillC2SPacket.ID, PacketByteBufs.empty());
         }
      }
   }

   private static void handleOtherGhostGSkill(PlayerEntity player) {
      LOGGER.debug("玩家 {} 执行其他鬼的G键技能", player.method_5477().getString());
   }

   private static void handleGhostEyeVSkill(PlayerEntity player) {
      LOGGER.debug("玩家 {} 执行鬼眼V键技能：鬼域瞬移", player.method_5477().getString());
      ClientPlayNetworking.send(ClientGhostDomainTeleportC2SPacket.ID, PacketByteBufs.empty());
   }

   private static void handleGhostFireVSkill(PlayerEntity player) {
      LOGGER.debug("玩家 {} 执行鬼火V键技能：点燃生物", player.method_5477().getString());
      ClientModNetwork.sendToServer(new ClientGhostDomainFireIgniteC2SPacket());
   }

   private static void handleLostGhostVSkill(PlayerEntity player) {
      if (MainGhostManager.hasMainGhost(player)) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int currentLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         if (currentLevel < 2) {
            LOGGER.warn("玩家 {} 尝试使用遗忘鬼V键技能但等级不足: {}", player.method_5477().getString(), currentLevel);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c复苏程度不足"), true);
            }
         } else {
            LOGGER.debug("玩家 {} 执行遗忘鬼V键技能：遗忘·肆", player.method_5477().getString());
            ClientPlayNetworking.send(ClientLostGhostSlowC2SPacket.ID, PacketByteBufs.empty());
         }
      }
   }

   private static void handleQiaomenGhostVSkill(PlayerEntity player) {
      if (MainGhostManager.hasMainGhost(player)) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int rawLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         int currentLevel = GhostDomainManager.getEffectiveSkillLevel(player, rawLevel);
         if (currentLevel < 2) {
            LOGGER.warn("玩家 {} 尝试使用敲门鬼V键技能但等级不足: {}", player.method_5477().getString(), currentLevel);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470(GhostDomainManager.getInsufficientLevelMessage(player)), true);
            }
         } else {
            LOGGER.debug("玩家 {} 执行敲门鬼V键技能：生成门", player.method_5477().getString());
            ClientPlayNetworking.send(ClientQiaomenGhostVSkillC2SPacket.ID, PacketByteBufs.empty());
         }
      }
   }

   private static void handleFoodGhostVSkill(PlayerEntity player) {
      if (MainGhostManager.hasMainGhost(player)) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int rawLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         int currentLevel = GhostDomainManager.getEffectiveSkillLevel(player, rawLevel);
         if (currentLevel < 3) {
            LOGGER.warn("玩家 {} 尝试使用食物鬼V键技能但等级不足: {}", player.method_5477().getString(), currentLevel);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470(GhostDomainManager.getInsufficientLevelMessage(player)), true);
            }
         } else {
            LOGGER.debug("玩家 {} 执行食物鬼V键技能：饥荒", player.method_5477().getString());
            ClientPlayNetworking.send(ClientFoodGhostVSkillC2SPacket.ID, PacketByteBufs.empty());
         }
      }
   }

   private static void handleBlockGhostVSkill(PlayerEntity player) {
      if (MainGhostManager.hasMainGhost(player)) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int rawLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         int currentLevel = GhostDomainManager.getEffectiveSkillLevel(player, rawLevel);
         if (currentLevel < 2) {
            LOGGER.warn("玩家 {} 尝试使用方块鬼V键技能但等级不足: {}", player.method_5477().getString(), currentLevel);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470(GhostDomainManager.getInsufficientLevelMessage(player)), true);
            }
         } else {
            LOGGER.debug("玩家 {} 执行方块鬼V键技能：受到4点伤害后随机获得一种颜色的混凝土方块16个", player.method_5477().getString());
            ClientPlayNetworking.send(ClientBlockGhostVSkillC2SPacket.ID, PacketByteBufs.empty());
         }
      }
   }

   private static void handleOtherGhostVSkill(PlayerEntity player) {
      LOGGER.debug("玩家 {} 执行其他鬼的V键技能", player.method_5477().getString());
   }

   private static void handleGhostLakeNSkill(PlayerEntity player) {
      if (MainGhostManager.hasMainGhost(player)) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int currentLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         if (currentLevel < 2) {
            LOGGER.warn("玩家 {} 尝试使用鬼湖N键技能但等级不足: {}", player.method_5477().getString(), currentLevel);
            player.method_7353(Text.method_43470("§c复苏程度不足"), true);
         } else if (!isPlayerInGhostLake(player)) {
            player.method_7353(Text.method_43470("§c需要在鬼湖中才能释放此技能"), true);
         } else {
            ClientPlayNetworking.send(ClientGhostLakeNSkillC2SPacket.ID, PacketByteBufs.empty());
         }
      }
   }

   private static void handleGhostLakeGSkill(PlayerEntity player) {
      if (MainGhostManager.hasMainGhost(player)) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int currentLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         if (currentLevel < 5) {
            LOGGER.warn("玩家 {} 尝试使用鬼湖G键技能但等级不足: {}", player.method_5477().getString(), currentLevel);
            player.method_7353(Text.method_43470("§c复苏程度不足"), true);
         } else {
            ClientPlayNetworking.send(ClientGhostLakeGSkillC2SPacket.ID, PacketByteBufs.empty());
         }
      }
   }

   private static void handleGhostLakeVSkill(PlayerEntity player) {
      if (MainGhostManager.hasMainGhost(player)) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int currentLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         if (currentLevel < 7) {
            LOGGER.warn("玩家 {} 尝试使用鬼湖V键技能但等级不足: {}", player.method_5477().getString(), currentLevel);
            player.method_7353(Text.method_43470("§c复苏程度不足"), true);
         } else if (!isPlayerInGhostLake(player)) {
            player.method_7353(Text.method_43470("§c需要在鬼湖中才能释放此技能"), true);
         } else {
            ClientPlayNetworking.send(ClientGhostLakeVSkillC2SPacket.ID, PacketByteBufs.empty());
         }
      }
   }

   private static void handleGhostDreamVSkill(PlayerEntity player) {
      if (MainGhostManager.hasMainGhost(player)) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int currentLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         if (currentLevel < 5) {
            LOGGER.warn("玩家 {} 尝试使用鬼梦V键技能但等级不足: {}", player.method_5477().getString(), currentLevel);
            player.method_7353(Text.method_43470("§c复苏程度不足"), true);
         } else {
            LOGGER.debug("玩家 {} 执行鬼梦V键技能：进入/离开鬼梦维度", player.method_5477().getString());
            ClientPlayNetworking.send(ClientGhostDreamVSkillC2SPacket.ID, PacketByteBufs.empty());
         }
      }
   }

   private static boolean isPlayerInGhostLake(PlayerEntity player) {
      if (player != null && player.method_5805()) {
         World world = player.method_37908();
         BlockPos pos = player.method_24515();
         BlockState blockState = world.method_8320(pos);
         BlockState bodyState = world.method_8320(pos.method_10084());
         return blockState.method_26204() instanceof GhostLakeBlock || bodyState.method_26204() instanceof GhostLakeBlock;
      } else {
         return false;
      }
   }

   private static void handleWishGhostNSkill(PlayerEntity player) {
      LOGGER.debug("玩家 {} 执行许愿鬼N键技能：给周围鬼蜮内所有鬼添加1分钟沉寂效果", player.method_5477().getString());
      ClientPlayNetworking.send(ClientWishGhostNSkillC2SPacket.ID, PacketByteBufs.empty());
   }

   private static void handleWishGhostGSkill(PlayerEntity player) {
      LOGGER.debug("玩家 {} 执行许愿鬼G键技能：给自己30秒抗性5和力量255", player.method_5477().getString());
      ClientPlayNetworking.send(ClientWishGhostGSkillC2SPacket.ID, PacketByteBufs.empty());
   }

   private static void handleWishGhostVSkill(PlayerEntity player) {
      LOGGER.debug("玩家 {} 执行许愿鬼V键技能：清除所有buff并传送到安全地方", player.method_5477().getString());
      ClientPlayNetworking.send(ClientWishGhostVSkillC2SPacket.ID, PacketByteBufs.empty());
   }

   public static void registerNSkillHandler(String skillType, Runnable handler) {
      nSkillHandlerMap.put(skillType, handler);
      LOGGER.debug("已注册N键技能处理：{}", skillType);
   }

   public static void registerGSkillHandler(String skillType, Runnable handler) {
      gSkillHandlerMap.put(skillType, handler);
      LOGGER.debug("已注册G键技能处理：{}", skillType);
   }

   public static void registerVSkillHandler(String skillType, Runnable handler) {
      vSkillHandlerMap.put(skillType, handler);
      LOGGER.debug("已注册V键技能处理：{}", skillType);
   }

   public static void registerJSkillHandler(String skillType, Runnable handler) {
      jSkillHandlerMap.put(skillType, handler);
      LOGGER.debug("已注册J键技能处理：{}", skillType);
   }

   private static void handleDefaultNSkill(String skillType, PlayerEntity player) {
      Runnable customHandler = nSkillHandlerMap.get(skillType);
      if (customHandler != null) {
         LOGGER.debug("玩家 {} 使用附属模组注册的N键技能处理：{}", player.method_5477().getString(), skillType);
         customHandler.run();
      } else {
         Map<String, Runnable> nSkillHandlers = new HashMap<>();
         nSkillHandlers.put("silent_ghost_eye", () -> handleGhostEyeNSkill(player));
         nSkillHandlers.put("ghost_fire", () -> handleGhostFireNSkill(player));
         nSkillHandlers.put("lost_ghost", () -> handleLostGhostNSkill(player));
         nSkillHandlers.put("ghost_lake", () -> handleGhostLakeNSkill(player));
         nSkillHandlers.put("qiaomen_ghost", () -> handleQiaomenGhostNSkill(player));
         nSkillHandlers.put("villager_ghost", () -> handleVillagerGhostNSkill(player));
         nSkillHandlers.put("wish_ghost", () -> handleWishGhostNSkill(player));
         nSkillHandlers.put("ghost_officer", () -> handleGhostOfficerNSkill(player));
         nSkillHandlers.put("funeral_music_ghost", () -> ClientPlayNetworking.send(ClientFuneralMusicGhostNSkillC2SPacket.ID, PacketByteBufs.empty()));
         nSkillHandlers.put("ghost_shadow_head", () -> {
            int mainSlot = MainGhostManager.getMainGhostSlot(player);
            int currentLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
            if (currentLevel < 7) {
               player.method_7353(Text.method_43470("§c复苏程度不足"), true);
            } else {
               LOGGER.debug("玩家 {} 执行鬼影头N键技能：切换目标控制权", player.method_5477().getString());
               GhostShadowHeadCameraManager.performNSkill(player);
            }
         });
         nSkillHandlers.put("complete_shadow_ghost", () -> {
            int mainSlot = MainGhostManager.getMainGhostSlot(player);
            int currentLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
            if (currentLevel < 7) {
               player.method_7353(Text.method_43470("§c复苏程度不足"), true);
            } else {
               LOGGER.debug("玩家 {} 执行完整鬼影N键技能：切换目标控制权", player.method_5477().getString());
               GhostShadowHeadCameraManager.performNSkill(player);
            }
         });
         Runnable handler = nSkillHandlers.get(skillType);
         if (handler != null) {
            handler.run();
         } else if (!skillType.equals("taitou_ghost") && !skillType.equals("ditou_ghost")) {
            LOGGER.debug("玩家 {} 使用N键技能，但当前鬼类型 {} 没有默认N键技能处理", player.method_5477().getString(), skillType);
         } else {
            LOGGER.debug("玩家 {} 尝试使用N键技能，但当前鬼类型无N键技能", player.method_5477().getString());
         }
      }
   }

   private static void handleDefaultGSkill(String skillType, PlayerEntity player) {
      Runnable customHandler = gSkillHandlerMap.get(skillType);
      if (customHandler != null) {
         LOGGER.debug("玩家 {} 使用附属模组注册的G键技能处理：{}", player.method_5477().getString(), skillType);
         customHandler.run();
      } else {
         Map<String, Runnable> gSkillHandlers = new HashMap<>();
         gSkillHandlers.put("silent_ghost_eye", () -> handleGhostEyeGSkill(player));
         gSkillHandlers.put("ghost_fire", () -> handleGhostFireGSkill(player));
         gSkillHandlers.put("lost_ghost", () -> handleLostGhostGSkill(player));
         gSkillHandlers.put("ghost_lake", () -> handleGhostLakeGSkill(player));
         gSkillHandlers.put("qiaomen_ghost", () -> handleQiaomenGhostGSkill(player));
         gSkillHandlers.put("villager_ghost", () -> handleVillagerGhostGSkill(player));
         gSkillHandlers.put("wish_ghost", () -> handleWishGhostGSkill(player));
         gSkillHandlers.put("ghost_officer", () -> {
            LOGGER.debug("玩家 {} 执行鬼差G键技能：秒杀沉寂状态玩家", player.method_5477().getString());
            ClientPlayNetworking.send(ClientGhostOfficerGSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         gSkillHandlers.put("funeral_music_ghost", () -> {
            LOGGER.debug("玩家 {} 执行丧乐鬼G键技能：退出演奏状态，并立刻解除自身沉寂，同时获得3s净化效果", player.method_5477().getString());
            ClientPlayNetworking.send(ClientFuneralMusicGhostGSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         gSkillHandlers.put("ghost_shadow_head", () -> {
            LOGGER.debug("玩家 {} 执行鬼影头G键技能：对附身目标施加缓慢3和虚弱3", player.method_5477().getString());
            GhostShadowHeadCameraManager.performGSkill(player);
         });
         gSkillHandlers.put("complete_shadow_ghost", () -> {
            LOGGER.debug("玩家 {} 执行完整鬼影G键技能：对附身目标施加缓慢3和虚弱3", player.method_5477().getString());
            GhostShadowHeadCameraManager.performGSkill(player);
         });
         Runnable handler = gSkillHandlers.get(skillType);
         if (handler != null) {
            handler.run();
         } else if (!skillType.equals("taitou_ghost") && !skillType.equals("ditou_ghost")) {
            LOGGER.debug("玩家 {} 使用G键技能，但当前鬼类型 {} 没有默认G键技能处理", player.method_5477().getString(), skillType);
         } else {
            LOGGER.debug("玩家 {} 尝试使用G键技能，但当前鬼类型无G键技能", player.method_5477().getString());
         }
      }
   }

   private static void handleDefaultVSkill(String skillType, PlayerEntity player) {
      Runnable customHandler = vSkillHandlerMap.get(skillType);
      if (customHandler != null) {
         LOGGER.debug("玩家 {} 使用附属模组注册的V键技能处理：{}", player.method_5477().getString(), skillType);
         customHandler.run();
      } else {
         Map<String, Runnable> vSkillHandlers = new HashMap<>();
         vSkillHandlers.put("silent_ghost_eye", () -> handleGhostEyeVSkill(player));
         vSkillHandlers.put("ghost_fire", () -> handleGhostFireVSkill(player));
         vSkillHandlers.put("lost_ghost", () -> handleLostGhostVSkill(player));
         vSkillHandlers.put("ghost_lake", () -> handleGhostLakeVSkill(player));
         vSkillHandlers.put("qiaomen_ghost", () -> handleQiaomenGhostVSkill(player));
         vSkillHandlers.put("food_ghost", () -> handleFoodGhostVSkill(player));
         vSkillHandlers.put("villager_ghost", () -> handleVillagerGhostVSkill(player));
         vSkillHandlers.put("block_ghost", () -> handleBlockGhostVSkill(player));
         vSkillHandlers.put("wish_ghost", () -> handleWishGhostVSkill(player));
         vSkillHandlers.put("ghost_officer", () -> ClientPlayNetworking.send(ClientGhostOfficerVSkillC2SPacket.ID, PacketByteBufs.empty()));
         vSkillHandlers.put("funeral_music_ghost", () -> {
            LOGGER.debug("玩家 {} 执行丧乐鬼V键技能：立刻解除自身沉寂并进入演奏状态，同时获得3s净化效果", player.method_5477().getString());
            ClientPlayNetworking.send(ClientFuneralMusicGhostVSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         vSkillHandlers.put("ghost_dream", () -> handleGhostDreamVSkill(player));
         vSkillHandlers.put("ghost_shadow_head", () -> {
            LOGGER.debug("玩家 {} 执行鬼影头V键技能：对附身目标发动灵异袭击", player.method_5477().getString());
            GhostShadowHeadCameraManager.performVSkillAttack(player);
         });
         vSkillHandlers.put("complete_shadow_ghost", () -> {
            LOGGER.debug("玩家 {} 执行完整鬼影V键技能：对附身目标发动灵异袭击", player.method_5477().getString());
            GhostShadowHeadCameraManager.performVSkillAttack(player);
         });
         Runnable handler = vSkillHandlers.get(skillType);
         if (handler != null) {
            handler.run();
         } else if (!skillType.equals("taitou_ghost") && !skillType.equals("ditou_ghost")) {
            LOGGER.debug("玩家 {} 使用V键技能，但当前鬼类型 {} 没有默认V键技能处理", player.method_5477().getString(), skillType);
         } else {
            LOGGER.debug("玩家 {} 尝试使用V键技能，但当前鬼类型无V键技能", player.method_5477().getString());
         }
      }
   }

   private static void handleDefaultJSkill(String skillType, PlayerEntity player) {
      Runnable customHandler = jSkillHandlerMap.get(skillType);
      if (customHandler != null) {
         LOGGER.debug("玩家 {} 使用附属模组注册的J键技能处理：{}", player.method_5477().getString(), skillType);
         customHandler.run();
      } else {
         Map<String, Runnable> jSkillHandlers = new HashMap<>();
         jSkillHandlers.put("silent_ghost_eye", () -> {
            LOGGER.debug("玩家 {} 执行鬼眼J键技能：修复骗人鬼项链耐久度", player.method_5477().getString());
            ClientPlayNetworking.send(ClientGhostEyeJSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("ghost_fire", () -> {
            LOGGER.debug("玩家 {} 执行鬼火J键技能：袭击所有燃烧的生物", player.method_5477().getString());
            ClientPlayNetworking.send(ClientGhostFireJSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("taitou_ghost", () -> {
            LOGGER.debug("玩家 {} 执行抬头鬼J键技能：抬头鬼袭击", player.method_5477().getString());
            ClientPlayNetworking.send(ClientTaitouGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("sneak_ghost", () -> {
            LOGGER.debug("玩家 {} 执行潜行鬼J键技能：袭击蹲下的玩家", player.method_5477().getString());
            ClientPlayNetworking.send(ClientSneakGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("clothes_ghost", () -> {
            LOGGER.debug("玩家 {} 执行裁缝鬼J键技能：袭击未穿戴装备的玩家", player.method_5477().getString());
            ClientPlayNetworking.send(ClientClothesGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("puppet_ghost", () -> {
            LOGGER.debug("玩家 {} 执行木偶鬼J键技能：袭击被标记的生物", player.method_5477().getString());
            ClientPlayNetworking.send(ClientPuppetGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("funeral_music_ghost", () -> {
            LOGGER.debug("玩家 {} 执行丧乐鬼J键技能：触发全部标记，按照层数造成不同程度的灵异叠加袭击", player.method_5477().getString());
            ClientPlayNetworking.send(ClientFuneralMusicGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("ditou_ghost", () -> ClientPlayNetworking.send(ClientDitouGhostSkillC2SPacket.ID, PacketByteBufs.empty()));
         jSkillHandlers.put("box_ghost", () -> ClientPlayNetworking.send(ClientBoxGhostSkillC2SPacket.ID, PacketByteBufs.empty()));
         jSkillHandlers.put("death_sight_ghost", () -> ClientPlayNetworking.send(ClientDeathSightGhostSkillC2SPacket.ID, PacketByteBufs.empty()));
         jSkillHandlers.put("ghost_merchant", () -> ClientPlayNetworking.send(ClientGhostMerchantSkillC2SPacket.ID, PacketByteBufs.empty()));
         jSkillHandlers.put("jump_ghost", () -> ClientPlayNetworking.send(ClientJumpGhostSkillC2SPacket.ID, PacketByteBufs.empty()));
         jSkillHandlers.put("fog_ghost", () -> ClientPlayNetworking.send(ClientFogGhostSkillC2SPacket.ID, PacketByteBufs.empty()));
         jSkillHandlers.put("block_ghost", () -> ClientPlayNetworking.send(ClientBlockGhostSkillC2SPacket.ID, PacketByteBufs.empty()));
         jSkillHandlers.put("food_ghost", () -> {
            LOGGER.debug("玩家 {} 执行食物鬼J键技能：饥饿袭击", player.method_5477().getString());
            ClientPlayNetworking.send(ClientFoodGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("villager_ghost", () -> {
            LOGGER.debug("玩家 {} 执行村民鬼J键技能：鬼奴控制", player.method_5477().getString());
            ClientPlayNetworking.send(ClientVillagerGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("lost_ghost", () -> {
            LOGGER.debug("玩家 {} 执行遗忘鬼J键技能：遗忘标记", player.method_5477().getString());
            ClientPlayNetworking.send(ClientLostGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("qiaomen_ghost", () -> {
            LOGGER.debug("玩家 {} 执行敲门鬼J键技能：敲门", player.method_5477().getString());
            ClientPlayNetworking.send(ClientQiaomenGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("crop_ghost", () -> {
            LOGGER.debug("玩家 {} 执行作物鬼J键技能：标记并袭击所有在作物上的玩家", player.method_5477().getString());
            ClientPlayNetworking.send(ClientCropGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("step_ghost", () -> {
            LOGGER.debug("玩家 {} 执行踩人鬼J键技能：标记并袭击所有在y坐标低于自己的玩家", player.method_5477().getString());
            ClientPlayNetworking.send(ClientStepGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("grave_earth_ghost", () -> {
            LOGGER.debug("玩家 {} 执行坟土鬼J键技能：在目标位置放置坟堆", player.method_5477().getString());
            ClientPlayNetworking.send(ClientGraveEarthGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("trash_ghost", () -> {
            LOGGER.debug("玩家 {} 执行垃圾鬼J键技能：标记并袭击所有周围6格内有掉落物的玩家", player.method_5477().getString());
            ClientPlayNetworking.send(ClientTrashGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("water_ghost", () -> {
            LOGGER.debug("玩家 {} 执行水鬼J键技能：标记并袭击所有在水里的玩家", player.method_5477().getString());
            ClientPlayNetworking.send(ClientWaterGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("giant_shadow_ghost", () -> {
            LOGGER.debug("玩家 {} 执行高大鬼影J键技能：标记并袭击所有背对自己的玩家", player.method_5477().getString());
            ClientPlayNetworking.send(ClientGiantShadowGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("ganshi_bride_ghost", () -> {
            LOGGER.debug("玩家 {} 执行干尸newlineJ键技能：剥夺对方的一个鬼", player.method_5477().getString());
            ClientPlayNetworking.send(ClientGanshiBrideGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("crying_ghost", () -> {
            LOGGER.debug("玩家 {} 执行哭丧鬼J键技能：哭泣标记", player.method_5477().getString());
            ClientPlayNetworking.send(ClientCryingGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("suona_ghost", () -> {
            LOGGER.debug("玩家 {} 执行唢呐鬼J键技能：吹唢呐标记", player.method_5477().getString());
            ClientPlayNetworking.send(ClientSuonaGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("gong_ghost", () -> {
            LOGGER.debug("玩家 {} 执行敲锣鬼J键技能：敲锣标记", player.method_5477().getString());
            ClientPlayNetworking.send(ClientGongGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("ghost_pressure", () -> {
            LOGGER.debug("玩家 {} 执行鬼压人J键技能：将背上的鬼扔到目标身上", player.method_5477().getString());
            ClientPlayNetworking.send(ClientGhostPressureSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("ghost_wind", () -> {
            LOGGER.debug("玩家 {} 执行鬼风J键技能", player.method_5477().getString());
            ClientPlayNetworking.send(ClientGhostWindSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("ghost_blood", () -> {
            LOGGER.debug("玩家 {} 执行鬼血J键技能：释放血雾攻击", player.method_5477().getString());
            ClientPlayNetworking.send(ClientGhostBloodSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("wish_ghost", () -> {
            LOGGER.debug("玩家 {} 执行许愿鬼J键技能：恢复满生命值", player.method_5477().getString());
            ClientPlayNetworking.send(ClientWishGhostJSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("scapegoat_ghost", () -> {
            LOGGER.debug("玩家 {} 执行替死鬼J键技能：替死标记", player.method_5477().getString());
            ClientPlayNetworking.send(ClientScapegoatGhostSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("candy_ghost", () -> {
            LOGGER.debug("玩家 {} 执行糖果鬼J键技能：扣除生命获得鬼糖果", player.method_5477().getString());
            ClientPlayNetworking.send(ClientCandyGhostJSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("silent_ghost", () -> {
            LOGGER.debug("玩家 {} 执行静悄悄J键技能：触发聊天提及传送", player.method_5477().getString());
            ClientPlayNetworking.send(ClientSilentGhostJSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("ghost_officer", () -> {
            LOGGER.debug("玩家 {} 执行鬼差J键技能：手动指定优先压制对象", player.method_5477().getString());
            ClientPlayNetworking.send(ClientGhostOfficerSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("ghost_lake", () -> {
            LOGGER.debug("玩家 {} 执行鬼湖J键技能：在目标位置召唤一处湖水", player.method_5477().getString());
            ClientPlayNetworking.send(ClientGhostLakeSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("plague_ghost", () -> {
            LOGGER.debug("玩家 {} 执行瘟鬼J键技能：传染目标生物瘟疫", player.method_5477().getString());
            ClientPlayNetworking.send(ClientPlagueGhostJSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("mineral_ghost", () -> {
            LOGGER.debug("玩家 {} 执行矿物鬼J键技能：引爆周围的矿物", player.method_5477().getString());
            ClientPlayNetworking.send(ClientMineralGhostJSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("shadow_ghost", () -> {
            LOGGER.debug("玩家 {} 执行黑影鬼J键技能：标记处于阴影中的生物并对其发动袭击", player.method_5477().getString());
            ClientPlayNetworking.send(ClientShadowGhostJSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("complete_shadow_ghost", () -> {
            LOGGER.debug("玩家 {} 执行完整鬼影J键技能：切换摄像机绑定", player.method_5477().getString());
            GhostShadowHeadCameraManager.toggleCameraBinding(player);
            ClientPlayNetworking.send(ClientGhostShadowHeadJSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("door_ghost", () -> {
            LOGGER.debug("玩家 {} 执行开门鬼J键技能：开门对周围生物造成袭击", player.method_5477().getString());
            ClientPlayNetworking.send(ClientDoorGhostJSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("ghost_dream", () -> {
            LOGGER.debug("玩家 {} 执行鬼梦J键技能：在目标位置生成鬼梦生物", player.method_5477().getString());
            ClientPlayNetworking.send(ClientGhostDreamSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         jSkillHandlers.put("ghost_shadow_head", () -> {
            LOGGER.debug("玩家 {} 执行鬼影头J键技能：切换摄像机绑定", player.method_5477().getString());
            GhostShadowHeadCameraManager.toggleCameraBinding(player);
            ClientPlayNetworking.send(ClientGhostShadowHeadJSkillC2SPacket.ID, PacketByteBufs.empty());
         });
         Runnable handler = jSkillHandlers.get(skillType);
         if (handler != null) {
            handler.run();
         } else {
            LOGGER.debug("玩家 {} 使用J键技能，但当前鬼类型 {} 没有默认J键技能处理", player.method_5477().getString(), skillType);
         }
      }
   }
}
