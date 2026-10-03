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
         player.sendMessage(Text.literal(GhostDomainManager.getInsufficientLevelMessage(player)), true);
      } else {
         double range = 64.0;
         BlockPos playerPos = player.getBlockPos();
         player.sendMessage(Text.literal("§a成功点燃生物周围方块！"), true);
         player.getWorld()
            .getOtherEntities(player, player.getBoundingBox().expand(range), entity -> entity.isAlive() && entity != player && entity instanceof LivingEntity)
            .forEach(entity -> igniteBlocksAroundEntity(player, entity));
      }
   }

   private static void igniteBlocksAroundEntity(ServerPlayerEntity player, Entity entity) {
      BlockPos entityPos = entity.getBlockPos();

      for (int x = entityPos.getX() - 1; x <= entityPos.getX() + 1; x++) {
         for (int y = entityPos.getY() - 1; y <= entityPos.getY() + 1; y++) {
            for (int z = entityPos.getZ() - 1; z <= entityPos.getZ() + 1; z++) {
               BlockPos blockPos = new BlockPos(x, y, z);
               BlockState blockState = player.getWorld().getBlockState(blockPos);
               if (canIgniteBlock(blockState)) {
                  player.getWorld().setBlockState(blockPos, Blocks.FIRE.getDefaultState());
               }
            }
         }
      }
   }

   private static boolean canIgniteBlock(BlockState blockState) {
      return !blockState.isAir() && blockState.getBlock() != Blocks.FIRE && blockState.isBurnable();
   }
}
