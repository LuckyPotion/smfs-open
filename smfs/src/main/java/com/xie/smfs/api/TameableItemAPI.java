package com.xie.smfs.api;

import com.xie.smfs.api.common.TameableItemAPIHolder;
import com.xie.smfs.api.common.TameableItemAttributes;
import com.xie.smfs.item.BaseGhostEyeItem;
import java.util.function.BiConsumer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public interface TameableItemAPI {
   static void register(TameableItemAPI instance) {
      TameableItemAPIHolder.INSTANCE = instance;
   }

   static TameableItemAPI getInstance() {
      if (TameableItemAPIHolder.INSTANCE == null) {
         throw new IllegalStateException("TameableItemAPI not registered yet. Please call TameableItemAPI.register() first.");
      } else {
         return TameableItemAPIHolder.INSTANCE;
      }
   }

   static boolean isRegistered() {
      return TameableItemAPIHolder.INSTANCE != null;
   }

   BaseGhostEyeItem registerCustomTameableItem(Identifier identifier, Class<? extends BaseGhostEyeItem> class_, int i, int j, int k, int l, double d);

   TameableItemAttributes.Builder createCustomTameableAttributes(int i, int j, int k, int l, double d);

   ItemStack getGhostSlotItem(PlayerEntity playerEntity, int i);

   boolean isGhostSlotOccupied(PlayerEntity playerEntity, int i);

   int getGhostSlotRevivalDegree(PlayerEntity playerEntity, int i);

   int getGhostSlotRequiredRevivalDegree(PlayerEntity playerEntity, int i);

   String getGhostTypeInSlot(PlayerEntity playerEntity, int i);

   int getGhostSlotLevel(PlayerEntity playerEntity, int i);

   int getTamedGhostCount(PlayerEntity playerEntity);

   boolean hasTamedGhost(PlayerEntity playerEntity, String string);

   String getGhostType(ItemStack itemStack);

   void setGhostSlotItem(PlayerEntity playerEntity, int i, ItemStack itemStack);

   void setGhostSlotRevivalDegree(PlayerEntity playerEntity, int i, int j);

   int getItemLevel(ItemStack itemStack);

   void setGhostSlotLevel(PlayerEntity playerEntity, int i, int j);

   void setGhostSlotRequiredRevivalDegree(PlayerEntity playerEntity, int i, int j);

   void clearGhostSlot(PlayerEntity playerEntity, int i);

   void addItemUseListener(String string, BiConsumer<PlayerEntity, ItemStack> biConsumer);

   void triggerItemUseListeners(PlayerEntity playerEntity, ItemStack itemStack, String string);
}
