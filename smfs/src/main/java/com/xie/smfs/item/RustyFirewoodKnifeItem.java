package com.xie.smfs.item;

import com.xie.smfs.Smfs;
import com.xie.smfs.block.FootprintBlock;
import com.xie.smfs.block.entity.FootprintBlockEntity;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.effect.SpiritAttributes;
import com.xie.smfs.effect.SpiritSurgeStatusEffect;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.event.ModEvents;
import com.xie.smfs.event.QuestEventHandler;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModSounds;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.Item.Settings;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RustyFirewoodKnifeItem extends SwordItem implements SpiritWeapon {
   private static final Logger LOGGER = LoggerFactory.getLogger(RustyFirewoodKnifeItem.class);
   private static final int COOLDOWN_TICKS = 100;

   public RustyFirewoodKnifeItem(Settings settings) {
      super(ToolMaterials.IRON, 4, -2.2F, settings);
   }

   public boolean isDamageable() {
      return false;
   }

   private void handleGhostSpiritDamage(LivingEntity attacker, GhostEntity ghost, float spiritDamage) {
      float ghostSpiritResistance = ghost.getSpiritualResistance();
      float actualSpiritDamage;
      if (attacker instanceof PlayerEntity player && SpiritSurgeStatusEffect.hasSpiritSurgeEffect(player)) {
         float playerSpiritDamage = PlayerEvents.getSpiritAttribute(player, SpiritAttributes.SPIRIT_DAMAGE);
         actualSpiritDamage = PlayerEvents.calculateSpiritSurgeDamage(playerSpiritDamage, spiritDamage, ghostSpiritResistance);
         LOGGER.debug("玩家 {} 使用灵异奔涌效果攻击鬼，基础灵异伤害: {}，使用特殊公式计算伤害", player.getName().getString(), playerSpiritDamage);
      } else {
         actualSpiritDamage = PlayerEvents.calculateSpiritDamage(spiritDamage, ghostSpiritResistance);
      }

      int currentSpirit = ghost.getSpiritualStrength();
      int newSpirit = Math.max(0, currentSpirit - (int)actualSpiritDamage);
      ghost.setSpiritualStrength(newSpirit);
      if (newSpirit == 0 && !ghost.isDeadlocked()) {
         ghost.setDeadlocked(true);
      }

      if (actualSpiritDamage > 0.0F && !ghost.getWorld().isClient()) {
         this.spawnSpiritAttackParticles(ghost);
      }
   }

   private void spawnSpiritAttackParticles(LivingEntity target) {
      if (!target.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)target.getWorld();
         Vec3d pos = target.getPos();

         for (int i = 0; i < 25; i++) {
            double offsetX = (target.getRandom().nextDouble() - 0.5) * 2.0;
            double offsetY = target.getRandom().nextDouble() * 1.5 + 0.5;
            double offsetZ = (target.getRandom().nextDouble() - 0.5) * 2.0;
            double velocityX = (target.getRandom().nextDouble() - 0.5) * 0.2;
            double velocityY = target.getRandom().nextDouble() * 0.3 + 0.1;
            double velocityZ = (target.getRandom().nextDouble() - 0.5) * 0.2;
            serverWorld.spawnParticles(ParticleTypes.WITCH, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5);
         }
      }
   }

   @Override
   public float getSpiritDamageBonus() {
      return 125.0F;
   }

   @Override
   public float getSpiritDamageMultiplier() {
      return 1.0F;
   }

   @Override
   public void onSpiritWeaponAttack(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (!attacker.getWorld().isClient()
         && attacker.isAlive()
         && !(
            attacker instanceof PlayerEntity player
               && (PlayerEvents.hasGhostType(player, "giant_shadow_ghost") || PlayerEvents.hasGhostType(player, "complete_shadow_ghost"))
         )) {
         attacker.addStatusEffect(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 60, 0));
      }
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.rusty_firewood_knife.description.source"));
      tooltip.add(Text.translatable("item.smfs.rusty_firewood_knife.description.desc"));
      tooltip.add(Text.translatable("item.smfs.rusty_firewood_knife.description.type"));
      tooltip.add(Text.translatable("item.smfs.rusty_firewood_knife.effect.self_damage"));
      tooltip.add(Text.translatable("item.smfs.rusty_firewood_knife.effect.dismember"));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_multiplier", new Object[]{this.getSpiritDamageMultiplier() * 100.0F}));
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (world.getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION) {
         return TypedActionResult.fail(stack);
      }

      if (user.getItemCooldownManager().isCoolingDown(this)) {
         return TypedActionResult.pass(stack);
      }

      LivingEntity targetEntity = null;
      BlockPos playerPos = user.getBlockPos();
      BlockState blockState = world.getBlockState(playerPos);
      if (blockState.getBlock() instanceof FootprintBlock) {
         FootprintBlockEntity footprintEntity = (FootprintBlockEntity)world.getBlockEntity(playerPos);
         if (footprintEntity != null && footprintEntity.hasValidEntityInfo()) {
            targetEntity = footprintEntity.getTargetEntity(world);
         }
      }

      if (targetEntity == null && this.isAberrationWithGiantShadowGhost(user)) {
         HitResult hitResult = user.raycast(30.0, 0.0F, false);
         if (hitResult.getType() == Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult)hitResult;
            BlockPos hitPos = blockHit.getBlockPos();
            BlockState hitState = world.getBlockState(hitPos);
            if (hitState.getBlock() instanceof FootprintBlock) {
               FootprintBlockEntity footprintEntity = (FootprintBlockEntity)world.getBlockEntity(hitPos);
               if (footprintEntity != null && footprintEntity.hasValidEntityInfo()) {
                  targetEntity = footprintEntity.getTargetEntity(world);
               }
            }
         }

         if (targetEntity == null) {
            if (!world.isClient()) {
               user.sendMessage(Text.literal("§c请对准脚印方块使用"), true);
            }

            return TypedActionResult.pass(stack);
         }
      }

      if (targetEntity != null && targetEntity.isAlive()) {
         if (!world.isClient()) {
            this.performMediumAttack(world, user, targetEntity);
         }

         user.getItemCooldownManager().set(this, 100);
         return TypedActionResult.success(stack);
      } else {
         if (!world.isClient() && !this.isAberrationWithGiantShadowGhost(user)) {
            user.sendMessage(Text.translatable("item.smfs.rusty_firewood_knife.skill.not_on_footprint"), true);
         }

         return TypedActionResult.pass(stack);
      }
   }

   private boolean isAberrationWithGiantShadowGhost(PlayerEntity user) {
      if (user instanceof ServerPlayerEntity serverPlayer) {
         return !AdvancementManager.hasAdvancement(serverPlayer, "smfs:become_aberration")
            ? false
            : PlayerEvents.hasGhostType(user, "giant_shadow_ghost") || PlayerEvents.hasGhostType(user, "complete_shadow_ghost");
      } else {
         return false;
      }
   }

   private void performMediumAttack(World world, PlayerEntity user, LivingEntity targetEntity) {
      if (targetEntity instanceof GhostEntity ghost) {
         int maxSpiritualStrength = ghost.getMaxSpiritualStrength();
         if (maxSpiritualStrength < 1000) {
            user.sendMessage(Text.literal("§c目标已无法被肢解"), true);
            return;
         }

         int damageAmount = (int)(maxSpiritualStrength * 0.2);
         int newMaxSpirit = Math.max(0, maxSpiritualStrength - damageAmount);
         ghost.setMaxSpiritualStrength(newMaxSpirit);
         int currentSpirit = ghost.getSpiritualStrength();
         int newCurrentSpirit = Math.min(currentSpirit, newMaxSpirit);
         ghost.setSpiritualStrength(newCurrentSpirit);
         world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.SWING_ATTACK_SOUND, SoundCategory.HOSTILE, 1.0F, 1.0F);
         if (!PlayerEvents.hasGhostType(user, "giant_shadow_ghost") && !PlayerEvents.hasGhostType(user, "complete_shadow_ghost")) {
            float playerDamage = damageAmount * 0.5F;
            PlayerEvents.handleSpiritDamage(user, playerDamage, playerDamage, ModDamageSources.ghost(world));
         }

         this.spawnSpiritAttackParticles(ghost);
      } else if (targetEntity instanceof PlayerEntity targetPlayer) {
         NbtCompound targetSpiritAttributes = PlayerEvents.getSpiritAttributes(targetPlayer);
         float maxSpirit = targetSpiritAttributes.contains("maxSpirit") ? (float)targetSpiritAttributes.getDouble("maxSpirit") : 0.0F;
         if (maxSpirit < 1000.0F) {
            user.sendMessage(Text.literal("§c目标已无法被肢解"), true);
            return;
         }

         float damageAmount = maxSpirit * 0.2F;
         float newMaxSpirit = maxSpirit - damageAmount;
         NbtCompound spiritData = PlayerEvents.getSpiritAttributes(targetPlayer);
         spiritData.putDouble("maxSpirit", newMaxSpirit);
         float currentSpirit = spiritData.contains("currentSpirit") ? (float)spiritData.getDouble("currentSpirit") : 0.0F;
         float newCurrentSpirit = Math.min(currentSpirit, newMaxSpirit);
         spiritData.putDouble("currentSpirit", newCurrentSpirit);
         PlayerEvents.setSpiritAttributes(targetPlayer, spiritData);
         targetPlayer.playSound(SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0F, 1.0F);
         if (!PlayerEvents.hasGhostType(user, "giant_shadow_ghost") && !PlayerEvents.hasGhostType(user, "complete_shadow_ghost")) {
            float playerDamage = damageAmount * 0.5F;
            PlayerEvents.handleSpiritDamage(user, playerDamage, playerDamage, ModDamageSources.ghost(world));
         }

         this.spawnSpiritAttackParticles(targetPlayer);
      } else {
         float spiritDamage = this.getSpiritDamageBonus() * 4.0F;
         ModEvents.processingSpiritDamage.set(true);

         try {
            this.spawnSpiritAttackParticles(targetEntity);
            targetEntity.damage(ModDamageSources.ghost(world), spiritDamage);
            targetEntity.playSound(SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0F, 1.0F);
            if (ModConfig.getInstance().showActionBarInfo) {
               user.sendMessage(
                  Text.literal("§a对 §f" + targetEntity.getName().getString() + " §a造成了 §f" + new DecimalFormat("#.###").format(spiritDamage) + " §a点灵异伤害"),
                  true
               );
            }

            if (!targetEntity.isAlive() && user instanceof ServerPlayerEntity serverPlayer) {
               QuestEventHandler.handleEntityKill(serverPlayer, targetEntity);
            }
         } finally {
            ModEvents.processingSpiritDamage.set(false);
         }

         if (!PlayerEvents.hasGhostType(user, "giant_shadow_ghost") && !PlayerEvents.hasGhostType(user, "complete_shadow_ghost")) {
            float playerDamage = spiritDamage * 0.5F;
            PlayerEvents.handleSpiritDamage(user, playerDamage, playerDamage, ModDamageSources.ghost(world));
         }
      }
   }
}
