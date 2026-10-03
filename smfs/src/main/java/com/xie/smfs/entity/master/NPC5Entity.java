package com.xie.smfs.entity.master;

import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;

public class NPC5Entity extends GhostMasterEntity {
   public NPC5Entity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.setGhostDomainLevel(0);
      this.setGhostDomainRadius(16.0);
      this.setSpiritualStrength(800);
      this.setSpiritualDamage(20);
      this.setSpiritualResistance(20);
      this.setRecoveryFactor(0.04F);
      this.setCustomName(Text.literal("§6驭鬼者"));
      this.setCustomNameVisible(true);
      this.faction = PlayerFaction.FOLK_GHOST_MASTER;
   }

   public static Builder createNPC5Attributes() {
      return MobEntity.createMobAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 500.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
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
      return "驭鬼者";
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient() && this.getTarget() != null) {
      }
   }

   @Override
   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         ItemStack defiledIngotInput = new ItemStack(ModItems.DEFILED_INGOT, 1);
         ItemStack goldNuggetOutput = new ItemStack(Items.GOLD_BLOCK, 2);
         this.tradeOffers.add(new TradeOffer(defiledIngotInput, goldNuggetOutput, 16, 2, 0.03F));
         this.tradeOffersInitialized = true;
      }
   }
}
