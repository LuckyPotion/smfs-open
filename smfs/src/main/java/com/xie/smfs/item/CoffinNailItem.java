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

   public ActionResult method_7847(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
      World world = user.method_37908();
      if (entity instanceof GhostEntity ghost) {
         if (!world.field_9236) {
            if (ghost instanceof LuoQianGhostEntity) {
               user.method_7353(Text.method_43470("§c无法将棺材钉插入罗千厉鬼！"), true);
               user.method_7353(Text.method_43470("§c罗千：哦？想用我的东西来对付我。"), false);
               return ActionResult.field_5812;
            }

            if (!ghost.isSuppressed()) {
               this.applySuppressionToGhost(ghost, user, stack);
               world.method_43128(
                  null, ghost.method_23317(), ghost.method_23318(), ghost.method_23321(), SoundEvents.field_14931, SoundCategory.field_15254, 1.0F, 1.0F
               );
               return ActionResult.field_5812;
            }
         }

         return ActionResult.field_5812;
      } else if (entity instanceof GhostMasterEntity ghostMaster) {
         if (!world.field_9236 && !this.isGhostMasterSuppressed(ghostMaster)) {
            this.applySuppressionToGhostMaster(ghostMaster, user, stack);
            world.method_43128(
               null,
               ghostMaster.method_23317(),
               ghostMaster.method_23318(),
               ghostMaster.method_23321(),
               SoundEvents.field_14931,
               SoundCategory.field_15254,
               1.0F,
               1.0F
            );
            return ActionResult.field_5812;
         } else {
            return ActionResult.field_5812;
         }
      } else {
         return ActionResult.field_5811;
      }
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      return TypedActionResult.method_22430(user.method_5998(hand));
   }

   public void method_7851(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.coffin_nail.description.source"));
      tooltip.add(Text.method_43471("item.smfs.coffin_nail.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.coffin_nail.description.type"));
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
         ItemStack nailCopy = stack.method_7972();
         stack.method_7934(1);
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
         ItemStack nailCopy = stack.method_7972();
         stack.method_7934(1);
         ghostMaster.setCoffinNail(nailCopy);
      }
   }
}
