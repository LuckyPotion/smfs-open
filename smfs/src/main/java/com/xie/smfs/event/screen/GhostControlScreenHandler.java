package com.xie.smfs.event.screen;

import com.xie.smfs.Smfs;
import com.xie.smfs.item.GhostPorcelainItem;
import com.xie.smfs.registry.ModScreenHandlers;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GhostControlScreenHandler extends ScreenHandler {
   private final Inventory ghostInventory;
   public static final int GHOST_SLOT_COUNT = 10;

   public GhostControlScreenHandler(int syncId, PlayerInventory playerInventory, Inventory ghostInventory) {
      super(ModScreenHandlers.GHOST_CONTROL_SCREEN_HANDLER, syncId);
      Smfs.LOGGER.info("[服务器] 服务器端构造函数被调用: syncId={}", syncId);
      method_17359(ghostInventory, 10);
      this.ghostInventory = ghostInventory;
      ghostInventory.method_5435(playerInventory.field_7546);
      int centerX = 88;
      int centerY = 74;
      int radius = 30;

      for (int i = 0; i < 6; i++) {
         double angle = (Math.PI * 2) * i / 6.0;
         int x = centerX + (int)(radius * Math.cos(angle)) - 8;
         int y = centerY + (int)(radius * Math.sin(angle)) - 8;
         this.method_7621(new GhostControlScreenHandler.GhostSlot(ghostInventory, i, x, y));
      }

      int[] customX = new int[]{33, 130, 22, 141};
      int[] customY = new int[]{29, 29, 73, 73};

      for (int i = 6; i < 10; i++) {
         int index = i - 6;
         int x = customX[index];
         int y = customY[index];
         this.method_7621(new GhostControlScreenHandler.GhostSlot(ghostInventory, i, x, y));
      }
   }

   public GhostControlScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
      this(syncId, playerInventory, new SimpleInventory(10));
      Smfs.LOGGER.info("[客户端] 客户端构造函数被调用（数据包）: syncId={}", syncId);
   }

   public GhostControlScreenHandler(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, new SimpleInventory(10));
      Smfs.LOGGER.info("[客户端] 简化客户端构造函数被调用: syncId={}", syncId);
   }

   public boolean method_7597(PlayerEntity player) {
      boolean canUse = this.ghostInventory.method_5443(player);
      if (!canUse) {
         Smfs.LOGGER.warn("[验证] 玩家{}使用权限被拒绝", player.method_5477().getString());
      }

      return canUse;
   }

   public ItemStack method_7601(PlayerEntity player, int slotIndex) {
      return ItemStack.field_8037;
   }

   public void method_7595(PlayerEntity player) {
      super.method_7595(player);
      this.ghostInventory.method_5432(player);
   }

   public static class GhostControlFactory implements ExtendedScreenHandlerFactory, NamedScreenHandlerFactory {
      public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
      }

      public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
         Smfs.LOGGER.info("[工厂] 开始创建厉鬼控制界面处理器: syncId={}, player={}", syncId, player.method_5477().getString());
         ScreenHandler handler = new GhostControlScreenHandler(syncId, inv, new SimpleInventory(10));
         Smfs.LOGGER.info("[工厂] 厉鬼控制界面处理器创建成功: {}", handler.getClass().getSimpleName());
         return handler;
      }

      public Text method_5476() {
         return Text.method_43471("screen.smfs.ghost_control");
      }
   }

   private static class GhostSlot extends Slot {
      public GhostSlot(Inventory inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }

      public boolean method_7680(ItemStack stack) {
         return stack.method_7909() instanceof GhostPorcelainItem;
      }
   }
}
