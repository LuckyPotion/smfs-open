package com.xie.smfs.client.screen;

import com.xie.smfs.common.events.PlayerEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostDeadlockSelectScreen extends Screen {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostDeadlockSelectScreen");
   private static final Identifier BACKGROUND_TEXTURE = new Identifier("smfs", "textures/gui/ghost_control_background.png");
   private static final Identifier BACKGROUND_TEXTURE2 = new Identifier("smfs", "textures/gui/ghost_control_background2.png");
   private static final Identifier LOCK_TEXTURE = new Identifier("smfs", "textures/gui/lock.png");
   private static final int TEXTURE_WIDTH = 177;
   private static final int TEXTURE_HEIGHT = 183;
   private static final int CIRCLE_CENTER_X = 87;
   private static final int CIRCLE_CENTER_Y = 73;
   private static final int CIRCLE_RADIUS = 30;
   private static final int SLOT_SIZE = 18;
   private static final int SLOT_OFFSET = 8;
   private int guiX;
   private int guiY;
   private boolean selectionMade = false;

   public GhostDeadlockSelectScreen() {
      super(Text.translatable("screen.smfs.ghost_deadlock_select"));
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

         if (!isUnlocked) {
            context.drawTexture(LOCK_TEXTURE, x, y, 0, 0, 18, 18);
         } else {
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
      Text title = Text.translatable("screen.smfs.ghost_deadlock_select.title");
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
                  context.drawTooltip(this.textRenderer, Text.translatable("screen.smfs.ghost_deadlock_select.click"), mouseX, mouseY);
               } else if (PlayerEvents.isGhostSlotOccupied(this.client.player, i)) {
                  context.drawTooltip(this.textRenderer, Text.translatable("screen.smfs.ghost_deadlock_select.deadlocked"), mouseX, mouseY);
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
                  context.drawTooltip(this.textRenderer, Text.translatable("screen.smfs.ghost_deadlock_select.click"), mouseX, mouseY);
               } else if (PlayerEvents.isGhostSlotOccupied(this.client.player, i)) {
                  context.drawTooltip(this.textRenderer, Text.translatable("screen.smfs.ghost_deadlock_select.deadlocked"), mouseX, mouseY);
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
         return slotData.getBoolean("occupied") && !slotData.getBoolean("slotDeadlocked");
      } else {
         return false;
      }
   }

   private void sendSelection(int slotIndex) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeInt(slotIndex);
      ClientPlayNetworking.send(new Identifier("smfs", "ghost_deadlock_select"), buf);
   }

   public void close() {
      if (!this.selectionMade) {
         PacketByteBuf buf = PacketByteBufs.create();
         buf.writeInt(-1);
         ClientPlayNetworking.send(new Identifier("smfs", "ghost_deadlock_select"), buf);
      }

      super.close();
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   public boolean shouldPause() {
      return false;
   }
}
