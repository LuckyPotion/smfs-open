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
   public float method_9594(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
      float baseSpeed = super.method_9594(state, player, world, pos);
      return baseSpeed * 0.25F;
   }

   @Override
   public void method_9576(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      ItemStack heldItem = player.method_6047();
      boolean isUsingGoldenPickaxe = heldItem.method_31574(Items.field_8335);
      if (!isUsingGoldenPickaxe && !world.field_9236) {
         player.method_5643(ModDamageSources.ghost(world), 8.0F);
         double currentMaxHealth = player.method_5996(EntityAttributes.field_23716).method_6201();
         if (currentMaxHealth > 1.0) {
            player.method_5996(EntityAttributes.field_23716).method_6192(currentMaxHealth - 1.0);
            if (player.method_6032() > currentMaxHealth - 1.0) {
               player.method_6033((float)(currentMaxHealth - 1.0));
            }
         }

         player.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 200, 1));
         player.method_6092(new StatusEffectInstance(StatusEffects.field_5901, 200, 1));
         world.method_8396(null, pos, SoundEvents.field_14729, SoundCategory.field_15248, 1.0F, 0.8F);
      }

      super.method_9576(world, pos, state, player);
   }

   @Override
   public List<ItemStack> method_9560(BlockState state, Builder builder) {
      return List.of(new ItemStack(this));
   }
}
