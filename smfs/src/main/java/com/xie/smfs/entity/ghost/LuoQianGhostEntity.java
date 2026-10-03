package com.xie.smfs.entity.ghost;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.other.FloatingDirtEntity;
import com.xie.smfs.entity.other.GraveWarningEntity;
import com.xie.smfs.event.GhostDeathHandler;
import com.xie.smfs.item.LuoQianDiaryItem;
import com.xie.smfs.network.packets.ui.s2c.LuoQianCombatStateS2CPacket;
import com.xie.smfs.registry.ModBlocks;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModEntities;
import com.xie.smfs.registry.ModItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap.Type;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.Animation.LoopType;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class LuoQianGhostEntity extends GhostEntity implements GeoAnimatable {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 30.0;
   private static final char TERROR_LEVEL = 'A';
   private static final double ATTACK_RANGE = 25.0;
   private static final double WALL_RADIUS = 30.0;
   private static final double RESTORE_RANGE = 35.0;
   private static final int WALL_HEIGHT = 25;
   private static final int WALL_SEGMENT_BLOCKS = 15;
   private static final int CLEAN_HEIGHT_ABOVE = 40;
   private static final int CLEAN_HEIGHT_BELOW = 40;
   private float wallAngleOffset = 0.0F;
   private static final int WARNING_SPAWN_INTERVAL = 60;
   private static final int GRAVE_MAX_COUNT = 15;
   private static final int DOMAIN_SKILL_COOLDOWN = 480;
   private static final int EXPLODE_SKILL_COOLDOWN = 180;
   private static final int DIRT_WALL_COOLDOWN = 120;
   private static final int DIRT_CHARGE_COOLDOWN = 300;
   private static final int DIRT_CHARGE_COUNT_MIN = 3;
   private static final int DIRT_CHARGE_COUNT_MAX = 6;
   private static final int DIRT_CHARGE_DELAY = 20;
   private static final int FLOATING_DIRT_INTERVAL = 60;
   private static final int MAX_FLOATING_DIRT = 50;
   private static final int MIN_DIRT_PER_GROUP = 4;
   private static final int MAX_DIRT_PER_GROUP = 10;
   private int warningSpawnTimer = 0;
   private int domainSkillCooldown = 0;
   private int explodeSkillCooldown = 0;
   private int dirtWallCooldown = 0;
   private int dirtChargeCooldown = 0;
   private int floatingDirtTimer = 0;
   private int activeGraveCount = 0;
   private int floatingDirtCount = 0;
   private boolean isChargingDirt = false;
   private int dirtChargeIndex = 0;
   private int dirtChargeDelayTimer = 0;
   private List<FloatingDirtEntity> dirtChargeTargets = new ArrayList<>();
   private double dirtChargeTargetX;
   private double dirtChargeTargetY;
   private double dirtChargeTargetZ;
   private boolean hasPlayerInRange = false;
   private boolean isInvincible = false;
   private int invincibleTimer = 0;
   private static final int INVINCIBLE_DURATION = 100;
   private int invincibleCooldownTimer = 0;
   private static final int INVINCIBLE_COOLDOWN = 1200;
   private boolean battleStarted = false;
   private Set<BlockPos> restoredGraves = new HashSet<>();
   private boolean isBuildingWall = false;
   private double wallStartAngle = 0.0;
   private int wallBlocksPerTick = 50;
   private int wallCurrentBlock = 0;
   private int wallTotalBlocks = 200;
   private boolean isCreatingPlatform = false;
   private boolean hasSavedBlocks = false;
   private NbtList savedBlocks = new NbtList();
   private NbtList generatedBlocks = new NbtList();
   private int platformCurrentRadius = 0;
   private int platformBlocksPerTick = 10;
   private boolean isRestoring = false;
   private int restoreCurrentRadius = 0;
   private int restoreStartRadius = 0;
   private int restoreTotalTicks = 60;
   private int restoreTickCount = 0;
   private Map<Long, NbtCompound> savedBlocksMap = new HashMap<>();
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private int combatHudUpdateTimer = 0;
   private boolean isDefeated = false;
   private boolean pendingRestore = false;

   public static Builder createLivingAttributes() {
      return GhostEntity.createGhostAttributes()
         .method_26868(EntityAttributes.field_23716, 80000.0)
         .method_26868(EntityAttributes.field_23719, 0.0)
         .method_26868(EntityAttributes.field_23721, 12.0)
         .method_26868(EntityAttributes.field_23717, 25.0);
   }

   public LuoQianGhostEntity(EntityType<LuoQianGhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 30.0, 'A', 18000, 1100, 180, 0.5F);
      this.ghostLevel = 5;
      this.setGhostDomainActualLevel(5);
      this.setSuppressionSlotCost(4);
      this.method_5971();
      this.setSpiritualStrength(this.getMaxSpiritualStrength());
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608()) {
         if (this.domainSkillCooldown > 0) {
            this.domainSkillCooldown--;
         }

         if (this.explodeSkillCooldown > 0) {
            this.explodeSkillCooldown--;
         }

         if (this.dirtWallCooldown > 0) {
            this.dirtWallCooldown--;
         }

         if (this.dirtChargeCooldown > 0) {
            this.dirtChargeCooldown--;
         }

         this.updateDirtChargeTick();
         this.updateInvincible();
         this.checkDefeated();
         this.updateCombatHud();
         this.checkPlayerInRange();
         this.updateGraveCount();
         this.updateFloatingDirtCount();
         if (this.hasPlayerInRange) {
            this.spawnFloatingDirt();
            if (!this.hasSavedBlocks) {
               this.saveSurroundingBlocks();
               this.hasSavedBlocks = true;
               this.isCreatingPlatform = true;
            }
         }

         this.spawnWarningSignals();
         this.tryActivateSkills();
         if (this.isBuildingWall) {
            this.buildDirtWallTick();
         }

         if (this.isCreatingPlatform) {
            this.createPlatformTick();
         }

         if (this.pendingRestore && !this.isCreatingPlatform && !this.isBuildingWall && !this.isRestoring) {
            this.pendingRestore = false;
            this.startRestoreAnimation();
         }

         if (this.isRestoring) {
            this.restoreTick();
         }
      }
   }

   private void checkPlayerInRange() {
      List<PlayerEntity> nearbyPlayers = this.method_37908()
         .method_8390(
            PlayerEntity.class,
            new Box(this.method_19538().method_1031(-25.0, -10.0, -25.0), this.method_19538().method_1031(25.0, 10.0, 25.0)),
            p -> !p.method_7325() && !p.method_7337()
         );
      boolean newHasPlayer = !nearbyPlayers.isEmpty();
      if (newHasPlayer && !this.hasPlayerInRange) {
         this.method_37908().method_8396(null, this.method_24515(), SoundEvents.field_14792, SoundCategory.field_15251, 3.0F, 0.5F);

         for (PlayerEntity player : nearbyPlayers) {
            player.method_7353(Text.method_43470("§c罗千：此地，已是坟场..."), false);
         }

         this.battleStarted = true;
         this.invincibleCooldownTimer = 0;
         this.sendCombatState(true);
      }

      if (!newHasPlayer && this.hasPlayerInRange) {
         this.sendCombatState(false);
      }

      if (!newHasPlayer && this.hasSavedBlocks && !this.isRestoring) {
         List<PlayerEntity> restoreCheckPlayers = this.method_37908()
            .method_8390(
               PlayerEntity.class,
               new Box(this.method_19538().method_1031(-35.0, -10.0, -35.0), this.method_19538().method_1031(35.0, 10.0, 35.0)),
               p -> !p.method_7325() && !p.method_7337()
            );
         if (restoreCheckPlayers.isEmpty()) {
            this.startRestoreAnimation();
         }
      }

      this.hasPlayerInRange = newHasPlayer;
   }

   private void sendCombatState(boolean isInCombat) {
      if (this.method_37908() instanceof ServerWorld) {
         ServerWorld serverWorld = (ServerWorld)this.method_37908();
         int currentStrength = this.getSpiritualStrength();
         int maxStrength = this.getMaxSpiritualStrength();
         LuoQianCombatStateS2CPacket.sendToAllPlayers(serverWorld, isInCombat, currentStrength, maxStrength, this.isInvincible, this.getRecoveryFactor());
      }
   }

   private void updateCombatHud() {
      if (!this.isDefeated) {
         if (this.hasPlayerInRange) {
            this.combatHudUpdateTimer++;
            if (this.combatHudUpdateTimer >= 20) {
               this.combatHudUpdateTimer = 0;
               this.sendCombatState(true);
            }
         }
      }
   }

   private void startInvincible() {
      this.isInvincible = true;
      this.invincibleTimer = 100;
   }

   private void updateInvincible() {
      if (this.isInvincible) {
         this.invincibleTimer--;
         if (this.invincibleTimer <= 0) {
            this.isInvincible = false;
         }
      } else if (this.battleStarted && this.hasPlayerInRange) {
         this.invincibleCooldownTimer++;
         if (this.invincibleCooldownTimer >= 1200) {
            this.invincibleCooldownTimer = 0;
            this.startInvincible();
         }
      }
   }

   public void onGraveRestore(BlockPos gravePos) {
      if (!this.restoredGraves.contains(gravePos)) {
         int restoreAmount = 1000;
         int currentStrength = this.getSpiritualStrength();
         int maxStrength = this.getMaxSpiritualStrength();
         int newStrength = Math.min(currentStrength + restoreAmount, maxStrength);
         this.setSpiritualStrength(newStrength);
         this.restoredGraves.add(gravePos);
         this.method_37908().method_8396(null, gravePos, SoundEvents.field_21951, SoundCategory.field_15251, 1.0F, 0.8F);
      }
   }

   @Override
   public void setSpiritualStrength(int strength) {
      if (this.isInvincible) {
         int currentStrength = this.getSpiritualStrength();
         if (strength < currentStrength) {
            return;
         }
      }

      super.setSpiritualStrength(strength);
   }

   private void checkDefeated() {
      if (!this.isDefeated) {
         float currentStrength = this.getSpiritualStrength();
         float maxStrength = this.getMaxSpiritualStrength();
         if (currentStrength < maxStrength * 0.2F) {
            this.isDefeated = true;

            for (PlayerEntity player : this.method_37908()
               .method_8390(
                  PlayerEntity.class,
                  new Box(this.method_19538().method_1031(-25.0, -10.0, -25.0), this.method_19538().method_1031(25.0, 10.0, 25.0)),
                  p -> !p.method_7325()
               )) {
               player.method_7353(Text.method_43470("§c罗千：真是位了不起的后生..."), false);
            }

            this.sendCombatState(false);
            this.startRestoreAnimation();
         }
      }
   }

   private void createLootChest() {
      BlockPos chestPos = this.method_24515().method_10084();

      while (chestPos.method_10264() < 256 && !this.method_37908().method_8320(chestPos).method_26215()) {
         chestPos = chestPos.method_10084();
      }

      this.method_37908().method_8501(chestPos, Blocks.field_10034.method_9564());
      if (this.method_37908().method_8321(chestPos) instanceof ChestBlockEntity chest) {
         chest.method_5447(0, new ItemStack(ModItems.EERIE_YELLOW_PAPER, 5 + (int)(Math.random() * 8.0)));
         chest.method_5447(1, new ItemStack(Items.field_8494, 4 + (int)(Math.random() * 5.0)));
         if (Math.random() < 0.5) {
            chest.method_5447(2, new ItemStack(ModItems.CORPSE_OIL, 16 + (int)(Math.random() * 9.0)));
         } else {
            chest.method_5447(2, new ItemStack(ModItems.CORPSE_PIECE, 16 + (int)(Math.random() * 9.0)));
         }

         int randomChoice = (int)(Math.random() * 3.0);
         if (randomChoice == 0) {
            chest.method_5447(3, new ItemStack(ModItems.BLACKENED_TOOTH, 4 + (int)(Math.random() * 3.0)));
         } else if (randomChoice == 1) {
            chest.method_5447(3, new ItemStack(ModItems.VISCOUS_BLOOD, 4 + (int)(Math.random() * 3.0)));
         } else {
            chest.method_5447(3, new ItemStack(ModItems.EERIE_RAG, 4 + (int)(Math.random() * 3.0)));
         }

         chest.method_5447(4, new ItemStack(ModItems.COFFIN_NAIL));
         chest.method_5447(5, LuoQianDiaryItem.createDiary());
      }
   }

   private void spawnFloatingDirt() {
      if (++this.floatingDirtTimer >= 60) {
         this.floatingDirtTimer = 0;
         int countToSpawn = 8 + this.method_37908().field_9229.method_43048(13);
         if (this.floatingDirtCount + countToSpawn > 50) {
            countToSpawn = 50 - this.floatingDirtCount;
            if (countToSpawn < 2) {
               return;
            }
         }

         double targetX = this.method_23317();
         double targetZ = this.method_23321();

         for (int i = 0; i < countToSpawn; i++) {
            double angle = this.method_37908().field_9229.method_43058() * Math.PI * 2.0;
            double radius = 5.0 + this.method_37908().field_9229.method_43058() * 20.0;
            double spawnX = targetX + Math.cos(angle) * radius + (this.method_37908().field_9229.method_43058() - 0.5) * 4.0;
            double spawnZ = targetZ + Math.sin(angle) * radius + (this.method_37908().field_9229.method_43058() - 0.5) * 4.0;
            int spawnY = this.method_37908().method_8624(Type.field_13202, (int)spawnX, (int)spawnZ);
            if (this.method_37908().method_8320(new BlockPos((int)spawnX, spawnY, (int)spawnZ)).method_26215()) {
               FloatingDirtEntity dirt = (FloatingDirtEntity)ModEntities.FLOATING_DIRT.method_5883(this.method_37908());
               if (dirt != null) {
                  dirt.init(targetX, spawnY, targetZ, spawnX + 0.5, spawnZ + 0.5);
                  this.method_37908().method_8649(dirt);
                  this.floatingDirtCount++;
               }
            }
         }
      }
   }

   private void updateFloatingDirtCount() {
      int count = 0;
      Box searchBox = new Box(this.method_19538().method_1031(-25.0, -20.0, -25.0), this.method_19538().method_1031(25.0, 20.0, 25.0));

      for (FloatingDirtEntity dirt : this.method_37908().method_8390(FloatingDirtEntity.class, searchBox, entity -> true)) {
         if (dirt.method_5858(this) <= 625.0) {
            count++;
         }
      }

      this.floatingDirtCount = count;
   }

   private void spawnWarningSignals() {
      if (++this.warningSpawnTimer >= 60) {
         this.warningSpawnTimer = 0;
         if (this.activeGraveCount < 15) {
            List<PlayerEntity> nearbyPlayers = this.method_37908()
               .method_8390(
                  PlayerEntity.class,
                  new Box(this.method_19538().method_1031(-25.0, -10.0, -25.0), this.method_19538().method_1031(25.0, 10.0, 25.0)),
                  p -> !p.method_7325() && !p.method_7337()
               );
            if (!nearbyPlayers.isEmpty()) {
               PlayerEntity targetPlayer = nearbyPlayers.get(this.method_37908().field_9229.method_43048(nearbyPlayers.size()));
               int x = targetPlayer.method_24515().method_10263();
               int z = targetPlayer.method_24515().method_10260();
               int y = this.method_37908().method_8624(Type.field_13202, x, z);
               BlockPos warningPos = new BlockPos(x, y, z);
               if (this.method_37908().method_8320(warningPos).method_26215()) {
                  GraveWarningEntity warning = (GraveWarningEntity)ModEntities.GRAVE_WARNING.method_5883(this.method_37908());
                  if (warning != null) {
                     warning.method_5808(warningPos.method_10263() + 0.5, warningPos.method_10264(), warningPos.method_10260() + 0.5, 0.0F, 0.0F);
                     this.method_37908().method_8649(warning);
                  }
               }
            }
         }
      }
   }

   private void updateGraveCount() {
      int count = 0;

      for (int x = -30; x <= 30; x++) {
         for (int y = -5; y <= 5; y++) {
            for (int z = -30; z <= 30; z++) {
               if (this.method_37908().method_8320(this.method_24515().method_10069(x, y, z)).method_27852(ModBlocks.GRAVE_MOUND)) {
                  count++;
               }
            }
         }
      }

      this.activeGraveCount = count;
   }

   private void tryActivateSkills() {
      if (this.domainSkillCooldown <= 0 && this.hasPlayerInRange) {
         this.activateGraveDomain();
         this.domainSkillCooldown = 480;
      }

      if (this.explodeSkillCooldown <= 0 && this.activeGraveCount >= 3) {
         this.explodeGraves();
         this.explodeSkillCooldown = 180;
      }

      if (this.dirtChargeCooldown <= 0 && this.hasPlayerInRange && this.floatingDirtCount >= 3 && !this.isChargingDirt) {
         this.activateDirtCharge();
         this.dirtChargeCooldown = 300;
      }
   }

   private void activateGraveDomain() {
      for (PlayerEntity player : this.method_37908()
         .method_8390(
            PlayerEntity.class,
            new Box(this.method_19538().method_1031(-25.0, -10.0, -25.0), this.method_19538().method_1031(25.0, 10.0, 25.0)),
            p -> !p.method_7325()
         )) {
         player.method_7353(Text.method_43470("§c罗千：入土为安吧..."), false);
      }

      for (int i = 0; i < 15; i++) {
         BlockPos pos = this.getRandomPositionInDomain();
         if (this.method_37908().method_8320(pos).method_26215()
            && this.method_37908().method_8320(pos.method_10074()).method_26212(this.method_37908(), pos.method_10074())) {
            this.method_37908().method_8501(pos, ModBlocks.GRAVE_MOUND.method_9564());
         }
      }

      this.method_37908().method_8396(null, this.method_24515(), SoundEvents.field_14792, SoundCategory.field_15251, 2.0F, 0.5F);
   }

   private BlockPos getRandomPositionInDomain() {
      double x = this.method_23317() + (this.method_37908().field_9229.method_43058() - 0.5) * 25.0 * 2.0;
      double z = this.method_23321() + (this.method_37908().field_9229.method_43058() - 0.5) * 25.0 * 2.0;
      return BlockPos.method_49637(x, this.method_37908().method_8624(Type.field_13202, (int)x, (int)z), z);
   }

   private void activateDirtCharge() {
      List<PlayerEntity> nearbyPlayers = this.method_37908()
         .method_8390(
            PlayerEntity.class,
            new Box(this.method_19538().method_1031(-25.0, -10.0, -25.0), this.method_19538().method_1031(25.0, 10.0, 25.0)),
            p -> !p.method_7325() && !p.method_7337()
         );
      if (!nearbyPlayers.isEmpty()) {
         for (PlayerEntity player : nearbyPlayers) {
            player.method_7353(Text.method_43470("§c罗千：坟土将埋葬一切..."), false);
         }

         PlayerEntity targetPlayer = nearbyPlayers.get(this.method_37908().field_9229.method_43048(nearbyPlayers.size()));
         this.dirtChargeTargetX = targetPlayer.method_23317();
         this.dirtChargeTargetY = targetPlayer.method_23318();
         this.dirtChargeTargetZ = targetPlayer.method_23321();
         Box searchBox = new Box(this.method_19538().method_1031(-25.0, -20.0, -25.0), this.method_19538().method_1031(25.0, 20.0, 25.0));
         List<FloatingDirtEntity> allDirt = this.method_37908().method_8390(FloatingDirtEntity.class, searchBox, entity -> true);
         Collections.shuffle(allDirt);
         int count = Math.min(3 + this.method_37908().field_9229.method_43048(4), allDirt.size());
         this.dirtChargeTargets.clear();

         for (int i = 0; i < count; i++) {
            this.dirtChargeTargets.add(allDirt.get(i));
         }

         this.isChargingDirt = true;
         this.dirtChargeIndex = 0;
         this.dirtChargeDelayTimer = 0;
         this.method_37908().method_8396(null, this.method_24515(), SoundEvents.field_15211, SoundCategory.field_15251, 2.0F, 0.8F);
      }
   }

   private void updateDirtChargeTick() {
      if (this.isChargingDirt) {
         if (this.dirtChargeDelayTimer > 0) {
            this.dirtChargeDelayTimer--;
         } else {
            if (this.dirtChargeIndex < this.dirtChargeTargets.size()) {
               FloatingDirtEntity dirt = this.dirtChargeTargets.get(this.dirtChargeIndex);
               if (dirt.method_5805()) {
                  List<PlayerEntity> nearbyPlayers = this.method_37908()
                     .method_8390(
                        PlayerEntity.class,
                        new Box(this.method_19538().method_1031(-25.0, -10.0, -25.0), this.method_19538().method_1031(25.0, 10.0, 25.0)),
                        p -> !p.method_7325() && !p.method_7337()
                     );
                  if (!nearbyPlayers.isEmpty()) {
                     PlayerEntity targetPlayer = nearbyPlayers.get(this.method_37908().field_9229.method_43048(nearbyPlayers.size()));
                     dirt.setTrackingTarget(targetPlayer.method_23317(), targetPlayer.method_23318(), targetPlayer.method_23321());
                     dirt.setExplosionDamage(this.getSpiritualDamage() * 0.8F);
                  }
               }

               this.dirtChargeIndex++;
               this.dirtChargeDelayTimer = 20;
            } else {
               this.isChargingDirt = false;
               this.dirtChargeTargets.clear();
            }
         }
      }
   }

   private void explodeGraves() {
      List<PlayerEntity> players = this.method_37908()
         .method_8390(
            PlayerEntity.class,
            new Box(this.method_19538().method_1031(-30.0, -5.0, -30.0), this.method_19538().method_1031(30.0, 5.0, 30.0)),
            p -> !p.method_7325() && !p.method_7337() && !this.isPlayerProtected(p)
         );

      for (int x = -30; x <= 30; x++) {
         for (int z = -30; z <= 30; z++) {
            BlockPos checkPos = this.method_24515().method_10069(x, 0, z);
            if (this.method_37908().method_8320(checkPos).method_27852(ModBlocks.GRAVE_MOUND)) {
               this.method_37908().method_8396(null, checkPos, SoundEvents.field_15152, SoundCategory.field_15251, 1.5F, 0.8F);
               if (this.method_37908() instanceof ServerWorld) {
                  ServerWorld serverWorld = (ServerWorld)this.method_37908();
                  serverWorld.method_14199(
                     ParticleTypes.field_11236,
                     checkPos.method_10263() + 0.5,
                     checkPos.method_10264() + 1.0,
                     checkPos.method_10260() + 0.5,
                     10,
                     0.5,
                     0.5,
                     0.5,
                     0.2
                  );
               }

               Box damageBox = new Box(checkPos.method_10069(-3, -3, -3), checkPos.method_10069(3, 3, 3));

               for (PlayerEntity player : players) {
                  if (damageBox.method_1006(player.method_19538())) {
                     float damage = this.getSpiritualDamage() * 1.2F;
                     PlayerEvents.handleSpiritDamage(player, damage, damage, ModDamageSources.ghost(this.method_37908()));
                     Vec3d knockback = player.method_19538().method_1020(checkPos.method_46558()).method_1029().method_18805(2.0, 1.0, 2.0);
                     player.method_5762(knockback.field_1352, knockback.field_1351, knockback.field_1350);
                     player.field_6037 = true;
                  }
               }

               this.method_37908().method_8501(checkPos, Blocks.field_10124.method_9564());
            }
         }
      }
   }

   private void startBuildingWall() {
      PlayerEntity targetPlayer = null;
      double closestDistance = Double.MAX_VALUE;

      for (PlayerEntity player : this.method_37908()
         .method_8390(
            PlayerEntity.class,
            new Box(this.method_19538().method_1031(-30.0, -10.0, -30.0), this.method_19538().method_1031(30.0, 10.0, 30.0)),
            p -> !p.method_7325() && !p.method_7337()
         )) {
         double distance = player.method_5858(this);
         if (distance > 625.0 && distance <= 900.0 && distance < closestDistance) {
            closestDistance = distance;
            targetPlayer = player;
         }
      }

      if (targetPlayer != null) {
         double dx = targetPlayer.method_23317() - this.method_23317();
         double dz = targetPlayer.method_23321() - this.method_23321();
         double startAngle = Math.atan2(dz, dx);
         double offsetAngle = (Math.PI / 4) * (this.method_37908().field_9229.method_43056() ? 1 : -1);
         this.wallStartAngle = startAngle + offsetAngle;
         this.wallCurrentBlock = 0;
         this.isBuildingWall = true;
         this.method_37908().method_8396(null, this.method_24515(), SoundEvents.field_14609, SoundCategory.field_15251, 2.0F, 0.8F);
      }
   }

   private void buildDirtWallTick() {
      double angleStep = (Math.PI * 2) / this.wallTotalBlocks;
      int platformTopY = (int)this.method_23318() - 2 + 1;
      int currentLayer = this.wallCurrentBlock / this.wallTotalBlocks;
      int currentBlockInLayer = this.wallCurrentBlock % this.wallTotalBlocks;
      if (currentLayer >= 25) {
         this.isBuildingWall = false;
         this.wallCurrentBlock = 0;
      } else {
         for (int i = 0; i < this.wallBlocksPerTick; i++) {
            if (currentBlockInLayer >= this.wallTotalBlocks) {
               currentLayer++;
               currentBlockInLayer = 0;
               if (currentLayer >= 25) {
                  this.isBuildingWall = false;
                  this.wallCurrentBlock = 0;
                  return;
               }
            }

            double angle = this.wallStartAngle + angleStep * currentBlockInLayer;
            double x = this.method_23317() + Math.cos(angle) * 30.0;
            double z = this.method_23321() + Math.sin(angle) * 30.0;
            BlockPos wallPos = new BlockPos((int)Math.floor(x), platformTopY + currentLayer, (int)Math.floor(z));
            if (this.method_37908().method_8320(wallPos).method_26215()) {
               this.method_37908().method_8501(wallPos, Blocks.field_10566.method_9564());
            }

            currentBlockInLayer++;
            this.wallCurrentBlock++;
         }
      }
   }

   private void saveSurroundingBlocks() {
      int radius = 33;
      int platformTopY = (int)this.method_23318() - 2 + 1;
      this.savedBlocksMap.clear();
      int maxSaveHeight = platformTopY + 40;
      int minSaveHeight = platformTopY - 40;

      for (int x = -radius; x <= radius; x++) {
         for (int z = -radius; z <= radius; z++) {
            for (int y = minSaveHeight; y <= maxSaveHeight; y++) {
               BlockPos pos = new BlockPos((int)this.method_23317() + x, y, (int)this.method_23321() + z);
               BlockState state = this.method_37908().method_8320(pos);
               if (!state.method_26215()) {
                  NbtCompound blockData = new NbtCompound();
                  blockData.method_10569("x", pos.method_10263());
                  blockData.method_10569("y", pos.method_10264());
                  blockData.method_10569("z", pos.method_10260());
                  blockData.method_10582("blockId", Registries.field_41175.method_10221(state.method_26204()).toString());
                  this.savedBlocks.add(blockData);
                  this.savedBlocksMap.put(pos.method_10063(), blockData);
               }
            }
         }
      }
   }

   private void createPlatformTick() {
      int targetRadius = 31;
      if (this.platformCurrentRadius > targetRadius) {
         this.isCreatingPlatform = false;
         this.platformCurrentRadius = 0;
         if (!this.isBuildingWall) {
            this.isBuildingWall = true;
            this.wallStartAngle = 0.0;
            this.wallCurrentBlock = 0;
         }
      } else {
         int currentR = this.platformCurrentRadius;

         for (int angle = 0; angle < 360; angle += 3) {
            double rad = Math.toRadians(angle);
            int x = (int)Math.round(Math.cos(rad) * currentR);
            int z = (int)Math.round(Math.sin(rad) * currentR);
            int groundY = this.method_37908().method_8624(Type.field_13202, (int)this.method_23317() + x, (int)this.method_23321() + z);
            int targetY = (int)this.method_23318() - 2;
            int platformTopY = targetY + 1;
            int maxCleanY = platformTopY + 40;
            int minCleanY = platformTopY - 40;

            for (int y = maxCleanY; y >= minCleanY; y--) {
               BlockPos pos = new BlockPos((int)this.method_23317() + x, y, (int)this.method_23321() + z);
               if (!this.method_37908().method_8320(pos).method_26215()) {
                  this.method_37908().method_8501(pos, Blocks.field_10124.method_9564());
               }
            }

            for (int y = groundY; y <= targetY; y++) {
               BlockPos pos = new BlockPos((int)this.method_23317() + x, y, (int)this.method_23321() + z);
               if (this.method_37908().method_8320(pos).method_26215()) {
                  this.method_37908().method_8501(pos, Blocks.field_10566.method_9564());
               }
            }

            BlockPos topPos = new BlockPos((int)this.method_23317() + x, targetY + 1, (int)this.method_23321() + z);
            if (this.method_37908().method_8320(topPos).method_26215()) {
               this.method_37908().method_8501(topPos, Blocks.field_10520.method_9564());
            }

            for (int dx = -1; dx <= 1; dx++) {
               for (int dz = -1; dz <= 1; dz++) {
                  if (dx != 0 || dz != 0) {
                     int nx = x + dx;
                     int nz = z + dz;

                     for (int y = maxCleanY; y >= minCleanY; y--) {
                        BlockPos pos = new BlockPos((int)this.method_23317() + nx, y, (int)this.method_23321() + nz);
                        if (!this.method_37908().method_8320(pos).method_26215()) {
                           this.method_37908().method_8501(pos, Blocks.field_10124.method_9564());
                        }
                     }

                     for (int y = minCleanY; y <= targetY; y++) {
                        BlockPos pos = new BlockPos((int)this.method_23317() + nx, y, (int)this.method_23321() + nz);
                        if (this.method_37908().method_8320(pos).method_26215()) {
                           this.method_37908().method_8501(pos, Blocks.field_10566.method_9564());
                        }
                     }

                     BlockPos neighborTopPos = new BlockPos((int)this.method_23317() + nx, targetY + 1, (int)this.method_23321() + nz);
                     if (this.method_37908().method_8320(neighborTopPos).method_26215()) {
                        this.method_37908().method_8501(neighborTopPos, Blocks.field_10520.method_9564());
                     }
                  }
               }
            }
         }

         this.platformCurrentRadius++;
      }
   }

   private void startRestoreAnimation() {
      if (!this.isCreatingPlatform && !this.isBuildingWall) {
         this.restoreStartRadius = 33;
         this.restoreCurrentRadius = this.restoreStartRadius;
         this.restoreTickCount = 0;
         this.isRestoring = true;
         this.triggerFloatingDirtReturn();
         this.method_37908().method_8396(null, this.method_24515(), SoundEvents.field_15211, SoundCategory.field_15251, 2.0F, 0.8F);
      } else {
         this.pendingRestore = true;
      }
   }

   private void triggerFloatingDirtReturn() {
      Box searchBox = new Box(this.method_19538().method_1031(-35.0, -30.0, -35.0), this.method_19538().method_1031(35.0, 30.0, 35.0));

      for (FloatingDirtEntity dirt : this.method_37908().method_8390(FloatingDirtEntity.class, searchBox, entity -> true)) {
         dirt.startReturning();
      }
   }

   private void restoreTick() {
      this.restoreTickCount++;
      double progress = (double)this.restoreTickCount / this.restoreTotalTicks;
      this.restoreCurrentRadius = (int)(this.restoreStartRadius * (1.0 - progress));
      if (this.restoreCurrentRadius < 0) {
         this.restoreCurrentRadius = 0;
      }

      if (this.hasSavedBlocks) {
         this.processRestoreRing(this.restoreCurrentRadius);
      }

      if (this.restoreTickCount >= this.restoreTotalTicks) {
         this.finishRestore();
      }
   }

   private void processRestoreRing(int radius) {
      int platformTopY = (int)this.method_23318() - 2 + 1;
      int maxRestoreHeight = platformTopY + 40;
      int minRestoreHeight = platformTopY - 40;

      for (int angle = 0; angle < 360; angle += 2) {
         double rad = Math.toRadians(angle);
         int x = (int)Math.round(Math.cos(rad) * radius);
         int z = (int)Math.round(Math.sin(rad) * radius);
         if (Math.abs(x) > 1 || Math.abs(z) > 1) {
            for (int y = minRestoreHeight; y <= maxRestoreHeight; y++) {
               BlockPos pos = new BlockPos((int)this.method_23317() + x, y, (int)this.method_23321() + z);
               NbtCompound blockData = this.savedBlocksMap.get(pos.method_10063());
               if (blockData != null) {
                  String blockId = blockData.method_10558("blockId");
                  Block block = (Block)Registries.field_41175.method_10223(new Identifier(blockId));
                  this.method_37908().method_8501(pos, block.method_9564());
               } else {
                  this.method_37908().method_8501(pos, Blocks.field_10124.method_9564());
               }
            }
         }
      }
   }

   private void finishRestore() {
      if (this.hasSavedBlocks) {
         int radius = 33;
         int platformTopY = (int)this.method_23318() - 2 + 1;
         int maxRestoreHeight = platformTopY + 40;
         int minRestoreHeight = platformTopY - 40;

         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               if (Math.abs(x) > 1 || Math.abs(z) > 1) {
                  for (int y = minRestoreHeight; y <= maxRestoreHeight; y++) {
                     BlockPos pos = new BlockPos((int)this.method_23317() + x, y, (int)this.method_23321() + z);
                     NbtCompound blockData = this.savedBlocksMap.get(pos.method_10063());
                     if (blockData != null) {
                        String blockId = blockData.method_10558("blockId");
                        Block block = (Block)Registries.field_41175.method_10223(new Identifier(blockId));
                        this.method_37908().method_8501(pos, block.method_9564());
                     } else {
                        this.method_37908().method_8501(pos, Blocks.field_10124.method_9564());
                     }
                  }
               }
            }
         }
      }

      this.savedBlocks.clear();
      this.savedBlocksMap.clear();
      this.hasSavedBlocks = false;
      this.isRestoring = false;
      this.restoreCurrentRadius = 0;
      this.restoreTickCount = 0;
      this.method_37908().method_8396(null, this.method_24515(), SoundEvents.field_14653, SoundCategory.field_15251, 1.5F, 1.0F);
      if (this.isDefeated) {
         this.createLootChest();
         this.method_37908().method_8396(null, this.method_24515(), SoundEvents.field_15136, SoundCategory.field_15251, 3.0F, 0.5F);
         if (this.method_37908() instanceof ServerWorld serverWorld) {
            for (int i = 0; i < 50; i++) {
               double x = this.method_23317() + (Math.random() - 0.5) * 2.0;
               double y = this.method_23318() + Math.random() * 2.0;
               double z = this.method_23321() + (Math.random() - 0.5) * 2.0;
               serverWorld.method_14199(ParticleTypes.field_11236, x, y, z, 1, 0.0, 0.0, 0.0, 0.1);
            }
         }

         GhostDeathHandler.markLegitimateRemoval(this);
         this.method_31472();
      }
   }

   private void restoreSavedBlocks() {
      this.startRestoreAnimation();
   }

   private void clearSavedArea() {
      int radius = 33;
      int platformTopY = (int)this.method_23318() - 2 + 1;

      for (int x = -radius; x <= radius; x++) {
         for (int z = -radius; z <= radius; z++) {
            if (Math.abs(x) > 1 || Math.abs(z) > 1) {
               for (int y = platformTopY - 5; y <= platformTopY + 25; y++) {
                  BlockPos pos = new BlockPos((int)this.method_23317() + x, y, (int)this.method_23321() + z);
                  this.method_37908().method_8501(pos, Blocks.field_10124.method_9564());
               }
            }
         }
      }
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   @Override
   public double getTick(Object o) {
      return this.field_6012;
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "controller", 2, this::predicate));
   }

   private <E extends GeoAnimatable> PlayState predicate(AnimationState<E> event) {
      if (this.field_6252) {
         event.getController().setAnimation(RawAnimation.begin().then("attack", LoopType.PLAY_ONCE));
      } else if (event.getLimbSwingAmount() > 0.01F) {
         event.getController().setAnimation(RawAnimation.begin().then("walk", LoopType.LOOP));
      } else {
         event.getController().setAnimation(RawAnimation.begin().then("idle", LoopType.LOOP));
      }

      return PlayState.CONTINUE;
   }

   public void method_36209() {
      super.method_36209();
      if (this.hasSavedBlocks && !this.isRestoring) {
         this.forceRestore();
      }
   }

   public void forceRestore() {
      this.triggerFloatingDirtReturn();
      if (this.hasSavedBlocks) {
         int radius = 33;
         int platformTopY = (int)this.method_23318() - 2 + 1;
         int maxRestoreHeight = platformTopY + 40;
         int minRestoreHeight = platformTopY - 40;

         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               if (Math.abs(x) > 1 || Math.abs(z) > 1) {
                  for (int y = minRestoreHeight; y <= maxRestoreHeight; y++) {
                     BlockPos pos = new BlockPos((int)this.method_23317() + x, y, (int)this.method_23321() + z);
                     NbtCompound blockData = this.savedBlocksMap.get(pos.method_10063());
                     if (blockData != null) {
                        String blockId = blockData.method_10558("blockId");
                        Block block = (Block)Registries.field_41175.method_10223(new Identifier(blockId));
                        this.method_37908().method_8501(pos, block.method_9564());
                     } else {
                        this.method_37908().method_8501(pos, Blocks.field_10124.method_9564());
                     }
                  }
               }
            }
         }

         this.savedBlocks.clear();
         this.savedBlocksMap.clear();
         this.hasSavedBlocks = false;
      }
   }

   public boolean method_6049(StatusEffectInstance effectInstance) {
      return effectInstance.method_5579() == ModEffects.SILENCE ? false : super.method_6049(effectInstance);
   }
}
