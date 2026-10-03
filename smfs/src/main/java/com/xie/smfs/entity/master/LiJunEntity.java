package com.xie.smfs.entity.master;

import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;

public class LiJunEntity extends GhostMasterEntity {
   public LiJunEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.setGhostDomainLevel(2);
      this.setGhostDomainRadius(32.0);
      this.setSpiritualStrength(5000);
      this.setSpiritualDamage(80);
      this.setSpiritualResistance(155);
      this.setRecoveryFactor(0.08F);
      this.shouldFleeFromGhosts = false;
      this.shouldAttackPlayers = false;
      this.shouldProtectPlayers = true;
      this.shouldAttackGhostsNearPlayers = true;
      this.method_5665(Text.method_43470("§6[鬼火]李军"));
      this.method_5880(true);
      this.faction = PlayerFaction.HEADQUARTERS;
   }

   public static Builder createLiJunAttributes() {
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
      return "李军";
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608()) {
         if (this.method_5968() != null && this.method_5968() instanceof PlayerEntity) {
            this.applyGreenGhostDomainToNearbyPlayers();
         }

         if (this.method_6032() <= this.method_6063() * 0.3 && this.field_6012 % 40 == 0) {
            this.activateFireSkill();
         }
      }
   }

   private void applyGreenGhostDomainToNearbyPlayers() {
      double radius = 16.0;
      this.method_37908()
         .method_8390(PlayerEntity.class, this.method_5829().method_1014(radius), player -> player != this.method_5968() && player instanceof PlayerEntity)
         .forEach(player -> {
            double distance = this.method_5739(player);
            if (distance <= radius) {
               player.method_6092(new StatusEffectInstance(ModEffects.GREEN_GHOST_DOMAIN_TARGET, 100, 0, false, false, false));
            }
         });
   }

   private void activateFireSkill() {
      World world = this.method_37908();
      BlockPos feetPos = this.method_24515();
      if (world.method_8320(feetPos).method_26215()) {
         world.method_8501(feetPos, Blocks.field_10036.method_9564());
      }

      double radius = 8.0;
      this.method_37908()
         .method_8390(LivingEntity.class, this.method_5829().method_1014(radius), entity -> entity != this && entity instanceof LivingEntity)
         .forEach(
            entity -> {
               double distance = this.method_5739(entity);
               if (distance <= radius) {
                  entity.method_5639(5);
                  if (world instanceof ServerWorld serverWorldx) {
                     serverWorldx.method_14199(
                        ParticleTypes.field_11240, entity.method_23317(), entity.method_23318() + 1.0, entity.method_23321(), 10, 0.5, 0.5, 0.5, 0.1
                     );
                  }
               }
            }
         );
      if (world instanceof ServerWorld serverWorld) {
         serverWorld.method_14199(ParticleTypes.field_11240, this.method_23317(), this.method_23318() + 1.0, this.method_23321(), 20, 1.0, 1.0, 1.0, 0.2);
      }
   }

   @Override
   protected void enableGhostDomain(PlayerEntity player, StatusEffect effect) {
      player.method_6092(new StatusEffectInstance(ModEffects.GREEN_GHOST_DOMAIN_TARGET, 20, this.getGhostDomainLevel() - 1, false, false, false));
   }

   @Override
   protected String getAttackDialogue() {
      String[] dialogues = new String[]{"大不了拼个厉鬼复苏。"};
      return dialogues[this.method_6051().method_43048(dialogues.length)];
   }

   @Override
   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         ItemStack ghostMoney3Input = new ItemStack(ModItems.GHOST_MONEY_3, 3);
         ItemStack goldIngotOutput2 = new ItemStack(Items.field_8695, 24);
         this.tradeOffers.add(new TradeOffer(ghostMoney3Input, goldIngotOutput2, 12, 5, 0.05F));
         ItemStack ghostAxeInput = new ItemStack(ModItems.GHOST_AXE, 1);
         ItemStack goldIngotOutput3 = new ItemStack(Items.field_8494, 4);
         this.tradeOffers.add(new TradeOffer(ghostAxeInput, goldIngotOutput3, 6, 8, 0.08F));
         this.tradeOffersInitialized = true;
      }
   }
}
