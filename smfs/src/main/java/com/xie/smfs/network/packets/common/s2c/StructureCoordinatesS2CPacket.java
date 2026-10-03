package com.xie.smfs.network.packets.common.s2c;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.gen.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructureCoordinatesS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "structure_coordinates");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/StructureCoordinatesS2CPacket");
   public static final TagKey<Structure> BONE_TREE_STRUCTURE_TAG = TagKey.of(RegistryKeys.STRUCTURE, new Identifier("smfs", "bone_tree"));
   public static final TagKey<Structure> ANCIENT_HOUSE_STRUCTURE_TAG = TagKey.of(RegistryKeys.STRUCTURE, new Identifier("smfs", "ancient_house"));
   public static final TagKey<Structure> CAESAR_HOTEL_STRUCTURE_TAG = TagKey.of(RegistryKeys.STRUCTURE, new Identifier("smfs", "caesar_hotel"));
   public static final TagKey<Structure> CHINESE_MEDICINE_SHOP_STRUCTURE_TAG = TagKey.of(
      RegistryKeys.STRUCTURE, new Identifier("smfs", "chinese_medicine_shop")
   );
   public static final TagKey<Structure> FUREN_MALL_STRUCTURE_TAG = TagKey.of(RegistryKeys.STRUCTURE, new Identifier("smfs", "furen_mall"));
   public static final TagKey<Structure> GHOST_POST_OFFICE_STRUCTURE_TAG = TagKey.of(RegistryKeys.STRUCTURE, new Identifier("smfs", "ghost_post_office"));
   public static final TagKey<Structure> GRAVEYARD_STRUCTURE_TAG = TagKey.of(RegistryKeys.STRUCTURE, new Identifier("smfs", "graveyard"));
   public static final TagKey<Structure> SCHOOL_STRUCTURE_TAG = TagKey.of(RegistryKeys.STRUCTURE, new Identifier("smfs", "school"));
   public static final TagKey<Structure> BAISHUI_TOWN_STRUCTURE_TAG = TagKey.of(RegistryKeys.STRUCTURE, new Identifier("smfs", "baishui_town"));
   public static final TagKey<Structure> GHOST_LAKE_STRUCTURE_TAG = TagKey.of(RegistryKeys.STRUCTURE, new Identifier("smfs", "ghost_lake"));
   public static final TagKey<Structure> MANOR_STRUCTURE_TAG = TagKey.of(RegistryKeys.STRUCTURE, new Identifier("smfs", "manor"));
   public static final TagKey<Structure> HEADQUARTERS_STRUCTURE_TAG = TagKey.of(RegistryKeys.STRUCTURE, new Identifier("smfs", "headquarters"));
   private final String structureType;
   private final BlockPos coordinates;
   private final boolean found;

   public StructureCoordinatesS2CPacket(String structureType, BlockPos coordinates, boolean found) {
      this.structureType = structureType;
      this.coordinates = coordinates;
      this.found = found;
   }

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(ID, StructureCoordinatesS2CPacket::handle);
   }

   public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      String structureType = buf.readString();
      boolean found = buf.readBoolean();
      BlockPos coordinates = found ? buf.readBlockPos() : null;
      client.execute(
         () -> {
            if (found && coordinates != null) {
               String structureName = getStructureDisplayName(structureType);
               client.player
                  .sendMessage(
                     Text.translatable(
                        "message.smfs.structure_coordinates_found", new Object[]{structureName, coordinates.getX(), coordinates.getY(), coordinates.getZ()}
                     ),
                     false
                  );
               LOGGER.info("向玩家发送{}坐标: {}", structureName, coordinates);
            } else {
               String structureName = getStructureDisplayName(structureType);
               client.player.sendMessage(Text.translatable("message.smfs.structure_coordinates_not_found", new Object[]{structureName}), false);
               LOGGER.info("未找到{}坐标", structureName);
            }
         }
      );
   }

   public static void sendCoordinates(ServerPlayerEntity player, String structureType) {
      BlockPos structurePos = findNearestStructure(player, structureType);
      boolean found = structurePos != null;
      StructureCoordinatesS2CPacket packet = new StructureCoordinatesS2CPacket(structureType, structurePos, found);
      PacketByteBuf buf = PacketByteBufs.create();
      packet.write(buf);
      ServerPlayNetworking.send(player, ID, buf);
   }

   public static BlockPos findNearestStructure(ServerPlayerEntity player, String structureType) {
      try {
         BlockPos playerPos = player.getBlockPos();
         ServerWorld serverWorld = player.getServerWorld();
         int searchRadius = 5000;
         LOGGER.debug("开始搜索{}结构，玩家位置: {}, {}, {}", getStructureDisplayName(structureType), playerPos.getX(), playerPos.getY(), playerPos.getZ());
         TagKey<Structure> structureTag = getStructureTag(structureType);
         if (structureTag == null) {
            LOGGER.warn("未知的结构类型: {}", structureType);
            return null;
         }

         BlockPos structurePos = serverWorld.locateStructure(structureTag, playerPos, searchRadius, false);
         if (structurePos != null) {
            int centerY = serverWorld.getTopY(Type.WORLD_SURFACE, structurePos.getX(), structurePos.getZ());
            LOGGER.debug("找到{}结构，坐标: {}, {}, {}", getStructureDisplayName(structureType), structurePos.getX(), centerY, structurePos.getZ());
            return new BlockPos(structurePos.getX(), centerY, structurePos.getZ());
         }

         LOGGER.debug("未找到{}结构", getStructureDisplayName(structureType));
      } catch (Exception e) {
         LOGGER.error("搜索{}结构时发生错误", getStructureDisplayName(structureType), e);
      }

      return null;
   }

   private static TagKey<Structure> getStructureTag(String structureType) {
      switch (structureType) {
         case "bone_tree":
            return BONE_TREE_STRUCTURE_TAG;
         case "ancient_house":
            return ANCIENT_HOUSE_STRUCTURE_TAG;
         case "caesar_hotel":
            return CAESAR_HOTEL_STRUCTURE_TAG;
         case "chinese_medicine_shop":
            return CHINESE_MEDICINE_SHOP_STRUCTURE_TAG;
         case "furen_mall":
            return FUREN_MALL_STRUCTURE_TAG;
         case "ghost_post_office":
            return GHOST_POST_OFFICE_STRUCTURE_TAG;
         case "graveyard":
            return GRAVEYARD_STRUCTURE_TAG;
         case "school":
            return SCHOOL_STRUCTURE_TAG;
         case "baishui_town":
            return BAISHUI_TOWN_STRUCTURE_TAG;
         case "ghost_lake":
            return GHOST_LAKE_STRUCTURE_TAG;
         case "manor":
            return MANOR_STRUCTURE_TAG;
         case "headquarters":
            return HEADQUARTERS_STRUCTURE_TAG;
         default:
            return null;
      }
   }

   private static String getStructureDisplayName(String structureType) {
      switch (structureType) {
         case "bone_tree":
            return "白骨树";
         case "ancient_house":
            return "古宅";
         case "caesar_hotel":
            return "凯撒大酒店";
         case "chinese_medicine_shop":
            return "中药铺";
         case "furen_mall":
            return "富仁商场";
         case "ghost_post_office":
            return "鬼邮局";
         case "graveyard":
            return "墓地";
         case "school":
            return "学校";
         case "baishui_town":
            return "白水镇";
         case "ghost_lake":
            return "鬼湖";
         case "manor":
            return "庄园";
         case "headquarters":
            return "总部";
         default:
            return structureType;
      }
   }

   public void write(PacketByteBuf buf) {
      buf.writeString(this.structureType);
      buf.writeBoolean(this.found);
      if (this.found && this.coordinates != null) {
         buf.writeBlockPos(this.coordinates);
      }
   }

   public String getStructureType() {
      return this.structureType;
   }

   public BlockPos getCoordinates() {
      return this.coordinates;
   }

   public boolean isFound() {
      return this.found;
   }
}
