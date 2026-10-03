package com.xie.smfs.network.packets.common.c2s;

import com.xie.smfs.client.screen.SpearBindScreen;
import com.xie.smfs.item.BoundSpearItem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SpearBindC2SPacket {
   private static final Identifier BIND_ID = new Identifier("smfs", "spear_bind");
   private static final Identifier ANSWER_ID = new Identifier("smfs", "spear_bind_answer");
   private final boolean isSettingOwner;
   private final String correctAnswer;

   public SpearBindC2SPacket(boolean isSettingOwner, String correctAnswer) {
      this.isSettingOwner = isSettingOwner;
      this.correctAnswer = correctAnswer;
   }

   public static void sendToClient(ServerPlayerEntity player, boolean isSettingOwner, String correctAnswer) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeBoolean(isSettingOwner);
      buf.method_10814(correctAnswer);
      ServerPlayNetworking.send(player, BIND_ID, buf);
   }

   public static void registerClient() {
      ClientPlayNetworking.registerGlobalReceiver(
         BIND_ID,
         (client, handler, buf, responseSender) -> {
            boolean isSettingOwner = buf.readBoolean();
            String correctAnswer = buf.method_19772();
            client.execute(
               () -> client.method_1507(new SpearBindScreen(Text.method_43470("请选择手持位置"), correctAnswer, isSettingOwner, choice -> sendAnswerToServer(choice)))
            );
         }
      );
   }

   private static void sendAnswerToServer(String choice) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.method_10814(choice);
      ClientPlayNetworking.send(ANSWER_ID, buf);
   }

   public static void registerServer() {
      ServerPlayNetworking.registerGlobalReceiver(ANSWER_ID, (server, player, handler, buf, responseSender) -> {
         String choice = buf.method_19772();
         server.execute(() -> {
            ItemStack stack = player.method_6047();
            if (stack.method_7909() instanceof BoundSpearItem) {
               ((BoundSpearItem)stack.method_7909()).handleAnswer(choice, stack, player);
            }
         });
      });
   }
}
