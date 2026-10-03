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
      this.setCustomName(Text.literal("§6[鬼火]李军"));
      this.setCustomNameVisible(true);
      this.faction = PlayerFaction.HEADQUARTERS;
   }

   public static Builder createLiJunAttributes() {
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
      return "李军";
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient()) {
         if (this.getTarget() != null && this.getTarget() instanceof PlayerEntity) {
            this.applyGreenGhostDomainToNearbyPlayers();
         }

         if (this.getHealth() <= this.getMaxHealth() * 0.3 && this.age % 40 == 0) {
            this.activateFireSkill();
         }
      }
   }

   private void applyGreenGhostDomainToNearbyPlayers() {
      double radius = 16.0;
      this.getWorld()
         .getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(radius), player -> player != this.getTarget() && player instanceof PlayerEntity)
         .forEach(player -> {
            double distance = this.distanceTo(player);
            if (distance <= radius) {
               player.addStatusEffect(new StatusEffectInstance(ModEffects.GREEN_GHOST_DOMAIN_TARGET, 100, 0, false, false, false));
            }
         });
   }

   private void activateFireSkill() {
      World world = this.getWorld();
      BlockPos feetPos = this.getBlockPos();
      if (world.getBlockState(feetPos).isAir()) {
         world.setBlockState(feetPos, Blocks.FIRE.getDefaultState());
      }

      double radius = 8.0;
      this.getWorld()
         .getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(radius), entity -> entity != this && entity instanceof LivingEntity)
         .forEach(entity -> {
            double distance = this.distanceTo(entity);
            if (distance <= radius) {
               entity.setOnFireFor(5);
               if (world instanceof ServerWorld serverWorldx) {
                  serverWorldx.spawnParticles(ParticleTypes.FLAME, entity.getX(), entity.getY() + 1.0, entity.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
               }
            }
         });
      if (world instanceof ServerWorld serverWorld) {
         serverWorld.spawnParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 20, 1.0, 1.0, 1.0, 0.2);
      }
   }

   @Override
   protected void enableGhostDomain(PlayerEntity player, StatusEffect effect) {
      player.addStatusEffect(new StatusEffectInstance(ModEffects.GREEN_GHOST_DOMAIN_TARGET, 20, this.getGhostDomainLevel() - 1, false, false, false));
   }

   @Override
   protected String getAttackDialogue() {
      String[] dialogues = new String[]{"大不了拼个厉鬼复苏。"};
      return dialogues[this.getRandom().nextInt(dialogues.length)];
   }

   @Override
   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         ItemStack ghostMoney3Input = new ItemStack(ModItems.GHOST_MONEY_3, 3);
         ItemStack goldIngotOutput2 = new ItemStack(Items.GOLD_INGOT, 24);
         this.tradeOffers.add(new TradeOffer(ghostMoney3Input, goldIngotOutput2, 12, 5, 0.05F));
         ItemStack ghostAxeInput = new ItemStack(ModItems.GHOST_AXE, 1);
         ItemStack goldIngotOutput3 = new ItemStack(Items.GOLD_BLOCK, 4);
         this.tradeOffers.add(new TradeOffer(ghostAxeInput, goldIngotOutput3, 6, 8, 0.08F));
         this.tradeOffersInitialized = true;
      }
   }
}
