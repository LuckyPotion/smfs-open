package com.xie.smfs.entity.master;

import com.xie.smfs.effect.GhostPressureEffect;
import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;

public class FangShiMinEntity extends GhostMasterEntity {
   public FangShiMinEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.setGhostDomainLevel(6);
      this.setGhostDomainRadius(32.0);
      this.setSpiritualStrength(5000);
      this.setSpiritualDamage(80);
      this.setSpiritualResistance(155);
      this.setRecoveryFactor(0.08F);
      this.shouldFleeFromGhosts = false;
      this.shouldAttackPlayers = false;
      this.method_5665(Text.method_43470("§6方世民"));
      this.method_5880(true);
      this.faction = PlayerFaction.PENGYOU_QUAN;
   }

   public static Builder createFangShiMinAttributes() {
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
      return "方世民";
   }

   @Override
   protected void enableGhostDomain(PlayerEntity player, StatusEffect effect) {
      player.method_6092(new StatusEffectInstance(ModEffects.CYAN_GHOST_DOMAIN_TARGET, 20, this.getGhostDomainLevel() - 1, false, false, false));
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608() && this.method_5968() != null && this.method_5968() instanceof PlayerEntity && this.field_6012 % 80 == 0) {
         this.activateGhostPressureSkill();
      }
   }

   private void activateGhostPressureSkill() {
      PlayerEntity target = (PlayerEntity)this.method_5968();
      if (target != null) {
         target.method_6092(GhostPressureEffect.createEffect(this, 100, 1));
      }
   }

   @Override
   public String[] getGreetingDialogues() {
      return new String[]{"有兴趣加入[朋友圈]吗？", "不听话的驭鬼者，就要做好随时被打掉的准备。", "总部也不过是强弩之末罢了。", "亚洲第一？就那个中二少年吗？别开玩笑了。"};
   }

   @Override
   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         ItemStack ghostMoney3Input = new ItemStack(ModItems.GHOST_MONEY_7, 1);
         ItemStack goldIngotOutput2 = new ItemStack(Items.field_8695, 21);
         this.tradeOffers.add(new TradeOffer(ghostMoney3Input, goldIngotOutput2, 12, 5, 0.05F));
         ItemStack rustyFirewoodKnifeInput = new ItemStack(ModItems.RUSTY_FIREWOOD_KNIFE, 1);
         ItemStack goldIngotOutput4 = new ItemStack(Items.field_8494, 8);
         this.tradeOffers.add(new TradeOffer(rustyFirewoodKnifeInput, goldIngotOutput4, 8, 3, 0.05F));
         this.tradeOffersInitialized = true;
      }
   }
}
