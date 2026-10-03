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
      super(Text.method_43470("设置代号"));
      this.entityId = entityId;
   }

   protected void method_25426() {
      super.method_25426();
      int centerX = this.field_22789 / 2;
      int centerY = this.field_22790 / 2;
      int panelLeft = centerX - 125;
      int panelTop = centerY - 75;
      int fieldWidth = 200;
      int fieldX = centerX - fieldWidth / 2;
      int fieldY = panelTop + 50;
      this.codenameField = new TextFieldWidget(this.field_22793, fieldX, fieldY, fieldWidth, 20, Text.method_43470("请输入代号"));
      this.codenameField.method_1880(16);
      this.codenameField.method_25365(true);
      this.method_37063(this.codenameField);
      int buttonWidth = 60;
      int buttonHeight = 20;
      int buttonY = panelTop + 90;
      int confirmX = centerX - buttonWidth - 5;
      int cancelX = centerX + 5;
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43470("确定"), button -> this.onConfirm())
            .method_46434(confirmX, buttonY, buttonWidth, buttonHeight)
            .method_46431()
      );
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43470("取消"), button -> this.onCancel()).method_46434(cancelX, buttonY, buttonWidth, buttonHeight).method_46431()
      );
   }

   private void onConfirm() {
      if (!this.submitted) {
         String codename = this.codenameField.method_1882().trim();
         if (!codename.isEmpty()) {
            this.submitted = true;
            SubmitCodenameC2SPacket.send(codename);
            this.method_25419();
         }
      }
   }

   private void onCancel() {
      this.method_25419();
   }

   public boolean method_25404(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.method_25419();
         return true;
      }

      if (keyCode != 257 && keyCode != 335) {
         return super.method_25404(keyCode, scanCode, modifiers);
      }

      this.onConfirm();
      return true;
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      super.method_25394(context, mouseX, mouseY, delta);
      int centerX = this.field_22789 / 2;
      int centerY = this.field_22790 / 2;
      int panelLeft = centerX - 125;
      int panelTop = centerY - 75;
      context.method_25294(panelLeft, panelTop, panelLeft + 250, panelTop + 150, -872415232);
      context.method_49601(panelLeft, panelTop, 250, 150, -11184811);
      context.method_27535(this.field_22793, Text.method_43470("设置你的代号"), panelLeft + 125 - this.field_22793.method_1727("设置你的代号") / 2, panelTop + 15, 16766720);
      context.method_27535(
         this.field_22793, Text.method_43470("代号将显示在信息界面上"), panelLeft + 125 - this.field_22793.method_1727("代号将显示在信息界面上") / 2, panelTop + 32, 16777215
      );
      if (this.codenameField != null) {
         this.codenameField.method_25394(context, mouseX, mouseY, delta);
      }
   }

   public boolean method_25421() {
      return false;
   }
}
