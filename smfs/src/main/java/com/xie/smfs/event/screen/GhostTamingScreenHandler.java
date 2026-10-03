package com.xie.smfs.event.screen;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import com.xie.smfs.faction.FactionManager;
import com.xie.smfs.item.GoldenContainerItem;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.manager.GhostSpawnManager;
import com.xie.smfs.registry.ModItems;
import com.xie.smfs.registry.ModScreenHandlers;
import com.xie.smfs.util.GhostUtils;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostTamingScreenHandler extends ScreenHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(GhostTamingScreenHandler.class);
   public static final Identifier BUTTON_CLICK_PACKET_ID = new Identifier("smfs", "ghost_taming_button_click");
   public static final int CONTAINER_SLOT_X = 79;
   public static final int CONTAINER_SLOT_Y = 20;
   public static final int PLAYER_INVENTORY_X = 8;
   public static final int PLAYER_INVENTORY_Y = 84;
   public static final int ACTIVE_SLOT_INDEX = 0;
   public static final int MIDDLE_SLOT_INDEX = 0;
   private final Inventory tamingInventory;
   private static final int SLOT_COUNT = 1;
   private static final int CUSTOM_SLOTS_COUNT = 1;
   private static final int PLAYER_MAIN_INVENTORY_COUNT = 27;
   private static final int PLAYER_HOTBAR_COUNT = 9;
   private static final int TOTAL_SLOT_COUNT = 37;
   private static final int CUSTOM_SLOTS_START = 0;
   private static final int PLAYER_MAIN_INVENTORY_START = 1;
   private static final int PLAYER_HOTBAR_START = 28;
   private String ghostType = "";

   public GhostTamingScreenHandler(int syncId, PlayerInventory playerInventory, Inventory tamingInventory) {
      super(ModScreenHandlers.GHOST_TAMING_SCREEN_HANDLER, syncId);
      method_17359(tamingInventory, 1);
      this.tamingInventory = tamingInventory;
      tamingInventory.method_5435(playerInventory.field_7546);
      this.method_7621(new GhostTamingScreenHandler.ContainerSlot(tamingInventory, 0, 79, 20));

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            int playerSlotIndex = j + i * 9 + 9;
            this.method_7621(new Slot(playerInventory, playerSlotIndex, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.method_7621(new Slot(playerInventory, i, 8 + i * 18, 142));
      }
   }

   public GhostTamingScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
      this(syncId, playerInventory, new SimpleInventory(1));
      this.ghostType = buf.method_19772();
   }

   public String getGhostType() {
      return this.ghostType;
   }

   public boolean method_7597(PlayerEntity player) {
      return this.tamingInventory.method_5443(player);
   }

   public void method_7609(Inventory inventory) {
      super.method_7609(inventory);
      if (inventory == this.tamingInventory) {
         this.method_7623();
      }
   }

   public boolean method_7604(PlayerEntity player, int id) {
      return id == 0 ? this.handleTaming(player) : false;
   }

   public ItemStack method_7601(PlayerEntity player, int slotIndex) {
      ItemStack originalStack = ItemStack.field_8037;
      Slot slot = (Slot)this.field_7761.get(slotIndex);
      if (slot != null && slot.method_7681()) {
         ItemStack slotStack = slot.method_7677();
         originalStack = slotStack.method_7972();
         if (slotIndex >= 0 && slotIndex < 1) {
            if (!this.method_7616(slotStack, 1, 37, false)) {
               return ItemStack.field_8037;
            }
         } else if (slotIndex >= 1 && slotIndex < 28) {
            if (!this.method_7616(slotStack, 0, 1, false) && !this.method_7616(slotStack, 28, 37, false)) {
               return ItemStack.field_8037;
            }
         } else {
            if (slotIndex < 28 || slotIndex >= 37) {
               return ItemStack.field_8037;
            }

            if (!this.method_7616(slotStack, 0, 1, false) && !this.method_7616(slotStack, 1, 28, false)) {
               return ItemStack.field_8037;
            }
         }

         if (slotStack.method_7960()) {
            slot.method_48931(ItemStack.field_8037);
         } else {
            slot.method_7668();
         }

         if (slotStack.method_7947() == originalStack.method_7947()) {
            return ItemStack.field_8037;
         }

         slot.method_7667(player, slotStack);
      }

      return originalStack;
   }

   public void method_7593(int slotId, int button, SlotActionType actionType, PlayerEntity player) {
      super.method_7593(slotId, button, actionType, player);
   }

   public boolean validateTamingConditions(PlayerEntity player) {
      if (player.method_37908().field_9236) {
         return false;
      }

      ItemStack containerStack = this.tamingInventory.method_5438(0);
      if (!containerStack.method_7960() && containerStack.method_7909() instanceof GoldenContainerItem) {
         NbtCompound nbt = containerStack.method_7948();
         boolean hasGhost = nbt.method_10577("HasGhost");
         if (!hasGhost) {
            return false;
         }

         NbtCompound contained = nbt.method_10562("ContainedGhost");
         String ghostType = contained.method_10558("id");
         if (ghostType.contains(":")) {
            ghostType = ghostType.split(":")[1];
         }

         return !ghostType.isEmpty();
      } else {
         return false;
      }
   }

   public boolean handleTaming(PlayerEntity player) {
      if (player.method_37908().field_9236) {
         return false;
      }

      ItemStack containerStack = this.tamingInventory.method_5438(0);
      if (!containerStack.method_7960() && containerStack.method_7909() instanceof GoldenContainerItem) {
         NbtCompound nbt = containerStack.method_7948();
         boolean hasGhost = nbt.method_10577("HasGhost");
         if (!hasGhost) {
            return false;
         }

         NbtCompound contained = nbt.method_10562("ContainedGhost");
         String ghostType = contained.method_10558("id");
         if (ghostType.contains(":")) {
            ghostType = ghostType.split(":")[1];
         }

         if (ghostType.isEmpty()) {
            player.method_7353(Text.method_43470("无法识别鬼的类型"), false);
            return false;
         } else {
            boolean isPlayerGhost = contained.method_10545("PlayerUuid");
            int controlSlotIndex = this.findFirstItemInPlayerInventory(player, ModItems.CONTROL_SLOT);
            boolean hasControlSlot = controlSlotIndex >= 0;
            return isPlayerGhost
               ? this.handlePlayerGhostTaming(player, containerStack, contained, controlSlotIndex, hasControlSlot)
               : this.handleNormalGhostTaming(player, containerStack, ghostType, nbt, controlSlotIndex, hasControlSlot);
         }
      } else {
         return false;
      }
   }

   private boolean handlePlayerGhostTaming(PlayerEntity player, ItemStack containerStack, NbtCompound ghostNbt, int controlSlotIndex, boolean hasControlSlot) {
      LOGGER.debug("检测到玩家鬼，开始读取内部储存的鬼信息");
      if (!ghostNbt.method_10545("GhostSlotsData")) {
         player.method_7353(Text.method_43470("该玩家鬼没有槽位信息，无法驾驭"), false);
         return false;
      }

      NbtCompound ghostSlotsData = ghostNbt.method_10562("GhostSlotsData");
      int validSlotCount = 0;

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlotsData.method_10545(slotKey)) {
            NbtCompound slotData = ghostSlotsData.method_10562(slotKey);
            if (slotData.method_10577("occupied")) {
               validSlotCount++;
            }
         }
      }

      if (validSlotCount == 0) {
         player.method_7353(Text.method_43470("该玩家鬼没有驾驭任何鬼，无法驾驭"), false);
         return false;
      }

      LOGGER.debug("玩家鬼包含{}个有效槽位", validSlotCount);
      if (!hasControlSlot) {
         if (player.method_6051().method_43057() > 0.2F) {
            return this.handleTamingFailure(player, containerStack, ghostNbt);
         }

         LOGGER.debug("玩家鬼驾驭成功（不使用驾驭名额，20%成功率）");
      } else {
         if (player.method_6051().method_43057() > 0.85F) {
            return this.handleTamingFailure(player, containerStack, ghostNbt);
         }

         player.method_31548().method_5438(controlSlotIndex).method_7934(1);
         LOGGER.debug("玩家鬼驾驭成功（使用驾驭名额，85%成功率）");
      }

      int successCount = 0;

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlotsData.method_10545(slotKey)) {
            NbtCompound slotData = ghostSlotsData.method_10562(slotKey);
            if (slotData.method_10577("occupied") && slotData.method_10545("item")) {
               NbtCompound itemNbt = slotData.method_10562("item");
               String ghostType = "";
               if (itemNbt.method_10545("TamedGhost")) {
                  NbtCompound tamedGhostData = itemNbt.method_10562("TamedGhost");
                  if (tamedGhostData.method_10545("Type")) {
                     ghostType = tamedGhostData.method_10558("Type");
                  } else if (tamedGhostData.method_10545("id")) {
                     String fullId = tamedGhostData.method_10558("id");
                     if (fullId.contains(":")) {
                        ghostType = fullId.split(":")[1];
                     }
                  }
               } else if (itemNbt.method_10545("id")) {
                  String fullId = itemNbt.method_10558("id");
                  if (fullId.contains(":")) {
                     ghostType = fullId.split(":")[1];
                  }
               }

               if (!ghostType.isEmpty()) {
                  ItemStack tamedItem = GhostUtils.createTamedItem(ghostType, itemNbt);
                  if (!tamedItem.method_7960()) {
                     if (!player.method_31548().method_7394(tamedItem)) {
                        player.method_7328(tamedItem, false);
                        LOGGER.debug("槽位{}驾驭物品掉落在地上，获得: {}", i + 1, tamedItem.method_7964().getString());
                     } else {
                        LOGGER.debug("槽位{}驾驭成功，获得: {}", i + 1, tamedItem.method_7964().getString());
                     }

                     successCount++;
                  } else {
                     LOGGER.warn("无法创建驾驭物品，鬼类型: {}", ghostType);
                  }
               } else {
                  LOGGER.warn("无法从槽位{}提取鬼类型信息，itemNbt: {}", i + 1, itemNbt.toString());
               }
            }
         }
      }

      containerStack.method_7934(1);
      this.tamingInventory.method_5431();
      this.method_7623();
      ItemStack emptyContainer = new ItemStack(ModItems.GOLDEN_CONTAINER);
      if (!player.method_31548().method_7394(emptyContainer)) {
         player.method_7328(emptyContainer, false);
      }

      if (successCount > 0) {
         player.method_7353(Text.method_43470("成功驾驭玩家鬼的" + successCount + "个槽位中的鬼魂"), false);
         LOGGER.debug("玩家鬼驾驭成功，共驾驭了{}个槽位中的鬼魂，玩家: {}", successCount, player.method_5477().getString());
         FactionManager.addReputation(player, 1000 * successCount);
         this.tryLockPlayerGhostsAfterTaming(player, ghostSlotsData);
         return true;
      } else {
         player.method_7353(Text.method_43470("驾驭玩家鬼失败，没有成功驾驭任何鬼魂"), false);
         return false;
      }
   }

   private boolean handleNormalGhostTaming(
      PlayerEntity player, ItemStack containerStack, String ghostType, NbtCompound nbt, int controlSlotIndex, boolean hasControlSlot
   ) {
      if (PlayerEvents.hasGhostType(player, ghostType)) {
         player.method_7353(Text.method_43470("无法重复驾驭"), false);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7346();
         }

         return false;
      } else {
         ItemStack tamedItem = GhostUtils.createTamedItem(ghostType, nbt);
         ItemStack displayStack = tamedItem.method_7972();
         if (tamedItem.method_7960()) {
            LOGGER.debug("驾驭转换失败：暂不支持驾驭该鬼类型，物品类型: {}，鬼类型: {}，玩家: {}", containerStack.method_7909().method_7876(), ghostType, player.method_5477().getString());
            player.method_7353(Text.method_43470("暂不支持驾驭"), false);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7346();
            }

            return false;
         } else if (tamedItem.method_7909() == ModItems.CONTROL_SLOT) {
            LOGGER.debug("驾驭转换失败：暂不支持驾驭该鬼类型（返回控制槽），鬼类型: {}，玩家: {}", ghostType, player.method_5477().getString());
            player.method_7353(Text.method_43470("暂不支持驾驭"), false);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7346();
            }

            return false;
         } else {
            if (!hasControlSlot) {
               if (player.method_6051().method_43057() > 0.2F) {
                  return this.handleTamingFailure(player, containerStack, nbt);
               }

               LOGGER.debug("普通鬼驾驭成功（不使用驾驭名额，20%成功率）");
            } else {
               if (player.method_6051().method_43057() > 0.85F) {
                  return this.handleTamingFailure(player, containerStack, nbt);
               }

               player.method_31548().method_5438(controlSlotIndex).method_7934(1);
               LOGGER.debug("普通鬼驾驭成功（使用驾驭名额，85%成功率）");
            }

            containerStack.method_7934(1);
            this.tamingInventory.method_5431();
            this.method_7623();
            ItemStack emptyContainer = new ItemStack(ModItems.GOLDEN_CONTAINER);
            if (!player.method_31548().method_7394(emptyContainer)) {
               player.method_7328(emptyContainer, false);
            }

            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7346();
            }

            boolean directTame = ModConfig.getInstance().directTameGhost;
            if (directTame) {
               int emptySlot = PlayerEvents.getUnlockedGhostSlot(player);
               LOGGER.debug("查找空槽位结果: {}", emptySlot == -1 ? "无可用槽位" : "找到空槽位: " + (emptySlot + 1));
               if (emptySlot != -1) {
                  try {
                     PlayerEvents.setGhostSlotData(player, emptySlot, tamedItem);
                     PlayerEvents.validateGhostSlots(player);
                     NbtCompound ghostData = new NbtCompound();
                     ghostData.method_10582("Type", ghostType);
                     ghostData.method_10582("Name", displayStack.method_7964().getString());
                     PlayerEvents.showGhostAbilityPopup(player, ghostData);
                     player.method_7353(Text.method_43470("成功驾驭，获得: " + displayStack.method_7964().getString()), false);
                     LOGGER.debug("strate转换成功并绑定到槽位{}，获得: {}，玩家: {}", emptySlot + 1, displayStack.method_7964().getString(), player.method_5477().getString());
                     FactionManager.addReputation(player, 1000);
                     return true;
                  } catch (Exception e) {
                     LOGGER.error("绑定驾驭物品到槽位 {} 失败: {}", emptySlot, e.getMessage(), e);
                     player.method_7353(Text.method_43470("绑定失败，请重试"), false);
                  }
               }
            }

            if (!player.method_31548().method_7394(tamedItem)) {
               player.method_7328(displayStack, false);
               LOGGER.debug("strate转换成功但物品栏已满，物品掉落在地上，获得: {}，玩家: {}", displayStack.method_7964().getString(), player.method_5477().getString());
            } else {
               LOGGER.debug("strate转换成功，获得: {}，玩家: {}", displayStack.method_7964().getString(), player.method_5477().getString());
            }

            player.method_7353(Text.method_43470("成功驾驭，获得: " + displayStack.method_7964().getString()), false);
            FactionManager.addReputation(player, 1000);
            tryLockGhostAfterTaming(player, ghostType);
            return true;
         }
      }
   }

   private int findFirstItemInPlayerInventory(PlayerEntity player, Item item) {
      for (int i = 0; i < player.method_31548().method_5439(); i++) {
         ItemStack s = player.method_31548().method_5438(i);
         if (!s.method_7960() && s.method_7909() == item) {
            return i;
         }
      }

      return -1;
   }

   public void method_7595(PlayerEntity player) {
      super.method_7595(player);

      for (int i = 0; i < 1; i++) {
         ItemStack stack = this.tamingInventory.method_5438(i);
         if (!stack.method_7960()) {
            if (!player.method_31548().method_7394(stack.method_7972())) {
               player.method_7328(stack.method_7972(), false);
            }

            this.tamingInventory.method_5447(i, ItemStack.field_8037);
         }
      }

      this.tamingInventory.method_5432(player);
   }

   private boolean handleTamingFailure(PlayerEntity player, ItemStack containerStack, NbtCompound nbt) {
      LOGGER.info("驾驭失败（不使用驾驭名额，20%成功率失败），开始处理失败后果");
      String ghostType = "";
      NbtCompound contained = new NbtCompound();
      if (nbt.method_10545("ContainedGhost")) {
         contained = nbt.method_10562("ContainedGhost");
         ghostType = contained.method_10558("id");
         if (ghostType.contains(":")) {
            ghostType = ghostType.split(":")[1];
         }
      }

      containerStack.method_7934(1);
      this.tamingInventory.method_5431();
      this.method_7623();
      ItemStack emptyContainer = new ItemStack(ModItems.GOLDEN_CONTAINER);
      if (!player.method_31548().method_7394(emptyContainer)) {
         player.method_7328(emptyContainer, false);
      }

      player.method_6092(new StatusEffectInstance(StatusEffects.field_5920, 200, 2));
      if (!ghostType.isEmpty()) {
         if (ghostType.equals("player_ghost")) {
            this.spawnPlayerGhostFromNbt(player, contained);
         } else {
            this.spawnCorrespondingGhost(player, ghostType);
         }
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.method_7346();
      }

      player.method_7353(Text.method_43470("驾驭失败！"), false);
      LOGGER.info("驾驭失败处理完成，玩家: {}，鬼类型: {}", player.method_5477().getString(), ghostType);
      return false;
   }

   private void spawnCorrespondingGhost(PlayerEntity player, String ghostType) {
      BlockPos playerPos = player.method_24515();
      World world = player.method_37908();

      try {
         EntityType<?> entityType = this.getEntityTypeByGhostType(ghostType, world);
         if (entityType != null) {
            MobEntity ghostEntity = (MobEntity)entityType.method_5883(world);
            if (ghostEntity != null) {
               BlockPos spawnPos = playerPos.method_10069(player.method_6051().method_43048(5) - 2, 0, player.method_6051().method_43048(5) - 2);
               spawnPos = this.findSafeSpawnPosition(world, spawnPos);
               ghostEntity.method_5808(
                  spawnPos.method_10263() + 0.5, spawnPos.method_10264(), spawnPos.method_10260() + 0.5, player.method_6051().method_43057() * 360.0F, 0.0F
               );
               ghostEntity.method_5980(player);
               world.method_8649(ghostEntity);
               LOGGER.debug("成功生成鬼实体: {}，位置: {}", ghostType, spawnPos);
            }
         }
      } catch (Exception e) {
         LOGGER.error("生成鬼实体失败，鬼类型: {}，错误: {}", ghostType, e.getMessage());
      }
   }

   private EntityType<?> getEntityTypeByGhostType(String ghostType, World world) {
      if (ghostType != null && !ghostType.isEmpty() && world != null) {
         GhostEntity ghostEntity = GhostUtils.createGhostEntityByType(ghostType, world);
         if (ghostEntity != null) {
            return ghostEntity.method_5864();
         }

         try {
            return (EntityType<?>)EntityType.method_5898(ghostType).orElse(null);
         } catch (Exception e) {
            LOGGER.warn("无法获取鬼类型对应的实体类型: {}", ghostType);
            return null;
         }
      } else {
         return null;
      }
   }

   private BlockPos findSafeSpawnPosition(World world, BlockPos pos) {
      for (int y = 0; y < 3; y++) {
         BlockPos checkPos = pos.method_10086(y);
         if (world.method_8320(checkPos).method_26215() && world.method_8320(checkPos.method_10084()).method_26215()) {
            return checkPos;
         }
      }

      return pos;
   }

   private void spawnPlayerGhostFromNbt(PlayerEntity player, NbtCompound ghostNbt) {
      World world = player.method_37908();

      try {
         Entity entity = (Entity)EntityType.method_5892(ghostNbt, world).orElse(null);
         if (entity instanceof PlayerGhostEntity playerGhost) {
            playerGhost.method_5651(ghostNbt);
            playerGhost.method_5749(ghostNbt);
            BlockPos playerPos = player.method_24515();
            BlockPos spawnPos = playerPos.method_10069(player.method_6051().method_43048(5) - 2, 0, player.method_6051().method_43048(5) - 2);
            spawnPos = this.findSafeSpawnPosition(world, spawnPos);
            playerGhost.method_5808(
               spawnPos.method_10263() + 0.5, spawnPos.method_10264(), spawnPos.method_10260() + 0.5, player.method_6051().method_43057() * 360.0F, 0.0F
            );
            playerGhost.method_5980(player);
            world.method_8649(playerGhost);
         } else {
            LOGGER.warn("玩家鬼NBT创建实体失败，类型: {}", entity != null ? entity.method_5864().method_5882() : "null");
         }
      } catch (Exception e) {
         LOGGER.error("生成玩家鬼失败: {}", e.getMessage());
      }
   }

   public static void tryLockGhostAfterTaming(PlayerEntity player, String ghostType) {
      if (ModConfig.getInstance().lockAfterTaming) {
         if (ghostType != null && !ghostType.isEmpty()) {
            if (player.method_37908() instanceof ServerWorld serverWorld) {
               GhostEntity ghostEntity = GhostUtils.createGhostEntityByType(ghostType, player.method_37908());
               if (ghostEntity != null) {
                  EntityType<?> entityType = ghostEntity.method_5864();
                  if (GhostSpawnManager.getSpawnedGhostTypes().contains(entityType)) {
                     GhostSpawnManager.lockGhostType(serverWorld, entityType);
                     if (player instanceof ServerPlayerEntity serverPlayer) {
                        AdvancementManager.checkAndUnlockEndGhostEra(serverPlayer);
                     }
                  }
               }
            }
         }
      }
   }

   private void tryLockPlayerGhostsAfterTaming(PlayerEntity player, NbtCompound ghostSlotsData) {
      if (ModConfig.getInstance().lockAfterTaming) {
         if (player.method_37908() instanceof ServerWorld serverWorld) {
            for (int var11 = 0; var11 < 10; var11++) {
               String slotKey = "Slot" + var11;
               if (ghostSlotsData.method_10545(slotKey)) {
                  NbtCompound slotData = ghostSlotsData.method_10562(slotKey);
                  if (slotData.method_10577("occupied") && slotData.method_10545("item")) {
                     NbtCompound itemNbt = slotData.method_10562("item");
                     String ghostType = "";
                     if (itemNbt.method_10545("TamedGhost")) {
                        NbtCompound tamedGhostData = itemNbt.method_10562("TamedGhost");
                        if (tamedGhostData.method_10545("Type")) {
                           ghostType = tamedGhostData.method_10558("Type");
                        }
                     } else if (itemNbt.method_10545("id")) {
                        String fullId = itemNbt.method_10558("id");
                        if (fullId.contains(":")) {
                           ghostType = fullId.split(":")[1];
                        }
                     }

                     if (!ghostType.isEmpty()) {
                        GhostEntity ghostEntity = GhostUtils.createGhostEntityByType(ghostType, player.method_37908());
                        if (ghostEntity != null) {
                           EntityType<?> entityType = ghostEntity.method_5864();
                           if (GhostSpawnManager.getSpawnedGhostTypes().contains(entityType)) {
                              GhostSpawnManager.lockGhostType(serverWorld, entityType);
                           }
                        }
                     }
                  }
               }
            }

            if (player instanceof ServerPlayerEntity serverPlayer) {
               AdvancementManager.checkAndUnlockEndGhostEra(serverPlayer);
            }
         }
      }
   }

   private static class ContainerSlot extends Slot {
      public ContainerSlot(Inventory inventory, int index, int x, int y) {
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

      public boolean method_7674(PlayerEntity playerEntity) {
         return true;
      }
   }

   public static class GhostTamingFactory implements ExtendedScreenHandlerFactory, NamedScreenHandlerFactory {
      private final ItemStack containerStack;

      public GhostTamingFactory(ItemStack containerStack) {
         this.containerStack = containerStack;
      }

      public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
         String ghostType = "";
         if (this.containerStack.method_7909() instanceof GoldenContainerItem) {
            NbtCompound nbt = this.containerStack.method_7948();
            boolean hasGhost = nbt.method_10577("HasGhost");
            if (hasGhost) {
               NbtCompound contained = nbt.method_10562("ContainedGhost");
               ghostType = contained.method_10558("id");
               if (ghostType.contains(":")) {
                  ghostType = ghostType.split(":")[1];
               }
            }
         }

         buf.method_10814(ghostType);
      }

      public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
         return new GhostTamingScreenHandler(syncId, inv, new SimpleInventory(1));
      }

      public Text method_5476() {
         return Text.method_43471("screen.smfs.ghost_taming");
      }
   }
}
