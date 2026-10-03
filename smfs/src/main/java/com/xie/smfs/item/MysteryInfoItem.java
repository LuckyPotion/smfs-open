package com.xie.smfs.item;

import com.xie.smfs.network.ClientModNetwork;
import com.xie.smfs.network.packets.common.s2c.StructureCoordinatesS2CPacket;
import com.xie.smfs.network.packets.skills.c2s.RequestBoneTreeCoordinatesC2SPacket;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MysteryInfoItem extends Item {
   public MysteryInfoItem(Settings settings) {
      super(settings);
   }

   public Text getName() {
      return Text.translatable("item.smfs.mystery_info");
   }

   public Text getName(ItemStack stack) {
      return Text.translatable("item.smfs.mystery_info");
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (!world.isClient) {
         String boneTreeCoords = this.getBoneTreeCoordinates(user);
         user.sendMessage(Text.translatable("message.smfs.mystery_info.bone_tree_location", new Object[]{boneTreeCoords}), false);
         if (boneTreeCoords.equals(Text.translatable("message.smfs.mystery_info.no_bone_tree_found").getString())) {
            return TypedActionResult.success(stack);
         }

         stack.decrement(1);
         return TypedActionResult.success(stack);
      } else {
         ClientModNetwork.sendToServer(new RequestBoneTreeCoordinatesC2SPacket());
         return TypedActionResult.success(stack);
      }
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.translatable("tooltip.smfs.mystery_info.coordinates"));
      tooltip.add(Text.translatable("tooltip.smfs.mystery_info.consumable"));
   }

   private String getBoneTreeCoordinates(PlayerEntity player) {
      if (player.getWorld().isClient()) {
         return Text.translatable("message.smfs.mystery_info.getting_coordinates").getString();
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         BlockPos boneTreePos = StructureCoordinatesS2CPacket.findNearestStructure(serverPlayer, "bone_tree");
         if (boneTreePos != null) {
            return "X: " + boneTreePos.getX() + ", Z: " + boneTreePos.getZ();
         }
      }

      return Text.translatable("message.smfs.mystery_info.no_bone_tree_found").getString();
   }
}
