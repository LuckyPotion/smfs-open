package com.xie.smfs.item;

import com.xie.smfs.damage.ModDamageSources;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GhostAxeItem extends AxeItem implements SpiritWeapon {
   public GhostAxeItem(Settings settings) {
      super(ToolMaterials.IRON, 6.0F, -3.0F, settings);
   }

   @Override
   public void onSpiritWeaponAttack(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      float damage = this.getAttackDamage();
      target.damage(ModDamageSources.ghost(attacker.getWorld()), damage);
      if (!attacker.getWorld().isClient() && attacker.isAlive()) {
         attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 0));
         attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0));
      }
   }

   @Override
   public float getSpiritDamageBonus() {
      return 60.0F;
   }

   @Override
   public float getSpiritDamageMultiplier() {
      return 0.35F;
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.ghost_axe.description.source"));
      tooltip.add(Text.translatable("item.smfs.ghost_axe.description.desc"));
      tooltip.add(Text.translatable("item.smfs.ghost_axe.description.type"));
      tooltip.add(Text.translatable("item.smfs.ghost_axe.effect.weakness"));
      tooltip.add(Text.translatable("item.smfs.ghost_axe.effect.slowness"));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_multiplier", new Object[]{this.getSpiritDamageMultiplier() * 100.0F}));
   }
}
