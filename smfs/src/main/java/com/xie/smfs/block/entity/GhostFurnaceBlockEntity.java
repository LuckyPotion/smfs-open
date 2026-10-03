package com.xie.smfs.block.entity;

import com.xie.smfs.recipe.GhostFurnaceRecipeType;
import com.xie.smfs.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GhostFurnaceBlockEntity extends AbstractFurnaceBlockEntity implements GeoAnimatable {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public GhostFurnaceBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.GHOST_FURNACE_BLOCK_ENTITY, pos, state, GhostFurnaceRecipeType.INSTANCE);
   }

   protected Text getContainerName() {
      return Text.translatable("container.smfs.ghost_furnace");
   }

   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
      return new FurnaceScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
   }

   protected int getFuelTime(ItemStack fuel) {
      return super.getFuelTime(fuel);
   }

   public static void tick(World world, BlockPos pos, BlockState state, GhostFurnaceBlockEntity blockEntity) {
      if (!state.isAir()) {
         AbstractFurnaceBlockEntity.tick(world, pos, state, blockEntity);
      }
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   @Override
   public double getTick(Object object) {
      return 0.0;
   }
}
