package com.xie.smfs.faction;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.entity.other.CaoYanHuaEntity;
import com.xie.smfs.entity.other.ChenDoctorEntity;
import com.xie.smfs.entity.other.LiuXiaoYuEntity;
import com.xie.smfs.entity.other.WangXiaoMingEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FactionManager {
   private static final Map<UUID, FactionManager.FactionData> PLAYER_FACTION_DATA = new HashMap<>();
   public static final int REPUTATION_PER_CAPTURE = 100;
   public static final int REQUIRED_REPUTATION = 10000;

   public static FactionManager.FactionData getFactionData(PlayerEntity player) {
      return PLAYER_FACTION_DATA.computeIfAbsent(player.getUuid(), k -> {
         FactionManager.FactionData data = new FactionManager.FactionData();
         loadFromCache(player, data);
         return data;
      });
   }

   public static PlayerFaction getFaction(PlayerEntity player) {
      return getFactionData(player).faction;
   }

   public static void setFaction(PlayerEntity player, PlayerFaction faction) {
      getFactionData(player).faction = faction;
      syncToCache(player);
   }

   public static int getReputation(PlayerEntity player) {
      return getFactionData(player).reputation;
   }

   public static int getGoldSalary(PlayerEntity player) {
      return getFactionData(player).goldSalary;
   }

   public static void setReputation(PlayerEntity player, int reputation) {
      getFactionData(player).reputation = reputation;
      syncToCache(player);
   }

   public static void addReputation(PlayerEntity player, int amount) {
      FactionManager.FactionData data = getFactionData(player);
      data.reputation += amount;
      syncToCache(player);
   }

   public static String getCodename(PlayerEntity player) {
      return getFactionData(player).codename;
   }

   public static void setCodename(PlayerEntity player, String codename) {
      getFactionData(player).codename = codename;
      syncToCache(player);
   }

   public static boolean hasCodename(PlayerEntity player) {
      String codename = getFactionData(player).codename;
      return codename != null && !codename.isEmpty();
   }

   public static boolean canJoinFaction(PlayerEntity player) {
      return getReputation(player) >= 10000;
   }

   public static PlayerFaction getFactionForEntity(Entity entity) {
      if (entity instanceof GhostMasterEntity) {
         return ((GhostMasterEntity)entity).getFaction();
      } else if (entity instanceof WangXiaoMingEntity) {
         return ((WangXiaoMingEntity)entity).getFaction();
      } else if (entity instanceof ChenDoctorEntity) {
         return ((ChenDoctorEntity)entity).getFaction();
      } else if (entity instanceof LiuXiaoYuEntity) {
         return ((LiuXiaoYuEntity)entity).getFaction();
      } else {
         return entity instanceof CaoYanHuaEntity ? ((CaoYanHuaEntity)entity).getFaction() : null;
      }
   }

   public static String getFactionDisplayName(Entity entity) {
      PlayerFaction faction = getFactionForEntity(entity);
      return faction != null ? faction.getDisplayName() : "";
   }

   public static boolean tryJoinFaction(ServerPlayerEntity player, PlayerFaction faction) {
      if (!canJoinFaction(player)) {
         player.sendMessage(Text.literal("§c声望不足，需要10000声望才能加入" + faction.getDisplayName() + "。").formatted(Formatting.RED), false);
         return false;
      }

      FactionManager.FactionData data = getFactionData(player);
      if (data.faction == faction) {
         player.sendMessage(Text.literal("§e你已经加入了" + faction.getDisplayName() + "。").formatted(Formatting.YELLOW), false);
         return false;
      }

      if (data.faction != PlayerFaction.HEADQUARTERS && data.faction != PlayerFaction.SPIRIT_FORUM && data.faction != PlayerFaction.PENGYOU_QUAN) {
         data.faction = faction;
         if (faction == PlayerFaction.HEADQUARTERS) {
            data.goldSalary = 27;
         } else if (faction == PlayerFaction.SPIRIT_FORUM) {
            data.goldSalary = 45;
         } else if (faction == PlayerFaction.PENGYOU_QUAN) {
            data.goldSalary = 25;
         }

         syncToCache(player);
         player.sendMessage(Text.literal("§a你已成功加入" + faction.getDisplayName() + "！").formatted(Formatting.GREEN), false);
         return true;
      } else {
         player.sendMessage(Text.literal("§c你已经加入了" + data.faction.getDisplayName() + "，无法再加入其他阵营。").formatted(Formatting.RED), false);
         return false;
      }
   }

   public static void onPlayerTamedFirstGhost(PlayerEntity player) {
      FactionManager.FactionData data = getFactionData(player);
      if (data.faction == PlayerFaction.ORDINARY) {
         data.faction = PlayerFaction.FOLK_GHOST_MASTER;
         syncToCache(player);
      }
   }

   private static void syncToCache(PlayerEntity player) {
      if (!player.getWorld().isClient) {
         FactionManager.FactionData data = getFactionData(player);
         NbtCompound cache = PlayerEvents.PLAYER_DATA_CACHE.get(player.getUuid());
         if (cache == null) {
            cache = new NbtCompound();
         }

         cache.putString("faction", data.faction.name());
         cache.putInt("reputation", data.reputation);
         cache.putInt("goldSalary", data.goldSalary);
         cache.putString("codename", data.codename);
         PlayerEvents.PLAYER_DATA_CACHE.put(player.getUuid(), cache);
         if (player instanceof ServerPlayerEntity) {
            PlayerEvents.saveDataToPlayer(player, cache);
         }
      }
   }

   private static void loadFromCache(PlayerEntity player, FactionManager.FactionData data) {
      NbtCompound cache = PlayerEvents.PLAYER_DATA_CACHE.get(player.getUuid());
      if (cache != null) {
         if (cache.contains("faction")) {
            data.faction = PlayerFaction.fromString(cache.getString("faction"));
         }

         if (cache.contains("reputation")) {
            data.reputation = cache.getInt("reputation");
         }

         if (cache.contains("goldSalary")) {
            data.goldSalary = cache.getInt("goldSalary");
         }

         if (cache.contains("codename")) {
            data.codename = cache.getString("codename");
         }
      }
   }

   public static class FactionData {
      public PlayerFaction faction = PlayerFaction.ORDINARY;
      public int reputation = 0;
      public int goldSalary = 0;
      public String codename = "";

      public NbtCompound toNbt() {
         NbtCompound nbt = new NbtCompound();
         nbt.putString("faction", this.faction.name());
         nbt.putInt("reputation", this.reputation);
         nbt.putInt("goldSalary", this.goldSalary);
         nbt.putString("codename", this.codename);
         return nbt;
      }

      public static FactionManager.FactionData fromNbt(NbtCompound nbt) {
         FactionManager.FactionData data = new FactionManager.FactionData();
         if (nbt.contains("faction")) {
            data.faction = PlayerFaction.fromString(nbt.getString("faction"));
         }

         if (nbt.contains("reputation")) {
            data.reputation = nbt.getInt("reputation");
         }

         if (nbt.contains("goldSalary")) {
            data.goldSalary = nbt.getInt("goldSalary");
         }

         if (nbt.contains("codename")) {
            data.codename = nbt.getString("codename");
         }

         return data;
      }
   }
}
