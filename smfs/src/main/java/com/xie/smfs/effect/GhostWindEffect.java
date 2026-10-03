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
      super(StatusEffectCategory.HARMFUL, 8900331);
   }

   public String getTranslationKey() {
      return "effect.smfs.ghost_wind";
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player && !entity.getWorld().isClient()) {
         if (!player.hasStatusEffect(ModEffects.CYAN_GHOST_DOMAIN_TARGET)) {
            NbtCompound playerData = PlayerEvents.getCachedData(player);
            if (playerData.contains("ghost_wind_effect")) {
               playerData.remove("ghost_wind_effect");
            }

            player.removeStatusEffect(this);
            return;
         }

         NbtCompound playerData = PlayerEvents.getCachedData(player);
         if (playerData.contains("ghost_wind_effect")) {
            NbtCompound ghostWindData = playerData.getCompound("ghost_wind_effect");
            long startTime = ghostWindData.getLong("wind_start_time");
            long currentTime = player.getWorld().getTime();
            long elapsedTime = currentTime - startTime;
            float currentProgress = Math.min(1.0F, (float)elapsedTime / 600.0F);
            LoggerFactory.getLogger("smfs/GhostWindEffect")
               .debug("玩家 {} 鬼风进度计算: 开始时间={}, 当前时间={}, 经过时间={}ticks, 进度={}", player.getName().getString(), startTime, currentTime, elapsedTime, currentProgress);
            ghostWindData.putFloat("wind_progress", currentProgress);
            ghostWindData.putLong("last_update_time", currentTime);
            playerData.put("ghost_wind_effect", ghostWindData);
            if (ghostWindData.containsUuid("ghost_wind_entity")) {
               List<GhostWindEntity> ghostWindEntities = player.getWorld()
                  .getEntitiesByClass(
                     GhostWindEntity.class,
                     player.getBoundingBox().expand(128.0),
                     ghostEntity -> ghostEntity.getUuid().equals(ghostWindData.getUuid("ghost_wind_entity")) && ghostEntity.isAlive()
                  );
               LoggerFactory.getLogger("smfs/GhostWindEffect").debug("玩家 {} 搜索鬼风鬼实体，找到 {} 个符合条件的实体", player.getName().getString(), ghostWindEntities.size());
               GhostWindEntity ghostWind = ghostWindEntities.isEmpty() ? null : ghostWindEntities.get(0);
               if (ghostWind != null) {
                  LoggerFactory.getLogger("smfs/GhostWindEffect")
                     .debug("玩家 {} 找到鬼风鬼实体: {}，位置: {}", player.getName().getString(), ghostWind.getUuid(), ghostWind.getPos());
                  float rotationSpeed = 0.05F * (amplifier + 1);
                  float windStrength = 0.1F * (amplifier + 1);
                  Vec3d ghostPos = ghostWind.getPos();
                  Vec3d playerPos = player.getPos();
                  Vec3d relativePos = playerPos.subtract(ghostPos);
                  LoggerFactory.getLogger("smfs/GhostWindEffect")
                     .debug("玩家 {} 相对位置计算: 鬼风鬼位置={}, 玩家位置={}, 相对位置={}", player.getName().getString(), ghostPos, playerPos, relativePos);
                  double radius = Math.max(relativePos.horizontalLength(), 2.0);
                  double currentAngle = Math.atan2(relativePos.z, relativePos.x);
                  double newAngle = currentAngle + rotationSpeed;
                  double newX = ghostPos.x + radius * Math.cos(newAngle);
                  double newZ = ghostPos.z + radius * Math.sin(newAngle);
                  double newY = Math.min(playerPos.y + windStrength, ghostPos.y + 10.0);
                  player.setPosition(newX, newY, newZ);
                  player.setVelocity(0.0, windStrength * 0.5, 0.0);
                  player.velocityModified = true;
                  player.setMovementSpeed(0.0F);
                  LoggerFactory.getLogger("smfs/GhostWindEffect")
                     .debug(
                        "玩家 {} 受到鬼风效果影响，位置: {} -> {}，速度: {}, 进度: {}",
                        player.getName().getString(),
                        playerPos,
                        new Vec3d(newX, newY, newZ),
                        player.getVelocity(),
                        currentProgress
                     );
                  if (!ghostWindData.getBoolean("sound_played")) {
                     player.getWorld()
                        .playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.GHOST_WIND_SOUND, SoundCategory.AMBIENT, 0.5F, 1.0F);
                     ghostWindData.putBoolean("sound_played", true);
                  }
               } else {
                  LoggerFactory.getLogger("smfs/GhostWindEffect").warn("玩家 {} 的鬼风鬼实体不存在或已死亡，清除鬼风效果", player.getName().getString());
                  playerData.remove("ghost_wind_effect");
                  player.removeStatusEffect(this);
                  LoggerFactory.getLogger("smfs/GhostWindEffect").debug("玩家 {} 的鬼风效果已清除，因为鬼风鬼不存在或已死亡", player.getName().getString());
               }
            } else {
               LoggerFactory.getLogger("smfs/GhostWindEffect").warn("玩家 {} 的鬼风效果数据中缺少鬼风鬼实体UUID", player.getName().getString());
            }
         }
      }
   }
}
