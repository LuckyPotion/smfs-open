package com.xie.smfs.item;

import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.registry.ModEffects;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GhostBuddhaBeadsItem extends Item {
   private static final int BASE_DURABILITY = 200;
   private static final double DISTANCE_THRESHOLD = 48.0;

   public GhostBuddhaBeadsItem(Settings settings) {
      super(settings.maxDamage(200));
   }

   public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      super.inventoryTick(stack, world, entity, slot, selected);
      if (!world.isClient && entity instanceof PlayerEntity player) {
         this.checkAndConsumeDurability(stack, player);
         this.clearPlayerEffects(player);
         if (stack.getDamage() >= stack.getMaxDamage()) {
            this.killPlayer(player, world);
            return;
         }
      }
   }

   private void checkAndConsumeDurability(ItemStack stack, PlayerEntity player) {
      World world = player.getWorld();
      List<GhostEntity> nearbyGhosts = world.getEntitiesByClass(GhostEntity.class, player.getBoundingBox().expand(48.0), ghostx -> true);
      if (!nearbyGhosts.isEmpty()) {
         float maxConsumptionRate = 0.0F;

         for (GhostEntity ghost : nearbyGhosts) {
            double distance = player.squaredDistanceTo(ghost);
            if (distance <= 2304.0) {
               double actualDistance = Math.sqrt(distance);
               float consumptionRate = this.calculateConsumptionRate(actualDistance, ghost.getTerrorLevel());
               if (consumptionRate > maxConsumptionRate) {
                  maxConsumptionRate = consumptionRate;
               }
            }
         }

         if (maxConsumptionRate > 0.0F && world.random.nextFloat() < maxConsumptionRate) {
            stack.damage(1, player, p -> p.sendToolBreakStatus(p.getActiveHand()));
         }
      }
   }

   private float calculateConsumptionRate(double actualDistance, char terrorLevel) {
      float distanceFactor = actualDistance < 48.0 ? (float)(1.0 - actualDistance / 48.0) : 0.0F;
      float levelFactor = this.getLevelFactor(terrorLevel);
      float baseRate = 0.1F;
      return Math.min(baseRate * distanceFactor * levelFactor, 0.8F);
   }

   private float getLevelFactor(char level) {
      return switch (level) {
         case 'A' -> 1.5F;
         case 'B' -> 1.2F;
         case 'C' -> 1.0F;
         case 'D' -> 0.8F;
         case 'S' -> 2.0F;
         default -> 1.0F;
      };
   }

   private void clearPlayerEffects(PlayerEntity player) {
      boolean hasRedGhostDomain = player.hasStatusEffect(ModEffects.RED_GHOST_DOMAIN);
      boolean hasGreenGhostDomain = player.hasStatusEffect(ModEffects.GREEN_GHOST_DOMAIN);
      boolean hasBlueGhostDomain = player.hasStatusEffect(ModEffects.BLUE_GHOST_DOMAIN);
      boolean hasGrayGhostDomain = player.hasStatusEffect(ModEffects.GRAY_GHOST_DOMAIN);
      boolean hasGoldenGhostDomain = player.hasStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN);
      boolean hasPurpleGhostDomain = player.hasStatusEffect(ModEffects.PURPLE_GHOST_DOMAIN);
      boolean hasBlackGhostDomain = player.hasStatusEffect(ModEffects.BLACK_GHOST_DOMAIN);
      boolean hasCyanGhostDomain = player.hasStatusEffect(ModEffects.CYAN_GHOST_DOMAIN);
      boolean hasThickFog = player.hasStatusEffect(ModEffects.THICK_FOG);
      boolean hasGhostSuppression = player.hasStatusEffect(ModEffects.GHOST_SUPPRESSION);
      StatusEffectInstance ghostSuppressionInstance = null;
      if (hasGhostSuppression) {
         ghostSuppressionInstance = player.getStatusEffect(ModEffects.GHOST_SUPPRESSION);
      }

      boolean hasDream = player.hasStatusEffect(ModEffects.DREAM);
      StatusEffectInstance dreamInstance = null;
      if (hasDream) {
         dreamInstance = player.getStatusEffect(ModEffects.DREAM);
      }

      boolean hasNightVision = player.hasStatusEffect(StatusEffects.NIGHT_VISION);
      StatusEffectInstance nightVisionInstance = null;
      if (hasNightVision) {
         nightVisionInstance = player.getStatusEffect(StatusEffects.NIGHT_VISION);
      }

      boolean hasResistance = player.hasStatusEffect(StatusEffects.RESISTANCE);
      StatusEffectInstance resistanceInstance = null;
      if (hasResistance) {
         resistanceInstance = player.getStatusEffect(StatusEffects.RESISTANCE);
      }

      player.clearStatusEffects();
      if (hasRedGhostDomain) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.RED_GHOST_DOMAIN, 100, 0));
      }

      if (hasGreenGhostDomain) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.GREEN_GHOST_DOMAIN, 100, 0));
      }

      if (hasBlueGhostDomain) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.BLUE_GHOST_DOMAIN, 100, 0));
      }

      if (hasGrayGhostDomain) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.GRAY_GHOST_DOMAIN, 100, 0));
      }

      if (hasGoldenGhostDomain) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.GOLDEN_GHOST_DOMAIN, 100, 0));
      }

      if (hasPurpleGhostDomain) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.PURPLE_GHOST_DOMAIN, 100, 0));
      }

      if (hasBlackGhostDomain) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN, 100, 0));
      }

      if (hasCyanGhostDomain) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.CYAN_GHOST_DOMAIN, 100, 0));
      }

      if (hasThickFog) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.THICK_FOG, 100, 0));
      }

      if (hasGhostSuppression && ghostSuppressionInstance != null) {
         player.addStatusEffect(
            new StatusEffectInstance(ModEffects.GHOST_SUPPRESSION, ghostSuppressionInstance.getDuration(), ghostSuppressionInstance.getAmplifier())
         );
      }

      if (hasDream && dreamInstance != null) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.DREAM, dreamInstance.getDuration(), dreamInstance.getAmplifier()));
      }

      if (hasNightVision && nightVisionInstance != null) {
         player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, nightVisionInstance.getDuration(), nightVisionInstance.getAmplifier()));
      }

      if (hasResistance && resistanceInstance != null) {
         player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, resistanceInstance.getDuration(), resistanceInstance.getAmplifier()));
      }
   }

   private boolean isUnsuffixedGhostDomainEffect(StatusEffect effect) {
      return effect == ModEffects.RED_GHOST_DOMAIN
         || effect == ModEffects.GREEN_GHOST_DOMAIN
         || effect == ModEffects.BLUE_GHOST_DOMAIN
         || effect == ModEffects.GRAY_GHOST_DOMAIN
         || effect == ModEffects.GOLDEN_GHOST_DOMAIN
         || effect == ModEffects.PURPLE_GHOST_DOMAIN
         || effect == ModEffects.BLACK_GHOST_DOMAIN
         || effect == ModEffects.CYAN_GHOST_DOMAIN;
   }

   private void killPlayer(PlayerEntity player, World world) {
      if (!world.isClient) {
         DamageSource damageSource = ModDamageSources.of(world, ModDamageSources.GHOST);
         player.damage(damageSource, 99999.0F);
      }
   }

   public boolean hasGlint(ItemStack stack) {
      return true;
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.translatable("item.smfs.ghost_buddha_beads.description.source"));
      tooltip.add(Text.translatable("item.smfs.ghost_buddha_beads.description.desc"));
      tooltip.add(Text.translatable("item.smfs.ghost_buddha_beads.description.type"));
   }
}
