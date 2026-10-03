package com.xie.smfs.data;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerRoyalCurseManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(PlayerRoyalCurseManager.class);

   public static RoyalCurseData getRoyalCurseData(PlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      if (data.contains("royalCurseData")) {
         NbtCompound royalCurseNbt = data.getCompound("royalCurseData");
         return new RoyalCurseData(royalCurseNbt);
      } else {
         RoyalCurseData royalCurseData = new RoyalCurseData();
         saveRoyalCurseData(player, royalCurseData);
         return royalCurseData;
      }
   }

   public static void saveRoyalCurseData(PlayerEntity player, RoyalCurseData data) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      playerData.put("royalCurseData", data.toNbt());
      PlayerEvents.saveDataToPlayer(player, playerData);
   }

   public static void summonServant(PlayerEntity player, int servantIndex) {
      LOGGER.debug("开始召唤奴仆，索引: {}", servantIndex);
      RoyalCurseData data = getRoyalCurseData(player);
      LOGGER.debug("获取到王家诅咒数据，奴仆数量: {}", data.getServantCount());
      RoyalCurseServantData servantData = data.getServant(servantIndex);
      LOGGER.debug("获取到奴仆数据: {}", servantData);
      if (servantData != null && !servantData.isReleased() && player.getWorld() != null && !player.getWorld().isClient) {
         LOGGER.debug("开始创建奴仆实体，UUID: {}", servantData.getUuid());
         PlayerGhostEntity servant = PlayerGhostEntity.createFromStoredData(player, player.getWorld(), servantData.getUuid());
         LOGGER.debug("创建奴仆实体成功: {}", servant);
         servant.setServantMode(true);
         servant.setMasterUuid(player.getUuid());
         Vec3d spawnPos = player.getPos().add(player.getRotationVector().multiply(3.0));
         servant.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, player.getYaw(), 0.0F);
         player.getWorld().spawnEntity(servant);
         LOGGER.debug("奴仆实体生成成功");
         playSummonEffects(player, spawnPos);
         player.sendMessage(Text.translatable("item.smfs.eerie_family_portrait.servant_summoned"), true);
         servantData.summon();
         saveRoyalCurseData(player, data);
         LOGGER.debug("奴仆状态更新并保存成功");
      } else {
         LOGGER.debug(
            "召唤条件不满足: servantData={}, isReleased={}, world={}, isClient={}",
            servantData,
            servantData != null ? servantData.isReleased() : "null",
            player.getWorld(),
            player.getWorld() != null ? player.getWorld().isClient : "null"
         );
      }
   }

   public static void recallServant(PlayerEntity player, int servantIndex) {
      LOGGER.debug("开始收回奴仆，索引: {}", servantIndex);
      RoyalCurseData data = getRoyalCurseData(player);
      LOGGER.debug("获取到皇家诅咒数据，奴仆数量: {}", data.getServantCount());
      RoyalCurseServantData servantData = data.getServant(servantIndex);
      LOGGER.debug("获取到奴仆数据: {}", servantData);
      if (servantData != null && servantData.isReleased() && player.getWorld() != null && !player.getWorld().isClient) {
         LOGGER.debug("开始搜索奴仆实体，UUID: {}", servantData.getUuid());
         List<PlayerGhostEntity> servants = player.getWorld()
            .getEntitiesByClass(
               PlayerGhostEntity.class,
               new Box(-3.0E7, -64.0, -3.0E7, 3.0E7, 320.0, 3.0E7),
               entity -> {
                  boolean hasMaster = entity.getMasterUuid() != null;
                  boolean isOwner = hasMaster && entity.getMasterUuid().equals(player.getUuid());
                  boolean hasOriginalServantUuid = entity.getOriginalServantUuid() != null;
                  boolean isTargetServant = isOwner && hasOriginalServantUuid && entity.getOriginalServantUuid().equals(servantData.getUuid());
                  LOGGER.debug(
                     "检查实体: UUID={}, OriginalServantUuid={}, hasMaster={}, isOwner={}, hasOriginalServantUuid={}, isTargetServant={}",
                     entity.getUuid(),
                     entity.getOriginalServantUuid(),
                     hasMaster,
                     isOwner,
                     hasOriginalServantUuid,
                     isTargetServant
                  );
                  return isTargetServant;
               }
            );
         LOGGER.debug("找到奴仆实体数量: {}", servants.size());

         for (PlayerGhostEntity servant : servants) {
            LOGGER.debug("找到奴仆实体: {}, 主人UUID: {}", servant.getUuid(), servant.getMasterUuid());
            playRecallEffects(player, servant);
            servant.discard();
            LOGGER.debug("奴仆实体已收回");
         }

         servantData.recall();
         saveRoyalCurseData(player, data);
         LOGGER.debug("奴仆状态更新并保存成功");
         player.sendMessage(Text.translatable("item.smfs.eerie_family_portrait.servant_recalled"), true);
      } else {
         LOGGER.debug(
            "收回条件不满足: servantData={}, isReleased={}, world={}, isClient={}",
            servantData,
            servantData != null ? servantData.isReleased() : "null",
            player.getWorld(),
            player.getWorld() != null ? player.getWorld().isClient : "null"
         );
      }
   }

   public static void recallAllServants(PlayerEntity player) {
      RoyalCurseData data = getRoyalCurseData(player);
      if (player.getWorld() != null && !player.getWorld().isClient) {
         for (PlayerGhostEntity servant : player.getWorld()
            .getEntitiesByClass(
               PlayerGhostEntity.class,
               player.getBoundingBox().expand(100.0),
               entity -> entity.getMasterUuid() != null && entity.getMasterUuid().equals(player.getUuid())
            )) {
            servant.discard();
         }

         for (RoyalCurseServantData servantData : data.getServants()) {
            if (servantData != null) {
               servantData.recall();
            }
         }

         saveRoyalCurseData(player, data);
      }
   }

   public static void summonAllServants(PlayerEntity player) {
      RoyalCurseData data = getRoyalCurseData(player);
      if (player.getWorld() != null && !player.getWorld().isClient) {
         for (int i = 0; i < data.getServantCount(); i++) {
            RoyalCurseServantData servantData = data.getServant(i);
            if (servantData != null && !servantData.isReleased()) {
               summonServant(player, i);
            }
         }
      }
   }

   public static boolean hasRoyalCurseUnlocked(PlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      return data.getBoolean("wangCurseUnlocked");
   }

   public static boolean unlockRoyalCurse(PlayerEntity player) {
      if (hasRoyalCurseUnlocked(player)) {
         return false;
      }

      NbtCompound data = PlayerEvents.getCachedData(player);
      data.putBoolean("wangCurseUnlocked", true);
      PlayerEvents.saveDataToPlayer(player, data);
      LOGGER.debug("玩家 {} 解锁了王家诅咒", player.getName().getString());
      return true;
   }

   private static void playSummonEffects(PlayerEntity player, Vec3d spawnPos) {
      player.playSound(SoundEvents.ENTITY_EVOKER_CAST_SPELL, 1.0F, 0.8F);
      if (player.getWorld() instanceof ServerWorld serverWorld) {
         ServerWorld world = serverWorld;

         for (int i = 0; i < 15; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 2.5;
            double offsetY = world.random.nextDouble() * 1.5;
            double offsetZ = (world.random.nextDouble() - 0.5) * 2.5;
            world.spawnParticles(ParticleTypes.SOUL, spawnPos.x + offsetX, spawnPos.y + offsetY, spawnPos.z + offsetZ, 1, 0.02, 0.02, 0.02, 0.005);
         }
      }
   }

   private static void playRecallEffects(PlayerEntity player, PlayerGhostEntity servant) {
      player.playSound(SoundEvents.ENTITY_EVOKER_CAST_SPELL, 1.0F, 1.2F);
      if (player.getWorld() instanceof ServerWorld serverWorld) {
         ServerWorld world = serverWorld;

         for (int i = 0; i < 30; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 2.0;
            double offsetY = (world.random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (world.random.nextDouble() - 0.5) * 2.0;
            world.spawnParticles(ParticleTypes.ASH, servant.getX() + offsetX, servant.getY() + offsetY, servant.getZ() + offsetZ, 6, 0.05, 0.05, 0.05, 0.01);
         }

         for (int i = 0; i < 15; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 1.5;
            double offsetY = (world.random.nextDouble() - 0.5) * 1.5;
            double offsetZ = (world.random.nextDouble() - 0.5) * 1.5;
            world.spawnParticles(ParticleTypes.SMOKE, servant.getX() + offsetX, servant.getY() + offsetY, servant.getZ() + offsetZ, 1, 0.03, 0.03, 0.03, 0.005);
         }
      }
   }
}
