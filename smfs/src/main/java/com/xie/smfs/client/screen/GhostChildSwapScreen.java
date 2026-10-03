package com.xie.smfs.client.screen;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.util.GhostUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GhostChildSwapScreen extends Screen {
   private static final Identifier BACKGROUND_TEXTURE = new Identifier("smfs", "textures/gui/ghost_control_background.png");
   private static final Identifier BACKGROUND_TEXTURE2 = new Identifier("smfs", "textures/gui/ghost_control_background2.png");
   private static final int TEXTURE_WIDTH = 177;
   private static final int TEXTURE_HEIGHT = 183;
   private static final int CIRCLE_CENTER_X = 87;
   private static final int CIRCLE_CENTER_Y = 73;
   private static final int CIRCLE_RADIUS = 30;
   private static final int SLOT_SIZE = 18;
   private static final int SLOT_OFFSET = 8;
   private int guiX;
   private int guiY;
   private String selectedGhostType;
   private boolean selectionMade = false;

   public GhostChildSwapScreen(String ghostType) {
      super(Text.translatable("screen.smfs.ghost_child_swap"));
      this.selectedGhostType = ghostType;
   }

   protected void init() {
      super.init();
      this.guiX = (this.width - 177) / 2;
      this.guiY = (this.height - 183) / 2;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      boolean isAberration = this.isPlayerAberration();
      Identifier bg = isAberration ? BACKGROUND_TEXTURE : BACKGROUND_TEXTURE2;
      context.drawTexture(bg, this.guiX, this.guiY, 0, 0, 177, 183);
      int centerX = this.guiX + 87;
      int centerY = this.guiY + 73;
      if (this.client != null && this.client.player != null) {
         for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2) * i / 6.0;
            int x = centerX + (int)(30.0 * Math.cos(angle)) - 8;
            int y = centerY + (int)(30.0 * Math.sin(angle)) - 8;
            this.drawSlot(context, x, y, i);
         }

         int[] customX = new int[]{32, 129, 21, 140};
         int[] customY = new int[]{29, 29, 73, 73};

         for (int i = 6; i < 10; i++) {
            int index = i - 6;
            int x = this.guiX + customX[index];
            int y = this.guiY + customY[index];
            this.drawSlot(context, x, y, i);
         }

         this.drawTooltip(context, mouseX, mouseY);
         this.drawTitle(context);
         super.render(context, mouseX, mouseY, delta);
      }
   }

   private void drawSlot(DrawContext context, int x, int y, int slotIndex) {
      if (this.client != null && this.client.player != null) {
         NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.client.player);
         String slotKey = "Slot" + slotIndex;
         boolean isUnlocked = true;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            isUnlocked = slotData.getBoolean("unlocked");
         }

         if (isUnlocked) {
            ItemStack itemStack = PlayerEvents.getGhostSlotItem(this.client.player, slotIndex);
            boolean isOccupied = PlayerEvents.isGhostSlotOccupied(this.client.player, slotIndex);
            if (isOccupied && !itemStack.isEmpty()) {
               context.drawItem(itemStack, x + 1, y + 1);
               context.drawItemInSlot(this.textRenderer, itemStack, x + 1, y + 1);
            }
         }
      }
   }

   private void drawTitle(DrawContext context) {
      String ghostName = this.formatGhostName(this.selectedGhostType);
      Text title = Text.literal("选择槽位放置: " + ghostName);
      int titleWidth = this.textRenderer.getWidth(title);
      context.drawText(this.textRenderer, title, this.guiX + (177 - titleWidth) / 2, this.guiY + 130, 16777215, true);
   }

   private void drawTooltip(DrawContext context, int mouseX, int mouseY) {
      if (this.client != null && this.client.player != null) {
         int centerX = this.guiX + 87;
         int centerY = this.guiY + 73;

         for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2) * i / 6.0;
            int x = centerX + (int)(30.0 * Math.cos(angle)) - 8;
            int y = centerY + (int)(30.0 * Math.sin(angle)) - 8;
            if (mouseX >= x && mouseX <= x + 18 && mouseY >= y && mouseY <= y + 18) {
               if (this.isSlotSelectable(i)) {
                  if (PlayerEvents.isGhostSlotOccupied(this.client.player, i)) {
                     context.drawTooltip(this.textRenderer, Text.literal("点击替换该槽位的鬼"), mouseX, mouseY);
                  } else {
                     context.drawTooltip(this.textRenderer, Text.literal("点击放入该槽位"), mouseX, mouseY);
                  }
               }

               return;
            }
         }

         int[] customX = new int[]{32, 129, 21, 140};
         int[] customY = new int[]{29, 29, 73, 73};

         for (int i = 6; i < 10; i++) {
            int index = i - 6;
            int x = this.guiX + customX[index];
            int y = this.guiY + customY[index];
            if (mouseX >= x && mouseX <= x + 18 && mouseY >= y && mouseY <= y + 18) {
               if (this.isSlotSelectable(i)) {
                  if (PlayerEvents.isGhostSlotOccupied(this.client.player, i)) {
                     context.drawTooltip(this.textRenderer, Text.literal("点击替换该槽位的鬼"), mouseX, mouseY);
                  } else {
                     context.drawTooltip(this.textRenderer, Text.literal("点击放入该槽位"), mouseX, mouseY);
                  }
               }

               return;
            }
         }
      }
   }

   private boolean isPlayerAberration() {
      if (this.client != null && this.client.player != null) {
         NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.client.player);
         String slot0Key = "Slot0";
         String slot3Key = "Slot3";
         return ghostSlots.contains(slot0Key) && ghostSlots.contains(slot3Key)
            ? ghostSlots.getCompound(slot0Key).getBoolean("unlocked") && ghostSlots.getCompound(slot3Key).getBoolean("unlocked")
            : false;
      } else {
         return false;
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.client != null && this.client.player != null) {
         int centerX = this.guiX + 87;
         int centerY = this.guiY + 73;

         for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2) * i / 6.0;
            int x = centerX + (int)(30.0 * Math.cos(angle)) - 8;
            int y = centerY + (int)(30.0 * Math.sin(angle)) - 8;
            if (mouseX >= x && mouseX <= x + 18 && mouseY >= y && mouseY <= y + 18 && this.isSlotSelectable(i)) {
               this.selectionMade = true;
               this.sendSelection(i);
               this.close();
               return true;
            }
         }

         int[] customX = new int[]{32, 129, 21, 140};
         int[] customY = new int[]{29, 29, 73, 73};

         for (int i = 6; i < 10; i++) {
            int index = i - 6;
            int x = this.guiX + customX[index];
            int y = this.guiY + customY[index];
            if (mouseX >= x && mouseX <= x + 18 && mouseY >= y && mouseY <= y + 18 && this.isSlotSelectable(i)) {
               this.selectionMade = true;
               this.sendSelection(i);
               this.close();
               return true;
            }
         }

         return super.mouseClicked(mouseX, mouseY, button);
      } else {
         return false;
      }
   }

   private boolean isSlotSelectable(int slotIndex) {
      if (this.client != null && this.client.player != null) {
         NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.client.player);
         String slotKey = "Slot" + slotIndex;
         if (!ghostSlots.contains(slotKey)) {
            return false;
         }

         NbtCompound slotData = ghostSlots.getCompound(slotKey);
         return slotData.getBoolean("unlocked");
      } else {
         return false;
      }
   }

   private void sendSelection(int slotIndex) {
      this.client.player.networkHandler.sendChatCommand("xie ghost_swap " + this.selectedGhostType + " " + slotIndex);
   }

   private String formatGhostName(String ghostType) {
      return GhostUtils.getGhostDisplayName(ghostType);
   }

   public void close() {
      super.close();
      if (this.client != null && this.client.player != null) {
         this.client.setScreen(new GodScreen());
      }
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   public boolean shouldPause() {
      return false;
   }
}
