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
      this.setCustomName(Text.literal("§6王小明"));
      this.setCustomNameVisible(true);
      this.setSilent(true);
      this.initTradeOffers();
   }

   private void initTradeOffers() {
      this.offers.clear();
      int experience = this.getExperience();
      if (experience >= 0) {
         ItemStack goldenContainerBuy = new ItemStack(ModItems.GOLDEN_CONTAINER, 1);
         goldenContainerBuy.getOrCreateNbt().putBoolean("HasGhost", true);
         this.offers.add(new WangXiaoMingEntity.GhostTradeOffer(goldenContainerBuy, new ItemStack(Items.GOLD_BLOCK, 3), 3, 5, 0.05F));
         ItemStack goldBlockInput1 = new ItemStack(Items.GOLD_BLOCK, 2);
         ItemStack whiteGhostCandleOutput = new ItemStack(ModItems.WHITE_GHOST_CANDLE, 1);
         this.offers.add(new TradeOffer(goldBlockInput1, whiteGhostCandleOutput, 8, 5, 0.05F));
         ItemStack goldBlockInput2 = new ItemStack(Items.GOLD_BLOCK, 3);
         ItemStack ghostPorcelainOutput = new ItemStack(ModItems.GHOST_PORCELAIN, 1);
         this.offers.add(new TradeOffer(goldBlockInput2, ghostPorcelainOutput, 6, 5, 0.05F));
      }

      if (experience >= 10) {
         ItemStack goldBlockInput3 = new ItemStack(Items.GOLD_BLOCK, 5);
         ItemStack redGhostCandleOutput = new ItemStack(ModItems.RED_GHOST_CANDLE, 1);
         this.offers.add(new TradeOffer(goldBlockInput3, redGhostCandleOutput, 4, 5, 0.05F));
      }

      if (experience >= 30) {
         ItemStack goldBlockInput4 = new ItemStack(Items.GOLD_BLOCK, 3);
         ItemStack ghostFactionOutput = new ItemStack(ModItems.GHOST_FACTION, 1);
         this.offers.add(new TradeOffer(goldBlockInput4, ghostFactionOutput, 3, 5, 0.05F));
         ItemStack goldBlockInput5 = new ItemStack(Items.GOLD_BLOCK, 1);
         ItemStack spiritSurgePotionOutput = new ItemStack(ModItems.SPIRIT_SURGE_POTION, 1);
         this.offers.add(new TradeOffer(goldBlockInput5, spiritSurgePotionOutput, 4, 5, 0.05F));
      }

      if (experience >= 60) {
         ItemStack goldBlockInput6 = new ItemStack(Items.GOLD_BLOCK, 6);
         ItemStack ghostChineseMedicineOutput = new ItemStack(ModItems.GHOST_CHINESE_MEDICINE, 1);
         this.offers.add(new TradeOffer(goldBlockInput6, ghostChineseMedicineOutput, 6, 5, 0.05F));
      }

      if (experience >= 100) {
         ItemStack goldBlockInput7 = new ItemStack(Items.GOLD_BLOCK, 8);
         ItemStack controlSlotOutput = new ItemStack(ModItems.CONTROL_SLOT, 1);
         this.offers.add(new TradeOffer(goldBlockInput7, controlSlotOutput, 2, 5, 0.05F));
         ItemStack goldBlockInput9 = new ItemStack(Items.GOLD_BLOCK, 12);
         ItemStack ghostDoorOutput = new ItemStack(ModItems.NEW_GHOST_DOOR, 1);
         this.offers.add(new TradeOffer(goldBlockInput9, ghostDoorOutput, 1, 5, 0.05F));
      }
   }

   public static Builder createWangXiaoMingAttributes() {
      return VillagerEntity.createVillagerAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0);
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
      if (!this.getWorld().isClient) {
         offer.use();
         int currentExperience = this.getExperience();
         this.setExperienceFromServer(currentExperience + 1);
         this.initTradeOffers();
         if (this.getCustomer() instanceof ServerPlayerEntity serverPlayer) {
            this.updateTradeQuestProgress(serverPlayer);
         }
      }
   }

   public void onSellingItem(ItemStack stack) {
   }

   public int getExperience() {
      return this.tradeExperience;
   }

   public PlayerFaction getFaction() {
      return this.faction;
   }

   public void setExperienceFromServer(int experience) {
      this.tradeExperience = experience;
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

   public String[] getGreetingDialogues() {
      return DIALOGUES;
   }

   public void openTradeScreen(ServerPlayerEntity player) {
      this.setCustomer(player);
      int experience = this.getExperience();
      int level = this.calculateLevel(experience);
      this.setCustomName(Text.literal("§6王小明 §7(等级" + level + ")"));
      player.openHandledScreen(new NamedScreenHandlerFactory() {
         public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity playerx) {
            return new MerchantScreenHandler(syncId, inventory, WangXiaoMingEntity.this);
         }

         public Text getDisplayName() {
            return WangXiaoMingEntity.this.getDisplayName();
         }
      });
      int syncId = player.currentScreenHandler != null ? player.currentScreenHandler.syncId : 0;
      player.networkHandler
         .sendPacket(
            new SetTradeOffersS2CPacket(
               syncId, this.offers, this.calculateLevelProgress(experience), this.getExperience(), this.isLeveledMerchant(), this.canRefreshTrades()
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

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("TradeExperience", this.tradeExperience);
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("TradeExperience")) {
         this.tradeExperience = nbt.getInt("TradeExperience");
      }

      this.initTradeOffers();
   }

   public boolean isInvulnerableTo(DamageSource damageSource) {
      return damageSource.getAttacker() instanceof ZombieEntity ? true : super.isInvulnerableTo(damageSource);
   }

   private void updateTradeQuestProgress(ServerPlayerEntity player) {
      try {
         NbtCompound data = PlayerEvents.getCachedData(player);
         String tradeKey = "wangxiaoming_trade_count";
         int currentCount = data.contains(tradeKey) ? data.getInt(tradeKey) : 0;
         data.putInt(tradeKey, currentCount + 1);
         PlayerEvents.saveDataToPlayer(player, data);
      } catch (Exception var5) {
      }
   }

   public static class GhostTradeOffer extends TradeOffer {
      public GhostTradeOffer(ItemStack firstBuyItem, ItemStack sellItem, int maxUses, int merchantExperience, float priceMultiplier) {
         super(firstBuyItem, sellItem, maxUses, merchantExperience, priceMultiplier);
      }

      public boolean matches(ItemStack offeredStack) {
         return offeredStack.isOf(ModItems.GOLDEN_CONTAINER) && GoldenContainerItem.hasGhost(offeredStack);
      }
   }
}
