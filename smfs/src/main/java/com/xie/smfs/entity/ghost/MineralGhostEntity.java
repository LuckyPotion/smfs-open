package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.registry.ModEffects;
import java.util.Objects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class MineralGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'C';

   public static Builder createLivingAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 90000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.18)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0);
   }

   public MineralGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1000, 100, 50, 0.1F);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(90000.0);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.18);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(8.0);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (RedGhostCandleItem.isHoldingCandle(player)) {
         return false;
      } else if (CoffinEffectManager.isPlayerInGoldCoffin(player)) {
         return false;
      } else if (player.hasStatusEffect(ModEffects.SPIRIT_IMMUNITY)) {
         return false;
      } else {
         return !this.isPlayerInRange(player) ? false : this.hasMineralsInInventory(player);
      }
   }

   private boolean isPlayerInRange(PlayerEntity player) {
      return this.squaredDistanceTo(player) <= 1024.0;
   }

   private boolean hasMineralsInInventory(PlayerEntity player) {
      for (int i = 0; i < player.getInventory().size(); i++) {
         ItemStack stack = player.getInventory().getStack(i);
         if (!stack.isEmpty() && this.isMineral(stack)) {
            return true;
         }
      }

      return false;
   }

   private boolean isMineral(ItemStack stack) {
      return stack.isOf(Items.COAL)
         || stack.isOf(Items.IRON_INGOT)
         || stack.isOf(Items.GOLD_INGOT)
         || stack.isOf(Items.DIAMOND)
         || stack.isOf(Items.EMERALD)
         || stack.isOf(Items.LAPIS_LAZULI)
         || stack.isOf(Items.REDSTONE)
         || stack.isOf(Items.QUARTZ)
         || stack.isOf(Items.NETHERITE_INGOT)
         || stack.isOf(Items.COPPER_INGOT)
         || stack.isOf(Items.AMETHYST_SHARD)
         || stack.isOf(Items.RAW_IRON)
         || stack.isOf(Items.RAW_GOLD)
         || stack.isOf(Items.RAW_COPPER)
         || stack.isOf(Items.COAL_ORE)
         || stack.isOf(Items.IRON_ORE)
         || stack.isOf(Items.GOLD_ORE)
         || stack.isOf(Items.DIAMOND_ORE)
         || stack.isOf(Items.EMERALD_ORE)
         || stack.isOf(Items.LAPIS_ORE)
         || stack.isOf(Items.REDSTONE_ORE)
         || stack.isOf(Items.NETHER_QUARTZ_ORE)
         || stack.isOf(Items.DEEPSLATE_COAL_ORE)
         || stack.isOf(Items.DEEPSLATE_IRON_ORE)
         || stack.isOf(Items.DEEPSLATE_GOLD_ORE)
         || stack.isOf(Items.DEEPSLATE_DIAMOND_ORE)
         || stack.isOf(Items.DEEPSLATE_EMERALD_ORE)
         || stack.isOf(Items.DEEPSLATE_LAPIS_ORE)
         || stack.isOf(Items.DEEPSLATE_REDSTONE_ORE)
         || stack.isOf(Items.COPPER_ORE)
         || stack.isOf(Items.DEEPSLATE_COPPER_ORE)
         || stack.isOf(Items.ANCIENT_DEBRIS);
   }
}
