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
      super(Text.translatable("screen.smfs.tutorial.title"));
   }

   protected void init() {
      super.init();
      PlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null) {
         this.currentStory = this.tutorialManager.getCurrentStory();
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      context.drawTexture(BACKGROUND_TEXTURE, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
      super.render(context, mouseX, mouseY, delta);
      if (this.currentStory != null) {
         List<OrderedText> wrappedText = this.textRenderer.wrapLines(Text.of(this.currentStory), 300);
         int y = 50;

         for (OrderedText line : wrappedText) {
            context.drawTextWithShadow(this.textRenderer, line, this.width / 2 - 150, y, 16711680);
            y += 9 + 4;
         }
      }
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   public void close() {
      this.client.setScreen(null);
   }
}
