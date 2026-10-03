package com.xie.smfs.client.screen;

import com.xie.smfs.config.GhostRespawnConfig;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.network.packets.config.c2s.ConfigSyncC2SPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
   private final Screen parent;
   private final ModConfig config;
   private final List<ConfigScreen.ConfigEntry> configEntries = new ArrayList<>();
   private final List<ConfigScreen.ConfigEntry> allConfigEntries = new ArrayList<>();
   private ConfigScreen.ConfigListWidget configList;
   private TextFieldWidget focusedTextField;
   private TextFieldWidget searchField;
   private String searchFilter = "";

   public ConfigScreen(Screen parent) {
      super(Text.method_43471("smfs.config.title"));
      this.parent = parent;
      this.config = ModConfig.getInstance();
      this.initializeConfigEntries();
   }

   protected void method_25426() {
      super.method_25426();
      this.searchField = new TextFieldWidget(this.field_22793, this.field_22789 - 145, 10, 130, 16, Text.method_43470(""));
      this.searchField.method_1880(50);
      this.searchField.method_1887(Text.method_43471("smfs.config.search").getString());
      this.searchField.method_1863(text -> {
         this.searchFilter = text.toLowerCase().trim();
         this.rebuildConfigList();
      });
      this.method_37063(this.searchField);
      this.configList = new ConfigScreen.ConfigListWidget(this.field_22787, this.field_22789, this.field_22790, 32, this.field_22790 - 64, 25);
      this.method_25429(this.configList);
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43471("smfs.config.save"), button -> this.saveAndClose())
            .method_46434(this.field_22789 / 2 - 206, this.field_22790 - 28, 100, 20)
            .method_46431()
      );
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43471("smfs.config.reset"), button -> this.resetToDefaults())
            .method_46434(this.field_22789 / 2 - 102, this.field_22790 - 28, 100, 20)
            .method_46431()
      );
      this.method_37063(ButtonWidget.method_46430(Text.method_43471("smfs.personal_config.title"), button -> {
         if (this.field_22787 != null) {
            this.field_22787.method_1507(new PersonalConfigScreen(this));
         }
      }).method_46434(this.field_22789 / 2 + 2, this.field_22790 - 28, 100, 20).method_46431());
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43471("smfs.config.cancel"), button -> this.cancelAndClose())
            .method_46434(this.field_22789 / 2 + 106, this.field_22790 - 28, 100, 20)
            .method_46431()
      );
   }

   public boolean method_25404(int keyCode, int scanCode, int modifiers) {
      return this.focusedTextField != null && this.focusedTextField.method_25404(keyCode, scanCode, modifiers)
         ? true
         : super.method_25404(keyCode, scanCode, modifiers);
   }

   public boolean method_25400(char chr, int modifiers) {
      return this.focusedTextField != null && this.focusedTextField.method_25400(chr, modifiers) ? true : super.method_25400(chr, modifiers);
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      boolean handled = super.method_25402(mouseX, mouseY, button);
      if (this.searchField != null && this.searchField.method_25370() && this.focusedTextField != null) {
         this.focusedTextField.method_25365(false);
         this.focusedTextField = null;
      }

      if (!handled) {
         this.focusedTextField = null;
      }

      return handled;
   }

   public void method_25393() {
      if (this.searchField != null) {
         this.searchField.method_1865();
      }

      if (this.focusedTextField != null) {
         this.focusedTextField.method_1865();
      }
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      this.configList.method_25394(context, mouseX, mouseY, delta);
      context.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 13, 16777215);
      if (this.searchField != null) {
         this.searchField.method_25394(context, mouseX, mouseY, delta);
      }

      super.method_25394(context, mouseX, mouseY, delta);
   }

   private void addGhostRespawnConfig(ModConfig config, String configKey, String ghostName) {
      GhostRespawnConfig ghostConfig = config.getGhostRespawnConfig(ghostName);
      this.configEntries.add(new ConfigScreen.ConfigEntry(configKey + ".name", ConfigScreen.ConfigEntry.Type.CATEGORY));
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.ghost_respawn.enabled", ConfigScreen.ConfigEntry.Type.TOGGLE, ghostConfig::isEnabled, ghostConfig::setEnabled
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.ghost_respawn.days",
               ConfigScreen.ConfigEntry.Type.SLIDER,
               () -> (double)ghostConfig.getRespawnDays(),
               value -> ghostConfig.setRespawnDays(value.intValue()),
               0.0,
               100.0,
               1.0
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.ghost_respawn.chance",
               ConfigScreen.ConfigEntry.Type.SLIDER,
               ghostConfig::getSpawnChance,
               ghostConfig::setSpawnChance,
               0.0,
               1.0,
               0.05
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.ghost_respawn.cooldown",
               ConfigScreen.ConfigEntry.Type.SLIDER,
               () -> (double)ghostConfig.getSpawnCooldown(),
               value -> ghostConfig.setSpawnCooldown(value.intValue()),
               600.0,
               72000.0,
               100.0
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.ghost_respawn.night_only", ConfigScreen.ConfigEntry.Type.TOGGLE, ghostConfig::isNightOnly, ghostConfig::setNightOnly
            )
         );
      if (this.isGhostMaster(ghostName)) {
         this.configEntries
            .add(
               new ConfigScreen.ConfigEntry(
                  "smfs.config.ghost_respawn.drop_chance",
                  ConfigScreen.ConfigEntry.Type.SLIDER,
                  () -> config.getGhostMasterDropChance(ghostName),
                  value -> config.setGhostMasterDropChance(ghostName, value),
                  0.0,
                  1.0,
                  0.05
               )
            );
      }
   }

   private boolean isGhostMaster(String ghostName) {
      return ghostName.equals("li_jun")
         || ghostName.equals("ye_zhen")
         || ghostName.equals("feng_quan")
         || ghostName.equals("cao_yang")
         || ghostName.equals("fang_shi_min")
         || ghostName.equals("yan_li")
         || ghostName.equals("li_le_ping")
         || ghostName.equals("wang_xiao_ming")
         || ghostName.equals("chen_doctor")
         || ghostName.equals("zhao_kai_ming")
         || ghostName.equals("npc1")
         || ghostName.equals("npc2")
         || ghostName.equals("npc3")
         || ghostName.equals("npc4")
         || ghostName.equals("npc5")
         || ghostName.equals("npc6");
   }

   private void initializeConfigEntries() {
      this.configEntries.clear();
      this.allConfigEntries.clear();
      this.configEntries.add(new ConfigScreen.ConfigEntry("smfs.config.category.ghost_domain", ConfigScreen.ConfigEntry.Type.CATEGORY));
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.ghost_domain_base_size",
               ConfigScreen.ConfigEntry.Type.SLIDER,
               () -> this.config.ghostDomainBaseSize,
               value -> this.config.ghostDomainBaseSize = value,
               1.0,
               100.0,
               1.0
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.ghost_domain_size_per_level",
               ConfigScreen.ConfigEntry.Type.SLIDER,
               () -> this.config.ghostDomainSizePerLevel,
               value -> this.config.ghostDomainSizePerLevel = value,
               0.5,
               50.0,
               0.5
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.enable_ghost_domain_effects_on_mobs",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.enableGhostDomainEffectsOnMobs,
               value -> this.config.enableGhostDomainEffectsOnMobs = value
            )
         );
      this.configEntries.add(new ConfigScreen.ConfigEntry("smfs.config.category.resurrection", ConfigScreen.ConfigEntry.Type.CATEGORY));
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.instant_reincarnation",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.instantReincarnation,
               value -> this.config.instantReincarnation = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.keep_ghosts_after_mirror_resurrection",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.keepGhostsAfterMirrorResurrection,
               value -> this.config.keepGhostsAfterMirrorResurrection = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.lose_ghosts_on_death",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.loseGhostsOnDeath,
               value -> this.config.loseGhostsOnDeath = value
            )
         );
      this.configEntries.add(new ConfigScreen.ConfigEntry("smfs.config.category.ghost_dream", ConfigScreen.ConfigEntry.Type.CATEGORY));
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.ghost_dream_chance",
               ConfigScreen.ConfigEntry.Type.SLIDER,
               () -> this.config.ghostDreamChance * 100.0,
               value -> this.config.ghostDreamChance = value / 100.0,
               0.0,
               100.0,
               1.0
            )
         );
      this.configEntries.add(new ConfigScreen.ConfigEntry("smfs.config.category.damage", ConfigScreen.ConfigEntry.Type.CATEGORY));
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.spirit_damage_penetration_threshold",
               ConfigScreen.ConfigEntry.Type.SLIDER,
               () -> this.config.spiritDamagePenetrationThreshold,
               value -> this.config.spiritDamagePenetrationThreshold = value,
               0.0,
               1.0,
               0.05
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.penetrate_creative_mode",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.penetrateCreativeMode,
               value -> this.config.penetrateCreativeMode = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.penetrate_spectator_mode",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.penetrateSpectatorMode,
               value -> this.config.penetrateSpectatorMode = value
            )
         );
      this.configEntries.add(new ConfigScreen.ConfigEntry("smfs.config.category.system", ConfigScreen.ConfigEntry.Type.CATEGORY));
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.enable_quest_system",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.enableQuestSystem,
               value -> this.config.enableQuestSystem = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.enable_player_ghost_real_skin",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.enablePlayerGhostRealSkin,
               value -> this.config.enablePlayerGhostRealSkin = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.iris_compatibility_mode",
               ConfigScreen.ConfigEntry.Type.SELECTOR,
               () -> this.config.irisCompatibilityMode,
               value -> this.config.irisCompatibilityMode = value,
               Arrays.asList(
                  "smfs.config.iris_compatibility_mode.auto_disable",
                  "smfs.config.iris_compatibility_mode.incompatible",
                  "smfs.config.iris_compatibility_mode.compatible"
               )
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.enable_evil_ghost_revival",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.enableEvilGhostRevival,
               value -> this.config.enableEvilGhostRevival = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.ghost_master_revival",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.ghostMasterRevival,
               value -> this.config.ghostMasterRevival = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.show_action_bar_info",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.showActionBarInfo,
               value -> this.config.showActionBarInfo = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.hardcore_deadlock_mode",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.hardcoreDeadlockMode,
               value -> this.config.hardcoreDeadlockMode = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.enable_resentment_system",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.enableResentmentSystem,
               value -> this.config.enableResentmentSystem = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.enable_sanity_system",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.enableSanitySystem,
               value -> this.config.enableSanitySystem = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.enable_player_spirit_damage",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.enablePlayerSpiritDamage,
               value -> this.config.enablePlayerSpiritDamage = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.lock_after_taming",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.lockAfterTaming,
               value -> this.config.lockAfterTaming = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.direct_tame_ghost",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.directTameGhost,
               value -> this.config.directTameGhost = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.unlock_save_lock",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.unlockSaveLock,
               value -> this.config.unlockSaveLock = value
            )
         );
      this.configEntries.add(new ConfigScreen.ConfigEntry("smfs.config.category.body_enhancement", ConfigScreen.ConfigEntry.Type.CATEGORY));
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.body_enhancement_mode",
               ConfigScreen.ConfigEntry.Type.SELECTOR,
               () -> this.config.bodyEnhancementMode,
               value -> this.config.bodyEnhancementMode = value,
               Arrays.asList(
                  "smfs.config.body_enhancement_mode.none", "smfs.config.body_enhancement_mode.stepped", "smfs.config.body_enhancement_mode.absolute"
               )
            )
         );
      this.configEntries.add(new ConfigScreen.ConfigEntry("smfs.config.category.evil_ghosts", ConfigScreen.ConfigEntry.Type.CATEGORY));
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.enable_evil_ghosts",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.enableEvilGhosts,
               value -> this.config.enableEvilGhosts = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.evil_ghost_max_spawn_range",
               ConfigScreen.ConfigEntry.Type.SLIDER,
               () -> (double)this.config.evilGhostMaxSpawnRange,
               value -> this.config.evilGhostMaxSpawnRange = value.intValue(),
               16.0,
               512.0,
               16.0
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.evil_ghost_min_spawn_range",
               ConfigScreen.ConfigEntry.Type.SLIDER,
               () -> (double)this.config.evilGhostMinSpawnRange,
               value -> this.config.evilGhostMinSpawnRange = value.intValue(),
               8.0,
               256.0,
               8.0
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.allow_evil_ghost_natural_despawn",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.allowEvilGhostNaturalDespawn,
               value -> this.config.allowEvilGhostNaturalDespawn = value
            )
         );
      this.configEntries.add(new ConfigScreen.ConfigEntry("smfs.config.category.ai_system", ConfigScreen.ConfigEntry.Type.CATEGORY));
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.ai_human_skin_paper_enabled",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.aiHumanSkinPaperEnabled,
               value -> this.config.aiHumanSkinPaperEnabled = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.ai_human_skin_paper_api_key",
               ConfigScreen.ConfigEntry.Type.TEXT_INPUT,
               null,
               null,
               null,
               null,
               0.0,
               0.0,
               0.0,
               null,
               null,
               null,
               null,
               () -> this.config.aiHumanSkinPaperApiKey,
               value -> this.config.aiHumanSkinPaperApiKey = value
            )
         );
      List<java.util.Map.Entry<String, String>> ghostConfigs = new ArrayList<>();
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.ghost_merchant", "ghost_merchant"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.taitou_ghost", "taitou_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.box_ghost", "box_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.villager_ghost", "villager_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.untouchable_ghost", "untouchable_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.fog_ghost", "fog_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.ditou_ghost", "ditou_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.jump_ghost", "jump_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.giant_shadow_ghost", "giant_shadow_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.ganshi_bride_ghost", "ganshi_bride_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.food_ghost", "food_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.crop_ghost", "crop_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.step_ghost", "step_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.grave_earth_ghost", "grave_earth_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.trash_ghost", "trash_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.water_ghost", "water_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.block_ghost", "block_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.suona_ghost", "suona_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.burn_ghost", "burn_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.death_sight_ghost", "death_sight_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.crying_ghost", "crying_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.gong_ghost", "gong_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.lost_ghost", "lost_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.ghost_wind", "ghost_wind"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.ghost_pressure", "ghost_pressure"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.goods_seller_ghost", "goods_seller_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.ye_zhen", "ye_zhen"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.feng_quan", "feng_quan"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.cao_yang", "cao_yang"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.fang_shi_min", "fang_shi_min"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.yan_li", "yan_li"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.li_le_ping", "li_le_ping"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.wang_xiao_ming", "wang_xiao_ming"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.chen_doctor", "chen_doctor"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.li_jun", "li_jun"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.zhao_kai_ming", "zhao_kai_ming"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.npc1", "npc1"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.npc2", "npc2"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.npc3", "npc3"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.npc4", "npc4"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.npc5", "npc5"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.npc6", "npc6"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.silent_ghost", "silent_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.plague_ghost", "plague_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.mineral_ghost", "mineral_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.shadow_ghost", "shadow_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.door_ghost", "door_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.ghost_smoke", "ghost_smoke"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.ghost_officer", "ghost_officer"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.sneak_ghost", "sneak_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.clothes_ghost", "clothes_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.puppet_ghost", "puppet_ghost"));
      ghostConfigs.add(new SimpleEntry<>("smfs.config.ghost_respawn.ghost_shadow_head", "ghost_shadow_head"));
      ghostConfigs.sort((entry1, entry2) -> {
         GhostRespawnConfig config1 = this.config.getGhostRespawnConfig(entry1.getValue());
         GhostRespawnConfig config2 = this.config.getGhostRespawnConfig(entry2.getValue());
         return Integer.compare(config1.getRespawnDays(), config2.getRespawnDays());
      });

      for (java.util.Map.Entry<String, String> entry : ghostConfigs) {
         this.addGhostRespawnConfig(this.config, entry.getKey(), entry.getValue());
      }

      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.enable_ghost_patrol",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.enableGhostPatrol,
               value -> this.config.enableGhostPatrol = value
            )
         );
      this.configEntries
         .add(
            new ConfigScreen.ConfigEntry(
               "smfs.config.enable_ghost_watchdog",
               ConfigScreen.ConfigEntry.Type.TOGGLE,
               () -> this.config.enableGhostWatchdog,
               value -> this.config.enableGhostWatchdog = value
            )
         );
      this.allConfigEntries.addAll(this.configEntries);
   }

   private void rebuildConfigList() {
      if (this.configList != null) {
         this.configList.rebuildWithFilter(this.searchFilter);
      }
   }

   private void saveAndClose() {
      this.config.validate();
      if (this.field_22787 != null && this.field_22787.method_1562() != null && !this.field_22787.method_1542()) {
         PacketByteBuf buf = PacketByteBufs.create();
         new ConfigSyncC2SPacket(this.config).write(buf);
         ClientPlayNetworking.send(ConfigSyncC2SPacket.ID, buf);
         this.config.save();
      } else {
         this.config.save();
      }

      ModConfig.reload();
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }

   private void resetToDefaults() {
      this.config.resetToDefaults();
      this.field_22787.method_1507(new ConfigScreen(this.parent));
   }

   private void cancelAndClose() {
      ModConfig.reload();
      this.field_22787.method_1507(this.parent);
   }

   public void method_25419() {
      this.field_22787.method_1507(this.parent);
   }

   private static class ConfigEntry {
      final String key;
      final ConfigScreen.ConfigEntry.Type type;
      final Supplier<Boolean> booleanGetter;
      final Consumer<Boolean> booleanSetter;
      final Supplier<Double> doubleGetter;
      final Consumer<Double> doubleSetter;
      final Supplier<Integer> intGetter;
      final Consumer<Integer> intSetter;
      final Supplier<String> stringGetter;
      final Consumer<String> stringSetter;
      final double minValue;
      final double maxValue;
      final double step;
      final Runnable buttonAction;
      final List<String> options;

      ConfigEntry(String key, ConfigScreen.ConfigEntry.Type type) {
         this(key, type, null, null, null, null, 0.0, 0.0, 0.0, null, null, null, null, null, null);
      }

      ConfigEntry(String key, ConfigScreen.ConfigEntry.Type type, Supplier<Boolean> booleanGetter, Consumer<Boolean> booleanSetter) {
         this(key, type, booleanGetter, booleanSetter, null, null, 0.0, 0.0, 0.0, null, null, null, null, null, null);
      }

      ConfigEntry(
         String key,
         ConfigScreen.ConfigEntry.Type type,
         Supplier<Double> doubleGetter,
         Consumer<Double> doubleSetter,
         double minValue,
         double maxValue,
         double step
      ) {
         this(key, type, null, null, doubleGetter, doubleSetter, minValue, maxValue, step, null, null, null, null, null, null);
      }

      ConfigEntry(String key, ConfigScreen.ConfigEntry.Type type, Runnable buttonAction) {
         this(key, type, null, null, null, null, 0.0, 0.0, 0.0, buttonAction, null, null, null, null, null);
      }

      ConfigEntry(String key, ConfigScreen.ConfigEntry.Type type, Supplier<Integer> intGetter, Consumer<Integer> intSetter, List<String> options) {
         this(key, type, null, null, null, null, 0.0, 0.0, 0.0, null, options, intGetter, intSetter, null, null);
      }

      private ConfigEntry(
         String key,
         ConfigScreen.ConfigEntry.Type type,
         Supplier<Boolean> booleanGetter,
         Consumer<Boolean> booleanSetter,
         Supplier<Double> doubleGetter,
         Consumer<Double> doubleSetter,
         double minValue,
         double maxValue,
         double step,
         Runnable buttonAction,
         List<String> options,
         Supplier<Integer> intGetter,
         Consumer<Integer> intSetter,
         Supplier<String> stringGetter,
         Consumer<String> stringSetter
      ) {
         this.key = key;
         this.type = type;
         this.booleanGetter = booleanGetter;
         this.booleanSetter = booleanSetter;
         this.doubleGetter = doubleGetter;
         this.doubleSetter = doubleSetter;
         this.intGetter = intGetter;
         this.intSetter = intSetter;
         this.stringGetter = stringGetter;
         this.stringSetter = stringSetter;
         this.minValue = minValue;
         this.maxValue = maxValue;
         this.step = step;
         this.buttonAction = buttonAction;
         this.options = options != null ? options : new ArrayList<>();
      }

      boolean getBooleanValue() {
         return this.booleanGetter != null ? this.booleanGetter.get() : false;
      }

      void setBooleanValue(boolean value) {
         if (this.booleanSetter != null) {
            this.booleanSetter.accept(value);
         }
      }

      double getDoubleValue() {
         return this.doubleGetter != null ? this.doubleGetter.get() : 0.0;
      }

      void setDoubleValue(double value) {
         if (this.doubleSetter != null) {
            this.doubleSetter.accept(value);
         }
      }

      int getIntValue() {
         return this.intGetter != null ? this.intGetter.get() : 0;
      }

      void setIntValue(int value) {
         if (this.intSetter != null) {
            this.intSetter.accept(value);
         }
      }

      String getStringValue() {
         return this.stringGetter != null ? this.stringGetter.get() : "";
      }

      void setStringValue(String value) {
         if (this.stringSetter != null) {
            this.stringSetter.accept(value);
         }
      }

      double normalizeValue() {
         if (this.doubleGetter == null) {
            return 0.0;
         }

         double value = this.doubleGetter.get();
         return (value - this.minValue) / (this.maxValue - this.minValue);
      }

      double denormalizeValue(double normalized) {
         double value = this.minValue + normalized * (this.maxValue - this.minValue);
         if (this.step > 0.0) {
            value = Math.round(value / this.step) * this.step;
            int decimalPlaces = Math.max(0, (int)Math.ceil(-Math.log10(this.step)));
            double scale = Math.pow(10.0, decimalPlaces);
            value = Math.round(value * scale) / scale;
         }

         return Math.max(this.minValue, Math.min(this.maxValue, value));
      }

      enum Type {
         CATEGORY,
         TOGGLE,
         SLIDER,
         BUTTON,
         SELECTOR,
         TEXT_INPUT;
      }
   }

   private class ConfigListWidget extends EntryListWidget<ConfigScreen.ConfigListWidget.Entry> {
      public ConfigListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
         super(client, width, height, top, bottom, itemHeight);

         for (ConfigScreen.ConfigEntry entry : ConfigScreen.this.configEntries) {
            this.method_25321(new ConfigScreen.ConfigListWidget.Entry(entry));
         }
      }

      protected void method_25325(DrawContext context) {
      }

      public int method_25322() {
         return 400;
      }

      protected int method_25329() {
         return this.field_22742 - 6;
      }

      public void method_37020(NarrationMessageBuilder builder) {
         builder.method_37033(NarrationPart.field_33788, "配置选项列表");
         if (this.method_25334() != null) {
            builder.method_37033(NarrationPart.field_33791, "使用方向键导航，Enter键选择");
         }
      }

      public void rebuildWithFilter(String filter) {
         int scrollAmount = (int)this.method_25341();
         this.method_25339();
         if (filter.isEmpty()) {
            for (ConfigScreen.ConfigEntry entry : ConfigScreen.this.allConfigEntries) {
               this.method_25321(new ConfigScreen.ConfigListWidget.Entry(entry));
            }
         } else {
            ConfigScreen.ConfigEntry currentCategory = null;
            List<ConfigScreen.ConfigEntry> currentBlock = new ArrayList<>();

            for (ConfigScreen.ConfigEntry entry : ConfigScreen.this.allConfigEntries) {
               if (entry.type == ConfigScreen.ConfigEntry.Type.CATEGORY) {
                  this.flushBlock(currentCategory, currentBlock, filter);
                  currentCategory = entry;
                  currentBlock.clear();
               } else {
                  currentBlock.add(entry);
               }
            }

            this.flushBlock(currentCategory, currentBlock, filter);
         }

         this.method_25307(Math.min(scrollAmount, this.method_25317()));
      }

      private void flushBlock(ConfigScreen.ConfigEntry category, List<ConfigScreen.ConfigEntry> entries, String filter) {
         if (category != null) {
            boolean categoryMatches = Text.method_43471(category.key).getString().toLowerCase().contains(filter);
            boolean anyEntryMatches = entries.stream().anyMatch(e -> Text.method_43471(e.key).getString().toLowerCase().contains(filter));
            if (categoryMatches || anyEntryMatches) {
               this.method_25321(new ConfigScreen.ConfigListWidget.Entry(category));

               for (ConfigScreen.ConfigEntry entry : entries) {
                  if (categoryMatches || Text.method_43471(entry.key).getString().toLowerCase().contains(filter)) {
                     this.method_25321(new ConfigScreen.ConfigListWidget.Entry(entry));
                  }
               }
            }
         }
      }

      private class Entry extends net.minecraft.client.gui.widget.EntryListWidget.Entry<ConfigScreen.ConfigListWidget.Entry> {
         private final ConfigScreen.ConfigEntry configEntry;
         private ClickableWidget controlWidget;

         public Entry(ConfigScreen.ConfigEntry configEntry) {
            this.configEntry = configEntry;
            if (configEntry.type == ConfigScreen.ConfigEntry.Type.CATEGORY) {
               this.controlWidget = null;
            } else if (configEntry.type == ConfigScreen.ConfigEntry.Type.TOGGLE) {
               this.controlWidget = CyclingButtonWidget.method_32613(configEntry.getBooleanValue())
                  .method_32617(0, 0, 150, 20, Text.method_43471(configEntry.key), (button, value) -> configEntry.setBooleanValue(value));
            } else if (configEntry.type == ConfigScreen.ConfigEntry.Type.SLIDER) {
               this.controlWidget = new SliderWidget(
                  0, 0, 150, 20, Text.method_43469(configEntry.key + ".value", new Object[]{configEntry.getDoubleValue()}), configEntry.normalizeValue()
               ) {
                  protected void method_25346() {
                     this.method_25355(Text.method_43469(configEntry.key + ".value", new Object[]{configEntry.getDoubleValue()}));
                  }

                  protected void method_25344() {
                     configEntry.setDoubleValue(configEntry.denormalizeValue(this.field_22753));
                  }
               };
            } else if (configEntry.type == ConfigScreen.ConfigEntry.Type.BUTTON) {
               this.controlWidget = ButtonWidget.method_46430(Text.method_43471(configEntry.key), button -> {
                  if (configEntry.buttonAction != null) {
                     configEntry.buttonAction.run();
                  }
               }).method_46434(0, 0, 150, 20).method_46431();
            } else if (configEntry.type == ConfigScreen.ConfigEntry.Type.SELECTOR) {
               List<Integer> values = new ArrayList<>();

               for (int i = 0; i < configEntry.options.size(); i++) {
                  values.add(i);
               }

               this.controlWidget = CyclingButtonWidget.method_32606(
                     value -> value >= 0 && value < configEntry.options.size()
                        ? Text.method_43471(configEntry.options.get(value))
                        : Text.method_43470("Unknown")
                  )
                  .method_32620(values)
                  .method_32619(configEntry.getIntValue())
                  .method_32617(0, 0, 150, 20, Text.method_43471(configEntry.key), (button, value) -> configEntry.setIntValue(value));
            } else if (configEntry.type == ConfigScreen.ConfigEntry.Type.TEXT_INPUT) {
               TextFieldWidget textField = new TextFieldWidget(ConfigScreen.this.field_22793, 0, 0, 150, 20, Text.method_43471(configEntry.key));
               textField.method_1852(configEntry.getStringValue());
               textField.method_1880(64);
               textField.method_1854((text, offset) -> {
                  StringBuilder masked = new StringBuilder();

                  for (int ix = 0; ix < text.length(); ix++) {
                     masked.append('*');
                  }

                  return Text.method_43470(masked.toString()).method_30937();
               });
               textField.method_1863(value -> configEntry.setStringValue(value));
               this.controlWidget = textField;
            }
         }

         public void method_25343(
            DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta
         ) {
            if (this.configEntry.type == ConfigScreen.ConfigEntry.Type.CATEGORY) {
               context.method_25303(ConfigScreen.this.field_22793, Text.method_43471(this.configEntry.key).getString(), x + 10, y + 6, 16776960);
            } else {
               context.method_25303(ConfigScreen.this.field_22793, Text.method_43471(this.configEntry.key).getString(), x + 10, y + 6, 16777215);
               if (this.controlWidget != null) {
                  this.controlWidget.method_46421(x + entryWidth - 160);
                  this.controlWidget.method_46419(y);
                  this.controlWidget.method_25394(context, mouseX, mouseY, tickDelta);
               }
            }
         }

         public boolean method_25402(double mouseX, double mouseY, int button) {
            if (this.controlWidget != null && this.controlWidget.method_25402(mouseX, mouseY, button)) {
               if (this.controlWidget instanceof TextFieldWidget) {
                  if (ConfigScreen.this.focusedTextField != null && ConfigScreen.this.focusedTextField != this.controlWidget) {
                     ConfigScreen.this.focusedTextField.method_25365(false);
                  }

                  ConfigScreen.this.focusedTextField = (TextFieldWidget)this.controlWidget;
                  ConfigScreen.this.focusedTextField.method_25365(true);
               } else if (ConfigScreen.this.focusedTextField != null) {
                  ConfigScreen.this.focusedTextField.method_25365(false);
                  ConfigScreen.this.focusedTextField = null;
               }

               return true;
            } else {
               if (ConfigScreen.this.focusedTextField != null) {
                  ConfigScreen.this.focusedTextField.method_25365(false);
                  ConfigScreen.this.focusedTextField = null;
               }

               return super.method_25402(mouseX, mouseY, button);
            }
         }

         public boolean method_25406(double mouseX, double mouseY, int button) {
            return this.controlWidget != null ? this.controlWidget.method_25406(mouseX, mouseY, button) : super.method_25406(mouseX, mouseY, button);
         }
      }
   }
}
