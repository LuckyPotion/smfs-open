package com.xie.smfs.event.screen;

import com.xie.smfs.data.GhostChildData;
import com.xie.smfs.data.PlayerGhostChildManager;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.GoldenContainerItem;
import com.xie.smfs.network.packets.ghostchild.s2c.GhostChildFeedSuccessPacket;
import com.xie.smfs.registry.ModScreenHandlers;
import com.xie.smfs.util.GhostUtils;
import java.util.Random;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GhostChildFeedScreenHandler extends ScreenHandler {
   public static final Identifier FEED_BUTTON_CLICK_PACKET_ID = new Identifier("smfs", "ghost_child_feed_button_click");
   private GhostChildData ghostChildData;
   private PlayerEntity player;
   private final SimpleInventory feedInventory = new SimpleInventory(1);

   public GhostChildFeedScreenHandler(int syncId, PlayerInventory inventory) {
      super(ModScreenHandlers.GHOST_CHILD_FEED_SCREEN_HANDLER, syncId);
      this.player = inventory.player;
      this.ghostChildData = PlayerGhostChildManager.getGhostChildData(this.player);
      this.addSlot(new GhostChildFeedScreenHandler.FeedSlot(this.feedInventory, 0, 80, 20));
      this.addPlayerInventorySlots(inventory);
   }

   private void addPlayerInventorySlots(PlayerInventory inventory) {
      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
      }
   }

   public GhostChildFeedScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
      this(syncId, inventory);
   }

   public GhostChildData getGhostChildData() {
      return this.ghostChildData;
   }

   public PlayerEntity getPlayer() {
      return this.player;
   }

   public boolean canUse(PlayerEntity player) {
      return true;
   }

   public void onClosed(PlayerEntity player) {
      if (!player.getWorld().isClient) {
         ItemStack stack = this.feedInventory.getStack(0);
         if (!stack.isEmpty()) {
            this.feedInventory.setStack(0, ItemStack.EMPTY);
            if (!player.getInventory().insertStack(stack)) {
               player.dropItem(stack, false);
            }
         }
      }

      super.onClosed(player);
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slotx = (Slot)this.slots.get(slot);
      if (slotx != null && slotx.hasStack()) {
         ItemStack itemStack2 = slotx.getStack();
         itemStack = itemStack2.copy();
         if (slot == 0) {
            if (!this.insertItem(itemStack2, 1, this.slots.size(), false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(itemStack2, 0, 1, false)) {
            return ItemStack.EMPTY;
         }

         if (itemStack2.isEmpty()) {
            slotx.setStack(ItemStack.EMPTY);
         } else {
            slotx.markDirty();
         }
      }

      return itemStack;
   }

   public ItemStack getContainerStack() {
      return this.feedInventory.getStack(0);
   }

   public void feedGhostChild() {
      ItemStack containerStack = this.feedInventory.getStack(0);
      int containerSlot = -1;
      if (this.player != null && !containerStack.isEmpty()) {
         if (containerStack.getItem() instanceof GoldenContainerItem) {
            NbtCompound nbt = containerStack.getOrCreateNbt();
            boolean hasGhost = nbt.getBoolean("HasGhost");
            if (hasGhost) {
               NbtCompound contained = nbt.getCompound("ContainedGhost");
               String ghostType = contained.getString("id");
               if (!ghostType.isEmpty()) {
                  if (ghostType.contains(":")) {
                     ghostType = ghostType.substring(ghostType.indexOf(":") + 1);
                  }

                  int oldLevel = this.ghostChildData.getLevel();
                  Random random = new Random();
                  int totalExperience = 0;
                  if ("player_ghost".equals(ghostType) && contained.contains("PlayerGhosts")) {
                     NbtCompound playerGhostsNbt = contained.getCompound("PlayerGhosts");

                     for (String key : playerGhostsNbt.getKeys()) {
                        String innerGhostType = playerGhostsNbt.getString(key);
                        if (!innerGhostType.isEmpty()) {
                           this.ghostChildData.addFedGhostType(innerGhostType);
                           char innerTerrorLevel = this.getTerrorLevelForGhostType(innerGhostType);
                           totalExperience += this.calculateExperience(innerTerrorLevel, random);
                        }
                     }
                  } else {
                     this.ghostChildData.addFedGhostType(ghostType);
                     char terrorLevel = 'C';
                     if (contained.contains("TerrorLevel")) {
                        String terrorLevelStr = contained.getString("TerrorLevel");
                        if (!terrorLevelStr.isEmpty()) {
                           terrorLevel = Character.toUpperCase(terrorLevelStr.charAt(0));
                        }
                     }

                     totalExperience = this.calculateExperience(terrorLevel, random);
                  }

                  this.ghostChildData.addExperience(totalExperience);
                  PlayerGhostChildManager.saveGhostChildData(this.player, this.ghostChildData);
                  ItemStack emptyContainer = containerStack.copy();
                  emptyContainer.setCount(1);
                  emptyContainer.getNbt().remove("ContainedGhost");
                  emptyContainer.getNbt().putBoolean("HasGhost", false);
                  emptyContainer.getNbt().remove("IsHeavy");
                  this.feedInventory.setStack(0, emptyContainer);
                  if (this.player instanceof ServerPlayerEntity serverPlayer) {
                     GhostChildFeedSuccessPacket.sendToClient(serverPlayer);
                  }

                  this.player.sendMessage(Text.literal("§a喂食成功！"), false);
                  this.player.sendMessage(Text.literal("§a鬼童获得了 " + totalExperience + " 点经验！"), false);
                  int newLevel = this.ghostChildData.getLevel();
                  if (newLevel > oldLevel) {
                     this.player.sendMessage(Text.literal("§a你的鬼童升级了！现在等级：" + newLevel), false);
                  }
               } else {
                  this.player.sendMessage(Text.literal("§c无法识别鬼的类型！"), false);
               }
            } else {
               this.player.sendMessage(Text.literal("§c黄金容器中没有厉鬼！"), false);
            }
         } else {
            this.player.sendMessage(Text.literal("§c请放入黄金容器！"), false);
         }
      } else if (this.player != null) {
         this.player.sendMessage(Text.literal("§c请放入黄金容器！"), false);
      }
   }

   private char getTerrorLevelForGhostType(String ghostType) {
      if (ghostType != null && !ghostType.isEmpty()) {
         try {
            GhostEntity tempGhost = GhostUtils.createGhostEntityByType(ghostType, this.player.getWorld());
            if (tempGhost != null) {
               return tempGhost.getTerrorLevel();
            }
         } catch (Exception var3) {
         }

         return 'C';
      } else {
         return 'C';
      }
   }

   private int calculateExperience(char terrorLevel, Random random) {
      switch (Character.toUpperCase(terrorLevel)) {
         case 'A':
            return random.nextInt(501) + 800;
         case 'B':
            return random.nextInt(351) + 350;
         case 'C':
         default:
            return random.nextInt(51) + 50;
         case 'S':
            return random.nextInt(1251) + 1500;
      }
   }

   private static class FeedSlot extends Slot {
      public FeedSlot(Inventory inventory, int index, int x, int y) {
         super(inventory, index, x, y);
      }

      public boolean canInsert(ItemStack stack) {
         return stack.getItem() instanceof GoldenContainerItem;
      }

      public int getMaxItemCount() {
         return 1;
      }

      public int getMaxItemCount(ItemStack stack) {
         return 1;
      }
   }

   public static class GhostChildFeedFactory implements ExtendedScreenHandlerFactory {
      public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
         return new GhostChildFeedScreenHandler(syncId, inventory);
      }

      public Text method_5476() {
         return Text.literal("Ghost Child Feed");
      }

      public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
      }
   }
}
