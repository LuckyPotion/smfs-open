package com.xie.smfs.entity.ghost;

import com.xie.smfs.block.GhostCoffinBlock;
import com.xie.smfs.block.entity.GhostCoffinBlockEntity;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.event.GhostDeathHandler;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModSounds;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GhostOfficerEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 3;
   private static final double GHOST_DOMAIN_RADIUS = 64.0;
   private static final double MAX_GHOST_DOMAIN_RADIUS = 160.0;
   private static final double GHOST_DOMAIN_INCREMENT = 20.0;
   private static final char TERROR_LEVEL = 'S';
   private int suppressionQuota = 1;
   private int suppressedTimer = 0;
   private int deadlockedTimer = 0;
   private final Map<UUID, Integer> attackCountdowns = new HashMap<>();
   private int musicCooldown = 0;
   private static final int MUSIC_INTERVAL = 6000;
   private boolean hasPlayedMusic = false;
   private BlockPos targetCoffinPos = null;
   private int coffinSearchCooldown = 0;
   private int coffinEnterCooldown = 0;
   private static final int COFFIN_SEARCH_INTERVAL = 20;
   private static final double COFFIN_DETECTION_RANGE = 6.0;
   private static final int COFFIN_ENTER_COOLDOWN_TICKS = 600;
   private boolean hasInitialSpawnedGhostSlaves = false;
   private Map<UUID, Integer> attackTimers = new HashMap<>();

   public GhostOfficerEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 3, 64.0, 'S', 6000, 3000, 300, 0.5F);
      this.ghostLevel = 6;
      this.setGhostDomainActualLevel(6);
      this.attackCooldown = 200;
      this.setEnableChaseAfterRule(false);
      this.coffinEnterCooldown = 600;
   }

   @Override
   public boolean isResentmentSystemDisabled() {
      return true;
   }

   @Override
   public void disableGhostDomain() {
   }

   @Override
   public boolean isGhostDomainEnabled() {
      return true;
   }

   @Override
   public void setGhostDomainEnabled(boolean enabled) {
   }

   @Override
   protected boolean isHasGhostDomain() {
      return true;
   }

   public void increaseSuppressionQuota() {
      this.suppressionQuota++;
      this.updateGhostDomainRadius();
   }

   public void setSuppressionQuota(int suppressionQuota) {
      this.suppressionQuota = suppressionQuota;
      this.updateGhostDomainRadius();
   }

   public int getSuppressionQuota() {
      return this.suppressionQuota;
   }

   private void updateGhostDomainRadius() {
      double newRadius = 64.0 + (this.suppressionQuota - 1) * 20.0;
      newRadius = Math.min(newRadius, 160.0);
      this.setGhostDomainRadius((float)newRadius);
   }

   @Override
   public float getGhostDomainRadius() {
      double radius = 64.0 + (this.suppressionQuota - 1) * 20.0;
      return (float)Math.min(radius, 160.0);
   }

   private int getPlayerGhostSlots(LivingEntity player) {
      return PlayerEvents.countOccupiedGhostSlots((PlayerEntity)player);
   }

   private int getCombinedGhostSlots(PlayerEntity player) {
      Box searchBox = Box.method_30048(player.method_19538(), 6.0, 6.0, 6.0);
      List<PlayerEntity> nearbyPlayers = this.method_37908()
         .method_18456()
         .stream()
         .filter(pl -> pl.method_5805() && pl.method_5858(player) <= 9.0)
         .collect(Collectors.toList());
      int totalSlots = 0;

      for (PlayerEntity nearbyPlayer : nearbyPlayers) {
         totalSlots += this.getPlayerGhostSlots(nearbyPlayer);
      }

      return totalSlots;
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (this.isSuppressed() || this.isDeadlocked()) {
         return false;
      } else if (this.attackCooldown > 0) {
         return false;
      } else if (!this.isInGhostDomain(player)) {
         return false;
      } else {
         double ghostDomainRadius = this.getGhostDomainRadius();
         List<GhostEntity> ghostsInDomain = this.method_37908()
            .method_8390(
               GhostEntity.class,
               this.method_5829().method_1014(ghostDomainRadius),
               ghost -> ghost != this && !ghost.isDeadlocked() && !ghost.isSuppressed() && ghost.method_5805()
            );
         if (!ghostsInDomain.isEmpty()) {
            LOGGER.debug("鬼差 {} 发现鬼域内有其他鬼，优先攻击鬼而不是玩家", this.method_5845());
            return false;
         } else {
            int combinedSlots = this.getCombinedGhostSlots(player);
            return this.suppressionQuota >= combinedSlots;
         }
      }
   }

   @Override
   protected void executeAttack(PlayerEntity player) {
      if (!this.isDeadlocked() && !this.isSuppressed() && player != null && player.method_5805()) {
         if (this.attackCooldown <= 0) {
            this.attackCooldown = 200;
            player.method_6092(new StatusEffectInstance(ModEffects.SILENCE, 60, 0, false, false, true));
            this.attackTimers.put(player.method_5667(), 60);
         }
      }
   }

   private void performActualAttack(PlayerEntity player) {
      if (player != null && player.method_5805()) {
         if (player.method_6059(ModEffects.SILENCE)) {
            int baseDamage = this.getSpiritualDamage();
            int actualDamage = baseDamage * this.suppressionQuota;
            PlayerEvents.handleSpiritDamage(player, actualDamage, actualDamage, ModDamageSources.ghost(this.method_37908()));
            if (!player.method_5805()) {
               this.increaseSuppressionQuota();
            }
         }
      }
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236) {
         if (this.musicCooldown > 0) {
            this.musicCooldown--;
         } else if (this.hasPlayedMusic) {
            this.hasPlayedMusic = false;
            this.playGhostOfficerEntrance();
         } else {
            this.playGhostOfficerEntrance();
         }

         if (this.isSuppressed()) {
            this.suppressedTimer++;
            if (this.suppressedTimer >= 100) {
               this.restartGhost();
            }
         } else {
            this.suppressedTimer = 0;
         }

         if (this.isDeadlocked()) {
            this.deadlockedTimer++;
            if (this.deadlockedTimer >= 100) {
               this.restartGhost();
            }
         } else {
            this.deadlockedTimer = 0;
         }

         if (!this.hasInitialSpawnedGhostSlaves) {
            for (int i = 0; i < 5; i++) {
               this.spawnGhostSlave();
            }

            this.hasInitialSpawnedGhostSlaves = true;
         }

         this.attackOtherGhostsInDomain();
         if (this.attackCooldown <= 0) {
            for (PlayerEntity player : this.method_37908().method_18456().stream().filter(x$0 -> this.isInGhostDomain(x$0)).collect(Collectors.toList())) {
               if (this.shouldAttackPlayer(player)) {
                  UUID playerUUID = player.method_5667();
                  if (!this.attackCountdowns.containsKey(playerUUID)) {
                     this.attackCountdowns.put(playerUUID, 60);
                  }

                  int countdown = this.attackCountdowns.get(playerUUID);
                  this.attackCountdowns.put(playerUUID, --countdown);
                  if (countdown <= 0) {
                     this.executeAttack(player);
                     this.attackCountdowns.remove(playerUUID);
                  }
               } else {
                  UUID playerUUID = player.method_5667();
                  if (this.attackCountdowns.containsKey(playerUUID)) {
                     this.attackCountdowns.remove(playerUUID);
                  }
               }
            }
         }

         if (!this.attackTimers.isEmpty()) {
            Iterator<Entry<UUID, Integer>> iterator = this.attackTimers.entrySet().iterator();

            while (iterator.hasNext()) {
               Entry<UUID, Integer> entry = iterator.next();
               UUID playerUUID = entry.getKey();
               int timer = entry.getValue();
               if (--timer <= 0) {
                  PlayerEntity player = this.method_37908().method_18470(playerUUID);
                  if (player != null && player.method_5805()) {
                     this.performActualAttack(player);
                  }

                  iterator.remove();
               } else {
                  entry.setValue(timer);
               }
            }
         }

         this.handleCoffinInteraction();
      }
   }

   private void attackOtherGhostsInDomain() {
      if (!this.isSuppressed() && !this.isDeadlocked() && this.attackCooldown <= 0) {
         double ghostDomainRadius = this.getGhostDomainRadius();

         for (GhostEntity ghost : this.method_37908()
            .method_8390(
               GhostEntity.class,
               this.method_5829().method_1014(ghostDomainRadius),
               ghostx -> ghostx != this && !ghostx.isDeadlocked() && !ghostx.isSuppressed() && ghostx.method_5805()
            )) {
            if (!(ghost instanceof GiantMaleCorpseGhostEntity) && !(ghost instanceof LuoQianGhostEntity)) {
               this.attackCooldown = 400;
               this.increaseSuppressionQuota();
               this.spawnBlackParticles(ghost.method_23317(), ghost.method_23318(), ghost.method_23321());
               this.notifyPlayersInDomain();
               GhostDeathHandler.markLegitimateRemoval(ghost);
               ghost.method_31472();
               break;
            }
         }
      }
   }

   private void handleCoffinInteraction() {
      if (this.isSuppressed() || this.isDeadlocked()) {
         this.targetCoffinPos = null;
      } else if (this.coffinEnterCooldown > 0) {
         this.coffinEnterCooldown--;
      } else {
         if (this.coffinSearchCooldown > 0) {
            this.coffinSearchCooldown--;
         }

         if (this.targetCoffinPos != null) {
            if (this.moveToCoffin()) {
               this.enterCoffin(this.targetCoffinPos);
            }
         } else {
            if (this.coffinSearchCooldown <= 0) {
               this.searchForCoffins();
               this.coffinSearchCooldown = 20;
            }
         }
      }
   }

   private void searchForCoffins() {
      BlockPos currentPos = this.method_24515();
      World world = this.method_37908();

      for (int x = -6; x <= 6; x++) {
         for (int y = -3; y <= 3; y++) {
            for (int z = -6; z <= 6; z++) {
               BlockPos checkPos = currentPos.method_10069(x, y, z);
               BlockState blockState = world.method_8320(checkPos);
               if (blockState.method_26204() instanceof GhostCoffinBlock
                  && blockState.method_28498(GhostCoffinBlock.OPEN)
                  && (Boolean)blockState.method_11654(GhostCoffinBlock.OPEN)
                  && blockState.method_28498(GhostCoffinBlock.OCCUPIED)
                  && !(Boolean)blockState.method_11654(GhostCoffinBlock.OCCUPIED)) {
                  GhostCoffinBlockEntity blockEntity = (GhostCoffinBlockEntity)world.method_8321(checkPos);
                  if (blockEntity != null && !blockEntity.hasStoredGhost()) {
                     this.targetCoffinPos = checkPos;
                     LOGGER.debug("鬼差发现打开的棺材，位置: {}", checkPos);
                     return;
                  }
               }
            }
         }
      }
   }

   private boolean moveToCoffin() {
      if (this.targetCoffinPos == null) {
         return false;
      }

      Vec3d targetPos = Vec3d.method_24953(this.targetCoffinPos);
      double distance = this.method_5707(targetPos);
      if (distance < 2.0) {
         return true;
      }

      this.method_5942().method_6337(targetPos.field_1352, targetPos.field_1351, targetPos.field_1350, 1.0);
      if (this.method_37908().method_8608()) {
         this.spawnMovementParticles();
      }

      return false;
   }

   private boolean enterCoffin(BlockPos coffinPos) {
      if (this.method_37908() != null && !this.method_31481()) {
         try {
            boolean success = GhostCoffinBlock.enterCoffin(this.method_37908(), coffinPos, this);
            if (success) {
               LOGGER.debug("鬼差成功进入棺材: {}，位置: {}", this.method_5667(), coffinPos);
               this.coffinEnterCooldown = 600;
               this.targetCoffinPos = null;
               this.coffinSearchCooldown = 0;
               GhostDeathHandler.markLegitimateRemoval(this);
               this.method_31472();
               return true;
            } else {
               LOGGER.warn("鬼差进入棺材失败: {}，位置: {}", this.method_5667(), coffinPos);
               this.coffinSearchCooldown = 100;
               return false;
            }
         } catch (Exception e) {
            LOGGER.error("鬼差进入棺材时发生异常", e);
            this.coffinSearchCooldown = 100;
            return false;
         }
      } else {
         return false;
      }
   }

   private void spawnMovementParticles() {
      World world = this.method_37908();
      Vec3d pos = this.method_19538();

      for (int i = 0; i < 5; i++) {
         double offsetX = (this.getGhostRandom().nextDouble() - 0.5) * 0.5;
         double offsetY = this.getGhostRandom().nextDouble() * 0.5;
         double offsetZ = (this.getGhostRandom().nextDouble() - 0.5) * 0.5;
         world.method_8406(ParticleTypes.field_11251, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 0.0, 0.0, 0.0);
      }
   }

   private void spawnEnterCoffinParticles() {
      if (this.targetCoffinPos != null) {
         World world = this.method_37908();
         Vec3d pos = Vec3d.method_24953(this.targetCoffinPos);
         if (world.method_8608()) {
            for (int i = 0; i < 20; i++) {
               double offsetX = (this.getGhostRandom().nextDouble() - 0.5) * 2.0;
               double offsetY = this.getGhostRandom().nextDouble() * 1.0;
               double offsetZ = (this.getGhostRandom().nextDouble() - 0.5) * 2.0;
               world.method_8406(ParticleTypes.field_11237, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 0.0, 0.1, 0.0);
            }
         } else {
            ServerWorld serverWorld = (ServerWorld)world;

            for (int i = 0; i < 20; i++) {
               double offsetX = (this.getGhostRandom().nextDouble() - 0.5) * 2.0;
               double offsetY = this.getGhostRandom().nextDouble() * 1.0;
               double offsetZ = (this.getGhostRandom().nextDouble() - 0.5) * 2.0;
               serverWorld.method_14199(
                  ParticleTypes.field_11237, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 1, 0.0, 0.1, 0.0, 0.0
               );
            }
         }
      }
   }

   private void notifyPlayersInDomain() {
      if (!this.method_37908().field_9236) {
         double radius = this.getGhostDomainRadius();

         for (PlayerEntity player : this.method_37908().method_18456().stream().filter(playerx -> {
            double dx = playerx.method_23317() - this.method_23317();
            double dy = playerx.method_23318() - this.method_23318();
            double dz = playerx.method_23321() - this.method_23321();
            double distanceSq = dx * dx + dy * dy + dz * dz;
            return distanceSq <= radius * radius;
         }).collect(Collectors.toList())) {
            player.method_7353(Text.method_43470("§c周围的鬼蜮变得更强大了..."), true);
         }
      }
   }

   public void restartGhost() {
      this.spawnBlackParticles(this.method_23317(), this.method_23318(), this.method_23321());
      if (this.hasCoffinNail()) {
         this.method_37908()
            .method_8649(new ItemEntity(this.method_37908(), this.method_23317(), this.method_23318(), this.method_23321(), this.getCoffinNail().method_7972()));
      }

      double newX = this.method_23317() + (this.getGhostRandom().nextDouble() - 0.5) * 50.0;
      double newY = this.method_23318();
      double newZ = this.method_23321() + (this.getGhostRandom().nextDouble() - 0.5) * 50.0;
      GhostOfficerEntity newGhost = new GhostOfficerEntity(this.method_5864(), this.method_37908());
      newGhost.method_5814(newX, newY, newZ);
      newGhost.setSuppressionQuota(this.suppressionQuota);
      this.method_37908().method_8649(newGhost);
      this.spawnBlackParticles(newX, newY, newZ);
      GhostDeathHandler.markLegitimateRemoval(this);
      this.method_31472();
   }

   private void playGhostOfficerEntrance() {
      if (!this.method_37908().field_9236) {
         ServerWorld world = (ServerWorld)this.method_37908();
         BlockPos pos = this.method_24515();
         world.method_8396(null, pos, ModSounds.GHOST_OFFICER_ENTRANCE, SoundCategory.field_15256, 0.7F, 1.0F);
         this.hasPlayedMusic = true;
         this.musicCooldown = 6000;
      }
   }

   private void spawnBlackParticles(double x, double y, double z) {
      if (this.method_37908().method_8608()) {
         World world = this.method_37908();

         for (int i = 0; i < 50; i++) {
            double offsetX = (this.getGhostRandom().nextDouble() - 0.5) * 4.0;
            double offsetY = (this.getGhostRandom().nextDouble() - 0.5) * 4.0;
            double offsetZ = (this.getGhostRandom().nextDouble() - 0.5) * 4.0;
            double velocityX = (this.getGhostRandom().nextDouble() - 0.5) * 0.2;
            double velocityY = (this.getGhostRandom().nextDouble() - 0.5) * 0.2;
            double velocityZ = (this.getGhostRandom().nextDouble() - 0.5) * 0.2;
            world.method_8406(ParticleTypes.field_11251, x + offsetX, y + offsetY, z + offsetZ, velocityX, velocityY, velocityZ);
            world.method_8406(ParticleTypes.field_11237, x + offsetX, y + offsetY, z + offsetZ, velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5);
         }
      } else {
         ServerWorld serverWorld = (ServerWorld)this.method_37908();

         for (int i = 0; i < 50; i++) {
            double offsetX = (this.getGhostRandom().nextDouble() - 0.5) * 4.0;
            double offsetY = (this.getGhostRandom().nextDouble() - 0.5) * 4.0;
            double offsetZ = (this.getGhostRandom().nextDouble() - 0.5) * 4.0;
            double velocityX = (this.getGhostRandom().nextDouble() - 0.5) * 0.2;
            double velocityY = (this.getGhostRandom().nextDouble() - 0.5) * 0.2;
            double velocityZ = (this.getGhostRandom().nextDouble() - 0.5) * 0.2;
            serverWorld.method_14199(ParticleTypes.field_11251, x + offsetX, y + offsetY, z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.0);
            serverWorld.method_14199(
               ParticleTypes.field_11237, x + offsetX, y + offsetY, z + offsetZ, 1, velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5, 0.0
            );
         }
      }
   }

   @Override
   public void method_5652(NbtCompound nbt) {
      super.method_5652(nbt);
      nbt.method_10569("MusicCooldown", this.musicCooldown);
      nbt.method_10556("HasPlayedMusic", this.hasPlayedMusic);
      nbt.method_10569("SuppressionQuota", this.suppressionQuota);
   }

   @Override
   public void method_5749(NbtCompound nbt) {
      super.method_5749(nbt);
      if (nbt.method_10545("MusicCooldown")) {
         this.musicCooldown = nbt.method_10550("MusicCooldown");
      }

      if (nbt.method_10545("HasPlayedMusic")) {
         this.hasPlayedMusic = nbt.method_10577("HasPlayedMusic");
      }

      if (nbt.method_10545("SuppressionQuota")) {
         this.suppressionQuota = nbt.method_10550("SuppressionQuota");
      }
   }
}
