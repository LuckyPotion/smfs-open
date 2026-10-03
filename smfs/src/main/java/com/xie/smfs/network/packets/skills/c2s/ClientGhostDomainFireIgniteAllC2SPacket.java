package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.manager.GhostDomainManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGhostDomainFireIgniteAllC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "client_ghost_domain_fire_ignite_all");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostDomainFireIgniteAllC2SPacket");

   public ClientGhostDomainFireIgniteAllC2SPacket() {
   }

   public ClientGhostDomainFireIgniteAllC2SPacket(PacketByteBuf buf) {
   }

   public void write(PacketByteBuf buf) {
   }

   public static void handle(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> igniteAllInGhostDomain(player));
   }

   private static void igniteAllInGhostDomain(ServerPlayerEntity player) {
      int ghostFireLevel = GhostDomainManager.getEffectiveSkillLevel(player, GhostDomainManager.getGhostFireLevel(player));
      if (ghostFireLevel < 4) {
         player.method_7353(Text.method_43470(GhostDomainManager.getInsufficientLevelMessage(player)), true);
      } else {
         double range = 64.0;
         BlockPos playerPos = player.method_24515();
         player.method_7353(Text.method_43470("§a成功点燃生物周围方块！"), true);
         player.method_37908()
            .method_8333(player, player.method_5829().method_1014(range), entity -> entity.method_5805() && entity != player && entity instanceof LivingEntity)
            .forEach(entity -> igniteBlocksAroundEntity(player, entity));
      }
   }

   private static void igniteBlocksAroundEntity(ServerPlayerEntity player, Entity entity) {
      BlockPos entityPos = entity.method_24515();

      for (int x = entityPos.method_10263() - 1; x <= entityPos.method_10263() + 1; x++) {
         for (int y = entityPos.method_10264() - 1; y <= entityPos.method_10264() + 1; y++) {
            for (int z = entityPos.method_10260() - 1; z <= entityPos.method_10260() + 1; z++) {
               BlockPos blockPos = new BlockPos(x, y, z);
               BlockState blockState = player.method_37908().method_8320(blockPos);
               if (canIgniteBlock(blockState)) {
                  player.method_37908().method_8501(blockPos, Blocks.field_10036.method_9564());
               }
            }
         }
      }
   }

   private static boolean canIgniteBlock(BlockState blockState) {
      return !blockState.method_26215() && blockState.method_26204() != Blocks.field_10036 && blockState.method_50011();
   }
}
