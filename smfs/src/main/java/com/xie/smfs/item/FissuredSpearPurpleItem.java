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
      super(ToolMaterials.field_8923, 5, -2.2F, settings);
   }

   public static String getThrowMode(ItemStack stack) {
      return !stack.method_7985() ? "suppress" : stack.method_7969().method_10558("ThrowMode");
   }

   public static void setThrowMode(ItemStack stack, String mode) {
      stack.method_7948().method_10582("ThrowMode", mode);
   }

   public static String getWishPreset(ItemStack stack) {
      return !stack.method_7985() ? "" : stack.method_7969().method_10558("WishPreset");
   }

   public static void setWishPreset(ItemStack stack, String preset) {
      stack.method_7948().method_10582("WishPreset", preset);
   }

   public static boolean isShowWishText(ItemStack stack) {
      if (!stack.method_7985()) {
         return true;
      } else {
         return !stack.method_7969().method_10545("ShowWishText") ? true : stack.method_7969().method_10577("ShowWishText");
      }
   }

   public static void setShowWishText(ItemStack stack, boolean show) {
      stack.method_7948().method_10556("ShowWishText", show);
   }

   public boolean method_7846() {
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

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.fissured_spear_purple.description.source"));
      tooltip.add(Text.method_43471("item.smfs.fissured_spear_purple.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.fissured_spear_purple.description.type"));
      tooltip.add(Text.method_43471("item.smfs.fissured_spear_purple.skill.description"));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_multiplier", new Object[]{Math.round(this.getSpiritDamageMultiplier() * 100.0F)}));
   }

   public UseAction method_7853(ItemStack stack) {
      return UseAction.field_8951;
   }

   public int method_7881(ItemStack stack) {
      return 72000;
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.method_5998(hand);
      if (world.method_27983() == Smfs.GHOST_DREAM_DIMENSION) {
         return TypedActionResult.method_22431(itemStack);
      } else if (user.method_7357().method_7904(this)) {
         user.method_6019(hand);
         return TypedActionResult.method_22428(itemStack);
      } else {
         user.method_6019(hand);
         return TypedActionResult.method_22428(itemStack);
      }
   }

   public void method_7840(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
      if (user instanceof PlayerEntity player) {
         int i = this.method_7881(stack) - remainingUseTicks;
         String mode = getThrowMode(stack);
         if (i < 10) {
            if (mode.equals("wish") && getWishPreset(stack).equals("ghost_silence")) {
               if (world.field_9236) {
                  return;
               }

               GhostDomainManager.handleWishGhostNSkill(player, isShowWishText(stack));
               return;
            }

            if (mode.equals("wish") && getWishPreset(stack).equals("strength")) {
               if (world.field_9236) {
                  return;
               }

               GhostDomainManager.handleWishGhostGSkill(player, isShowWishText(stack));
               return;
            }

            if (mode.equals("wish") && getWishPreset(stack).equals("escape")) {
               if (world.field_9236) {
                  return;
               }

               GhostDomainManager.handleWishGhostVSkill(player, isShowWishText(stack));
               return;
            }

            if (mode.equals("wish") && getWishPreset(stack).equals("full_heal")) {
               if (world.field_9236) {
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
            if (!world.field_9236) {
               FissuredSpearEntity spearEntity = new FissuredSpearEntity(ModEntities.FISSURED_SPEAR, player, world);
               spearEntity.setOriginalStack(stack);
               spearEntity.method_24919(player, player.method_36455(), player.method_36454(), 0.0F, chargeLevel * 2.5F, 1.0F);
               world.method_8649(spearEntity);
            }

            world.method_43128(
               null, player.method_23317(), player.method_23318(), player.method_23321(), SoundEvents.field_15001, SoundCategory.field_15248, 1.0F, 1.0F
            );
            if (!world.field_9236 && mode.equals("wish") && getWishPreset(stack).equals("tracking") && isShowWishText(stack)) {
               player.method_7353(Text.method_43470("§6我说长枪投出必定命中眼前生物"), false);
            }

            if (!player.method_31549().field_7477) {
               stack.method_7934(1);
            }

            player.method_7259(Stats.field_15372.method_14956(this));
         }
      }
   }

   private boolean tryFootprintSkill(PlayerEntity user, ItemStack stack, World world) {
      if (user.method_7357().method_7904(this)) {
         return false;
      }

      LivingEntity targetEntity = null;
      BlockPos playerPos = user.method_24515();
      BlockState blockState = world.method_8320(playerPos);
      if (blockState.method_26204() instanceof FootprintBlock) {
         FootprintBlockEntity footprintEntity = (FootprintBlockEntity)world.method_8321(playerPos);
         if (footprintEntity != null && footprintEntity.hasValidEntityInfo()) {
            targetEntity = footprintEntity.getTargetEntity(world);
         }
      }

      if (targetEntity == null && this.isAberrationWithGiantShadowGhost(user)) {
         HitResult hitResult = user.method_5745(30.0, 0.0F, false);
         if (hitResult.method_17783() == Type.field_1332) {
            BlockHitResult blockHit = (BlockHitResult)hitResult;
            BlockPos hitPos = blockHit.method_17777();
            BlockState hitState = world.method_8320(hitPos);
            if (hitState.method_26204() instanceof FootprintBlock) {
               FootprintBlockEntity footprintEntity = (FootprintBlockEntity)world.method_8321(hitPos);
               if (footprintEntity != null && footprintEntity.hasValidEntityInfo()) {
                  targetEntity = footprintEntity.getTargetEntity(world);
               }
            }
         }

         if (targetEntity == null) {
            if (!world.method_8608()) {
               user.method_7353(Text.method_43470("§c请对准脚印方块使用"), true);
            }

            return false;
         }
      }

      if (targetEntity != null && targetEntity.method_5805()) {
         if (!world.method_8608()) {
            this.performMediumAttack(world, user, targetEntity);
         }

         user.method_7357().method_7906(this, 100);
         return true;
      } else {
         if (!world.method_8608() && !this.isAberrationWithGiantShadowGhost(user)) {
            if (blockState.method_26204() instanceof FootprintBlock) {
               FootprintBlockEntity footprintEntity = (FootprintBlockEntity)world.method_8321(playerPos);
               if (footprintEntity != null && footprintEntity.hasValidEntityInfo()) {
                  user.method_7353(Text.method_43471("item.smfs.rusty_firewood_knife.skill.no_target"), true);
               } else {
                  user.method_7353(Text.method_43471("item.smfs.rusty_firewood_knife.skill.no_footprint"), true);
               }
            } else {
               user.method_7353(Text.method_43471("item.smfs.rusty_firewood_knife.skill.not_on_footprint"), true);
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
            user.method_7353(Text.method_43470("§c目标已无法被攻击"), true);
            return;
         }

         int damageAmount = (int)(maxSpiritualStrength * 0.2);
         int newMaxSpirit = Math.max(0, maxSpiritualStrength - damageAmount);
         ghost.setMaxSpiritualStrength(newMaxSpirit);
         int currentSpirit = ghost.getSpiritualStrength();
         int newCurrentSpirit = Math.min(currentSpirit, newMaxSpirit);
         ghost.setSpiritualStrength(newCurrentSpirit);
         world.method_43128(
            null, user.method_23317(), user.method_23318(), user.method_23321(), ModSounds.SWING_ATTACK_SOUND, SoundCategory.field_15251, 1.0F, 1.0F
         );
         if (!PlayerEvents.hasGhostType(user, "giant_shadow_ghost") && !PlayerEvents.hasGhostType(user, "complete_shadow_ghost")) {
            float playerDamage = damageAmount * 0.5F;
            PlayerEvents.handleSpiritDamage(user, playerDamage, playerDamage, ModDamageSources.ghost(world));
         }

         spawnSpiritAttackParticles(ghost);
      } else if (targetEntity instanceof PlayerEntity targetPlayer) {
         NbtCompound targetSpiritAttributes = PlayerEvents.getSpiritAttributes(targetPlayer);
         float maxSpirit = targetSpiritAttributes.method_10545("maxSpirit") ? (float)targetSpiritAttributes.method_10574("maxSpirit") : 0.0F;
         if (maxSpirit < 1000.0F) {
            user.method_7353(Text.method_43470("§c目标已无法被攻击"), true);
            return;
         }

         float damageAmount = maxSpirit * 0.2F;
         float newMaxSpirit = maxSpirit - damageAmount;
         NbtCompound spiritData = PlayerEvents.getSpiritAttributes(targetPlayer);
         spiritData.method_10549("maxSpirit", newMaxSpirit);
         float currentSpirit = spiritData.method_10545("currentSpirit") ? (float)spiritData.method_10574("currentSpirit") : 0.0F;
         float newCurrentSpirit = Math.min(currentSpirit, newMaxSpirit);
         spiritData.method_10549("currentSpirit", newCurrentSpirit);
         PlayerEvents.setSpiritAttributes(targetPlayer, spiritData);
         world.method_43128(
            null, user.method_23317(), user.method_23318(), user.method_23321(), ModSounds.SWING_ATTACK_SOUND, SoundCategory.field_15251, 1.0F, 1.0F
         );
         targetPlayer.method_5783(SoundEvents.field_14941, 1.0F, 1.0F);
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
            targetEntity.method_5643(ModDamageSources.ghost(world), spiritDamage);
            world.method_43128(
               null, user.method_23317(), user.method_23318(), user.method_23321(), ModSounds.SWING_ATTACK_SOUND, SoundCategory.field_15251, 1.0F, 1.0F
            );
            targetEntity.method_5783(SoundEvents.field_14941, 1.0F, 1.0F);
            if (ModConfig.getInstance().showActionBarInfo) {
               user.method_7353(
                  Text.method_43470(
                     "§a对 §f" + targetEntity.method_5477().getString() + " §a造成了 §f" + new DecimalFormat("#.###").format(spiritDamage) + " §a点灵异伤害"
                  ),
                  true
               );
            }

            if (!targetEntity.method_5805() && user instanceof ServerPlayerEntity serverPlayer) {
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
      if (!target.method_37908().method_8608()) {
         ServerWorld serverWorld = (ServerWorld)target.method_37908();
         Vec3d pos = target.method_19538();

         for (int i = 0; i < 25; i++) {
            double offsetX = (target.method_6051().method_43058() - 0.5) * 2.0;
            double offsetY = target.method_6051().method_43058() * 1.5 + 0.5;
            double offsetZ = (target.method_6051().method_43058() - 0.5) * 2.0;
            double velocityX = (target.method_6051().method_43058() - 0.5) * 0.2;
            double velocityY = target.method_6051().method_43058() * 0.3 + 0.1;
            double velocityZ = (target.method_6051().method_43058() - 0.5) * 0.2;
            serverWorld.method_14199(
               ParticleTypes.field_11249, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5
            );
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
