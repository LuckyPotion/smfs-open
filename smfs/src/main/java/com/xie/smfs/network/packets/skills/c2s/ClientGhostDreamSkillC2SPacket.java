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
            if (player != null && player.method_5805()) {
               World world = player.method_37908();
               HitResult hitResult = player.method_5745(30.0, 0.0F, false);
               if (hitResult.method_17783() == Type.field_1333) {
                  player.method_7353(Text.method_43470("§c请对准一个位置使用鬼梦技能"), true);
               } else {
                  BlockPos targetPos;
                  if (hitResult.method_17783() == Type.field_1332) {
                     BlockHitResult blockHitResult = (BlockHitResult)hitResult;
                     BlockPos blockPos = blockHitResult.method_17777();
                     Direction side = blockHitResult.method_17780();
                     targetPos = blockPos.method_10093(side);
                  } else {
                     Vec3d pos = hitResult.method_17784();
                     targetPos = new BlockPos((int)pos.field_1352, (int)pos.field_1351, (int)pos.field_1350);
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
         GhostDreamEntity ghostDream = (GhostDreamEntity)ModEntities.GHOST_DREAM.method_5883(world);
         if (ghostDream != null) {
            ghostDream.method_5808(
               targetPos.method_10263() + 0.5, targetPos.method_10264() + 1.0, targetPos.method_10260() + 0.5, world.field_9229.method_43057() * 360.0F, 0.0F
            );
            ghostDream.method_6170(player);
            ghostDream.method_6173(true);
            world.method_8649(ghostDream);
            LOGGER.debug("玩家 {} 使用鬼梦J键技能，成功在位置 {} 生成鬼梦生物", player.method_5477().getString(), targetPos.toString());
            player.method_7353(Text.method_43470("§a成功召唤鬼梦生物"), true);
         } else {
            LOGGER.error("无法创建鬼梦实体");
            player.method_7353(Text.method_43470("§c召唤失败"), true);
         }
      } catch (Exception e) {
         LOGGER.error("生成鬼梦生物时发生错误", e);
         player.method_7353(Text.method_43470("§c召唤失败"), true);
      }
   }
}
