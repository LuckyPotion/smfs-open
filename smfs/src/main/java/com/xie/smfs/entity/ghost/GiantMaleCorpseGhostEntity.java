package com.xie.smfs.entity.ghost;

import com.xie.smfs.block.FootprintBlock;
import com.xie.smfs.block.entity.FootprintBlockEntity;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.CaesarHotelDiaryItem;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.manager.GoldBlockProtectionManager;
import com.xie.smfs.network.packets.common.c2s.SpectateModePacket;
import com.xie.smfs.registry.ModBlocks;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import com.xie.smfs.registry.ModSounds;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.Animation.LoopType;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GiantMaleCorpseGhostEntity extends GhostEntity implements GeoAnimatable {
   private static final TrackedData<Boolean> IS_ATTACKING_FROM_FOOTPRINT = DataTracker.registerData(
      GiantMaleCorpseGhostEntity.class, TrackedDataHandlerRegistry.BOOLEAN
   );
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 50.0;
   private static final char TERROR_LEVEL = 'A';
   private static final int FOOTPRINT_CHECK_RANGE = 30;
   private static final int FOOTPRINT_DURATION_TICKS = 3600;
   private static final int FOOTPRINT_COOLDOWN_TICKS = 10;
   private static final double FOOTPRINT_DETECTION_RANGE = 1.5;
   private static final int ATTACK_DELAY_TICKS = 20;
   private static final double ANTI_FLIGHT_RADIUS = 50.0;
   private static final int MESSAGE_COOLDOWN_TICKS = 20;
   private int messageCooldownTimer = 0;
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private int footprintSpawnTimer = 0;
   private int attackDelayTimer = 0;
   private PlayerEntity pendingAttackTarget = null;

   public static Builder createLivingAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 120000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0)
         .add(EntityAttributes.GENERIC_ATTACK_SPEED, 0.3)
         .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.0)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
   }

   public GiantMaleCorpseGhostEntity(EntityType<GiantMaleCorpseGhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 50.0, 'A', 9500, 1200, 200, 0.6F);
      this.ghostLevel = 5;
      this.setGhostDomainActualLevel(5);
      this.setNoGravity(false);
      this.setSuppressionSlotCost(4);
   }

   @Override
   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(IS_ATTACKING_FROM_FOOTPRINT, false);
   }

   @Override
   public boolean isResentmentSystemDisabled() {
      return true;
   }

   @Override
   public boolean isPlayerProtected(PlayerEntity player) {
      return CoffinEffectManager.isPlayerInGoldCoffin(player)
         || GoldBlockProtectionManager.isPlayerInGoldBlockShelter(player)
         || SpectateModePacket.isInSpectatorMode(player.getUuid())
         || player.hasStatusEffect(ModEffects.SPIRIT_IMMUNITY);
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      if (this.getWorld().isClient()) {
         return ActionResult.SUCCESS;
      }

      if (!player.getStackInHand(hand).isEmpty()) {
         return ActionResult.PASS;
      }

      for (ItemStack stack : player.getInventory().main) {
         if (CaesarHotelDiaryItem.isDiary(stack)) {
            player.sendMessage(Text.literal("§7似乎没有什么其他东西了。"), true);
            return ActionResult.SUCCESS;
         }
      }

      ItemStack diary = CaesarHotelDiaryItem.createDiary();
      if (player.getInventory().insertStack(diary)) {
         player.sendMessage(Text.literal("§6你找到了一本破旧的日记：§r§e凯撒大酒店管理员日记"), false);
      } else {
         player.sendMessage(Text.literal("§c背包已满，无法获得日记！"), false);
      }

      return ActionResult.SUCCESS;
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient()) {
         this.spawnFootprintsForNearbyPlayers();
         this.checkFootprintAttack();
         this.processDelayedAttack();
         if (!this.isDeadlocked() && !this.isSuppressed()) {
            this.checkAntiFlightEffect();
         }
      }
   }

   private void processDelayedAttack() {
      if (this.attackDelayTimer > 0) {
         this.attackDelayTimer--;
         if (this.attackDelayTimer <= 0 && this.pendingAttackTarget != null) {
            if (RedGhostCandleItem.isHoldingCandle(this.pendingAttackTarget)) {
               this.breakCandle(this.pendingAttackTarget);
            } else {
               this.dealSpiritualDamage(this.pendingAttackTarget);
            }

            this.pendingAttackTarget = null;
            this.dataTracker.set(IS_ATTACKING_FROM_FOOTPRINT, false);
         }
      }
   }

   private void spawnFootprintsForNearbyPlayers() {
      if (++this.footprintSpawnTimer >= 10) {
         this.footprintSpawnTimer = 0;
         Box detectionBox = new Box(this.getPos().add(-30.0, -2.0, -30.0), this.getPos().add(30.0, 3.0, 30.0));

         for (PlayerEntity player : this.getWorld()
            .getEntitiesByClass(PlayerEntity.class, detectionBox, playerx -> !playerx.isSpectator() && !playerx.isCreative())) {
            BlockPos playerFeetPos = player.getBlockPos().down();
            BlockState playerFeetState = this.getWorld().getBlockState(playerFeetPos);
            if (playerFeetState.isOf(ModBlocks.UNBREAKABLE_RED_WOOL)) {
               double offsetX = (new Random().nextDouble() - 0.5) * 0.8;
               double offsetZ = (new Random().nextDouble() - 0.5) * 0.8;
               BlockPos footPos = BlockPos.ofFloored(player.getPos().add(offsetX, 0.0, offsetZ));
               if (this.canPlaceFootprintAt(footPos, this.getWorld())) {
                  this.getWorld().setBlockState(footPos, ModBlocks.FOOTPRINT.getDefaultState());
                  if (this.getWorld().getBlockEntity(footPos) instanceof FootprintBlockEntity footprintBE) {
                     footprintBE.setEntityUUID(player.getUuidAsString());
                     footprintBE.setDecayTime(3600);
                  }
               }
            }
         }
      }
   }

   private boolean canPlaceFootprintAt(BlockPos pos, World world) {
      BlockState currentState = world.getBlockState(pos);
      boolean canReplace = currentState.isAir() || currentState.getBlock() instanceof FootprintBlock || this.isReplaceableBlock(currentState);
      BlockPos belowPos = pos.down();
      BlockState belowState = world.getBlockState(belowPos);
      boolean hasSupport = !belowState.isAir() && belowState.isSolidBlock(world, belowPos);
      return canReplace && hasSupport;
   }

   private boolean isReplaceableBlock(BlockState state) {
      String blockName = state.getBlock().getTranslationKey().toLowerCase();
      return blockName.contains("grass")
         || blockName.contains("snow")
         || blockName.contains("flower")
         || blockName.contains("plant")
         || blockName.contains("vine")
         || blockName.contains("fern")
         || blockName.contains("mushroom")
         || blockName.contains("carpet")
         || blockName.contains("reeds")
         || blockName.contains("sugar_cane");
   }

   private void checkFootprintAttack() {
      if (!(Boolean)this.dataTracker.get(IS_ATTACKING_FROM_FOOTPRINT)) {
         BlockPos feetPos = BlockPos.ofFloored(this.getPos());
         BlockState state = this.getWorld().getBlockState(feetPos);
         if (state.getBlock() instanceof FootprintBlock
            && this.getWorld().getBlockEntity(feetPos) instanceof FootprintBlockEntity footprintBE
            && footprintBE.hasValidEntityInfo()
            && footprintBE.getTargetEntity(this.getWorld()) instanceof PlayerEntity player
            && !player.isDead()) {
            this.executeFootprintAttack(player);
            this.getWorld().removeBlock(feetPos, false);
         }
      }
   }

   private void executeFootprintAttack(PlayerEntity target) {
      this.dataTracker.set(IS_ATTACKING_FROM_FOOTPRINT, true);
      this.playAttackSound();
      this.pendingAttackTarget = target;
      this.attackDelayTimer = 20;
   }

   private void playAttackSound() {
      this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.SWING_ATTACK_SOUND, SoundCategory.HOSTILE, 1.0F, 1.0F);
   }

   private void dealSpiritualDamage(PlayerEntity target) {
      float damage = this.getSpiritualDamage();
      PlayerEvents.handleSpiritDamage(target, damage, damage, ModDamageSources.ghost(this.getWorld()));
   }

   private void breakCandle(PlayerEntity target) {
      ItemStack mainHand = target.getMainHandStack();
      ItemStack offHand = target.getOffHandStack();
      if (mainHand.isOf(ModItems.RED_GHOST_CANDLE)) {
         mainHand.decrement(1);
      } else if (offHand.isOf(ModItems.RED_GHOST_CANDLE)) {
         offHand.decrement(1);
      }

      this.playCandleBreakSound();
   }

   private void playCandleBreakSound() {
      this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.HOSTILE, 1.0F, 1.0F);
   }

   @Override
   public boolean tryAttack(Entity target) {
      if (!this.isDeadlocked() && !this.isSuppressed()) {
         this.playAttackSound();
         if (target instanceof PlayerEntity player && RedGhostCandleItem.isHoldingCandle(player)) {
            this.breakCandle(player);
            return true;
         } else {
            return super.tryAttack(target);
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean damage(DamageSource source, float amount) {
      boolean result = super.damage(source, amount);
      if (!this.isDeadlocked() && !this.isChasing() && this.getAttackCooldown() <= 0 && source.getAttacker() instanceof PlayerEntity player) {
         this.setTarget(player);
      }

      return result;
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   @Override
   public double getTick(Object object) {
      return this.age;
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "controller", 2, this::predicate));
   }

   private <E extends GeoAnimatable> PlayState predicate(AnimationState<E> event) {
      if (!(Boolean)this.dataTracker.get(IS_ATTACKING_FROM_FOOTPRINT) && !this.handSwinging) {
         if (this.getVelocity().horizontalLengthSquared() > 0.001) {
            event.getController().setAnimation(RawAnimation.begin().then("walk", LoopType.LOOP));
         } else {
            event.getController().setAnimation(RawAnimation.begin().then("idle", LoopType.LOOP));
         }
      } else {
         event.getController().setAnimation(RawAnimation.begin().then("attack", LoopType.PLAY_ONCE));
      }

      return PlayState.CONTINUE;
   }

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("footprintSpawnTimer", this.footprintSpawnTimer);
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("footprintSpawnTimer")) {
         this.footprintSpawnTimer = nbt.getInt("footprintSpawnTimer");
      }
   }

   private void checkAntiFlightEffect() {
      if (this.messageCooldownTimer > 0) {
         this.messageCooldownTimer--;
      }

      Box detectionBox = new Box(this.getPos().add(-50.0, -50.0, -50.0), this.getPos().add(50.0, 50.0, 50.0));
      List<PlayerEntity> nearbyPlayers = this.getWorld()
         .getEntitiesByClass(PlayerEntity.class, detectionBox, player -> !player.isSpectator() && !player.isCreative());
      boolean shouldShowMessage = this.messageCooldownTimer <= 0;

      for (PlayerEntity player : nearbyPlayers) {
         if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.sendAbilitiesUpdate();
            if (shouldShowMessage) {
               player.sendMessage(Text.literal("§c周围有极其强大的存在，鬼域被影响，无法继续保持飞行。"), false);
            }
         }
      }

      if (shouldShowMessage && !nearbyPlayers.isEmpty()) {
         boolean anyPlayerFlying = nearbyPlayers.stream().anyMatch(p -> p.getAbilities().flying);
         if (!anyPlayerFlying) {
            boolean anyWasFlying = nearbyPlayers.stream().anyMatch(p -> !p.isOnGround() && p.getVelocity().y < 0.0);
            if (anyWasFlying) {
               this.messageCooldownTimer = 20;
            }
         }
      }
   }
}
