package com.xie.smfs.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class GhostDreamItem extends BaseGhostEyeItem {
   public GhostDreamItem(Settings settings) {
      super(settings.method_7889(1), 1800, 40, 200, 800, 0.3);
   }

   @Override
   public Text method_7864(ItemStack stack) {
      return Text.method_43471("item.smfs.ghost_dream");
   }

   @Override
   protected Text getBindSuccessMessage(int slot) {
      return Text.method_43471("item.smfs.ghost_dream.bind_success").method_10852(Text.method_43470(" (槽位 " + (slot + 1) + ")"));
   }

   @Override
   protected void sendSlotsFullMessage(PlayerEntity player) {
      player.method_7353(Text.method_43470("槽位已满，无法绑定更多"), true);
   }

   @Override
   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity player, Hand hand) {
      return super.method_7836(world, player, hand);
   }

   @Override
   public void method_7851(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.method_43471("item.smfs.ghost_eye.description.source"));
      tooltip.add(Text.method_43471("item.smfs.ghost_dream.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.ghost_eye.description.type"));
      tooltip.add(Text.method_43469("item.smfs.ghost_eye.max_spirit", new Object[]{this.getMaxSpiritBonus()}));
      tooltip.add(Text.method_43469("item.smfs.ghost_eye.spirit_resistance", new Object[]{this.getSpiritResistanceBonus()}));
      tooltip.add(Text.method_43469("item.smfs.ghost_eye.spirit_damage", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.method_43469("item.smfs.ghost_eye.sanity", new Object[]{this.getSanityBonus()}));
      tooltip.add(Text.method_43469("item.smfs.ghost_eye.revival_factor", new Object[]{String.format("%.1f", this.getRevivalFactor() * 100.0)}));
      tooltip.add(Text.method_43471("item.smfs.ghost_eye.usage"));
   }

   @Override
   public String getGhostType() {
      return "ghost_dream";
   }
}
