package com.xie.smfs.client.screen;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ClientModConfig;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.manager.MainGhostManager;
import com.xie.smfs.network.packets.skills.c2s.GhostDomainLevelC2SPacket;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class PersonalConfigScreen extends Screen {
   private final ClientModConfig config = ClientModConfig.getInstance();
   private final Screen parent;

   public PersonalConfigScreen(Screen parent) {
      super(Text.translatable("smfs.personal_config.title"));
      this.parent = parent;
   }

   protected void init() {
      super.init();
      final UUID uuid = this.client != null && this.client.player != null ? this.client.player.getUuid() : null;
      int leftX = this.width / 2 - 160;
      int rightX = this.width / 2 + 10;
      int startY = this.height / 2 - 40;
      int y = startY;
      this.addDrawableChild(
         CyclingButtonWidget.onOffBuilder(this.config.showDamageText(uuid))
            .build(leftX, y, 150, 20, Text.translatable("smfs.personal_config.show_damage_text"), (button, value) -> {
               if (uuid != null) {
                  this.config.setShowDamageText(uuid, value);
               }
            })
      );
      y += 25;
      this.addDrawableChild(
         CyclingButtonWidget.onOffBuilder(this.config.showDamageTakenText(uuid))
            .build(leftX, y, 150, 20, Text.translatable("smfs.personal_config.show_damage_taken_text"), (button, value) -> {
               if (uuid != null) {
                  this.config.setShowDamageTakenText(uuid, value);
               }
            })
      );
      y += 25;
      this.addDrawableChild(
         new SliderWidget(
            leftX,
            y,
            150,
            20,
            Text.translatable("smfs.personal_config.screen_shake", new Object[]{String.format("%.0f", this.config.getScreenShakeIntensity(uuid) * 100.0F)}),
            this.config.getScreenShakeIntensity(uuid)
         ) {
            protected void updateMessage() {
               this.setMessage(Text.translatable("smfs.personal_config.screen_shake", new Object[]{String.format("%.0f", this.value * 100.0)}));
            }

            protected void applyValue() {
               if (uuid != null) {
                  PersonalConfigScreen.this.config.setScreenShakeIntensity(uuid, (float)this.value);
               }
            }
         }
      );
      y += 25;
      this.addDrawableChild(
         CyclingButtonWidget.builder(value -> Text.translatable("smfs.personal_config.music_box_mode." + value))
            .values(new String[]{"default", "custom"})
            .initially(this.config.getMusicBoxMode(uuid))
            .build(leftX, y, 150, 20, Text.translatable("smfs.personal_config.music_box_mode"), (button, value) -> {
               if (uuid != null) {
                  this.config.setMusicBoxMode(uuid, value);
               }
            })
      );
      y += 25;
      y = startY;
      this.addDrawableChild(
         CyclingButtonWidget.onOffBuilder(this.config.isCustomGhostDomainLevelEnabled(uuid))
            .build(rightX, y, 150, 20, Text.translatable("smfs.personal_config.custom_ghost_domain_level"), (button, value) -> {
               if (uuid != null) {
                  this.config.setCustomGhostDomainLevelEnabled(uuid, value);
               }
            })
      );
      y += 25;
      int currentRange = this.config.getGhostDomainRange(uuid);
      int maxRadius = this.getMaxDomainRadius();
      final int sliderRange = maxRadius - 5;
      this.addDrawableChild(
         new SliderWidget(
            rightX,
            y,
            150,
            20,
            Text.translatable("smfs.personal_config.ghost_domain_range", new Object[]{currentRange <= 0 ? "默认" : String.valueOf(currentRange)}),
            currentRange <= 0 ? 0.0 : (currentRange - 5.0) / sliderRange
         ) {
            protected void updateMessage() {
               int val = (int)(this.value * sliderRange + 5.0);
               this.setMessage(Text.translatable("smfs.personal_config.ghost_domain_range", new Object[]{this.value <= 0.0 ? "默认" : String.valueOf(val)}));
            }

            protected void applyValue() {
               if (uuid != null) {
                  int range;
                  if (this.value <= 0.0) {
                     range = -1;
                  } else {
                     range = (int)(this.value * sliderRange + 5.0);
                  }

                  PersonalConfigScreen.this.config.setGhostDomainRange(uuid, range);
                  PacketByteBuf buf = PacketByteBufs.create();
                  buf.writeInt(range);
                  ClientPlayNetworking.send(GhostDomainLevelC2SPacket.RANGE_SYNC_ID, buf);
               }
            }
         }
      );
      y += 25;
      int currentFogRange = this.config.getGhostDomainFogRange(uuid);
      this.addDrawableChild(
         new SliderWidget(
            rightX,
            y,
            150,
            20,
            Text.translatable("smfs.personal_config.ghost_domain_fog_range", new Object[]{currentFogRange <= 0 ? "默认" : String.valueOf(currentFogRange)}),
            currentFogRange <= 0 ? 0.0 : (currentFogRange - 5.0) / 123.0
         ) {
            protected void updateMessage() {
               int val = (int)(this.value * 123.0 + 5.0);
               this.setMessage(Text.translatable("smfs.personal_config.ghost_domain_fog_range", new Object[]{this.value <= 0.0 ? "默认" : String.valueOf(val)}));
            }

            protected void applyValue() {
               if (uuid != null) {
                  int range = this.value <= 0.0 ? -1 : (int)(this.value * 123.0 + 5.0);
                  PersonalConfigScreen.this.config.setGhostDomainFogRange(uuid, range);
               }
            }
         }
      );
      y += 25;
      this.addDrawableChild(
         CyclingButtonWidget.onOffBuilder(this.config.isDamageWhitelistEnabled(uuid))
            .build(rightX, y, 150, 20, Text.translatable("smfs.personal_config.damage_whitelist"), (button, value) -> {
               if (uuid != null) {
                  this.config.setDamageWhitelistEnabled(uuid, value);
               }
            })
      );
      y += 25;
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("smfs.personal_config.damage_whitelist_manage"), button -> {
         if (this.client != null) {
            this.client.setScreen(new DamageWhitelistScreen(this));
         }
      }).dimensions(rightX, y, 150, 20).build());
      this.addDrawableChild(
         ButtonWidget.builder(Text.translatable("smfs.config.save"), button -> this.saveAndClose())
            .dimensions(this.width / 2 - 154, this.height - 28, 100, 20)
            .build()
      );
      this.addDrawableChild(
         ButtonWidget.builder(Text.translatable("smfs.config.reset"), button -> this.resetToDefaults())
            .dimensions(this.width / 2 - 50, this.height - 28, 100, 20)
            .build()
      );
      this.addDrawableChild(
         ButtonWidget.builder(Text.translatable("smfs.config.cancel"), button -> this.close())
            .dimensions(this.width / 2 + 54, this.height - 28, 100, 20)
            .build()
      );
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 16777215);
      super.render(context, mouseX, mouseY, delta);
   }

   private void resetToDefaults() {
      UUID uuid = this.client != null && this.client.player != null ? this.client.player.getUuid() : null;
      if (uuid != null) {
         this.config.setShowDamageText(uuid, true);
         this.config.setShowDamageTakenText(uuid, true);
         this.config.setScreenShakeIntensity(uuid, 1.0F);
         this.config.setCustomGhostDomainLevelEnabled(uuid, false);
         this.config.setGhostDomainRange(uuid, -1);
         this.config.setGhostDomainFogRange(uuid, -1);
         this.config.setDamageWhitelistEnabled(uuid, false);
         this.config.getDamageWhitelist(uuid).clear();
         this.config.save();
         this.clearAndInit();
      }
   }

   private void saveAndClose() {
      this.config.save();
      this.close();
   }

   private int getMaxDomainRadius() {
      if (this.client != null && this.client.player != null) {
         try {
            int mainSlot = MainGhostManager.getMainGhostSlot(this.client.player);
            int level = PlayerEvents.getGhostSlotLevel(this.client.player, mainSlot);
            if (level <= 0) {
               level = 1;
            }

            ModConfig config = ModConfig.getInstance();
            return (int)(config.ghostDomainBaseSize + (level - 1) * config.ghostDomainSizePerLevel);
         } catch (Exception e) {
            return 256;
         }
      } else {
         return 256;
      }
   }

   public void close() {
      if (this.client != null) {
         this.client.setScreen(this.parent);
      }
   }
}
