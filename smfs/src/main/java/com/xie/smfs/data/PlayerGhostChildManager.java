package com.xie.smfs.data;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.ghost.GhostChildEntity;
import com.xie.smfs.registry.ModEntities;
import java.util.Iterator;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerGhostChildManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(PlayerGhostChildManager.class);
   private static final Identifier GHOST_CHILD_ID = new Identifier("smfs", "ghost_child");

   public static GhostChildData getGhostChildData(PlayerEntity player) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      if (!playerData.contains("GhostChildData")) {
         return new GhostChildData();
      }

      NbtCompound ghostChildNbt = playerData.getCompound("GhostChildData");
      return new GhostChildData(ghostChildNbt);
   }

   public static void saveGhostChildData(PlayerEntity player, GhostChildData data) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      NbtCompound ghostChildNbt = new NbtCompound();
      data.saveToNbt(ghostChildNbt);
      playerData.put("GhostChildData", ghostChildNbt);
      PlayerEvents.saveDataToPlayer(player, playerData);
   }

   public static void summonGhostChild(PlayerEntity player) {
      GhostChildData data = getGhostChildData(player);
      if (!data.isSummoned() && player.getWorld() instanceof ServerWorld serverWorld) {
         Iterator var11 = serverWorld.getEntitiesByClass(
               GhostChildEntity.class,
               new Box(-3.0E7, -64.0, -3.0E7, 3.0E7, 320.0, 3.0E7),
               entity -> entity.getOwner() != null && entity.getOwner().getUuid().equals(player.getUuid())
            )
            .iterator();
         if (var11.hasNext()) {
            GhostChildEntity existingGhostChild = (GhostChildEntity)var11.next();
            existingGhostChild.setSummoned(true);
            data.summon();
            saveGhostChildData(player, data);
            Vec3d spawnPos = existingGhostChild.getPos();
            playSummonEffects(player, spawnPos);
            player.sendMessage(Text.literal("§a鬼童已召唤"), true);
            return;
         }

         GhostChildEntity ghostChild = (GhostChildEntity)ModEntities.GHOST_CHILD.create(serverWorld);
         if (ghostChild != null) {
            ghostChild.setOwner(player);
            ghostChild.setSummoned(true);
            Vec3d playerPos = player.getPos();
            Vec3d spawnPos = playerPos.add(player.getRotationVector().multiply(3.0));
            int spawnX = (int)Math.floor(spawnPos.x);
            int spawnZ = (int)Math.floor(spawnPos.z);
            int topY = serverWorld.getTopY(Type.MOTION_BLOCKING, spawnX, spawnZ);
            double spawnY;
            if (topY > 0) {
               spawnY = Math.max(topY + 0.5, spawnPos.y);
            } else {
               spawnY = playerPos.y;
               spawnPos = playerPos;
            }

            ghostChild.refreshPositionAndAngles(spawnPos.x, spawnY, spawnPos.z, player.getYaw(), 0.0F);
            serverWorld.spawnEntity(ghostChild);
            playSummonEffects(player, spawnPos);
            player.sendMessage(Text.literal("§a鬼童已召唤"), true);
            data.summon();
            saveGhostChildData(player, data);
         }
      } else {
         LOGGER.debug(
            "召唤条件不满足: isSummoned={}, world={}, isClient={}",
            data.isSummoned(),
            player.getWorld(),
            player.getWorld() != null ? player.getWorld().isClient : "null"
         );
      }
   }

   public static void recallGhostChild(PlayerEntity player) {
      GhostChildData data = getGhostChildData(player);
      if (data.isSummoned() && player.getWorld() instanceof ServerWorld serverWorld) {
         for (GhostChildEntity ghostChild : serverWorld.getEntitiesByClass(
            GhostChildEntity.class,
            new Box(-3.0E7, -64.0, -3.0E7, 3.0E7, 320.0, 3.0E7),
            entity -> entity.getOwner() != null && entity.getOwner().getUuid().equals(player.getUuid())
         )) {
            if (ghostChild.hasCoffinNail()) {
               ItemStack coffinNail = ghostChild.getCoffinNail();
               if (!coffinNail.isEmpty()) {
                  ItemEntity itemEntity = new ItemEntity(ghostChild.getWorld(), ghostChild.getX(), ghostChild.getY(), ghostChild.getZ(), coffinNail);
                  ghostChild.getWorld().spawnEntity(itemEntity);
                  ghostChild.setCoffinNail(ItemStack.EMPTY);
               }
            }

            playRecallEffects(player, ghostChild);
            ghostChild.remove(RemovalReason.DISCARDED);
         }

         data.recall();
         saveGhostChildData(player, data);
         player.sendMessage(Text.literal("§a鬼童已收回"), true);
      } else {
         LOGGER.debug(
            "收回条件不满足: isSummoned={}, world={}, isClient={}",
            data.isSummoned(),
            player.getWorld(),
            player.getWorld() != null ? player.getWorld().isClient : "null"
         );
      }
   }

   public static boolean hasGhostChild(PlayerEntity player) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      return playerData.contains("GhostChildData");
   }

   public static void initializeGhostChild(PlayerEntity player) {
      if (!hasGhostChild(player)) {
         GhostChildData data = new GhostChildData();
         saveGhostChildData(player, data);
      }
   }

   private static void playSummonEffects(PlayerEntity player, Vec3d spawnPos) {
      player.playSound(SoundEvents.ENTITY_EVOKER_CAST_SPELL, 1.0F, 0.8F);
      if (player.getWorld() instanceof ServerWorld serverWorld) {
         for (int i = 0; i < 15; i++) {
            double offsetX = (serverWorld.random.nextDouble() - 0.5) * 2.5;
            double offsetY = serverWorld.random.nextDouble() * 1.5;
            double offsetZ = (serverWorld.random.nextDouble() - 0.5) * 2.5;
            serverWorld.spawnParticles(ParticleTypes.SOUL, spawnPos.x + offsetX, spawnPos.y + offsetY, spawnPos.z + offsetZ, 1, 0.02, 0.02, 0.02, 0.005);
         }
      }
   }

   private static void playRecallEffects(PlayerEntity player, GhostChildEntity ghostChild) {
      player.playSound(SoundEvents.ENTITY_EVOKER_CAST_SPELL, 1.0F, 1.2F);
      if (player.getWorld() instanceof ServerWorld serverWorld) {
         for (int i = 0; i < 30; i++) {
            double offsetX = (serverWorld.random.nextDouble() - 0.5) * 2.0;
            double offsetY = (serverWorld.random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (serverWorld.random.nextDouble() - 0.5) * 2.0;
            serverWorld.spawnParticles(
               ParticleTypes.ASH, ghostChild.getX() + offsetX, ghostChild.getY() + offsetY, ghostChild.getZ() + offsetZ, 6, 0.05, 0.05, 0.05, 0.01
            );
         }

         for (int i = 0; i < 15; i++) {
            double offsetX = (serverWorld.random.nextDouble() - 0.5) * 1.5;
            double offsetY = (serverWorld.random.nextDouble() - 0.5) * 1.5;
            double offsetZ = (serverWorld.random.nextDouble() - 0.5) * 1.5;
            serverWorld.spawnParticles(
               ParticleTypes.SMOKE, ghostChild.getX() + offsetX, ghostChild.getY() + offsetY, ghostChild.getZ() + offsetZ, 1, 0.03, 0.03, 0.03, 0.005
            );
         }
      }
   }
}
