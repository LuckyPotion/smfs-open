package com.xie.smfs.entity.other;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.item.GoldenContainerItem;
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
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
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

public class WangXiaoMingEntity extends VillagerEntity implements Merchant {
   private TradeOfferList offers = new TradeOfferList();
   private PlayerEntity customer;
   private int tradeExperience = 0;
   private final PlayerFaction faction = PlayerFaction.HEADQUARTERS;
   private static final String[] DIALOGUES = new String[]{"我只在乎你的价值，同样，你肯定也会衡量我能够带给你什么。", "我有一个弟弟...", "加入总部，这是你当下最好的选择。", "鬼无法影响黄金，黄金是硬通货。"};

   public WangXiaoMingEntity(EntityType<? extends VillagerEntity> entityType, World world) {
      super(entityType, world);
      this.method_5665(Text.method_43470("§6王小明"));
      this.method_5880(true);
      this.method_5803(true);
      this.initTradeOffers();
   }

   private void initTradeOffers() {
      this.offers.clear();
      int experience = this.method_19269();
      if (experience >= 0) {
         ItemStack goldenContainerBuy = new ItemStack(ModItems.GOLDEN_CONTAINER, 1);
         goldenContainerBuy.method_7948().method_10556("HasGhost", true);
         this.offers.add(new WangXiaoMingEntity.GhostTradeOffer(goldenContainerBuy, new ItemStack(Items.field_8494, 3), 3, 5, 0.05F));
         ItemStack goldBlockInput1 = new ItemStack(Items.field_8494, 2);
         ItemStack whiteGhostCandleOutput = new ItemStack(ModItems.WHITE_GHOST_CANDLE, 1);
         this.offers.add(new TradeOffer(goldBlockInput1, whiteGhostCandleOutput, 8, 5, 0.05F));
         ItemStack goldBlockInput2 = new ItemStack(Items.field_8494, 3);
         ItemStack ghostPorcelainOutput = new ItemStack(ModItems.GHOST_PORCELAIN, 1);
         this.offers.add(new TradeOffer(goldBlockInput2, ghostPorcelainOutput, 6, 5, 0.05F));
      }

      if (experience >= 10) {
         ItemStack goldBlockInput3 = new ItemStack(Items.field_8494, 5);
         ItemStack redGhostCandleOutput = new ItemStack(ModItems.RED_GHOST_CANDLE, 1);
         this.offers.add(new TradeOffer(goldBlockInput3, redGhostCandleOutput, 4, 5, 0.05F));
      }

      if (experience >= 30) {
         ItemStack goldBlockInput4 = new ItemStack(Items.field_8494, 3);
         ItemStack ghostFactionOutput = new ItemStack(ModItems.GHOST_FACTION, 1);
         this.offers.add(new TradeOffer(goldBlockInput4, ghostFactionOutput, 3, 5, 0.05F));
         ItemStack goldBlockInput5 = new ItemStack(Items.field_8494, 1);
         ItemStack spiritSurgePotionOutput = new ItemStack(ModItems.SPIRIT_SURGE_POTION, 1);
         this.offers.add(new TradeOffer(goldBlockInput5, spiritSurgePotionOutput, 4, 5, 0.05F));
      }

      if (experience >= 60) {
         ItemStack goldBlockInput6 = new ItemStack(Items.field_8494, 6);
         ItemStack ghostChineseMedicineOutput = new ItemStack(ModItems.GHOST_CHINESE_MEDICINE, 1);
         this.offers.add(new TradeOffer(goldBlockInput6, ghostChineseMedicineOutput, 6, 5, 0.05F));
      }

      if (experience >= 100) {
         ItemStack goldBlockInput7 = new ItemStack(Items.field_8494, 8);
         ItemStack controlSlotOutput = new ItemStack(ModItems.CONTROL_SLOT, 1);
         this.offers.add(new TradeOffer(goldBlockInput7, controlSlotOutput, 2, 5, 0.05F));
         ItemStack goldBlockInput9 = new ItemStack(Items.field_8494, 12);
         ItemStack ghostDoorOutput = new ItemStack(ModItems.NEW_GHOST_DOOR, 1);
         this.offers.add(new TradeOffer(goldBlockInput9, ghostDoorOutput, 1, 5, 0.05F));
      }
   }

   public static Builder createWangXiaoMingAttributes() {
      return VillagerEntity.method_26955().method_26868(EntityAttributes.field_23716, 100.0);
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
      if (!this.method_37908().field_9236) {
         offer.method_8244();
         int currentExperience = this.method_19269();
         this.method_19271(currentExperience + 1);
         this.initTradeOffers();
         if (this.method_8257() instanceof ServerPlayerEntity serverPlayer) {
            this.updateTradeQuestProgress(serverPlayer);
         }
      }
   }

   public void method_8258(ItemStack stack) {
   }

   public int method_19269() {
      return this.tradeExperience;
   }

   public PlayerFaction getFaction() {
      return this.faction;
   }

   public void method_19271(int experience) {
      this.tradeExperience = experience;
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

   public String[] getGreetingDialogues() {
      return DIALOGUES;
   }

   public void openTradeScreen(ServerPlayerEntity player) {
      this.method_8259(player);
      int experience = this.method_19269();
      int level = this.calculateLevel(experience);
      this.method_5665(Text.method_43470("§6王小明 §7(等级" + level + ")"));
      player.method_17355(new NamedScreenHandlerFactory() {
         public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity playerx) {
            return new MerchantScreenHandler(syncId, inventory, WangXiaoMingEntity.this);
         }

         public Text method_5476() {
            return WangXiaoMingEntity.this.method_5476();
         }
      });
      int syncId = player.field_7512 != null ? player.field_7512.field_7763 : 0;
      player.field_13987
         .method_14364(
            new SetTradeOffersS2CPacket(
               syncId, this.offers, this.calculateLevelProgress(experience), this.method_19269(), this.method_19270(), this.method_20708()
            )
         );
   }

   private int calculateLevel(int experience) {
      if (experience >= 100) {
         return 5;
      } else if (experience >= 60) {
         return 4;
      } else if (experience >= 30) {
         return 3;
      } else {
         return experience >= 10 ? 2 : 1;
      }
   }

   private int calculateLevelProgress(int experience) {
      if (experience >= 100) {
         return 5;
      } else if (experience >= 60) {
         return 4;
      } else if (experience >= 30) {
         return 3;
      } else if (experience >= 10) {
         return 2;
      } else {
         return experience >= 0 ? 1 : 0;
      }
   }

   public void method_5652(NbtCompound nbt) {
      super.method_5652(nbt);
      nbt.method_10569("TradeExperience", this.tradeExperience);
   }

   public void method_5749(NbtCompound nbt) {
      super.method_5749(nbt);
      if (nbt.method_10545("TradeExperience")) {
         this.tradeExperience = nbt.method_10550("TradeExperience");
      }

      this.initTradeOffers();
   }

   public boolean method_5679(DamageSource damageSource) {
      return damageSource.method_5529() instanceof ZombieEntity ? true : super.method_5679(damageSource);
   }

   private void updateTradeQuestProgress(ServerPlayerEntity player) {
      try {
         NbtCompound data = PlayerEvents.getCachedData(player);
         String tradeKey = "wangxiaoming_trade_count";
         int currentCount = data.method_10545(tradeKey) ? data.method_10550(tradeKey) : 0;
         data.method_10569(tradeKey, currentCount + 1);
         PlayerEvents.saveDataToPlayer(player, data);
      } catch (Exception var5) {
      }
   }

   public static class GhostTradeOffer extends TradeOffer {
      public GhostTradeOffer(ItemStack firstBuyItem, ItemStack sellItem, int maxUses, int merchantExperience, float priceMultiplier) {
         super(firstBuyItem, sellItem, maxUses, merchantExperience, priceMultiplier);
      }

      public boolean matches(ItemStack offeredStack) {
         return offeredStack.method_31574(ModItems.GOLDEN_CONTAINER) && GoldenContainerItem.hasGhost(offeredStack);
      }
   }
}
