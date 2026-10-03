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
      this.field_2779 = 166;
   }

   protected void method_2389(DrawContext context, float delta, int mouseX, int mouseY) {
      int x = (this.field_22789 - this.field_2792) / 2;
      int y = (this.field_22790 - this.field_2779) / 2;
      context.method_25302(TEXTURE, x, y, 0, 0, this.field_2792, this.field_2779);
      this.drawSlotBackgrounds(context, x, y);
      int brewingTime = ((SpiritBrewingStandScreenHandler)this.field_2797).getPropertyDelegate().method_17390(0);
      int maxBrewingTime = ((SpiritBrewingStandScreenHandler)this.field_2797).getPropertyDelegate().method_17390(1);
      if (maxBrewingTime > 0) {
         int remainingSeconds = brewingTime / 20;
         int minutes = remainingSeconds / 60;
         int seconds = remainingSeconds % 60;
         String timeText = String.format("%d:%02d", minutes, seconds);
         context.method_51448().method_22903();
         context.method_51448().method_22905(0.8F, 0.8F, 1.0F);
         context.method_25303(this.field_22793, timeText, (int)((x + 116) / 0.8F), (int)((y + 55) / 0.8F), 16777215);
         context.method_51448().method_22909();
      }
   }

   private void drawSlotBackgrounds(DrawContext context, int x, int y) {
      context.method_25302(SLOT_TEXTURE, x + 24, y + 19, 0, 0, 16, 16);
      context.method_25302(SLOT_TEXTURE, x + 56, y + 19, 0, 0, 16, 16);
      context.method_25302(SLOT_TEXTURE, x + 40, y + 53, 0, 0, 16, 16);
      context.method_25302(SLOT_TEXTURE, x + 40, y + 35, 0, 0, 16, 16);
      context.method_25302(SLOT_TEXTURE, x + 116, y + 35, 0, 0, 16, 16);

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            context.method_25302(SLOT_TEXTURE, x + 9 + j * 18, y + 85 + i * 18, 0, 0, 16, 16);
         }
      }

      for (int i = 0; i < 9; i++) {
         context.method_25302(SLOT_TEXTURE, x + 9 + i * 18, y + 143, 0, 0, 16, 16);
      }
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      super.method_25394(context, mouseX, mouseY, delta);
      this.method_2380(context, mouseX, mouseY);
   }

   protected void method_2380(DrawContext context, int x, int y) {
      super.method_2380(context, x, y);
   }
}
