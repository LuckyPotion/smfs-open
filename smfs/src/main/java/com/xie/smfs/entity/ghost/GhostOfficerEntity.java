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
      Box searchBox = Box.of(player.getPos(), 6.0, 6.0, 6.0);
      List<PlayerEntity> nearbyPlayers = this.getWorld()
         .getPlayers()
         .stream()
         .filter(pl -> pl.isAlive() && pl.squaredDistanceTo(player) <= 9.0)
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
         List<GhostEntity> ghostsInDomain = this.getWorld()
            .getEntitiesByClass(
               GhostEntity.class,
               this.getBoundingBox().expand(ghostDomainRadius),
               ghost -> ghost != this && !ghost.isDeadlocked() && !ghost.isSuppressed() && ghost.isAlive()
            );
         if (!ghostsInDomain.isEmpty()) {
            LOGGER.debug("鬼差 {} 发现鬼域内有其他鬼，优先攻击鬼而不是玩家", this.getUuidAsString());
            return false;
         } else {
            int combinedSlots = this.getCombinedGhostSlots(player);
            return this.suppressionQuota >= combinedSlots;
         }
      }
   }

   @Override
   protected void executeAttack(PlayerEntity player) {
      if (!this.isDeadlocked() && !this.isSuppressed() && player != null && player.isAlive()) {
         if (this.attackCooldown <= 0) {
            this.attackCooldown = 200;
            player.addStatusEffect(new StatusEffectInstance(ModEffects.SILENCE, 60, 0, false, false, true));
            this.attackTimers.put(player.getUuid(), 60);
         }
      }
   }

   private void performActualAttack(PlayerEntity player) {
      if (player != null && player.isAlive()) {
         if (player.hasStatusEffect(ModEffects.SILENCE)) {
            int baseDamage = this.getSpiritualDamage();
            int actualDamage = baseDamage * this.suppressionQuota;
            PlayerEvents.handleSpiritDamage(player, actualDamage, actualDamage, ModDamageSources.ghost(this.getWorld()));
            if (!player.isAlive()) {
               this.increaseSuppressionQuota();
            }
         }
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient) {
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
            for (PlayerEntity player : this.getWorld().getPlayers().stream().filter(x$0 -> this.isInGhostDomain(x$0)).collect(Collectors.toList())) {
               if (this.shouldAttackPlayer(player)) {
                  UUID playerUUID = player.getUuid();
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
                  UUID playerUUID = player.getUuid();
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
                  PlayerEntity player = this.getWorld().getPlayerByUuid(playerUUID);
                  if (player != null && player.isAlive()) {
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

         for (GhostEntity ghost : this.getWorld()
            .getEntitiesByClass(
               GhostEntity.class,
               this.getBoundingBox().expand(ghostDomainRadius),
               ghostx -> ghostx != this && !ghostx.isDeadlocked() && !ghostx.isSuppressed() && ghostx.isAlive()
            )) {
            if (!(ghost instanceof GiantMaleCorpseGhostEntity) && !(ghost instanceof LuoQianGhostEntity)) {
               this.attackCooldown = 400;
               this.increaseSuppressionQuota();
               this.spawnBlackParticles(ghost.getX(), ghost.getY(), ghost.getZ());
               this.notifyPlayersInDomain();
               GhostDeathHandler.markLegitimateRemoval(ghost);
               ghost.discard();
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
      BlockPos currentPos = this.getBlockPos();
      World world = this.getWorld();

      for (int x = -6; x <= 6; x++) {
         for (int y = -3; y <= 3; y++) {
            for (int z = -6; z <= 6; z++) {
               BlockPos checkPos = currentPos.add(x, y, z);
               BlockState blockState = world.getBlockState(checkPos);
               if (blockState.getBlock() instanceof GhostCoffinBlock
                  && blockState.contains(GhostCoffinBlock.OPEN)
                  && (Boolean)blockState.get(GhostCoffinBlock.OPEN)
                  && blockState.contains(GhostCoffinBlock.OCCUPIED)
                  && !(Boolean)blockState.get(GhostCoffinBlock.OCCUPIED)) {
                  GhostCoffinBlockEntity blockEntity = (GhostCoffinBlockEntity)world.getBlockEntity(checkPos);
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

      Vec3d targetPos = Vec3d.ofCenter(this.targetCoffinPos);
      double distance = this.squaredDistanceTo(targetPos);
      if (distance < 2.0) {
         return true;
      }

      this.getNavigation().startMovingTo(targetPos.x, targetPos.y, targetPos.z, 1.0);
      if (this.getWorld().isClient()) {
         this.spawnMovementParticles();
      }

      return false;
   }

   private boolean enterCoffin(BlockPos coffinPos) {
      if (this.getWorld() != null && !this.isRemoved()) {
         try {
            boolean success = GhostCoffinBlock.enterCoffin(this.getWorld(), coffinPos, this);
            if (success) {
               LOGGER.debug("鬼差成功进入棺材: {}，位置: {}", this.getUuid(), coffinPos);
               this.coffinEnterCooldown = 600;
               this.targetCoffinPos = null;
               this.coffinSearchCooldown = 0;
               GhostDeathHandler.markLegitimateRemoval(this);
               this.discard();
               return true;
            } else {
               LOGGER.warn("鬼差进入棺材失败: {}，位置: {}", this.getUuid(), coffinPos);
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
      World world = this.getWorld();
      Vec3d pos = this.getPos();

      for (int i = 0; i < 5; i++) {
         double offsetX = (this.getGhostRandom().nextDouble() - 0.5) * 0.5;
         double offsetY = this.getGhostRandom().nextDouble() * 0.5;
         double offsetZ = (this.getGhostRandom().nextDouble() - 0.5) * 0.5;
         world.addParticle(ParticleTypes.SMOKE, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 0.0, 0.0, 0.0);
      }
   }

   private void spawnEnterCoffinParticles() {
      if (this.targetCoffinPos != null) {
         World world = this.getWorld();
         Vec3d pos = Vec3d.ofCenter(this.targetCoffinPos);
         if (world.isClient()) {
            for (int i = 0; i < 20; i++) {
               double offsetX = (this.getGhostRandom().nextDouble() - 0.5) * 2.0;
               double offsetY = this.getGhostRandom().nextDouble() * 1.0;
               double offsetZ = (this.getGhostRandom().nextDouble() - 0.5) * 2.0;
               world.addParticle(ParticleTypes.LARGE_SMOKE, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 0.0, 0.1, 0.0);
            }
         } else {
            ServerWorld serverWorld = (ServerWorld)world;

            for (int i = 0; i < 20; i++) {
               double offsetX = (this.getGhostRandom().nextDouble() - 0.5) * 2.0;
               double offsetY = this.getGhostRandom().nextDouble() * 1.0;
               double offsetZ = (this.getGhostRandom().nextDouble() - 0.5) * 2.0;
               serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, 0.0, 0.1, 0.0, 0.0);
            }
         }
      }
   }

   private void notifyPlayersInDomain() {
      if (!this.getWorld().isClient) {
         double radius = this.getGhostDomainRadius();

         for (PlayerEntity player : this.getWorld().getPlayers().stream().filter(playerx -> {
            double dx = playerx.getX() - this.getX();
            double dy = playerx.getY() - this.getY();
            double dz = playerx.getZ() - this.getZ();
            double distanceSq = dx * dx + dy * dy + dz * dz;
            return distanceSq <= radius * radius;
         }).collect(Collectors.toList())) {
            player.sendMessage(Text.literal("§c周围的鬼蜮变得更强大了..."), true);
         }
      }
   }

   public void restartGhost() {
      this.spawnBlackParticles(this.getX(), this.getY(), this.getZ());
      if (this.hasCoffinNail()) {
         this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), this.getCoffinNail().copy()));
      }

      double newX = this.getX() + (this.getGhostRandom().nextDouble() - 0.5) * 50.0;
      double newY = this.getY();
      double newZ = this.getZ() + (this.getGhostRandom().nextDouble() - 0.5) * 50.0;
      GhostOfficerEntity newGhost = new GhostOfficerEntity(this.getType(), this.getWorld());
      newGhost.setPosition(newX, newY, newZ);
      newGhost.setSuppressionQuota(this.suppressionQuota);
      this.getWorld().spawnEntity(newGhost);
      this.spawnBlackParticles(newX, newY, newZ);
      GhostDeathHandler.markLegitimateRemoval(this);
      this.discard();
   }

   private void playGhostOfficerEntrance() {
      if (!this.getWorld().isClient) {
         ServerWorld world = (ServerWorld)this.getWorld();
         BlockPos pos = this.getBlockPos();
         world.playSound(null, pos, ModSounds.GHOST_OFFICER_ENTRANCE, SoundCategory.AMBIENT, 0.7F, 1.0F);
         this.hasPlayedMusic = true;
         this.musicCooldown = 6000;
      }
   }

   private void spawnBlackParticles(double x, double y, double z) {
      if (this.getWorld().isClient()) {
         World world = this.getWorld();

         for (int i = 0; i < 50; i++) {
            double offsetX = (this.getGhostRandom().nextDouble() - 0.5) * 4.0;
            double offsetY = (this.getGhostRandom().nextDouble() - 0.5) * 4.0;
            double offsetZ = (this.getGhostRandom().nextDouble() - 0.5) * 4.0;
            double velocityX = (this.getGhostRandom().nextDouble() - 0.5) * 0.2;
            double velocityY = (this.getGhostRandom().nextDouble() - 0.5) * 0.2;
            double velocityZ = (this.getGhostRandom().nextDouble() - 0.5) * 0.2;
            world.addParticle(ParticleTypes.SMOKE, x + offsetX, y + offsetY, z + offsetZ, velocityX, velocityY, velocityZ);
            world.addParticle(ParticleTypes.LARGE_SMOKE, x + offsetX, y + offsetY, z + offsetZ, velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5);
         }
      } else {
         ServerWorld serverWorld = (ServerWorld)this.getWorld();

         for (int i = 0; i < 50; i++) {
            double offsetX = (this.getGhostRandom().nextDouble() - 0.5) * 4.0;
            double offsetY = (this.getGhostRandom().nextDouble() - 0.5) * 4.0;
            double offsetZ = (this.getGhostRandom().nextDouble() - 0.5) * 4.0;
            double velocityX = (this.getGhostRandom().nextDouble() - 0.5) * 0.2;
            double velocityY = (this.getGhostRandom().nextDouble() - 0.5) * 0.2;
            double velocityZ = (this.getGhostRandom().nextDouble() - 0.5) * 0.2;
            serverWorld.spawnParticles(ParticleTypes.SMOKE, x + offsetX, y + offsetY, z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.0);
            serverWorld.spawnParticles(
               ParticleTypes.LARGE_SMOKE, x + offsetX, y + offsetY, z + offsetZ, 1, velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5, 0.0
            );
         }
      }
   }

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("MusicCooldown", this.musicCooldown);
      nbt.putBoolean("HasPlayedMusic", this.hasPlayedMusic);
      nbt.putInt("SuppressionQuota", this.suppressionQuota);
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("MusicCooldown")) {
         this.musicCooldown = nbt.getInt("MusicCooldown");
      }

      if (nbt.contains("HasPlayedMusic")) {
         this.hasPlayedMusic = nbt.getBoolean("HasPlayedMusic");
      }

      if (nbt.contains("SuppressionQuota")) {
         this.suppressionQuota = nbt.getInt("SuppressionQuota");
      }
   }
}
