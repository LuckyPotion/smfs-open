package com.xie.smfs.item;

import com.xie.smfs.block.GraveMoundBlock;
import com.xie.smfs.client.renderer.item.GraveMoundItemRenderer;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtils;

public class GraveMoundBlockItem extends BlockItem implements GeoItem {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public GraveMoundBlockItem(GraveMoundBlock block, Settings settings) {
      super(block, settings);
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
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
      consumer.accept(new GraveMoundItemRenderer());
   }

   @Override
   public Supplier<Object> getRenderProvider() {
      return GeoItem.makeRenderer(this);
   }
}
