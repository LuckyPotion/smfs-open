package com.xie.smfs.entity.master;

import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import java.util.Objects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;

public class LiLePingEntity extends GhostMasterEntity {
   private boolean isInvisibleActive = false;
   private int invisibleDuration = 0;
   private int invisibleCooldown = 0;
   private static final int INVISIBLE_DURATION = 60;
   private static final int INVISIBLE_COOLDOWN = 200;
   private boolean wasNightMode = false;

   public LiLePingEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.setGhostDomainLevel(3);
      this.setGhostDomainRadius(32.0);
      this.setSpiritualStrength(1900);
      this.setSpiritualDamage(25);
      this.setSpiritualResistance(70);
      this.setRecoveryFactor(0.06F);
      this.shouldFleeFromGhosts = false;
      this.shouldAttackPlayers = false;
      this.shouldProtectPlayers = true;
      this.shouldAttackGhostsNearPlayers = true;
      this.method_5665(Text.method_43470("§6李乐平"));
      this.method_5880(true);
      this.faction = PlayerFaction.HEADQUARTERS;
   }

   public static Builder createLiLePingAttributes() {
      return MobEntity.method_26828()
         .method_26868(EntityAttributes.field_23716, 950.0)
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
      return "李乐平";
   }

   @Override
   protected void enableGhostDomain(PlayerEntity player, StatusEffect effect) {
      int domainLevel = this.getGhostDomainLevel() - 1;
      player.method_6092(new StatusEffectInstance(ModEffects.GRAY_GHOST_DOMAIN_TARGET, 20, domainLevel, false, false, false));
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608()) {
         this.handleNightEnhancement();
         if (this.invisibleCooldown > 0) {
            this.invisibleCooldown--;
         }

         if (this.isInvisibleActive) {
            this.invisibleDuration--;
            if (this.invisibleDuration <= 0) {
               this.isInvisibleActive = false;
               this.method_5648(false);
               this.invisibleCooldown = 200;
            }
         }

         if (!this.isInvisibleActive && this.invisibleCooldown <= 0) {
            float healthPercentage = this.method_6032() / this.method_6063();
            if (healthPercentage <= 0.5F && this.method_6051().method_43057() < 0.3F) {
               this.activateInvisibility();
            }
         }

         if (this.method_5968() != null) {
         }
      }
   }

   private void handleNightEnhancement() {
      boolean isNight = this.method_37908().method_23886();
      int originalSpiritualDamage = 25;
      double originalMovementSpeed = 0.3;
      double originalAttackDamage = 18.0;
      if (isNight && !this.wasNightMode) {
         this.wasNightMode = true;
         this.setSpiritualDamage(originalSpiritualDamage * 2);
         Objects.requireNonNull(this.method_5996(EntityAttributes.field_23719)).method_6192(originalMovementSpeed * 2.0);
         Objects.requireNonNull(this.method_5996(EntityAttributes.field_23721)).method_6192(originalAttackDamage * 2.0);
         this.shouldProtectPlayers = false;
         this.shouldAttackGhostsNearPlayers = false;
      } else if (!isNight && this.wasNightMode) {
         this.wasNightMode = false;
         this.setSpiritualDamage(originalSpiritualDamage);
         Objects.requireNonNull(this.method_5996(EntityAttributes.field_23719)).method_6192(originalMovementSpeed);
         Objects.requireNonNull(this.method_5996(EntityAttributes.field_23721)).method_6192(originalAttackDamage);
         this.shouldProtectPlayers = true;
         this.shouldAttackGhostsNearPlayers = true;
      }
   }

   private void activateInvisibility() {
      this.isInvisibleActive = true;
      this.invisibleDuration = 60;
      this.method_5648(true);
      float currentHealth = this.method_6032();
      float maxHealth = this.method_6063();
      float newHealth = Math.min(currentHealth + 10.0F, maxHealth);
      this.method_6033(newHealth);
   }

   @Override
   public String[] getGreetingDialogues() {
      return new String[]{"记忆是一种非常脆弱的东西，遗忘...", "有时候，看不见的东西更危险", "成为驭鬼者以后，活人的情感越来越少了。", "一旦进入黑夜状态，我便会失控，届时没有人能拦住我...", "我太强了。"};
   }

   @Override
   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         ItemStack ghostMoney3Input = new ItemStack(ModItems.GHOST_MONEY_3, 5);
         ItemStack goldIngotOutput2 = new ItemStack(Items.field_8695, 42);
         this.tradeOffers.add(new TradeOffer(ghostMoney3Input, goldIngotOutput2, 12, 5, 0.05F));
         ItemStack ghostBowInput = new ItemStack(ModItems.GHOST_BOW, 1);
         ItemStack goldIngotOutput3 = new ItemStack(Items.field_8494, 5);
         this.tradeOffers.add(new TradeOffer(ghostBowInput, goldIngotOutput3, 6, 8, 0.08F));
         this.tradeOffersInitialized = true;
      }
   }
}
