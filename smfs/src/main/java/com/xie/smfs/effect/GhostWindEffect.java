package com.xie.smfs.effect;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.ghost.GhostWindEntity;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModSounds;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import org.slf4j.LoggerFactory;

public class GhostWindEffect extends StatusEffect {
   public GhostWindEffect() {
      super(StatusEffectCategory.field_18272, 8900331);
   }

   public String method_5567() {
      return "effect.smfs.ghost_wind";
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player && !entity.method_37908().method_8608()) {
         if (!player.method_6059(ModEffects.CYAN_GHOST_DOMAIN_TARGET)) {
            NbtCompound playerData = PlayerEvents.getCachedData(player);
            if (playerData.method_10545("ghost_wind_effect")) {
               playerData.method_10551("ghost_wind_effect");
            }

            player.method_6016(this);
            return;
         }

         NbtCompound playerData = PlayerEvents.getCachedData(player);
         if (playerData.method_10545("ghost_wind_effect")) {
            NbtCompound ghostWindData = playerData.method_10562("ghost_wind_effect");
            long startTime = ghostWindData.method_10537("wind_start_time");
            long currentTime = player.method_37908().method_8510();
            long elapsedTime = currentTime - startTime;
            float currentProgress = Math.min(1.0F, (float)elapsedTime / 600.0F);
            LoggerFactory.getLogger("smfs/GhostWindEffect")
               .debug(
                  "玩家 {} 鬼风进度计算: 开始时间={}, 当前时间={}, 经过时间={}ticks, 进度={}", player.method_5477().getString(), startTime, currentTime, elapsedTime, currentProgress
               );
            ghostWindData.method_10548("wind_progress", currentProgress);
            ghostWindData.method_10544("last_update_time", currentTime);
            playerData.method_10566("ghost_wind_effect", ghostWindData);
            if (ghostWindData.method_25928("ghost_wind_entity")) {
               List<GhostWindEntity> ghostWindEntities = player.method_37908()
                  .method_8390(
                     GhostWindEntity.class,
                     player.method_5829().method_1014(128.0),
                     ghostEntity -> ghostEntity.method_5667().equals(ghostWindData.method_25926("ghost_wind_entity")) && ghostEntity.method_5805()
                  );
               LoggerFactory.getLogger("smfs/GhostWindEffect")
                  .debug("玩家 {} 搜索鬼风鬼实体，找到 {} 个符合条件的实体", player.method_5477().getString(), ghostWindEntities.size());
               GhostWindEntity ghostWind = ghostWindEntities.isEmpty() ? null : ghostWindEntities.get(0);
               if (ghostWind != null) {
                  LoggerFactory.getLogger("smfs/GhostWindEffect")
                     .debug("玩家 {} 找到鬼风鬼实体: {}，位置: {}", player.method_5477().getString(), ghostWind.method_5667(), ghostWind.method_19538());
                  float rotationSpeed = 0.05F * (amplifier + 1);
                  float windStrength = 0.1F * (amplifier + 1);
                  Vec3d ghostPos = ghostWind.method_19538();
                  Vec3d playerPos = player.method_19538();
                  Vec3d relativePos = playerPos.method_1020(ghostPos);
                  LoggerFactory.getLogger("smfs/GhostWindEffect")
                     .debug("玩家 {} 相对位置计算: 鬼风鬼位置={}, 玩家位置={}, 相对位置={}", player.method_5477().getString(), ghostPos, playerPos, relativePos);
                  double radius = Math.max(relativePos.method_37267(), 2.0);
                  double currentAngle = Math.atan2(relativePos.field_1350, relativePos.field_1352);
                  double newAngle = currentAngle + rotationSpeed;
                  double newX = ghostPos.field_1352 + radius * Math.cos(newAngle);
                  double newZ = ghostPos.field_1350 + radius * Math.sin(newAngle);
                  double newY = Math.min(playerPos.field_1351 + windStrength, ghostPos.field_1351 + 10.0);
                  player.method_5814(newX, newY, newZ);
                  player.method_18800(0.0, windStrength * 0.5, 0.0);
                  player.field_6037 = true;
                  player.method_6125(0.0F);
                  LoggerFactory.getLogger("smfs/GhostWindEffect")
                     .debug(
                        "玩家 {} 受到鬼风效果影响，位置: {} -> {}，速度: {}, 进度: {}",
                        player.method_5477().getString(),
                        playerPos,
                        new Vec3d(newX, newY, newZ),
                        player.method_18798(),
                        currentProgress
                     );
                  if (!ghostWindData.method_10577("sound_played")) {
                     player.method_37908()
                        .method_43128(
                           null,
                           player.method_23317(),
                           player.method_23318(),
                           player.method_23321(),
                           ModSounds.GHOST_WIND_SOUND,
                           SoundCategory.field_15256,
                           0.5F,
                           1.0F
                        );
                     ghostWindData.method_10556("sound_played", true);
                  }
               } else {
                  LoggerFactory.getLogger("smfs/GhostWindEffect").warn("玩家 {} 的鬼风鬼实体不存在或已死亡，清除鬼风效果", player.method_5477().getString());
                  playerData.method_10551("ghost_wind_effect");
                  player.method_6016(this);
                  LoggerFactory.getLogger("smfs/GhostWindEffect").debug("玩家 {} 的鬼风效果已清除，因为鬼风鬼不存在或已死亡", player.method_5477().getString());
               }
            } else {
               LoggerFactory.getLogger("smfs/GhostWindEffect").warn("玩家 {} 的鬼风效果数据中缺少鬼风鬼实体UUID", player.method_5477().getString());
            }
         }
      }
   }
}
