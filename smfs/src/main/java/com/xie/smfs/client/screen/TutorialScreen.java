package com.xie.smfs.client.screen;

import com.xie.smfs.client.data.ClientTutorialManager;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TutorialScreen extends Screen {
   private static final Identifier BACKGROUND_TEXTURE = new Identifier("smfs", "textures/gui/tutorial_ground.png");
   private final ClientTutorialManager tutorialManager = ClientTutorialManager.getInstance();
   private String currentStory = null;

   public TutorialScreen() {
      super(Text.method_43471("screen.smfs.tutorial.title"));
   }

   protected void method_25426() {
      super.method_25426();
      PlayerEntity player = MinecraftClient.method_1551().field_1724;
      if (player != null) {
         this.currentStory = this.tutorialManager.getCurrentStory();
      }
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      context.method_25290(BACKGROUND_TEXTURE, 0, 0, 0.0F, 0.0F, this.field_22789, this.field_22790, this.field_22789, this.field_22790);
      super.method_25394(context, mouseX, mouseY, delta);
      if (this.currentStory != null) {
         List<OrderedText> wrappedText = this.field_22793.method_1728(Text.method_30163(this.currentStory), 300);
         int y = 50;

         for (OrderedText line : wrappedText) {
            context.method_35720(this.field_22793, line, this.field_22789 / 2 - 150, y, 16711680);
            y += 9 + 4;
         }
      }
   }

   public boolean method_25422() {
      return true;
   }

   public void method_25419() {
      this.field_22787.method_1507(null);
   }
}
