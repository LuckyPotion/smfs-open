package com.xie.smfs.entity.master;

import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;

public class CaoYangEntity extends GhostMasterEntity {
   public CaoYangEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.setGhostDomainLevel(4);
      this.setGhostDomainRadius(32.0);
      this.setSpiritualStrength(5000);
      this.setSpiritualDamage(80);
      this.setSpiritualResistance(155);
      this.setRecoveryFactor(0.08F);
      this.shouldFleeFromGhosts = false;
      this.shouldAttackPlayers = false;
      this.shouldProtectPlayers = true;
      this.shouldAttackGhostsNearPlayers = true;
      this.method_5665(Text.method_43470("§6[鬼风]曹洋"));
      this.method_5880(true);
      this.faction = PlayerFaction.HEADQUARTERS;
   }

   public static Builder createCaoYangAttributes() {
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
      return "曹洋";
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608() && this.method_5968() != null && this.method_5968() instanceof PlayerEntity) {
         this.applyCyanGhostDomainToNearbyPlayers();
         if (this.field_6012 % 60 == 0) {
            this.activateSpecialSkill();
         }
      }
   }

   private void applyCyanGhostDomainToNearbyPlayers() {
      double radius = 16.0;
      this.method_37908()
         .method_8390(PlayerEntity.class, this.method_5829().method_1014(radius), player -> player != this.method_5968() && player instanceof PlayerEntity)
         .forEach(player -> {
            double distance = this.method_5739(player);
            if (distance <= radius) {
               player.method_6092(new StatusEffectInstance(ModEffects.CYAN_GHOST_DOMAIN_TARGET, 100, 0, false, false, false));
            }
         });
   }

   @Override
   protected void enableGhostDomain(PlayerEntity player, StatusEffect effect) {
      player.method_6092(new StatusEffectInstance(ModEffects.CYAN_GHOST_DOMAIN_TARGET, 100, this.getGhostDomainLevel() - 1, false, false, false));
   }

   private void activateSpecialSkill() {
      PlayerEntity target = (PlayerEntity)this.method_5968();
      if (target != null) {
         target.method_6092(new StatusEffectInstance(StatusEffects.field_5902, 200, 0, false, false, true));

         for (int i = 0; i < 3; i++) {
            this.spawnShulkerBullet(target);
         }
      }
   }

   private void spawnShulkerBullet(PlayerEntity target) {
      double offsetX = (this.field_5974.method_43058() - 0.5) * 4.0;
      double offsetY = this.field_5974.method_43058() * 2.0;
      double offsetZ = (this.field_5974.method_43058() - 0.5) * 4.0;
      ShulkerBulletEntity shulkerBullet = new ShulkerBulletEntity(this.method_37908(), this, target, Axis.field_11048);
      shulkerBullet.method_5814(this.method_23317() + offsetX, this.method_23318() + offsetY, this.method_23321() + offsetZ);
      this.method_37908().method_8649(shulkerBullet);
   }

   @Override
   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         ItemStack ghostMoney3Input = new ItemStack(ModItems.GHOST_MONEY_3, 2);
         ItemStack goldIngotOutput2 = new ItemStack(Items.field_8695, 16);
         this.tradeOffers.add(new TradeOffer(ghostMoney3Input, goldIngotOutput2, 12, 5, 0.05F));
         ItemStack ghostScissorsInput = new ItemStack(ModItems.GHOST_SCISSORS, 1);
         ItemStack goldIngotOutput5 = new ItemStack(Items.field_8494, 12);
         this.tradeOffers.add(new TradeOffer(ghostScissorsInput, goldIngotOutput5, 8, 5, 0.05F));
         this.tradeOffersInitialized = true;
      }
   }
}
