package com.xie.smfs.network.packets.common.c2s;

import com.xie.smfs.client.screen.SpearConfigScreen;
import com.xie.smfs.item.FissuredSpearPurpleItem;
import com.xie.smfs.item.WishSpearItem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SpearModeConfigC2SPacket {
   private static final Identifier REQUEST_ID = new Identifier("smfs", "spear_mode_request");
   private static final Identifier UPDATE_ID = new Identifier("smfs", "spear_mode_update");
   private static final Identifier CONFIG_ID = new Identifier("smfs", "spear_mode_config");
   private static final Identifier WISH_PRESET_UPDATE_ID = new Identifier("smfs", "wish_preset_update");
   private static final Identifier SHOW_WISH_TEXT_ID = new Identifier("smfs", "show_wish_text_update");

   public static void sendRequestToServer() {
      PacketByteBuf buf = PacketByteBufs.create();
      ClientPlayNetworking.send(REQUEST_ID, buf);
   }

   public static void sendModeUpdateToServer(String mode) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeString(mode);
      ClientPlayNetworking.send(UPDATE_ID, buf);
   }

   public static void sendWishPresetUpdateToServer(String preset) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeString(preset);
      ClientPlayNetworking.send(WISH_PRESET_UPDATE_ID, buf);
   }

   public static void sendShowWishTextUpdateToServer(boolean show) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeBoolean(show);
      ClientPlayNetworking.send(SHOW_WISH_TEXT_ID, buf);
   }

   public static void sendConfigToClient(ServerPlayerEntity player, String currentMode, boolean isWishSpear, String currentWishPreset, boolean showWishText) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeString(currentMode);
      buf.writeBoolean(isWishSpear);
      buf.writeString(currentWishPreset);
      buf.writeBoolean(showWishText);
      ServerPlayNetworking.send(player, CONFIG_ID, buf);
   }

   public static void registerClient() {
      ClientPlayNetworking.registerGlobalReceiver(CONFIG_ID, (client, handler, buf, responseSender) -> {
         String currentMode = buf.readString();
         boolean isWishSpear = buf.readBoolean();
         String currentWishPreset = buf.readString();
         boolean showWishText = buf.readBoolean();
         client.execute(() -> client.setScreen(new SpearConfigScreen(Text.literal("长枪配置"), currentMode, isWishSpear, currentWishPreset, showWishText)));
      });
   }

   public static void registerServer() {
      ServerPlayNetworking.registerGlobalReceiver(REQUEST_ID, (server, player, handler, buf, responseSender) -> server.execute(() -> {
         ItemStack stack = player.getMainHandStack();
         if (stack.getItem() instanceof FissuredSpearPurpleItem) {
            String currentMode = FissuredSpearPurpleItem.getThrowMode(stack);
            boolean isWishSpear = stack.getItem() instanceof WishSpearItem;
            String currentWishPreset = FissuredSpearPurpleItem.getWishPreset(stack);
            boolean showWishText = FissuredSpearPurpleItem.isShowWishText(stack);
            sendConfigToClient(player, currentMode, isWishSpear, currentWishPreset, showWishText);
         }
      }));
      ServerPlayNetworking.registerGlobalReceiver(UPDATE_ID, (server, player, handler, buf, responseSender) -> {
         String mode = buf.readString();
         server.execute(() -> {
            ItemStack stack = player.getMainHandStack();
            if (stack.getItem() instanceof FissuredSpearPurpleItem) {
               FissuredSpearPurpleItem.setThrowMode(stack, mode);
            }
         });
      });
      ServerPlayNetworking.registerGlobalReceiver(WISH_PRESET_UPDATE_ID, (server, player, handler, buf, responseSender) -> {
         String preset = buf.readString();
         server.execute(() -> {
            ItemStack stack = player.getMainHandStack();
            if (stack.getItem() instanceof FissuredSpearPurpleItem) {
               FissuredSpearPurpleItem.setWishPreset(stack, preset);
            }
         });
      });
      ServerPlayNetworking.registerGlobalReceiver(SHOW_WISH_TEXT_ID, (server, player, handler, buf, responseSender) -> {
         boolean show = buf.readBoolean();
         server.execute(() -> {
            ItemStack stack = player.getMainHandStack();
            if (stack.getItem() instanceof FissuredSpearPurpleItem) {
               FissuredSpearPurpleItem.setShowWishText(stack, show);
            }
         });
      });
   }

   public static boolean isSpearItem(Item item) {
      return item instanceof FissuredSpearPurpleItem;
   }
}
