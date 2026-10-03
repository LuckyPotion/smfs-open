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
      return PLAYER_FACTION_DATA.computeIfAbsent(player.method_5667(), k -> {
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
         player.method_7353(Text.method_43470("§c声望不足，需要10000声望才能加入" + faction.getDisplayName() + "。").method_27692(Formatting.field_1061), false);
         return false;
      }

      FactionManager.FactionData data = getFactionData(player);
      if (data.faction == faction) {
         player.method_7353(Text.method_43470("§e你已经加入了" + faction.getDisplayName() + "。").method_27692(Formatting.field_1054), false);
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
         player.method_7353(Text.method_43470("§a你已成功加入" + faction.getDisplayName() + "！").method_27692(Formatting.field_1060), false);
         return true;
      } else {
         player.method_7353(Text.method_43470("§c你已经加入了" + data.faction.getDisplayName() + "，无法再加入其他阵营。").method_27692(Formatting.field_1061), false);
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
      if (!player.method_37908().field_9236) {
         FactionManager.FactionData data = getFactionData(player);
         NbtCompound cache = PlayerEvents.PLAYER_DATA_CACHE.get(player.method_5667());
         if (cache == null) {
            cache = new NbtCompound();
         }

         cache.method_10582("faction", data.faction.name());
         cache.method_10569("reputation", data.reputation);
         cache.method_10569("goldSalary", data.goldSalary);
         cache.method_10582("codename", data.codename);
         PlayerEvents.PLAYER_DATA_CACHE.put(player.method_5667(), cache);
         if (player instanceof ServerPlayerEntity) {
            PlayerEvents.saveDataToPlayer(player, cache);
         }
      }
   }

   private static void loadFromCache(PlayerEntity player, FactionManager.FactionData data) {
      NbtCompound cache = PlayerEvents.PLAYER_DATA_CACHE.get(player.method_5667());
      if (cache != null) {
         if (cache.method_10545("faction")) {
            data.faction = PlayerFaction.fromString(cache.method_10558("faction"));
         }

         if (cache.method_10545("reputation")) {
            data.reputation = cache.method_10550("reputation");
         }

         if (cache.method_10545("goldSalary")) {
            data.goldSalary = cache.method_10550("goldSalary");
         }

         if (cache.method_10545("codename")) {
            data.codename = cache.method_10558("codename");
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
         nbt.method_10582("faction", this.faction.name());
         nbt.method_10569("reputation", this.reputation);
         nbt.method_10569("goldSalary", this.goldSalary);
         nbt.method_10582("codename", this.codename);
         return nbt;
      }

      public static FactionManager.FactionData fromNbt(NbtCompound nbt) {
         FactionManager.FactionData data = new FactionManager.FactionData();
         if (nbt.method_10545("faction")) {
            data.faction = PlayerFaction.fromString(nbt.method_10558("faction"));
         }

         if (nbt.method_10545("reputation")) {
            data.reputation = nbt.method_10550("reputation");
         }

         if (nbt.method_10545("goldSalary")) {
            data.goldSalary = nbt.method_10550("goldSalary");
         }

         if (nbt.method_10545("codename")) {
            data.codename = nbt.method_10558("codename");
         }

         return data;
      }
   }
}
