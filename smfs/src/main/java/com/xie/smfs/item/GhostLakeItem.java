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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostLakeItem extends BaseGhostEyeItem {
   private static final Logger LOGGER = LoggerFactory.getLogger(GhostLakeItem.class);

   public GhostLakeItem(Settings settings, int maxSpiritBonus, int spiritResistanceBonus, int spiritDamageBonus, int sanityBonus, double revivalFactor) {
      super(settings.maxCount(1), maxSpiritBonus, spiritResistanceBonus, spiritDamageBonus, sanityBonus, revivalFactor);
   }

   public GhostLakeItem(Settings settings) {
      this(settings, 1200, 15, 15, 100, 0.3);
   }

   @Override
   public Text getName(ItemStack stack) {
      return Text.translatable("item.smfs.ghost_lake");
   }

   @Override
   protected Text getBindSuccessMessage(int slot) {
      return Text.translatable("item.smfs.silent_ghost_eye.bind_success").append(Text.literal(" (槽位 " + (slot + 1) + ")"));
   }

   @Override
   protected void sendSlotsFullMessage(PlayerEntity player) {
      player.sendMessage(Text.translatable("item.smfs.silent_ghost_eye.slots_full"), true);
   }

   @Override
   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      return super.use(world, user, hand);
   }

   @Override
   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.translatable("item.smfs.ghost_eye.description.source"));
      tooltip.add(Text.translatable("item.smfs.ghost_lake.description.desc"));
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
      return "ghost_lake";
   }
}
