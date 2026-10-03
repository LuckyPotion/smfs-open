package com.xie.smfs.client.screen;

import com.xie.smfs.event.screen.GhostChildCultivationScreenHandler;
import com.xie.smfs.event.screen.GhostChildFeedScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GhostChildFeedScreen extends HandledScreen<GhostChildFeedScreenHandler> {
   private static final Identifier BACKGROUND_TEXTURE = new Identifier("smfs", "textures/gui/ghost_taming_background.png");
   private static final int TEXTURE_WIDTH = 176;
   private static final int TEXTURE_HEIGHT = 166;
   private static final int FEED_BUTTON_X = 45;
   private static final int CLOSE_BUTTON_X = 95;
   private static final int BUTTON_Y = 50;
   private static final int BUTTON_WIDTH = 40;
   private static final int BUTTON_HEIGHT = 16;
   private ButtonWidget feedButton;
   private ButtonWidget closeButton;

   public GhostChildFeedScreen(GhostChildFeedScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.field_2792 = 176;
      this.field_2779 = 166;
      this.field_25270 = this.field_2779 - 94;
   }

   protected void method_25426() {
      super.method_25426();
      this.field_2776 = (this.field_22789 - this.field_2792) / 2;
      this.field_2800 = (this.field_22790 - this.field_2779) / 2;
      this.feedButton = ButtonWidget.method_46430(Text.method_43470("喂食"), button -> {
         if (this.field_22787 != null && this.field_2797 != null && this.field_22787.field_1724 != null) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(((GhostChildFeedScreenHandler)this.field_2797).field_7763);
            ClientPlayNetworking.send(GhostChildFeedScreenHandler.FEED_BUTTON_CLICK_PACKET_ID, buf);
         }
      }).method_46434(this.field_2776 + 45 + 3, this.field_2800 + 50 + 5, 40, 16).method_46431();
      this.closeButton = ButtonWidget.method_46430(
            Text.method_43470("关闭"),
            button -> this.field_22787
               .method_1507(
                  new GhostChildCultivationScreen(
                     new GhostChildCultivationScreenHandler(0, this.field_22787.field_1724.method_31548()),
                     this.field_22787.field_1724.method_31548(),
                     Text.method_43471("screen.smfs.ghost_child_cultivation")
                  )
               )
         )
         .method_46434(this.field_2776 + 95 + 3, this.field_2800 + 50 + 5, 40, 16)
         .method_46431();
      this.method_37063(this.feedButton);
      this.method_37063(this.closeButton);
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      super.method_25394(context, mouseX, mouseY, delta);
      this.method_2380(context, mouseX, mouseY);
   }

   protected void method_2389(DrawContext context, float delta, int mouseX, int mouseY) {
      context.method_25302(BACKGROUND_TEXTURE, this.field_2776, this.field_2800, 0, 0, this.field_2792, this.field_2779);
   }

   protected void method_2388(DrawContext context, int mouseX, int mouseY) {
      context.method_51439(this.field_22793, this.field_29347, this.field_25269, this.field_25270, 4210752, false);
   }
}
