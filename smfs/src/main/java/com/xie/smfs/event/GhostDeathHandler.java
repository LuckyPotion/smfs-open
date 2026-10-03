package com.xie.smfs.event;

import com.xie.smfs.config.ModConfig;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.util.InstantKillUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AllowDamage;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AllowDeath;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.Load;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.Unload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndWorldTick;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class GhostDeathHandler {
   private static final Map<UUID, GhostDeathHandler.PlayerPunishmentRecord> playerPunishmentRecords = new HashMap<>();
   private static final int MAX_ILLEGAL_KILL_ATTEMPTS = 3;
   private static final Set<UUID> activeGhosts = new HashSet<>();
   private static final Set<UUID> pendingLegitimateRemovals = new HashSet<>();
   private static final Map<UUID, GhostDeathHandler.GhostPosition> ghostPositions = new HashMap<>();
   private static final int PATROL_INTERVAL = 100;
   private static int patrolCounter = 0;
   private static final double PLAYER_NEARBY_RANGE_SQ = 100.0;
   private static final int DECAY_INTERVAL = 24000;
   private static int decayCounter = 0;

   public static void register() {
      ServerLivingEntityEvents.ALLOW_DEATH
         .register((AllowDeath)(entity, source, damageAmount) -> entity instanceof GhostEntity ghost ? handleGhostDeath(ghost, source, damageAmount) : true);
      ServerLivingEntityEvents.ALLOW_DAMAGE
         .register(
            (AllowDamage)(entity, source, damageAmount) -> entity instanceof GhostEntity ghost
               ? handleGhostDamageInterception(ghost, source, damageAmount)
               : true
         );
      ServerEntityEvents.ENTITY_LOAD
         .register(
            (Load)(entity, world) -> {
               if (ModConfig.getInstance().enableGhostWatchdog) {
                  if (entity instanceof GhostEntity && !world.method_8608()) {
                     activeGhosts.add(entity.method_5667());
                     ghostPositions.put(
                        entity.method_5667(), new GhostDeathHandler.GhostPosition(entity.method_23317(), entity.method_23318(), entity.method_23321())
                     );
                  }
               }
            }
         );
      ServerEntityEvents.ENTITY_UNLOAD.register((Unload)(entity, world) -> {
         if (ModConfig.getInstance().enableGhostWatchdog) {
            if (entity instanceof GhostEntity ghost && !world.method_8608()) {
               handleGhostUnload(ghost);
            }
         }
      });
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         if (!world.method_8608()) {
            decayCounter++;
            if (decayCounter >= 24000) {
               decayCounter = 0;

               for (GhostDeathHandler.PlayerPunishmentRecord r : playerPunishmentRecords.values()) {
                  if (r.illegalKillAttempts > 0) {
                     r.illegalKillAttempts--;
                  }
               }
            }

            if (ModConfig.getInstance().enableGhostPatrol) {
               patrolCounter++;
               if (patrolCounter >= 100) {
                  patrolCounter = 0;
                  patrolGhosts(world);
               }
            }
         }
      });
   }

   public static boolean isPlayerPermanentlyLocked(PlayerEntity player) {
      GhostDeathHandler.LockedPlayersState lockedPlayersState = getLockedPlayersState(player.method_37908());
      return lockedPlayersState != null && lockedPlayersState.isPlayerLocked(player.method_5667());
   }

   private static boolean handleGhostDeath(GhostEntity ghost, DamageSource source, float damageAmount) {
      if (source.method_5529() instanceof PlayerEntity player) {
         player.method_7353(Text.method_43471("entity.smfs.ghost.immortal_warning"), true);
         handleIllegalKillPunishment(player, ghost, source);
      }

      return false;
   }

   public static void markLegitimateRemoval(GhostEntity ghost) {
      pendingLegitimateRemovals.add(ghost.method_5667());
   }

   private static void handleGhostUnload(GhostEntity ghost) {
      UUID uuid = ghost.method_5667();
      activeGhosts.remove(uuid);
      ghostPositions.remove(uuid);
      if (!pendingLegitimateRemovals.remove(uuid)) {
         PlayerEntity nearestPlayer = findNearestPlayer(ghost);
         if (nearestPlayer != null) {
            nearestPlayer.method_7353(Text.method_43471("entity.smfs.ghost.immortal_warning"), true);
            handleIllegalKillPunishment(nearestPlayer, ghost, ModDamageSources.ghost(ghost.method_37908()));
         }
      }
   }

   private static PlayerEntity findNearestPlayer(GhostEntity ghost) {
      if (ghost.method_37908() instanceof ServerWorld serverWorld) {
         List<ServerPlayerEntity> nearbyPlayers = serverWorld.method_18766(player -> player.method_5858(ghost) <= 2500.0);
         return nearbyPlayers.isEmpty()
            ? null
            : (PlayerEntity)nearbyPlayers.stream().min((p1, p2) -> Float.compare((float)p1.method_5858(ghost), (float)p2.method_5858(ghost))).orElse(null);
      } else {
         return null;
      }
   }

   private static void patrolGhosts(ServerWorld world) {
      Iterator<Entry<UUID, GhostDeathHandler.GhostPosition>> it = ghostPositions.entrySet().iterator();

      while (it.hasNext()) {
         Entry<UUID, GhostDeathHandler.GhostPosition> entry = it.next();
         UUID uuid = entry.getKey();
         GhostDeathHandler.GhostPosition pos = entry.getValue();
         if (world.method_14190(uuid) instanceof GhostEntity ghost && ghost.method_5805() && !ghost.method_31481()) {
            pos.x = ghost.method_23317();
            pos.y = ghost.method_23318();
            pos.z = ghost.method_23321();
         } else if (pendingLegitimateRemovals.remove(uuid)) {
            it.remove();
            activeGhosts.remove(uuid);
         } else {
            boolean playerNearby = false;
            Iterator nearestPlayer = world.method_18456().iterator();

            while (true) {
               if (nearestPlayer.hasNext()) {
                  ServerPlayerEntity player = (ServerPlayerEntity)nearestPlayer.next();
                  double dx = player.method_23317() - pos.x;
                  double dy = player.method_23318() - pos.y;
                  double dz = player.method_23321() - pos.z;
                  if (!(dx * dx + dy * dy + dz * dz <= 100.0)) {
                     continue;
                  }

                  playerNearby = true;
               }

               if (playerNearby) {
                  ServerPlayerEntity nearestPlayerx = world.method_18456().stream().min((p1, p2) -> {
                     double d1 = p1.method_5649(pos.x, pos.y, pos.z);
                     double d2 = p2.method_5649(pos.x, pos.y, pos.z);
                     return Double.compare(d1, d2);
                  }).orElse(null);
                  if (nearestPlayerx != null) {
                     nearestPlayerx.method_7353(Text.method_43471("entity.smfs.ghost.immortal_warning"), true);
                     handleIllegalKillPunishment(nearestPlayerx, null, ModDamageSources.ghost(world));
                  }
               }

               it.remove();
               activeGhosts.remove(uuid);
               break;
            }
         }
      }
   }

   public static void handleIllegalKillPunishment(PlayerEntity player, GhostEntity ghost, DamageSource source) {
      UUID playerId = player.method_5667();
      GhostDeathHandler.PlayerPunishmentRecord record = playerPunishmentRecords.computeIfAbsent(playerId, k -> new GhostDeathHandler.PlayerPunishmentRecord());
      record.incrementAttempts();
      if (record.shouldPunish()) {
         executePunishment(player, ghost, record);
         record.punishmentLevel++;
      }
   }

   private static void executePunishment(PlayerEntity player, GhostEntity ghost, GhostDeathHandler.PlayerPunishmentRecord record) {
      switch (record.punishmentLevel) {
         case 0:
            player.method_7353(Text.method_43471("entity.smfs.ghost.immortal_warning"), true);
            damagePlayerEquipment(player);
            break;
         case 1:
            player.method_7353(Text.method_43471("entity.smfs.ghost.immortal_warning"), true);
            killPlayer(player);
            break;
         default:
            player.method_7353(Text.method_43471("entity.smfs.ghost.immortal_warning"), true);
            lockPlayerSave(player);
      }

      record.illegalKillAttempts = 0;
   }

   private static void damagePlayerEquipment(PlayerEntity player) {
      if (player.method_6047() != null && !player.method_6047().method_7960()) {
         player.method_6047().method_7970(50, player.method_6051(), null);
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.method_6092(new StatusEffectInstance(StatusEffects.field_5899, 1200, 4, false, true, true));
      }
   }

   private static void killPlayer(PlayerEntity player) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         DamageSource ghostPunishmentDamage = ModDamageSources.ghostPorcelainCurse(serverPlayer.method_37908());
         InstantKillUtil.executePlayerSelfKill(serverPlayer, ghostPunishmentDamage, false, true);
      }
   }

   private static void lockPlayerSave(PlayerEntity player) {
      GhostDeathHandler.LockedPlayersState lockedPlayersState = getLockedPlayersState(player.method_37908());
      if (lockedPlayersState != null) {
         lockedPlayersState.addLockedPlayer(player.method_5667());
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.field_13987.method_14367(Text.method_43471("message.smfs.ghost.world_corrupted"));
      }

      System.out.println("玩家 " + player.method_7334().getName() + " 的存档已被永久锁定");
   }

   private static GhostDeathHandler.LockedPlayersState getLockedPlayersState(World world) {
      if (world instanceof ServerWorld serverWorld) {
         PersistentStateManager stateManager = serverWorld.method_17983();

         try {
            Function<NbtCompound, GhostDeathHandler.LockedPlayersState> fromNbt = GhostDeathHandler.LockedPlayersState::fromNbt;
            Supplier<GhostDeathHandler.LockedPlayersState> supplier = GhostDeathHandler.LockedPlayersState::new;
            return (GhostDeathHandler.LockedPlayersState)stateManager.method_17924(fromNbt, supplier, "smfs_locked_players");
         } catch (Exception e) {
            System.err.println("获取永久锁定玩家状态失败: " + e.getMessage());
            return null;
         }
      } else {
         return null;
      }
   }

   public static void unlockPlayer(PlayerEntity player) {
      GhostDeathHandler.LockedPlayersState lockedPlayersState = getLockedPlayersState(player.method_37908());
      if (lockedPlayersState != null) {
         lockedPlayersState.removeLockedPlayer(player.method_5667());
      }
   }

   private static boolean handleGhostDamageInterception(GhostEntity ghost, DamageSource source, float damageAmount) {
      float HIGH_DAMAGE_THRESHOLD = 100000.0F;
      if (damageAmount > 100000.0F) {
         if (source.method_5529() instanceof PlayerEntity player) {
            player.method_7353(Text.method_43471("entity.smfs.ghost.immortal_warning"), true);
            handleIllegalKillPunishment(player, ghost, source);
         }

         return false;
      } else {
         return true;
      }
   }

   private static class GhostPosition {
      double x;
      double y;
      double z;

      GhostPosition(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }
   }

   public static class LockedPlayersState extends PersistentState {
      private final Set<UUID> lockedPlayers = new HashSet<>();

      public LockedPlayersState() {
      }

      public LockedPlayersState(NbtCompound nbt) {
         if (nbt.method_10545("locked_players")) {
            NbtList lockedList = nbt.method_10554("locked_players", 8);

            for (int i = 0; i < lockedList.size(); i++) {
               try {
                  UUID playerId = UUID.fromString(lockedList.method_10608(i));
                  this.lockedPlayers.add(playerId);
               } catch (IllegalArgumentException e) {
                  System.err.println("无效的玩家UUID: " + lockedList.method_10608(i));
               }
            }
         }
      }

      public void addLockedPlayer(UUID playerId) {
         this.lockedPlayers.add(playerId);
         this.method_80();
      }

      public void removeLockedPlayer(UUID playerId) {
         this.lockedPlayers.remove(playerId);
         this.method_80();
      }

      public boolean isPlayerLocked(UUID playerId) {
         return this.lockedPlayers.contains(playerId);
      }

      public Set<UUID> getLockedPlayers() {
         return new HashSet<>(this.lockedPlayers);
      }

      public NbtCompound method_75(NbtCompound nbt) {
         NbtList lockedList = new NbtList();

         for (UUID playerId : this.lockedPlayers) {
            lockedList.add(NbtString.method_23256(playerId.toString()));
         }

         nbt.method_10566("locked_players", lockedList);
         return nbt;
      }

      public static GhostDeathHandler.LockedPlayersState fromNbt(NbtCompound nbt) {
         return new GhostDeathHandler.LockedPlayersState(nbt);
      }
   }

   private static class PlayerPunishmentRecord {
      private int illegalKillAttempts = 0;
      private int punishmentLevel = 0;

      public void incrementAttempts() {
         this.illegalKillAttempts++;
      }

      public boolean shouldPunish() {
         return this.illegalKillAttempts >= 3;
      }

      public void reset() {
         this.illegalKillAttempts = 0;
         this.punishmentLevel = 0;
      }
   }
}
