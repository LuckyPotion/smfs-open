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

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      super.render(context, mouseX, mouseY, delta);
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      String titleText = this.isSettingOwner ? "请设置手持位置" : "请选择手持位置";
      context.drawCenteredTextWithShadow(this.textRenderer, titleText, centerX, 40, 16777215);
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
      context.fill(x + 2, y + 2, x + 80 - 2, y + 120 - 2, bgColor);
      context.drawBorder(x, y, 80, 120, borderColor);
      String[] labels = new String[]{"上部", "中部", "下部"};
      String label = labels[index];
      int textWidth = this.textRenderer.getWidth(label);
      int textX = x + (80 - textWidth) / 2;
      int textY = y + (120 - 9) / 2;
      context.drawTextWithShadow(this.textRenderer, Text.literal(label).formatted(Formatting.GOLD), textX, textY, 16777215);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      int totalWidth = 320;
      int startX = centerX - totalWidth / 2;
      if (mouseX >= startX && mouseX <= startX + 80 && mouseY >= centerY - 60 && mouseY <= centerY + 60) {
         this.stackProvider.onSelect("upper");
         this.close();
         return true;
      } else if (mouseX >= startX + 80 + 40 && mouseX <= startX + 160 + 40 && mouseY >= centerY - 60 && mouseY <= centerY + 60) {
         this.stackProvider.onSelect("middle");
         this.close();
         return true;
      } else if (mouseX >= startX + 160 + 80 && mouseX <= startX + 240 + 80 && mouseY >= centerY - 60 && mouseY <= centerY + 60) {
         this.stackProvider.onSelect("lower");
         this.close();
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public interface ItemStackProvider {
      void onSelect(String string);
   }
}
