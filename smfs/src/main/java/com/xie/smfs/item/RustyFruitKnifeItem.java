package com.xie.smfs.item;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.event.ModEvents;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RustyFruitKnifeItem extends SwordItem implements SpiritWeapon {
   private static final Logger LOGGER = LoggerFactory.getLogger(RustyFruitKnifeItem.class);

   public RustyFruitKnifeItem(Settings settings) {
      super(ToolMaterials.field_8923, 3, -2.4F, settings);
   }

   @Override
   public void onSpiritWeaponAttack(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      float baseDamage = this.method_8020();
      float spiritDamage = baseDamage + this.getSpiritDamageBonus() * this.getSpiritDamageMultiplier();
      if (!attacker.method_37908().method_8608()) {
         ModEvents.processingSpiritDamage.set(true);

         try {
            if (attacker instanceof PlayerEntity player) {
               PlayerEvents.handleSpiritDamage(player, spiritDamage, spiritDamage, ModDamageSources.ghost(attacker.method_37908()));
            } else {
               attacker.method_5643(ModDamageSources.ghost(attacker.method_37908()), spiritDamage);
            }
         } finally {
            ModEvents.processingSpiritDamage.set(false);
         }
      }
   }

   @Override
   public float getSpiritDamageBonus() {
      return 20.0F;
   }

   @Override
   public float getSpiritDamageMultiplier() {
      return 0.5F;
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.rusty_fruit_knife.description.source"));
      tooltip.add(Text.method_43471("item.smfs.rusty_fruit_knife.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.rusty_fruit_knife.description.type"));
      tooltip.add(Text.method_43471("item.smfs.rusty_fruit_knife.effect.self_damage"));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_multiplier", new Object[]{this.getSpiritDamageMultiplier() * 100.0F}));
   }
}
