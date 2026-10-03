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
      this.setCustomName(Text.literal("§6[鬼风]曹洋"));
      this.setCustomNameVisible(true);
      this.faction = PlayerFaction.HEADQUARTERS;
   }

   public static Builder createCaoYangAttributes() {
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
      return "曹洋";
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient() && this.getTarget() != null && this.getTarget() instanceof PlayerEntity) {
         this.applyCyanGhostDomainToNearbyPlayers();
         if (this.age % 60 == 0) {
            this.activateSpecialSkill();
         }
      }
   }

   private void applyCyanGhostDomainToNearbyPlayers() {
      double radius = 16.0;
      this.getWorld()
         .getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(radius), player -> player != this.getTarget() && player instanceof PlayerEntity)
         .forEach(player -> {
            double distance = this.distanceTo(player);
            if (distance <= radius) {
               player.addStatusEffect(new StatusEffectInstance(ModEffects.CYAN_GHOST_DOMAIN_TARGET, 100, 0, false, false, false));
            }
         });
   }

   @Override
   protected void enableGhostDomain(PlayerEntity player, StatusEffect effect) {
      player.addStatusEffect(new StatusEffectInstance(ModEffects.CYAN_GHOST_DOMAIN_TARGET, 100, this.getGhostDomainLevel() - 1, false, false, false));
   }

   private void activateSpecialSkill() {
      PlayerEntity target = (PlayerEntity)this.getTarget();
      if (target != null) {
         target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 200, 0, false, false, true));

         for (int i = 0; i < 3; i++) {
            this.spawnShulkerBullet(target);
         }
      }
   }

   private void spawnShulkerBullet(PlayerEntity target) {
      double offsetX = (this.random.nextDouble() - 0.5) * 4.0;
      double offsetY = this.random.nextDouble() * 2.0;
      double offsetZ = (this.random.nextDouble() - 0.5) * 4.0;
      ShulkerBulletEntity shulkerBullet = new ShulkerBulletEntity(this.getWorld(), this, target, Axis.X);
      shulkerBullet.setPosition(this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ);
      this.getWorld().spawnEntity(shulkerBullet);
   }

   @Override
   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         ItemStack ghostMoney3Input = new ItemStack(ModItems.GHOST_MONEY_3, 2);
         ItemStack goldIngotOutput2 = new ItemStack(Items.GOLD_INGOT, 16);
         this.tradeOffers.add(new TradeOffer(ghostMoney3Input, goldIngotOutput2, 12, 5, 0.05F));
         ItemStack ghostScissorsInput = new ItemStack(ModItems.GHOST_SCISSORS, 1);
         ItemStack goldIngotOutput5 = new ItemStack(Items.GOLD_BLOCK, 12);
         this.tradeOffers.add(new TradeOffer(ghostScissorsInput, goldIngotOutput5, 8, 5, 0.05F));
         this.tradeOffersInitialized = true;
      }
   }
}
