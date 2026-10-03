package com.xie.smfs.entity.master;

import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;

public class YanLiEntity extends GhostMasterEntity {
   public YanLiEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.setGhostDomainLevel(0);
      this.setGhostDomainRadius(12.0);
      this.setSpiritualStrength(1600);
      this.setSpiritualDamage(15);
      this.setSpiritualResistance(25);
      this.setRecoveryFactor(0.05F);
      this.shouldFleeFromGhosts = false;
      this.shouldAttackPlayers = false;
      this.shouldProtectPlayers = true;
      this.shouldAttackGhostsNearPlayers = true;
      this.method_5665(Text.method_43470("§6严力"));
      this.method_5880(true);
      this.faction = PlayerFaction.FOLK_GHOST_MASTER;
   }

   public static Builder createYanLiAttributes() {
      return MobEntity.method_26828()
         .method_26868(EntityAttributes.field_23716, 850.0)
         .method_26868(EntityAttributes.field_23719, 0.3)
         .method_26868(EntityAttributes.field_23721, 18.0)
         .method_26868(EntityAttributes.field_23717, 16.0);
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
      return "严力";
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608() && this.method_5968() != null) {
      }
   }

   @Override
   public boolean method_6121(Entity target) {
      boolean attacked = super.method_6121(target);
      if (!this.method_37908().method_8608() && attacked && target instanceof LivingEntity livingTarget) {
         StatusEffectInstance silenceEffect = new StatusEffectInstance(ModEffects.SILENCE, 200, 0, false, true, true);
         livingTarget.method_6092(silenceEffect);
         if (this.method_37908() instanceof ServerWorld serverWorld) {
            Vec3d pos = target.method_19538();

            for (int i = 0; i < 15; i++) {
               double offsetX = (this.method_6051().method_43058() - 0.5) * 2.0;
               double offsetY = this.method_6051().method_43058() * 2.0;
               double offsetZ = (this.method_6051().method_43058() - 0.5) * 2.0;
               serverWorld.method_14199(
                  ParticleTypes.field_11251, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 2, 0.1, 0.1, 0.1, 0.2
               );
            }
         }
      }

      return attacked;
   }

   @Override
   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         ItemStack ghostMoney3Input = new ItemStack(ModItems.GHOST_MONEY_7, 3);
         ItemStack goldIngotOutput2 = new ItemStack(Items.field_8695, 78);
         this.tradeOffers.add(new TradeOffer(ghostMoney3Input, goldIngotOutput2, 12, 5, 0.05F));
         ItemStack eerieYellowPaperInput = new ItemStack(ModItems.EERIE_YELLOW_PAPER, 1);
         ItemStack goldNuggetOutput = new ItemStack(Items.field_8494, 1);
         this.tradeOffers.add(new TradeOffer(eerieYellowPaperInput, goldNuggetOutput, 64, 1, 0.01F));
         this.tradeOffersInitialized = true;
      }
   }
}
