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
      super(settings.maxCount(1));
   }

   public Text getName(ItemStack stack) {
      return Text.translatable("item.smfs.eerie_family_portrait");
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      if (world.isClient) {
         return TypedActionResult.pass(user.getStackInHand(hand));
      }

      ItemStack stack = user.getStackInHand(hand);
      if (user instanceof ServerPlayerEntity serverPlayer) {
         if (PlayerRoyalCurseManager.hasRoyalCurseUnlocked(serverPlayer)) {
            user.sendMessage(Text.translatable("item.smfs.eerie_family_portrait.already_unlocked"), true);
            return TypedActionResult.fail(stack);
         } else if (PlayerRoyalCurseManager.unlockRoyalCurse(serverPlayer)) {
            this.playUnlockEffects(serverPlayer);
            user.sendMessage(Text.translatable("item.smfs.eerie_family_portrait.unlock_success"), true);
            stack.decrement(1);
            return TypedActionResult.success(stack);
         } else {
            LOGGER.error("玩家 {} 解锁王家诅咒失败", serverPlayer.getGameProfile().getName());
            user.sendMessage(Text.translatable("item.smfs.eerie_family_portrait.unlock_failed"), true);
            return TypedActionResult.fail(stack);
         }
      } else {
         return TypedActionResult.pass(stack);
      }
   }

   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.eerie_family_portrait.description.source"));
      tooltip.add(Text.translatable("item.smfs.eerie_family_portrait.description.desc"));
      tooltip.add(Text.translatable("item.smfs.eerie_family_portrait.description.type"));
   }

   private void playUnlockEffects(ServerPlayerEntity player) {
      player.playSound(SoundEvents.ENTITY_ENDERMAN_STARE, 1.0F, 0.5F);
      if (player.getWorld() instanceof ServerWorld serverWorld) {
         ServerWorld world = serverWorld;

         for (int i = 0; i < 20; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 4.0;
            double offsetY = world.random.nextDouble() * 2.0;
            double offsetZ = (world.random.nextDouble() - 0.5) * 4.0;
            world.spawnParticles(ParticleTypes.SMOKE, player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ, 3, 0.1, 0.1, 0.1, 0.02);
         }
      }
   }
}
