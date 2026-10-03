package com.xie.smfs.entity.other;

import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;

public class LiuXiaoYuEntity extends VillagerEntity implements Merchant {
   private TradeOfferList offers = new TradeOfferList();
   private PlayerEntity customer;
   private final PlayerFaction faction = PlayerFaction.HEADQUARTERS;
   private static final String[] DIALOGUES = new String[]{"外面好危险，你要小心啊...", "你好呀，有什么事吗？", "听说最近附近不太安宁...", "晚上最好不要出门。", "希望一切都会好起来。"};

   public LiuXiaoYuEntity(EntityType<? extends VillagerEntity> entityType, World world) {
      super(entityType, world);
      this.setCustomName(Text.literal("§6刘小雨"));
      this.setCustomNameVisible(true);
      this.setSilent(true);
      this.initTradeOffers();
   }

   private void initTradeOffers() {
      ItemStack input = new ItemStack(ModItems.GHOST_MONEY_7, 2);
      ItemStack output = new ItemStack(ModItems.GHOST_LOT, 1);
      this.offers.add(new TradeOffer(input, output, 12, 5, 0.05F));
   }

   public static Builder createLiuXiaoYuAttributes() {
      return VillagerEntity.createVillagerAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0);
   }

   public String[] getGreetingDialogues() {
      return DIALOGUES;
   }

   protected void initGoals() {
      super.initGoals();
   }

   public boolean canBreed() {
      return false;
   }

   public VillagerData getVillagerData() {
      return new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1);
   }

   public void setVillagerData(VillagerData villagerData) {
   }

   public void setCustomer(PlayerEntity customer) {
      this.customer = customer;
   }

   public PlayerEntity getCustomer() {
      return this.customer;
   }

   public TradeOfferList getOffers() {
      return this.offers;
   }

   public void setOffersFromServer(TradeOfferList offers) {
      this.offers = offers;
   }

   public void trade(TradeOffer offer) {
   }

   public void onSellingItem(ItemStack stack) {
   }

   public int getExperience() {
      return 0;
   }

   public PlayerFaction getFaction() {
      return this.faction;
   }

   public void setExperienceFromServer(int experience) {
   }

   public boolean isLeveledMerchant() {
      return false;
   }

   public SoundEvent getYesSound() {
      return null;
   }

   public boolean canRefreshTrades() {
      return false;
   }

   public void openTradeScreen(ServerPlayerEntity player) {
      this.setCustomer(player);
      player.openHandledScreen(new NamedScreenHandlerFactory() {
         public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity playerx) {
            return new MerchantScreenHandler(syncId, inventory, LiuXiaoYuEntity.this);
         }

         public Text getDisplayName() {
            return LiuXiaoYuEntity.this.getDisplayName();
         }
      });
      int syncId = player.currentScreenHandler != null ? player.currentScreenHandler.syncId : 0;
      player.networkHandler
         .sendPacket(new SetTradeOffersS2CPacket(syncId, this.offers, 0, this.getExperience(), this.isLeveledMerchant(), this.canRefreshTrades()));
   }

   public Text getDisplayName() {
      return Text.literal("§6刘小雨");
   }

   public boolean isInvulnerableTo(DamageSource damageSource) {
      return damageSource.getAttacker() instanceof ZombieEntity ? true : super.isInvulnerableTo(damageSource);
   }
}
