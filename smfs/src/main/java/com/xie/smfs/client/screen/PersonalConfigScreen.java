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
      super(Text.method_43471("smfs.personal_config.title"));
      this.parent = parent;
   }

   protected void method_25426() {
      super.method_25426();
      final UUID uuid = this.field_22787 != null && this.field_22787.field_1724 != null ? this.field_22787.field_1724.method_5667() : null;
      int leftX = this.field_22789 / 2 - 160;
      int rightX = this.field_22789 / 2 + 10;
      int startY = this.field_22790 / 2 - 40;
      int y = startY;
      this.method_37063(
         CyclingButtonWidget.method_32613(this.config.showDamageText(uuid))
            .method_32617(leftX, y, 150, 20, Text.method_43471("smfs.personal_config.show_damage_text"), (button, value) -> {
               if (uuid != null) {
                  this.config.setShowDamageText(uuid, value);
               }
            })
      );
      y += 25;
      this.method_37063(
         CyclingButtonWidget.method_32613(this.config.showDamageTakenText(uuid))
            .method_32617(leftX, y, 150, 20, Text.method_43471("smfs.personal_config.show_damage_taken_text"), (button, value) -> {
               if (uuid != null) {
                  this.config.setShowDamageTakenText(uuid, value);
               }
            })
      );
      y += 25;
      this.method_37063(
         new SliderWidget(
            leftX,
            y,
            150,
            20,
            Text.method_43469("smfs.personal_config.screen_shake", new Object[]{String.format("%.0f", this.config.getScreenShakeIntensity(uuid) * 100.0F)}),
            this.config.getScreenShakeIntensity(uuid)
         ) {
            protected void method_25346() {
               this.method_25355(Text.method_43469("smfs.personal_config.screen_shake", new Object[]{String.format("%.0f", this.field_22753 * 100.0)}));
            }

            protected void method_25344() {
               if (uuid != null) {
                  PersonalConfigScreen.this.config.setScreenShakeIntensity(uuid, (float)this.field_22753);
               }
            }
         }
      );
      y += 25;
      this.method_37063(
         CyclingButtonWidget.method_32606(value -> Text.method_43471("smfs.personal_config.music_box_mode." + value))
            .method_32624(new String[]{"default", "custom"})
            .method_32619(this.config.getMusicBoxMode(uuid))
            .method_32617(leftX, y, 150, 20, Text.method_43471("smfs.personal_config.music_box_mode"), (button, value) -> {
               if (uuid != null) {
                  this.config.setMusicBoxMode(uuid, value);
               }
            })
      );
      y += 25;
      y = startY;
      this.method_37063(
         CyclingButtonWidget.method_32613(this.config.isCustomGhostDomainLevelEnabled(uuid))
            .method_32617(rightX, y, 150, 20, Text.method_43471("smfs.personal_config.custom_ghost_domain_level"), (button, value) -> {
               if (uuid != null) {
                  this.config.setCustomGhostDomainLevelEnabled(uuid, value);
               }
            })
      );
      y += 25;
      int currentRange = this.config.getGhostDomainRange(uuid);
      int maxRadius = this.getMaxDomainRadius();
      final int sliderRange = maxRadius - 5;
      this.method_37063(
         new SliderWidget(
            rightX,
            y,
            150,
            20,
            Text.method_43469("smfs.personal_config.ghost_domain_range", new Object[]{currentRange <= 0 ? "默认" : String.valueOf(currentRange)}),
            currentRange <= 0 ? 0.0 : (currentRange - 5.0) / sliderRange
         ) {
            protected void method_25346() {
               int val = (int)(this.field_22753 * sliderRange + 5.0);
               this.method_25355(
                  Text.method_43469("smfs.personal_config.ghost_domain_range", new Object[]{this.field_22753 <= 0.0 ? "默认" : String.valueOf(val)})
               );
            }

            protected void method_25344() {
               if (uuid != null) {
                  int range;
                  if (this.field_22753 <= 0.0) {
                     range = -1;
                  } else {
                     range = (int)(this.field_22753 * sliderRange + 5.0);
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
      this.method_37063(
         new SliderWidget(
            rightX,
            y,
            150,
            20,
            Text.method_43469("smfs.personal_config.ghost_domain_fog_range", new Object[]{currentFogRange <= 0 ? "默认" : String.valueOf(currentFogRange)}),
            currentFogRange <= 0 ? 0.0 : (currentFogRange - 5.0) / 123.0
         ) {
            protected void method_25346() {
               int val = (int)(this.field_22753 * 123.0 + 5.0);
               this.method_25355(
                  Text.method_43469("smfs.personal_config.ghost_domain_fog_range", new Object[]{this.field_22753 <= 0.0 ? "默认" : String.valueOf(val)})
               );
            }

            protected void method_25344() {
               if (uuid != null) {
                  int range = this.field_22753 <= 0.0 ? -1 : (int)(this.field_22753 * 123.0 + 5.0);
                  PersonalConfigScreen.this.config.setGhostDomainFogRange(uuid, range);
               }
            }
         }
      );
      y += 25;
      this.method_37063(
         CyclingButtonWidget.method_32613(this.config.isDamageWhitelistEnabled(uuid))
            .method_32617(rightX, y, 150, 20, Text.method_43471("smfs.personal_config.damage_whitelist"), (button, value) -> {
               if (uuid != null) {
                  this.config.setDamageWhitelistEnabled(uuid, value);
               }
            })
      );
      y += 25;
      this.method_37063(ButtonWidget.method_46430(Text.method_43471("smfs.personal_config.damage_whitelist_manage"), button -> {
         if (this.field_22787 != null) {
            this.field_22787.method_1507(new DamageWhitelistScreen(this));
         }
      }).method_46434(rightX, y, 150, 20).method_46431());
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43471("smfs.config.save"), button -> this.saveAndClose())
            .method_46434(this.field_22789 / 2 - 154, this.field_22790 - 28, 100, 20)
            .method_46431()
      );
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43471("smfs.config.reset"), button -> this.resetToDefaults())
            .method_46434(this.field_22789 / 2 - 50, this.field_22790 - 28, 100, 20)
            .method_46431()
      );
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43471("smfs.config.cancel"), button -> this.method_25419())
            .method_46434(this.field_22789 / 2 + 54, this.field_22790 - 28, 100, 20)
            .method_46431()
      );
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      context.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 15, 16777215);
      super.method_25394(context, mouseX, mouseY, delta);
   }

   private void resetToDefaults() {
      UUID uuid = this.field_22787 != null && this.field_22787.field_1724 != null ? this.field_22787.field_1724.method_5667() : null;
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
         this.method_41843();
      }
   }

   private void saveAndClose() {
      this.config.save();
      this.method_25419();
   }

   private int getMaxDomainRadius() {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         try {
            int mainSlot = MainGhostManager.getMainGhostSlot(this.field_22787.field_1724);
            int level = PlayerEvents.getGhostSlotLevel(this.field_22787.field_1724, mainSlot);
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

   public void method_25419() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }
}
