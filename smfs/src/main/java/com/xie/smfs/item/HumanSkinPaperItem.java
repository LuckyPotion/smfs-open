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

   public EquipmentSlot getSlotType() {
      return EquipmentSlot.HEAD;
   }

   public Text getName() {
      return Text.translatable("item.smfs.human_skin_paper");
   }

   public Text getName(ItemStack stack) {
      return Text.translatable("item.smfs.human_skin_paper");
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      user.setCurrentHand(hand);
      if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
         try {
            Identifier packetId = new Identifier("smfs", "open_human_skin_paper_screen");
            PacketByteBuf buf = PacketByteBufs.create();
            ServerPlayNetworking.send(serverPlayer, packetId, buf);
         } catch (Exception e) {
            LoggerFactory.getLogger("smfs/HumanSkinPaperItem").error("发送打开人皮纸界面网络包时发生错误: {}", e.getMessage());
         }
      }

      return TypedActionResult.consume(stack);
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.translatable("tooltip.smfs.human_skin_paper.description"));
   }
}
