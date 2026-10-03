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
   private static Perspective savedPerspective = Perspective.field_26664;
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
      MinecraftClient client = MinecraftClient.method_1551();
      if (client.field_1724 != null) {
         Entity target = getTargetEntity(player);
         if (target == null) {
            player.method_7353(Text.method_43470("§c未找到目标实体，请对准一个实体"), true);
         } else if (target == player) {
            player.method_7353(Text.method_43470("§c不能入侵到自己身上"), true);
         } else {
            savedPerspective = client.field_1690.method_31044();
            client.field_1690.method_31043(Perspective.field_26665);
            isCameraBound = true;
            boundEntity = target;
            LOGGER.debug("玩家 {} 已将摄像机绑定到实体 {}", player.method_5477().getString(), target.method_5628());
            player.method_7353(Text.method_43470("§a意识已入侵"), true);
         }
      }
   }

   private static void unbindCamera(PlayerEntity player) {
      MinecraftClient client = MinecraftClient.method_1551();
      client.field_1690.method_31043(savedPerspective);
      int unboundEntityId = boundEntity != null ? boundEntity.method_5628() : -1;
      isCameraBound = false;
      boundEntity = null;
      isNControlActive = false;
      savedPlayerX = 0.0;
      savedPlayerY = 0.0;
      savedPlayerZ = 0.0;
      if (unboundEntityId != -1) {
         ClientGhostShadowHeadUnbindControlC2SPacket.sendToServer(unboundEntityId);
      }

      LOGGER.debug("玩家 {} 已将摄像机返回自身视角", player.method_5477().getString());
      player.method_7353(Text.method_43470("§a意识已回归"), true);
   }

   private static Entity getTargetEntity(PlayerEntity player) {
      return TargetingUtil.findEntityInLookDirection(player, 30.0, 0.866);
   }

   public static boolean isBoundToEntity(Entity entity) {
      return isCameraBound && boundEntity != null && boundEntity.equals(entity);
   }

   public static void resetBinding() {
      if (isCameraBound) {
         MinecraftClient client = MinecraftClient.method_1551();
         client.field_1690.method_31043(savedPerspective);
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
         MinecraftClient client = MinecraftClient.method_1551();
         if (client.method_22683() != null) {
            int width = client.method_22683().method_4486();
            int height = client.method_22683().method_4502();
            FilterRenderer.renderEdgeGradient(context, width, height, FilterRenderer.FilterPreset.BLACK_CURSE);
         }
      }
   }

   public static void performVSkillAttack(PlayerEntity player) {
      if (isCameraBound && boundEntity != null && !boundEntity.method_31481()) {
         int targetId = boundEntity.method_5628();
         ClientGhostShadowHeadVSkillC2SPacket.sendToServer(targetId);
         LOGGER.debug("鬼影头V技能：对实体 {} (ID: {}) 发动灵异袭击", boundEntity.method_5477().getString(), targetId);
         unbindCamera(player);
      } else {
         player.method_7353(Text.method_43470("§c没有入侵目标，无法发动灵异袭击"), true);
      }
   }

   public static void performGSkill(PlayerEntity player) {
      if (isCameraBound && boundEntity != null && !boundEntity.method_31481()) {
         int targetId = boundEntity.method_5628();
         ClientGhostShadowHeadGSkillC2SPacket.sendToServer(targetId);
         LOGGER.debug("鬼影头G技能：对实体 {} (ID: {}) 施加缓慢3和虚弱3", boundEntity.method_5477().getString(), targetId);
      } else {
         player.method_7353(Text.method_43470("§c没有入侵目标，无法施加效果"), true);
      }
   }

   public static void performNSkill(PlayerEntity player) {
      if (isCameraBound && boundEntity != null && !boundEntity.method_31481()) {
         isNControlActive = !isNControlActive;
         if (isNControlActive) {
            savedPlayerX = player.method_23317();
            savedPlayerY = player.method_23318();
            savedPlayerZ = player.method_23321();
            player.method_7353(Text.method_43470("§a控制权已移交至 " + boundEntity.method_5477().getString()), true);
         } else {
            ClientGhostShadowHeadUnbindControlC2SPacket.sendToServer(boundEntity.method_5628());
            player.method_7353(Text.method_43470("§a控制权已回归"), true);
         }
      } else {
         player.method_7353(Text.method_43470("§c没有入侵目标，无法移交控制"), true);
      }
   }

   public static boolean isNControlActive() {
      return isNControlActive && isCameraBound && boundEntity != null && !boundEntity.method_31481();
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
