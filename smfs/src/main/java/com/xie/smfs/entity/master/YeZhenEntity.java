package com.xie.smfs.entity.master;

import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.event.ModEvents;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.item.ScapegoatGhostItem;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;

public class YeZhenEntity extends GhostMasterEntity {
   public YeZhenEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.setGhostDomainLevel(0);
      this.setGhostDomainRadius(32.0);
      this.setSpiritualStrength(10000);
      this.setSpiritualDamage(80);
      this.setSpiritualResistance(155);
      this.setRecoveryFactor(0.08F);
      this.shouldFleeFromGhosts = false;
      this.shouldAttackPlayers = false;
      this.shouldProtectPlayers = true;
      this.shouldAttackGhostsNearPlayers = true;
      this.setCustomName(Text.literal("§6[替死鬼]叶真"));
      this.setCustomNameVisible(true);
      this.faction = PlayerFaction.SPIRIT_FORUM;
   }

   public static Builder createYeZhenAttributes() {
      return GhostMasterEntity.createGhostMasterAttributes().add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.8);
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
      return "叶真";
   }

   @Override
   protected void applyGhostDomainToNearbyPlayers(StatusEffect effect) {
   }

   @Override
   public boolean tryAttack(Entity target) {
      boolean attacked = super.tryAttack(target);
      if (attacked && target instanceof LivingEntity livingTarget) {
         livingTarget.addStatusEffect(new StatusEffectInstance(ModEffects.SILENCE, 60, 0, false, true, true));
      }

      return attacked;
   }

   @Override
   public boolean damage(DamageSource source, float amount) {
      if (this.isSuppressed()) {
         return super.damage(source, amount);
      }

      if (this.getHealth() - amount <= 0.0F && !this.getWorld().isClient()) {
         LivingEntity transferTarget = this.findTransferTargetWithin64Blocks();
         if (transferTarget != null) {
            boolean isGhostMasterOrGhost = transferTarget instanceof GhostMasterEntity || transferTarget instanceof GhostEntity;
            if (this.hasScapegoatAbility(transferTarget) && transferTarget.getHealth() - amount <= 0.0F) {
               return super.damage(source, amount);
            }

            if (isGhostMasterOrGhost) {
               ModEvents.processingSpiritDamage.set(true);
               DamageSource spiritDamageSource = ModDamageSources.ghost(this.getWorld());
               transferTarget.damage(spiritDamageSource, amount);
               ModEvents.processingSpiritDamage.set(false);
               this.setHealth(this.getMaxHealth());
               this.getWorld().addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 0.0, 0.0, 0.0);
               return true;
            }

            DamageSource spiritDamageSource = ModDamageSources.ghost(this.getWorld());
            boolean damageApplied = transferTarget.damage(spiritDamageSource, amount);
            if (damageApplied) {
               this.setHealth(this.getMaxHealth());
               this.getWorld().addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 0.0, 0.0, 0.0);
               return true;
            }
         }
      }

      return super.damage(source, amount);
   }

   private LivingEntity findTransferTargetWithin64Blocks() {
      List<LivingEntity> nearbyEntities = this.getWorld()
         .getEntitiesByClass(
            LivingEntity.class, this.getBoundingBox().expand(64.0), entity -> entity != this && entity.isAlive() && !(entity instanceof PlayerEntity)
         );
      if (!nearbyEntities.isEmpty()) {
         nearbyEntities.sort((entity1, entity2) -> {
            double distance1 = this.squaredDistanceTo(entity1);
            double distance2 = this.squaredDistanceTo(entity2);
            return Double.compare(distance1, distance2);
         });
         return nearbyEntities.get(0);
      } else {
         return null;
      }
   }

   private void spawnDamageTransferParticles() {
      ServerWorld serverWorld = (ServerWorld)this.getWorld();
      Vec3d pos = this.getPos();

      for (int i = 0; i < 20; i++) {
         double offsetX = (this.getRandom().nextDouble() - 0.5) * 2.0;
         double offsetY = this.getRandom().nextDouble() * 2.0;
         double offsetZ = (this.getRandom().nextDouble() - 0.5) * 2.0;
         serverWorld.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, 0.0, 0.0, 0.0, 0.1);
      }
   }

   private boolean hasScapegoatAbility(LivingEntity entity) {
      return entity instanceof PlayerEntity ? ScapegoatGhostItem.hasScapegoatGhostEquipped((PlayerEntity)entity) : entity instanceof YeZhenEntity;
   }

   @Override
   protected String getAttackDialogue() {
      String[] dialogues = new String[]{"我叶某人无敌于世间。"};
      return dialogues[this.getRandom().nextInt(dialogues.length)];
   }

   @Override
   public String[] getGreetingDialogues() {
      return new String[]{
         "我叶某人天下无敌！", "想做我的小弟吗？放心，有我罩着，没人敢欺负你！", "总感觉有一些很重要的事情记不起来。", "每天早上喝一杯牛奶，我也不知道为什么会有这个习惯。", "想做我叶某人的小弟，你还不够格。", "小方最近很飘啊，除了杨无敌，我叶某人没有对手。"
      };
   }

   @Override
   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         ItemStack ghostMoney3Input = new ItemStack(ModItems.GHOST_MONEY_3, 3);
         ItemStack goldIngotOutput2 = new ItemStack(Items.GOLD_INGOT, 26);
         this.tradeOffers.add(new TradeOffer(ghostMoney3Input, goldIngotOutput2, 12, 5, 0.05F));
         ItemStack coffinNailInput = new ItemStack(ModItems.COFFIN_NAIL, 1);
         ItemStack goldNuggetOutput2 = new ItemStack(Items.GOLD_BLOCK, 22);
         this.tradeOffers.add(new TradeOffer(coffinNailInput, goldNuggetOutput2, 3, 15, 0.15F));
         this.tradeOffersInitialized = true;
      }
   }
}
