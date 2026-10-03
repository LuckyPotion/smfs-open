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
      this.method_5665(Text.method_43470("§6[替死鬼]叶真"));
      this.method_5880(true);
      this.faction = PlayerFaction.SPIRIT_FORUM;
   }

   public static Builder createYeZhenAttributes() {
      return GhostMasterEntity.createGhostMasterAttributes().method_26868(EntityAttributes.field_23722, 0.8);
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
      return "叶真";
   }

   @Override
   protected void applyGhostDomainToNearbyPlayers(StatusEffect effect) {
   }

   @Override
   public boolean method_6121(Entity target) {
      boolean attacked = super.method_6121(target);
      if (attacked && target instanceof LivingEntity livingTarget) {
         livingTarget.method_6092(new StatusEffectInstance(ModEffects.SILENCE, 60, 0, false, true, true));
      }

      return attacked;
   }

   @Override
   public boolean method_5643(DamageSource source, float amount) {
      if (this.isSuppressed()) {
         return super.method_5643(source, amount);
      }

      if (this.method_6032() - amount <= 0.0F && !this.method_37908().method_8608()) {
         LivingEntity transferTarget = this.findTransferTargetWithin64Blocks();
         if (transferTarget != null) {
            boolean isGhostMasterOrGhost = transferTarget instanceof GhostMasterEntity || transferTarget instanceof GhostEntity;
            if (this.hasScapegoatAbility(transferTarget) && transferTarget.method_6032() - amount <= 0.0F) {
               return super.method_5643(source, amount);
            }

            if (isGhostMasterOrGhost) {
               ModEvents.processingSpiritDamage.set(true);
               DamageSource spiritDamageSource = ModDamageSources.ghost(this.method_37908());
               transferTarget.method_5643(spiritDamageSource, amount);
               ModEvents.processingSpiritDamage.set(false);
               this.method_6033(this.method_6063());
               this.method_37908().method_8406(ParticleTypes.field_22246, this.method_23317(), this.method_23318() + 1.0, this.method_23321(), 0.0, 0.0, 0.0);
               return true;
            }

            DamageSource spiritDamageSource = ModDamageSources.ghost(this.method_37908());
            boolean damageApplied = transferTarget.method_5643(spiritDamageSource, amount);
            if (damageApplied) {
               this.method_6033(this.method_6063());
               this.method_37908().method_8406(ParticleTypes.field_22246, this.method_23317(), this.method_23318() + 1.0, this.method_23321(), 0.0, 0.0, 0.0);
               return true;
            }
         }
      }

      return super.method_5643(source, amount);
   }

   private LivingEntity findTransferTargetWithin64Blocks() {
      List<LivingEntity> nearbyEntities = this.method_37908()
         .method_8390(
            LivingEntity.class, this.method_5829().method_1014(64.0), entity -> entity != this && entity.method_5805() && !(entity instanceof PlayerEntity)
         );
      if (!nearbyEntities.isEmpty()) {
         nearbyEntities.sort((entity1, entity2) -> {
            double distance1 = this.method_5858(entity1);
            double distance2 = this.method_5858(entity2);
            return Double.compare(distance1, distance2);
         });
         return nearbyEntities.get(0);
      } else {
         return null;
      }
   }

   private void spawnDamageTransferParticles() {
      ServerWorld serverWorld = (ServerWorld)this.method_37908();
      Vec3d pos = this.method_19538();

      for (int i = 0; i < 20; i++) {
         double offsetX = (this.method_6051().method_43058() - 0.5) * 2.0;
         double offsetY = this.method_6051().method_43058() * 2.0;
         double offsetZ = (this.method_6051().method_43058() - 0.5) * 2.0;
         serverWorld.method_14199(
            ParticleTypes.field_11209, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 1, 0.0, 0.0, 0.0, 0.1
         );
      }
   }

   private boolean hasScapegoatAbility(LivingEntity entity) {
      return entity instanceof PlayerEntity ? ScapegoatGhostItem.hasScapegoatGhostEquipped((PlayerEntity)entity) : entity instanceof YeZhenEntity;
   }

   @Override
   protected String getAttackDialogue() {
      String[] dialogues = new String[]{"我叶某人无敌于世间。"};
      return dialogues[this.method_6051().method_43048(dialogues.length)];
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
         ItemStack goldIngotOutput2 = new ItemStack(Items.field_8695, 26);
         this.tradeOffers.add(new TradeOffer(ghostMoney3Input, goldIngotOutput2, 12, 5, 0.05F));
         ItemStack coffinNailInput = new ItemStack(ModItems.COFFIN_NAIL, 1);
         ItemStack goldNuggetOutput2 = new ItemStack(Items.field_8494, 22);
         this.tradeOffers.add(new TradeOffer(coffinNailInput, goldNuggetOutput2, 3, 15, 0.15F));
         this.tradeOffersInitialized = true;
      }
   }
}
