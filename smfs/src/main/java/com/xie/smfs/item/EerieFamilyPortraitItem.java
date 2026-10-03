package com.xie.smfs.item;

import com.xie.smfs.data.PlayerRoyalCurseManager;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EerieFamilyPortraitItem extends Item {
   private static final Logger LOGGER = LoggerFactory.getLogger(EerieFamilyPortraitItem.class);

   public EerieFamilyPortraitItem(Settings settings) {
      super(settings.method_7889(1));
   }

   public Text method_7864(ItemStack stack) {
      return Text.method_43471("item.smfs.eerie_family_portrait");
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity player, Hand hand) {
      if (world.field_9236) {
         return TypedActionResult.method_22430(player.method_5998(hand));
      }

      ItemStack stack = player.method_5998(hand);
      if (player instanceof ServerPlayerEntity serverPlayer) {
         if (PlayerRoyalCurseManager.hasRoyalCurseUnlocked(serverPlayer)) {
            player.method_7353(Text.method_43471("item.smfs.eerie_family_portrait.already_unlocked"), true);
            return TypedActionResult.method_22431(stack);
         } else if (PlayerRoyalCurseManager.unlockRoyalCurse(serverPlayer)) {
            this.playUnlockEffects(serverPlayer);
            player.method_7353(Text.method_43471("item.smfs.eerie_family_portrait.unlock_success"), true);
            stack.method_7934(1);
            return TypedActionResult.method_22427(stack);
         } else {
            LOGGER.error("玩家 {} 解锁王家诅咒失败", serverPlayer.method_7334().getName());
            player.method_7353(Text.method_43471("item.smfs.eerie_family_portrait.unlock_failed"), true);
            return TypedActionResult.method_22431(stack);
         }
      } else {
         return TypedActionResult.method_22430(stack);
      }
   }

   public void method_7851(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.eerie_family_portrait.description.source"));
      tooltip.add(Text.method_43471("item.smfs.eerie_family_portrait.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.eerie_family_portrait.description.type"));
   }

   private void playUnlockEffects(ServerPlayerEntity player) {
      player.method_5783(SoundEvents.field_14967, 1.0F, 0.5F);
      if (player.method_37908() instanceof ServerWorld serverWorld) {
         ServerWorld world = serverWorld;

         for (int i = 0; i < 20; i++) {
            double offsetX = (world.field_9229.method_43058() - 0.5) * 4.0;
            double offsetY = world.field_9229.method_43058() * 2.0;
            double offsetZ = (world.field_9229.method_43058() - 0.5) * 4.0;
            world.method_14199(
               ParticleTypes.field_11251,
               player.method_23317() + offsetX,
               player.method_23318() + offsetY,
               player.method_23321() + offsetZ,
               3,
               0.1,
               0.1,
               0.1,
               0.02
            );
         }
      }
   }
}
