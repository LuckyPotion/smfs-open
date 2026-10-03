package com.xie.smfs.item;

import com.xie.smfs.Smfs;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.Item.Settings;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DefiledSwordItem extends SwordItem implements SpiritWeapon {
   public DefiledSwordItem(Settings settings) {
      super(ToolMaterials.field_8930, 7, -2.4F, settings);
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.method_5998(hand);
      if (world.method_27983() == Smfs.GHOST_DREAM_DIMENSION) {
         return TypedActionResult.method_22431(stack);
      }

      if (!world.method_8608()) {
         user.method_5783(SoundEvents.field_14706, 1.0F, 0.8F);
         user.method_7357().method_7906(this, 10);
      }

      return TypedActionResult.method_22427(stack);
   }

   @Override
   public void onSpiritWeaponAttack(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (!target.method_37908().method_8608()) {
         target.method_6092(new StatusEffectInstance(StatusEffects.field_5911, 100, 0));
      }
   }

   @Override
   public float getSpiritDamageBonus() {
      return 30.0F;
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.defiled_sword.description.source"));
      tooltip.add(Text.method_43471("item.smfs.defiled_sword.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.defiled_sword.description.type"));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_multiplier", new Object[]{this.getSpiritDamageMultiplier() * 100.0F}));
   }

   public boolean method_7846() {
      return false;
   }
}
