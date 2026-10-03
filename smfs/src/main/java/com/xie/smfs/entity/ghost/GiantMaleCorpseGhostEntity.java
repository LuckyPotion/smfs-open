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
   private static final TrackedData<Boolean> IS_ATTACKING_FROM_FOOTPRINT = DataTracker.method_12791(
      GiantMaleCorpseGhostEntity.class, TrackedDataHandlerRegistry.field_13323
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
      return LivingEntity.method_26827()
         .method_26868(EntityAttributes.field_23716, 120000.0)
         .method_26868(EntityAttributes.field_23719, 0.2)
         .method_26868(EntityAttributes.field_23721, 15.0)
         .method_26868(EntityAttributes.field_23723, 0.3)
         .method_26868(EntityAttributes.field_23722, 0.0)
         .method_26868(EntityAttributes.field_23717, 32.0);
   }

   public GiantMaleCorpseGhostEntity(EntityType<GiantMaleCorpseGhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 50.0, 'A', 9500, 1200, 200, 0.6F);
      this.ghostLevel = 5;
      this.setGhostDomainActualLevel(5);
      this.method_5875(false);
      this.setSuppressionSlotCost(4);
   }

   @Override
   protected void method_5693() {
      super.method_5693();
      this.field_6011.method_12784(IS_ATTACKING_FROM_FOOTPRINT, false);
   }

   @Override
   public boolean isResentmentSystemDisabled() {
      return true;
   }

   @Override
   public boolean isPlayerProtected(PlayerEntity player) {
      return CoffinEffectManager.isPlayerInGoldCoffin(player)
         || GoldBlockProtectionManager.isPlayerInGoldBlockShelter(player)
         || SpectateModePacket.isInSpectatorMode(player.method_5667())
         || player.method_6059(ModEffects.SPIRIT_IMMUNITY);
   }

   public ActionResult method_5992(PlayerEntity player, Hand hand) {
      if (this.method_37908().method_8608()) {
         return ActionResult.field_5812;
      }

      if (!player.method_5998(hand).method_7960()) {
         return ActionResult.field_5811;
      }

      for (ItemStack stack : player.method_31548().field_7547) {
         if (CaesarHotelDiaryItem.isDiary(stack)) {
            player.method_7353(Text.method_43470("§7似乎没有什么其他东西了。"), true);
            return ActionResult.field_5812;
         }
      }

      ItemStack diary = CaesarHotelDiaryItem.createDiary();
      if (player.method_31548().method_7394(diary)) {
         player.method_7353(Text.method_43470("§6你找到了一本破旧的日记：§r§e凯撒大酒店管理员日记"), false);
      } else {
         player.method_7353(Text.method_43470("§c背包已满，无法获得日记！"), false);
      }

      return ActionResult.field_5812;
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608()) {
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
            this.field_6011.method_12778(IS_ATTACKING_FROM_FOOTPRINT, false);
         }
      }
   }

   private void spawnFootprintsForNearbyPlayers() {
      if (++this.footprintSpawnTimer >= 10) {
         this.footprintSpawnTimer = 0;
         Box detectionBox = new Box(this.method_19538().method_1031(-30.0, -2.0, -30.0), this.method_19538().method_1031(30.0, 3.0, 30.0));

         for (PlayerEntity player : this.method_37908()
            .method_8390(PlayerEntity.class, detectionBox, playerx -> !playerx.method_7325() && !playerx.method_7337())) {
            BlockPos playerFeetPos = player.method_24515().method_10074();
            BlockState playerFeetState = this.method_37908().method_8320(playerFeetPos);
            if (playerFeetState.method_27852(ModBlocks.UNBREAKABLE_RED_WOOL)) {
               double offsetX = (new Random().nextDouble() - 0.5) * 0.8;
               double offsetZ = (new Random().nextDouble() - 0.5) * 0.8;
               BlockPos footPos = BlockPos.method_49638(player.method_19538().method_1031(offsetX, 0.0, offsetZ));
               if (this.canPlaceFootprintAt(footPos, this.method_37908())) {
                  this.method_37908().method_8501(footPos, ModBlocks.FOOTPRINT.method_9564());
                  if (this.method_37908().method_8321(footPos) instanceof FootprintBlockEntity footprintBE) {
                     footprintBE.setEntityUUID(player.method_5845());
                     footprintBE.setDecayTime(3600);
                  }
               }
            }
         }
      }
   }

   private boolean canPlaceFootprintAt(BlockPos pos, World world) {
      BlockState currentState = world.method_8320(pos);
      boolean canReplace = currentState.method_26215() || currentState.method_26204() instanceof FootprintBlock || this.isReplaceableBlock(currentState);
      BlockPos belowPos = pos.method_10074();
      BlockState belowState = world.method_8320(belowPos);
      boolean hasSupport = !belowState.method_26215() && belowState.method_26212(world, belowPos);
      return canReplace && hasSupport;
   }

   private boolean isReplaceableBlock(BlockState state) {
      String blockName = state.method_26204().method_9539().toLowerCase();
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
      if (!(Boolean)this.field_6011.method_12789(IS_ATTACKING_FROM_FOOTPRINT)) {
         BlockPos feetPos = BlockPos.method_49638(this.method_19538());
         BlockState state = this.method_37908().method_8320(feetPos);
         if (state.method_26204() instanceof FootprintBlock
            && this.method_37908().method_8321(feetPos) instanceof FootprintBlockEntity footprintBE
            && footprintBE.hasValidEntityInfo()
            && footprintBE.getTargetEntity(this.method_37908()) instanceof PlayerEntity player
            && !player.method_29504()) {
            this.executeFootprintAttack(player);
            this.method_37908().method_8650(feetPos, false);
         }
      }
   }

   private void executeFootprintAttack(PlayerEntity target) {
      this.field_6011.method_12778(IS_ATTACKING_FROM_FOOTPRINT, true);
      this.playAttackSound();
      this.pendingAttackTarget = target;
      this.attackDelayTimer = 20;
   }

   private void playAttackSound() {
      this.method_37908()
         .method_43128(null, this.method_23317(), this.method_23318(), this.method_23321(), ModSounds.SWING_ATTACK_SOUND, SoundCategory.field_15251, 1.0F, 1.0F);
   }

   private void dealSpiritualDamage(PlayerEntity target) {
      float damage = this.getSpiritualDamage();
      PlayerEvents.handleSpiritDamage(target, damage, damage, ModDamageSources.ghost(this.method_37908()));
   }

   private void breakCandle(PlayerEntity target) {
      ItemStack mainHand = target.method_6047();
      ItemStack offHand = target.method_6079();
      if (mainHand.method_31574(ModItems.RED_GHOST_CANDLE)) {
         mainHand.method_7934(1);
      } else if (offHand.method_31574(ModItems.RED_GHOST_CANDLE)) {
         offHand.method_7934(1);
      }

      this.playCandleBreakSound();
   }

   private void playCandleBreakSound() {
      this.method_37908()
         .method_43128(null, this.method_23317(), this.method_23318(), this.method_23321(), SoundEvents.field_15075, SoundCategory.field_15251, 1.0F, 1.0F);
   }

   @Override
   public boolean method_6121(Entity target) {
      if (!this.isDeadlocked() && !this.isSuppressed()) {
         this.playAttackSound();
         if (target instanceof PlayerEntity player && RedGhostCandleItem.isHoldingCandle(player)) {
            this.breakCandle(player);
            return true;
         } else {
            return super.method_6121(target);
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean method_5643(DamageSource source, float amount) {
      boolean result = super.method_5643(source, amount);
      if (!this.isDeadlocked() && !this.isChasing() && this.getAttackCooldown() <= 0 && source.method_5529() instanceof PlayerEntity player) {
         this.method_5980(player);
      }

      return result;
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   @Override
   public double getTick(Object object) {
      return this.field_6012;
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "controller", 2, this::predicate));
   }

   private <E extends GeoAnimatable> PlayState predicate(AnimationState<E> event) {
      if (!(Boolean)this.field_6011.method_12789(IS_ATTACKING_FROM_FOOTPRINT) && !this.field_6252) {
         if (this.method_18798().method_37268() > 0.001) {
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
   public void method_5652(NbtCompound nbt) {
      super.method_5652(nbt);
      nbt.method_10569("footprintSpawnTimer", this.footprintSpawnTimer);
   }

   @Override
   public void method_5749(NbtCompound nbt) {
      super.method_5749(nbt);
      if (nbt.method_10545("footprintSpawnTimer")) {
         this.footprintSpawnTimer = nbt.method_10550("footprintSpawnTimer");
      }
   }

   private void checkAntiFlightEffect() {
      if (this.messageCooldownTimer > 0) {
         this.messageCooldownTimer--;
      }

      Box detectionBox = new Box(this.method_19538().method_1031(-50.0, -50.0, -50.0), this.method_19538().method_1031(50.0, 50.0, 50.0));
      List<PlayerEntity> nearbyPlayers = this.method_37908()
         .method_8390(PlayerEntity.class, detectionBox, player -> !player.method_7325() && !player.method_7337());
      boolean shouldShowMessage = this.messageCooldownTimer <= 0;

      for (PlayerEntity player : nearbyPlayers) {
         if (player.method_31549().field_7479) {
            player.method_31549().field_7479 = false;
            player.method_7355();
            if (shouldShowMessage) {
               player.method_7353(Text.method_43470("§c周围有极其强大的存在，鬼域被影响，无法继续保持飞行。"), false);
            }
         }
      }

      if (shouldShowMessage && !nearbyPlayers.isEmpty()) {
         boolean anyPlayerFlying = nearbyPlayers.stream().anyMatch(p -> p.method_31549().field_7479);
         if (!anyPlayerFlying) {
            boolean anyWasFlying = nearbyPlayers.stream().anyMatch(p -> !p.method_24828() && p.method_18798().field_1351 < 0.0);
            if (anyWasFlying) {
               this.messageCooldownTimer = 20;
            }
         }
      }
   }
}
