package com.xie.smfs.event.screen;

import com.xie.smfs.data.GhostChildData;
import com.xie.smfs.data.PlayerGhostChildManager;
import com.xie.smfs.network.ClientModNetwork;
import com.xie.smfs.network.packets.ghostchild.c2s.GhostChildRecallPacket;
import com.xie.smfs.network.packets.ghostchild.c2s.GhostChildSummonPacket;
import com.xie.smfs.registry.ModScreenHandlers;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GhostChildCultivationScreenHandler extends ScreenHandler {
   private GhostChildData ghostChildData;
   private PlayerEntity player;

   public GhostChildCultivationScreenHandler(int syncId, PlayerInventory inventory) {
      super(ModScreenHandlers.GHOST_CHILD_CULTIVATION_SCREEN_HANDLER, syncId);
      this.player = inventory.field_7546;
      this.ghostChildData = PlayerGhostChildManager.getGhostChildData(this.player);
   }

   public GhostChildCultivationScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
      this(syncId, inventory);
   }

   public GhostChildData getGhostChildData() {
      return this.ghostChildData;
   }

   public PlayerEntity getPlayer() {
      return this.player;
   }

   public boolean method_7597(PlayerEntity player) {
      return true;
   }

   public ItemStack method_7601(PlayerEntity player, int index) {
      return ItemStack.field_8037;
   }

   public void performBreakthrough() {
      if (this.ghostChildData != null && this.ghostChildData.isAtBreakthroughLevel()) {
         int index = this.ghostChildData.getBreakthroughIndex();
         if (index != -1) {
            this.ghostChildData.performBreakthrough(index);
            if (this.player != null) {
               PlayerGhostChildManager.saveGhostChildData(this.player, this.ghostChildData);
            }
         }
      }
   }

   public void summonGhostChild() {
      if (this.player != null) {
         ClientModNetwork.sendToServer(GhostChildSummonPacket.create());
      }
   }

   public void recallGhostChild() {
      if (this.player != null) {
         ClientModNetwork.sendToServer(GhostChildRecallPacket.create());
      }
   }

   public static class GhostChildCultivationFactory implements ExtendedScreenHandlerFactory {
      public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
         return new GhostChildCultivationScreenHandler(syncId, inventory);
      }

      public Text method_5476() {
         return Text.method_43470("Ghost Child Cultivation");
      }

      public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
      }
   }
}
