package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.ghost.GhostDreamEntity;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.registry.ModEntities;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGhostDreamSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_dream_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostDreamSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostDreamSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (GhostDomainManager.checkAndSetJSkillCooldown(player, "ghost_dream_j_skill", 200, "鬼梦J键技能")) {
            if (player != null && player.isAlive()) {
               World world = player.getWorld();
               HitResult hitResult = player.raycast(30.0, 0.0F, false);
               if (hitResult.getType() == Type.MISS) {
                  player.sendMessage(Text.literal("§c请对准一个位置使用鬼梦技能"), true);
               } else {
                  BlockPos targetPos;
                  if (hitResult.getType() == Type.BLOCK) {
                     BlockHitResult blockHitResult = (BlockHitResult)hitResult;
                     BlockPos blockPos = blockHitResult.getBlockPos();
                     Direction side = blockHitResult.getSide();
                     targetPos = blockPos.offset(side);
                  } else {
                     Vec3d pos = hitResult.getPos();
                     targetPos = new BlockPos((int)pos.x, (int)pos.y, (int)pos.z);
                  }

                  spawnGhostDream(world, targetPos, player);
                  PlayerEvents.balanceRevivalDegree(player);
               }
            } else {
               LOGGER.warn("玩家无效，无法执行鬼梦技能");
            }
         }
      });
   }

   private static void spawnGhostDream(World world, BlockPos targetPos, ServerPlayerEntity player) {
      try {
         GhostDreamEntity ghostDream = (GhostDreamEntity)ModEntities.GHOST_DREAM.create(world);
         if (ghostDream != null) {
            ghostDream.refreshPositionAndAngles(targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5, world.random.nextFloat() * 360.0F, 0.0F);
            ghostDream.setOwner(player);
            ghostDream.setTamed(true);
            world.spawnEntity(ghostDream);
            LOGGER.debug("玩家 {} 使用鬼梦J键技能，成功在位置 {} 生成鬼梦生物", player.getName().getString(), targetPos.toString());
            player.sendMessage(Text.literal("§a成功召唤鬼梦生物"), true);
         } else {
            LOGGER.error("无法创建鬼梦实体");
            player.sendMessage(Text.literal("§c召唤失败"), true);
         }
      } catch (Exception e) {
         LOGGER.error("生成鬼梦生物时发生错误", e);
         player.sendMessage(Text.literal("§c召唤失败"), true);
      }
   }
}
