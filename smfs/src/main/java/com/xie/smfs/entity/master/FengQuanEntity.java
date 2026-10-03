package com.xie.smfs.entity.master;

import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.registry.ModBlocks;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;

public class FengQuanEntity extends GhostMasterEntity {
   private int stationaryPlayerTimer = 0;
   private static final int STATIONARY_THRESHOLD = 60;

   public FengQuanEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.setGhostDomainLevel(3);
      this.setGhostDomainRadius(32.0);
      this.setSpiritualStrength(4000);
      this.setSpiritualDamage(80);
      this.setSpiritualResistance(155);
      this.setRecoveryFactor(0.08F);
      this.shouldFleeFromGhosts = false;
      this.shouldAttackPlayers = false;
      this.shouldProtectPlayers = true;
      this.shouldAttackGhostsNearPlayers = true;
      this.setCustomName(Text.literal("§6[鬼雾]冯全"));
      this.setCustomNameVisible(true);
      this.faction = PlayerFaction.HEADQUARTERS;
   }

   public static Builder createFengQuanAttributes() {
      return GhostMasterEntity.createGhostMasterAttributes();
   }

   @Override
   protected void initGoals() {
      super.initGoals();
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   protected String getGhostMasterDisplayName() {
      return "冯全";
   }

   @Override
   protected void enableGhostDomain(PlayerEntity player, StatusEffect effect) {
      player.addStatusEffect(new StatusEffectInstance(ModEffects.THICK_FOG_TARGET, 20, this.getGhostDomainLevel() - 1, false, false, false));
   }

   @Override
   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         ItemStack ghostMoney3Input = new ItemStack(ModItems.GHOST_MONEY_3, 7);
         ItemStack goldIngotOutput2 = new ItemStack(Items.GOLD_INGOT, 21);
         this.tradeOffers.add(new TradeOffer(ghostMoney3Input, goldIngotOutput2, 12, 5, 0.05F));
         ItemStack ghostMirrorInput = new ItemStack(ModItems.GHOST_MIRROR, 1);
         ItemStack goldNuggetOutput = new ItemStack(Items.GOLD_BLOCK, 16);
         this.tradeOffers.add(new TradeOffer(ghostMirrorInput, goldNuggetOutput, 8, 5, 0.08F));
         this.tradeOffersInitialized = true;
      }
   }

   private void applyFogGhostDomainToNearbyPlayers() {
      double radius = 16.0;
      this.getWorld()
         .getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(radius), player -> player != this.getTarget() && player instanceof PlayerEntity)
         .forEach(player -> {
            double distance = this.distanceTo(player);
            if (distance <= radius) {
               player.addStatusEffect(new StatusEffectInstance(ModEffects.THICK_FOG_TARGET, 100, 0, false, false, false));
            }
         });
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient) {
         if (this.age % 20 == 0) {
            this.checkStationaryPlayers();
         }
      }
   }

   private void checkStationaryPlayers() {
      if (!(this.getHealth() > this.getMaxHealth() * 0.5F)) {
         if (this.getWorld() instanceof ServerWorld) {
            double range = 20.0;

            for (PlayerEntity player : this.getWorld().getPlayers()) {
               if (!player.isSpectator()
                  && !player.isCreative()
                  && !RedGhostCandleItem.isHoldingCandle(player)
                  && !player.hasStatusEffect(ModEffects.SPIRIT_IMMUNITY)
                  && !(this.squaredDistanceTo(player) > range * range)) {
                  Vec3d currentPos = player.getPos();
                  Vec3d prevPos = new Vec3d(player.prevX, player.prevY, player.prevZ);
                  if (currentPos.squaredDistanceTo(prevPos) < 0.01) {
                     this.stationaryPlayerTimer += 20;
                     if (this.stationaryPlayerTimer >= 60) {
                        this.spawnGraveMoundAt(player.getBlockPos());
                        this.stationaryPlayerTimer = 0;
                     }
                  } else {
                     this.stationaryPlayerTimer = 0;
                  }
               }
            }
         }
      }
   }

   private void spawnGraveMoundAt(BlockPos pos) {
      if (this.getWorld() instanceof ServerWorld serverWorld) {
         BlockPos var5 = pos;

         while (var5.getY() > serverWorld.getBottomY() && serverWorld.getBlockState(var5).isAir()) {
            var5 = var5.down();
         }

         BlockPos placePos = var5.up();
         if (serverWorld.getBlockState(placePos).isAir()) {
            serverWorld.setBlockState(placePos, ModBlocks.GRAVE_MOUND.getDefaultState());
         }
      }
   }
}
