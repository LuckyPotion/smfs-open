package com.xie.smfs.network.packets.config.c2s;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xie.smfs.config.ModConfig;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigSyncC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "config_sync");
   private static final Logger LOGGER = LoggerFactory.getLogger(ConfigSyncC2SPacket.class);
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private String configJson;

   public ConfigSyncC2SPacket() {
   }

   public ConfigSyncC2SPacket(ModConfig config) {
      this.configJson = GSON.toJson(config);
   }

   public ConfigSyncC2SPacket(PacketByteBuf buf) {
      this.configJson = buf.method_19772();
   }

   public void write(PacketByteBuf buf) {
      buf.method_10814(this.configJson);
   }

   public static void handle(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      String configJson = buf.method_19772();
      server.execute(() -> {
         if (!server.method_3724() && !player.method_5687(4)) {
            LOGGER.warn("玩家 {} 没有权限修改服务端配置", player.method_5477().getString());
         } else {
            try {
               ModConfig newConfig = GSON.fromJson(configJson, ModConfig.class);
               if (newConfig != null) {
                  newConfig.validate();
                  ModConfig serverConfig = ModConfig.getInstance();
                  serverConfig.enableEvilGhosts = newConfig.enableEvilGhosts;
                  serverConfig.evilGhostMaxSpawnRange = newConfig.evilGhostMaxSpawnRange;
                  serverConfig.evilGhostMinSpawnRange = newConfig.evilGhostMinSpawnRange;
                  serverConfig.allowEvilGhostNaturalDespawn = newConfig.allowEvilGhostNaturalDespawn;
                  serverConfig.ghostRespawnConfigs = newConfig.ghostRespawnConfigs;
                  serverConfig.ghostDomainBaseSize = newConfig.ghostDomainBaseSize;
                  serverConfig.ghostDomainSizePerLevel = newConfig.ghostDomainSizePerLevel;
                  serverConfig.enableGhostDomainEffectsOnMobs = newConfig.enableGhostDomainEffectsOnMobs;
                  serverConfig.instantReincarnation = newConfig.instantReincarnation;
                  serverConfig.keepGhostsAfterMirrorResurrection = newConfig.keepGhostsAfterMirrorResurrection;
                  serverConfig.bodyEnhancementMode = newConfig.bodyEnhancementMode;
                  serverConfig.enableQuestSystem = newConfig.enableQuestSystem;
                  serverConfig.enablePlayerGhostRealSkin = newConfig.enablePlayerGhostRealSkin;
                  serverConfig.irisCompatibilityMode = newConfig.irisCompatibilityMode;
                  serverConfig.enableEvilGhostRevival = newConfig.enableEvilGhostRevival;
                  serverConfig.loseGhostsOnDeath = newConfig.loseGhostsOnDeath;
                  serverConfig.ghostMasterRevival = newConfig.ghostMasterRevival;
                  serverConfig.showActionBarInfo = newConfig.showActionBarInfo;
                  serverConfig.hardcoreDeadlockMode = newConfig.hardcoreDeadlockMode;
                  serverConfig.enableResentmentSystem = newConfig.enableResentmentSystem;
                  serverConfig.enableSanitySystem = newConfig.enableSanitySystem;
                  serverConfig.enablePlayerSpiritDamage = newConfig.enablePlayerSpiritDamage;
                  serverConfig.ghostMasterDropChances = newConfig.ghostMasterDropChances;
                  serverConfig.save();
                  ModConfig.reload();
                  LOGGER.info("服务端配置已由玩家 {} 更新", player.method_5477().getString());
               }
            } catch (Exception e) {
               LOGGER.error("处理配置同步数据包失败", e);
            }
         }
      });
   }
}
