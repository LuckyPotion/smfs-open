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
      ServerEntityEvents.ENTITY_LOAD.register((Load)(entity, world) -> {
         if (ModConfig.getInstance().enableGhostWatchdog) {
            if (entity instanceof GhostEntity && !world.isClient()) {
               activeGhosts.add(entity.getUuid());
               ghostPositions.put(entity.getUuid(), new GhostDeathHandler.GhostPosition(entity.getX(), entity.getY(), entity.getZ()));
            }
         }
      });
      ServerEntityEvents.ENTITY_UNLOAD.register((Unload)(entity, world) -> {
         if (ModConfig.getInstance().enableGhostWatchdog) {
            if (entity instanceof GhostEntity ghost && !world.isClient()) {
               handleGhostUnload(ghost);
            }
         }
      });
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         if (!world.isClient()) {
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
      GhostDeathHandler.LockedPlayersState lockedPlayersState = getLockedPlayersState(player.getWorld());
      return lockedPlayersState != null && lockedPlayersState.isPlayerLocked(player.getUuid());
   }

   private static boolean handleGhostDeath(GhostEntity ghost, DamageSource source, float damageAmount) {
      if (source.getAttacker() instanceof PlayerEntity player) {
         player.sendMessage(Text.translatable("entity.smfs.ghost.immortal_warning"), true);
         handleIllegalKillPunishment(player, ghost, source);
      }

      return false;
   }

   public static void markLegitimateRemoval(GhostEntity ghost) {
      pendingLegitimateRemovals.add(ghost.getUuid());
   }

   private static void handleGhostUnload(GhostEntity ghost) {
      UUID uuid = ghost.getUuid();
      activeGhosts.remove(uuid);
      ghostPositions.remove(uuid);
      if (!pendingLegitimateRemovals.remove(uuid)) {
         PlayerEntity nearestPlayer = findNearestPlayer(ghost);
         if (nearestPlayer != null) {
            nearestPlayer.sendMessage(Text.translatable("entity.smfs.ghost.immortal_warning"), true);
            handleIllegalKillPunishment(nearestPlayer, ghost, ModDamageSources.ghost(ghost.getWorld()));
         }
      }
   }

   private static PlayerEntity findNearestPlayer(GhostEntity ghost) {
      if (ghost.getWorld() instanceof ServerWorld serverWorld) {
         List<ServerPlayerEntity> nearbyPlayers = serverWorld.getPlayers(player -> player.squaredDistanceTo(ghost) <= 2500.0);
         return nearbyPlayers.isEmpty()
            ? null
            : (PlayerEntity)nearbyPlayers.stream()
               .min((p1, p2) -> Float.compare((float)p1.squaredDistanceTo(ghost), (float)p2.squaredDistanceTo(ghost)))
               .orElse(null);
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
         if (world.getEntity(uuid) instanceof GhostEntity ghost && ghost.isAlive() && !ghost.isRemoved()) {
            pos.x = ghost.getX();
            pos.y = ghost.getY();
            pos.z = ghost.getZ();
         } else if (pendingLegitimateRemovals.remove(uuid)) {
            it.remove();
            activeGhosts.remove(uuid);
         } else {
            boolean playerNearby = false;
            Iterator nearestPlayer = world.getPlayers().iterator();

            while (true) {
               if (nearestPlayer.hasNext()) {
                  ServerPlayerEntity player = (ServerPlayerEntity)nearestPlayer.next();
                  double dx = player.getX() - pos.x;
                  double dy = player.getY() - pos.y;
                  double dz = player.getZ() - pos.z;
                  if (!(dx * dx + dy * dy + dz * dz <= 100.0)) {
                     continue;
                  }

                  playerNearby = true;
               }

               if (playerNearby) {
                  ServerPlayerEntity nearestPlayerx = world.getPlayers().stream().min((p1, p2) -> {
                     double d1 = p1.squaredDistanceTo(pos.x, pos.y, pos.z);
                     double d2 = p2.squaredDistanceTo(pos.x, pos.y, pos.z);
                     return Double.compare(d1, d2);
                  }).orElse(null);
                  if (nearestPlayerx != null) {
                     nearestPlayerx.sendMessage(Text.translatable("entity.smfs.ghost.immortal_warning"), true);
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
      UUID playerId = player.getUuid();
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
            player.sendMessage(Text.translatable("entity.smfs.ghost.immortal_warning"), true);
            damagePlayerEquipment(player);
            break;
         case 1:
            player.sendMessage(Text.translatable("entity.smfs.ghost.immortal_warning"), true);
            killPlayer(player);
            break;
         default:
            player.sendMessage(Text.translatable("entity.smfs.ghost.immortal_warning"), true);
            lockPlayerSave(player);
      }

      record.illegalKillAttempts = 0;
   }

   private static void damagePlayerEquipment(PlayerEntity player) {
      if (player.getMainHandStack() != null && !player.getMainHandStack().isEmpty()) {
         player.getMainHandStack().damage(50, player.getRandom(), null);
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 1200, 4, false, true, true));
      }
   }

   private static void killPlayer(PlayerEntity player) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         DamageSource ghostPunishmentDamage = ModDamageSources.ghostPorcelainCurse(serverPlayer.getWorld());
         InstantKillUtil.executePlayerSelfKill(serverPlayer, ghostPunishmentDamage, false, true);
      }
   }

   private static void lockPlayerSave(PlayerEntity player) {
      GhostDeathHandler.LockedPlayersState lockedPlayersState = getLockedPlayersState(player.getWorld());
      if (lockedPlayersState != null) {
         lockedPlayersState.addLockedPlayer(player.getUuid());
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.networkHandler.disconnect(Text.translatable("message.smfs.ghost.world_corrupted"));
      }

      System.out.println("玩家 " + player.getGameProfile().getName() + " 的存档已被永久锁定");
   }

   private static GhostDeathHandler.LockedPlayersState getLockedPlayersState(World world) {
      if (world instanceof ServerWorld serverWorld) {
         PersistentStateManager stateManager = serverWorld.getPersistentStateManager();

         try {
            Function<NbtCompound, GhostDeathHandler.LockedPlayersState> fromNbt = GhostDeathHandler.LockedPlayersState::fromNbt;
            Supplier<GhostDeathHandler.LockedPlayersState> supplier = GhostDeathHandler.LockedPlayersState::new;
            return (GhostDeathHandler.LockedPlayersState)stateManager.getOrCreate(fromNbt, supplier, "smfs_locked_players");
         } catch (Exception e) {
            System.err.println("获取永久锁定玩家状态失败: " + e.getMessage());
            return null;
         }
      } else {
         return null;
      }
   }

   public static void unlockPlayer(PlayerEntity player) {
      GhostDeathHandler.LockedPlayersState lockedPlayersState = getLockedPlayersState(player.getWorld());
      if (lockedPlayersState != null) {
         lockedPlayersState.removeLockedPlayer(player.getUuid());
      }
   }

   private static boolean handleGhostDamageInterception(GhostEntity ghost, DamageSource source, float damageAmount) {
      float HIGH_DAMAGE_THRESHOLD = 100000.0F;
      if (damageAmount > 100000.0F) {
         if (source.getAttacker() instanceof PlayerEntity player) {
            player.sendMessage(Text.translatable("entity.smfs.ghost.immortal_warning"), true);
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
         if (nbt.contains("locked_players")) {
            NbtList lockedList = nbt.getList("locked_players", 8);

            for (int i = 0; i < lockedList.size(); i++) {
               try {
                  UUID playerId = UUID.fromString(lockedList.getString(i));
                  this.lockedPlayers.add(playerId);
               } catch (IllegalArgumentException e) {
                  System.err.println("无效的玩家UUID: " + lockedList.getString(i));
               }
            }
         }
      }

      public void addLockedPlayer(UUID playerId) {
         this.lockedPlayers.add(playerId);
         this.markDirty();
      }

      public void removeLockedPlayer(UUID playerId) {
         this.lockedPlayers.remove(playerId);
         this.markDirty();
      }

      public boolean isPlayerLocked(UUID playerId) {
         return this.lockedPlayers.contains(playerId);
      }

      public Set<UUID> getLockedPlayers() {
         return new HashSet<>(this.lockedPlayers);
      }

      public NbtCompound writeNbt(NbtCompound nbt) {
         NbtList lockedList = new NbtList();

         for (UUID playerId : this.lockedPlayers) {
            lockedList.add(NbtString.of(playerId.toString()));
         }

         nbt.put("locked_players", lockedList);
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
