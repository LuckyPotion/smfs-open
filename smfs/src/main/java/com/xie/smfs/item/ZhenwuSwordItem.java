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
      super(ToolMaterials.IRON, 6, -2.4F, settings);
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (world.getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION) {
         return TypedActionResult.fail(stack);
      }

      if (!world.isClient()) {
         Vec3d playerPos = user.getPos();
         Vec3d lookVec = user.getRotationVec(1.0F);
         double radius = 34.0;
         Box searchBox = new Box(
            playerPos.x - radius, playerPos.y - radius, playerPos.z - radius, playerPos.x + radius, playerPos.y + radius, playerPos.z + radius
         );

         for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, searchBox, entity -> entity != user && this.isFacingPlayer(entity, user))) {
            this.attackTarget(stack, target, user);
         }

         user.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 1.0F);
         user.getItemCooldownManager().set(this, 20);
      }

      return TypedActionResult.success(stack);
   }

   private boolean isFacingPlayer(LivingEntity entity, PlayerEntity player) {
      Vec3d entityLookVec = entity.getRotationVec(1.0F);
      Vec3d toPlayer = player.getPos().subtract(entity.getPos()).normalize();
      double dotProduct = entityLookVec.dotProduct(toPlayer);
      return dotProduct > 0.5;
   }

   private void attackTarget(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      float damage = this.getAttackDamage() + this.getSpiritDamageBonus() * this.getSpiritDamageMultiplier();
      target.damage(ModDamageSources.ghost(attacker.getWorld()), damage);
      if (!target.getWorld().isClient()) {
         target.addStatusEffect(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 100, 4));
      }

      this.spawnAttackParticles(target);
   }

   private void spawnAttackParticles(LivingEntity target) {
      if (!target.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)target.getWorld();
         Vec3d pos = target.getPos();

         for (int i = 0; i < 10; i++) {
            double offsetX = (target.getRandom().nextDouble() - 0.5) * 2.0;
            double offsetY = target.getRandom().nextDouble() * 2.0;
            double offsetZ = (target.getRandom().nextDouble() - 0.5) * 2.0;
            serverWorld.spawnParticles(ParticleTypes.SMOKE, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 5, 0.1, 0.1, 0.1, 0.02);
         }
      }
   }

   @Override
   public void onSpiritWeaponAttack(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (!target.getWorld().isClient()) {
         target.addStatusEffect(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 60, 0));
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

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.zhenwu_sword.description.source"));
      tooltip.add(Text.translatable("item.smfs.zhenwu_sword.description.desc"));
      tooltip.add(Text.translatable("item.smfs.zhenwu_sword.description.type"));
      tooltip.add(Text.translatable("item.smfs.zhenwu_sword.effect.right_click"));
      tooltip.add(Text.translatable("item.smfs.zhenwu_sword.effect.wither"));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_multiplier", new Object[]{this.getSpiritDamageMultiplier() * 100.0F}));
   }

   public boolean isDamageable() {
      return false;
   }
}
