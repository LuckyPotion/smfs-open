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
      if (!playerData.method_10545("GhostChildData")) {
         return new GhostChildData();
      }

      NbtCompound ghostChildNbt = playerData.method_10562("GhostChildData");
      return new GhostChildData(ghostChildNbt);
   }

   public static void saveGhostChildData(PlayerEntity player, GhostChildData data) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      NbtCompound ghostChildNbt = new NbtCompound();
      data.saveToNbt(ghostChildNbt);
      playerData.method_10566("GhostChildData", ghostChildNbt);
      PlayerEvents.saveDataToPlayer(player, playerData);
   }

   public static void summonGhostChild(PlayerEntity player) {
      GhostChildData data = getGhostChildData(player);
      if (!data.isSummoned() && player.method_37908() instanceof ServerWorld serverWorld) {
         Iterator var11 = serverWorld.method_8390(
               GhostChildEntity.class,
               new Box(-3.0E7, -64.0, -3.0E7, 3.0E7, 320.0, 3.0E7),
               entity -> entity.getOwner() != null && entity.getOwner().method_5667().equals(player.method_5667())
            )
            .iterator();
         if (var11.hasNext()) {
            GhostChildEntity existingGhostChild = (GhostChildEntity)var11.next();
            existingGhostChild.setSummoned(true);
            data.summon();
            saveGhostChildData(player, data);
            Vec3d spawnPos = existingGhostChild.method_19538();
            playSummonEffects(player, spawnPos);
            player.method_7353(Text.method_43470("§a鬼童已召唤"), true);
            return;
         }

         GhostChildEntity ghostChild = (GhostChildEntity)ModEntities.GHOST_CHILD.method_5883(serverWorld);
         if (ghostChild != null) {
            ghostChild.setOwner(player);
            ghostChild.setSummoned(true);
            Vec3d playerPos = player.method_19538();
            Vec3d spawnPos = playerPos.method_1019(player.method_5720().method_1021(3.0));
            int spawnX = (int)Math.floor(spawnPos.field_1352);
            int spawnZ = (int)Math.floor(spawnPos.field_1350);
            int topY = serverWorld.method_8624(Type.field_13197, spawnX, spawnZ);
            double spawnY;
            if (topY > 0) {
               spawnY = Math.max(topY + 0.5, spawnPos.field_1351);
            } else {
               spawnY = playerPos.field_1351;
               spawnPos = playerPos;
            }

            ghostChild.method_5808(spawnPos.field_1352, spawnY, spawnPos.field_1350, player.method_36454(), 0.0F);
            serverWorld.method_8649(ghostChild);
            playSummonEffects(player, spawnPos);
            player.method_7353(Text.method_43470("§a鬼童已召唤"), true);
            data.summon();
            saveGhostChildData(player, data);
         }
      } else {
         LOGGER.debug(
            "召唤条件不满足: isSummoned={}, world={}, isClient={}",
            data.isSummoned(),
            player.method_37908(),
            player.method_37908() != null ? player.method_37908().field_9236 : "null"
         );
      }
   }

   public static void recallGhostChild(PlayerEntity player) {
      GhostChildData data = getGhostChildData(player);
      if (data.isSummoned() && player.method_37908() instanceof ServerWorld serverWorld) {
         for (GhostChildEntity ghostChild : serverWorld.method_8390(
            GhostChildEntity.class,
            new Box(-3.0E7, -64.0, -3.0E7, 3.0E7, 320.0, 3.0E7),
            entity -> entity.getOwner() != null && entity.getOwner().method_5667().equals(player.method_5667())
         )) {
            if (ghostChild.hasCoffinNail()) {
               ItemStack coffinNail = ghostChild.getCoffinNail();
               if (!coffinNail.method_7960()) {
                  ItemEntity itemEntity = new ItemEntity(
                     ghostChild.method_37908(), ghostChild.method_23317(), ghostChild.method_23318(), ghostChild.method_23321(), coffinNail
                  );
                  ghostChild.method_37908().method_8649(itemEntity);
                  ghostChild.setCoffinNail(ItemStack.field_8037);
               }
            }

            playRecallEffects(player, ghostChild);
            ghostChild.method_5650(RemovalReason.field_26999);
         }

         data.recall();
         saveGhostChildData(player, data);
         player.method_7353(Text.method_43470("§a鬼童已收回"), true);
      } else {
         LOGGER.debug(
            "收回条件不满足: isSummoned={}, world={}, isClient={}",
            data.isSummoned(),
            player.method_37908(),
            player.method_37908() != null ? player.method_37908().field_9236 : "null"
         );
      }
   }

   public static boolean hasGhostChild(PlayerEntity player) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      return playerData.method_10545("GhostChildData");
   }

   public static void initializeGhostChild(PlayerEntity player) {
      if (!hasGhostChild(player)) {
         GhostChildData data = new GhostChildData();
         saveGhostChildData(player, data);
      }
   }

   private static void playSummonEffects(PlayerEntity player, Vec3d spawnPos) {
      player.method_5783(SoundEvents.field_14858, 1.0F, 0.8F);
      if (player.method_37908() instanceof ServerWorld serverWorld) {
         for (int i = 0; i < 15; i++) {
            double offsetX = (serverWorld.field_9229.method_43058() - 0.5) * 2.5;
            double offsetY = serverWorld.field_9229.method_43058() * 1.5;
            double offsetZ = (serverWorld.field_9229.method_43058() - 0.5) * 2.5;
            serverWorld.method_14199(
               ParticleTypes.field_23114,
               spawnPos.field_1352 + offsetX,
               spawnPos.field_1351 + offsetY,
               spawnPos.field_1350 + offsetZ,
               1,
               0.02,
               0.02,
               0.02,
               0.005
            );
         }
      }
   }

   private static void playRecallEffects(PlayerEntity player, GhostChildEntity ghostChild) {
      player.method_5783(SoundEvents.field_14858, 1.0F, 1.2F);
      if (player.method_37908() instanceof ServerWorld serverWorld) {
         for (int i = 0; i < 30; i++) {
            double offsetX = (serverWorld.field_9229.method_43058() - 0.5) * 2.0;
            double offsetY = (serverWorld.field_9229.method_43058() - 0.5) * 2.0;
            double offsetZ = (serverWorld.field_9229.method_43058() - 0.5) * 2.0;
            serverWorld.method_14199(
               ParticleTypes.field_22247,
               ghostChild.method_23317() + offsetX,
               ghostChild.method_23318() + offsetY,
               ghostChild.method_23321() + offsetZ,
               6,
               0.05,
               0.05,
               0.05,
               0.01
            );
         }

         for (int i = 0; i < 15; i++) {
            double offsetX = (serverWorld.field_9229.method_43058() - 0.5) * 1.5;
            double offsetY = (serverWorld.field_9229.method_43058() - 0.5) * 1.5;
            double offsetZ = (serverWorld.field_9229.method_43058() - 0.5) * 1.5;
            serverWorld.method_14199(
               ParticleTypes.field_11251,
               ghostChild.method_23317() + offsetX,
               ghostChild.method_23318() + offsetY,
               ghostChild.method_23321() + offsetZ,
               1,
               0.03,
               0.03,
               0.03,
               0.005
            );
         }
      }
   }
}
