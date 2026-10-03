package com.xie.smfs.item;

import com.xie.smfs.Smfs;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.registry.ModEffects;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.Item.Settings;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ZhenwuSwordItem extends SwordItem implements SpiritWeapon {
   public ZhenwuSwordItem(Settings settings) {
      super(ToolMaterials.field_8923, 6, -2.4F, settings);
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.method_5998(hand);
      if (world.method_27983() == Smfs.GHOST_DREAM_DIMENSION) {
         return TypedActionResult.method_22431(stack);
      }

      if (!world.method_8608()) {
         Vec3d playerPos = user.method_19538();
         Vec3d lookVec = user.method_5828(1.0F);
         double radius = 34.0;
         Box searchBox = new Box(
            playerPos.field_1352 - radius,
            playerPos.field_1351 - radius,
            playerPos.field_1350 - radius,
            playerPos.field_1352 + radius,
            playerPos.field_1351 + radius,
            playerPos.field_1350 + radius
         );

         for (LivingEntity target : world.method_8390(LivingEntity.class, searchBox, entity -> entity != user && this.isFacingPlayer(entity, user))) {
            this.attackTarget(stack, target, user);
         }

         user.method_5783(SoundEvents.field_14706, 1.0F, 1.0F);
         user.method_7357().method_7906(this, 20);
      }

      return TypedActionResult.method_22427(stack);
   }

   private boolean isFacingPlayer(LivingEntity entity, PlayerEntity player) {
      Vec3d entityLookVec = entity.method_5828(1.0F);
      Vec3d toPlayer = player.method_19538().method_1020(entity.method_19538()).method_1029();
      double dotProduct = entityLookVec.method_1026(toPlayer);
      return dotProduct > 0.5;
   }

   private void attackTarget(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      float damage = this.method_8020() + this.getSpiritDamageBonus() * this.getSpiritDamageMultiplier();
      target.method_5643(ModDamageSources.ghost(attacker.method_37908()), damage);
      if (!target.method_37908().method_8608()) {
         target.method_6092(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 100, 4));
      }

      this.spawnAttackParticles(target);
   }

   private void spawnAttackParticles(LivingEntity target) {
      if (!target.method_37908().method_8608()) {
         ServerWorld serverWorld = (ServerWorld)target.method_37908();
         Vec3d pos = target.method_19538();

         for (int i = 0; i < 10; i++) {
            double offsetX = (target.method_6051().method_43058() - 0.5) * 2.0;
            double offsetY = target.method_6051().method_43058() * 2.0;
            double offsetZ = (target.method_6051().method_43058() - 0.5) * 2.0;
            serverWorld.method_14199(
               ParticleTypes.field_11251, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 5, 0.1, 0.1, 0.1, 0.02
            );
         }
      }
   }

   @Override
   public void onSpiritWeaponAttack(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (!target.method_37908().method_8608()) {
         target.method_6092(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 60, 0));
      }
   }

   @Override
   public float getSpiritDamageBonus() {
      return 120.0F;
   }

   @Override
   public float getSpiritDamageMultiplier() {
      return 0.7F;
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.zhenwu_sword.description.source"));
      tooltip.add(Text.method_43471("item.smfs.zhenwu_sword.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.zhenwu_sword.description.type"));
      tooltip.add(Text.method_43471("item.smfs.zhenwu_sword.effect.right_click"));
      tooltip.add(Text.method_43471("item.smfs.zhenwu_sword.effect.wither"));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_multiplier", new Object[]{this.getSpiritDamageMultiplier() * 100.0F}));
   }

   public boolean method_7846() {
      return false;
   }
}
