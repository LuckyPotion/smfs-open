package com.xie.smfs.api.impl;

import com.xie.smfs.Smfs;
import com.xie.smfs.api.TameableItemAPI;
import com.xie.smfs.api.common.TameableItemAttributes;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.item.BaseGhostEyeItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.util.Identifier;

public class TameableItemAPIImpl implements TameableItemAPI {
   private final Set<String> registeredGhostTypes = new HashSet<>();
   private final Map<String, List<BiConsumer<PlayerEntity, ItemStack>>> itemUseListeners = new HashMap<>();

   @Override
   public BaseGhostEyeItem registerCustomTameableItem(
      Identifier id,
      Class<? extends BaseGhostEyeItem> itemClass,
      int maxSpiritBonus,
      int spiritResistanceBonus,
      int spiritDamageBonus,
      int sanityBonus,
      double revivalFactor
   ) {
      if (!BaseGhostEyeItem.class.isAssignableFrom(itemClass)) {
         throw new IllegalArgumentException("Custom tameable item class must extend BaseGhostEyeItem");
      }

      try {
         BaseGhostEyeItem item = itemClass.getConstructor(Settings.class, int.class, int.class, int.class, int.class, double.class)
            .newInstance(new Settings().maxCount(1), maxSpiritBonus, spiritResistanceBonus, spiritDamageBonus, sanityBonus, revivalFactor);
         String ghostType = item.getGhostType();
         this.registeredGhostTypes.add(ghostType);
         Smfs.LOGGER.debug("成功注册自定义可驾驭物品: {} (鬼类型: {})", id, ghostType);
         return item;
      } catch (Exception e) {
         throw new RuntimeException("Failed to create custom tameable item", e);
      }
   }

   @Override
   public TameableItemAttributes.Builder createCustomTameableAttributes(
      int maxSpiritBonus, int spiritResistanceBonus, int spiritDamageBonus, int sanityBonus, double revivalFactor
   ) {
      return TameableItemAttributes.builder()
         .maxSpiritBonus(maxSpiritBonus)
         .spiritResistanceBonus(spiritResistanceBonus)
         .spiritDamageBonus(spiritDamageBonus)
         .sanityBonus(sanityBonus)
         .revivalFactor(revivalFactor);
   }

   @Override
   public ItemStack getGhostSlotItem(PlayerEntity player, int slotIndex) {
      return PlayerEvents.getGhostSlotItem(player, slotIndex);
   }

   @Override
   public boolean isGhostSlotOccupied(PlayerEntity player, int slotIndex) {
      return PlayerEvents.isGhostSlotOccupied(player, slotIndex);
   }

   @Override
   public int getGhostSlotRevivalDegree(PlayerEntity player, int slotIndex) {
      return PlayerEvents.getGhostSlotRevivalDegree(player, slotIndex);
   }

   @Override
   public int getGhostSlotRequiredRevivalDegree(PlayerEntity player, int slotIndex) {
      return PlayerEvents.getGhostSlotRequiredRevivalDegree(player, slotIndex);
   }

   @Override
   public String getGhostTypeInSlot(PlayerEntity player, int slotIndex) {
      return PlayerEvents.getGhostTypeInSlot(player, slotIndex);
   }

   @Override
   public int getGhostSlotLevel(PlayerEntity player, int slotIndex) {
      return PlayerEvents.getGhostSlotLevel(player, slotIndex);
   }

   @Override
   public int getTamedGhostCount(PlayerEntity player) {
      return PlayerEvents.countOccupiedGhostSlots(player);
   }

   @Override
   public boolean hasTamedGhost(PlayerEntity player, String ghostType) {
      return PlayerEvents.hasGhostType(player, ghostType);
   }

   @Override
   public String getGhostType(ItemStack itemStack) {
      return itemStack.getItem() instanceof BaseGhostEyeItem ? ((BaseGhostEyeItem)itemStack.getItem()).getGhostType() : null;
   }

   @Override
   public void setGhostSlotItem(PlayerEntity player, int slotIndex, ItemStack itemStack) {
      PlayerEvents.setGhostSlotData(player, slotIndex, itemStack);
   }

   @Override
   public void setGhostSlotRevivalDegree(PlayerEntity player, int slotIndex, int revivalDegree) {
      PlayerEvents.updateGhostSlotValue(player, slotIndex, "revivalDegree", revivalDegree);
   }

   @Override
   public void setGhostSlotLevel(PlayerEntity player, int slotIndex, int level) {
      PlayerEvents.updateGhostSlotValue(player, slotIndex, "level", level);
   }

   @Override
   public void setGhostSlotRequiredRevivalDegree(PlayerEntity player, int slotIndex, int requiredRevivalDegree) {
      PlayerEvents.updateGhostSlotValue(player, slotIndex, "requiredRevivalDegree", requiredRevivalDegree);
   }

   @Override
   public void clearGhostSlot(PlayerEntity player, int slotIndex) {
      PlayerEvents.clearGhostSlot(player, slotIndex);
   }

   @Override
   public int getItemLevel(ItemStack itemStack) {
      if (itemStack == null || itemStack.isEmpty()) {
         return 0;
      } else if (itemStack.getItem() instanceof BaseGhostEyeItem) {
         return itemStack.hasNbt() && itemStack.getNbt().contains("level") ? itemStack.getNbt().getInt("level") : 1;
      } else {
         return 0;
      }
   }

   @Override
   public void addItemUseListener(String ghostType, BiConsumer<PlayerEntity, ItemStack> listener) {
      this.itemUseListeners.computeIfAbsent(ghostType, k -> new ArrayList<>()).add(listener);
   }

   @Override
   public void triggerItemUseListeners(PlayerEntity player, ItemStack itemStack, String ghostType) {
      if (player != null && itemStack != null && ghostType != null) {
         List<BiConsumer<PlayerEntity, ItemStack>> specificListeners = this.itemUseListeners.get(ghostType);
         if (specificListeners != null) {
            for (BiConsumer<PlayerEntity, ItemStack> listener : specificListeners) {
               try {
                  listener.accept(player, itemStack);
               } catch (Exception e) {
                  Smfs.LOGGER.error("触发特定鬼类型监听器失败: ghostType={}, error={}", ghostType, e.getMessage(), e);
               }
            }
         }

         List<BiConsumer<PlayerEntity, ItemStack>> wildcardListeners = this.itemUseListeners.get("*");
         if (wildcardListeners != null) {
            for (BiConsumer<PlayerEntity, ItemStack> listener : wildcardListeners) {
               try {
                  listener.accept(player, itemStack);
               } catch (Exception e) {
                  Smfs.LOGGER.error("触发通配符监听器失败: error={}", e.getMessage(), e);
               }
            }
         }
      }
   }
}
