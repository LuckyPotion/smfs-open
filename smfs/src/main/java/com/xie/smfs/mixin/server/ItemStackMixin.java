package com.xie.smfs.mixin.server;

import com.xie.smfs.item.BaseGhostEyeItem;
import com.xie.smfs.util.WeaponOilHandler;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
   @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
   private void onGetName(CallbackInfoReturnable<Text> cir) {
      ItemStack stack = (ItemStack)this;
      if (stack.method_7909() instanceof BaseGhostEyeItem && BaseGhostEyeItem.isShard(stack)) {
         Text original = cir.getReturnValue();
         cir.setReturnValue(Text.method_43470(original.getString() + "（碎片）"));
      }
   }

   @Inject(method = "getTooltip", at = @At("RETURN"))
   private void onGetTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
      ItemStack stack = (ItemStack)this;
      if (stack.method_7909() instanceof SwordItem) {
         int layers = WeaponOilHandler.getCorpseOilLayers(stack);
         if (layers > 0) {
            List<Text> tooltip = cir.getReturnValue();
            tooltip.add(Text.method_43469("tooltip.smfs.corpse_oil_layers", new Object[]{layers}));
         }
      }

      if (stack.method_7909() instanceof BaseGhostEyeItem && BaseGhostEyeItem.isShard(stack)) {
         List<Text> tooltip = cir.getReturnValue();
         Text title = (Text)(tooltip.isEmpty() ? Text.method_43473() : tooltip.get(0));
         tooltip.clear();
         tooltip.add(title);
         int uses = stack.method_7948().method_10550("ShardUses");
         if (uses <= 0) {
            uses = 2;
         }

         tooltip.add(Text.method_43470("§7被肢解下来的部分灵异，可短暂使用厉鬼力量"));
         tooltip.add(Text.method_43470("§7使用方法：J、V、G、N"));
         tooltip.add(Text.method_43470("§7剩余使用次数： §c" + uses + "§7/§c2"));
      }
   }
}
