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
      super(Text.method_43471("smfs.whitelist.title"));
      this.parent = parent;
   }

   protected void method_25426() {
      super.method_25426();
      UUID uuid = this.field_22787 != null && this.field_22787.field_1724 != null ? this.field_22787.field_1724.method_5667() : null;
      int listTop = 40;
      int listBottom = this.field_22790 - 60;
      this.listWidget = new DamageWhitelistScreen.WhitelistListWidget(this.field_22787, this.field_22789, this.field_22790, listTop, listBottom, 25);
      this.method_25429(this.listWidget);
      this.nameField = new TextFieldWidget(
         this.field_22793, this.field_22789 / 2 - 100, this.field_22790 - 46, 130, 20, Text.method_43471("smfs.whitelist.input")
      );
      this.nameField.method_1880(16);
      this.method_25429(this.nameField);
      this.method_37063(ButtonWidget.method_46430(Text.method_43471("smfs.whitelist.add"), button -> {
         if (uuid != null) {
            String name = this.nameField.method_1882().trim();
            if (!name.isEmpty()) {
               this.config.addDamageWhitelist(uuid, name);
               this.nameField.method_1852("");
               this.refreshList();
            }
         }
      }).method_46434(this.field_22789 / 2 + 34, this.field_22790 - 46, 80, 20).method_46431());
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43471("smfs.config.cancel"), button -> this.method_25419())
            .method_46434(this.field_22789 / 2 - 100, this.field_22790 - 24, 200, 20)
            .method_46431()
      );
   }

   private void refreshList() {
      if (this.listWidget != null) {
         this.listWidget.refresh();
      }
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      this.listWidget.method_25394(context, mouseX, mouseY, delta);
      context.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 15, 16777215);
      this.nameField.method_25394(context, mouseX, mouseY, delta);
      super.method_25394(context, mouseX, mouseY, delta);
   }

   public void method_25419() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }

   private class WhitelistListWidget extends EntryListWidget<DamageWhitelistScreen.WhitelistListWidget.Entry> {
      public WhitelistListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
         super(client, width, height, top, bottom, itemHeight);
         this.refresh();
      }

      public void refresh() {
         this.method_25339();
         UUID uuid = this.field_22740 != null && this.field_22740.field_1724 != null ? this.field_22740.field_1724.method_5667() : null;
         if (uuid != null) {
            Set<String> whitelist = DamageWhitelistScreen.this.config.getDamageWhitelist(uuid);
            List<String> sortedList = new ArrayList<>(whitelist);
            sortedList.sort(String::compareToIgnoreCase);

            for (String name : sortedList) {
               this.method_25321(new DamageWhitelistScreen.WhitelistListWidget.Entry(name));
            }
         }
      }

      public int method_25322() {
         return 300;
      }

      protected int method_25329() {
         return this.field_22742 / 2 + 150;
      }

      public void method_37020(NarrationMessageBuilder builder) {
         builder.method_37033(NarrationPart.field_33788, "技能伤害白名单");
      }

      private class Entry extends net.minecraft.client.gui.widget.EntryListWidget.Entry<DamageWhitelistScreen.WhitelistListWidget.Entry> {
         private final String playerName;
         private final ButtonWidget removeButton;

         public Entry(String playerName) {
            this.playerName = playerName;
            this.removeButton = ButtonWidget.method_46430(
                  Text.method_43470("§c✕ " + playerName),
                  button -> {
                     UUID uuid = WhitelistListWidget.this.field_22740 != null && WhitelistListWidget.this.field_22740.field_1724 != null
                        ? WhitelistListWidget.this.field_22740.field_1724.method_5667()
                        : null;
                     if (uuid != null) {
                        DamageWhitelistScreen.this.config.removeDamageWhitelist(uuid, playerName);
                        WhitelistListWidget.this.refresh();
                     }
                  }
               )
               .method_46434(0, 0, 200, 20)
               .method_46431();
         }

         public void method_25343(
            DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta
         ) {
            this.removeButton.method_46421(x + (entryWidth - 200) / 2);
            this.removeButton.method_46419(y);
            this.removeButton.method_25394(context, mouseX, mouseY, tickDelta);
         }

         public boolean method_25402(double mouseX, double mouseY, int button) {
            return this.removeButton.method_25402(mouseX, mouseY, button);
         }

         public boolean method_25406(double mouseX, double mouseY, int button) {
            return this.removeButton.method_25406(mouseX, mouseY, button);
         }
      }
   }
}
