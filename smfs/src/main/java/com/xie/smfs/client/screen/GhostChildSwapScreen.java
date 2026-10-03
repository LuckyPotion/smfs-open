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
      super(Text.method_43471("screen.smfs.ghost_child_swap"));
      this.selectedGhostType = ghostType;
   }

   protected void method_25426() {
      super.method_25426();
      this.guiX = (this.field_22789 - 177) / 2;
      this.guiY = (this.field_22790 - 183) / 2;
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      boolean isAberration = this.isPlayerAberration();
      Identifier bg = isAberration ? BACKGROUND_TEXTURE : BACKGROUND_TEXTURE2;
      context.method_25302(bg, this.guiX, this.guiY, 0, 0, 177, 183);
      int centerX = this.guiX + 87;
      int centerY = this.guiY + 73;
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
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
         super.method_25394(context, mouseX, mouseY, delta);
      }
   }

   private void drawSlot(DrawContext context, int x, int y, int slotIndex) {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.field_22787.field_1724);
         String slotKey = "Slot" + slotIndex;
         boolean isUnlocked = true;
         if (ghostSlots.method_10545(slotKey)) {
            NbtCompound slotData = ghostSlots.method_10562(slotKey);
            isUnlocked = slotData.method_10577("unlocked");
         }

         if (isUnlocked) {
            ItemStack itemStack = PlayerEvents.getGhostSlotItem(this.field_22787.field_1724, slotIndex);
            boolean isOccupied = PlayerEvents.isGhostSlotOccupied(this.field_22787.field_1724, slotIndex);
            if (isOccupied && !itemStack.method_7960()) {
               context.method_51427(itemStack, x + 1, y + 1);
               context.method_51431(this.field_22793, itemStack, x + 1, y + 1);
            }
         }
      }
   }

   private void drawTitle(DrawContext context) {
      String ghostName = this.formatGhostName(this.selectedGhostType);
      Text title = Text.method_43470("选择槽位放置: " + ghostName);
      int titleWidth = this.field_22793.method_27525(title);
      context.method_51439(this.field_22793, title, this.guiX + (177 - titleWidth) / 2, this.guiY + 130, 16777215, true);
   }

   private void drawTooltip(DrawContext context, int mouseX, int mouseY) {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         int centerX = this.guiX + 87;
         int centerY = this.guiY + 73;

         for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2) * i / 6.0;
            int x = centerX + (int)(30.0 * Math.cos(angle)) - 8;
            int y = centerY + (int)(30.0 * Math.sin(angle)) - 8;
            if (mouseX >= x && mouseX <= x + 18 && mouseY >= y && mouseY <= y + 18) {
               if (this.isSlotSelectable(i)) {
                  if (PlayerEvents.isGhostSlotOccupied(this.field_22787.field_1724, i)) {
                     context.method_51438(this.field_22793, Text.method_43470("点击替换该槽位的鬼"), mouseX, mouseY);
                  } else {
                     context.method_51438(this.field_22793, Text.method_43470("点击放入该槽位"), mouseX, mouseY);
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
                  if (PlayerEvents.isGhostSlotOccupied(this.field_22787.field_1724, i)) {
                     context.method_51438(this.field_22793, Text.method_43470("点击替换该槽位的鬼"), mouseX, mouseY);
                  } else {
                     context.method_51438(this.field_22793, Text.method_43470("点击放入该槽位"), mouseX, mouseY);
                  }
               }

               return;
            }
         }
      }
   }

   private boolean isPlayerAberration() {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.field_22787.field_1724);
         String slot0Key = "Slot0";
         String slot3Key = "Slot3";
         return ghostSlots.method_10545(slot0Key) && ghostSlots.method_10545(slot3Key)
            ? ghostSlots.method_10562(slot0Key).method_10577("unlocked") && ghostSlots.method_10562(slot3Key).method_10577("unlocked")
            : false;
      } else {
         return false;
      }
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         int centerX = this.guiX + 87;
         int centerY = this.guiY + 73;

         for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2) * i / 6.0;
            int x = centerX + (int)(30.0 * Math.cos(angle)) - 8;
            int y = centerY + (int)(30.0 * Math.sin(angle)) - 8;
            if (mouseX >= x && mouseX <= x + 18 && mouseY >= y && mouseY <= y + 18 && this.isSlotSelectable(i)) {
               this.selectionMade = true;
               this.sendSelection(i);
               this.method_25419();
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
               this.method_25419();
               return true;
            }
         }

         return super.method_25402(mouseX, mouseY, button);
      } else {
         return false;
      }
   }

   private boolean isSlotSelectable(int slotIndex) {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.field_22787.field_1724);
         String slotKey = "Slot" + slotIndex;
         if (!ghostSlots.method_10545(slotKey)) {
            return false;
         }

         NbtCompound slotData = ghostSlots.method_10562(slotKey);
         return slotData.method_10577("unlocked");
      } else {
         return false;
      }
   }

   private void sendSelection(int slotIndex) {
      this.field_22787.field_1724.field_3944.method_45730("xie ghost_swap " + this.selectedGhostType + " " + slotIndex);
   }

   private String formatGhostName(String ghostType) {
      return GhostUtils.getGhostDisplayName(ghostType);
   }

   public void method_25419() {
      super.method_25419();
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         this.field_22787.method_1507(new GodScreen());
      }
   }

   public boolean method_25422() {
      return true;
   }

   public boolean method_25421() {
      return false;
   }
}
