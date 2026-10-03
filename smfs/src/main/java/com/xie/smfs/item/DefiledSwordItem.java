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
      super(ToolMaterials.DIAMOND, 7, -2.4F, settings);
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (world.getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION) {
         return TypedActionResult.fail(stack);
      }

      if (!world.isClient()) {
         user.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 0.8F);
         user.getItemCooldownManager().set(this, 10);
      }

      return TypedActionResult.success(stack);
   }

   @Override
   public void onSpiritWeaponAttack(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (!target.getWorld().isClient()) {
         target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 0));
      }
   }

   @Override
   public float getSpiritDamageBonus() {
      return 30.0F;
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.defiled_sword.description.source"));
      tooltip.add(Text.translatable("item.smfs.defiled_sword.description.desc"));
      tooltip.add(Text.translatable("item.smfs.defiled_sword.description.type"));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_multiplier", new Object[]{this.getSpiritDamageMultiplier() * 100.0F}));
   }

   public boolean isDamageable() {
      return false;
   }
}
