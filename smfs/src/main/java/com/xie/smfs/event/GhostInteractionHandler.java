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
      if (world.field_9236) {
         return ActionResult.field_5811;
      }

      if (entity instanceof GhostEntity ghost && ghost.hasCoffinNail() && player.method_5998(hand).method_7960()) {
         ItemStack coffinNail = ghost.getCoffinNail();
         ghost.setCoffinNail(null);
         ghost.setSuppressed(false);
         ghost.enableGhostDomain();
         ghost.enableKillingRules();
         if (player.method_31549().field_7477) {
            player.method_7270(new ItemStack(coffinNail.method_7909(), 1));
         } else if (!player.method_31548().method_7394(coffinNail)) {
            entity.method_5699(coffinNail, 0.5F);
         }

         player.method_5783(SoundEvent.method_47908(Identifier.method_43902("minecraft", "entity.item.pickup")), 1.0F, 1.0F);
         return ActionResult.field_5812;
      } else if (entity instanceof PlayerGhostEntity playerGhost
         && playerGhost.hasCoffinNail()
         && player.method_5998(hand).method_7960()
         && playerGhost.retrieveCoffinNail()) {
         player.method_5783(SoundEvent.method_47908(Identifier.method_43902("minecraft", "entity.item.pickup")), 1.0F, 1.0F);
         return ActionResult.field_5812;
      } else if (entity instanceof GhostMasterEntity ghostMaster
         && ghostMaster.hasCoffinNail()
         && player.method_5998(hand).method_7960()
         && ghostMaster.retrieveCoffinNail()) {
         player.method_5783(SoundEvent.method_47908(Identifier.method_43902("minecraft", "entity.item.pickup")), 1.0F, 1.0F);
         return ActionResult.field_5812;
      } else {
         if (entity instanceof GhostEntity || entity instanceof PlayerGhostEntity) {
            ItemStack mainHand = player.method_6047();
            ItemStack offHand = player.method_6079();
            if (mainHand.method_7909() == Items.field_8469 || offHand.method_7909() == Items.field_8469) {
               ItemStack corpseOil = new ItemStack(ModItems.CORPSE_OIL);
               if (!player.method_31548().method_7394(corpseOil)) {
                  player.method_7328(corpseOil, false);
               }

               if (mainHand.method_7909() == Items.field_8469) {
                  mainHand.method_7934(1);
               } else {
                  offHand.method_7934(1);
               }

               return ActionResult.field_5812;
            }

            if (mainHand.method_7909() instanceof SwordItem && mainHand.method_7985() && mainHand.method_7969().method_10545("corpse_oil_layers")) {
               ItemStack corpsePiece = new ItemStack(ModItems.CORPSE_PIECE);
               if (!player.method_31548().method_7394(corpsePiece)) {
                  player.method_7328(corpsePiece, false);
               }

               ItemStack glassBottle = new ItemStack(Items.field_8469);
               if (!player.method_31548().method_7394(glassBottle)) {
                  player.method_7328(glassBottle, false);
               }

               int layers = mainHand.method_7969().method_10550("corpse_oil_layers");
               if (layers > 0) {
                  mainHand.method_7969().method_10569("corpse_oil_layers", --layers);
                  if (layers == 0) {
                     mainHand.method_7969().method_10551("corpse_oil_layers");
                  }
               }

               return ActionResult.field_5812;
            }
         }

         if (entity instanceof QiaomenGhostEntity qiaomenGhost) {
            if (!player.method_5998(hand).method_7960() && player.method_5998(hand).method_7909() == ModItems.COFFIN_NAIL) {
               return ActionResult.field_5811;
            } else {
               return !player.method_5998(hand).method_7960() && player.method_5998(hand).method_7909() == ModItems.GOLDEN_CONTAINER
                  ? ActionResult.field_5811
                  : this.handleQiaomenGhostInteraction(player, hand, qiaomenGhost);
            }
         } else {
            return ActionResult.field_5811;
         }
      }
   }

   private ActionResult handleQiaomenGhostInteraction(PlayerEntity player, Hand hand, QiaomenGhostEntity qiaomenGhost) {
      if (qiaomenGhost.hasItem() && qiaomenGhost.method_37908().method_8409().method_43057() < 0.2F) {
         qiaomenGhost.setHasItem(false);
         this.giveGhostPostOfficeMysteryInfo(player);
      }

      this.giveSpiritErosionEffect(player);
      return ActionResult.field_5812;
   }

   private void giveGhostPostOfficeMysteryInfo(PlayerEntity player) {
      ItemStack itemStack = new ItemStack(ModItems.GHOST_POST_OFFICE_MYSTERY_INFO);
      boolean addedToInventory = player.method_31548().method_7394(itemStack);
      if (!addedToInventory) {
         player.method_7328(itemStack, false);
      }

      player.method_5783(SoundEvents.field_15197, 1.0F, 1.0F);
   }

   private void giveSpiritErosionEffect(PlayerEntity player) {
      StatusEffectInstance spiritErosionEffect = new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 1200, 9, false, true, true);
      player.method_6092(spiritErosionEffect);
   }
}
