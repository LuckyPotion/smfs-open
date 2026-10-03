package com.xie.smfs.entity.master;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.ghost.LuoQianGhostEntity;
import com.xie.smfs.event.GhostDeathHandler;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModSounds;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YangJianEntity extends VillagerEntity {
   private static final Logger LOGGER = LoggerFactory.getLogger("YangJianEntity");
   private int summonTimer = 260;
   private boolean hasActivated = false;
   private boolean hasPlayedSound = false;
   private List<PlayerEntity> affectedPlayers = new ArrayList<>();

   public YangJianEntity(EntityType<? extends VillagerEntity> entityType, World world) {
      super(entityType, world);
      this.setCustomName(Text.literal("§6杨戬"));
      this.setCustomNameVisible(true);
      this.setInvulnerable(true);
      this.setNoGravity(true);
      this.setSilent(true);
      this.setAiDisabled(true);
   }

   public static Builder createYangJianAttributes() {
      return MobEntity.createMobAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
   }

   protected void initGoals() {
      this.goalSelector.add(0, new SwimGoal(this));
      this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(2, new WanderAroundGoal(this, 0.6));
   }

   public boolean damage(DamageSource source, float amount) {
      return false;
   }

   public void tick() {
      super.tick();
      if (!this.getWorld().isClient()) {
         if (!this.hasPlayedSound) {
            this.playSummonSound();
            this.hasPlayedSound = true;
         }

         if (!this.hasActivated) {
            this.activateYangJianEffects();
            this.hasActivated = true;
         }

         if (this.hasActivated) {
            this.lockPlayerView();
            this.shakePlayerCamera();
         }

         this.summonTimer--;
         if (this.summonTimer <= 0) {
            this.clearPlayerGhostDomainEffects();
            this.discard();
            return;
         }

         if (this.hasActivated && this.isOnGround()) {
            this.setVelocity(this.getVelocity().add(0.0, 0.2, 0.0));
         }
      } else {
         this.spawnGoldenParticles();
      }
   }

   private void spawnGoldenParticles() {
      World world = this.getWorld();
      Vec3d pos = this.getPos();
      double coreRadius = 10.0 + Math.sin(world.getTime() * 0.2) * 2.5;

      for (int i = 0; i < 36; i++) {
         double angle = i * 10 * Math.PI / 180.0;
         double x = pos.x + Math.cos(angle) * coreRadius;
         double z = pos.z + Math.sin(angle) * coreRadius;
         world.addParticle(ParticleTypes.GLOW, x, pos.y + 2.0, z, 0.0, 0.0, 0.0);
         world.addParticle(ParticleTypes.FIREWORK, x, pos.y + 2.0, z, 0.0, 0.05, 0.0);
      }

      for (int i = 0; i < 25; i++) {
         double x = pos.x + (world.random.nextDouble() - 0.5) * 20.0;
         double z = pos.z + (world.random.nextDouble() - 0.5) * 20.0;
         double y = pos.y - 5.0 + world.random.nextDouble() * 10.0;
         world.addParticle(ParticleTypes.GLOW, x, y, z, 0.0, 0.3, 0.0);
      }

      if (world.getTime() % 20L == 0L) {
         for (int i = 0; i < 50; i++) {
            double angle = world.random.nextDouble() * 2.0 * Math.PI;
            double radius = world.random.nextDouble() * 10.0;
            double x = pos.x + Math.cos(angle) * radius;
            double z = pos.z + Math.sin(angle) * radius;
            world.addParticle(ParticleTypes.FIREWORK, x, pos.y + 3.0, z, (world.random.nextDouble() - 0.5) * 0.3, 0.0, (world.random.nextDouble() - 0.5) * 0.3);
         }
      }

      double ringRadius = 10.0;

      for (int i = 0; i < 72; i++) {
         double angle = (i * 5 + world.getTime() * 3L) * Math.PI / 180.0;
         double x = pos.x + Math.cos(angle) * ringRadius;
         double z = pos.z + Math.sin(angle) * ringRadius;
         world.addParticle(ParticleTypes.GLOW, x, pos.y + 4.0, z, 0.0, 0.0, 0.0);
      }

      double groundRadius = 10.0 + Math.cos(world.getTime() * 0.25) * 2.5;

      for (int i = 0; i < 48; i++) {
         double angle = i * 7.5 * Math.PI / 180.0;
         double x = pos.x + Math.cos(angle) * groundRadius;
         double z = pos.z + Math.sin(angle) * groundRadius;
         world.addParticle(ParticleTypes.GLOW, x, pos.y + 0.5, z, 0.0, 0.02, 0.0);
      }

      for (int i = 0; i < 20; i++) {
         double x = pos.x + (world.random.nextDouble() - 0.5) * 15.0;
         double z = pos.z + (world.random.nextDouble() - 0.5) * 15.0;
         double y = pos.y + 1.0 + world.random.nextDouble() * 6.0;
         world.addParticle(ParticleTypes.GLOW, x, y, z, (world.random.nextDouble() - 0.5) * 0.1, -0.05, (world.random.nextDouble() - 0.5) * 0.1);
      }
   }

   private void shakePlayerCamera() {
      ServerWorld world = (ServerWorld)this.getWorld();
      BlockPos pos = this.getBlockPos();

      for (PlayerEntity player : world.getEntitiesByClass(PlayerEntity.class, new Box(pos).expand(64.0), playerx -> true)) {
         double distance = player.getPos().distanceTo(this.getPos());
         float shakeIntensity = (float)Math.max(0.1, 1.0 - distance / 64.0);
         float time = (260 - this.summonTimer) * 0.1F;
         float shakeX = (float)(Math.sin(time * 2.0) * 0.5 * shakeIntensity);
         float shakeY = (float)(Math.cos(time * 1.5) * 0.3 * shakeIntensity);
         float currentYaw = player.getYaw();
         float currentPitch = player.getPitch();
         player.setYaw(currentYaw + shakeX);
         player.setPitch(Math.max(-90.0F, Math.min(90.0F, currentPitch + shakeY)));
         player.setHeadYaw(player.getYaw());
         player.setBodyYaw(player.getYaw());
         player.prevYaw = player.getYaw();
         player.prevPitch = player.getPitch();
         player.prevHeadYaw = player.getYaw();
      }
   }

   private void activateYangJianEffects() {
      ServerWorld world = (ServerWorld)this.getWorld();
      BlockPos pos = this.getBlockPos();
      world.setWeather(12000, 0, false, false);
      this.setPosition(this.getX(), this.getY() + 15.0, this.getZ());
      List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos).expand(20.0), playerx -> true);

      for (GhostEntity ghost : world.getEntitiesByClass(GhostEntity.class, new Box(pos).expand(64.0), ghostx -> true)) {
         if (this.getWorld().isClient()) {
            this.spawnGhostAbsorptionParticles(ghost);
         }

         if (ghost instanceof LuoQianGhostEntity luoQian) {
            luoQian.forceRestore();

            for (PlayerEntity player : world.getEntitiesByClass(PlayerEntity.class, new Box(pos).expand(20.0), playerx -> true)) {
               player.sendMessage(Text.literal("§c罗千：这我就放心了。"), false);
            }
         }

         GhostDeathHandler.markLegitimateRemoval(ghost);
         ghost.discard();
      }

      this.killHostileMobs(world, pos);

      for (PlayerEntity player : nearbyPlayers) {
         String playerName = player.getName().getString();

         for (StatusEffectInstance effect : player.getStatusEffects()) {
            LOGGER.info("玩家 {} 清除前效果: {} 等级: {}", playerName, effect.getEffectType().getName().getString(), effect.getAmplifier());
         }

         this.removeNegativeEffects(player);
         this.removeSuffixedGhostDomainEffects(player);
         Collection<StatusEffectInstance> effectsAfter = player.getStatusEffects();
         LOGGER.info("玩家 {} 清除后有 {} 个效果", playerName, effectsAfter.size());

         for (StatusEffectInstance effect : effectsAfter) {
            LOGGER.info("玩家 {} 清除后效果: {} 等级: {}", playerName, effect.getEffectType().getName().getString(), effect.getAmplifier());
         }

         player.setHealth(player.getMaxHealth());
         player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 600, 1, false, false, true));
         player.addStatusEffect(new StatusEffectInstance(ModEffects.GOLDEN_GHOST_DOMAIN, 6000, 0, false, false, true));
         if (!this.affectedPlayers.contains(player)) {
            this.affectedPlayers.add(player);
         }
      }
   }

   private void spawnGhostAbsorptionParticles(GhostEntity ghost) {
      World world = this.getWorld();
      Vec3d ghostPos = ghost.getPos();
      Vec3d yangJianPos = this.getPos();
      double dx = yangJianPos.x - ghostPos.x;
      double dy = yangJianPos.y - ghostPos.y;
      double dz = yangJianPos.z - ghostPos.z;
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (distance > 0.0) {
         dx /= distance;
         dy /= distance;
         dz /= distance;
      }

      for (int i = 0; i < 30; i++) {
         double offsetX = (world.random.nextDouble() - 0.5) * 2.0;
         double offsetY = (world.random.nextDouble() - 0.5) * 2.0;
         double offsetZ = (world.random.nextDouble() - 0.5) * 2.0;
         double particleX = ghostPos.x + offsetX;
         double particleY = ghostPos.y + offsetY;
         double particleZ = ghostPos.z + offsetZ;
         double speed = 0.3 + world.random.nextDouble() * 0.2;
         double velocityX = dx * speed;
         double velocityY = dy * speed;
         double velocityZ = dz * speed;
         velocityX += (world.random.nextDouble() - 0.5) * 0.1;
         velocityY += (world.random.nextDouble() - 0.5) * 0.1;
         velocityZ += (world.random.nextDouble() - 0.5) * 0.1;
         world.addParticle(ParticleTypes.GLOW, particleX, particleY, particleZ, velocityX, velocityY, velocityZ);
         world.addParticle(ParticleTypes.FLAME, particleX, particleY, particleZ, velocityX * 0.8, velocityY * 0.8, velocityZ * 0.8);
      }

      world.playSound(ghostPos.x, ghostPos.y, ghostPos.z, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.AMBIENT, 0.5F, 1.2F, false);
   }

   private void removeNegativeEffects(PlayerEntity player) {
      String playerName = player.getName().getString();
      Collection<StatusEffectInstance> effects = player.getStatusEffects();
      List<StatusEffect> negativeEffects = new ArrayList<>();

      for (StatusEffectInstance effect : effects) {
         StatusEffect statusEffect = effect.getEffectType();
         if (!statusEffect.isBeneficial()) {
            negativeEffects.add(statusEffect);
         }
      }

      for (StatusEffect effect : negativeEffects) {
         player.removeStatusEffect(effect);
      }

      if (negativeEffects.isEmpty()) {
         LOGGER.info("玩家 {} 没有需要移除的负面效果", playerName);
      }
   }

   private void removeSuffixedGhostDomainEffects(PlayerEntity player) {
      String playerName = player.getName().getString();
      LOGGER.info("玩家 {} 开始检查带后缀的鬼蜮效果", playerName);
      boolean foundTargetEffects = false;
      if (player.hasStatusEffect(ModEffects.RED_GHOST_DOMAIN_TARGET)) {
         player.removeStatusEffect(ModEffects.RED_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除红色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.hasStatusEffect(ModEffects.GREEN_GHOST_DOMAIN_TARGET)) {
         player.removeStatusEffect(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除绿色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.hasStatusEffect(ModEffects.BLUE_GHOST_DOMAIN_TARGET)) {
         player.removeStatusEffect(ModEffects.BLUE_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除蓝色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.hasStatusEffect(ModEffects.GRAY_GHOST_DOMAIN_TARGET)) {
         player.removeStatusEffect(ModEffects.GRAY_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除灰色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.hasStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET)) {
         player.removeStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除金色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.hasStatusEffect(ModEffects.PURPLE_GHOST_DOMAIN_TARGET)) {
         player.removeStatusEffect(ModEffects.PURPLE_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除紫色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.hasStatusEffect(ModEffects.BLACK_GHOST_DOMAIN_TARGET)) {
         player.removeStatusEffect(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除黑色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.hasStatusEffect(ModEffects.CYAN_GHOST_DOMAIN_TARGET)) {
         player.removeStatusEffect(ModEffects.CYAN_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除青色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.hasStatusEffect(ModEffects.THICK_FOG_TARGET)) {
         player.removeStatusEffect(ModEffects.THICK_FOG_TARGET);
         LOGGER.info("玩家 {} 已移除浓雾目标效果", playerName);
         foundTargetEffects = true;
      }

      if (!foundTargetEffects) {
         LOGGER.info("玩家 {} 没有发现带后缀的鬼蜮效果", playerName);
      }
   }

   private void killHostileMobs(ServerWorld world, BlockPos pos) {
      for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, new Box(pos).expand(32.0), entityx -> entityx instanceof Monster)) {
         entity.kill();
         world.playSound(null, entity.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.0F, 1.0F);
         world.spawnParticles(ParticleTypes.EXPLOSION, entity.getX(), entity.getY() + 1.0, entity.getZ(), 10, 0.5, 0.5, 0.5, 0.2);
      }
   }

   private void lockPlayerView() {
      ServerWorld world = (ServerWorld)this.getWorld();
      BlockPos pos = this.getBlockPos();
      List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos).expand(64.0), player -> true);
      PlayerEntity nearestPlayer = null;
      double nearestDistance = Double.MAX_VALUE;

      for (PlayerEntity player : nearbyPlayers) {
         double distance = player.getPos().distanceTo(this.getPos());
         if (distance < nearestDistance) {
            nearestDistance = distance;
            nearestPlayer = player;
         }
      }

      if (nearestPlayer != null) {
         Vec3d yangJianPos = this.getPos();
         Vec3d playerPos = nearestPlayer.getPos();
         double dx = playerPos.x - yangJianPos.x;
         double dz = playerPos.z - yangJianPos.z;
         float targetYaw = (float)(Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
         double dy = playerPos.y - yangJianPos.y;
         double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
         float targetPitch = (float)(-Math.atan2(dy, horizontalDistance) * (180.0 / Math.PI));
         this.setYaw(targetYaw);
         this.setPitch(targetPitch);
         this.setHeadYaw(targetYaw);
         this.setBodyYaw(targetYaw);
         this.prevYaw = targetYaw;
         this.prevPitch = targetPitch;
         this.prevHeadYaw = targetYaw;
      }
   }

   private void playSummonSound() {
      ServerWorld world = (ServerWorld)this.getWorld();
      BlockPos pos = this.getBlockPos();
      world.playSound(null, pos, ModSounds.YANG_JIAN_ENTRANCE, SoundCategory.AMBIENT, 1.0F, 1.0F);
      world.playSound(null, pos, ModSounds.YANG_JIAN_BGM, SoundCategory.AMBIENT, 0.8F, 1.0F);
   }

   private void clearPlayerGhostDomainEffects() {
      LOGGER.info("杨戬离开，开始清除受影响玩家的鬼蜮效果");
      LOGGER.info("受影响玩家数量: {}", this.affectedPlayers.size());

      for (PlayerEntity player : this.affectedPlayers) {
         if (player != null && !player.isRemoved()) {
            String playerName = player.getName().getString();
            LOGGER.info("清除玩家 {} 的鬼蜮效果", playerName);
            Collection<StatusEffectInstance> effectsBefore = player.getStatusEffects();
            LOGGER.info("玩家 {} 清除前有 {} 个效果", playerName, effectsBefore.size());

            for (StatusEffectInstance effect : effectsBefore) {
               LOGGER.info("玩家 {} 清除前效果: {} 等级: {}", playerName, effect.getEffectType().getName().getString(), effect.getAmplifier());
            }

            GhostDomainManager.disableGhostDomain(player);
            LOGGER.info("玩家 {} 已通过GhostDomainManager清除鬼蜮效果", playerName);
            player.removeStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN);
            LOGGER.info("玩家 {} 已移除金色鬼蜮效果", playerName);
            Collection<StatusEffectInstance> effectsAfter = player.getStatusEffects();
            LOGGER.info("玩家 {} 清除后有 {} 个效果", playerName, effectsAfter.size());

            for (StatusEffectInstance effect : effectsAfter) {
               LOGGER.info("玩家 {} 清除后效果: {} 等级: {}", playerName, effect.getEffectType().getName().getString(), effect.getAmplifier());
            }
         } else {
            LOGGER.warn("发现无效或已移除的玩家，跳过清除操作");
         }
      }

      LOGGER.info("杨戬离开时鬼蜮效果清除完成");
      this.affectedPlayers.clear();
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      return ActionResult.PASS;
   }

   public boolean canImmediatelyDespawn(double distanceSquared) {
      return false;
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("SummonTimer", this.summonTimer);
      nbt.putBoolean("HasActivated", this.hasActivated);
      nbt.putBoolean("HasPlayedSound", this.hasPlayedSound);
      int[] affectedPlayerUUIDs = new int[this.affectedPlayers.size()];

      for (int i = 0; i < this.affectedPlayers.size(); i++) {
         affectedPlayerUUIDs[i] = this.affectedPlayers.get(i).getId();
      }

      nbt.putIntArray("AffectedPlayers", affectedPlayerUUIDs);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("SummonTimer")) {
         this.summonTimer = nbt.getInt("SummonTimer");
      }

      if (nbt.contains("HasActivated")) {
         this.hasActivated = nbt.getBoolean("HasActivated");
      }

      if (nbt.contains("HasPlayedSound")) {
         this.hasPlayedSound = nbt.getBoolean("HasPlayedSound");
      }

      if (nbt.contains("AffectedPlayers")) {
         int[] affectedPlayerUUIDs = nbt.getIntArray("AffectedPlayers");
         this.affectedPlayers.clear();

         for (int playerId : affectedPlayerUUIDs) {
            Entity entity = this.getWorld().getEntityById(playerId);
            if (entity instanceof PlayerEntity) {
               this.affectedPlayers.add((PlayerEntity)entity);
            }
         }
      }
   }

   public void onRemoved() {
      super.onRemoved();
      this.clearPlayerGhostDomainEffects();
   }
}
