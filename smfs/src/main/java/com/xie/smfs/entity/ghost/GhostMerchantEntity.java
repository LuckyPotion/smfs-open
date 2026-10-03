package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.GhostMoneyItem;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostMerchantEntity extends GhostEntity implements Merchant {
   private static final Logger LOGGER = LoggerFactory.getLogger(GhostMerchantEntity.class);
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private PlayerEntity customer;
   private TradeOfferList offers = new TradeOfferList();
   private boolean tradeOffersInitialized = false;

   public GhostMerchantEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, false, 0, 32.0, 'C', 3500, 380, 60, 0.3F);
      this.ghostLevel = 0;
      this.attackCooldown = 60;
      this.setEnableChaseAfterRule(false);
      this.initMerchantAttributes();
      this.enableGhostDomain();
      this.setGhostDomainRadius(this.getDefaultGhostDomainRadius() * 1.2F);
   }

   private void initMerchantAttributes() {
      this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(100000.0);
      this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3);
      this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).setBaseValue(16.0);
      this.setHealth(30.0F);
   }

   @Override
   protected void initGoals() {
      super.initGoals();
      if (this.goalSelector != null) {
         this.goalSelector.getGoals().removeIf(goal -> {
            String goalClassName = goal.getGoal().getClass().getSimpleName();
            return goalClassName.contains("Wander") || goalClassName.contains("WanderAround");
         });
      }
   }

   private void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.offers = new TradeOfferList();
         ItemStack emeraldInput = new ItemStack(ModItems.GHOST_MONEY_3, 1);
         ItemStack candyOutput = new ItemStack(ModItems.GHOST_CANDY, 8);
         this.offers.add(new TradeOffer(emeraldInput, candyOutput, 12, 5, 0.05F));
         ItemStack input = new ItemStack(ModItems.GHOST_MONEY_7, 1);
         ItemStack output = new ItemStack(ModItems.RUSTY_FRUIT_KNIFE, 1);
         this.offers.add(new TradeOffer(input, output, 12, 5, 0.05F));
         ItemStack blackCoffinInput = new ItemStack(ModItems.GHOST_MONEY_7, 1);
         ItemStack blackCoffinOutput = new ItemStack(ModItems.GHOST_COFFIN, 1);
         this.offers.add(new TradeOffer(blackCoffinInput, blackCoffinOutput, 12, 5, 0.05F));
         ItemStack redCoffinInput = new ItemStack(ModItems.GHOST_MONEY_7, 2);
         ItemStack redCoffinOutput = new ItemStack(ModItems.RED_COFFIN, 1);
         this.offers.add(new TradeOffer(redCoffinInput, redCoffinOutput, 12, 5, 0.05F));
         ItemStack ghostRecordInput = new ItemStack(ModItems.GHOST_MONEY_3, 1);
         ItemStack ghostRecordOutput = new ItemStack(ModItems.GHOST_RECORD, 1);
         this.offers.add(new TradeOffer(ghostRecordInput, ghostRecordOutput, 12, 5, 0.05F));
         ItemStack ghostMirrorInput = new ItemStack(ModItems.GHOST_MONEY_7, 5);
         ItemStack ghostMirrorOutput = new ItemStack(ModItems.GHOST_MIRROR, 1);
         this.offers.add(new TradeOffer(ghostMirrorInput, ghostMirrorOutput, 12, 5, 0.05F));
         this.tradeOffersInitialized = true;
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient && !this.tradeOffersInitialized) {
         this.initTradeOffers();
      }

      if (!this.getWorld().isClient && !this.isSuppressed() && !this.isDeadlocked() && this.getNavigation().isFollowingPath()) {
         this.getNavigation().stop();
      }

      if (!this.getWorld().isClient && !this.isSuppressed() && !this.isDeadlocked() && this.isKillingRulesEnabled() && this.customer == null) {
         for (PlayerEntity player : this.getWorld().getPlayers()) {
            boolean attackable = this.shouldAttackPlayer(player);
            boolean reachable = this.canReachPlayer(player);
            if (attackable && reachable) {
               this.executeAttack(player);
               break;
            }
         }
      }
   }

   private boolean canReachPlayer(PlayerEntity player) {
      double distanceSq = this.squaredDistanceTo(player);
      return distanceSq <= 256.0;
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (!this.isSuppressed() && !this.isDeadlocked() && this.isKillingRulesEnabled() && this.attackCooldown <= 0) {
         if (this.squaredDistanceTo(player) > 1024.0) {
            return false;
         }

         if (RedGhostCandleItem.isHoldingCandle(player)) {
            return false;
         }

         if (this.customer != null && this.customer.equals(player)) {
            boolean hasGhostMoney = false;

            for (int i = 0; i < player.getInventory().size(); i++) {
               ItemStack stack = player.getInventory().getStack(i);
               if (stack.getItem() instanceof GhostMoneyItem && stack.getCount() > 0) {
                  hasGhostMoney = true;
                  break;
               }
            }

            return !hasGhostMoney;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      if (this.getWorld().isClient) {
         return ActionResult.SUCCESS;
      }

      if (player.isSneaking()) {
         return super.interactMob(player, hand);
      }

      if (!this.tradeOffersInitialized) {
         this.initTradeOffers();
      }

      if (this.getCustomer() != player) {
         this.setCustomer(player);
         player.openHandledScreen(new NamedScreenHandlerFactory() {
            public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity playerx) {
               return new MerchantScreenHandler(syncId, inventory, GhostMerchantEntity.this);
            }

            public Text getDisplayName() {
               return GhostMerchantEntity.this.getDisplayName();
            }
         });
         if (!this.getWorld().isClient && player instanceof ServerPlayerEntity serverPlayer) {
            if (!this.tradeOffersInitialized || this.offers.isEmpty()) {
               this.initTradeOffers();
            }

            int syncId = serverPlayer.currentScreenHandler != null ? serverPlayer.currentScreenHandler.syncId : 0;
            serverPlayer.networkHandler
               .sendPacket(new SetTradeOffersS2CPacket(syncId, this.getOffers(), 0, this.getExperience(), this.isLeveledMerchant(), this.canRefreshTrades()));
         }
      }

      return ActionResult.CONSUME;
   }

   public TradeOfferList getOffers() {
      if (this.getWorld().isClient) {
         LOGGER.info("GhostMerchantEntity.getOffers(客户端): offers.size() = {}, tradeOffersInitialized = {}", this.offers.size(), this.tradeOffersInitialized);
         if (this.offers.isEmpty()) {
            LOGGER.info("客户端交易列表为空，创建默认交易列表");
            TradeOfferList defaultOffers = new TradeOfferList();
            ItemStack emeraldInput = new ItemStack(ModItems.GHOST_MONEY_3, 1);
            ItemStack candyOutput = new ItemStack(ModItems.GHOST_CANDY, 8);
            defaultOffers.add(new TradeOffer(emeraldInput, candyOutput, 12, 5, 0.05F));
            ItemStack input = new ItemStack(ModItems.GHOST_MONEY_7, 1);
            ItemStack output = new ItemStack(ModItems.RUSTY_FRUIT_KNIFE, 1);
            defaultOffers.add(new TradeOffer(input, output, 12, 5, 0.05F));
            ItemStack blackCoffinInput = new ItemStack(ModItems.GHOST_MONEY_7, 1);
            ItemStack blackCoffinOutput = new ItemStack(ModItems.GHOST_COFFIN, 1);
            defaultOffers.add(new TradeOffer(blackCoffinInput, blackCoffinOutput, 12, 5, 0.05F));
            ItemStack redCoffinInput = new ItemStack(ModItems.GHOST_MONEY_7, 2);
            ItemStack redCoffinOutput = new ItemStack(ModItems.RED_COFFIN, 1);
            defaultOffers.add(new TradeOffer(redCoffinInput, redCoffinOutput, 12, 5, 0.05F));
            ItemStack ghostRecordInput = new ItemStack(ModItems.GHOST_MONEY_3, 1);
            ItemStack ghostRecordOutput = new ItemStack(ModItems.GHOST_RECORD, 1);
            defaultOffers.add(new TradeOffer(ghostRecordInput, ghostRecordOutput, 12, 5, 0.05F));
            LOGGER.info("返回默认交易列表，数量: {}", defaultOffers.size());
            return defaultOffers;
         } else {
            LOGGER.info("返回同步的交易列表，数量: {}", this.offers.size());
            return this.offers;
         }
      } else {
         LOGGER.info("GhostMerchantEntity.getOffers(服务器端): offers.size() = {}, tradeOffersInitialized = {}", this.offers.size(), this.tradeOffersInitialized);
         if (!this.tradeOffersInitialized || this.offers.isEmpty()) {
            LOGGER.info("服务器端交易列表未初始化或为空，初始化交易列表");
            this.initTradeOffers();
         }

         LOGGER.info("服务器端返回交易列表，数量: {}", this.offers.size());
         return this.offers;
      }
   }

   public void setOffersFromServer(TradeOfferList offers) {
      if (offers != null) {
         LOGGER.info("GhostMerchantEntity.setOffersFromServer: 接收到 {} 个交易项目", offers.size());

         for (int i = 0; i < offers.size(); i++) {
            TradeOffer offer = (TradeOffer)offers.get(i);
            if (offer != null) {
               LOGGER.info(
                  "交易项目 {}: 输入1={}, 输入2={}, 输出={}, 使用次数={}, 最大使用次数={}",
                  i,
                  offer.getOriginalFirstBuyItem(),
                  offer.getSecondBuyItem(),
                  offer.getSellItem(),
                  offer.getUses(),
                  offer.getMaxUses()
               );
            }
         }

         this.offers = offers;
         this.tradeOffersInitialized = true;
         LOGGER.info("交易列表已更新，当前交易数量: {}", this.offers.size());
      } else {
         LOGGER.warn("setOffersFromServer: 传入的交易列表为空");
      }
   }

   public void setCustomer(PlayerEntity customer) {
      this.customer = customer;
   }

   public PlayerEntity getCustomer() {
      return this.customer;
   }

   public void trade(TradeOffer offer) {
      offer.use();
   }

   public void onSellingItem(ItemStack stack) {
   }

   public int getExperience() {
      return 0;
   }

   public void setExperienceFromServer(int experience) {
   }

   public boolean isLeveledMerchant() {
      return false;
   }

   public SoundEvent getYesSound() {
      return null;
   }

   public boolean isClient() {
      return this.getWorld().isClient;
   }

   @Override
   protected float getDefaultGhostDomainRadius() {
      return 10.0F + this.ghostLevel * 2.0F;
   }

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      NbtList offersNbt = new NbtList();

      for (TradeOffer offer : this.offers) {
         NbtCompound offerNbt = offer.toNbt();
         offersNbt.add(offerNbt);
      }

      nbt.put("Offers", offersNbt);
      nbt.putBoolean("TradeOffersInitialized", this.tradeOffersInitialized);
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("Offers", 9)) {
         NbtList offersNbt = nbt.getList("Offers", 10);
         this.offers = new TradeOfferList();

         for (int i = 0; i < offersNbt.size(); i++) {
            NbtCompound offerNbt = offersNbt.getCompound(i);
            TradeOffer offer = new TradeOffer(offerNbt);
            this.offers.add(offer);
         }
      }

      if (nbt.contains("TradeOffersInitialized")) {
         this.tradeOffersInitialized = nbt.getBoolean("TradeOffersInitialized");
      }
   }
}
