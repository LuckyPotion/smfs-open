package com.xie.smfs.item;

import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

public class HumanSkinPaperItem extends Item implements Equipment {
   public HumanSkinPaperItem(Settings settings) {
      super(settings);
   }

   public EquipmentSlot method_7685() {
      return EquipmentSlot.field_6169;
   }

   public Text method_7848() {
      return Text.method_43471("item.smfs.human_skin_paper");
   }

   public Text method_7864(ItemStack stack) {
      return Text.method_43471("item.smfs.human_skin_paper");
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.method_5998(hand);
      user.method_6019(hand);
      if (!world.field_9236 && user instanceof ServerPlayerEntity serverPlayer) {
         try {
            Identifier packetId = new Identifier("smfs", "open_human_skin_paper_screen");
            PacketByteBuf buf = PacketByteBufs.create();
            ServerPlayNetworking.send(serverPlayer, packetId, buf);
         } catch (Exception e) {
            LoggerFactory.getLogger("smfs/HumanSkinPaperItem").error("发送打开人皮纸界面网络包时发生错误: {}", e.getMessage());
         }
      }

      return TypedActionResult.method_22428(stack);
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.method_43471("tooltip.smfs.human_skin_paper.description"));
   }
}
