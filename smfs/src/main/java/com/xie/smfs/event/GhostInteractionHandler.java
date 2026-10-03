package com.xie.smfs.event;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.entity.ghost.QiaomenGhostEntity;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class GhostInteractionHandler implements UseEntityCallback {
   public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
      if (world.isClient) {
         return ActionResult.PASS;
      }

      if (entity instanceof GhostEntity ghost && ghost.hasCoffinNail() && player.getStackInHand(hand).isEmpty()) {
         ItemStack coffinNail = ghost.getCoffinNail();
         ghost.setCoffinNail(null);
         ghost.setSuppressed(false);
         ghost.enableGhostDomain();
         ghost.enableKillingRules();
         if (player.getAbilities().creativeMode) {
            player.giveItemStack(new ItemStack(coffinNail.getItem(), 1));
         } else if (!player.getInventory().insertStack(coffinNail)) {
            entity.dropStack(coffinNail, 0.5F);
         }

         player.playSound(SoundEvent.of(Identifier.of("minecraft", "entity.item.pickup")), 1.0F, 1.0F);
         return ActionResult.SUCCESS;
      } else if (entity instanceof PlayerGhostEntity playerGhost
         && playerGhost.hasCoffinNail()
         && player.getStackInHand(hand).isEmpty()
         && playerGhost.retrieveCoffinNail()) {
         player.playSound(SoundEvent.of(Identifier.of("minecraft", "entity.item.pickup")), 1.0F, 1.0F);
         return ActionResult.SUCCESS;
      } else if (entity instanceof GhostMasterEntity ghostMaster
         && ghostMaster.hasCoffinNail()
         && player.getStackInHand(hand).isEmpty()
         && ghostMaster.retrieveCoffinNail()) {
         player.playSound(SoundEvent.of(Identifier.of("minecraft", "entity.item.pickup")), 1.0F, 1.0F);
         return ActionResult.SUCCESS;
      } else {
         if (entity instanceof GhostEntity || entity instanceof PlayerGhostEntity) {
            ItemStack mainHand = player.getMainHandStack();
            ItemStack offHand = player.getOffHandStack();
            if (mainHand.getItem() == Items.GLASS_BOTTLE || offHand.getItem() == Items.GLASS_BOTTLE) {
               ItemStack corpseOil = new ItemStack(ModItems.CORPSE_OIL);
               if (!player.getInventory().insertStack(corpseOil)) {
                  player.dropItem(corpseOil, false);
               }

               if (mainHand.getItem() == Items.GLASS_BOTTLE) {
                  mainHand.decrement(1);
               } else {
                  offHand.decrement(1);
               }

               return ActionResult.SUCCESS;
            }

            if (mainHand.getItem() instanceof SwordItem && mainHand.hasNbt() && mainHand.getNbt().contains("corpse_oil_layers")) {
               ItemStack corpsePiece = new ItemStack(ModItems.CORPSE_PIECE);
               if (!player.getInventory().insertStack(corpsePiece)) {
                  player.dropItem(corpsePiece, false);
               }

               ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
               if (!player.getInventory().insertStack(glassBottle)) {
                  player.dropItem(glassBottle, false);
               }

               int layers = mainHand.getNbt().getInt("corpse_oil_layers");
               if (layers > 0) {
                  mainHand.getNbt().putInt("corpse_oil_layers", --layers);
                  if (layers == 0) {
                     mainHand.getNbt().remove("corpse_oil_layers");
                  }
               }

               return ActionResult.SUCCESS;
            }
         }

         if (entity instanceof QiaomenGhostEntity qiaomenGhost) {
            if (!player.getStackInHand(hand).isEmpty() && player.getStackInHand(hand).getItem() == ModItems.COFFIN_NAIL) {
               return ActionResult.PASS;
            } else {
               return !player.getStackInHand(hand).isEmpty() && player.getStackInHand(hand).getItem() == ModItems.GOLDEN_CONTAINER
                  ? ActionResult.PASS
                  : this.handleQiaomenGhostInteraction(player, hand, qiaomenGhost);
            }
         } else {
            return ActionResult.PASS;
         }
      }
   }

   private ActionResult handleQiaomenGhostInteraction(PlayerEntity player, Hand hand, QiaomenGhostEntity qiaomenGhost) {
      if (qiaomenGhost.hasItem() && qiaomenGhost.getWorld().getRandom().nextFloat() < 0.2F) {
         qiaomenGhost.setHasItem(false);
         this.giveGhostPostOfficeMysteryInfo(player);
      }

      this.giveSpiritErosionEffect(player);
      return ActionResult.SUCCESS;
   }

   private void giveGhostPostOfficeMysteryInfo(PlayerEntity player) {
      ItemStack itemStack = new ItemStack(ModItems.GHOST_POST_OFFICE_MYSTERY_INFO);
      boolean addedToInventory = player.getInventory().insertStack(itemStack);
      if (!addedToInventory) {
         player.dropItem(itemStack, false);
      }

      player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0F, 1.0F);
   }

   private void giveSpiritErosionEffect(PlayerEntity player) {
      StatusEffectInstance spiritErosionEffect = new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 1200, 9, false, true, true);
      player.addStatusEffect(spiritErosionEffect);
   }
}
