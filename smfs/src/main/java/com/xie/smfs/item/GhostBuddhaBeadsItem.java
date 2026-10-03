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
      super(settings.method_7895(200));
   }

   public void method_7888(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      super.method_7888(stack, world, entity, slot, selected);
      if (!world.field_9236 && entity instanceof PlayerEntity player) {
         this.checkAndConsumeDurability(stack, player);
         this.clearPlayerEffects(player);
         if (stack.method_7919() >= stack.method_7936()) {
            this.killPlayer(player, world);
            return;
         }
      }
   }

   private void checkAndConsumeDurability(ItemStack stack, PlayerEntity player) {
      World world = player.method_37908();
      List<GhostEntity> nearbyGhosts = world.method_8390(GhostEntity.class, player.method_5829().method_1014(48.0), ghostx -> true);
      if (!nearbyGhosts.isEmpty()) {
         float maxConsumptionRate = 0.0F;

         for (GhostEntity ghost : nearbyGhosts) {
            double distance = player.method_5858(ghost);
            if (distance <= 2304.0) {
               double actualDistance = Math.sqrt(distance);
               float consumptionRate = this.calculateConsumptionRate(actualDistance, ghost.getTerrorLevel());
               if (consumptionRate > maxConsumptionRate) {
                  maxConsumptionRate = consumptionRate;
               }
            }
         }

         if (maxConsumptionRate > 0.0F && world.field_9229.method_43057() < maxConsumptionRate) {
            stack.method_7956(1, player, p -> p.method_20236(p.method_6058()));
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
      boolean hasRedGhostDomain = player.method_6059(ModEffects.RED_GHOST_DOMAIN);
      boolean hasGreenGhostDomain = player.method_6059(ModEffects.GREEN_GHOST_DOMAIN);
      boolean hasBlueGhostDomain = player.method_6059(ModEffects.BLUE_GHOST_DOMAIN);
      boolean hasGrayGhostDomain = player.method_6059(ModEffects.GRAY_GHOST_DOMAIN);
      boolean hasGoldenGhostDomain = player.method_6059(ModEffects.GOLDEN_GHOST_DOMAIN);
      boolean hasPurpleGhostDomain = player.method_6059(ModEffects.PURPLE_GHOST_DOMAIN);
      boolean hasBlackGhostDomain = player.method_6059(ModEffects.BLACK_GHOST_DOMAIN);
      boolean hasCyanGhostDomain = player.method_6059(ModEffects.CYAN_GHOST_DOMAIN);
      boolean hasThickFog = player.method_6059(ModEffects.THICK_FOG);
      boolean hasGhostSuppression = player.method_6059(ModEffects.GHOST_SUPPRESSION);
      StatusEffectInstance ghostSuppressionInstance = null;
      if (hasGhostSuppression) {
         ghostSuppressionInstance = player.method_6112(ModEffects.GHOST_SUPPRESSION);
      }

      boolean hasDream = player.method_6059(ModEffects.DREAM);
      StatusEffectInstance dreamInstance = null;
      if (hasDream) {
         dreamInstance = player.method_6112(ModEffects.DREAM);
      }

      boolean hasNightVision = player.method_6059(StatusEffects.field_5925);
      StatusEffectInstance nightVisionInstance = null;
      if (hasNightVision) {
         nightVisionInstance = player.method_6112(StatusEffects.field_5925);
      }

      boolean hasResistance = player.method_6059(StatusEffects.field_5907);
      StatusEffectInstance resistanceInstance = null;
      if (hasResistance) {
         resistanceInstance = player.method_6112(StatusEffects.field_5907);
      }

      player.method_6012();
      if (hasRedGhostDomain) {
         player.method_6092(new StatusEffectInstance(ModEffects.RED_GHOST_DOMAIN, 100, 0));
      }

      if (hasGreenGhostDomain) {
         player.method_6092(new StatusEffectInstance(ModEffects.GREEN_GHOST_DOMAIN, 100, 0));
      }

      if (hasBlueGhostDomain) {
         player.method_6092(new StatusEffectInstance(ModEffects.BLUE_GHOST_DOMAIN, 100, 0));
      }

      if (hasGrayGhostDomain) {
         player.method_6092(new StatusEffectInstance(ModEffects.GRAY_GHOST_DOMAIN, 100, 0));
      }

      if (hasGoldenGhostDomain) {
         player.method_6092(new StatusEffectInstance(ModEffects.GOLDEN_GHOST_DOMAIN, 100, 0));
      }

      if (hasPurpleGhostDomain) {
         player.method_6092(new StatusEffectInstance(ModEffects.PURPLE_GHOST_DOMAIN, 100, 0));
      }

      if (hasBlackGhostDomain) {
         player.method_6092(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN, 100, 0));
      }

      if (hasCyanGhostDomain) {
         player.method_6092(new StatusEffectInstance(ModEffects.CYAN_GHOST_DOMAIN, 100, 0));
      }

      if (hasThickFog) {
         player.method_6092(new StatusEffectInstance(ModEffects.THICK_FOG, 100, 0));
      }

      if (hasGhostSuppression && ghostSuppressionInstance != null) {
         player.method_6092(
            new StatusEffectInstance(ModEffects.GHOST_SUPPRESSION, ghostSuppressionInstance.method_5584(), ghostSuppressionInstance.method_5578())
         );
      }

      if (hasDream && dreamInstance != null) {
         player.method_6092(new StatusEffectInstance(ModEffects.DREAM, dreamInstance.method_5584(), dreamInstance.method_5578()));
      }

      if (hasNightVision && nightVisionInstance != null) {
         player.method_6092(new StatusEffectInstance(StatusEffects.field_5925, nightVisionInstance.method_5584(), nightVisionInstance.method_5578()));
      }

      if (hasResistance && resistanceInstance != null) {
         player.method_6092(new StatusEffectInstance(StatusEffects.field_5907, resistanceInstance.method_5584(), resistanceInstance.method_5578()));
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
      if (!world.field_9236) {
         DamageSource damageSource = ModDamageSources.of(world, ModDamageSources.GHOST);
         player.method_5643(damageSource, 99999.0F);
      }
   }

   public boolean method_7886(ItemStack stack) {
      return true;
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.method_43471("item.smfs.ghost_buddha_beads.description.source"));
      tooltip.add(Text.method_43471("item.smfs.ghost_buddha_beads.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.ghost_buddha_beads.description.type"));
   }
}
