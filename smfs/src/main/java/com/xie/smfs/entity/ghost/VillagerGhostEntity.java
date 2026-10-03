package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.other.GhostSlaveEntity;
import com.xie.smfs.registry.ModEntities;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VillagerGhostEntity extends GhostEntity {
   private static final Logger LOGGER = LoggerFactory.getLogger(VillagerGhostEntity.class);
   private static final HashMap<UUID, Long> playerLastVillagerAttack = new HashMap<>();
   private static final long ATTACK_MEMORY_DURATION = 10000L;
   private int domainCheckCounter = 0;
   private static final int DOMAIN_CHECK_INTERVAL = 1200;
   private int villagerSpawnCounter = 0;
   private static final int VILLAGER_SPAWN_INTERVAL = 600;
   private static final int MIN_VILLAGER_COUNT = 5;
   private final HashMap<UUID, Boolean> playerAttackStatus = new HashMap<>();
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 3;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'C';

   public static Builder createLivingAttributes() {
      return LivingEntity.method_26827()
         .method_26868(EntityAttributes.field_23716, 100000.0)
         .method_26868(EntityAttributes.field_23719, 0.25)
         .method_26868(EntityAttributes.field_23721, 5.0);
   }

   public VillagerGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 3, 32.0, 'C', 3500, 400, 70, 0.3F);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23716)).method_6192(100000.0);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23719)).method_6192(0.25);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23721)).method_6192(5.0);
      this.attackCooldown = 20;
      this.setGhostDomainActualLevel(4);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (!this.isPlayerInRange(player)) {
         this.playerAttackStatus.remove(player.method_5667());
         playerLastVillagerAttack.remove(player.method_5667());
         return false;
      }

      UUID playerId = player.method_5667();
      long currentTime = System.currentTimeMillis();
      if (this.playerAttackStatus.containsKey(playerId) && this.playerAttackStatus.get(playerId)) {
         if (this.attackCooldown <= 0) {
            this.playerAttackStatus.put(playerId, false);
            playerLastVillagerAttack.remove(playerId);
            LOGGER.info("村民鬼对玩家 {} 的袭击冷却结束，重置袭击状态", player.method_5477().getString());
            return false;
         } else {
            return true;
         }
      } else {
         if (playerLastVillagerAttack.containsKey(playerId)) {
            long lastAttackTime = playerLastVillagerAttack.get(playerId);
            long timeDiff = currentTime - lastAttackTime;
            if (timeDiff < 10000L) {
               if (this.attackCooldown <= 0) {
                  this.playerAttackStatus.put(playerId, true);
                  LOGGER.info("村民鬼检测到玩家 {} 最近攻击过村民，开始袭击", player.method_5477().getString());
                  return true;
               }

               LOGGER.info("村民鬼检测到玩家 {} 最近攻击过村民，但攻击冷却中（{}ticks），跳过袭击", player.method_5477().getString(), this.attackCooldown);
               return false;
            }

            playerLastVillagerAttack.remove(playerId);
         }

         Entity attackingEntity = player.method_6052();
         boolean isAttackingVillager = attackingEntity instanceof VillagerEntity;
         if (isAttackingVillager) {
            if (this.attackCooldown <= 0) {
               this.playerAttackStatus.put(playerId, true);
               LOGGER.info("村民鬼检测到玩家 {} 正在攻击村民，开始袭击", player.method_5477().getString());
               return true;
            } else {
               LOGGER.info("村民鬼检测到玩家 {} 正在攻击村民，但攻击冷却中（{}ticks），跳过袭击", player.method_5477().getString(), this.attackCooldown);
               return false;
            }
         } else {
            this.playerAttackStatus.put(playerId, false);
            return false;
         }
      }
   }

   private boolean isPlayerInRange(PlayerEntity player) {
      return this.method_5858(player) <= 1024.0;
   }

   public static void recordVillagerAttack(PlayerEntity player) {
      UUID playerId = player.method_5667();
      long currentTime = System.currentTimeMillis();
      playerLastVillagerAttack.put(playerId, currentTime);
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.isSuppressed() && !this.isDeadlocked()) {
         if (!this.method_37908().field_9236) {
            this.domainCheckCounter++;
            if (this.domainCheckCounter >= 1200) {
               this.domainCheckCounter = 0;
               this.checkVillagerGhostDomainForPlayers();
            }

            this.villagerSpawnCounter++;
            if (this.villagerSpawnCounter >= 600) {
               this.villagerSpawnCounter = 0;
               this.spawnVillagerIfNeeded();
            }
         }
      }
   }

   private void checkVillagerGhostDomainForPlayers() {
      try {
         double domainRadius = this.getGhostDomainRadius();
         List<PlayerEntity> playersInDomain = this.method_37908()
            .method_8390(
               PlayerEntity.class, this.method_5829().method_1014(domainRadius), player -> player.method_5805() && this.method_5739(player) <= domainRadius
            );
         List<VillagerEntity> villagersInDomain = this.method_37908()
            .method_8390(
               VillagerEntity.class,
               this.method_5829().method_1014(domainRadius),
               villager -> villager.method_5805() && this.method_5739(villager) <= domainRadius
            );
         if (!playersInDomain.isEmpty() && !villagersInDomain.isEmpty()) {
            LOGGER.info("开始转换村民鬼鬼奴");
            this.convertVillagerToGhostSlaveInVillagerGhostDomain(domainRadius);
         }
      } catch (Exception e) {
         LoggerFactory.getLogger(VillagerGhostEntity.class).error("检查村民鬼鬼域时发生错误", e);
      }
   }

   private void convertVillagerToGhostSlaveInVillagerGhostDomain(double radius) {
      try {
         List<VillagerEntity> villagers = this.method_37908()
            .method_8390(
               VillagerEntity.class, this.method_5829().method_1014(radius), villager -> villager.method_5805() && this.method_5739(villager) <= radius
            );
         if (villagers.isEmpty()) {
            LOGGER.info("村民鬼鬼域范围内没有村民，跳过转换");
            return;
         }

         VillagerEntity targetVillager = villagers.get(this.method_37908().field_9229.method_43048(villagers.size()));
         LOGGER.info("村民鬼开始转换村民为鬼奴，目标村民位置: ({}, {}, {})", targetVillager.method_23317(), targetVillager.method_23318(), targetVillager.method_23321());
         GhostSlaveEntity ghostSlave = new GhostSlaveEntity(ModEntities.GHOST_SLAVE, this.method_37908());
         ghostSlave.method_5808(
            targetVillager.method_23317(),
            targetVillager.method_23318(),
            targetVillager.method_23321(),
            this.method_37908().field_9229.method_43057() * 360.0F,
            0.0F
         );
         ghostSlave.method_6033(ghostSlave.method_6063());
         ghostSlave.setMaster(this);
         targetVillager.method_5650(RemovalReason.field_26999);
         this.method_37908().method_8649(ghostSlave);
         LOGGER.info("村民鬼成功转换村民为鬼奴，鬼奴位置: ({}, {}, {})", ghostSlave.method_23317(), ghostSlave.method_23318(), ghostSlave.method_23321());
         List<PlayerEntity> playersInDomain = this.method_37908()
            .method_8390(PlayerEntity.class, this.method_5829().method_1014(radius), playerx -> playerx.method_5805() && this.method_5739(playerx) <= radius);

         for (PlayerEntity player : playersInDomain) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c村民鬼的鬼域将一个村民转换为鬼奴！"), true);
            }
         }

         LOGGER.info("村民鬼转换村民为鬼奴完成，通知了{}名玩家", playersInDomain.size());
      } catch (Exception e) {
         LoggerFactory.getLogger(VillagerGhostEntity.class).error("在村民鬼鬼域中转换村民为鬼奴时发生错误", e);
      }
   }

   private void spawnVillagerIfNeeded() {
      try {
         double domainRadius = this.getGhostDomainRadius();
         List<VillagerEntity> villagersInDomain = this.method_37908()
            .method_8390(
               VillagerEntity.class,
               this.method_5829().method_1014(domainRadius),
               villager -> villager.method_5805() && this.method_5739(villager) <= domainRadius
            );
         if (villagersInDomain.size() < 5) {
            this.spawnVillagerInDomain(domainRadius);
         }
      } catch (Exception e) {
         LoggerFactory.getLogger(VillagerGhostEntity.class).error("检查并生成村民时发生错误", e);
      }
   }

   private void spawnVillagerInDomain(double radius) {
      try {
         double x = this.method_23317() + (this.method_37908().field_9229.method_43058() - 0.5) * radius * 2.0;
         double z = this.method_23321() + (this.method_37908().field_9229.method_43058() - 0.5) * radius * 2.0;
         double y = this.method_37908().method_8624(Type.field_13202, (int)x, (int)z);
         VillagerEntity villager = new VillagerEntity(EntityType.field_6077, this.method_37908());
         villager.method_5808(x, y, z, this.method_37908().field_9229.method_43057() * 360.0F, 0.0F);
         villager.method_6033(villager.method_6063());
         this.method_37908().method_8649(villager);

         for (PlayerEntity player : this.method_37908()
            .method_8390(PlayerEntity.class, this.method_5829().method_1014(radius), playerx -> playerx.method_5805() && this.method_5739(playerx) <= radius)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c村民鬼的鬼域中出现了新的村民！"), true);
            }
         }
      } catch (Exception e) {
         LoggerFactory.getLogger(VillagerGhostEntity.class).error("在村民鬼鬼域中生成村民时发生错误", e);
      }
   }
}
