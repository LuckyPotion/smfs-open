package com.xie.smfs.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FissuredSpearRedItem extends BoundSpearItem {
   public FissuredSpearRedItem(Settings settings) {
      super(settings);
   }

   @Override
   public float getSpiritDamageBonus() {
      return 180.0F;
   }

   @Override
   public float getSpiritDamageMultiplier() {
      return 1.3F;
   }

   @Override
   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.method_43471("item.smfs.fissured_spear_red.description.source"));
      tooltip.add(Text.method_43471("item.smfs.fissured_spear_red.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.fissured_spear_red.description.type"));
      tooltip.add(Text.method_43471("item.smfs.fissured_spear_red.skill.description"));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_multiplier", new Object[]{Math.round(this.getSpiritDamageMultiplier() * 100.0F)}));
      if (this.isBound(stack)) {
         tooltip.add(Text.method_43470("已认主").method_27692(Formatting.field_1054));
      } else {
         tooltip.add(Text.method_43470("未认主").method_27692(Formatting.field_1080));
      }
   }
}
