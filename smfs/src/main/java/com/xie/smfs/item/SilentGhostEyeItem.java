package com.xie.smfs.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SilentGhostEyeItem extends BaseGhostEyeItem {
   private static final Logger LOGGER = LoggerFactory.getLogger(SilentGhostEyeItem.class);

   public SilentGhostEyeItem(Settings settings) {
      super(settings.maxCount(1), 500, 15, 10, 100, 0.4);
   }

   @Override
   public Text getName(ItemStack stack) {
      return Text.translatable("item.smfs.silent_ghost_eye");
   }

   @Override
   protected Text getBindSuccessMessage(int slot) {
      return Text.translatable("item.smfs.silent_ghost_eye.bind_success").append(Text.literal(" (槽位 " + (slot + 1) + ")"));
   }

   @Override
   protected void sendSlotsFullMessage(PlayerEntity player) {
      player.sendMessage(Text.literal("槽位已满，无法绑定更多"), true);
   }

   @Override
   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.translatable("item.smfs.ghost_eye.description.source"));
      tooltip.add(Text.translatable("item.smfs.silent_ghost_eye.description.desc"));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.description.type"));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.max_spirit", new Object[]{this.getMaxSpiritBonus()}));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.spirit_resistance", new Object[]{this.getSpiritResistanceBonus()}));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.spirit_damage", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.sanity", new Object[]{this.getSanityBonus()}));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.revival_factor", new Object[]{String.format("%.1f", this.getRevivalFactor() * 100.0)}));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.usage"));
   }

   @Override
   public String getGhostType() {
      return "silent_ghost_eye";
   }
}
