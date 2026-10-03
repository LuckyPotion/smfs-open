package com.xie.smfs.item;

import com.xie.smfs.client.renderer.item.EerieMusicBoxRenderer;
import com.xie.smfs.registry.ModEffects;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtils;

public class EerieMusicBoxItem extends Item implements GeoItem {
   private static final String TAG_IS_OPEN = "IsOpen";
   private static final String TAG_COOLDOWN_END = "CooldownEnd";
   private static final int CURSE_DURATION = 18000;
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public EerieMusicBoxItem() {
      super(new FabricItemSettings().maxCount(1));
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity player, Hand hand) {
      ItemStack stack = player.method_5998(hand);
      if (!world.field_9236) {
         if (this.isOnCooldown(stack)) {
            long remainingTicks = this.getRemainingCooldown(stack);
            int remainingSeconds = (int)(remainingTicks / 20L);
            player.method_7353(Text.method_43471("item.smfs.eerie_music_box.on_cooldown").method_10852(Text.method_43470(" (" + remainingSeconds + "秒)")), true);
            return TypedActionResult.method_22430(stack);
         }

         this.openMusicBox(player, stack, world);
      }

      return TypedActionResult.method_22427(stack);
   }

   private boolean isMusicBoxOpen(ItemStack stack) {
      NbtCompound nbt = stack.method_7948();
      return nbt.method_10577("IsOpen");
   }

   public void setMusicBoxOpen(ItemStack stack, boolean open) {
      NbtCompound nbt = stack.method_7948();
      nbt.method_10556("IsOpen", open);
   }

   private void openMusicBox(PlayerEntity player, ItemStack stack, World world) {
      this.setMusicBoxOpen(stack, true);
      player.method_6092(new StatusEffectInstance(ModEffects.MUSIC_BOX_CURSE, 18000, 0));
      this.setCooldown(stack, 18000);
      player.method_7353(Text.method_43471("item.smfs.eerie_music_box.opened"), true);
   }

   public boolean isOnCooldown(ItemStack stack) {
      NbtCompound nbt = stack.method_7969();
      if (nbt != null && nbt.method_10545("CooldownEnd")) {
         long cooldownEnd = nbt.method_10537("CooldownEnd");
         return cooldownEnd > 0L && this.getCurrentTick() < cooldownEnd;
      } else {
         return false;
      }
   }

   private long getRemainingCooldown(ItemStack stack) {
      NbtCompound nbt = stack.method_7969();
      if (nbt != null && nbt.method_10545("CooldownEnd")) {
         long cooldownEnd = nbt.method_10537("CooldownEnd");
         long remaining = cooldownEnd - this.getCurrentTick();
         return Math.max(0L, remaining);
      } else {
         return 0L;
      }
   }

   private void setCooldown(ItemStack stack, int durationTicks) {
      NbtCompound nbt = stack.method_7948();
      nbt.method_10544("CooldownEnd", this.getCurrentTick() + durationTicks);
   }

   private long getCurrentTick() {
      return System.currentTimeMillis() / 50L;
   }

   public ActionResult method_7884(ItemUsageContext context) {
      return ActionResult.field_5811;
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.eerie_music_box.description.source"));
      tooltip.add(Text.method_43471("item.smfs.eerie_music_box.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.eerie_music_box.description.type"));
      tooltip.add(Text.method_43471("item.smfs.eerie_music_box.description.effect"));
      tooltip.add(Text.method_43471("item.smfs.eerie_music_box.description.side_effect"));
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
      consumer.accept(new EerieMusicBoxRenderer());
   }

   @Override
   public Supplier<Object> getRenderProvider() {
      return GeoItem.makeRenderer(this);
   }
}
