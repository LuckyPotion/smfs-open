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
      VillagerProfession profession = villager.getVillagerData().getProfession();
      this.addGoldTradesForProfession(villager, profession, villager.getRandom());
   }

   @Unique
   private void addGoldTradesForProfession(VillagerEntity villager, VillagerProfession profession, Random random) {
      TradeOfferList offers = villager.getOffers();
      int level = villager.getVillagerData().getLevel();
      switch (profession.toString()) {
         case "armorer":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.IRON_INGOT, 8), 4, 6, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.GOLD_INGOT, 4), 2, 5, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.DIAMOND, 3), 6, 4, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.NETHERITE_INGOT, 1), 5, 3, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.NETHERITE_INGOT, 2), 10, 2, random);
            }
            break;
         case "butcher":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.BEEF, 16), 3, 8, random);
            }

            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.PORKCHOP, 16), 3, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.MUTTON, 16), 3, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.CHICKEN, 16), 3, 8, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.RABBIT, 16), 3, 8, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.COOKED_BEEF, 8), 3, 6, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.COOKED_PORKCHOP, 8), 3, 6, random);
            }
            break;
         case "cartographer":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.PAPER, 32), 3, 12, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.MAP, 8), 5, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.COMPASS, 4), 4, 4, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.CLOCK, 2), 5, 3, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.ENDER_EYE, 1), 6, 2, random);
            }
            break;
         case "cleric":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.ROTTEN_FLESH, 32), 3, 12, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.REDSTONE, 16), 3, 8, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.LAPIS_LAZULI, 16), 3, 8, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.GOLDEN_CARROT, 8), 3, 6, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.GHAST_TEAR, 2), 3, 3, random);
            }
            break;
         case "farmer":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.WHEAT, 32), 3, 12, random);
            }

            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.POTATO, 32), 3, 12, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.CARROT, 32), 3, 12, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.BEETROOT, 32), 3, 12, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.PUMPKIN, 8), 2, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.MELON, 8), 2, 6, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.HAY_BLOCK, 4), 4, 5, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.GOLDEN_CARROT, 4), 5, 3, random);
            }
            break;
         case "fisherman":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.COD, 16), 3, 8, random);
            }

            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.SALMON, 16), 3, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.TROPICAL_FISH, 4), 2, 6, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.PUFFERFISH, 4), 2, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.COOKED_COD, 8), 2, 6, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.COOKED_SALMON, 8), 2, 6, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.NAUTILUS_SHELL, 2), 5, 3, random);
            }
            break;
         case "fletcher":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.STICK, 32), 1, 12, random);
            }

            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.FLINT, 16), 2, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.FEATHER, 16), 1, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.ARROW, 8), 2, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.SPECTRAL_ARROW, 4), 3, 5, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.TIPPED_ARROW, 4), 4, 4, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.BOW, 1), 6, 3, random);
            }
            break;
         case "leatherworker":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.LEATHER, 16), 5, 8, random);
            }

            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.RABBIT_HIDE, 16), 5, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.SADDLE, 2), 4, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.LEATHER_HORSE_ARMOR, 1), 3, 5, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.LEATHER_BOOTS, 2), 4, 4, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.LEATHER_CHESTPLATE, 1), 6, 3, random);
            }
            break;
         case "librarian":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.PAPER, 32), 3, 12, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.INK_SAC, 16), 2, 8, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.BOOK, 8), 3, 6, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.GLOW_INK_SAC, 8), 3, 6, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.ENCHANTED_BOOK, 1), 6, 3, random);
            }
            break;
         case "mason":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.CLAY_BALL, 16), 3, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.QUARTZ, 8), 2, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.TERRACOTTA, 8), 2, 6, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.GLOWSTONE, 4), 3, 5, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.SEA_LANTERN, 2), 5, 3, random);
            }
            break;
         case "shepherd":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.WHITE_WOOL, 16), 3, 8, random);
            }

            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.STRING, 16), 1, 8, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.WHITE_CARPET, 8), 2, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.SHEARS, 2), 3, 5, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.WHITE_BED, 2), 4, 4, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.WHITE_BANNER, 1), 6, 3, random);
            }
            break;
         case "toolsmith":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.IRON_INGOT, 8), 4, 6, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.GOLD_INGOT, 4), 2, 5, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.DIAMOND, 3), 6, 4, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.NETHERITE_INGOT, 1), 6, 3, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.NETHERITE_INGOT, 2), 12, 2, random);
            }
            break;
         case "weaponsmith":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.IRON_INGOT, 8), 3, 6, random);
            }

            if (level >= 2) {
               this.addGoldForItemTrade(offers, 36, new ItemStack(ModItems.GOLDEN_PISTOL, 1), 3, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.GOLD_INGOT, 4), 2, 5, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.DIAMOND, 3), 6, 4, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.NETHERITE_INGOT, 1), 8, 3, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.NETHERITE_INGOT, 2), 18, 2, random);
            }
            break;
         case "nitwit":
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.POISONOUS_POTATO, 16), 1, 6, random);
            }

            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.ROTTEN_FLESH, 16), 1, 6, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.SPIDER_EYE, 8), 1, 6, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.FERMENTED_SPIDER_EYE, 4), 2, 5, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.SUSPICIOUS_STEW, 2), 3, 4, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.DRAGON_BREATH, 1), 5, 3, random);
            }
            break;
         default:
            if (level >= 1) {
               this.addTrade(offers, new ItemStack(Items.EMERALD, 4), 6, 6, random);
            }

            if (level >= 2) {
               this.addTrade(offers, new ItemStack(Items.EMERALD, 2), 2, 5, random);
            }

            if (level >= 3) {
               this.addTrade(offers, new ItemStack(Items.EMERALD, 1), 3, 4, random);
            }

            if (level >= 4) {
               this.addTrade(offers, new ItemStack(Items.DIAMOND, 1), 4, 3, random);
            }

            if (level >= 5) {
               this.addTrade(offers, new ItemStack(Items.NETHERITE_INGOT, 1), 6, 2, random);
            }
      }
   }

   @Unique
   private void addTrade(TradeOfferList offers, ItemStack inputItem, int goldAmount, int maxUses, Random random) {
      ItemStack goldOutput = new ItemStack(Items.GOLD_INGOT, goldAmount);
      TradeOffer trade = new TradeOffer(inputItem, goldOutput, maxUses, 2, 0.05F);
      offers.add(trade);
   }

   @Unique
   private void addGoldForItemTrade(TradeOfferList offers, int goldAmount, ItemStack outputItem, int maxUses, Random random) {
      ItemStack goldInput = new ItemStack(Items.GOLD_INGOT, goldAmount);
      TradeOffer trade = new TradeOffer(goldInput, outputItem, maxUses, 2, 0.05F);
      offers.add(trade);
   }
}
