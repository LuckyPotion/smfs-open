package com.xie.smfs.network.packets.ghostchild.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.data.GhostChildData;
import com.xie.smfs.data.PlayerGhostChildManager;
import com.xie.smfs.effect.SpiritAttributes;
import com.xie.smfs.item.EndingSupernaturalSecretItem;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.manager.GoldBlockProtectionManager;
import com.xie.smfs.network.packets.ghostchild.s2c.GhostChildFusionBeginS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostChildFusionC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_child_fusion");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostChildFusionC2SPacket");

   public static void handleServer(ServerPlayerEntity player, PacketByteBuf buf) {
      if (!GoldBlockProtectionManager.isPlayerInGoldBlockShelter(player)) {
         player.method_7353(Text.method_43470("§c需要在金块包裹状态下才能进行融合！"), false);
      } else {
         int tamedCount = PlayerEvents.countOccupiedGhostSlots(player);
         if (tamedCount < 6) {
            player.method_7353(Text.method_43470("§c需要驾驭6只鬼才能进行融合！当前驾驭：" + tamedCount + "只"), false);
         } else {
            GhostChildData data = PlayerGhostChildManager.getGhostChildData(player);
            if (data == null) {
               player.method_7353(Text.method_43470("§c你没有鬼童！"), false);
            } else if (data.getLevel() < 10) {
               player.method_7353(Text.method_43470("§c鬼童需要10级才能进行融合！当前等级：" + data.getLevel()), false);
            } else if (data.isSummoned()) {
               player.method_7353(Text.method_43470("§c鬼童需要处于收回状态才能进行融合！"), false);
            } else if (AdvancementManager.hasHumanSkinPaper(player)) {
               AdvancementManager.checkHumanSkinPaperEnding(player);
            } else if (!EndingSupernaturalSecretItem.hasComprehended(player)) {
               player.method_7353(Text.method_43470("§c似乎还缺失了关键的一环..."), false);
            } else {
               AdvancementManager.checkAndUnlockBecomeGod(player);
               GhostChildFusionBeginS2CPacket.send(player);
            }
         }
      }
   }

   private static void inheritGhostChildAttributes(ServerPlayerEntity player, GhostChildData ghostChildData) {
      int ghostChildMaxSpirit = ghostChildData.getMaxSpiritPower();
      int ghostChildResistance = ghostChildData.getSpiritResistance();
      int ghostChildDamage = ghostChildData.getSpiritDamage();
      PlayerEvents.addSpiritAttribute(player, SpiritAttributes.MAX_SPIRIT, ghostChildMaxSpirit);
      PlayerEvents.addSpiritAttribute(player, SpiritAttributes.SPIRIT_RESISTANCE, ghostChildResistance);
      PlayerEvents.addSpiritAttribute(player, SpiritAttributes.SPIRIT_DAMAGE, ghostChildDamage);
   }

   private static void unlockAdditionalGhostSlots(ServerPlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      NbtCompound ghostSlots = data.method_10562("GhostSlots");

      for (int i = 6; i <= 9; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.method_10545(slotKey)) {
            NbtCompound slotData = ghostSlots.method_10562(slotKey);
            slotData.method_10556("unlocked", true);
            ghostSlots.method_10566(slotKey, slotData);
         }
      }

      data.method_10566("GhostSlots", ghostSlots);
      PlayerEvents.setSpiritAttributes(player, data);
   }

   public static void sendToServer() {
      PacketByteBuf buf = PacketByteBufs.create();
      ClientPlayNetworking.send(ID, buf);
   }
}
