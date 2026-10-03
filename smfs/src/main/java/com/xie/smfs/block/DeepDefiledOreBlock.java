package com.xie.smfs.block;

import com.xie.smfs.damage.ModDamageSources;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet.Builder;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DeepDefiledOreBlock extends DefiledOreBlock {
   public DeepDefiledOreBlock(Settings settings) {
      super(settings);
   }

   @Override
   public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
      float baseSpeed = super.calcBlockBreakingDelta(state, player, world, pos);
      return baseSpeed * 0.25F;
   }

   @Override
   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      ItemStack heldItem = player.getMainHandStack();
      boolean isUsingGoldenPickaxe = heldItem.isOf(Items.GOLDEN_PICKAXE);
      if (!isUsingGoldenPickaxe && !world.isClient) {
         player.damage(ModDamageSources.ghost(world), 8.0F);
         double currentMaxHealth = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).getBaseValue();
         if (currentMaxHealth > 1.0) {
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(currentMaxHealth - 1.0);
            if (player.getHealth() > currentMaxHealth - 1.0) {
               player.setHealth((float)(currentMaxHealth - 1.0));
            }
         }

         player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 1));
         player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 200, 1));
         world.playSound(null, pos, SoundEvents.ENTITY_PHANTOM_BITE, SoundCategory.PLAYERS, 1.0F, 0.8F);
      }

      super.onBreak(world, pos, state, player);
   }

   @Override
   public List<ItemStack> getDroppedStacks(BlockState state, Builder builder) {
      return List.of(new ItemStack(this));
   }
}
