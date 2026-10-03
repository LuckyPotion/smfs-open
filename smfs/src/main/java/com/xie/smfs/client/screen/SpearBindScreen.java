package com.xie.smfs.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SpearBindScreen extends Screen {
   private final String correctAnswer;
   private final boolean isSettingOwner;
   private final SpearBindScreen.ItemStackProvider stackProvider;
   private static final int CARD_WIDTH = 80;
   private static final int CARD_HEIGHT = 120;
   private static final int CARD_SPACING = 40;
   private int hoveredCard = -1;

   public SpearBindScreen(Text title, String correctAnswer, boolean isSettingOwner, SpearBindScreen.ItemStackProvider provider) {
      super(title);
      this.correctAnswer = correctAnswer;
      this.isSettingOwner = isSettingOwner;
      this.stackProvider = provider;
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      super.method_25394(context, mouseX, mouseY, delta);
      int centerX = this.field_22789 / 2;
      int centerY = this.field_22790 / 2;
      String titleText = this.isSettingOwner ? "请设置手持位置" : "请选择手持位置";
      context.method_25300(this.field_22793, titleText, centerX, 40, 16777215);
      int totalWidth = 320;
      int startX = centerX - totalWidth / 2;
      this.drawCard(context, startX, centerY - 60, 0, mouseX, mouseY);
      this.drawCard(context, startX + 80 + 40, centerY - 60, 1, mouseX, mouseY);
      this.drawCard(context, startX + 160 + 80, centerY - 60, 2, mouseX, mouseY);
   }

   private void drawCard(DrawContext context, int x, int y, int index, int mouseX, int mouseY) {
      boolean isHovered = mouseX >= x && mouseX <= x + 80 && mouseY >= y && mouseY <= y + 120;
      if (isHovered) {
         this.hoveredCard = index;
      } else if (this.hoveredCard == index) {
         this.hoveredCard = -1;
      }

      int bgColor = isHovered ? -1438366584 : -1440603580;
      int borderColor = isHovered ? -10066177 : -12303224;
      context.method_25294(x + 2, y + 2, x + 80 - 2, y + 120 - 2, bgColor);
      context.method_49601(x, y, 80, 120, borderColor);
      String[] labels = new String[]{"上部", "中部", "下部"};
      String label = labels[index];
      int textWidth = this.field_22793.method_1727(label);
      int textX = x + (80 - textWidth) / 2;
      int textY = y + (120 - 9) / 2;
      context.method_27535(this.field_22793, Text.method_43470(label).method_27692(Formatting.field_1065), textX, textY, 16777215);
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      int centerX = this.field_22789 / 2;
      int centerY = this.field_22790 / 2;
      int totalWidth = 320;
      int startX = centerX - totalWidth / 2;
      if (mouseX >= startX && mouseX <= startX + 80 && mouseY >= centerY - 60 && mouseY <= centerY + 60) {
         this.stackProvider.onSelect("upper");
         this.method_25419();
         return true;
      } else if (mouseX >= startX + 80 + 40 && mouseX <= startX + 160 + 40 && mouseY >= centerY - 60 && mouseY <= centerY + 60) {
         this.stackProvider.onSelect("middle");
         this.method_25419();
         return true;
      } else if (mouseX >= startX + 160 + 80 && mouseX <= startX + 240 + 80 && mouseY >= centerY - 60 && mouseY <= centerY + 60) {
         this.stackProvider.onSelect("lower");
         this.method_25419();
         return true;
      } else {
         return super.method_25402(mouseX, mouseY, button);
      }
   }

   public boolean method_25422() {
      return false;
   }

   public interface ItemStackProvider {
      void onSelect(String string);
   }
}
