package com.xie.smfs.client.util;

import com.xie.smfs.client.preset.FilterRenderer;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostShadowHeadGSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostShadowHeadUnbindControlC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostShadowHeadVSkillC2SPacket;
import com.xie.smfs.util.TargetingUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostShadowHeadCameraManager {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostShadowHeadCameraManager");
   private static boolean isCameraBound = false;
   private static Entity boundEntity = null;
   private static Perspective savedPerspective = Perspective.FIRST_PERSON;
   private static boolean isNControlActive = false;
   private static double savedPlayerX = 0.0;
   private static double savedPlayerY = 0.0;
   private static double savedPlayerZ = 0.0;

   public static void toggleCameraBinding(PlayerEntity player) {
      if (player == null) {
         LOGGER.warn("尝试切换摄像机绑定但玩家为空");
      } else {
         if (isCameraBound) {
            unbindCamera(player);
         } else {
            bindCamera(player);
         }
      }
   }

   private static void bindCamera(PlayerEntity player) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         Entity target = getTargetEntity(player);
         if (target == null) {
            player.sendMessage(Text.literal("§c未找到目标实体，请对准一个实体"), true);
         } else if (target == player) {
            player.sendMessage(Text.literal("§c不能入侵到自己身上"), true);
         } else {
            savedPerspective = client.options.getPerspective();
            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            isCameraBound = true;
            boundEntity = target;
            LOGGER.debug("玩家 {} 已将摄像机绑定到实体 {}", player.getName().getString(), target.getId());
            player.sendMessage(Text.literal("§a意识已入侵"), true);
         }
      }
   }

   private static void unbindCamera(PlayerEntity player) {
      MinecraftClient client = MinecraftClient.getInstance();
      client.options.setPerspective(savedPerspective);
      int unboundEntityId = boundEntity != null ? boundEntity.getId() : -1;
      isCameraBound = false;
      boundEntity = null;
      isNControlActive = false;
      savedPlayerX = 0.0;
      savedPlayerY = 0.0;
      savedPlayerZ = 0.0;
      if (unboundEntityId != -1) {
         ClientGhostShadowHeadUnbindControlC2SPacket.sendToServer(unboundEntityId);
      }

      LOGGER.debug("玩家 {} 已将摄像机返回自身视角", player.getName().getString());
      player.sendMessage(Text.literal("§a意识已回归"), true);
   }

   private static Entity getTargetEntity(PlayerEntity player) {
      return TargetingUtil.findEntityInLookDirection(player, 30.0, 0.866);
   }

   public static boolean isBoundToEntity(Entity entity) {
      return isCameraBound && boundEntity != null && boundEntity.equals(entity);
   }

   public static void resetBinding() {
      if (isCameraBound) {
         MinecraftClient client = MinecraftClient.getInstance();
         client.options.setPerspective(savedPerspective);
         isCameraBound = false;
         boundEntity = null;
         LOGGER.debug("重置鬼影头摄像机绑定状态");
      }
   }

   public static boolean isCameraBound() {
      return isCameraBound;
   }

   public static Entity getBoundEntity() {
      return boundEntity;
   }

   public static void renderFilter(DrawContext context) {
      if (isCameraBound) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.getWindow() != null) {
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            FilterRenderer.renderEdgeGradient(context, width, height, FilterRenderer.FilterPreset.BLACK_CURSE);
         }
      }
   }

   public static void performVSkillAttack(PlayerEntity player) {
      if (isCameraBound && boundEntity != null && !boundEntity.isRemoved()) {
         int targetId = boundEntity.getId();
         ClientGhostShadowHeadVSkillC2SPacket.sendToServer(targetId);
         LOGGER.debug("鬼影头V技能：对实体 {} (ID: {}) 发动灵异袭击", boundEntity.getName().getString(), targetId);
         unbindCamera(player);
      } else {
         player.sendMessage(Text.literal("§c没有入侵目标，无法发动灵异袭击"), true);
      }
   }

   public static void performGSkill(PlayerEntity player) {
      if (isCameraBound && boundEntity != null && !boundEntity.isRemoved()) {
         int targetId = boundEntity.getId();
         ClientGhostShadowHeadGSkillC2SPacket.sendToServer(targetId);
         LOGGER.debug("鬼影头G技能：对实体 {} (ID: {}) 施加缓慢3和虚弱3", boundEntity.getName().getString(), targetId);
      } else {
         player.sendMessage(Text.literal("§c没有入侵目标，无法施加效果"), true);
      }
   }

   public static void performNSkill(PlayerEntity player) {
      if (isCameraBound && boundEntity != null && !boundEntity.isRemoved()) {
         isNControlActive = !isNControlActive;
         if (isNControlActive) {
            savedPlayerX = player.getX();
            savedPlayerY = player.getY();
            savedPlayerZ = player.getZ();
            player.sendMessage(Text.literal("§a控制权已移交至 " + boundEntity.getName().getString()), true);
         } else {
            ClientGhostShadowHeadUnbindControlC2SPacket.sendToServer(boundEntity.getId());
            player.sendMessage(Text.literal("§a控制权已回归"), true);
         }
      } else {
         player.sendMessage(Text.literal("§c没有入侵目标，无法移交控制"), true);
      }
   }

   public static boolean isNControlActive() {
      return isNControlActive && isCameraBound && boundEntity != null && !boundEntity.isRemoved();
   }

   public static double getSavedPlayerX() {
      return savedPlayerX;
   }

   public static double getSavedPlayerY() {
      return savedPlayerY;
   }

   public static double getSavedPlayerZ() {
      return savedPlayerZ;
   }
}
