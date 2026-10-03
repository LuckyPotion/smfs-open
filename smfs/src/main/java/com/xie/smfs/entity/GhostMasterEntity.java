package com.xie.smfs.entity;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.effect.LostStatusEffectInstance;
import com.xie.smfs.effect.SpiritSurgeStatusEffect;
import com.xie.smfs.entity.ghost.GhostDreamEntity;
import com.xie.smfs.entity.ghost.GhostPressureEntity;
import com.xie.smfs.entity.other.GoldenBulletEntity;
import com.xie.smfs.event.ModEvents;
import com.xie.smfs.faction.FactionManager;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.item.SpiritWeapon;
import com.xie.smfs.manager.DifficultyManager;
import com.xie.smfs.manager.GhostSkillManager;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModEntities;
import com.xie.smfs.registry.ModFluids;
import com.xie.smfs.registry.ModItems;
import com.xie.smfs.util.WeaponOilHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;

public abstract class GhostMasterEntity extends PathAwareEntity implements Merchant {
   protected int ghostDomainLevel = 1;
   protected double ghostDomainRadius = 16.0;
   protected int spiritualStrength = 100;
   protected int spiritualDamage = 10;
   protected int spiritualResistance = 20;
   protected float recoveryFactor = 0.1F;
   protected PlayerFaction faction = PlayerFaction.ORDINARY;
   protected boolean isSkillActive = false;
   protected int skillHitCount = 0;
   protected int skillCooldown = 0;
   protected int skillDelayCounter = 0;
   protected static final int MAX_SKILL_HITS = 10;
   protected static final int SKILL_COOLDOWN_TICKS = 100;
   protected static final int SKILL_ATTACK_DELAY = 20;
   protected boolean isSuppressed = false;
   protected boolean isDeadlocked = false;
   protected ItemStack coffinNail = ItemStack.EMPTY;
   protected Set<UUID> attackedPlayers = new HashSet<>();
   protected boolean hasSentWarning = false;
   private final Map<String, String> questList = new HashMap<>();
   protected boolean shouldFleeFromGhosts = true;
   protected boolean shouldAttackPlayers = true;
   protected boolean shouldProtectPlayers = false;
   protected boolean shouldAttackGhostsNearPlayers = false;
   protected int protectPlayerCooldown = 0;
   protected static final int PROTECT_PLAYER_COOLDOWN_TICKS = 600;
   protected PlayerEntity customer;
   protected TradeOfferList tradeOffers = new TradeOfferList();
   protected boolean tradeOffersInitialized = false;
   protected int tradeExperience = 0;
   private GhostMasterEntity.BehaviorState currentState = GhostMasterEntity.BehaviorState.IDLE;
   private Entity currentTarget = null;
   protected boolean hasSentEscapeMessage = false;
   protected boolean hasSentAttackMessage = false;
   protected static final int MAX_ESCAPE_DISTANCE = 30;
   protected static final String[] DEFAULT_GREETING_DIALOGUES = new String[]{
      "成为驭鬼者以后，活人的情感越来越少了。", "近来，灵异事件越发频繁...", "需要什么帮助吗?", "我这里有些不错的东西...", "听说在民国时期的一些房子中能翻到好东西..."
   };

   public GhostMasterEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.setCustomName(Text.literal("§6" + this.getGhostMasterDisplayName()));
      this.setCustomNameVisible(true);
      this.setSilent(true);
      this.spiritualStrength = DifficultyManager.adjustSpiritualStrength(this.spiritualStrength, this.getWorld());
      this.spiritualDamage = DifficultyManager.adjustSpiritualDamage(this.spiritualDamage, this.getWorld());
      this.spiritualResistance = DifficultyManager.adjustSpiritualResistance(this.spiritualResistance, this.getWorld());
   }

   protected abstract String getGhostMasterDisplayName();

   public static Builder createGhostMasterAttributes() {
      return MobEntity.createMobAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 1000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
   }

   protected void initGoals() {
      super.initGoals();
      this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
      this.goalSelector.add(1, new MeleeAttackGoal(this, 1.2, true));
      this.goalSelector.add(2, new WanderAroundGoal(this, 0.8));
      this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(4, new LookAroundGoal(this));
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isGoldenSwordDamage(source)) {
         if (source.getAttacker() instanceof PlayerEntity player) {
            this.attackedPlayers.add(player.getUuid());
         }

         return super.damage(source, amount);
      } else if (this.isGoldenBulletDamage(source)) {
         if (source.getSource() instanceof PlayerEntity player) {
            this.attackedPlayers.add(player.getUuid());
         }

         return super.damage(source, amount);
      } else if (this.isSpiritDamage(source)) {
         if (source.getAttacker() instanceof PlayerEntity player) {
            this.attackedPlayers.add(player.getUuid());
         }

         this.triggerSpiritCounterAttack(source);
         float actualDamage = PlayerEvents.calculateSpiritDamage(amount, this.getSpiritResistance());
         return super.damage(source, actualDamage);
      } else {
         return false;
      }
   }

   public boolean isInvulnerableTo(DamageSource damageSource) {
      return !this.isGoldenSwordDamage(damageSource) && !this.isGoldenBulletDamage(damageSource) && !this.isSpiritDamage(damageSource);
   }

   private boolean isGoldenSwordDamage(DamageSource source) {
      if (!(source.getAttacker() instanceof PlayerEntity player)) {
         return false;
      } else {
         ItemStack heldItem = player.getMainHandStack();
         return heldItem.getItem() == Items.GOLDEN_SWORD || heldItem.getItem() == Items.GOLDEN_AXE;
      }
   }

   private boolean isGoldenBulletDamage(DamageSource source) {
      return source.getSource() instanceof GoldenBulletEntity;
   }

   private boolean isSpiritDamage(DamageSource source) {
      if (ModEvents.processingSpiritDamage.get()) {
         return true;
      }

      if (!(source.getAttacker() instanceof GhostEntity) && !(source.getAttacker() instanceof GhostDreamEntity)) {
         if (source.getAttacker() instanceof PlayerEntity player) {
            boolean hasSpiritWeapon = SpiritWeapon.isSpiritWeapon(player.getMainHandStack());
            boolean hasCorpseOilWeapon = false;
            if (player.getMainHandStack().getItem() instanceof SwordItem && WeaponOilHandler.getCorpseOilLayers(player.getMainHandStack()) > 0) {
               hasCorpseOilWeapon = true;
            }

            boolean isSkillDamage = GhostSkillManager.isSkillDamage(source);
            boolean hasSpiritSurge = SpiritSurgeStatusEffect.hasSpiritSurgeEffect(player);
            return hasSpiritWeapon || hasCorpseOilWeapon || isSkillDamage || hasSpiritSurge;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public float getSpiritResistance() {
      return this.spiritualResistance;
   }

   public void setSpiritResistance(float resistance) {
      this.spiritualResistance = (int)resistance;
   }

   public boolean isPushable() {
      return false;
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      return super.interactMob(player, hand);
   }

   protected Identifier getLootTableId() {
      return new Identifier("smfs", "entities/ghost_master");
   }

   public void pushAwayFrom(Entity entity) {
      if (!(entity instanceof BoatEntity) && !(entity instanceof AbstractMinecartEntity)) {
         super.pushAwayFrom(entity);
      }
   }

   public void tick() {
      super.tick();
      if (!this.isSuppressed && !this.isDeadlocked) {
         if (!this.getWorld().isClient()) {
            if (this.getTarget() instanceof GhostEntity ghostTarget
               && (
                  ghostTarget.getSpiritualStrength() <= 0
                     || ghostTarget.getMaxSpiritualStrength() > 0 && (float)ghostTarget.getSpiritualStrength() / ghostTarget.getMaxSpiritualStrength() <= 0.2F
               )) {
               this.setTarget(null);
               this.hasSentWarning = false;
            }

            if (this.getHealth() <= this.getMaxHealth() * 0.5 && this.ghostDomainLevel >= 1) {
               this.applyGhostDomainToNearbyPlayers(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
               if (!this.isSkillActive
                  && this.skillCooldown <= 0
                  && this.currentState != GhostMasterEntity.BehaviorState.ESCAPING
                  && this.random.nextFloat() < 0.01F) {
                  this.startSkill();
               }
            }

            if (this.isSkillActive) {
               this.handleSkill();
            }

            if (this.skillCooldown > 0) {
               this.skillCooldown--;
            }

            if (this.protectPlayerCooldown > 0) {
               this.protectPlayerCooldown--;
            }

            if (!this.getWorld().isClient() && this.age % 20 == 0 && this.getTarget() == null) {
               if (!this.shouldFleeFromGhosts && !this.shouldAttackPlayers) {
                  this.attackHostileMobsNearPlayers();
               } else {
                  boolean isHandlingGhost = this.handleGhostDetectionAndEscape();
                  if (!isHandlingGhost) {
                     this.attackHostileMobsNearPlayers();
                  }
               }
            }
         }
      } else {
         if (this.isSkillActive) {
            this.endSkill();
         }

         this.disableGhostDomain();
         this.setTarget(null);
         this.setAttacker(null);
         this.setVelocity(0.0, this.getVelocity().y, 0.0);
         this.navigation.stop();
         this.setAiDisabled(true);
      }
   }

   protected void enableGhostDomain(PlayerEntity player, StatusEffect effect) {
      player.addStatusEffect(new StatusEffectInstance(effect, 20, this.ghostDomainLevel - 1, false, false, false));
   }

   protected void applyGhostDomainToNearbyPlayers(StatusEffect effect) {
      double radius = this.ghostDomainRadius;
      this.getWorld()
         .getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(radius), player -> player instanceof PlayerEntity)
         .forEach(player -> {
            double distance = this.distanceTo(player);
            if (distance <= radius) {
               boolean hasHigherLevelGhostDomain = this.checkPlayerHasHigherLevelGhostDomain(player);
               if (!hasHigherLevelGhostDomain) {
                  this.enableGhostDomain(player, effect);
                  player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, this.ghostDomainLevel - 1, false, false, false));
                  if (!player.hasStatusEffect(ModEffects.LOST)) {
                     player.addStatusEffect(new LostStatusEffectInstance(ModEffects.LOST, Integer.MAX_VALUE, 0, false, false, false, this.getUuid()));
                  }
               } else {
                  player.removeStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
                  player.removeStatusEffect(ModEffects.RED_GHOST_DOMAIN_TARGET);
                  player.removeStatusEffect(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
                  player.removeStatusEffect(ModEffects.CYAN_GHOST_DOMAIN_TARGET);
                  player.removeStatusEffect(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
                  player.removeStatusEffect(ModEffects.THICK_FOG_TARGET);
                  player.removeStatusEffect(ModEffects.LOST);
                  player.removeStatusEffect(StatusEffects.SLOWNESS);
               }
            }
         });
   }

   private boolean checkPlayerHasHigherLevelGhostDomain(PlayerEntity player) {
      if (player.hasStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN)) {
         StatusEffectInstance goldenEffect = player.getStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN);
         if (goldenEffect != null && goldenEffect.getAmplifier() + 1 > this.ghostDomainLevel) {
            return true;
         }
      }

      if (player.hasStatusEffect(ModEffects.RED_GHOST_DOMAIN)) {
         StatusEffectInstance redEffect = player.getStatusEffect(ModEffects.RED_GHOST_DOMAIN);
         if (redEffect != null && redEffect.getAmplifier() + 1 > this.ghostDomainLevel) {
            return true;
         }
      }

      if (player.hasStatusEffect(ModEffects.GREEN_GHOST_DOMAIN)) {
         StatusEffectInstance greenEffect = player.getStatusEffect(ModEffects.GREEN_GHOST_DOMAIN);
         if (greenEffect != null && greenEffect.getAmplifier() + 1 > this.ghostDomainLevel) {
            return true;
         }
      }

      if (player.hasStatusEffect(ModEffects.CYAN_GHOST_DOMAIN)) {
         StatusEffectInstance cyanEffect = player.getStatusEffect(ModEffects.CYAN_GHOST_DOMAIN);
         if (cyanEffect != null && cyanEffect.getAmplifier() + 1 > this.ghostDomainLevel) {
            return true;
         }
      }

      if (player.hasStatusEffect(ModEffects.BLACK_GHOST_DOMAIN)) {
         StatusEffectInstance blackEffect = player.getStatusEffect(ModEffects.BLACK_GHOST_DOMAIN);
         if (blackEffect != null && blackEffect.getAmplifier() + 1 > this.ghostDomainLevel) {
            return true;
         }
      }

      if (player.hasStatusEffect(ModEffects.THICK_FOG)) {
         StatusEffectInstance thickFogEffect = player.getStatusEffect(ModEffects.THICK_FOG);
         if (thickFogEffect != null && thickFogEffect.getAmplifier() + 1 > this.ghostDomainLevel) {
            return true;
         }
      }

      return false;
   }

   public boolean hasGhostDomain() {
      return this.getHealth() <= this.getMaxHealth() * 0.5;
   }

   public int getGhostDomainLevel() {
      return this.ghostDomainLevel;
   }

   public void setGhostDomainLevel(int ghostDomainLevel) {
      this.ghostDomainLevel = ghostDomainLevel;
   }

   public double getGhostDomainRadius() {
      return this.ghostDomainRadius;
   }

   public void setGhostDomainRadius(double ghostDomainRadius) {
      this.ghostDomainRadius = ghostDomainRadius;
   }

   public int getSpiritualStrength() {
      return this.spiritualStrength;
   }

   public void setSpiritualStrength(int spiritualStrength) {
      this.spiritualStrength = DifficultyManager.adjustSpiritualStrength(spiritualStrength, this.getWorld());
   }

   public int getSpiritualDamage() {
      return this.spiritualDamage;
   }

   public void setSpiritualDamage(int spiritualDamage) {
      this.spiritualDamage = DifficultyManager.adjustSpiritualDamage(spiritualDamage, this.getWorld());
   }

   public int getSpiritualResistance() {
      return this.spiritualResistance;
   }

   public void setSpiritualResistance(int spiritualResistance) {
      this.spiritualResistance = DifficultyManager.adjustSpiritualResistance(spiritualResistance, this.getWorld());
   }

   public float getRecoveryFactor() {
      return this.recoveryFactor;
   }

   public void setRecoveryFactor(float recoveryFactor) {
      this.recoveryFactor = recoveryFactor;
   }

   public PlayerFaction getFaction() {
      return this.faction;
   }

   protected void startSkill() {
      this.isSkillActive = true;
      this.skillHitCount = 0;
      this.skillDelayCounter = 0;
      LivingEntity target = this.getTarget();
      if (target != null) {
         this.teleportBehindTarget(target);
      }
   }

   protected void handleSkill() {
      LivingEntity target = this.getTarget();
      if (target == null || !target.isAlive()) {
         this.endSkill();
      } else if (this.skillHitCount >= 10) {
         this.endSkill();
      } else if (this.skillDelayCounter > 0) {
         this.skillDelayCounter--;
      } else {
         double distance = this.distanceTo(target);
         if (distance > 4.0) {
            this.teleportBehindTarget(target);
            this.skillDelayCounter = 20;
         } else {
            if (this.canSee(target) && distance <= 4.0) {
               this.tryAttack(target);
               this.skillHitCount++;
               if (this.skillHitCount < 10) {
                  this.teleportBehindTarget(target);
                  this.skillDelayCounter = 20;
               }
            }
         }
      }
   }

   protected void teleportBehindTarget(LivingEntity target) {
      Vec3d targetLookVec = target.getRotationVec(1.0F);
      Vec3d behindPos = target.getPos().subtract(targetLookVec.multiply(4.0));
      behindPos = this.findSafeTeleportPosition(behindPos);
      this.teleport(behindPos.x, behindPos.y, behindPos.z);
   }

   protected Vec3d findSafeTeleportPosition(Vec3d originalPos) {
      World world = this.getWorld();
      if (this.isPositionSafe(world, originalPos)) {
         return originalPos;
      }

      for (int x = -2; x <= 2; x++) {
         for (int z = -2; z <= 2; z++) {
            Vec3d testPos = originalPos.add(x, 0.0, z);
            if (this.isPositionSafe(world, testPos)) {
               return testPos;
            }
         }
      }

      return originalPos;
   }

   protected boolean isPositionSafe(World world, Vec3d pos) {
      BlockPos blockPos = new BlockPos((int)pos.x, (int)pos.y, (int)pos.z);
      BlockPos abovePos = blockPos.up();
      return !world.getBlockState(blockPos).blocksMovement() && !world.getBlockState(abovePos).blocksMovement();
   }

   protected void endSkill() {
      this.isSkillActive = false;
      this.skillHitCount = 0;
      this.skillCooldown = 100;
   }

   public boolean tryAttack(Entity target) {
      boolean attacked = false;
      if (target instanceof GhostEntity targetGhost) {
         if (!targetGhost.isDeadlocked() && !targetGhost.isSuppressed() && targetGhost.getSpiritualStrength() > 0) {
            targetGhost.damage(this.getDamageSources().mobAttack(this), this.getSpiritualDamage());
            attacked = true;
         }
      } else {
         attacked = super.tryAttack(target);
      }

      if (attacked && this.isSkillActive && target instanceof LivingEntity livingTarget) {
         livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1, false, false, false));
         livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 20, 0, false, false, false));
      }

      if (attacked) {
         this.triggerAttackDialogue();
      }

      return attacked;
   }

   protected void triggerAttackDialogue() {
      if (this.getRandom().nextFloat() < 0.2F) {
         String dialogue = this.getAttackDialogue();
         if (dialogue != null && !dialogue.isEmpty() && !this.getWorld().isClient()) {
            this.getWorld()
               .getPlayers()
               .forEach(player -> player.sendMessage(Text.literal("§6[" + this.getGhostMasterDisplayName() + "] §f" + dialogue), false));
         }
      }
   }

   protected String getAttackDialogue() {
      return "";
   }

   public List<String> getStoryDialogues() {
      return null;
   }

   public boolean isStoryMode() {
      List<String> stories = this.getStoryDialogues();
      return stories != null && !stories.isEmpty();
   }

   public boolean hasCompletedStory(PlayerEntity player) {
      return false;
   }

   public void markStoryCompleted(PlayerEntity player) {
   }

   public String[] getGreetingDialogues() {
      return DEFAULT_GREETING_DIALOGUES;
   }

   public String getRandomGreeting() {
      String[] dialogues = this.getGreetingDialogues();
      if (dialogues == null || dialogues.length == 0) {
         dialogues = DEFAULT_GREETING_DIALOGUES;
      }

      return dialogues[this.getRandom().nextInt(dialogues.length)];
   }

   public void onDeath(DamageSource damageSource) {
      super.onDeath(damageSource);
      if (!this.getWorld().isClient()) {
         boolean ghostMasterRevival = ModConfig.getInstance().ghostMasterRevival;
         EntityType<?> entityType = this.getType();
         if (entityType != ModEntities.YE_ZHEN
            && entityType != ModEntities.YAN_LI
            && entityType != ModEntities.ZHAO_KAI_MING
            && entityType != ModEntities.XIAN_WANG
            && entityType != ModEntities.GUI_NIAO) {
            if (ghostMasterRevival) {
               this.spawnGhostAtDeathLocation();
            } else if (entityType == ModEntities.CAO_YANG) {
               if (this.shouldDropItem("cao_yang")) {
                  this.dropItem(ModItems.GHOST_WIND);
               }
            } else if (entityType == ModEntities.FENG_QUAN) {
               if (this.shouldDropItem("feng_quan")) {
                  this.dropItem(ModItems.FOG_GHOST);
                  this.dropItem(ModItems.GRAVE_EARTH_GHOST);
               }
            } else if (entityType == ModEntities.LI_JUN) {
               if (this.shouldDropItem("li_jun")) {
                  this.dropItem(ModItems.GHOST_FIRE);
               }
            } else if (entityType == ModEntities.FANG_SHI_MIN) {
               if (this.shouldDropItem("fang_shi_min")) {
                  this.dropItem(ModItems.GHOST_PRESSURE);
               }
            } else if (entityType == ModEntities.NPC1) {
               if (this.shouldDropItem("npc1")) {
                  this.dropItem(ModItems.TAITOU_GHOST);
               }
            } else if (entityType == ModEntities.NPC2) {
               if (this.shouldDropItem("npc2")) {
                  this.dropItem(ModItems.DITOU_GHOST);
               }
            } else if (entityType == ModEntities.NPC3) {
               if (this.shouldDropItem("npc3")) {
                  this.dropItem(ModItems.STEP_GHOST);
               }
            } else if (entityType == ModEntities.NPC4) {
               if (this.shouldDropItem("npc4")) {
                  this.dropItem(ModItems.JUMP_GHOST);
               }
            } else if (entityType == ModEntities.NPC5) {
               if (this.shouldDropItem("npc5")) {
                  this.dropItem(ModItems.BOX_GHOST);
               }
            } else if (entityType == ModEntities.NPC6) {
               if (this.shouldDropItem("npc6")) {
                  this.dropItem(ModItems.CROP_GHOST);
               }
            } else if (entityType == ModEntities.LI_LE_PING) {
               if (this.shouldDropItem("li_le_ping")) {
                  this.dropItem(ModItems.LOST_GHOST);
                  this.dropItem(ModItems.GHOST_SMOKE);
               }
            } else if (entityType == ModEntities.YIN_QI && this.shouldDropItem("yin_qi")) {
               this.dropItem(ModItems.XINKAI_GHOST);
            }
         } else if (entityType == ModEntities.YE_ZHEN) {
            if (this.shouldDropItem("ye_zhen")) {
               this.dropItem(ModItems.SCAPEGOAT_GHOST);
               this.dropItem(ModItems.GHOST_FIST);
            }
         } else if (entityType == ModEntities.YAN_LI) {
            this.generateBloodLakeAtDeathLocation();
         } else if (entityType == ModEntities.ZHAO_KAI_MING) {
            if (this.shouldDropItem("zhao_kai_ming")) {
               this.dropItem(ModItems.WISH_GHOST);
            }
         } else if (entityType == ModEntities.XIAN_WANG) {
            if (this.shouldDropItem("xian_wang")) {
               this.dropItem(ModItems.FUNERAL_MUSIC_GHOST);
            }
         } else if (entityType == ModEntities.GUI_NIAO && this.shouldDropItem("gui_niao")) {
            this.dropItem(ModItems.GHOST_PRESSURE);
         }

         if (this.hasCoffinNail()) {
            this.dropStack(this.getCoffinNail(), 0.5F);
         }
      }
   }

   private boolean shouldDropItem(String ghostMasterType) {
      double dropChance = ModConfig.getInstance().getGhostMasterDropChance(ghostMasterType);
      return this.getRandom().nextDouble() < dropChance;
   }

   private void spawnGhostAtDeathLocation() {
      EntityType<?> entityType = this.getType();
      if (entityType == ModEntities.CAO_YANG) {
         this.spawnGhostEntity(ModEntities.GHOST_WIND);
      } else if (entityType == ModEntities.FENG_QUAN) {
         this.spawnGhostEntity(ModEntities.FOG_GHOST);
         this.spawnGhostEntity(ModEntities.GRAVE_EARTH_GHOST);
      } else if (entityType == ModEntities.LI_JUN) {
         this.spawnGhostEntity(ModEntities.BURN_GHOST);
      } else if (entityType == ModEntities.FANG_SHI_MIN) {
         this.spawnGhostEntity(ModEntities.GHOST_PRESSURE);
      } else if (entityType == ModEntities.NPC1) {
         this.spawnGhostEntity(ModEntities.TAITOU_GHOST);
      } else if (entityType == ModEntities.NPC2) {
         this.spawnGhostEntity(ModEntities.DITOU_GHOST);
      } else if (entityType == ModEntities.NPC3) {
         this.spawnGhostEntity(ModEntities.STEP_GHOST);
      } else if (entityType == ModEntities.NPC4) {
         this.spawnGhostEntity(ModEntities.JUMP_GHOST);
      } else if (entityType == ModEntities.NPC5) {
         this.spawnGhostEntity(ModEntities.BOX_GHOST);
      } else if (entityType == ModEntities.NPC6) {
         this.spawnGhostEntity(ModEntities.CROP_GHOST);
      } else if (entityType == ModEntities.LI_LE_PING) {
         this.spawnGhostEntity(ModEntities.LOST_GHOST);
         this.spawnGhostEntity(ModEntities.GHOST_SMOKE);
      } else if (entityType == ModEntities.YANG_XIAO) {
         this.spawnGhostDreamAtDeathLocation();
      } else if (entityType == ModEntities.YIN_QI) {
         this.spawnGhostEntity(ModEntities.XINKAI_GHOST);
      } else if (entityType == ModEntities.GUI_NIAO) {
         this.spawnGhostPressureAtDeathLocation();
      }
   }

   private void spawnGhostDreamAtDeathLocation() {
      GhostDreamEntity ghostDream = new GhostDreamEntity(ModEntities.GHOST_DREAM, this.getWorld());
      ghostDream.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
      this.getWorld().spawnEntity(ghostDream);
   }

   private void spawnGhostPressureAtDeathLocation() {
      GhostPressureEntity ghostPressure = new GhostPressureEntity(ModEntities.GHOST_PRESSURE, this.getWorld());
      ghostPressure.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
      this.getWorld().spawnEntity(ghostPressure);
   }

   private void spawnGhostEntity(EntityType<?> ghostType) {
      Entity ghostEntity = ghostType.create(this.getWorld());
      if (ghostEntity != null) {
         ghostEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
         if (ghostEntity instanceof GhostEntity ghost) {
            ghost.setSpiritualStrength(100);
         }

         this.getWorld().spawnEntity(ghostEntity);
      }
   }

   private void generateBloodLakeAtDeathLocation() {
      World world = this.getWorld();
      BlockPos deathPos = this.getBlockPos();

      for (int x = -1; x <= 1; x++) {
         for (int z = -1; z <= 1; z++) {
            BlockPos lakePos = deathPos.add(x, 0, z);
            world.setBlockState(lakePos, ModFluids.BLOOD_LAKE_BLOCK.getDefaultState());
         }
      }
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("IsSuppressed", this.isSuppressed);
      nbt.putBoolean("IsDeadlocked", this.isDeadlocked);
      if (!this.coffinNail.isEmpty()) {
         NbtCompound nailNbt = new NbtCompound();
         this.coffinNail.writeNbt(nailNbt);
         nbt.put("CoffinNail", nailNbt);
      }

      NbtList offersNbt = new NbtList();

      for (TradeOffer offer : this.tradeOffers) {
         NbtCompound offerNbt = offer.toNbt();
         offersNbt.add(offerNbt);
      }

      nbt.put("TradeOffers", offersNbt);
      nbt.putBoolean("TradeOffersInitialized", this.tradeOffersInitialized);
      nbt.putInt("TradeExperience", this.tradeExperience);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("IsSuppressed")) {
         this.isSuppressed = nbt.getBoolean("IsSuppressed");
      }

      if (nbt.contains("IsDeadlocked")) {
         this.isDeadlocked = nbt.getBoolean("IsDeadlocked");
      }

      if (nbt.contains("CoffinNail")) {
         NbtCompound nailNbt = nbt.getCompound("CoffinNail");
         this.coffinNail = ItemStack.fromNbt(nailNbt);
      }

      if (nbt.contains("TradeOffers", 9)) {
         NbtList offersNbt = nbt.getList("TradeOffers", 10);
         this.tradeOffers = new TradeOfferList();

         for (int i = 0; i < offersNbt.size(); i++) {
            NbtCompound offerNbt = offersNbt.getCompound(i);
            TradeOffer offer = new TradeOffer(offerNbt);
            this.tradeOffers.add(offer);
         }
      }

      if (nbt.contains("TradeOffersInitialized")) {
         this.tradeOffersInitialized = nbt.getBoolean("TradeOffersInitialized");
      }

      if (nbt.contains("TradeExperience")) {
         this.tradeExperience = nbt.getInt("TradeExperience");
      }
   }

   public boolean isSuppressed() {
      return this.isSuppressed;
   }

   public void setSuppressed(boolean suppressed) {
      this.isSuppressed = suppressed;
   }

   public boolean isDeadlocked() {
      return this.isDeadlocked;
   }

   public void setDeadlocked(boolean deadlocked) {
      this.isDeadlocked = deadlocked;
   }

   public boolean hasCoffinNail() {
      return !this.coffinNail.isEmpty();
   }

   public ItemStack getCoffinNail() {
      return this.coffinNail;
   }

   public void setCoffinNail(ItemStack coffinNail) {
      this.coffinNail = coffinNail;
   }

   public void disableGhostDomain() {
      this.getWorld()
         .getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(this.ghostDomainRadius), player -> player instanceof PlayerEntity)
         .forEach(player -> {
            player.removeStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.RED_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.CYAN_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.THICK_FOG_TARGET);
            player.removeStatusEffect(ModEffects.LOST);
            player.removeStatusEffect(StatusEffects.SLOWNESS);
         });
   }

   private void updateBehaviorState() {
      if (this.isSuppressed()) {
         this.resetState();
      } else if (this.isDeadlocked()) {
         this.resetState();
      } else {
         GhostMasterEntity.EscapeCondition escapeCondition = this.checkEscapeConditions();
         if (escapeCondition.shouldEscape()) {
            this.handleEscape(escapeCondition);
         } else {
            GhostMasterEntity.AttackCondition attackCondition = this.checkAttackConditions();
            if (attackCondition.shouldAttack()) {
               this.handleAttack(attackCondition);
            } else {
               this.enterIdleState();
            }
         }
      }
   }

   private GhostMasterEntity.EscapeCondition checkEscapeConditions() {
      if (!this.shouldFleeFromGhosts) {
         return new GhostMasterEntity.EscapeCondition(false, null, null);
      }

      Entity nearestGhost = this.findNearestGhost();
      if (nearestGhost != null) {
         return new GhostMasterEntity.EscapeCondition(true, nearestGhost, GhostMasterEntity.EscapeCondition.EscapeReason.FROM_GHOST);
      }

      Entity powerfulPlayer = this.findPowerfulPlayer();
      return powerfulPlayer != null
         ? new GhostMasterEntity.EscapeCondition(true, powerfulPlayer, GhostMasterEntity.EscapeCondition.EscapeReason.FROM_POWERFUL_PLAYER)
         : new GhostMasterEntity.EscapeCondition(false, null, null);
   }

   private void handleEscape(GhostMasterEntity.EscapeCondition condition) {
      this.currentState = GhostMasterEntity.BehaviorState.ESCAPING;
      this.currentTarget = condition.getTarget();
      this.protectPlayerCooldown = 600;
      Vec3d targetPos = this.currentTarget.getPos();
      Vec3d thisPos = this.getPos();
      Vec3d fleeDirection = thisPos.subtract(targetPos).normalize();
      Vec3d fleeTarget = thisPos.add(fleeDirection.multiply(30.0));
      if (!this.navigation.isFollowingPath() || this.age % 20 == 0) {
         this.navigation.startMovingTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, 1.5);
      }

      this.sendEscapeMessage(condition.getReason());
      this.checkEscapeCompletion();
   }

   private void checkEscapeCompletion() {
      if (this.currentTarget != null && this.currentTarget.isAlive()) {
         double distance = this.distanceTo(this.currentTarget);
         if (distance > 30.0) {
            this.navigation.stop();
            this.resetState();
         }
      } else {
         this.resetState();
      }
   }

   private void sendEscapeMessage(GhostMasterEntity.EscapeCondition.EscapeReason reason) {
      if (!this.hasSentEscapeMessage) {
         String message = switch (reason) {
            case FROM_GHOST -> "§f" + this.getGhostMasterDisplayName() + "：开什么玩笑？！";
            case FROM_POWERFUL_PLAYER -> "§f" + this.getGhostMasterDisplayName() + "：有事好商量！";
         };
         this.broadcastMessage(message, 10.0);
         this.hasSentEscapeMessage = true;
      }
   }

   private GhostMasterEntity.AttackCondition checkAttackConditions() {
      if (this.shouldAttackPlayers) {
         PlayerEntity target = this.findWeakPlayerWithSpiritWeapon();
         if (target != null) {
            return new GhostMasterEntity.AttackCondition(true, target, GhostMasterEntity.AttackCondition.AttackReason.WEAK_PLAYER_WITH_SPIRIT_WEAPON);
         }
      }

      return new GhostMasterEntity.AttackCondition(false, null, null);
   }

   private void handleAttack(GhostMasterEntity.AttackCondition condition) {
      this.currentState = GhostMasterEntity.BehaviorState.ATTACKING;
      this.currentTarget = condition.getTarget();
      this.protectPlayerCooldown = 600;
      this.setTarget((LivingEntity)this.currentTarget);
      this.sendAttackMessage();
   }

   private void sendAttackMessage() {
      if (!this.hasSentAttackMessage) {
         String message = "§f" + this.getGhostMasterDisplayName() + "：以你这实力拿着也是浪费，不如交给我来保管。";
         this.broadcastMessage(message, 16.0);
         this.hasSentAttackMessage = true;
      }
   }

   private Entity findNearestGhost() {
      double radius = 10.0;
      return this.getWorld()
         .getEntitiesByClass(GhostEntity.class, this.getBoundingBox().expand(radius), ghost -> ghost.isAlive() && !ghost.isDeadlocked())
         .stream()
         .min((g1, g2) -> Double.compare(this.distanceTo(g1), this.distanceTo(g2)))
         .orElse(null);
   }

   private Entity findPowerfulPlayer() {
      double radius = 10.0;
      return this.getWorld()
         .getEntitiesByClass(
            PlayerEntity.class, this.getBoundingBox().expand(radius), player -> player.isAlive() && PlayerEvents.countOccupiedGhostSlots(player) >= 4
         )
         .stream()
         .min((p1, p2) -> Double.compare(this.distanceTo(p1), this.distanceTo(p2)))
         .orElse(null);
   }

   private PlayerEntity findWeakPlayerWithSpiritWeapon() {
      double radius = 10.0;

      for (PlayerEntity player : this.getWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(radius), playerx -> playerx.isAlive())) {
         int ghostCount = PlayerEvents.countOccupiedGhostSlots(player);
         boolean hasSpiritWeapon = SpiritWeapon.isSpiritWeapon(player.getMainHandStack()) || SpiritWeapon.isSpiritWeapon(player.getOffHandStack());
         if (hasSpiritWeapon && ghostCount < 2) {
            return player;
         }
      }

      return null;
   }

   private Entity findGhostNearPlayer(PlayerEntity player, double radius) {
      List<GhostEntity> ghostsNearPlayer = this.getWorld()
         .getEntitiesByClass(
            GhostEntity.class,
            player.getBoundingBox().expand(radius),
            ghost -> ghost.isAlive()
               && !ghost.isDeadlocked()
               && ghost.getSpiritualStrength() > 0
               && (ghost.getMaxSpiritualStrength() <= 0 || (float)ghost.getSpiritualStrength() / ghost.getMaxSpiritualStrength() > 0.2F)
         );
      return !ghostsNearPlayer.isEmpty()
         ? (Entity)ghostsNearPlayer.stream().min((g1, g2) -> Double.compare(this.distanceTo(g1), this.distanceTo(g2))).orElse(null)
         : null;
   }

   private void broadcastMessage(String message, double radius) {
      this.getWorld()
         .getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(radius), player -> player instanceof PlayerEntity)
         .forEach(player -> player.sendMessage(Text.literal(message)));
   }

   private void resetState() {
      this.currentState = GhostMasterEntity.BehaviorState.IDLE;
      this.currentTarget = null;
      this.hasSentEscapeMessage = false;
      this.hasSentAttackMessage = false;
   }

   private void enterIdleState() {
      if (this.currentState != GhostMasterEntity.BehaviorState.IDLE) {
         this.resetState();
      }
   }

   private boolean handleGhostDetectionAndEscape() {
      this.updateBehaviorState();
      return this.currentState != GhostMasterEntity.BehaviorState.IDLE;
   }

   private void attackHostileMobsNearPlayers() {
      if (this.shouldProtectPlayers) {
         if (this.protectPlayerCooldown <= 0) {
            if (!this.isSuppressed() && !this.isDeadlocked()) {
               if (this.currentState != GhostMasterEntity.BehaviorState.ESCAPING) {
                  double radius = 10.0;
                  List<PlayerEntity> nearbyPlayers = this.getWorld()
                     .getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(radius), playerx -> playerx instanceof PlayerEntity);
                  if (!nearbyPlayers.isEmpty()) {
                     for (PlayerEntity player : nearbyPlayers) {
                        if (!this.attackedPlayers.contains(player.getUuid())) {
                           if (this.faction != PlayerFaction.ORDINARY) {
                              PlayerFaction playerFaction = FactionManager.getFaction(player);
                              if (playerFaction != this.faction) {
                                 continue;
                              }
                           }

                           if (this.shouldAttackGhostsNearPlayers) {
                              Entity ghostTarget = this.findGhostNearPlayer(player, 8.0);
                              if (ghostTarget != null && this.distanceTo(ghostTarget) <= 16.0 && (this.getTarget() == null || !this.getTarget().isAlive())) {
                                 this.setTarget((LivingEntity)ghostTarget);
                                 if (!this.hasSentWarning && this.getRandom().nextFloat() < 0.3F) {
                                    player.sendMessage(Text.literal("§f" + this.getGhostMasterDisplayName() + "：小心！"));
                                    this.hasSentWarning = true;
                                 }
                                 continue;
                              }
                           }

                           List<HostileEntity> hostileMobs = this.getWorld()
                              .getEntitiesByClass(
                                 HostileEntity.class,
                                 player.getBoundingBox().expand(radius),
                                 mob -> mob instanceof HostileEntity && mob.isAlive() && mob.getUuid() != this.getUuid()
                              );
                           if (!hostileMobs.isEmpty()) {
                              HostileEntity nearestHostile = hostileMobs.stream()
                                 .min((mob1, mob2) -> Double.compare(this.distanceTo(mob1), this.distanceTo(mob2)))
                                 .orElse(null);
                              if (this.distanceTo(nearestHostile) <= 16.0 && (this.getTarget() == null || !this.getTarget().isAlive())) {
                                 this.setTarget(nearestHostile);
                                 if (!this.hasSentWarning && this.getRandom().nextFloat() < 0.3F) {
                                    player.sendMessage(Text.literal("§f" + this.getGhostMasterDisplayName() + "：小心！"));
                                    this.hasSentWarning = true;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void triggerSpiritCounterAttack(DamageSource source) {
      if (!this.getWorld().isClient() && source.getAttacker() instanceof PlayerEntity player) {
         if (this.isSuppressed() || this.isDeadlocked()) {
            return;
         }

         if (this.currentState == GhostMasterEntity.BehaviorState.ESCAPING) {
            return;
         }

         this.setTarget(player);
      }
   }

   public boolean retrieveCoffinNail() {
      if (this.hasCoffinNail()) {
         ItemStack coffinNail = new ItemStack(ModItems.COFFIN_NAIL);
         this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), coffinNail));
         this.setSuppressed(false);
         this.setDeadlocked(false);
         this.setCoffinNail(ItemStack.EMPTY);
         this.setAiDisabled(false);
         return true;
      } else {
         return false;
      }
   }

   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         this.tradeOffersInitialized = true;
      }
   }

   public void openTradeScreen(PlayerEntity player) {
      if (!this.getWorld().isClient) {
         if (!this.tradeOffersInitialized) {
            this.initTradeOffers();
         }

         if (this.getCustomer() != player) {
            this.setCustomer(player);
            this.setAiDisabled(true);
            this.getNavigation().stop();
            player.openHandledScreen(new NamedScreenHandlerFactory() {
               public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity playerx) {
                  return new MerchantScreenHandler(syncId, inventory, GhostMasterEntity.this);
               }

               public Text getDisplayName() {
                  return GhostMasterEntity.this.getDisplayName();
               }
            });
            if (player instanceof ServerPlayerEntity serverPlayer) {
               int syncId = serverPlayer.currentScreenHandler != null ? serverPlayer.currentScreenHandler.syncId : 0;
               serverPlayer.networkHandler
                  .sendPacket(new SetTradeOffersS2CPacket(syncId, this.tradeOffers, 0, this.getExperience(), this.isLeveledMerchant(), this.canRefreshTrades()));
            }
         }
      }
   }

   public TradeOfferList getOffers() {
      if (!this.tradeOffersInitialized) {
         this.initTradeOffers();
      }

      return this.tradeOffers;
   }

   public void setOffersFromServer(TradeOfferList offers) {
      if (offers != null) {
         this.tradeOffers = offers;
         this.tradeOffersInitialized = true;
      }
   }

   public void setCustomer(PlayerEntity customer) {
      if (customer == null && this.customer != null) {
         this.setAiDisabled(false);
      }

      this.customer = customer;
   }

   public PlayerEntity getCustomer() {
      return this.customer;
   }

   public void trade(TradeOffer offer) {
      offer.use();
   }

   public void onSellingItem(ItemStack stack) {
   }

   public int getExperience() {
      return this.tradeExperience;
   }

   public void setExperienceFromServer(int experience) {
      this.tradeExperience = experience;
   }

   public boolean isLeveledMerchant() {
      return false;
   }

   public SoundEvent getYesSound() {
      return null;
   }

   public boolean isClient() {
      return this.getWorld().isClient;
   }

   public boolean canRefreshTrades() {
      return false;
   }

   public Map<String, String> getQuestList() {
      return this.questList;
   }

   public boolean hasQuests() {
      return !this.questList.isEmpty();
   }

   public void addQuest(String questId, String questText) {
      this.questList.put(questId, questText);
   }

   public void removeQuest(String questId) {
      this.questList.remove(questId);
   }

   public void clearQuests() {
      this.questList.clear();
   }

   public String getQuestText(String questId) {
      return this.questList.get(questId);
   }

   private static class AttackCondition {
      private final boolean shouldAttack;
      private final PlayerEntity target;
      private final GhostMasterEntity.AttackCondition.AttackReason reason;

      public AttackCondition(boolean shouldAttack, PlayerEntity target, GhostMasterEntity.AttackCondition.AttackReason reason) {
         this.shouldAttack = shouldAttack;
         this.target = target;
         this.reason = reason;
      }

      public boolean shouldAttack() {
         return this.shouldAttack;
      }

      public PlayerEntity getTarget() {
         return this.target;
      }

      public GhostMasterEntity.AttackCondition.AttackReason getReason() {
         return this.reason;
      }

      public enum AttackReason {
         WEAK_PLAYER_WITH_SPIRIT_WEAPON;
      }
   }

   private enum BehaviorState {
      IDLE,
      ESCAPING,
      ATTACKING;
   }

   private static class EscapeCondition {
      private final boolean shouldEscape;
      private final Entity target;
      private final GhostMasterEntity.EscapeCondition.EscapeReason reason;

      public EscapeCondition(boolean shouldEscape, Entity target, GhostMasterEntity.EscapeCondition.EscapeReason reason) {
         this.shouldEscape = shouldEscape;
         this.target = target;
         this.reason = reason;
      }

      public boolean shouldEscape() {
         return this.shouldEscape;
      }

      public Entity getTarget() {
         return this.target;
      }

      public GhostMasterEntity.EscapeCondition.EscapeReason getReason() {
         return this.reason;
      }

      public enum EscapeReason {
         FROM_GHOST,
         FROM_POWERFUL_PLAYER;
      }
   }
}
