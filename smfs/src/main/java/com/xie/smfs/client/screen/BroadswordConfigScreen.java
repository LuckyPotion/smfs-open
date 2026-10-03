package com.xie.smfs.client.screen;

import com.xie.smfs.client.preset.ScreenTearRenderer;
import java.util.Arrays;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class BroadswordConfigScreen extends Screen {
   private final ItemStack broadswordStack;
   private boolean crackFixed;
   private static final int BUTTON_WIDTH = 160;
   private static final int BUTTON_HEIGHT = 20;
   private static final int BUTTON_SPACING = 10;

   public BroadswordConfigScreen(ItemStack broadswordStack) {
      super(Text.method_43470("大刀配置"));
      this.broadswordStack = broadswordStack;
      this.loadConfigFromStack();
   }

   private void loadConfigFromStack() {
      NbtCompound nbt = this.broadswordStack.method_7969();
      if (nbt == null) {
         nbt = new NbtCompound();
         this.broadswordStack.method_7980(nbt);
      }

      this.crackFixed = nbt.method_10577("broadsword_crack_fixed");
      if (!nbt.method_10545("broadsword_crack_fixed")) {
         this.crackFixed = false;
      }
   }

   private void saveConfigToStack() {
      NbtCompound nbt = this.broadswordStack.method_7948();
      nbt.method_10556("broadsword_crack_fixed", this.crackFixed);
      this.broadswordStack.method_7980(nbt);
   }

   protected void method_25426() {
      super.method_25426();
      int centerX = this.field_22789 / 2;
      int centerY = this.field_22790 / 2;
      int totalWidgets = 2;
      int totalHeight = 20 * totalWidgets + 10 * (totalWidgets - 1);
      int startY = centerY - totalHeight / 2;
      this.method_37063(
         CyclingButtonWidget.method_32606(value -> value ? Text.method_43470("§a固定不变") : Text.method_43470("§e攻击刷新"))
            .method_32620(Arrays.asList(true, false))
            .method_32619(this.crackFixed)
            .method_32617(centerX - 80, startY, 160, 20, Text.method_43470("裂纹模式"), (button, value) -> {
               this.crackFixed = value;
               this.saveConfigToStack();
            })
      );
      int currentY = startY + 20 + 10;
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43470("§6立刻刷新"), button -> ScreenTearRenderer.forceRefresh())
            .method_46434(centerX - 80, currentY, 160, 20)
            .method_46431()
      );
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      super.method_25394(context, mouseX, mouseY, delta);
      int centerX = this.field_22789 / 2;
      context.method_25300(this.field_22793, "§c生锈的大刀配置", centerX, 30, 16777215);
      context.method_25300(this.field_22793, "§7按 ESC 关闭", centerX, this.field_22790 - 20, 11184810);
   }

   public boolean method_25422() {
      return true;
   }

   public boolean method_25404(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.method_25419();
         return true;
      } else {
         return super.method_25404(keyCode, scanCode, modifiers);
      }
   }
}
