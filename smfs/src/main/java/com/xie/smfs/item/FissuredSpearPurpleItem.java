package com.xie.smfs.item;

import com.xie.smfs.Smfs;
import com.xie.smfs.block.FootprintBlock;
import com.xie.smfs.block.entity.FootprintBlockEntity;
import com.xie.smfs.client.renderer.item.FissuredSpearItemRenderer;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.other.FissuredSpearEntity;
import com.xie.smfs.event.ModEvents;
import com.xie.smfs.event.QuestEventHandler;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.registry.ModEntities;
import com.xie.smfs.registry.ModSounds;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.Vanishable;
import net.minecraft.item.Item.Settings;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtils;

public class FissuredSpearPurpleItem extends SwordItem implements SpiritWeapon, Vanishable, GeoItem {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private static final int COOLDOWN_TICKS = 100;
   private static final int SHORT_PRESS_THRESHOLD = 10;
   private static final String THROW_MODE_KEY = "ThrowMode";
   public static final String MODE_SUPPRESS = "suppress";
   public static final String MODE_ATTACK = "attack";
   public static final String MODE_MEDIUM = "medium";
   public static final String MODE_WISH = "wish";
   private static final String WISH_PRESET_KEY = "WishPreset";
   private static final String SHOW_WISH_TEXT_KEY = "ShowWishText";
   public static final String WISH_PRESET_NONE = "";
   public static final String WISH_PRESET_TRACKING = "tracking";
   public static final String WISH_PRESET_REMOTE_ATTACK = "remote_attack";
   public static final String WISH_PRESET_GHOST_SILENCE = "ghost_silence";
   public static final String WISH_PRESET_STRENGTH = "strength";
   public static final String WISH_PRESET_ESCAPE = "escape";
   public static final String WISH_PRESET_FULL_HEAL = "full_heal";

   public FissuredSpearPurpleItem(Settings settings) {
      super(ToolMaterials.IRON, 5, -2.2F, settings);
   }

   public static String getThrowMode(ItemStack stack) {
      return !stack.hasNbt() ? "suppress" : stack.getNbt().getString("ThrowMode");
   }

   public static void setThrowMode(ItemStack stack, String mode) {
      stack.getOrCreateNbt().putString("ThrowMode", mode);
   }

   public static String getWishPreset(ItemStack stack) {
      return !stack.hasNbt() ? "" : stack.getNbt().getString("WishPreset");
   }

   public static void setWishPreset(ItemStack stack, String preset) {
      stack.getOrCreateNbt().putString("WishPreset", preset);
   }

   public static boolean isShowWishText(ItemStack stack) {
      if (!stack.hasNbt()) {
         return true;
      } else {
         return !stack.getNbt().contains("ShowWishText") ? true : stack.getNbt().getBoolean("ShowWishText");
      }
   }

   public static void setShowWishText(ItemStack stack, boolean show) {
      stack.getOrCreateNbt().putBoolean("ShowWishText", show);
   }

   public boolean isDamageable() {
      return false;
   }

   @Override
   public float getSpiritDamageBonus() {
      return 150.0F;
   }

   @Override
   public float getSpiritDamageMultiplier() {
      return 1.2F;
   }

   @Override
   public void onSpiritWeaponAttack(ItemStack stack, LivingEntity target, LivingEntity attacker) {
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.fissured_spear_purple.description.source"));
      tooltip.add(Text.translatable("item.smfs.fissured_spear_purple.description.desc"));
      tooltip.add(Text.translatable("item.smfs.fissured_spear_purple.description.type"));
      tooltip.add(Text.translatable("item.smfs.fissured_spear_purple.skill.description"));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_multiplier", new Object[]{Math.round(this.getSpiritDamageMultiplier() * 100.0F)}));
   }

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.SPEAR;
   }

   public int getMaxUseTime(ItemStack stack) {
      return 72000;
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.getStackInHand(hand);
      if (world.getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION) {
         return TypedActionResult.fail(itemStack);
      } else if (user.getItemCooldownManager().isCoolingDown(this)) {
         user.setCurrentHand(hand);
         return TypedActionResult.consume(itemStack);
      } else {
         user.setCurrentHand(hand);
         return TypedActionResult.consume(itemStack);
      }
   }

   public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
      if (user instanceof PlayerEntity player) {
         int i = this.getMaxUseTime(stack) - remainingUseTicks;
         String mode = getThrowMode(stack);
         if (i < 10) {
            if (mode.equals("wish") && getWishPreset(stack).equals("ghost_silence")) {
               if (world.isClient) {
                  return;
               }

               GhostDomainManager.handleWishGhostNSkill(player, isShowWishText(stack));
               return;
            }

            if (mode.equals("wish") && getWishPreset(stack).equals("strength")) {
               if (world.isClient) {
                  return;
               }

               GhostDomainManager.handleWishGhostGSkill(player, isShowWishText(stack));
               return;
            }

            if (mode.equals("wish") && getWishPreset(stack).equals("escape")) {
               if (world.isClient) {
                  return;
               }

               GhostDomainManager.handleWishGhostVSkill(player, isShowWishText(stack));
               return;
            }

            if (mode.equals("wish") && getWishPreset(stack).equals("full_heal")) {
               if (world.isClient) {
                  return;
               }

               GhostDomainManager.handleWishGhostJSkill(player, isShowWishText(stack));
               return;
            }

            if (mode.equals("wish")) {
               return;
            }

            if (this.tryFootprintSkill(player, stack, world)) {
               return;
            }
         }

         if (i >= 10) {
            if (i < 10) {
               return;
            }

            float chargeLevel = Math.min(i / 20.0F, 3.0F);
            if (!world.isClient) {
               FissuredSpearEntity spearEntity = new FissuredSpearEntity(ModEntities.FISSURED_SPEAR, player, world);
               spearEntity.setOriginalStack(stack);
               spearEntity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, chargeLevel * 2.5F, 1.0F);
               world.spawnEntity(spearEntity);
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
            if (!world.isClient && mode.equals("wish") && getWishPreset(stack).equals("tracking") && isShowWishText(stack)) {
               player.sendMessage(Text.literal("§6我说长枪投出必定命中眼前生物"), false);
            }

            if (!player.getAbilities().creativeMode) {
               stack.decrement(1);
            }

            player.incrementStat(Stats.USED.getOrCreateStat(this));
         }
      }
   }

   private boolean tryFootprintSkill(PlayerEntity user, ItemStack stack, World world) {
      if (user.getItemCooldownManager().isCoolingDown(this)) {
         return false;
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

            return false;
         }
      }

      if (targetEntity != null && targetEntity.isAlive()) {
         if (!world.isClient()) {
            this.performMediumAttack(world, user, targetEntity);
         }

         user.getItemCooldownManager().set(this, 100);
         return true;
      } else {
         if (!world.isClient() && !this.isAberrationWithGiantShadowGhost(user)) {
            if (blockState.getBlock() instanceof FootprintBlock) {
               FootprintBlockEntity footprintEntity = (FootprintBlockEntity)world.getBlockEntity(playerPos);
               if (footprintEntity != null && footprintEntity.hasValidEntityInfo()) {
                  user.sendMessage(Text.translatable("item.smfs.rusty_firewood_knife.skill.no_target"), true);
               } else {
                  user.sendMessage(Text.translatable("item.smfs.rusty_firewood_knife.skill.no_footprint"), true);
               }
            } else {
               user.sendMessage(Text.translatable("item.smfs.rusty_firewood_knife.skill.not_on_footprint"), true);
            }
         }

         return false;
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
            user.sendMessage(Text.literal("§c目标已无法被攻击"), true);
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

         spawnSpiritAttackParticles(ghost);
      } else if (targetEntity instanceof PlayerEntity targetPlayer) {
         NbtCompound targetSpiritAttributes = PlayerEvents.getSpiritAttributes(targetPlayer);
         float maxSpirit = targetSpiritAttributes.contains("maxSpirit") ? (float)targetSpiritAttributes.getDouble("maxSpirit") : 0.0F;
         if (maxSpirit < 1000.0F) {
            user.sendMessage(Text.literal("§c目标已无法被攻击"), true);
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
         world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.SWING_ATTACK_SOUND, SoundCategory.HOSTILE, 1.0F, 1.0F);
         targetPlayer.playSound(SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0F, 1.0F);
         if (!PlayerEvents.hasGhostType(user, "giant_shadow_ghost") && !PlayerEvents.hasGhostType(user, "complete_shadow_ghost")) {
            float playerDamage = damageAmount * 0.5F;
            PlayerEvents.handleSpiritDamage(user, playerDamage, playerDamage, ModDamageSources.ghost(world));
         }

         spawnSpiritAttackParticles(targetPlayer);
      } else {
         float spiritDamage = this.getSpiritDamageBonus() * 4.0F;
         ModEvents.processingSpiritDamage.set(true);

         try {
            spawnSpiritAttackParticles(targetEntity);
            targetEntity.damage(ModDamageSources.ghost(world), spiritDamage);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.SWING_ATTACK_SOUND, SoundCategory.HOSTILE, 1.0F, 1.0F);
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

   public static void spawnSpiritAttackParticles(LivingEntity target) {
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
   public void registerControllers(ControllerRegistrar registrar) {
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   @Override
   public double getTick(Object itemStack) {
      return RenderUtils.getCurrentTick();
   }

   @Override
   public void createRenderer(Consumer<Object> consumer) {
      consumer.accept(new FissuredSpearItemRenderer());
   }

   @Override
   public Supplier<Object> getRenderProvider() {
      return GeoItem.makeRenderer(this);
   }
}
