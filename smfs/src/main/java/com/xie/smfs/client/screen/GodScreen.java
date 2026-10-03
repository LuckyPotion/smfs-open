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
      super(Text.literal("神"));
      this.availableGhostTypes = new ArrayList<>();
      this.ghostButtons = new ArrayList<>();
   }

   protected void init() {
      this.clearChildren();
      this.screenX = (this.width - 300) / 2;
      this.screenY = (this.height - 250) / 2;
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
         ButtonWidget button = ButtonWidget.builder(Text.literal(this.formatGhostName(ghostType)), btn -> this.selectGhost(ghostType))
            .dimensions(startX, buttonY, buttonWidth, 25)
            .build();
         this.ghostButtons.add(button);
         this.addDrawableChild(button);
      }

      int totalPages = (this.availableGhostTypes.size() + 6 - 1) / 6;
      if (totalPages > 1) {
         int bottomY = this.screenY + 250 - 30;
         int prevButtonWidth = 50;
         int closeButtonWidth = 50;
         int nextButtonWidth = 50;
         int totalWidth = prevButtonWidth + closeButtonWidth + nextButtonWidth;
         int startBottomX = this.screenX + (300 - totalWidth) / 2;
         this.upButton = ButtonWidget.builder(Text.literal("上一页"), btn -> {
            if (this.scrollOffset > 0) {
               this.scrollOffset = Math.max(0, this.scrollOffset - 6);
               this.init();
            }
         }).dimensions(startBottomX, bottomY, prevButtonWidth, 20).build();
         this.addDrawableChild(this.upButton);
         this.closeButton = ButtonWidget.builder(Text.literal("关闭"), btn -> this.close())
            .dimensions(startBottomX + prevButtonWidth, bottomY, closeButtonWidth, 20)
            .build();
         this.addDrawableChild(this.closeButton);
         this.downButton = ButtonWidget.builder(Text.literal("下一页"), btn -> {
            if (this.scrollOffset < this.availableGhostTypes.size() - 6) {
               this.scrollOffset = Math.min(this.availableGhostTypes.size() - 6, this.scrollOffset + 6);
               this.init();
            }
         }).dimensions(startBottomX + prevButtonWidth + closeButtonWidth, bottomY, nextButtonWidth, 20).build();
         this.addDrawableChild(this.downButton);
      } else {
         this.closeButton = ButtonWidget.builder(Text.literal("关闭"), btn -> this.close())
            .dimensions(this.screenX + 120, this.screenY + 250 - 30, 60, 20)
            .build();
         this.addDrawableChild(this.closeButton);
      }
   }

   private void loadAvailableGhostTypes() {
      this.availableGhostTypes.clear();
      if (this.client != null && this.client.player != null) {
         try {
            NbtCompound spiritData = PlayerEvents.getCachedData(this.client.player);
            if (spiritData != null && spiritData.contains("GhostChildData")) {
               NbtCompound ghostChildData = spiritData.getCompound("GhostChildData");
               if (ghostChildData.contains("fedGhostTypes")) {
                  NbtList list = ghostChildData.getList("fedGhostTypes", 8);

                  for (int i = 0; i < list.size(); i++) {
                     String ghostType = list.getString(i);
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
      if (this.client != null && this.client.player != null) {
         this.client.setScreen(new GhostChildSwapScreen(ghostType));
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      int backgroundColor = -2145772994;
      context.fill(this.screenX, this.screenY, this.screenX + 300, this.screenY + 250, backgroundColor);
      int borderColor = -10496;
      context.drawBorder(this.screenX, this.screenY, 300, 250, borderColor);
      String title = "神";
      int titleWidth = this.textRenderer.getWidth(title);
      int titleX = this.screenX + (300 - titleWidth) / 2;
      int titleY = this.screenY + 10;
      context.drawText(this.textRenderer, title, titleX, titleY, -10496, false);
      if (this.availableGhostTypes.isEmpty()) {
         String emptyText = "没有可选择的灵异";
         int textWidth = this.textRenderer.getWidth(emptyText);
         int textX = this.screenX + (300 - textWidth) / 2;
         int textY = this.screenY + 125;
         context.drawText(this.textRenderer, emptyText, textX, textY, 16746632, false);
      }

      super.render(context, mouseX, mouseY, delta);
   }

   public void close() {
      super.close();
   }

   public boolean shouldPause() {
      return false;
   }
}
