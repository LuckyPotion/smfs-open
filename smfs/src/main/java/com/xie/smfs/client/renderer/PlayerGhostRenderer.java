package com.xie.smfs.client.renderer;

import com.mojang.authlib.GameProfile;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerGhostRenderer extends BipedEntityRenderer<PlayerGhostEntity, BipedEntityModel<PlayerGhostEntity>> {
   private static final Identifier STEVE_TEXTURE = new Identifier("smfs", "textures/entity/steve.png");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/PlayerGhostRenderer");

   public PlayerGhostRenderer(Context ctx) {
      super(ctx, new BipedEntityModel(getModelPart(ctx)), 0.5F);
   }

   private static ModelPart getModelPart(Context ctx) {
      return ctx.getPart(EntityModelLayers.PLAYER);
   }

   public Identifier getTexture(PlayerGhostEntity entity) {
      if (!ModConfig.getInstance().enablePlayerGhostRealSkin) {
         return STEVE_TEXTURE;
      }

      String playerUuidStr = entity.getPlayerUuid();
      if (playerUuidStr != null && !playerUuidStr.isEmpty()) {
         try {
            UUID playerUuid = UUID.fromString(playerUuidStr);
            MinecraftClient client = MinecraftClient.getInstance();
            Identifier skin = this.tryGetSkinFromOnlinePlayer(playerUuid, client);
            if (skin != null) {
               return skin;
            }

            skin = this.tryGetSkinFromSkinProvider(playerUuid, client);
            if (skin != null) {
               return skin;
            }
         } catch (IllegalArgumentException e) {
            LOGGER.warn("[皮肤获取] UUID格式错误: {}", e.getMessage());
         }
      }

      return STEVE_TEXTURE;
   }

   private Identifier tryGetSkinFromOnlinePlayer(UUID playerUuid, MinecraftClient client) {
      if (client != null && client.world != null) {
         for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
            if (player != null && player.getUuid().equals(playerUuid)) {
               Identifier skinTexture = player.getSkinTexture();
               if (skinTexture != null) {
                  return skinTexture;
               }
            }
         }
      }

      return null;
   }

   private Identifier tryGetSkinFromSkinProvider(UUID playerUuid, MinecraftClient client) {
      if (client != null) {
         try {
            PlayerSkinProvider skinProvider = client.getSkinProvider();
            GameProfile profile = new GameProfile(playerUuid, null);
            Identifier skinTexture = skinProvider.loadSkin(profile);
            if (skinTexture != null) {
               return skinTexture;
            }
         } catch (Exception e) {
            LOGGER.warn("[皮肤获取] 方法2 - 异常: {}", e.getMessage());
         }
      }

      return null;
   }

   public void render(PlayerGhostEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      if (entity.isVisible()) {
         matrices.push();
         matrices.scale(1.2F, 1.2F, 1.2F);
         Identifier texture = this.getTexture(entity);
         VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture));
         super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
         matrices.pop();
      }
   }
}
