package com.xie.smfs.util;

import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.item.ScapegoatGhostItem;
import com.xie.smfs.registry.ModEffects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public class InstantKillUtil {
   private static final ConcurrentHashMap<UUID, Long> FORCED_KILL_QUEUE = new ConcurrentHashMap<>();
   private static final long KILL_MARK_EXPIRE_TIME = 5000L;

   public static void markForInstantKill(LivingEntity entity) {
      if (entity != null && entity.isAlive()) {
         FORCED_KILL_QUEUE.put(entity.getUuid(), System.currentTimeMillis());
      }
   }

   public static boolean isMarkedForInstantKill(LivingEntity entity) {
      if (entity == null) {
         return false;
      }

      Long timestamp = FORCED_KILL_QUEUE.get(entity.getUuid());
      if (timestamp != null) {
         if (System.currentTimeMillis() - timestamp < 5000L) {
            return true;
         }

         FORCED_KILL_QUEUE.remove(entity.getUuid());
      }

      return false;
   }

   public static void clearKillMark(LivingEntity entity) {
      if (entity != null) {
         FORCED_KILL_QUEUE.remove(entity.getUuid());
      }
   }

   public static boolean executeMultiLevelInstantKill(LivingEntity entity, DamageSource source) {
      if (entity == null || !entity.isAlive()) {
         return false;
      } else if (tryLevel1Kill(entity, source)) {
         return true;
      } else {
         return tryLevel2Kill(entity) ? true : tryLevel3Kill(entity, source);
      }
   }

   private static boolean tryLevel1Kill(LivingEntity entity, DamageSource source) {
      try {
         float lethalDamage = entity.getHealth() * 2.0F + 100.0F;
         if (source != null) {
            entity.damage(source, lethalDamage);
         } else {
            entity.damage(entity.getDamageSources().generic(), lethalDamage);
         }

         if (!entity.isAlive()) {
            return true;
         }
      } catch (Exception var3) {
      }

      return false;
   }

   private static boolean tryLevel2Kill(LivingEntity entity) {
      try {
         entity.setHealth(0.0F);
         if (!entity.isAlive()) {
            return true;
         }
      } catch (Exception var2) {
      }

      return false;
   }

   private static boolean tryLevel3Kill(LivingEntity entity, DamageSource source) {
      try {
         markForInstantKill(entity);
         if (source != null) {
            entity.damage(source, 1.0F);
         } else {
            entity.damage(entity.getDamageSources().magic(), 1.0F);
         }

         try {
            Thread.sleep(1L);
         } catch (InterruptedException var3) {
         }

         if (!entity.isAlive()) {
            clearKillMark(entity);
            return true;
         }
      } catch (Exception var4) {
      }

      clearKillMark(entity);
      return false;
   }

   public static boolean executePlayerInstantKill(ServerPlayerEntity player) {
      return executePlayerInstantKill(player, null);
   }

   public static boolean executePlayerInstantKill(ServerPlayerEntity player, DamageSource source) {
      if (player != null && player.isAlive()) {
         DamageSource scapegoatSource = source != null ? source : ModDamageSources.ghost(player.getWorld());
         if (ScapegoatGhostItem.handleScapegoatPassiveSkill(player, scapegoatSource, player.getHealth())) {
            return false;
         }

         markForInstantKill(player);
         player.setHealth(0.0F);

         try {
            if (source != null) {
               player.onDeath(source);
            } else {
               player.onDeath(player.getDamageSources().magic());
            }
         } catch (Exception var4) {
         }

         if (source != null) {
            player.damage(source, Float.MAX_VALUE);
         } else {
            player.damage(player.getDamageSources().outOfWorld(), Float.MAX_VALUE);
         }

         clearKillMark(player);
         return !player.isAlive();
      } else {
         return false;
      }
   }

   public static boolean executePlayerSelfKill(ServerPlayerEntity player) {
      return executePlayerSelfKill(player, null, false, true);
   }

   public static boolean executePlayerSelfKill(ServerPlayerEntity player, DamageSource source) {
      return executePlayerSelfKill(player, source, false, true);
   }

   public static boolean executePlayerSelfKill(ServerPlayerEntity player, DamageSource source, boolean skipCreative) {
      return executePlayerSelfKill(player, source, skipCreative, true);
   }

   public static boolean executePlayerSelfKill(ServerPlayerEntity player, DamageSource source, boolean skipCreative, boolean skipSpectator) {
      if (player == null || !player.isAlive()) {
         return false;
      } else if (hasMusicBoxCurse(player)) {
         return false;
      } else {
         GameMode gameMode = player.interactionManager.getGameMode();
         if (skipCreative && gameMode == GameMode.CREATIVE) {
            return false;
         } else {
            return skipSpectator && gameMode == GameMode.SPECTATOR ? false : executePlayerInstantKill(player, source);
         }
      }
   }

   private static boolean hasMusicBoxCurse(ServerPlayerEntity player) {
      return player.hasStatusEffect(ModEffects.MUSIC_BOX_CURSE);
   }

   public static boolean executeInstantKillIgnoreMusicBox(ServerPlayerEntity player) {
      return executeInstantKillIgnoreMusicBox(player, null);
   }

   public static boolean executeInstantKillIgnoreMusicBox(ServerPlayerEntity player, DamageSource source) {
      if (player != null && player.isAlive()) {
         GameMode gameMode = player.interactionManager.getGameMode();
         return gameMode == GameMode.SPECTATOR ? false : executePlayerInstantKill(player, source);
      } else {
         return false;
      }
   }
}
