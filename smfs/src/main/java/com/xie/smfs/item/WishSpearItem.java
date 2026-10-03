package com.xie.smfs.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WishSpearItem extends BoundSpearItem {
   public WishSpearItem(Settings settings) {
      super(settings);
   }

   @Override
   public float getSpiritDamageBonus() {
      return 210.0F;
   }

   @Override
   public float getSpiritDamageMultiplier() {
      return 1.5F;
   }

   @Override
   public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      super.inventoryTick(stack, world, entity, slot, selected);
      if (!stack.hasNbt() || !stack.getOrCreateNbt().contains("ThrowMode")) {
         setThrowMode(stack, "medium");
      }
   }

   @Override
   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.translatable("item.smfs.wish_spear.description.source"));
      tooltip.add(Text.translatable("item.smfs.wish_spear.description.desc"));
      tooltip.add(Text.translatable("item.smfs.wish_spear.description.type"));
      tooltip.add(Text.translatable("item.smfs.wish_spear.skill.description"));
      String mode = getThrowMode(stack);

      String modeText = switch (mode) {
         case "medium" -> "§b媒介模式";
         case "wish" -> "§d许愿模式";
         default -> "§7未知";
      };
      tooltip.add(Text.literal("当前模式: " + modeText));
      if (mode.equals("wish")) {
         String preset = getWishPreset(stack);

         String presetText = switch (preset) {
            case "tracking" -> "§e追踪";
            case "remote_attack" -> "§e远程攻击";
            default -> "§7未设置";
         };
         tooltip.add(Text.literal("许愿预设: " + presetText));
      }

      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_multiplier", new Object[]{this.getSpiritDamageMultiplier() * 100.0F}));
      if (this.isBound(stack)) {
         tooltip.add(Text.literal("已认主").formatted(Formatting.YELLOW));
      } else {
         tooltip.add(Text.literal("未认主").formatted(Formatting.GRAY));
      }
   }
}
