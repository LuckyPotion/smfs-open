package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.util.TargetingUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientLostGhostBlindC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "lost_ghost_blind_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientLostGhostBlindC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientLostGhostBlindC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         try {
            LOGGER.info("玩家 {} 执行遗忘鬼G键技能：打乱目标玩家物品栏", player.method_5477().getString());
            PlayerEvents.balanceRevivalDegree(player);
            handleLostGhostShuffleSkill(player);
         } catch (Exception e) {
            LOGGER.error("处理遗忘鬼G键技能时发生错误", e);
         }
      });
   }

   private static void handleLostGhostShuffleSkill(ServerPlayerEntity player) {
      LivingEntity bestTarget = TargetingUtil.findEntityInLookDirection(player, 4.0, 0.5, e -> e instanceof ServerPlayerEntity);
      if (bestTarget == null) {
         LOGGER.warn("玩家 {} 准星位置没有其他玩家，无法使用打乱物品栏技能", player.method_5477().getString());
      } else {
         ServerPlayerEntity targetPlayer = (ServerPlayerEntity)bestTarget;
         shufflePlayerInventory(targetPlayer);
         LOGGER.debug("玩家 {} 成功对玩家 {} 使用打乱物品栏技能", player.method_5477().getString(), bestTarget.method_5477().getString());
         spawnSkillParticles(player, targetPlayer);
      }
   }

   private static void shufflePlayerInventory(ServerPlayerEntity player) {
      PlayerInventory inventory = player.method_31548();
      List<ItemStack> allStacks = new ArrayList<>();

      for (int i = 0; i < inventory.field_7547.size(); i++) {
         ItemStack stack = (ItemStack)inventory.field_7547.get(i);
         if (!stack.method_7960()) {
            allStacks.add(stack.method_7972());
            inventory.field_7547.set(i, ItemStack.field_8037);
         }
      }

      for (int i = 0; i < inventory.field_7548.size(); i++) {
         ItemStack stack = (ItemStack)inventory.field_7548.get(i);
         if (!stack.method_7960()) {
            allStacks.add(stack.method_7972());
            inventory.field_7548.set(i, ItemStack.field_8037);
         }
      }

      ItemStack offhandStack = (ItemStack)inventory.field_7544.get(0);
      if (!offhandStack.method_7960()) {
         allStacks.add(offhandStack.method_7972());
         inventory.field_7544.set(0, ItemStack.field_8037);
      }

      Collections.shuffle(allStacks, new Random());
      int totalSlots = inventory.field_7547.size() + inventory.field_7548.size() + 1;
      List<Integer> slotIndices = new ArrayList<>();

      for (int i = 0; i < totalSlots; i++) {
         slotIndices.add(i);
      }

      Collections.shuffle(slotIndices, new Random());

      for (int i = 0; i < allStacks.size() && i < slotIndices.size(); i++) {
         int targetSlot = slotIndices.get(i);
         if (targetSlot < inventory.field_7547.size()) {
            inventory.field_7547.set(targetSlot, allStacks.get(i));
         } else if (targetSlot < inventory.field_7547.size() + inventory.field_7548.size()) {
            inventory.field_7548.set(targetSlot - inventory.field_7547.size(), allStacks.get(i));
         } else {
            inventory.field_7544.set(0, allStacks.get(i));
         }
      }
   }

   private static void spawnSkillParticles(ServerPlayerEntity player, ServerPlayerEntity targetPlayer) {
      World world = player.method_37908();

      for (int i = 0; i < 10; i++) {
         world.method_8406(
            ParticleTypes.field_22246,
            player.method_23317() + (world.field_9229.method_43058() - 0.5) * 2.0,
            player.method_23318() + world.field_9229.method_43058() * 2.0,
            player.method_23321() + (world.field_9229.method_43058() - 0.5) * 2.0,
            0.0,
            0.1,
            0.0
         );
      }

      for (int i = 0; i < 10; i++) {
         world.method_8406(
            ParticleTypes.field_22246,
            targetPlayer.method_23317() + (world.field_9229.method_43058() - 0.5) * 2.0,
            targetPlayer.method_23318() + world.field_9229.method_43058() * 2.0,
            targetPlayer.method_23321() + (world.field_9229.method_43058() - 0.5) * 2.0,
            0.0,
            0.1,
            0.0
         );
      }
   }
}
