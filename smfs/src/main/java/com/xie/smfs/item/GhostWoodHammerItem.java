package com.xie.smfs.item;

import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.registry.ModEffects;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.Item.Settings;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GhostWoodHammerItem extends SwordItem implements SpiritWeapon {
   private static final int SILENCE_DURATION = 60;
   private static final int COOLDOWN_TICKS = 200;

   public GhostWoodHammerItem(Settings settings) {
      super(ToolMaterials.WOOD, 5, -2.8F, settings);
   }

   @Override
   public void onSpiritWeaponAttack(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      float damage = this.getAttackDamage();
      target.damage(ModDamageSources.ghost(attacker.getWorld()), damage);
      if (!this.isOnCooldown(stack, attacker.getWorld())) {
         if (!attacker.getWorld().isClient() && target.isAlive()) {
            target.addStatusEffect(new StatusEffectInstance(ModEffects.SILENCE, 60, 0, false, true, true));
            attacker.getWorld()
               .playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BLOCK_ANCIENT_DEBRIS_BREAK, SoundCategory.PLAYERS, 0.8F, 0.5F);
            this.markCooldown(stack, attacker.getWorld());
         }
      }
   }

   private boolean isOnCooldown(ItemStack stack, World world) {
      NbtCompound nbt = stack.getOrCreateNbt();
      if (nbt.contains("LastSilenceTick")) {
         long lastTick = nbt.getLong("LastSilenceTick");
         long currentTick = world.getTime();
         return currentTick - lastTick < 200L;
      } else {
         return false;
      }
   }

   private void markCooldown(ItemStack stack, World world) {
      NbtCompound nbt = stack.getOrCreateNbt();
      nbt.putLong("LastSilenceTick", world.getTime());
   }

   @Override
   public float getSpiritDamageBonus() {
      return 20.0F;
   }

   @Override
   public float getSpiritDamageMultiplier() {
      return 0.3F;
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.ghost_wood_hammer.description.source"));
      tooltip.add(Text.translatable("item.smfs.ghost_wood_hammer.description.desc"));
      tooltip.add(Text.translatable("item.smfs.ghost_wood_hammer.description.type"));
      tooltip.add(Text.translatable("item.smfs.ghost_wood_hammer.effect.silence"));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_multiplier", new Object[]{Math.round(this.getSpiritDamageMultiplier() * 100.0F)}));
   }
}
