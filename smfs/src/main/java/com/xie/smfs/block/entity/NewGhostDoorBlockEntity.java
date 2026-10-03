package com.xie.smfs.block.entity;

import com.xie.smfs.block.NewGhostDoorBlock;
import com.xie.smfs.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NewGhostDoorBlockEntity extends BlockEntity implements GeoBlockEntity {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public NewGhostDoorBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.NEW_GHOST_DOOR_BLOCK_ENTITY, pos, state);
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "state_controller", 0, this::handleAnimation));
   }

   private PlayState handleAnimation(AnimationState<NewGhostDoorBlockEntity> state) {
      BlockState blockState = this.method_11010();
      if (!blockState.method_28498(NewGhostDoorBlock.OPEN)) {
         return PlayState.STOP;
      }

      boolean isOpen = (Boolean)blockState.method_11654(NewGhostDoorBlock.OPEN);
      if (isOpen) {
         state.getController().setAnimation(RawAnimation.begin().thenPlay("animation.new_ghost_door.open"));
      } else {
         state.getController().setAnimation(RawAnimation.begin().thenPlay("animation.new_ghost_door.close"));
      }

      return PlayState.CONTINUE;
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
