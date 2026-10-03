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
      this.method_5996(EntityAttributes.field_23716).method_6192(100000.0);
      this.method_5996(EntityAttributes.field_23719).method_6192(0.3);
      this.method_5996(EntityAttributes.field_23717).method_6192(16.0);
      this.method_6033(30.0F);
   }

   @Override
   protected void method_5959() {
      super.method_5959();
      if (this.field_6201 != null) {
         this.field_6201.method_35115().removeIf(goal -> {
            String goalClassName = goal.method_19058().getClass().getSimpleName();
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
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236 && !this.tradeOffersInitialized) {
         this.initTradeOffers();
      }

      if (!this.method_37908().field_9236 && !this.isSuppressed() && !this.isDeadlocked() && this.method_5942().method_23966()) {
         this.method_5942().method_6340();
      }

      if (!this.method_37908().field_9236 && !this.isSuppressed() && !this.isDeadlocked() && this.isKillingRulesEnabled() && this.customer == null) {
         for (PlayerEntity player : this.method_37908().method_18456()) {
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
      double distanceSq = this.method_5858(player);
      return distanceSq <= 256.0;
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (!this.isSuppressed() && !this.isDeadlocked() && this.isKillingRulesEnabled() && this.attackCooldown <= 0) {
         if (this.method_5858(player) > 1024.0) {
            return false;
         }

         if (RedGhostCandleItem.isHoldingCandle(player)) {
            return false;
         }

         if (this.customer != null && this.customer.equals(player)) {
            boolean hasGhostMoney = false;

            for (int i = 0; i < player.method_31548().method_5439(); i++) {
               ItemStack stack = player.method_31548().method_5438(i);
               if (stack.method_7909() instanceof GhostMoneyItem && stack.method_7947() > 0) {
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

   public ActionResult method_5992(PlayerEntity player, Hand hand) {
      if (this.method_37908().field_9236) {
         return ActionResult.field_5812;
      }

      if (player.method_5715()) {
         return super.method_5992(player, hand);
      }

      if (!this.tradeOffersInitialized) {
         this.initTradeOffers();
      }

      if (this.method_8257() != player) {
         this.method_8259(player);
         player.method_17355(new NamedScreenHandlerFactory() {
            public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity playerx) {
               return new MerchantScreenHandler(syncId, inventory, GhostMerchantEntity.this);
            }

            public Text method_5476() {
               return GhostMerchantEntity.this.method_5476();
            }
         });
         if (!this.method_37908().field_9236 && player instanceof ServerPlayerEntity serverPlayer) {
            if (!this.tradeOffersInitialized || this.offers.isEmpty()) {
               this.initTradeOffers();
            }

            int syncId = serverPlayer.field_7512 != null ? serverPlayer.field_7512.field_7763 : 0;
            serverPlayer.field_13987
               .method_14364(new SetTradeOffersS2CPacket(syncId, this.method_8264(), 0, this.method_19269(), this.method_19270(), this.method_20708()));
         }
      }

      return ActionResult.field_21466;
   }

   public TradeOfferList method_8264() {
      if (this.method_37908().field_9236) {
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

   public void method_8261(TradeOfferList offers) {
      if (offers != null) {
         LOGGER.info("GhostMerchantEntity.setOffersFromServer: 接收到 {} 个交易项目", offers.size());

         for (int i = 0; i < offers.size(); i++) {
            TradeOffer offer = (TradeOffer)offers.get(i);
            if (offer != null) {
               LOGGER.info(
                  "交易项目 {}: 输入1={}, 输入2={}, 输出={}, 使用次数={}, 最大使用次数={}",
                  i,
                  offer.method_8246(),
                  offer.method_8247(),
                  offer.method_8250(),
                  offer.method_8249(),
                  offer.method_8248()
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

   public void method_8259(PlayerEntity customer) {
      this.customer = customer;
   }

   public PlayerEntity method_8257() {
      return this.customer;
   }

   public void method_8262(TradeOffer offer) {
      offer.method_8244();
   }

   public void method_8258(ItemStack stack) {
   }

   public int method_19269() {
      return 0;
   }

   public void method_19271(int experience) {
   }

   public boolean method_19270() {
      return false;
   }

   public SoundEvent method_18010() {
      return null;
   }

   public boolean method_38069() {
      return this.method_37908().field_9236;
   }

   @Override
   protected float getDefaultGhostDomainRadius() {
      return 10.0F + this.ghostLevel * 2.0F;
   }

   @Override
   public void method_5652(NbtCompound nbt) {
      super.method_5652(nbt);
      NbtList offersNbt = new NbtList();

      for (TradeOffer offer : this.offers) {
         NbtCompound offerNbt = offer.method_8251();
         offersNbt.add(offerNbt);
      }

      nbt.method_10566("Offers", offersNbt);
      nbt.method_10556("TradeOffersInitialized", this.tradeOffersInitialized);
   }

   @Override
   public void method_5749(NbtCompound nbt) {
      super.method_5749(nbt);
      if (nbt.method_10573("Offers", 9)) {
         NbtList offersNbt = nbt.method_10554("Offers", 10);
         this.offers = new TradeOfferList();

         for (int i = 0; i < offersNbt.size(); i++) {
            NbtCompound offerNbt = offersNbt.method_10602(i);
            TradeOffer offer = new TradeOffer(offerNbt);
            this.offers.add(offer);
         }
      }

      if (nbt.method_10545("TradeOffersInitialized")) {
         this.tradeOffersInitialized = nbt.method_10577("TradeOffersInitialized");
      }
   }
}
