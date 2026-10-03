package com.xie.smfs.event.screen;

import com.xie.smfs.data.PlayerRoyalCurseManager;
import com.xie.smfs.data.RoyalCurseData;
import com.xie.smfs.data.RoyalCurseServantData;
import com.xie.smfs.registry.ModScreenHandlers;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoyalCurseScreenHandler extends ScreenHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(RoyalCurseScreenHandler.class);
   private RoyalCurseData royalCurseData;
   private PlayerEntity player;
   private int selectedServantIndex = 0;
   private static final int BUTTON_SUMMON = 0;
   private static final int BUTTON_RECALL = 1;
   private static final int BUTTON_SUMMON_ALL = 2;
   private static final int BUTTON_RECALL_ALL = 3;
   private static final int BUTTON_SET_INDEX = 4;

   public RoyalCurseScreenHandler(int syncId, PlayerInventory inventory) {
      super(ModScreenHandlers.ROYAL_CURSE_SCREEN_HANDLER, syncId);
      this.player = inventory.player;
      this.royalCurseData = PlayerRoyalCurseManager.getRoyalCurseData(this.player);
   }

   public RoyalCurseScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
      this(syncId, inventory);
   }

   public RoyalCurseData getRoyalCurseData() {
      return this.royalCurseData;
   }

   public PlayerEntity getPlayer() {
      return this.player;
   }

   public boolean canUse(PlayerEntity player) {
      return true;
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      return ItemStack.EMPTY;
   }

   public boolean onButtonClick(PlayerEntity player, int id) {
      switch (id) {
         case 0:
            LOGGER.debug("收到召唤按钮点击事件，索引: {}", this.selectedServantIndex);
            this.summonServant(this.selectedServantIndex);
            break;
         case 1:
            LOGGER.debug("收到收回按钮点击事件，索引: {}", this.selectedServantIndex);
            this.recallServant(this.selectedServantIndex);
            break;
         case 2:
            LOGGER.debug("收到全部召唤按钮点击事件");
            this.summonAllServants();
            break;
         case 3:
            LOGGER.debug("收到全部收回按钮点击事件");
            this.recallAllServants();
      }

      return true;
   }

   public void summonServant(int servantIndex) {
      if (this.player != null && !this.player.getWorld().isClient) {
         PlayerRoyalCurseManager.summonServant(this.player, servantIndex);
         this.royalCurseData = PlayerRoyalCurseManager.getRoyalCurseData(this.player);
         this.syncDataToClient();
      }
   }

   public void recallServant(int servantIndex) {
      if (this.player != null && !this.player.getWorld().isClient) {
         PlayerRoyalCurseManager.recallServant(this.player, servantIndex);
         this.royalCurseData = PlayerRoyalCurseManager.getRoyalCurseData(this.player);
         this.syncDataToClient();
      }
   }

   public void summonAllServants() {
      if (this.player != null && !this.player.getWorld().isClient) {
         RoyalCurseData data = PlayerRoyalCurseManager.getRoyalCurseData(this.player);
         int servantCount = data.getServantCount();

         for (int i = 0; i < servantCount; i++) {
            RoyalCurseServantData servantData = data.getServant(i);
            if (servantData != null && !servantData.isReleased()) {
               PlayerRoyalCurseManager.summonServant(this.player, i);
            }
         }

         this.royalCurseData = PlayerRoyalCurseManager.getRoyalCurseData(this.player);
         this.syncDataToClient();
      }
   }

   public void recallAllServants() {
      if (this.player != null && !this.player.getWorld().isClient) {
         RoyalCurseData data = PlayerRoyalCurseManager.getRoyalCurseData(this.player);
         int servantCount = data.getServantCount();

         for (int i = 0; i < servantCount; i++) {
            RoyalCurseServantData servantData = data.getServant(i);
            if (servantData != null && servantData.isReleased()) {
               PlayerRoyalCurseManager.recallServant(this.player, i);
            }
         }

         this.royalCurseData = PlayerRoyalCurseManager.getRoyalCurseData(this.player);
         this.syncDataToClient();
      }
   }

   private void syncDataToClient() {
      if (this.player != null && this.player instanceof ServerPlayerEntity serverPlayer) {
         this.royalCurseData = PlayerRoyalCurseManager.getRoyalCurseData(this.player);
      }
   }

   public static class RoyalCurseFactory implements ExtendedScreenHandlerFactory {
      public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
         return new RoyalCurseScreenHandler(syncId, inventory);
      }

      public Text method_5476() {
         return Text.literal("王家诅咒");
      }

      public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
      }
   }
}
