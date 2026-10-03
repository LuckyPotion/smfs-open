package com.xie.smfs.network.packets.common.s2c;

import com.mojang.datafixers.util.Pair;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryList.Direct;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoneTreeCoordinatesS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "bone_tree_coordinates");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/BoneTreeCoordinatesS2CPacket");
   public static final TagKey<Structure> BONE_TREE_STRUCTURE_TAG = TagKey.method_40092(RegistryKeys.field_41246, new Identifier("smfs", "bone_tree"));
   private static final AtomicReference<String> receivedCoordinates = new AtomicReference<>(null);

   public static void registerClient() {
      ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
         String coordinates = buf.method_19772();
         client.execute(() -> {
            receivedCoordinates.set(coordinates);
            LOGGER.debug("客户端接收到白骨树坐标: {}", coordinates);
         });
      });
   }

   public static String getReceivedCoordinates() {
      return receivedCoordinates.get();
   }

   public static void clearReceivedCoordinates() {
      receivedCoordinates.set(null);
   }

   public static void sendCoordinates(ServerPlayerEntity player, String coordinates) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.method_10814(coordinates);
      ServerPlayNetworking.send(player, ID, buf);
      LOGGER.debug("向玩家 {} 发送白骨树坐标: {}", player.method_5477().getString(), coordinates);
   }

   public static void sendCoordinates(ServerPlayerEntity player, BlockPos pos) {
      String coordinates = String.format("X: %d, Z: %d", pos.method_10263(), pos.method_10260());
      sendCoordinates(player, coordinates);
   }

   public static void sendCoordinates(ServerPlayerEntity player) {
      BlockPos nearestBoneTree = findNearestBoneTree(player);
      if (nearestBoneTree != null) {
         sendCoordinates(player, nearestBoneTree);
      } else {
         sendCoordinates(player, "未找到白骨树");
         LOGGER.warn("无法为玩家 {} 找到白骨树结构", player.method_5477().getString());
      }
   }

   public static BlockPos findNearestBoneTree(ServerPlayerEntity player) {
      try {
         BlockPos playerPos = player.method_24515();
         ServerWorld serverWorld = player.method_51469();
         int searchRadius = 5000;
         LOGGER.debug("开始搜索白骨树结构，玩家位置: {}, {}, {}", playerPos.method_10263(), playerPos.method_10264(), playerPos.method_10260());
         BlockPos structurePos = serverWorld.method_8487(BONE_TREE_STRUCTURE_TAG, playerPos, searchRadius, false);
         if (structurePos != null) {
            int centerY = serverWorld.method_8624(Type.field_13202, structurePos.method_10263(), structurePos.method_10260());
            LOGGER.debug("找到白骨树结构，坐标: {}, {}, {}", structurePos.method_10263(), centerY, structurePos.method_10260());
            return new BlockPos(structurePos.method_10263(), centerY, structurePos.method_10260());
         } else {
            LOGGER.debug("TagKey方式未找到白骨树结构，尝试备选方案");
            return findNearestBoneTreeAlternative(player);
         }
      } catch (Exception e) {
         LOGGER.error("搜索白骨树结构时发生错误", e);
         return null;
      }
   }

   public static BlockPos findNearestBoneTreeAlternative(ServerPlayerEntity player) {
      try {
         BlockPos playerPos = player.method_24515();
         ServerWorld serverWorld = player.method_51469();
         Registry<Structure> structureRegistry = serverWorld.method_30349().method_30530(RegistryKeys.field_41246);
         Identifier boneTreeId = new Identifier("smfs", "bone_tree");
         Structure boneTreeStructure = (Structure)structureRegistry.method_10223(boneTreeId);
         if (boneTreeStructure == null) {
            LOGGER.warn("白骨树结构未注册: {}", boneTreeId);
            return null;
         }

         RegistryEntry<Structure> structureEntry = structureRegistry.method_47983(boneTreeStructure);
         if (structureEntry == null) {
            LOGGER.warn("无法获取白骨树结构的注册表条目");
            return null;
         }

         int searchRadius = 5000;
         Direct<Structure> structureList = RegistryEntryList.method_40246(new RegistryEntry[]{structureEntry});
         ChunkGenerator chunkGenerator = serverWorld.method_14178().method_12129();
         Pair<BlockPos, RegistryEntry<Structure>> structureResult = chunkGenerator.method_12103(serverWorld, structureList, playerPos, searchRadius, false);
         if (structureResult != null) {
            BlockPos structurePos = (BlockPos)structureResult.getFirst();
            int centerY = serverWorld.method_8624(Type.field_13202, structurePos.method_10263(), structurePos.method_10260());
            LOGGER.debug("找到白骨树结构，坐标: {}, {}, {}", structurePos.method_10263(), centerY, structurePos.method_10260());
            return new BlockPos(structurePos.method_10263(), centerY, structurePos.method_10260());
         }
      } catch (Exception e) {
         LOGGER.error("搜索白骨树结构时发生错误", e);
      }

      return null;
   }

   public static BlockPos findNearestBoneTreeSimple(ServerPlayerEntity player) {
      try {
         BlockPos playerPos = player.method_24515();
         ServerWorld serverWorld = player.method_51469();
         int searchRadius = 5000;
         BlockPos structurePos = serverWorld.method_8487(StructureTags.field_37045, playerPos, searchRadius, false);
         if (structurePos != null) {
            int centerY = serverWorld.method_8624(Type.field_13202, structurePos.method_10263(), structurePos.method_10260());
            LOGGER.debug("找到测试结构，坐标: {}, {}, {}", structurePos.method_10263(), centerY, structurePos.method_10260());
            return new BlockPos(structurePos.method_10263(), centerY, structurePos.method_10260());
         }

         LOGGER.debug("未找到测试结构，请检查白骨树结构标签配置");
      } catch (Exception e) {
         LOGGER.error("搜索结构时发生错误", e);
      }

      return null;
   }

   public static boolean hasReceivedCoordinates() {
      return receivedCoordinates.get() != null;
   }
}
