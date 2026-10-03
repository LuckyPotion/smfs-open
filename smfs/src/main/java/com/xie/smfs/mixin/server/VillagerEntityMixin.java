package com.xie.smfs.mixin.server;

import com.xie.smfs.registry.ModItems;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {
   @Inject(method = "fillRecipes", at = @At("TAIL"))
   private void addGoldTrades(CallbackInfo ci) {
      VillagerEntity villager = (VillagerEntity)this;
      VillagerProfession profession = villager.method_7231().method_16924();
      this.addGoldTradesForProfession(villager, profession, villager.method_6051());
   }

   @Unique
   private void addGoldTradesForProfession(VillagerEntity villager, VillagerProfession profession, Random random) {
      TradeOfferList offers = villager.method_8264();
      int level = villager.method_7231().method_16925();
      switch (profession.toString()) {
         case "armorer":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8620, 8), 4, 6, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8695, 4), 2, 5, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_8477, 3), 6, 4, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_22020, 1), 5, 3, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_22020, 2), 10, 2, random);
            }
            break;
         case "butcher":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8046, 16), 3, 8, random);
            }

            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8389, 16), 3, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8748, 16), 3, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8726, 16), 3, 8, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_8504, 16), 3, 8, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_8176, 8), 3, 6, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_8261, 8), 3, 6, random);
            }
            break;
         case "cartographer":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8407, 32), 3, 12, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8895, 8), 5, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_8251, 4), 4, 4, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_8557, 2), 5, 3, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_8449, 1), 6, 2, random);
            }
            break;
         case "cleric":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8511, 32), 3, 12, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8725, 16), 3, 8, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_8759, 16), 3, 8, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_8071, 8), 3, 6, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_8070, 2), 3, 3, random);
            }
            break;
         case "farmer":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8861, 32), 3, 12, random);
            }

            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8567, 32), 3, 12, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8179, 32), 3, 12, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8186, 32), 3, 12, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_17518, 8), 2, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_17522, 8), 2, 6, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_17528, 4), 4, 5, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_8071, 4), 5, 3, random);
            }
            break;
         case "fisherman":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8429, 16), 3, 8, random);
            }

            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8209, 16), 3, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8846, 4), 2, 6, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8323, 4), 2, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_8373, 8), 2, 6, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_8509, 8), 2, 6, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_8864, 2), 5, 3, random);
            }
            break;
         case "fletcher":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8600, 32), 1, 12, random);
            }

            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8145, 16), 2, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8153, 16), 1, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8107, 8), 2, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_8236, 4), 3, 5, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_8087, 4), 4, 4, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_8102, 1), 6, 3, random);
            }
            break;
         case "leatherworker":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8745, 16), 5, 8, random);
            }

            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8245, 16), 5, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8175, 2), 4, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_18138, 1), 3, 5, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_8370, 2), 4, 4, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_8577, 1), 6, 3, random);
            }
            break;
         case "librarian":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8407, 32), 3, 12, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8794, 16), 2, 8, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_8529, 8), 3, 6, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_28410, 8), 3, 6, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_8598, 1), 6, 3, random);
            }
            break;
         case "mason":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8696, 16), 3, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8155, 8), 2, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_8260, 8), 2, 6, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_8801, 4), 3, 5, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_8305, 2), 5, 3, random);
            }
            break;
         case "shepherd":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_19044, 16), 3, 8, random);
            }

            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8276, 16), 1, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8850, 8), 2, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_8868, 2), 3, 5, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_8258, 2), 4, 4, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_8539, 1), 6, 3, random);
            }
            break;
         case "toolsmith":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8620, 8), 4, 6, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8695, 4), 2, 5, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_8477, 3), 6, 4, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_22020, 1), 6, 3, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_22020, 2), 12, 2, random);
            }
            break;
         case "weaponsmith":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8620, 8), 3, 6, random);
            }

            if (level >= 2) {
               this.addGoldForItemTrade(offers, 36, new ItemStack(ModItems.GOLDEN_PISTOL, 1), 3, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8695, 4), 2, 5, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_8477, 3), 6, 4, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_22020, 1), 8, 3, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_22020, 2), 18, 2, random);
            }
            break;
         case "nitwit":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8635, 16), 1, 6, random);
            }

            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8511, 16), 1, 6, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8680, 8), 1, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_8711, 4), 2, 5, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_8766, 2), 3, 4, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_8613, 1), 5, 3, random);
            }
            break;
         default:
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.field_8687, 4), 6, 6, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.field_8687, 2), 2, 5, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.field_8687, 1), 3, 4, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.field_8477, 1), 4, 3, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.field_22020, 1), 6, 2, random);
            }
      }
   }

   @Unique
   private void addTrade(TradeOfferList offers, ItemStack inputItem, int goldAmount, int maxUses, Random random) {
      ItemStack goldOutput = new ItemStack(Items.field_8695, goldAmount);
      TradeOffer trade = new TradeOffer(inputItem, goldOutput, maxUses, 2, 0.05F);
      offers.add(trade);
   }

   @Unique
   private void addGoldForItemTrade(TradeOfferList offers, int goldAmount, ItemStack outputItem, int maxUses, Random random) {
      ItemStack goldInput = new ItemStack(Items.field_8695, goldAmount);
      TradeOffer trade = new TradeOffer(goldInput, outputItem, maxUses, 2, 0.05F);
      offers.add(trade);
   }
}
