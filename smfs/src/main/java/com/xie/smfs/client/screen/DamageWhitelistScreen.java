package com.xie.smfs.client.screen;

import com.xie.smfs.config.ClientModConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class DamageWhitelistScreen extends Screen {
   private final ClientModConfig config = ClientModConfig.getInstance();
   private final Screen parent;
   private DamageWhitelistScreen.WhitelistListWidget listWidget;
   private TextFieldWidget nameField;

   public DamageWhitelistScreen(Screen parent) {
      super(Text.translatable("smfs.whitelist.title"));
      this.parent = parent;
   }

   protected void init() {
      super.init();
      UUID uuid = this.client != null && this.client.player != null ? this.client.player.getUuid() : null;
      int listTop = 40;
      int listBottom = this.height - 60;
      this.listWidget = new DamageWhitelistScreen.WhitelistListWidget(this.client, this.width, this.height, listTop, listBottom, 25);
      this.addSelectableChild(this.listWidget);
      this.nameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height - 46, 130, 20, Text.translatable("smfs.whitelist.input"));
      this.nameField.setMaxLength(16);
      this.addSelectableChild(this.nameField);
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("smfs.whitelist.add"), button -> {
         if (uuid != null) {
            String name = this.nameField.getText().trim();
            if (!name.isEmpty()) {
               this.config.addDamageWhitelist(uuid, name);
               this.nameField.setText("");
               this.refreshList();
            }
         }
      }).dimensions(this.width / 2 + 34, this.height - 46, 80, 20).build());
      this.addDrawableChild(
         ButtonWidget.builder(Text.translatable("smfs.config.cancel"), button -> this.close())
            .dimensions(this.width / 2 - 100, this.height - 24, 200, 20)
            .build()
      );
   }

   private void refreshList() {
      if (this.listWidget != null) {
         this.listWidget.refresh();
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      this.listWidget.render(context, mouseX, mouseY, delta);
      context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 16777215);
      this.nameField.render(context, mouseX, mouseY, delta);
      super.render(context, mouseX, mouseY, delta);
   }

   public void close() {
      if (this.client != null) {
         this.client.setScreen(this.parent);
      }
   }

   private class WhitelistListWidget extends EntryListWidget<DamageWhitelistScreen.WhitelistListWidget.Entry> {
      public WhitelistListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
         super(client, width, height, top, bottom, itemHeight);
         this.refresh();
      }

      public void refresh() {
         this.clearEntries();
         UUID uuid = this.client != null && this.client.player != null ? this.client.player.getUuid() : null;
         if (uuid != null) {
            Set<String> whitelist = DamageWhitelistScreen.this.config.getDamageWhitelist(uuid);
            List<String> sortedList = new ArrayList<>(whitelist);
            sortedList.sort(String::compareToIgnoreCase);

            for (String name : sortedList) {
               this.addEntry(new DamageWhitelistScreen.WhitelistListWidget.Entry(name));
            }
         }
      }

      public int getRowWidth() {
         return 300;
      }

      protected int getScrollbarPositionX() {
         return this.width / 2 + 150;
      }

      public void appendNarrations(NarrationMessageBuilder builder) {
         builder.put(NarrationPart.TITLE, "技能伤害白名单");
      }

      private class Entry extends net.minecraft.client.gui.widget.EntryListWidget.Entry<DamageWhitelistScreen.WhitelistListWidget.Entry> {
         private final String playerName;
         private final ButtonWidget removeButton;

         public Entry(String playerName) {
            this.playerName = playerName;
            this.removeButton = ButtonWidget.builder(
                  Text.literal("§c✕ " + playerName),
                  button -> {
                     UUID uuid = WhitelistListWidget.this.client != null && WhitelistListWidget.this.client.player != null
                        ? WhitelistListWidget.this.client.player.getUuid()
                        : null;
                     if (uuid != null) {
                        DamageWhitelistScreen.this.config.removeDamageWhitelist(uuid, playerName);
                        WhitelistListWidget.this.refresh();
                     }
                  }
               )
               .dimensions(0, 0, 200, 20)
               .build();
         }

         public void render(
            DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta
         ) {
            this.removeButton.setX(x + (entryWidth - 200) / 2);
            this.removeButton.setY(y);
            this.removeButton.render(context, mouseX, mouseY, tickDelta);
         }

         public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.removeButton.mouseClicked(mouseX, mouseY, button);
         }

         public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.removeButton.mouseReleased(mouseX, mouseY, button);
         }
      }
   }
}
