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

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
         if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.pass(user.getStackInHand(hand));
         }

         ItemStack mainHand = serverPlayer.getMainHandStack();
         ItemStack offHand = serverPlayer.getOffHandStack();
         ItemStack photoStack = ItemStack.EMPTY;
         if (PhotoItem.isNamedPhoto(mainHand) && hand == Hand.MAIN_HAND) {
            photoStack = mainHand;
         } else if (PhotoItem.isNamedPhoto(offHand) && hand == Hand.OFF_HAND) {
            photoStack = offHand;
         } else if (PhotoItem.isNamedPhoto(offHand)) {
            photoStack = offHand;
         } else if (PhotoItem.isNamedPhoto(mainHand)) {
            photoStack = mainHand;
         }

         if (!photoStack.isEmpty()) {
            String targetName = photoStack.getName().getString();
            ServerPlayerEntity targetPlayer = world.getServer().getPlayerManager().getPlayer(targetName);
            if (targetPlayer != null && !targetPlayer.isDead()) {
               targetPlayer.damage(ModDamageSources.ghost(world), 5500.0F);
               targetPlayer.sendMessage(Text.translatable("message.smfs.photo_killed"), true);
               serverPlayer.sendMessage(Text.translatable("message.smfs.photo_kill_success"), true);
               world.playSound(
                  null,
                  serverPlayer.getX(),
                  serverPlayer.getY(),
                  serverPlayer.getZ(),
                  SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
                  SoundCategory.PLAYERS,
                  1.0F,
                  1.0F
               );
               photoStack.decrement(1);
               user.getItemCooldownManager().set(this, 600);
               return TypedActionResult.success(user.getStackInHand(hand));
            }

            serverPlayer.sendMessage(Text.translatable("message.smfs.photo_target_not_found"), true);
         } else {
            boolean foundChasingGhost = false;

            for (GhostEntity ghost : world.getEntitiesByClass(
               GhostEntity.class, serverPlayer.getBoundingBox().expand(48.0), ghostx -> ghostx.isChasing() && ghostx.getChaseTarget() == serverPlayer
            )) {
               ghost.setChasing(false);
               ghost.setChaseTarget(null);
               ghost.setChaseTimer(0);
               ghost.setTarget(null);
               foundChasingGhost = true;
               world.playSound(null, ghost.getX(), ghost.getY(), ghost.getZ(), SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.HOSTILE, 1.0F, 1.0F);
            }

            boolean foundBeckoningBride = false;

            for (GanshiBrideGhostEntity bride : world.getEntitiesByClass(
               GanshiBrideGhostEntity.class,
               serverPlayer.getBoundingBox().expand(48.0),
               bridex -> bridex.isInBeckoningPhase() && bridex.getCurrentTarget() == serverPlayer
            )) {
               bride.cancelBeckoningPhase();
               foundBeckoningBride = true;
               world.playSound(null, bride.getX(), bride.getY(), bride.getZ(), SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.HOSTILE, 1.0F, 1.0F);
            }

            if (foundChasingGhost || foundBeckoningBride) {
               serverPlayer.sendMessage(Text.translatable("message.smfs.ghost_scissors.chasing_cleared"), true);
               world.playSound(
                  null,
                  serverPlayer.getX(),
                  serverPlayer.getY(),
                  serverPlayer.getZ(),
                  SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
                  SoundCategory.PLAYERS,
                  1.0F,
                  1.0F
               );
               user.getItemCooldownManager().set(this, 300);
               return TypedActionResult.success(user.getStackInHand(hand));
            }

            serverPlayer.sendMessage(Text.translatable("message.smfs.photo_required"), true);
         }
      }

      return TypedActionResult.pass(user.getStackInHand(hand));
   }

   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.ghost_scissors.description.source"));
      tooltip.add(Text.translatable("item.smfs.ghost_scissors.description.desc"));
      tooltip.add(Text.translatable("item.smfs.ghost_scissors.description.type"));
      tooltip.add(Text.translatable("item.smfs.ghost_scissors.effect.remote_attack"));
      tooltip.add(Text.translatable("item.smfs.ghost_scissors.effect.clear_chase"));
      tooltip.add(Text.empty());
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_bonus", new Object[]{5500.0F}));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_multiplier", new Object[]{0.0F}));
   }
}
