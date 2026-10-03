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
      super(ToolMaterials.field_8922, 5, -2.8F, settings);
   }

   @Override
   public void onSpiritWeaponAttack(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      float damage = this.method_8020();
      target.method_5643(ModDamageSources.ghost(attacker.method_37908()), damage);
      if (!this.isOnCooldown(stack, attacker.method_37908())) {
         if (!attacker.method_37908().method_8608() && target.method_5805()) {
            target.method_6092(new StatusEffectInstance(ModEffects.SILENCE, 60, 0, false, true, true));
            attacker.method_37908()
               .method_43128(
                  null, target.method_23317(), target.method_23318(), target.method_23321(), SoundEvents.field_21891, SoundCategory.field_15248, 0.8F, 0.5F
               );
            this.markCooldown(stack, attacker.method_37908());
         }
      }
   }

   private boolean isOnCooldown(ItemStack stack, World world) {
      NbtCompound nbt = stack.method_7948();
      if (nbt.method_10545("LastSilenceTick")) {
         long lastTick = nbt.method_10537("LastSilenceTick");
         long currentTick = world.method_8510();
         return currentTick - lastTick < 200L;
      } else {
         return false;
      }
   }

   private void markCooldown(ItemStack stack, World world) {
      NbtCompound nbt = stack.method_7948();
      nbt.method_10544("LastSilenceTick", world.method_8510());
   }

   @Override
   public float getSpiritDamageBonus() {
      return 20.0F;
   }

   @Override
   public float getSpiritDamageMultiplier() {
      return 0.3F;
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.ghost_wood_hammer.description.source"));
      tooltip.add(Text.method_43471("item.smfs.ghost_wood_hammer.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.ghost_wood_hammer.description.type"));
      tooltip.add(Text.method_43471("item.smfs.ghost_wood_hammer.effect.silence"));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_multiplier", new Object[]{Math.round(this.getSpiritDamageMultiplier() * 100.0F)}));
   }
}
