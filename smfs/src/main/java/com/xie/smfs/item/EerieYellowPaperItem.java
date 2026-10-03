package com.xie.smfs.item;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.MainGhostManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class EerieYellowPaperItem extends Item {
   private static final Map<PlayerEntity, Long> cooldowns = new HashMap<>();
   private static final long COOLDOWN_TICKS = 100L;
   private static final int REVIVAL_DEGREE_REDUCTION = 110;

   public EerieYellowPaperItem(Settings settings) {
      super(settings);
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.getStackInHand(hand);
      if (this.hasCooldown(user)) {
         user.sendMessage(Text.translatable("item.smfs.eerie_yellow_paper.cooldown"), true);
         return TypedActionResult.fail(itemStack);
      }

      if (!world.isClient) {
         int mainSlot = MainGhostManager.getMainGhostSlot(user);
         if (PlayerEvents.isGhostSlotOccupied(user, mainSlot)) {
            int currentDegree = PlayerEvents.getGhostSlotRevivalDegree(user, mainSlot);
            int newDegree = Math.max(0, currentDegree - 110);
            PlayerEvents.updateGhostSlotValue(user, mainSlot, "revivalDegree", newDegree);
            this.setCooldown(user);
            itemStack.decrement(1);
            user.sendMessage(Text.translatable("item.smfs.eerie_yellow_paper.use_success"), true);
            return TypedActionResult.success(itemStack);
         } else {
            user.sendMessage(Text.translatable("item.smfs.eerie_yellow_paper.no_main_ghost"), true);
            return TypedActionResult.fail(itemStack);
         }
      } else {
         return TypedActionResult.fail(itemStack);
      }
   }

   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.eerie_yellow_paper.description.source"));
      tooltip.add(Text.translatable("item.smfs.eerie_yellow_paper.description.desc"));
      tooltip.add(Text.translatable("item.smfs.eerie_yellow_paper.description.type"));
   }

   private boolean hasCooldown(PlayerEntity player) {
      Long lastUseTime = cooldowns.get(player);
      return lastUseTime == null ? false : player.getWorld().getTime() - lastUseTime < 100L;
   }

   private void setCooldown(PlayerEntity player) {
      cooldowns.put(player, player.getWorld().getTime());
   }
}
