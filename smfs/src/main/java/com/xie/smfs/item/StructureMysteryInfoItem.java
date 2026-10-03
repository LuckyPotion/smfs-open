package com.xie.smfs.item;

import com.xie.smfs.network.ClientModNetwork;
import com.xie.smfs.network.packets.common.s2c.StructureCoordinatesS2CPacket;
import com.xie.smfs.network.packets.skills.c2s.RequestStructureCoordinatesC2SPacket;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class StructureMysteryInfoItem extends Item {
   private final String structureId;
   @Nullable
   private final RegistryKey<World> requiredDimension;

   public StructureMysteryInfoItem(Settings settings, String structureId) {
      super(settings);
      this.structureId = structureId;
      this.requiredDimension = null;
   }

   public StructureMysteryInfoItem(Settings settings, String structureId, @Nullable RegistryKey<World> requiredDimension) {
      super(settings);
      this.structureId = structureId;
      this.requiredDimension = requiredDimension;
   }

   public Text method_7848() {
      return Text.method_43471("item.smfs.mystery_info." + this.structureId);
   }

   public Text method_7864(ItemStack stack) {
      return Text.method_43471("item.smfs.mystery_info." + this.structureId);
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.method_5998(hand);
      if (!world.field_9236) {
         if (this.requiredDimension != null && !world.method_27983().equals(this.requiredDimension)) {
            user.method_7353(
               Text.method_43469(
                  "message.smfs.mystery_info.wrong_dimension",
                  new Object[]{
                     Text.method_43471("item.smfs.mystery_info." + this.structureId),
                     Text.method_43471("dimension.smfs." + this.requiredDimension.method_29177().method_12832())
                  }
               ),
               false
            );
            return TypedActionResult.method_22431(stack);
         }

         String structureCoords = this.getStructureCoordinates(user);
         user.method_7353(
            Text.method_43469(
               "message.smfs.mystery_info.structure_location", new Object[]{Text.method_43471("structure.smfs." + this.structureId), structureCoords}
            ),
            false
         );
         if (structureCoords.equals(Text.method_43471("message.smfs.mystery_info.no_structure_found").getString())) {
            return TypedActionResult.method_22427(stack);
         }

         stack.method_7934(1);
         return TypedActionResult.method_22427(stack);
      } else {
         ClientModNetwork.sendToServer(new RequestStructureCoordinatesC2SPacket(this.structureId));
         return TypedActionResult.method_22427(stack);
      }
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.method_43471("item.smfs.mystery_info." + this.structureId + ".description.desc"));
      tooltip.add(Text.method_43471("tooltip.smfs.mystery_info.consumable"));
   }

   private String getStructureCoordinates(PlayerEntity player) {
      if (player.method_37908().method_8608()) {
         return Text.method_43471("message.smfs.mystery_info.getting_coordinates").getString();
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         BlockPos structurePos = StructureCoordinatesS2CPacket.findNearestStructure(serverPlayer, this.structureId);
         if (structurePos != null) {
            return "X: " + structurePos.method_10263() + ", Z: " + structurePos.method_10260();
         }
      }

      return Text.method_43471("message.smfs.mystery_info.no_structure_found").getString();
   }

   public String getStructureId() {
      return this.structureId;
   }
}
