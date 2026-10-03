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

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity player, Hand hand) {
      ItemStack itemStack = player.method_5998(hand);
      if (this.hasCooldown(player)) {
         player.method_7353(Text.method_43471("item.smfs.eerie_yellow_paper.cooldown"), true);
         return TypedActionResult.method_22431(itemStack);
      }

      if (!world.field_9236) {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         if (PlayerEvents.isGhostSlotOccupied(player, mainSlot)) {
            int currentDegree = PlayerEvents.getGhostSlotRevivalDegree(player, mainSlot);
            int newDegree = Math.max(0, currentDegree - 110);
            PlayerEvents.updateGhostSlotValue(player, mainSlot, "revivalDegree", newDegree);
            this.setCooldown(player);
            itemStack.method_7934(1);
            player.method_7353(Text.method_43471("item.smfs.eerie_yellow_paper.use_success"), true);
            return TypedActionResult.method_22427(itemStack);
         } else {
            player.method_7353(Text.method_43471("item.smfs.eerie_yellow_paper.no_main_ghost"), true);
            return TypedActionResult.method_22431(itemStack);
         }
      } else {
         return TypedActionResult.method_22431(itemStack);
      }
   }

   public void method_7851(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.eerie_yellow_paper.description.source"));
      tooltip.add(Text.method_43471("item.smfs.eerie_yellow_paper.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.eerie_yellow_paper.description.type"));
   }

   private boolean hasCooldown(PlayerEntity player) {
      Long lastUseTime = cooldowns.get(player);
      return lastUseTime == null ? false : player.method_37908().method_8510() - lastUseTime < 100L;
   }

   private void setCooldown(PlayerEntity player) {
      cooldowns.put(player, player.method_37908().method_8510());
   }
}
