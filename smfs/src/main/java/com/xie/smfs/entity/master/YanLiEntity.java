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
      this.setCustomName(Text.literal("§6严力"));
      this.setCustomNameVisible(true);
      this.faction = PlayerFaction.FOLK_GHOST_MASTER;
   }

   public static Builder createYanLiAttributes() {
      return MobEntity.createMobAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 850.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 18.0)
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
      return "严力";
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient() && this.getTarget() != null) {
      }
   }

   @Override
   public boolean tryAttack(Entity target) {
      boolean attacked = super.tryAttack(target);
      if (!this.getWorld().isClient() && attacked && target instanceof LivingEntity livingTarget) {
         StatusEffectInstance silenceEffect = new StatusEffectInstance(ModEffects.SILENCE, 200, 0, false, true, true);
         livingTarget.addStatusEffect(silenceEffect);
         if (this.getWorld() instanceof ServerWorld serverWorld) {
            Vec3d pos = target.getPos();

            for (int i = 0; i < 15; i++) {
               double offsetX = (this.getRandom().nextDouble() - 0.5) * 2.0;
               double offsetY = this.getRandom().nextDouble() * 2.0;
               double offsetZ = (this.getRandom().nextDouble() - 0.5) * 2.0;
               serverWorld.spawnParticles(ParticleTypes.SMOKE, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 2, 0.1, 0.1, 0.1, 0.2);
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
         ItemStack goldIngotOutput2 = new ItemStack(Items.GOLD_INGOT, 78);
         this.tradeOffers.add(new TradeOffer(ghostMoney3Input, goldIngotOutput2, 12, 5, 0.05F));
         ItemStack eerieYellowPaperInput = new ItemStack(ModItems.EERIE_YELLOW_PAPER, 1);
         ItemStack goldNuggetOutput = new ItemStack(Items.GOLD_BLOCK, 1);
         this.tradeOffers.add(new TradeOffer(eerieYellowPaperInput, goldNuggetOutput, 64, 1, 0.01F));
         this.tradeOffersInitialized = true;
      }
   }
}
