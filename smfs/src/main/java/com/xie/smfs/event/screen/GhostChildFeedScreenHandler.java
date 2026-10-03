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
      this.player = inventory.field_7546;
      this.ghostChildData = PlayerGhostChildManager.getGhostChildData(this.player);
      this.method_7621(new GhostChildFeedScreenHandler.FeedSlot(this.feedInventory, 0, 80, 20));
      this.addPlayerInventorySlots(inventory);
   }

   private void addPlayerInventorySlots(PlayerInventory inventory) {
      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.method_7621(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.method_7621(new Slot(inventory, col, 8 + col * 18, 142));
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

   public boolean method_7597(PlayerEntity player) {
      return true;
   }

   public void method_7595(PlayerEntity player) {
      if (!player.method_37908().field_9236) {
         ItemStack stack = this.feedInventory.method_5438(0);
         if (!stack.method_7960()) {
            this.feedInventory.method_5447(0, ItemStack.field_8037);
            if (!player.method_31548().method_7394(stack)) {
               player.method_7328(stack, false);
            }
         }
      }

      super.method_7595(player);
   }

   public ItemStack method_7601(PlayerEntity player, int index) {
      ItemStack itemStack = ItemStack.field_8037;
      Slot slot = (Slot)this.field_7761.get(index);
      if (slot != null && slot.method_7681()) {
         ItemStack itemStack2 = slot.method_7677();
         itemStack = itemStack2.method_7972();
         if (index == 0) {
            if (!this.method_7616(itemStack2, 1, this.field_7761.size(), false)) {
               return ItemStack.field_8037;
            }
         } else if (!this.method_7616(itemStack2, 0, 1, false)) {
            return ItemStack.field_8037;
         }

         if (itemStack2.method_7960()) {
            slot.method_48931(ItemStack.field_8037);
         } else {
            slot.method_7668();
         }
      }

      return itemStack;
   }

   public ItemStack getContainerStack() {
      return this.feedInventory.method_5438(0);
   }

   public void feedGhostChild() {
      ItemStack containerStack = this.feedInventory.method_5438(0);
      int containerSlot = -1;
      if (this.player != null && !containerStack.method_7960()) {
         if (containerStack.method_7909() instanceof GoldenContainerItem) {
            NbtCompound nbt = containerStack.method_7948();
            boolean hasGhost = nbt.method_10577("HasGhost");
            if (hasGhost) {
               NbtCompound contained = nbt.method_10562("ContainedGhost");
               String ghostType = contained.method_10558("id");
               if (!ghostType.isEmpty()) {
                  if (ghostType.contains(":")) {
                     ghostType = ghostType.substring(ghostType.indexOf(":") + 1);
                  }

                  int oldLevel = this.ghostChildData.getLevel();
                  Random random = new Random();
                  int totalExperience = 0;
                  if ("player_ghost".equals(ghostType) && contained.method_10545("PlayerGhosts")) {
                     NbtCompound playerGhostsNbt = contained.method_10562("PlayerGhosts");

                     for (String key : playerGhostsNbt.method_10541()) {
                        String innerGhostType = playerGhostsNbt.method_10558(key);
                        if (!innerGhostType.isEmpty()) {
                           this.ghostChildData.addFedGhostType(innerGhostType);
                           char innerTerrorLevel = this.getTerrorLevelForGhostType(innerGhostType);
                           totalExperience += this.calculateExperience(innerTerrorLevel, random);
                        }
                     }
                  } else {
                     this.ghostChildData.addFedGhostType(ghostType);
                     char terrorLevel = 'C';
                     if (contained.method_10545("TerrorLevel")) {
                        String terrorLevelStr = contained.method_10558("TerrorLevel");
                        if (!terrorLevelStr.isEmpty()) {
                           terrorLevel = Character.toUpperCase(terrorLevelStr.charAt(0));
                        }
                     }

                     totalExperience = this.calculateExperience(terrorLevel, random);
                  }

                  this.ghostChildData.addExperience(totalExperience);
                  PlayerGhostChildManager.saveGhostChildData(this.player, this.ghostChildData);
                  ItemStack emptyContainer = containerStack.method_7972();
                  emptyContainer.method_7939(1);
                  emptyContainer.method_7969().method_10551("ContainedGhost");
                  emptyContainer.method_7969().method_10556("HasGhost", false);
                  emptyContainer.method_7969().method_10551("IsHeavy");
                  this.feedInventory.method_5447(0, emptyContainer);
                  if (this.player instanceof ServerPlayerEntity serverPlayer) {
                     GhostChildFeedSuccessPacket.sendToClient(serverPlayer);
                  }

                  this.player.method_7353(Text.method_43470("§a喂食成功！"), false);
                  this.player.method_7353(Text.method_43470("§a鬼童获得了 " + totalExperience + " 点经验！"), false);
                  int newLevel = this.ghostChildData.getLevel();
                  if (newLevel > oldLevel) {
                     this.player.method_7353(Text.method_43470("§a你的鬼童升级了！现在等级：" + newLevel), false);
                  }
               } else {
                  this.player.method_7353(Text.method_43470("§c无法识别鬼的类型！"), false);
               }
            } else {
               this.player.method_7353(Text.method_43470("§c黄金容器中没有厉鬼！"), false);
            }
         } else {
            this.player.method_7353(Text.method_43470("§c请放入黄金容器！"), false);
         }
      } else if (this.player != null) {
         this.player.method_7353(Text.method_43470("§c请放入黄金容器！"), false);
      }
   }

   private char getTerrorLevelForGhostType(String ghostType) {
      if (ghostType != null && !ghostType.isEmpty()) {
         try {
            GhostEntity tempGhost = GhostUtils.createGhostEntityByType(ghostType, this.player.method_37908());
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

      public boolean method_7680(ItemStack stack) {
         return stack.method_7909() instanceof GoldenContainerItem;
      }

      public int method_7675() {
         return 1;
      }

      public int method_7676(ItemStack stack) {
         return 1;
      }
   }

   public static class GhostChildFeedFactory implements ExtendedScreenHandlerFactory {
      public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
         return new GhostChildFeedScreenHandler(syncId, inventory);
      }

      public Text method_5476() {
         return Text.method_43470("Ghost Child Feed");
      }

      public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
      }
   }
}
