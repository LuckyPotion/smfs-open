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
      if (entity != null && entity.method_5805()) {
         FORCED_KILL_QUEUE.put(entity.method_5667(), System.currentTimeMillis());
      }
   }

   public static boolean isMarkedForInstantKill(LivingEntity entity) {
      if (entity == null) {
         return false;
      }

      Long timestamp = FORCED_KILL_QUEUE.get(entity.method_5667());
      if (timestamp != null) {
         if (System.currentTimeMillis() - timestamp < 5000L) {
            return true;
         }

         FORCED_KILL_QUEUE.remove(entity.method_5667());
      }

      return false;
   }

   public static void clearKillMark(LivingEntity entity) {
      if (entity != null) {
         FORCED_KILL_QUEUE.remove(entity.method_5667());
      }
   }

   public static boolean executeMultiLevelInstantKill(LivingEntity entity, DamageSource source) {
      if (entity == null || !entity.method_5805()) {
         return false;
      } else if (tryLevel1Kill(entity, source)) {
         return true;
      } else {
         return tryLevel2Kill(entity) ? true : tryLevel3Kill(entity, source);
      }
   }

   private static boolean tryLevel1Kill(LivingEntity entity, DamageSource source) {
      try {
         float lethalDamage = entity.method_6032() * 2.0F + 100.0F;
         if (source != null) {
            entity.method_5643(source, lethalDamage);
         } else {
            entity.method_5643(entity.method_48923().method_48830(), lethalDamage);
         }

         if (!entity.method_5805()) {
            return true;
         }
      } catch (Exception var3) {
      }

      return false;
   }

   private static boolean tryLevel2Kill(LivingEntity entity) {
      try {
         entity.method_6033(0.0F);
         if (!entity.method_5805()) {
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
            entity.method_5643(source, 1.0F);
         } else {
            entity.method_5643(entity.method_48923().method_48831(), 1.0F);
         }

         try {
            Thread.sleep(1L);
         } catch (InterruptedException var3) {
         }

         if (!entity.method_5805()) {
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
      if (player != null && player.method_5805()) {
         DamageSource scapegoatSource = source != null ? source : ModDamageSources.ghost(player.method_37908());
         if (ScapegoatGhostItem.handleScapegoatPassiveSkill(player, scapegoatSource, player.method_6032())) {
            return false;
         }

         markForInstantKill(player);
         player.method_6033(0.0F);

         try {
            if (source != null) {
               player.method_6078(source);
            } else {
               player.method_6078(player.method_48923().method_48831());
            }
         } catch (Exception var4) {
         }

         if (source != null) {
            player.method_5643(source, Float.MAX_VALUE);
         } else {
            player.method_5643(player.method_48923().method_48829(), Float.MAX_VALUE);
         }

         clearKillMark(player);
         return !player.method_5805();
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
      if (player == null || !player.method_5805()) {
         return false;
      } else if (hasMusicBoxCurse(player)) {
         return false;
      } else {
         GameMode gameMode = player.field_13974.method_14257();
         if (skipCreative && gameMode == GameMode.field_9220) {
            return false;
         } else {
            return skipSpectator && gameMode == GameMode.field_9219 ? false : executePlayerInstantKill(player, source);
         }
      }
   }

   private static boolean hasMusicBoxCurse(ServerPlayerEntity player) {
      return player.method_6059(ModEffects.MUSIC_BOX_CURSE);
   }

   public static boolean executeInstantKillIgnoreMusicBox(ServerPlayerEntity player) {
      return executeInstantKillIgnoreMusicBox(player, null);
   }

   public static boolean executeInstantKillIgnoreMusicBox(ServerPlayerEntity player, DamageSource source) {
      if (player != null && player.method_5805()) {
         GameMode gameMode = player.field_13974.method_14257();
         return gameMode == GameMode.field_9219 ? false : executePlayerInstantKill(player, source);
      } else {
         return false;
      }
   }
}
