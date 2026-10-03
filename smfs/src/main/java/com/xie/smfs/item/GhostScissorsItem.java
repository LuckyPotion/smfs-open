package com.xie.smfs.item;

import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.ghost.GanshiBrideGhostEntity;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class GhostScissorsItem extends Item {
   private static final int COOLDOWN_TICKS = 600;

   public GhostScissorsItem(Settings settings) {
      super(settings);
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      if (!world.field_9236 && user instanceof ServerPlayerEntity serverPlayer) {
         if (user.method_7357().method_7904(this)) {
            return TypedActionResult.method_22430(user.method_5998(hand));
         }

         ItemStack mainHand = serverPlayer.method_6047();
         ItemStack offHand = serverPlayer.method_6079();
         ItemStack photoStack = ItemStack.field_8037;
         if (PhotoItem.isNamedPhoto(mainHand) && hand == Hand.field_5808) {
            photoStack = mainHand;
         } else if (PhotoItem.isNamedPhoto(offHand) && hand == Hand.field_5810) {
            photoStack = offHand;
         } else if (PhotoItem.isNamedPhoto(offHand)) {
            photoStack = offHand;
         } else if (PhotoItem.isNamedPhoto(mainHand)) {
            photoStack = mainHand;
         }

         if (!photoStack.method_7960()) {
            String targetName = photoStack.method_7964().getString();
            ServerPlayerEntity targetPlayer = world.method_8503().method_3760().method_14566(targetName);
            if (targetPlayer != null && !targetPlayer.method_29504()) {
               targetPlayer.method_5643(ModDamageSources.ghost(world), 5500.0F);
               targetPlayer.method_7353(Text.method_43471("message.smfs.photo_killed"), true);
               serverPlayer.method_7353(Text.method_43471("message.smfs.photo_kill_success"), true);
               world.method_43128(
                  null,
                  serverPlayer.method_23317(),
                  serverPlayer.method_23318(),
                  serverPlayer.method_23321(),
                  SoundEvents.field_14545,
                  SoundCategory.field_15248,
                  1.0F,
                  1.0F
               );
               photoStack.method_7934(1);
               user.method_7357().method_7906(this, 600);
               return TypedActionResult.method_22427(user.method_5998(hand));
            }

            serverPlayer.method_7353(Text.method_43471("message.smfs.photo_target_not_found"), true);
         } else {
            boolean foundChasingGhost = false;

            for (GhostEntity ghost : world.method_8390(
               GhostEntity.class, serverPlayer.method_5829().method_1014(48.0), ghostx -> ghostx.isChasing() && ghostx.getChaseTarget() == serverPlayer
            )) {
               ghost.setChasing(false);
               ghost.setChaseTarget(null);
               ghost.setChaseTimer(0);
               ghost.method_5980(null);
               foundChasingGhost = true;
               world.method_43128(
                  null, ghost.method_23317(), ghost.method_23318(), ghost.method_23321(), SoundEvents.field_14941, SoundCategory.field_15251, 1.0F, 1.0F
               );
            }

            boolean foundBeckoningBride = false;

            for (GanshiBrideGhostEntity bride : world.method_8390(
               GanshiBrideGhostEntity.class,
               serverPlayer.method_5829().method_1014(48.0),
               bridex -> bridex.isInBeckoningPhase() && bridex.getCurrentTarget() == serverPlayer
            )) {
               bride.cancelBeckoningPhase();
               foundBeckoningBride = true;
               world.method_43128(
                  null, bride.method_23317(), bride.method_23318(), bride.method_23321(), SoundEvents.field_14941, SoundCategory.field_15251, 1.0F, 1.0F
               );
            }

            if (foundChasingGhost || foundBeckoningBride) {
               serverPlayer.method_7353(Text.method_43471("message.smfs.ghost_scissors.chasing_cleared"), true);
               world.method_43128(
                  null,
                  serverPlayer.method_23317(),
                  serverPlayer.method_23318(),
                  serverPlayer.method_23321(),
                  SoundEvents.field_14545,
                  SoundCategory.field_15248,
                  1.0F,
                  1.0F
               );
               user.method_7357().method_7906(this, 300);
               return TypedActionResult.method_22427(user.method_5998(hand));
            }

            serverPlayer.method_7353(Text.method_43471("message.smfs.photo_required"), true);
         }
      }

      return TypedActionResult.method_22430(user.method_5998(hand));
   }

   public void method_7851(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.ghost_scissors.description.source"));
      tooltip.add(Text.method_43471("item.smfs.ghost_scissors.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.ghost_scissors.description.type"));
      tooltip.add(Text.method_43471("item.smfs.ghost_scissors.effect.remote_attack"));
      tooltip.add(Text.method_43471("item.smfs.ghost_scissors.effect.clear_chase"));
      tooltip.add(Text.method_43473());
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_bonus", new Object[]{5500.0F}));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_multiplier", new Object[]{0.0F}));
   }
}
