package com.xie.smfs.client.screen;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.util.GhostUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

public class GodScreen extends Screen {
   private static final int SCREEN_WIDTH = 300;
   private static final int SCREEN_HEIGHT = 250;
   private static final int MAX_VISIBLE_BUTTONS = 6;
   private static final int BUTTON_HEIGHT = 25;
   private static final int BUTTON_SPACING = 5;
   private int screenX;
   private int screenY;
   private List<String> availableGhostTypes;
   private ButtonWidget closeButton;
   private ButtonWidget upButton;
   private ButtonWidget downButton;
   private List<ButtonWidget> ghostButtons;
   private int scrollOffset = 0;

   public GodScreen() {
      super(Text.method_43470("神"));
      this.availableGhostTypes = new ArrayList<>();
      this.ghostButtons = new ArrayList<>();
   }

   protected void method_25426() {
      this.method_37067();
      this.screenX = (this.field_22789 - 300) / 2;
      this.screenY = (this.field_22790 - 250) / 2;
      this.loadAvailableGhostTypes();
      this.createButtons();
   }

   private void createButtons() {
      this.ghostButtons.clear();
      int buttonWidth = 240;
      int startX = this.screenX + 30;
      int startY = this.screenY + 40;
      int maxButtons = Math.min(6, this.availableGhostTypes.size() - this.scrollOffset);

      for (int i = 0; i < maxButtons; i++) {
         int listIndex = this.scrollOffset + i;
         String ghostType = this.availableGhostTypes.get(listIndex);
         int buttonY = startY + i * 30;
         ButtonWidget button = ButtonWidget.method_46430(Text.method_43470(this.formatGhostName(ghostType)), btn -> this.selectGhost(ghostType))
            .method_46434(startX, buttonY, buttonWidth, 25)
            .method_46431();
         this.ghostButtons.add(button);
         this.method_37063(button);
      }

      int totalPages = (this.availableGhostTypes.size() + 6 - 1) / 6;
      if (totalPages > 1) {
         int bottomY = this.screenY + 250 - 30;
         int prevButtonWidth = 50;
         int closeButtonWidth = 50;
         int nextButtonWidth = 50;
         int totalWidth = prevButtonWidth + closeButtonWidth + nextButtonWidth;
         int startBottomX = this.screenX + (300 - totalWidth) / 2;
         this.upButton = ButtonWidget.method_46430(Text.method_43470("上一页"), btn -> {
            if (this.scrollOffset > 0) {
               this.scrollOffset = Math.max(0, this.scrollOffset - 6);
               this.method_25426();
            }
         }).method_46434(startBottomX, bottomY, prevButtonWidth, 20).method_46431();
         this.method_37063(this.upButton);
         this.closeButton = ButtonWidget.method_46430(Text.method_43470("关闭"), btn -> this.method_25419())
            .method_46434(startBottomX + prevButtonWidth, bottomY, closeButtonWidth, 20)
            .method_46431();
         this.method_37063(this.closeButton);
         this.downButton = ButtonWidget.method_46430(Text.method_43470("下一页"), btn -> {
            if (this.scrollOffset < this.availableGhostTypes.size() - 6) {
               this.scrollOffset = Math.min(this.availableGhostTypes.size() - 6, this.scrollOffset + 6);
               this.method_25426();
            }
         }).method_46434(startBottomX + prevButtonWidth + closeButtonWidth, bottomY, nextButtonWidth, 20).method_46431();
         this.method_37063(this.downButton);
      } else {
         this.closeButton = ButtonWidget.method_46430(Text.method_43470("关闭"), btn -> this.method_25419())
            .method_46434(this.screenX + 120, this.screenY + 250 - 30, 60, 20)
            .method_46431();
         this.method_37063(this.closeButton);
      }
   }

   private void loadAvailableGhostTypes() {
      this.availableGhostTypes.clear();
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         try {
            NbtCompound spiritData = PlayerEvents.getCachedData(this.field_22787.field_1724);
            if (spiritData != null && spiritData.method_10545("GhostChildData")) {
               NbtCompound ghostChildData = spiritData.method_10562("GhostChildData");
               if (ghostChildData.method_10545("fedGhostTypes")) {
                  NbtList list = ghostChildData.method_10554("fedGhostTypes", 8);

                  for (int i = 0; i < list.size(); i++) {
                     String ghostType = list.method_10608(i);
                     if (ghostType != null && !ghostType.isEmpty() && !ghostType.startsWith("minecraft:")) {
                        this.availableGhostTypes.add(ghostType);
                     }
                  }
               }
            }
         } catch (Exception var6) {
         }
      }
   }

   private String formatGhostName(String ghostType) {
      return GhostUtils.getGhostDisplayName(ghostType);
   }

   private void selectGhost(String ghostType) {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         this.field_22787.method_1507(new GhostChildSwapScreen(ghostType));
      }
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      int backgroundColor = -2145772994;
      context.method_25294(this.screenX, this.screenY, this.screenX + 300, this.screenY + 250, backgroundColor);
      int borderColor = -10496;
      context.method_49601(this.screenX, this.screenY, 300, 250, borderColor);
      String title = "神";
      int titleWidth = this.field_22793.method_1727(title);
      int titleX = this.screenX + (300 - titleWidth) / 2;
      int titleY = this.screenY + 10;
      context.method_51433(this.field_22793, title, titleX, titleY, -10496, false);
      if (this.availableGhostTypes.isEmpty()) {
         String emptyText = "没有可选择的灵异";
         int textWidth = this.field_22793.method_1727(emptyText);
         int textX = this.screenX + (300 - textWidth) / 2;
         int textY = this.screenY + 125;
         context.method_51433(this.field_22793, emptyText, textX, textY, 16746632, false);
      }

      super.method_25394(context, mouseX, mouseY, delta);
   }

   public void method_25419() {
      super.method_25419();
   }

   public boolean method_25421() {
      return false;
   }
}
