package com.xie.smfs.client.screen;

import com.xie.smfs.network.packets.ui.c2s.SubmitCodenameC2SPacket;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class CodenameInputScreen extends Screen {
   private static final int WIDTH = 250;
   private static final int HEIGHT = 150;
   private static final int MAX_CODENAME_LENGTH = 16;
   private TextFieldWidget codenameField;
   private final int entityId;
   private boolean submitted = false;

   public CodenameInputScreen(int entityId) {
      super(Text.literal("设置代号"));
      this.entityId = entityId;
   }

   protected void init() {
      super.init();
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      int panelLeft = centerX - 125;
      int panelTop = centerY - 75;
      int fieldWidth = 200;
      int fieldX = centerX - fieldWidth / 2;
      int fieldY = panelTop + 50;
      this.codenameField = new TextFieldWidget(this.textRenderer, fieldX, fieldY, fieldWidth, 20, Text.literal("请输入代号"));
      this.codenameField.setMaxLength(16);
      this.codenameField.setFocused(true);
      this.addDrawableChild(this.codenameField);
      int buttonWidth = 60;
      int buttonHeight = 20;
      int buttonY = panelTop + 90;
      int confirmX = centerX - buttonWidth - 5;
      int cancelX = centerX + 5;
      this.addDrawableChild(
         ButtonWidget.builder(Text.literal("确定"), button -> this.onConfirm()).dimensions(confirmX, buttonY, buttonWidth, buttonHeight).build()
      );
      this.addDrawableChild(ButtonWidget.builder(Text.literal("取消"), button -> this.onCancel()).dimensions(cancelX, buttonY, buttonWidth, buttonHeight).build());
   }

   private void onConfirm() {
      if (!this.submitted) {
         String codename = this.codenameField.getText().trim();
         if (!codename.isEmpty()) {
            this.submitted = true;
            SubmitCodenameC2SPacket.send(codename);
            this.close();
         }
      }
   }

   private void onCancel() {
      this.close();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.close();
         return true;
      }

      if (keyCode != 257 && keyCode != 335) {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }

      this.onConfirm();
      return true;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      super.render(context, mouseX, mouseY, delta);
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      int panelLeft = centerX - 125;
      int panelTop = centerY - 75;
      context.fill(panelLeft, panelTop, panelLeft + 250, panelTop + 150, -872415232);
      context.drawBorder(panelLeft, panelTop, 250, 150, -11184811);
      context.drawTextWithShadow(this.textRenderer, Text.literal("设置你的代号"), panelLeft + 125 - this.textRenderer.getWidth("设置你的代号") / 2, panelTop + 15, 16766720);
      context.drawTextWithShadow(
         this.textRenderer, Text.literal("代号将显示在信息界面上"), panelLeft + 125 - this.textRenderer.getWidth("代号将显示在信息界面上") / 2, panelTop + 32, 16777215
      );
      if (this.codenameField != null) {
         this.codenameField.render(context, mouseX, mouseY, delta);
      }
   }

   public boolean shouldPause() {
      return false;
   }
}
