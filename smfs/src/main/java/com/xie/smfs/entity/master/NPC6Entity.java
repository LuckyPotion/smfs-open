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

public class NPC6Entity extends GhostMasterEntity {
   public NPC6Entity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.setGhostDomainLevel(0);
      this.setGhostDomainRadius(16.0);
      this.setSpiritualStrength(800);
      this.setSpiritualDamage(20);
      this.setSpiritualResistance(20);
      this.setRecoveryFactor(0.04F);
      this.method_5665(Text.method_43470("§6驭鬼者"));
      this.method_5880(true);
      this.faction = PlayerFaction.FOLK_GHOST_MASTER;
   }

   public static Builder createNPC6Attributes() {
      return MobEntity.method_26828()
         .method_26868(EntityAttributes.field_23716, 500.0)
         .method_26868(EntityAttributes.field_23719, 0.3)
         .method_26868(EntityAttributes.field_23721, 10.0)
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
      return "驭鬼者";
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608() && this.method_5968() != null) {
      }
   }

   @Override
   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         ItemStack ghostBuddhaBeadsInput = new ItemStack(ModItems.GHOST_BUDDHA_BEADS, 1);
         ItemStack goldIngotOutput = new ItemStack(Items.field_8494, 6);
         this.tradeOffers.add(new TradeOffer(ghostBuddhaBeadsInput, goldIngotOutput, 6, 8, 0.1F));
         this.tradeOffersInitialized = true;
      }
   }
}
