package com.xie.smfs.entity.other;

import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.registry.ModBlocks;
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

public class ChenDoctorEntity extends VillagerEntity implements Merchant {
   private TradeOfferList offers = new TradeOfferList();
   private PlayerEntity customer;
   private int tradeExperience = 0;
   private final PlayerFaction faction = PlayerFaction.HEADQUARTERS;
   private static final String[] DIALOGUES = new String[]{"研究灵异，需要合适的材料...", "我的研究又有突破性的进展了。", "最近有什么新的发现吗？", "厉鬼身上的宝贝不少..."};

   public ChenDoctorEntity(EntityType<? extends VillagerEntity> entityType, World world) {
      super(entityType, world);
      this.method_5665(Text.method_43470("§6陈博士"));
      this.method_5880(true);
      this.method_5803(true);
      this.initTradeOffers();
   }

   private void initTradeOffers() {
      this.offers.clear();
      int experience = this.method_19269();
      if (experience >= 0) {
         ItemStack goldIngotInput1 = new ItemStack(Items.field_8695, 12);
         ItemStack corpsePieceOutput = new ItemStack(ModItems.CORPSE_PIECE, 1);
         this.offers.add(new TradeOffer(goldIngotInput1, corpsePieceOutput, 8, 5, 0.05F));
         ItemStack goldIngotInput2 = new ItemStack(Items.field_8695, 6);
         ItemStack corpseOilOutput = new ItemStack(ModItems.CORPSE_OIL, 1);
         this.offers.add(new TradeOffer(goldIngotInput2, corpseOilOutput, 6, 5, 0.05F));
      }

      if (experience >= 20) {
         ItemStack blackCoffinInput = new ItemStack(ModItems.GHOST_COFFIN, 1);
         ItemStack goldIngotOutput = new ItemStack(Items.field_8695, 8);
         this.offers.add(new TradeOffer(blackCoffinInput, goldIngotOutput, 8, 5, 0.05F));
         ItemStack goldIngotInput3 = new ItemStack(Items.field_8695, 12);
         ItemStack deafnessPotionOutput = new ItemStack(ModItems.DEAFNESS_POTION, 1);
         this.offers.add(new TradeOffer(goldIngotInput3, deafnessPotionOutput, 8, 5, 0.05F));
      }

      if (experience >= 40) {
         ItemStack eerieRagInput = new ItemStack(ModItems.EERIE_RAG, 1);
         ItemStack goldIngotOutput1 = new ItemStack(Items.field_8695, 3);
         this.offers.add(new TradeOffer(eerieRagInput, goldIngotOutput1, 8, 5, 0.05F));
         ItemStack viscousBloodInput = new ItemStack(ModItems.VISCOUS_BLOOD, 1);
         ItemStack goldIngotOutput2 = new ItemStack(Items.field_8695, 5);
         this.offers.add(new TradeOffer(viscousBloodInput, goldIngotOutput2, 8, 5, 0.05F));
         ItemStack blackenedToothInput = new ItemStack(ModItems.BLACKENED_TOOTH, 1);
         ItemStack goldIngotOutput3 = new ItemStack(Items.field_8695, 5);
         this.offers.add(new TradeOffer(blackenedToothInput, goldIngotOutput3, 8, 5, 0.05F));
      }

      if (experience >= 50) {
         ItemStack goldBlockInput8 = new ItemStack(Items.field_8494, 6);
         ItemStack spiritBrewingStandOutput = new ItemStack(ModBlocks.SPIRIT_BREWING_STAND, 1);
         this.offers.add(new TradeOffer(goldBlockInput8, spiritBrewingStandOutput, 1, 5, 0.05F));
      }
   }

   public static Builder createChenDoctorAttributes() {
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
         this.method_19271(currentExperience + 5);
         this.initTradeOffers();
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
      this.method_5665(Text.method_43470("§6陈博士 §7(等级" + level + ")"));
      player.method_17355(new NamedScreenHandlerFactory() {
         public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity playerx) {
            return new MerchantScreenHandler(syncId, inventory, ChenDoctorEntity.this);
         }

         public Text method_5476() {
            return ChenDoctorEntity.this.method_5476();
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

   public Text method_5476() {
      return Text.method_43470("§6陈博士");
   }

   private int calculateLevel(int experience) {
      if (experience >= 50) {
         return 4;
      } else if (experience >= 40) {
         return 3;
      } else {
         return experience >= 20 ? 2 : 1;
      }
   }

   private int calculateLevelProgress(int experience) {
      if (experience >= 50) {
         return 4;
      } else if (experience >= 40) {
         return 3;
      } else if (experience >= 20) {
         return 2;
      } else {
         return experience >= 0 ? 1 : 0;
      }
   }

   public boolean method_5679(DamageSource damageSource) {
      return damageSource.method_5529() instanceof ZombieEntity ? true : super.method_5679(damageSource);
   }
}
