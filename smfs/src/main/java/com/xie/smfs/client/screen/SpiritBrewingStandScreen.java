package com.xie.smfs.client.screen;

import com.xie.smfs.block.entity.SpiritBrewingStandScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SpiritBrewingStandScreen extends HandledScreen<SpiritBrewingStandScreenHandler> {
   private static final Identifier TEXTURE = new Identifier("smfs", "textures/gui/spirit_brewing_stand.png");
   private static final Identifier SLOT_TEXTURE = new Identifier("smfs", "textures/gui/slot_type3.png");

   public SpiritBrewingStandScreen(SpiritBrewingStandScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.backgroundHeight = 166;
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      int x = (this.width - this.backgroundWidth) / 2;
      int y = (this.height - this.backgroundHeight) / 2;
      context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
      this.drawSlotBackgrounds(context, x, y);
      int brewingTime = ((SpiritBrewingStandScreenHandler)this.handler).getPropertyDelegate().get(0);
      int maxBrewingTime = ((SpiritBrewingStandScreenHandler)this.handler).getPropertyDelegate().get(1);
      if (maxBrewingTime > 0) {
         int remainingSeconds = brewingTime / 20;
         int minutes = remainingSeconds / 60;
         int seconds = remainingSeconds % 60;
         String timeText = String.format("%d:%02d", minutes, seconds);
         context.getMatrices().push();
         context.getMatrices().scale(0.8F, 0.8F, 1.0F);
         context.drawTextWithShadow(this.textRenderer, timeText, (int)((x + 116) / 0.8F), (int)((y + 55) / 0.8F), 16777215);
         context.getMatrices().pop();
      }
   }

   private void drawSlotBackgrounds(DrawContext context, int x, int y) {
      context.drawTexture(SLOT_TEXTURE, x + 24, y + 19, 0, 0, 16, 16);
      context.drawTexture(SLOT_TEXTURE, x + 56, y + 19, 0, 0, 16, 16);
      context.drawTexture(SLOT_TEXTURE, x + 40, y + 53, 0, 0, 16, 16);
      context.drawTexture(SLOT_TEXTURE, x + 40, y + 35, 0, 0, 16, 16);
      context.drawTexture(SLOT_TEXTURE, x + 116, y + 35, 0, 0, 16, 16);

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            context.drawTexture(SLOT_TEXTURE, x + 9 + j * 18, y + 85 + i * 18, 0, 0, 16, 16);
         }
      }

      for (int i = 0; i < 9; i++) {
         context.drawTexture(SLOT_TEXTURE, x + 9 + i * 18, y + 143, 0, 0, 16, 16);
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }

   protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
      super.drawMouseoverTooltip(context, x, y);
   }
}
