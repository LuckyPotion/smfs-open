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
      this.method_5665(Text.method_43470("§6[鬼雾]冯全"));
      this.method_5880(true);
      this.faction = PlayerFaction.HEADQUARTERS;
   }

   public static Builder createFengQuanAttributes() {
      return GhostMasterEntity.createGhostMasterAttributes();
   }

   @Override
   protected void method_5959() {
      super.method_5959();
   }

   @Override
   public boolean method_5810() {
      return false;
   }

   @Override
   protected String getGhostMasterDisplayName() {
      return "冯全";
   }

   @Override
   protected void enableGhostDomain(PlayerEntity player, StatusEffect effect) {
      player.method_6092(new StatusEffectInstance(ModEffects.THICK_FOG_TARGET, 20, this.getGhostDomainLevel() - 1, false, false, false));
   }

   @Override
   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         ItemStack ghostMoney3Input = new ItemStack(ModItems.GHOST_MONEY_3, 7);
         ItemStack goldIngotOutput2 = new ItemStack(Items.field_8695, 21);
         this.tradeOffers.add(new TradeOffer(ghostMoney3Input, goldIngotOutput2, 12, 5, 0.05F));
         ItemStack ghostMirrorInput = new ItemStack(ModItems.GHOST_MIRROR, 1);
         ItemStack goldNuggetOutput = new ItemStack(Items.field_8494, 16);
         this.tradeOffers.add(new TradeOffer(ghostMirrorInput, goldNuggetOutput, 8, 5, 0.08F));
         this.tradeOffersInitialized = true;
      }
   }

   private void applyFogGhostDomainToNearbyPlayers() {
      double radius = 16.0;
      this.method_37908()
         .method_8390(PlayerEntity.class, this.method_5829().method_1014(radius), player -> player != this.method_5968() && player instanceof PlayerEntity)
         .forEach(player -> {
            double distance = this.method_5739(player);
            if (distance <= radius) {
               player.method_6092(new StatusEffectInstance(ModEffects.THICK_FOG_TARGET, 100, 0, false, false, false));
            }
         });
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236) {
         if (this.field_6012 % 20 == 0) {
            this.checkStationaryPlayers();
         }
      }
   }

   private void checkStationaryPlayers() {
      if (!(this.method_6032() > this.method_6063() * 0.5F)) {
         if (this.method_37908() instanceof ServerWorld) {
            double range = 20.0;

            for (PlayerEntity player : this.method_37908().method_18456()) {
               if (!player.method_7325()
                  && !player.method_7337()
                  && !RedGhostCandleItem.isHoldingCandle(player)
                  && !player.method_6059(ModEffects.SPIRIT_IMMUNITY)
                  && !(this.method_5858(player) > range * range)) {
                  Vec3d currentPos = player.method_19538();
                  Vec3d prevPos = new Vec3d(player.field_6014, player.field_6036, player.field_5969);
                  if (currentPos.method_1025(prevPos) < 0.01) {
                     this.stationaryPlayerTimer += 20;
                     if (this.stationaryPlayerTimer >= 60) {
                        this.spawnGraveMoundAt(player.method_24515());
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
      if (this.method_37908() instanceof ServerWorld serverWorld) {
         BlockPos var5 = pos;

         while (var5.method_10264() > serverWorld.method_31607() && serverWorld.method_8320(var5).method_26215()) {
            var5 = var5.method_10074();
         }

         BlockPos placePos = var5.method_10084();
         if (serverWorld.method_8320(placePos).method_26215()) {
            serverWorld.method_8501(placePos, ModBlocks.GRAVE_MOUND.method_9564());
         }
      }
   }
}
