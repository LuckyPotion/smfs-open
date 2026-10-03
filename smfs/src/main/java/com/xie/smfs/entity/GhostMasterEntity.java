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
   protected ItemStack coffinNail = ItemStack.field_8037;
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
      this.method_5665(Text.method_43470("§6" + this.getGhostMasterDisplayName()));
      this.method_5880(true);
      this.method_5803(true);
      this.spiritualStrength = DifficultyManager.adjustSpiritualStrength(this.spiritualStrength, this.method_37908());
      this.spiritualDamage = DifficultyManager.adjustSpiritualDamage(this.spiritualDamage, this.method_37908());
      this.spiritualResistance = DifficultyManager.adjustSpiritualResistance(this.spiritualResistance, this.method_37908());
   }

   protected abstract String getGhostMasterDisplayName();

   public static Builder createGhostMasterAttributes() {
      return MobEntity.method_26828()
         .method_26868(EntityAttributes.field_23716, 1000.0)
         .method_26868(EntityAttributes.field_23719, 0.3)
         .method_26868(EntityAttributes.field_23721, 15.0)
         .method_26868(EntityAttributes.field_23717, 16.0);
   }

   protected void method_5959() {
      super.method_5959();
      this.field_6185.method_6277(1, new RevengeGoal(this, new Class[0]));
      this.field_6201.method_6277(1, new MeleeAttackGoal(this, 1.2, true));
      this.field_6201.method_6277(2, new WanderAroundGoal(this, 0.8));
      this.field_6201.method_6277(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.field_6201.method_6277(4, new LookAroundGoal(this));
   }

   public boolean method_5643(DamageSource source, float amount) {
      if (this.isGoldenSwordDamage(source)) {
         if (source.method_5529() instanceof PlayerEntity player) {
            this.attackedPlayers.add(player.method_5667());
         }

         return super.method_5643(source, amount);
      } else if (this.isGoldenBulletDamage(source)) {
         if (source.method_5526() instanceof PlayerEntity player) {
            this.attackedPlayers.add(player.method_5667());
         }

         return super.method_5643(source, amount);
      } else if (this.isSpiritDamage(source)) {
         if (source.method_5529() instanceof PlayerEntity player) {
            this.attackedPlayers.add(player.method_5667());
         }

         this.triggerSpiritCounterAttack(source);
         float actualDamage = PlayerEvents.calculateSpiritDamage(amount, this.getSpiritResistance());
         return super.method_5643(source, actualDamage);
      } else {
         return false;
      }
   }

   public boolean method_5679(DamageSource damageSource) {
      return !this.isGoldenSwordDamage(damageSource) && !this.isGoldenBulletDamage(damageSource) && !this.isSpiritDamage(damageSource);
   }

   private boolean isGoldenSwordDamage(DamageSource source) {
      if (!(source.method_5529() instanceof PlayerEntity player)) {
         return false;
      } else {
         ItemStack heldItem = player.method_6047();
         return heldItem.method_7909() == Items.field_8845 || heldItem.method_7909() == Items.field_8825;
      }
   }

   private boolean isGoldenBulletDamage(DamageSource source) {
      return source.method_5526() instanceof GoldenBulletEntity;
   }

   private boolean isSpiritDamage(DamageSource source) {
      if (ModEvents.processingSpiritDamage.get()) {
         return true;
      }

      if (!(source.method_5529() instanceof GhostEntity) && !(source.method_5529() instanceof GhostDreamEntity)) {
         if (source.method_5529() instanceof PlayerEntity player) {
            boolean hasSpiritWeapon = SpiritWeapon.isSpiritWeapon(player.method_6047());
            boolean hasCorpseOilWeapon = false;
            if (player.method_6047().method_7909() instanceof SwordItem && WeaponOilHandler.getCorpseOilLayers(player.method_6047()) > 0) {
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

   public boolean method_5810() {
      return false;
   }

   public ActionResult method_5992(PlayerEntity player, Hand hand) {
      return super.method_5992(player, hand);
   }

   protected Identifier method_5991() {
      return new Identifier("smfs", "entities/ghost_master");
   }

   public void method_5697(Entity entity) {
      if (!(entity instanceof BoatEntity) && !(entity instanceof AbstractMinecartEntity)) {
         super.method_5697(entity);
      }
   }

   public void method_5773() {
      super.method_5773();
      if (!this.isSuppressed && !this.isDeadlocked) {
         if (!this.method_37908().method_8608()) {
            if (this.method_5968() instanceof GhostEntity ghostTarget
               && (
                  ghostTarget.getSpiritualStrength() <= 0
                     || ghostTarget.getMaxSpiritualStrength() > 0 && (float)ghostTarget.getSpiritualStrength() / ghostTarget.getMaxSpiritualStrength() <= 0.2F
               )) {
               this.method_5980(null);
               this.hasSentWarning = false;
            }

            if (this.method_6032() <= this.method_6063() * 0.5 && this.ghostDomainLevel >= 1) {
               this.applyGhostDomainToNearbyPlayers(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
               if (!this.isSkillActive
                  && this.skillCooldown <= 0
                  && this.currentState != GhostMasterEntity.BehaviorState.ESCAPING
                  && this.field_5974.method_43057() < 0.01F) {
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

            if (!this.method_37908().method_8608() && this.field_6012 % 20 == 0 && this.method_5968() == null) {
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
         this.method_5980(null);
         this.method_6015(null);
         this.method_18800(0.0, this.method_18798().field_1351, 0.0);
         this.field_6189.method_6340();
         this.method_5977(true);
      }
   }

   protected void enableGhostDomain(PlayerEntity player, StatusEffect effect) {
      player.method_6092(new StatusEffectInstance(effect, 20, this.ghostDomainLevel - 1, false, false, false));
   }

   protected void applyGhostDomainToNearbyPlayers(StatusEffect effect) {
      double radius = this.ghostDomainRadius;
      this.method_37908().method_8390(PlayerEntity.class, this.method_5829().method_1014(radius), player -> player instanceof PlayerEntity).forEach(player -> {
         double distance = this.method_5739(player);
         if (distance <= radius) {
            boolean hasHigherLevelGhostDomain = this.checkPlayerHasHigherLevelGhostDomain(player);
            if (!hasHigherLevelGhostDomain) {
               this.enableGhostDomain(player, effect);
               player.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 100, this.ghostDomainLevel - 1, false, false, false));
               if (!player.method_6059(ModEffects.LOST)) {
                  player.method_6092(new LostStatusEffectInstance(ModEffects.LOST, Integer.MAX_VALUE, 0, false, false, false, this.method_5667()));
               }
            } else {
               player.method_6016(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
               player.method_6016(ModEffects.RED_GHOST_DOMAIN_TARGET);
               player.method_6016(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
               player.method_6016(ModEffects.CYAN_GHOST_DOMAIN_TARGET);
               player.method_6016(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
               player.method_6016(ModEffects.THICK_FOG_TARGET);
               player.method_6016(ModEffects.LOST);
               player.method_6016(StatusEffects.field_5909);
            }
         }
      });
   }

   private boolean checkPlayerHasHigherLevelGhostDomain(PlayerEntity player) {
      if (player.method_6059(ModEffects.GOLDEN_GHOST_DOMAIN)) {
         StatusEffectInstance goldenEffect = player.method_6112(ModEffects.GOLDEN_GHOST_DOMAIN);
         if (goldenEffect != null && goldenEffect.method_5578() + 1 > this.ghostDomainLevel) {
            return true;
         }
      }

      if (player.method_6059(ModEffects.RED_GHOST_DOMAIN)) {
         StatusEffectInstance redEffect = player.method_6112(ModEffects.RED_GHOST_DOMAIN);
         if (redEffect != null && redEffect.method_5578() + 1 > this.ghostDomainLevel) {
            return true;
         }
      }

      if (player.method_6059(ModEffects.GREEN_GHOST_DOMAIN)) {
         StatusEffectInstance greenEffect = player.method_6112(ModEffects.GREEN_GHOST_DOMAIN);
         if (greenEffect != null && greenEffect.method_5578() + 1 > this.ghostDomainLevel) {
            return true;
         }
      }

      if (player.method_6059(ModEffects.CYAN_GHOST_DOMAIN)) {
         StatusEffectInstance cyanEffect = player.method_6112(ModEffects.CYAN_GHOST_DOMAIN);
         if (cyanEffect != null && cyanEffect.method_5578() + 1 > this.ghostDomainLevel) {
            return true;
         }
      }

      if (player.method_6059(ModEffects.BLACK_GHOST_DOMAIN)) {
         StatusEffectInstance blackEffect = player.method_6112(ModEffects.BLACK_GHOST_DOMAIN);
         if (blackEffect != null && blackEffect.method_5578() + 1 > this.ghostDomainLevel) {
            return true;
         }
      }

      if (player.method_6059(ModEffects.THICK_FOG)) {
         StatusEffectInstance thickFogEffect = player.method_6112(ModEffects.THICK_FOG);
         if (thickFogEffect != null && thickFogEffect.method_5578() + 1 > this.ghostDomainLevel) {
            return true;
         }
      }

      return false;
   }

   public boolean hasGhostDomain() {
      return this.method_6032() <= this.method_6063() * 0.5;
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
      this.spiritualStrength = DifficultyManager.adjustSpiritualStrength(spiritualStrength, this.method_37908());
   }

   public int getSpiritualDamage() {
      return this.spiritualDamage;
   }

   public void setSpiritualDamage(int spiritualDamage) {
      this.spiritualDamage = DifficultyManager.adjustSpiritualDamage(spiritualDamage, this.method_37908());
   }

   public int getSpiritualResistance() {
      return this.spiritualResistance;
   }

   public void setSpiritualResistance(int spiritualResistance) {
      this.spiritualResistance = DifficultyManager.adjustSpiritualResistance(spiritualResistance, this.method_37908());
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
      LivingEntity target = this.method_5968();
      if (target != null) {
         this.teleportBehindTarget(target);
      }
   }

   protected void handleSkill() {
      LivingEntity target = this.method_5968();
      if (target == null || !target.method_5805()) {
         this.endSkill();
      } else if (this.skillHitCount >= 10) {
         this.endSkill();
      } else if (this.skillDelayCounter > 0) {
         this.skillDelayCounter--;
      } else {
         double distance = this.method_5739(target);
         if (distance > 4.0) {
            this.teleportBehindTarget(target);
            this.skillDelayCounter = 20;
         } else {
            if (this.method_6057(target) && distance <= 4.0) {
               this.method_6121(target);
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
      Vec3d targetLookVec = target.method_5828(1.0F);
      Vec3d behindPos = target.method_19538().method_1020(targetLookVec.method_1021(4.0));
      behindPos = this.findSafeTeleportPosition(behindPos);
      this.method_20620(behindPos.field_1352, behindPos.field_1351, behindPos.field_1350);
   }

   protected Vec3d findSafeTeleportPosition(Vec3d originalPos) {
      World world = this.method_37908();
      if (this.isPositionSafe(world, originalPos)) {
         return originalPos;
      }

      for (int x = -2; x <= 2; x++) {
         for (int z = -2; z <= 2; z++) {
            Vec3d testPos = originalPos.method_1031(x, 0.0, z);
            if (this.isPositionSafe(world, testPos)) {
               return testPos;
            }
         }
      }

      return originalPos;
   }

   protected boolean isPositionSafe(World world, Vec3d pos) {
      BlockPos blockPos = new BlockPos((int)pos.field_1352, (int)pos.field_1351, (int)pos.field_1350);
      BlockPos abovePos = blockPos.method_10084();
      return !world.method_8320(blockPos).method_51366() && !world.method_8320(abovePos).method_51366();
   }

   protected void endSkill() {
      this.isSkillActive = false;
      this.skillHitCount = 0;
      this.skillCooldown = 100;
   }

   public boolean method_6121(Entity target) {
      boolean attacked = false;
      if (target instanceof GhostEntity targetGhost) {
         if (!targetGhost.isDeadlocked() && !targetGhost.isSuppressed() && targetGhost.getSpiritualStrength() > 0) {
            targetGhost.method_5643(this.method_48923().method_48812(this), this.getSpiritualDamage());
            attacked = true;
         }
      } else {
         attacked = super.method_6121(target);
      }

      if (attacked && this.isSkillActive && target instanceof LivingEntity livingTarget) {
         livingTarget.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 40, 1, false, false, false));
         livingTarget.method_6092(new StatusEffectInstance(StatusEffects.field_5919, 20, 0, false, false, false));
      }

      if (attacked) {
         this.triggerAttackDialogue();
      }

      return attacked;
   }

   protected void triggerAttackDialogue() {
      if (this.method_6051().method_43057() < 0.2F) {
         String dialogue = this.getAttackDialogue();
         if (dialogue != null && !dialogue.isEmpty() && !this.method_37908().method_8608()) {
            this.method_37908()
               .method_18456()
               .forEach(player -> player.method_7353(Text.method_43470("§6[" + this.getGhostMasterDisplayName() + "] §f" + dialogue), false));
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

      return dialogues[this.method_6051().method_43048(dialogues.length)];
   }

   public void method_6078(DamageSource damageSource) {
      super.method_6078(damageSource);
      if (!this.method_37908().method_8608()) {
         boolean ghostMasterRevival = ModConfig.getInstance().ghostMasterRevival;
         EntityType<?> entityType = this.method_5864();
         if (entityType != ModEntities.YE_ZHEN
            && entityType != ModEntities.YAN_LI
            && entityType != ModEntities.ZHAO_KAI_MING
            && entityType != ModEntities.XIAN_WANG
            && entityType != ModEntities.GUI_NIAO) {
            if (ghostMasterRevival) {
               this.spawnGhostAtDeathLocation();
            } else if (entityType == ModEntities.CAO_YANG) {
               if (this.shouldDropItem("cao_yang")) {
                  this.method_5706(ModItems.GHOST_WIND);
               }
            } else if (entityType == ModEntities.FENG_QUAN) {
               if (this.shouldDropItem("feng_quan")) {
                  this.method_5706(ModItems.FOG_GHOST);
                  this.method_5706(ModItems.GRAVE_EARTH_GHOST);
               }
            } else if (entityType == ModEntities.LI_JUN) {
               if (this.shouldDropItem("li_jun")) {
                  this.method_5706(ModItems.GHOST_FIRE);
               }
            } else if (entityType == ModEntities.FANG_SHI_MIN) {
               if (this.shouldDropItem("fang_shi_min")) {
                  this.method_5706(ModItems.GHOST_PRESSURE);
               }
            } else if (entityType == ModEntities.NPC1) {
               if (this.shouldDropItem("npc1")) {
                  this.method_5706(ModItems.TAITOU_GHOST);
               }
            } else if (entityType == ModEntities.NPC2) {
               if (this.shouldDropItem("npc2")) {
                  this.method_5706(ModItems.DITOU_GHOST);
               }
            } else if (entityType == ModEntities.NPC3) {
               if (this.shouldDropItem("npc3")) {
                  this.method_5706(ModItems.STEP_GHOST);
               }
            } else if (entityType == ModEntities.NPC4) {
               if (this.shouldDropItem("npc4")) {
                  this.method_5706(ModItems.JUMP_GHOST);
               }
            } else if (entityType == ModEntities.NPC5) {
               if (this.shouldDropItem("npc5")) {
                  this.method_5706(ModItems.BOX_GHOST);
               }
            } else if (entityType == ModEntities.NPC6) {
               if (this.shouldDropItem("npc6")) {
                  this.method_5706(ModItems.CROP_GHOST);
               }
            } else if (entityType == ModEntities.LI_LE_PING) {
               if (this.shouldDropItem("li_le_ping")) {
                  this.method_5706(ModItems.LOST_GHOST);
                  this.method_5706(ModItems.GHOST_SMOKE);
               }
            } else if (entityType == ModEntities.YIN_QI && this.shouldDropItem("yin_qi")) {
               this.method_5706(ModItems.XINKAI_GHOST);
            }
         } else if (entityType == ModEntities.YE_ZHEN) {
            if (this.shouldDropItem("ye_zhen")) {
               this.method_5706(ModItems.SCAPEGOAT_GHOST);
               this.method_5706(ModItems.GHOST_FIST);
            }
         } else if (entityType == ModEntities.YAN_LI) {
            this.generateBloodLakeAtDeathLocation();
         } else if (entityType == ModEntities.ZHAO_KAI_MING) {
            if (this.shouldDropItem("zhao_kai_ming")) {
               this.method_5706(ModItems.WISH_GHOST);
            }
         } else if (entityType == ModEntities.XIAN_WANG) {
            if (this.shouldDropItem("xian_wang")) {
               this.method_5706(ModItems.FUNERAL_MUSIC_GHOST);
            }
         } else if (entityType == ModEntities.GUI_NIAO && this.shouldDropItem("gui_niao")) {
            this.method_5706(ModItems.GHOST_PRESSURE);
         }

         if (this.hasCoffinNail()) {
            this.method_5699(this.getCoffinNail(), 0.5F);
         }
      }
   }

   private boolean shouldDropItem(String ghostMasterType) {
      double dropChance = ModConfig.getInstance().getGhostMasterDropChance(ghostMasterType);
      return this.method_6051().method_43058() < dropChance;
   }

   private void spawnGhostAtDeathLocation() {
      EntityType<?> entityType = this.method_5864();
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
      GhostDreamEntity ghostDream = new GhostDreamEntity(ModEntities.GHOST_DREAM, this.method_37908());
      ghostDream.method_5808(this.method_23317(), this.method_23318(), this.method_23321(), this.method_36454(), this.method_36455());
      this.method_37908().method_8649(ghostDream);
   }

   private void spawnGhostPressureAtDeathLocation() {
      GhostPressureEntity ghostPressure = new GhostPressureEntity(ModEntities.GHOST_PRESSURE, this.method_37908());
      ghostPressure.method_5808(this.method_23317(), this.method_23318(), this.method_23321(), this.method_36454(), this.method_36455());
      this.method_37908().method_8649(ghostPressure);
   }

   private void spawnGhostEntity(EntityType<?> ghostType) {
      Entity ghostEntity = ghostType.method_5883(this.method_37908());
      if (ghostEntity != null) {
         ghostEntity.method_5808(this.method_23317(), this.method_23318(), this.method_23321(), this.method_36454(), this.method_36455());
         if (ghostEntity instanceof GhostEntity ghost) {
            ghost.setSpiritualStrength(100);
         }

         this.method_37908().method_8649(ghostEntity);
      }
   }

   private void generateBloodLakeAtDeathLocation() {
      World world = this.method_37908();
      BlockPos deathPos = this.method_24515();

      for (int x = -1; x <= 1; x++) {
         for (int z = -1; z <= 1; z++) {
            BlockPos lakePos = deathPos.method_10069(x, 0, z);
            world.method_8501(lakePos, ModFluids.BLOOD_LAKE_BLOCK.method_9564());
         }
      }
   }

   public void method_5652(NbtCompound nbt) {
      super.method_5652(nbt);
      nbt.method_10556("IsSuppressed", this.isSuppressed);
      nbt.method_10556("IsDeadlocked", this.isDeadlocked);
      if (!this.coffinNail.method_7960()) {
         NbtCompound nailNbt = new NbtCompound();
         this.coffinNail.method_7953(nailNbt);
         nbt.method_10566("CoffinNail", nailNbt);
      }

      NbtList offersNbt = new NbtList();

      for (TradeOffer offer : this.tradeOffers) {
         NbtCompound offerNbt = offer.method_8251();
         offersNbt.add(offerNbt);
      }

      nbt.method_10566("TradeOffers", offersNbt);
      nbt.method_10556("TradeOffersInitialized", this.tradeOffersInitialized);
      nbt.method_10569("TradeExperience", this.tradeExperience);
   }

   public void method_5749(NbtCompound nbt) {
      super.method_5749(nbt);
      if (nbt.method_10545("IsSuppressed")) {
         this.isSuppressed = nbt.method_10577("IsSuppressed");
      }

      if (nbt.method_10545("IsDeadlocked")) {
         this.isDeadlocked = nbt.method_10577("IsDeadlocked");
      }

      if (nbt.method_10545("CoffinNail")) {
         NbtCompound nailNbt = nbt.method_10562("CoffinNail");
         this.coffinNail = ItemStack.method_7915(nailNbt);
      }

      if (nbt.method_10573("TradeOffers", 9)) {
         NbtList offersNbt = nbt.method_10554("TradeOffers", 10);
         this.tradeOffers = new TradeOfferList();

         for (int i = 0; i < offersNbt.size(); i++) {
            NbtCompound offerNbt = offersNbt.method_10602(i);
            TradeOffer offer = new TradeOffer(offerNbt);
            this.tradeOffers.add(offer);
         }
      }

      if (nbt.method_10545("TradeOffersInitialized")) {
         this.tradeOffersInitialized = nbt.method_10577("TradeOffersInitialized");
      }

      if (nbt.method_10545("TradeExperience")) {
         this.tradeExperience = nbt.method_10550("TradeExperience");
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
      return !this.coffinNail.method_7960();
   }

   public ItemStack getCoffinNail() {
      return this.coffinNail;
   }

   public void setCoffinNail(ItemStack coffinNail) {
      this.coffinNail = coffinNail;
   }

   public void disableGhostDomain() {
      this.method_37908()
         .method_8390(PlayerEntity.class, this.method_5829().method_1014(this.ghostDomainRadius), player -> player instanceof PlayerEntity)
         .forEach(player -> {
            player.method_6016(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.RED_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.CYAN_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.THICK_FOG_TARGET);
            player.method_6016(ModEffects.LOST);
            player.method_6016(StatusEffects.field_5909);
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
      Vec3d targetPos = this.currentTarget.method_19538();
      Vec3d thisPos = this.method_19538();
      Vec3d fleeDirection = thisPos.method_1020(targetPos).method_1029();
      Vec3d fleeTarget = thisPos.method_1019(fleeDirection.method_1021(30.0));
      if (!this.field_6189.method_23966() || this.field_6012 % 20 == 0) {
         this.field_6189.method_6337(fleeTarget.field_1352, fleeTarget.field_1351, fleeTarget.field_1350, 1.5);
      }

      this.sendEscapeMessage(condition.getReason());
      this.checkEscapeCompletion();
   }

   private void checkEscapeCompletion() {
      if (this.currentTarget != null && this.currentTarget.method_5805()) {
         double distance = this.method_5739(this.currentTarget);
         if (distance > 30.0) {
            this.field_6189.method_6340();
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
      this.method_5980((LivingEntity)this.currentTarget);
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
      return this.method_37908()
         .method_8390(GhostEntity.class, this.method_5829().method_1014(radius), ghost -> ghost.method_5805() && !ghost.isDeadlocked())
         .stream()
         .min((g1, g2) -> Double.compare(this.method_5739(g1), this.method_5739(g2)))
         .orElse(null);
   }

   private Entity findPowerfulPlayer() {
      double radius = 10.0;
      return this.method_37908()
         .method_8390(
            PlayerEntity.class, this.method_5829().method_1014(radius), player -> player.method_5805() && PlayerEvents.countOccupiedGhostSlots(player) >= 4
         )
         .stream()
         .min((p1, p2) -> Double.compare(this.method_5739(p1), this.method_5739(p2)))
         .orElse(null);
   }

   private PlayerEntity findWeakPlayerWithSpiritWeapon() {
      double radius = 10.0;

      for (PlayerEntity player : this.method_37908().method_8390(PlayerEntity.class, this.method_5829().method_1014(radius), playerx -> playerx.method_5805())) {
         int ghostCount = PlayerEvents.countOccupiedGhostSlots(player);
         boolean hasSpiritWeapon = SpiritWeapon.isSpiritWeapon(player.method_6047()) || SpiritWeapon.isSpiritWeapon(player.method_6079());
         if (hasSpiritWeapon && ghostCount < 2) {
            return player;
         }
      }

      return null;
   }

   private Entity findGhostNearPlayer(PlayerEntity player, double radius) {
      List<GhostEntity> ghostsNearPlayer = this.method_37908()
         .method_8390(
            GhostEntity.class,
            player.method_5829().method_1014(radius),
            ghost -> ghost.method_5805()
               && !ghost.isDeadlocked()
               && ghost.getSpiritualStrength() > 0
               && (ghost.getMaxSpiritualStrength() <= 0 || (float)ghost.getSpiritualStrength() / ghost.getMaxSpiritualStrength() > 0.2F)
         );
      return !ghostsNearPlayer.isEmpty()
         ? (Entity)ghostsNearPlayer.stream().min((g1, g2) -> Double.compare(this.method_5739(g1), this.method_5739(g2))).orElse(null)
         : null;
   }

   private void broadcastMessage(String message, double radius) {
      this.method_37908()
         .method_8390(PlayerEntity.class, this.method_5829().method_1014(radius), player -> player instanceof PlayerEntity)
         .forEach(player -> player.method_43496(Text.method_43470(message)));
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
                  List<PlayerEntity> nearbyPlayers = this.method_37908()
                     .method_8390(PlayerEntity.class, this.method_5829().method_1014(radius), playerx -> playerx instanceof PlayerEntity);
                  if (!nearbyPlayers.isEmpty()) {
                     for (PlayerEntity player : nearbyPlayers) {
                        if (!this.attackedPlayers.contains(player.method_5667())) {
                           if (this.faction != PlayerFaction.ORDINARY) {
                              PlayerFaction playerFaction = FactionManager.getFaction(player);
                              if (playerFaction != this.faction) {
                                 continue;
                              }
                           }

                           if (this.shouldAttackGhostsNearPlayers) {
                              Entity ghostTarget = this.findGhostNearPlayer(player, 8.0);
                              if (ghostTarget != null
                                 && this.method_5739(ghostTarget) <= 16.0
                                 && (this.method_5968() == null || !this.method_5968().method_5805())) {
                                 this.method_5980((LivingEntity)ghostTarget);
                                 if (!this.hasSentWarning && this.method_6051().method_43057() < 0.3F) {
                                    player.method_43496(Text.method_43470("§f" + this.getGhostMasterDisplayName() + "：小心！"));
                                    this.hasSentWarning = true;
                                 }
                                 continue;
                              }
                           }

                           List<HostileEntity> hostileMobs = this.method_37908()
                              .method_8390(
                                 HostileEntity.class,
                                 player.method_5829().method_1014(radius),
                                 mob -> mob instanceof HostileEntity && mob.method_5805() && mob.method_5667() != this.method_5667()
                              );
                           if (!hostileMobs.isEmpty()) {
                              HostileEntity nearestHostile = hostileMobs.stream()
                                 .min((mob1, mob2) -> Double.compare(this.method_5739(mob1), this.method_5739(mob2)))
                                 .orElse(null);
                              if (this.method_5739(nearestHostile) <= 16.0 && (this.method_5968() == null || !this.method_5968().method_5805())) {
                                 this.method_5980(nearestHostile);
                                 if (!this.hasSentWarning && this.method_6051().method_43057() < 0.3F) {
                                    player.method_43496(Text.method_43470("§f" + this.getGhostMasterDisplayName() + "：小心！"));
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
      if (!this.method_37908().method_8608() && source.method_5529() instanceof PlayerEntity player) {
         if (this.isSuppressed() || this.isDeadlocked()) {
            return;
         }

         if (this.currentState == GhostMasterEntity.BehaviorState.ESCAPING) {
            return;
         }

         this.method_5980(player);
      }
   }

   public boolean retrieveCoffinNail() {
      if (this.hasCoffinNail()) {
         ItemStack coffinNail = new ItemStack(ModItems.COFFIN_NAIL);
         this.method_37908().method_8649(new ItemEntity(this.method_37908(), this.method_23317(), this.method_23318(), this.method_23321(), coffinNail));
         this.setSuppressed(false);
         this.setDeadlocked(false);
         this.setCoffinNail(ItemStack.field_8037);
         this.method_5977(false);
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
      if (!this.method_37908().field_9236) {
         if (!this.tradeOffersInitialized) {
            this.initTradeOffers();
         }

         if (this.method_8257() != player) {
            this.method_8259(player);
            this.method_5977(true);
            this.method_5942().method_6340();
            player.method_17355(new NamedScreenHandlerFactory() {
               public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity playerx) {
                  return new MerchantScreenHandler(syncId, inventory, GhostMasterEntity.this);
               }

               public Text method_5476() {
                  return GhostMasterEntity.this.method_5476();
               }
            });
            if (player instanceof ServerPlayerEntity serverPlayer) {
               int syncId = serverPlayer.field_7512 != null ? serverPlayer.field_7512.field_7763 : 0;
               serverPlayer.field_13987
                  .method_14364(new SetTradeOffersS2CPacket(syncId, this.tradeOffers, 0, this.method_19269(), this.method_19270(), this.method_20708()));
            }
         }
      }
   }

   public TradeOfferList method_8264() {
      if (!this.tradeOffersInitialized) {
         this.initTradeOffers();
      }

      return this.tradeOffers;
   }

   public void method_8261(TradeOfferList offers) {
      if (offers != null) {
         this.tradeOffers = offers;
         this.tradeOffersInitialized = true;
      }
   }

   public void method_8259(PlayerEntity customer) {
      if (customer == null && this.customer != null) {
         this.method_5977(false);
      }

      this.customer = customer;
   }

   public PlayerEntity method_8257() {
      return this.customer;
   }

   public void method_8262(TradeOffer offer) {
      offer.method_8244();
   }

   public void method_8258(ItemStack stack) {
   }

   public int method_19269() {
      return this.tradeExperience;
   }

   public void method_19271(int experience) {
      this.tradeExperience = experience;
   }

   public boolean method_19270() {
      return false;
   }

   public SoundEvent method_18010() {
      return null;
   }

   public boolean method_38069() {
      return this.method_37908().field_9236;
   }

   public boolean method_20708() {
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
