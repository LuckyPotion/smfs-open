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
      super(Text.literal("大刀配置"));
      this.broadswordStack = broadswordStack;
      this.loadConfigFromStack();
   }

   private void loadConfigFromStack() {
      NbtCompound nbt = this.broadswordStack.getNbt();
      if (nbt == null) {
         nbt = new NbtCompound();
         this.broadswordStack.setNbt(nbt);
      }

      this.crackFixed = nbt.getBoolean("broadsword_crack_fixed");
      if (!nbt.contains("broadsword_crack_fixed")) {
         this.crackFixed = false;
      }
   }

   private void saveConfigToStack() {
      NbtCompound nbt = this.broadswordStack.getOrCreateNbt();
      nbt.putBoolean("broadsword_crack_fixed", this.crackFixed);
      this.broadswordStack.setNbt(nbt);
   }

   protected void init() {
      super.init();
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      int totalWidgets = 2;
      int totalHeight = 20 * totalWidgets + 10 * (totalWidgets - 1);
      int startY = centerY - totalHeight / 2;
      this.addDrawableChild(
         CyclingButtonWidget.builder(value -> value ? Text.literal("§a固定不变") : Text.literal("§e攻击刷新"))
            .values(Arrays.asList(true, false))
            .initially(this.crackFixed)
            .build(centerX - 80, startY, 160, 20, Text.literal("裂纹模式"), (button, value) -> {
               this.crackFixed = value;
               this.saveConfigToStack();
            })
      );
      int currentY = startY + 20 + 10;
      this.addDrawableChild(
         ButtonWidget.builder(Text.literal("§6立刻刷新"), button -> ScreenTearRenderer.forceRefresh()).dimensions(centerX - 80, currentY, 160, 20).build()
      );
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      super.render(context, mouseX, mouseY, delta);
      int centerX = this.width / 2;
      context.drawCenteredTextWithShadow(this.textRenderer, "§c生锈的大刀配置", centerX, 30, 16777215);
      context.drawCenteredTextWithShadow(this.textRenderer, "§7按 ESC 关闭", centerX, this.height - 20, 11184810);
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.close();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }
}
