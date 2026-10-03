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

   public Text getName() {
      return Text.translatable("item.smfs.mystery_info." + this.structureId);
   }

   public Text getName(ItemStack stack) {
      return Text.translatable("item.smfs.mystery_info." + this.structureId);
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (!world.isClient) {
         if (this.requiredDimension != null && !world.getRegistryKey().equals(this.requiredDimension)) {
            user.sendMessage(
               Text.translatable(
                  "message.smfs.mystery_info.wrong_dimension",
                  new Object[]{
                     Text.translatable("item.smfs.mystery_info." + this.structureId),
                     Text.translatable("dimension.smfs." + this.requiredDimension.getValue().getPath())
                  }
               ),
               false
            );
            return TypedActionResult.fail(stack);
         }

         String structureCoords = this.getStructureCoordinates(user);
         user.sendMessage(
            Text.translatable(
               "message.smfs.mystery_info.structure_location", new Object[]{Text.translatable("structure.smfs." + this.structureId), structureCoords}
            ),
            false
         );
         if (structureCoords.equals(Text.translatable("message.smfs.mystery_info.no_structure_found").getString())) {
            return TypedActionResult.success(stack);
         }

         stack.decrement(1);
         return TypedActionResult.success(stack);
      } else {
         ClientModNetwork.sendToServer(new RequestStructureCoordinatesC2SPacket(this.structureId));
         return TypedActionResult.success(stack);
      }
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.translatable("item.smfs.mystery_info." + this.structureId + ".description.desc"));
      tooltip.add(Text.translatable("tooltip.smfs.mystery_info.consumable"));
   }

   private String getStructureCoordinates(PlayerEntity player) {
      if (player.getWorld().isClient()) {
         return Text.translatable("message.smfs.mystery_info.getting_coordinates").getString();
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         BlockPos structurePos = StructureCoordinatesS2CPacket.findNearestStructure(serverPlayer, this.structureId);
         if (structurePos != null) {
            return "X: " + structurePos.getX() + ", Z: " + structurePos.getZ();
         }
      }

      return Text.translatable("message.smfs.mystery_info.no_structure_found").getString();
   }

   public String getStructureId() {
      return this.structureId;
   }
}
