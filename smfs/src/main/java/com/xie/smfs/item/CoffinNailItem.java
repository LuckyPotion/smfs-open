package com.xie.smfs.item;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.entity.ghost.LuoQianGhostEntity;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CoffinNailItem extends Item {
   public CoffinNailItem(Settings settings) {
      super(settings);
   }

   public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
      World world = user.getWorld();
      if (entity instanceof GhostEntity ghost) {
         if (!world.isClient) {
            if (ghost instanceof LuoQianGhostEntity) {
               user.sendMessage(Text.literal("§c无法将棺材钉插入罗千厉鬼！"), true);
               user.sendMessage(Text.literal("§c罗千：哦？想用我的东西来对付我。"), false);
               return ActionResult.SUCCESS;
            }

            if (!ghost.isSuppressed()) {
               this.applySuppressionToGhost(ghost, user, stack);
               world.playSound(null, ghost.getX(), ghost.getY(), ghost.getZ(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.NEUTRAL, 1.0F, 1.0F);
               return ActionResult.SUCCESS;
            }
         }

         return ActionResult.SUCCESS;
      } else if (entity instanceof GhostMasterEntity ghostMaster) {
         if (!world.isClient && !this.isGhostMasterSuppressed(ghostMaster)) {
            this.applySuppressionToGhostMaster(ghostMaster, user, stack);
            world.playSound(null, ghostMaster.getX(), ghostMaster.getY(), ghostMaster.getZ(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            return ActionResult.SUCCESS;
         } else {
            return ActionResult.SUCCESS;
         }
      } else {
         return ActionResult.PASS;
      }
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      return TypedActionResult.pass(user.getStackInHand(hand));
   }

   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.coffin_nail.description.source"));
      tooltip.add(Text.translatable("item.smfs.coffin_nail.description.desc"));
      tooltip.add(Text.translatable("item.smfs.coffin_nail.description.type"));
   }

   private boolean canSuppressGhost(GhostEntity ghost) {
      return ghost != null && !ghost.isSuppressed();
   }

   private void applySuppressionToGhost(GhostEntity ghost, PlayerEntity player, ItemStack stack) {
      if (!ghost.isDeadlocked() && !ghost.hasCoffinNail()) {
         ghost.setDeadlocked(true);
         ghost.setSuppressed(true);
         ghost.disableGhostDomain();
         ghost.disableKillingRules();
         ItemStack nailCopy = stack.copy();
         stack.decrement(1);
         ghost.setCoffinNail(nailCopy);
      }
   }

   private boolean isGhostMasterSuppressed(GhostMasterEntity ghostMaster) {
      return ghostMaster != null && ghostMaster.isSuppressed();
   }

   private void applySuppressionToGhostMaster(GhostMasterEntity ghostMaster, PlayerEntity player, ItemStack stack) {
      if (!ghostMaster.isDeadlocked() && !ghostMaster.hasCoffinNail()) {
         ghostMaster.setDeadlocked(true);
         ghostMaster.setSuppressed(true);
         ghostMaster.disableGhostDomain();
         ItemStack nailCopy = stack.copy();
         stack.decrement(1);
         ghostMaster.setCoffinNail(nailCopy);
      }
   }
}
