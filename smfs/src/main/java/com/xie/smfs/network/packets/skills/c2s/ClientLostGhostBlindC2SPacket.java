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
            LOGGER.info("玩家 {} 执行遗忘鬼G键技能：打乱目标玩家物品栏", player.getName().getString());
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
         LOGGER.warn("玩家 {} 准星位置没有其他玩家，无法使用打乱物品栏技能", player.getName().getString());
      } else {
         ServerPlayerEntity targetPlayer = (ServerPlayerEntity)bestTarget;
         shufflePlayerInventory(targetPlayer);
         LOGGER.debug("玩家 {} 成功对玩家 {} 使用打乱物品栏技能", player.getName().getString(), bestTarget.getName().getString());
         spawnSkillParticles(player, targetPlayer);
      }
   }

   private static void shufflePlayerInventory(ServerPlayerEntity player) {
      PlayerInventory inventory = player.getInventory();
      List<ItemStack> allStacks = new ArrayList<>();

      for (int i = 0; i < inventory.main.size(); i++) {
         ItemStack stack = (ItemStack)inventory.main.get(i);
         if (!stack.isEmpty()) {
            allStacks.add(stack.copy());
            inventory.main.set(i, ItemStack.EMPTY);
         }
      }

      for (int i = 0; i < inventory.armor.size(); i++) {
         ItemStack stack = (ItemStack)inventory.armor.get(i);
         if (!stack.isEmpty()) {
            allStacks.add(stack.copy());
            inventory.armor.set(i, ItemStack.EMPTY);
         }
      }

      ItemStack offhandStack = (ItemStack)inventory.offHand.get(0);
      if (!offhandStack.isEmpty()) {
         allStacks.add(offhandStack.copy());
         inventory.offHand.set(0, ItemStack.EMPTY);
      }

      Collections.shuffle(allStacks, new Random());
      int totalSlots = inventory.main.size() + inventory.armor.size() + 1;
      List<Integer> slotIndices = new ArrayList<>();

      for (int i = 0; i < totalSlots; i++) {
         slotIndices.add(i);
      }

      Collections.shuffle(slotIndices, new Random());

      for (int i = 0; i < allStacks.size() && i < slotIndices.size(); i++) {
         int targetSlot = slotIndices.get(i);
         if (targetSlot < inventory.main.size()) {
            inventory.main.set(targetSlot, allStacks.get(i));
         } else if (targetSlot < inventory.main.size() + inventory.armor.size()) {
            inventory.armor.set(targetSlot - inventory.main.size(), allStacks.get(i));
         } else {
            inventory.offHand.set(0, allStacks.get(i));
         }
      }
   }

   private static void spawnSkillParticles(ServerPlayerEntity player, ServerPlayerEntity targetPlayer) {
      World world = player.getWorld();

      for (int i = 0; i < 10; i++) {
         world.addParticle(
            ParticleTypes.SOUL_FIRE_FLAME,
            player.getX() + (world.random.nextDouble() - 0.5) * 2.0,
            player.getY() + world.random.nextDouble() * 2.0,
            player.getZ() + (world.random.nextDouble() - 0.5) * 2.0,
            0.0,
            0.1,
            0.0
         );
      }

      for (int i = 0; i < 10; i++) {
         world.addParticle(
            ParticleTypes.SOUL_FIRE_FLAME,
            targetPlayer.getX() + (world.random.nextDouble() - 0.5) * 2.0,
            targetPlayer.getY() + world.random.nextDouble() * 2.0,
            targetPlayer.getZ() + (world.random.nextDouble() - 0.5) * 2.0,
            0.0,
            0.1,
            0.0
         );
      }
   }
}
