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

   public Text method_7848() {
      return Text.method_43471("item.smfs.mystery_info");
   }

   public Text method_7864(ItemStack stack) {
      return Text.method_43471("item.smfs.mystery_info");
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.method_5998(hand);
      if (!world.field_9236) {
         String boneTreeCoords = this.getBoneTreeCoordinates(user);
         user.method_7353(Text.method_43469("message.smfs.mystery_info.bone_tree_location", new Object[]{boneTreeCoords}), false);
         if (boneTreeCoords.equals(Text.method_43471("message.smfs.mystery_info.no_bone_tree_found").getString())) {
            return TypedActionResult.method_22427(stack);
         }

         stack.method_7934(1);
         return TypedActionResult.method_22427(stack);
      } else {
         ClientModNetwork.sendToServer(new RequestBoneTreeCoordinatesC2SPacket());
         return TypedActionResult.method_22427(stack);
      }
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.method_43471("tooltip.smfs.mystery_info.coordinates"));
      tooltip.add(Text.method_43471("tooltip.smfs.mystery_info.consumable"));
   }

   private String getBoneTreeCoordinates(PlayerEntity player) {
      if (player.method_37908().method_8608()) {
         return Text.method_43471("message.smfs.mystery_info.getting_coordinates").getString();
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         BlockPos boneTreePos = StructureCoordinatesS2CPacket.findNearestStructure(serverPlayer, "bone_tree");
         if (boneTreePos != null) {
            return "X: " + boneTreePos.method_10263() + ", Z: " + boneTreePos.method_10260();
         }
      }

      return Text.method_43471("message.smfs.mystery_info.no_bone_tree_found").getString();
   }
}
