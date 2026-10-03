package com.xie.smfs.block.entity;

import com.xie.smfs.block.GoldCoffinBlock;
import com.xie.smfs.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GoldCoffinBlockEntity extends BlockEntity implements GeoBlockEntity {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public GoldCoffinBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.GOLD_COFFIN_BLOCK_ENTITY, pos, state);
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "state_controller", 0, state -> {
         BlockState blockState = this.getCachedState();
         if (!blockState.contains(GoldCoffinBlock.OPEN)) {
            return PlayState.STOP;
         }

         boolean isOpen = (Boolean)blockState.get(GoldCoffinBlock.OPEN);
         if (isOpen) {
            state.setAndContinue(RawAnimation.begin().thenPlay("animation.gold_coffin.open"));
         } else {
            state.setAndContinue(RawAnimation.begin().thenPlay("animation.gold_coffin.close"));
         }

         return PlayState.CONTINUE;
      }));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
