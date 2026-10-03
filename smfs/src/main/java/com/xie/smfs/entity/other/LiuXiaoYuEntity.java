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
      this.method_5665(Text.method_43470("§6刘小雨"));
      this.method_5880(true);
      this.method_5803(true);
      this.initTradeOffers();
   }

   private void initTradeOffers() {
      ItemStack input = new ItemStack(ModItems.GHOST_MONEY_7, 2);
      ItemStack output = new ItemStack(ModItems.GHOST_LOT, 1);
      this.offers.add(new TradeOffer(input, output, 12, 5, 0.05F));
   }

   public static Builder createLiuXiaoYuAttributes() {
      return VillagerEntity.method_26955().method_26868(EntityAttributes.field_23716, 100.0);
   }

   public String[] getGreetingDialogues() {
      return DIALOGUES;
   }

   protected void method_5959() {
      super.method_5959();
   }

   public boolean method_7239() {
      return false;
   }

   public VillagerData method_7231() {
      return new VillagerData(VillagerType.field_17073, VillagerProfession.field_17051, 1);
   }

   public void method_7195(VillagerData villagerData) {
   }

   public void method_8259(PlayerEntity customer) {
      this.customer = customer;
   }

   public PlayerEntity method_8257() {
      return this.customer;
   }

   public TradeOfferList method_8264() {
      return this.offers;
   }

   public void method_8261(TradeOfferList offers) {
      this.offers = offers;
   }

   public void method_8262(TradeOffer offer) {
   }

   public void method_8258(ItemStack stack) {
   }

   public int method_19269() {
      return 0;
   }

   public PlayerFaction getFaction() {
      return this.faction;
   }

   public void method_19271(int experience) {
   }

   public boolean method_19270() {
      return false;
   }

   public SoundEvent method_18010() {
      return null;
   }

   public boolean method_20708() {
      return false;
   }

   public void openTradeScreen(ServerPlayerEntity player) {
      this.method_8259(player);
      player.method_17355(new NamedScreenHandlerFactory() {
         public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity playerx) {
            return new MerchantScreenHandler(syncId, inventory, LiuXiaoYuEntity.this);
         }

         public Text method_5476() {
            return LiuXiaoYuEntity.this.method_5476();
         }
      });
      int syncId = player.field_7512 != null ? player.field_7512.field_7763 : 0;
      player.field_13987.method_14364(new SetTradeOffersS2CPacket(syncId, this.offers, 0, this.method_19269(), this.method_19270(), this.method_20708()));
   }

   public Text method_5476() {
      return Text.method_43470("§6刘小雨");
   }

   public boolean method_5679(DamageSource damageSource) {
      return damageSource.method_5529() instanceof ZombieEntity ? true : super.method_5679(damageSource);
   }
}
