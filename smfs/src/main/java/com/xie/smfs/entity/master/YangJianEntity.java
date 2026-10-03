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
      this.method_5665(Text.method_43470("§6杨戬"));
      this.method_5880(true);
      this.method_5684(true);
      this.method_5875(true);
      this.method_5803(true);
      this.method_5977(true);
   }

   public static Builder createYangJianAttributes() {
      return MobEntity.method_26828()
         .method_26868(EntityAttributes.field_23716, 100.0)
         .method_26868(EntityAttributes.field_23719, 0.3)
         .method_26868(EntityAttributes.field_23717, 32.0);
   }

   protected void method_5959() {
      this.field_6201.method_6277(0, new SwimGoal(this));
      this.field_6201.method_6277(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.field_6201.method_6277(2, new WanderAroundGoal(this, 0.6));
   }

   public boolean method_5643(DamageSource source, float amount) {
      return false;
   }

   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608()) {
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
            this.method_31472();
            return;
         }

         if (this.hasActivated && this.method_24828()) {
            this.method_18799(this.method_18798().method_1031(0.0, 0.2, 0.0));
         }
      } else {
         this.spawnGoldenParticles();
      }
   }

   private void spawnGoldenParticles() {
      World world = this.method_37908();
      Vec3d pos = this.method_19538();
      double coreRadius = 10.0 + Math.sin(world.method_8510() * 0.2) * 2.5;

      for (int i = 0; i < 36; i++) {
         double angle = i * 10 * Math.PI / 180.0;
         double x = pos.field_1352 + Math.cos(angle) * coreRadius;
         double z = pos.field_1350 + Math.sin(angle) * coreRadius;
         world.method_8406(ParticleTypes.field_28479, x, pos.field_1351 + 2.0, z, 0.0, 0.0, 0.0);
         world.method_8406(ParticleTypes.field_11248, x, pos.field_1351 + 2.0, z, 0.0, 0.05, 0.0);
      }

      for (int i = 0; i < 25; i++) {
         double x = pos.field_1352 + (world.field_9229.method_43058() - 0.5) * 20.0;
         double z = pos.field_1350 + (world.field_9229.method_43058() - 0.5) * 20.0;
         double y = pos.field_1351 - 5.0 + world.field_9229.method_43058() * 10.0;
         world.method_8406(ParticleTypes.field_28479, x, y, z, 0.0, 0.3, 0.0);
      }

      if (world.method_8510() % 20L == 0L) {
         for (int i = 0; i < 50; i++) {
            double angle = world.field_9229.method_43058() * 2.0 * Math.PI;
            double radius = world.field_9229.method_43058() * 10.0;
            double x = pos.field_1352 + Math.cos(angle) * radius;
            double z = pos.field_1350 + Math.sin(angle) * radius;
            world.method_8406(
               ParticleTypes.field_11248,
               x,
               pos.field_1351 + 3.0,
               z,
               (world.field_9229.method_43058() - 0.5) * 0.3,
               0.0,
               (world.field_9229.method_43058() - 0.5) * 0.3
            );
         }
      }

      double ringRadius = 10.0;

      for (int i = 0; i < 72; i++) {
         double angle = (i * 5 + world.method_8510() * 3L) * Math.PI / 180.0;
         double x = pos.field_1352 + Math.cos(angle) * ringRadius;
         double z = pos.field_1350 + Math.sin(angle) * ringRadius;
         world.method_8406(ParticleTypes.field_28479, x, pos.field_1351 + 4.0, z, 0.0, 0.0, 0.0);
      }

      double groundRadius = 10.0 + Math.cos(world.method_8510() * 0.25) * 2.5;

      for (int i = 0; i < 48; i++) {
         double angle = i * 7.5 * Math.PI / 180.0;
         double x = pos.field_1352 + Math.cos(angle) * groundRadius;
         double z = pos.field_1350 + Math.sin(angle) * groundRadius;
         world.method_8406(ParticleTypes.field_28479, x, pos.field_1351 + 0.5, z, 0.0, 0.02, 0.0);
      }

      for (int i = 0; i < 20; i++) {
         double x = pos.field_1352 + (world.field_9229.method_43058() - 0.5) * 15.0;
         double z = pos.field_1350 + (world.field_9229.method_43058() - 0.5) * 15.0;
         double y = pos.field_1351 + 1.0 + world.field_9229.method_43058() * 6.0;
         world.method_8406(
            ParticleTypes.field_28479, x, y, z, (world.field_9229.method_43058() - 0.5) * 0.1, -0.05, (world.field_9229.method_43058() - 0.5) * 0.1
         );
      }
   }

   private void shakePlayerCamera() {
      ServerWorld world = (ServerWorld)this.method_37908();
      BlockPos pos = this.method_24515();

      for (PlayerEntity player : world.method_8390(PlayerEntity.class, new Box(pos).method_1014(64.0), playerx -> true)) {
         double distance = player.method_19538().method_1022(this.method_19538());
         float shakeIntensity = (float)Math.max(0.1, 1.0 - distance / 64.0);
         float time = (260 - this.summonTimer) * 0.1F;
         float shakeX = (float)(Math.sin(time * 2.0) * 0.5 * shakeIntensity);
         float shakeY = (float)(Math.cos(time * 1.5) * 0.3 * shakeIntensity);
         float currentYaw = player.method_36454();
         float currentPitch = player.method_36455();
         player.method_36456(currentYaw + shakeX);
         player.method_36457(Math.max(-90.0F, Math.min(90.0F, currentPitch + shakeY)));
         player.method_5847(player.method_36454());
         player.method_5636(player.method_36454());
         player.field_5982 = player.method_36454();
         player.field_6004 = player.method_36455();
         player.field_6259 = player.method_36454();
      }
   }

   private void activateYangJianEffects() {
      ServerWorld world = (ServerWorld)this.method_37908();
      BlockPos pos = this.method_24515();
      world.method_27910(12000, 0, false, false);
      this.method_5814(this.method_23317(), this.method_23318() + 15.0, this.method_23321());
      List<PlayerEntity> nearbyPlayers = world.method_8390(PlayerEntity.class, new Box(pos).method_1014(20.0), playerx -> true);

      for (GhostEntity ghost : world.method_8390(GhostEntity.class, new Box(pos).method_1014(64.0), ghostx -> true)) {
         if (this.method_37908().method_8608()) {
            this.spawnGhostAbsorptionParticles(ghost);
         }

         if (ghost instanceof LuoQianGhostEntity luoQian) {
            luoQian.forceRestore();

            for (PlayerEntity player : world.method_8390(PlayerEntity.class, new Box(pos).method_1014(20.0), playerx -> true)) {
               player.method_7353(Text.method_43470("§c罗千：这我就放心了。"), false);
            }
         }

         GhostDeathHandler.markLegitimateRemoval(ghost);
         ghost.method_31472();
      }

      this.killHostileMobs(world, pos);

      for (PlayerEntity player : nearbyPlayers) {
         String playerName = player.method_5477().getString();

         for (StatusEffectInstance effect : player.method_6026()) {
            LOGGER.info("玩家 {} 清除前效果: {} 等级: {}", playerName, effect.method_5579().method_5560().getString(), effect.method_5578());
         }

         this.removeNegativeEffects(player);
         this.removeSuffixedGhostDomainEffects(player);
         Collection<StatusEffectInstance> effectsAfter = player.method_6026();
         LOGGER.info("玩家 {} 清除后有 {} 个效果", playerName, effectsAfter.size());

         for (StatusEffectInstance effect : effectsAfter) {
            LOGGER.info("玩家 {} 清除后效果: {} 等级: {}", playerName, effect.method_5579().method_5560().getString(), effect.method_5578());
         }

         player.method_6033(player.method_6063());
         player.method_6092(new StatusEffectInstance(StatusEffects.field_5898, 600, 1, false, false, true));
         player.method_6092(new StatusEffectInstance(ModEffects.GOLDEN_GHOST_DOMAIN, 6000, 0, false, false, true));
         if (!this.affectedPlayers.contains(player)) {
            this.affectedPlayers.add(player);
         }
      }
   }

   private void spawnGhostAbsorptionParticles(GhostEntity ghost) {
      World world = this.method_37908();
      Vec3d ghostPos = ghost.method_19538();
      Vec3d yangJianPos = this.method_19538();
      double dx = yangJianPos.field_1352 - ghostPos.field_1352;
      double dy = yangJianPos.field_1351 - ghostPos.field_1351;
      double dz = yangJianPos.field_1350 - ghostPos.field_1350;
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (distance > 0.0) {
         dx /= distance;
         dy /= distance;
         dz /= distance;
      }

      for (int i = 0; i < 30; i++) {
         double offsetX = (world.field_9229.method_43058() - 0.5) * 2.0;
         double offsetY = (world.field_9229.method_43058() - 0.5) * 2.0;
         double offsetZ = (world.field_9229.method_43058() - 0.5) * 2.0;
         double particleX = ghostPos.field_1352 + offsetX;
         double particleY = ghostPos.field_1351 + offsetY;
         double particleZ = ghostPos.field_1350 + offsetZ;
         double speed = 0.3 + world.field_9229.method_43058() * 0.2;
         double velocityX = dx * speed;
         double velocityY = dy * speed;
         double velocityZ = dz * speed;
         velocityX += (world.field_9229.method_43058() - 0.5) * 0.1;
         velocityY += (world.field_9229.method_43058() - 0.5) * 0.1;
         velocityZ += (world.field_9229.method_43058() - 0.5) * 0.1;
         world.method_8406(ParticleTypes.field_28479, particleX, particleY, particleZ, velocityX, velocityY, velocityZ);
         world.method_8406(ParticleTypes.field_11240, particleX, particleY, particleZ, velocityX * 0.8, velocityY * 0.8, velocityZ * 0.8);
      }

      world.method_8486(ghostPos.field_1352, ghostPos.field_1351, ghostPos.field_1350, SoundEvents.field_14703, SoundCategory.field_15256, 0.5F, 1.2F, false);
   }

   private void removeNegativeEffects(PlayerEntity player) {
      String playerName = player.method_5477().getString();
      Collection<StatusEffectInstance> effects = player.method_6026();
      List<StatusEffect> negativeEffects = new ArrayList<>();

      for (StatusEffectInstance effect : effects) {
         StatusEffect statusEffect = effect.method_5579();
         if (!statusEffect.method_5573()) {
            negativeEffects.add(statusEffect);
         }
      }

      for (StatusEffect effect : negativeEffects) {
         player.method_6016(effect);
      }

      if (negativeEffects.isEmpty()) {
         LOGGER.info("玩家 {} 没有需要移除的负面效果", playerName);
      }
   }

   private void removeSuffixedGhostDomainEffects(PlayerEntity player) {
      String playerName = player.method_5477().getString();
      LOGGER.info("玩家 {} 开始检查带后缀的鬼蜮效果", playerName);
      boolean foundTargetEffects = false;
      if (player.method_6059(ModEffects.RED_GHOST_DOMAIN_TARGET)) {
         player.method_6016(ModEffects.RED_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除红色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.method_6059(ModEffects.GREEN_GHOST_DOMAIN_TARGET)) {
         player.method_6016(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除绿色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.method_6059(ModEffects.BLUE_GHOST_DOMAIN_TARGET)) {
         player.method_6016(ModEffects.BLUE_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除蓝色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.method_6059(ModEffects.GRAY_GHOST_DOMAIN_TARGET)) {
         player.method_6016(ModEffects.GRAY_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除灰色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.method_6059(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET)) {
         player.method_6016(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除金色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.method_6059(ModEffects.PURPLE_GHOST_DOMAIN_TARGET)) {
         player.method_6016(ModEffects.PURPLE_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除紫色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.method_6059(ModEffects.BLACK_GHOST_DOMAIN_TARGET)) {
         player.method_6016(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除黑色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.method_6059(ModEffects.CYAN_GHOST_DOMAIN_TARGET)) {
         player.method_6016(ModEffects.CYAN_GHOST_DOMAIN_TARGET);
         LOGGER.info("玩家 {} 已移除青色鬼蜮目标效果", playerName);
         foundTargetEffects = true;
      }

      if (player.method_6059(ModEffects.THICK_FOG_TARGET)) {
         player.method_6016(ModEffects.THICK_FOG_TARGET);
         LOGGER.info("玩家 {} 已移除浓雾目标效果", playerName);
         foundTargetEffects = true;
      }

      if (!foundTargetEffects) {
         LOGGER.info("玩家 {} 没有发现带后缀的鬼蜮效果", playerName);
      }
   }

   private void killHostileMobs(ServerWorld world, BlockPos pos) {
      for (LivingEntity entity : world.method_8390(LivingEntity.class, new Box(pos).method_1014(32.0), entityx -> entityx instanceof Monster)) {
         entity.method_5768();
         world.method_8396(null, entity.method_24515(), SoundEvents.field_15152, SoundCategory.field_15251, 1.0F, 1.0F);
         world.method_14199(ParticleTypes.field_11236, entity.method_23317(), entity.method_23318() + 1.0, entity.method_23321(), 10, 0.5, 0.5, 0.5, 0.2);
      }
   }

   private void lockPlayerView() {
      ServerWorld world = (ServerWorld)this.method_37908();
      BlockPos pos = this.method_24515();
      List<PlayerEntity> nearbyPlayers = world.method_8390(PlayerEntity.class, new Box(pos).method_1014(64.0), player -> true);
      PlayerEntity nearestPlayer = null;
      double nearestDistance = Double.MAX_VALUE;

      for (PlayerEntity player : nearbyPlayers) {
         double distance = player.method_19538().method_1022(this.method_19538());
         if (distance < nearestDistance) {
            nearestDistance = distance;
            nearestPlayer = player;
         }
      }

      if (nearestPlayer != null) {
         Vec3d yangJianPos = this.method_19538();
         Vec3d playerPos = nearestPlayer.method_19538();
         double dx = playerPos.field_1352 - yangJianPos.field_1352;
         double dz = playerPos.field_1350 - yangJianPos.field_1350;
         float targetYaw = (float)(Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
         double dy = playerPos.field_1351 - yangJianPos.field_1351;
         double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
         float targetPitch = (float)(-Math.atan2(dy, horizontalDistance) * (180.0 / Math.PI));
         this.method_36456(targetYaw);
         this.method_36457(targetPitch);
         this.method_5847(targetYaw);
         this.method_5636(targetYaw);
         this.field_5982 = targetYaw;
         this.field_6004 = targetPitch;
         this.field_6259 = targetYaw;
      }
   }

   private void playSummonSound() {
      ServerWorld world = (ServerWorld)this.method_37908();
      BlockPos pos = this.method_24515();
      world.method_8396(null, pos, ModSounds.YANG_JIAN_ENTRANCE, SoundCategory.field_15256, 1.0F, 1.0F);
      world.method_8396(null, pos, ModSounds.YANG_JIAN_BGM, SoundCategory.field_15256, 0.8F, 1.0F);
   }

   private void clearPlayerGhostDomainEffects() {
      LOGGER.info("杨戬离开，开始清除受影响玩家的鬼蜮效果");
      LOGGER.info("受影响玩家数量: {}", this.affectedPlayers.size());

      for (PlayerEntity player : this.affectedPlayers) {
         if (player != null && !player.method_31481()) {
            String playerName = player.method_5477().getString();
            LOGGER.info("清除玩家 {} 的鬼蜮效果", playerName);
            Collection<StatusEffectInstance> effectsBefore = player.method_6026();
            LOGGER.info("玩家 {} 清除前有 {} 个效果", playerName, effectsBefore.size());

            for (StatusEffectInstance effect : effectsBefore) {
               LOGGER.info("玩家 {} 清除前效果: {} 等级: {}", playerName, effect.method_5579().method_5560().getString(), effect.method_5578());
            }

            GhostDomainManager.disableGhostDomain(player);
            LOGGER.info("玩家 {} 已通过GhostDomainManager清除鬼蜮效果", playerName);
            player.method_6016(ModEffects.GOLDEN_GHOST_DOMAIN);
            LOGGER.info("玩家 {} 已移除金色鬼蜮效果", playerName);
            Collection<StatusEffectInstance> effectsAfter = player.method_6026();
            LOGGER.info("玩家 {} 清除后有 {} 个效果", playerName, effectsAfter.size());

            for (StatusEffectInstance effect : effectsAfter) {
               LOGGER.info("玩家 {} 清除后效果: {} 等级: {}", playerName, effect.method_5579().method_5560().getString(), effect.method_5578());
            }
         } else {
            LOGGER.warn("发现无效或已移除的玩家，跳过清除操作");
         }
      }

      LOGGER.info("杨戬离开时鬼蜮效果清除完成");
      this.affectedPlayers.clear();
   }

   public ActionResult method_5992(PlayerEntity player, Hand hand) {
      return ActionResult.field_5811;
   }

   public boolean method_5974(double distanceSquared) {
      return false;
   }

   public void method_5652(NbtCompound nbt) {
      super.method_5652(nbt);
      nbt.method_10569("SummonTimer", this.summonTimer);
      nbt.method_10556("HasActivated", this.hasActivated);
      nbt.method_10556("HasPlayedSound", this.hasPlayedSound);
      int[] affectedPlayerUUIDs = new int[this.affectedPlayers.size()];

      for (int i = 0; i < this.affectedPlayers.size(); i++) {
         affectedPlayerUUIDs[i] = this.affectedPlayers.get(i).method_5628();
      }

      nbt.method_10539("AffectedPlayers", affectedPlayerUUIDs);
   }

   public void method_5749(NbtCompound nbt) {
      super.method_5749(nbt);
      if (nbt.method_10545("SummonTimer")) {
         this.summonTimer = nbt.method_10550("SummonTimer");
      }

      if (nbt.method_10545("HasActivated")) {
         this.hasActivated = nbt.method_10577("HasActivated");
      }

      if (nbt.method_10545("HasPlayedSound")) {
         this.hasPlayedSound = nbt.method_10577("HasPlayedSound");
      }

      if (nbt.method_10545("AffectedPlayers")) {
         int[] affectedPlayerUUIDs = nbt.method_10561("AffectedPlayers");
         this.affectedPlayers.clear();

         for (int playerId : affectedPlayerUUIDs) {
            Entity entity = this.method_37908().method_8469(playerId);
            if (entity instanceof PlayerEntity) {
               this.affectedPlayers.add((PlayerEntity)entity);
            }
         }
      }
   }

   public void method_36209() {
      super.method_36209();
      this.clearPlayerGhostDomainEffects();
   }
}
