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
      checkSize(tamingInventory, 1);
      this.tamingInventory = tamingInventory;
      tamingInventory.onOpen(playerInventory.player);
      this.addSlot(new GhostTamingScreenHandler.ContainerSlot(tamingInventory, 0, 79, 20));

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            int playerSlotIndex = j + i * 9 + 9;
            this.addSlot(new Slot(playerInventory, playerSlotIndex, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
      }
   }

   public GhostTamingScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
      this(syncId, playerInventory, new SimpleInventory(1));
      this.ghostType = buf.readString();
   }

   public String getGhostType() {
      return this.ghostType;
   }

   public boolean canUse(PlayerEntity player) {
      return this.tamingInventory.canPlayerUse(player);
   }

   public void onContentChanged(Inventory inventory) {
      super.onContentChanged(inventory);
      if (inventory == this.tamingInventory) {
         this.sendContentUpdates();
      }
   }

   public boolean onButtonClick(PlayerEntity player, int id) {
      return id == 0 ? this.handleTaming(player) : false;
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack originalStack = ItemStack.EMPTY;
      Slot slotx = (Slot)this.slots.get(slot);
      if (slotx != null && slotx.hasStack()) {
         ItemStack slotStack = slotx.getStack();
         originalStack = slotStack.copy();
         if (slot >= 0 && slot < 1) {
            if (!this.insertItem(slotStack, 1, 37, false)) {
               return ItemStack.EMPTY;
            }
         } else if (slot >= 1 && slot < 28) {
            if (!this.insertItem(slotStack, 0, 1, false) && !this.insertItem(slotStack, 28, 37, false)) {
               return ItemStack.EMPTY;
            }
         } else {
            if (slot < 28 || slot >= 37) {
               return ItemStack.EMPTY;
            }

            if (!this.insertItem(slotStack, 0, 1, false) && !this.insertItem(slotStack, 1, 28, false)) {
               return ItemStack.EMPTY;
            }
         }

         if (slotStack.isEmpty()) {
            slotx.setStack(ItemStack.EMPTY);
         } else {
            slotx.markDirty();
         }

         if (slotStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
         }

         slotx.onTakeItem(player, slotStack);
      }

      return originalStack;
   }

   public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
      super.onSlotClick(slotIndex, button, actionType, player);
   }

   public boolean validateTamingConditions(PlayerEntity player) {
      if (player.getWorld().isClient) {
         return false;
      }

      ItemStack containerStack = this.tamingInventory.getStack(0);
      if (!containerStack.isEmpty() && containerStack.getItem() instanceof GoldenContainerItem) {
         NbtCompound nbt = containerStack.getOrCreateNbt();
         boolean hasGhost = nbt.getBoolean("HasGhost");
         if (!hasGhost) {
            return false;
         }

         NbtCompound contained = nbt.getCompound("ContainedGhost");
         String ghostType = contained.getString("id");
         if (ghostType.contains(":")) {
            ghostType = ghostType.split(":")[1];
         }

         return !ghostType.isEmpty();
      } else {
         return false;
      }
   }

   public boolean handleTaming(PlayerEntity player) {
      if (player.getWorld().isClient) {
         return false;
      }

      ItemStack containerStack = this.tamingInventory.getStack(0);
      if (!containerStack.isEmpty() && containerStack.getItem() instanceof GoldenContainerItem) {
         NbtCompound nbt = containerStack.getOrCreateNbt();
         boolean hasGhost = nbt.getBoolean("HasGhost");
         if (!hasGhost) {
            return false;
         }

         NbtCompound contained = nbt.getCompound("ContainedGhost");
         String ghostType = contained.getString("id");
         if (ghostType.contains(":")) {
            ghostType = ghostType.split(":")[1];
         }

         if (ghostType.isEmpty()) {
            player.sendMessage(Text.literal("无法识别鬼的类型"), false);
            return false;
         } else {
            boolean isPlayerGhost = contained.contains("PlayerUuid");
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
      if (!ghostNbt.contains("GhostSlotsData")) {
         player.sendMessage(Text.literal("该玩家鬼没有槽位信息，无法驾驭"), false);
         return false;
      }

      NbtCompound ghostSlotsData = ghostNbt.getCompound("GhostSlotsData");
      int validSlotCount = 0;

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlotsData.contains(slotKey)) {
            NbtCompound slotData = ghostSlotsData.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               validSlotCount++;
            }
         }
      }

      if (validSlotCount == 0) {
         player.sendMessage(Text.literal("该玩家鬼没有驾驭任何鬼，无法驾驭"), false);
         return false;
      }

      LOGGER.debug("玩家鬼包含{}个有效槽位", validSlotCount);
      if (!hasControlSlot) {
         if (player.getRandom().nextFloat() > 0.2F) {
            return this.handleTamingFailure(player, containerStack, ghostNbt);
         }

         LOGGER.debug("玩家鬼驾驭成功（不使用驾驭名额，20%成功率）");
      } else {
         if (player.getRandom().nextFloat() > 0.85F) {
            return this.handleTamingFailure(player, containerStack, ghostNbt);
         }

         player.getInventory().getStack(controlSlotIndex).decrement(1);
         LOGGER.debug("玩家鬼驾驭成功（使用驾驭名额，85%成功率）");
      }

      int successCount = 0;

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlotsData.contains(slotKey)) {
            NbtCompound slotData = ghostSlotsData.getCompound(slotKey);
            if (slotData.getBoolean("occupied") && slotData.contains("item")) {
               NbtCompound itemNbt = slotData.getCompound("item");
               String ghostType = "";
               if (itemNbt.contains("TamedGhost")) {
                  NbtCompound tamedGhostData = itemNbt.getCompound("TamedGhost");
                  if (tamedGhostData.contains("Type")) {
                     ghostType = tamedGhostData.getString("Type");
                  } else if (tamedGhostData.contains("id")) {
                     String fullId = tamedGhostData.getString("id");
                     if (fullId.contains(":")) {
                        ghostType = fullId.split(":")[1];
                     }
                  }
               } else if (itemNbt.contains("id")) {
                  String fullId = itemNbt.getString("id");
                  if (fullId.contains(":")) {
                     ghostType = fullId.split(":")[1];
                  }
               }

               if (!ghostType.isEmpty()) {
                  ItemStack tamedItem = GhostUtils.createTamedItem(ghostType, itemNbt);
                  if (!tamedItem.isEmpty()) {
                     if (!player.getInventory().insertStack(tamedItem)) {
                        player.dropItem(tamedItem, false);
                        LOGGER.debug("槽位{}驾驭物品掉落在地上，获得: {}", i + 1, tamedItem.getName().getString());
                     } else {
                        LOGGER.debug("槽位{}驾驭成功，获得: {}", i + 1, tamedItem.getName().getString());
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

      containerStack.decrement(1);
      this.tamingInventory.markDirty();
      this.sendContentUpdates();
      ItemStack emptyContainer = new ItemStack(ModItems.GOLDEN_CONTAINER);
      if (!player.getInventory().insertStack(emptyContainer)) {
         player.dropItem(emptyContainer, false);
      }

      if (successCount > 0) {
         player.sendMessage(Text.literal("成功驾驭玩家鬼的" + successCount + "个槽位中的鬼魂"), false);
         LOGGER.debug("玩家鬼驾驭成功，共驾驭了{}个槽位中的鬼魂，玩家: {}", successCount, player.getName().getString());
         FactionManager.addReputation(player, 1000 * successCount);
         this.tryLockPlayerGhostsAfterTaming(player, ghostSlotsData);
         return true;
      } else {
         player.sendMessage(Text.literal("驾驭玩家鬼失败，没有成功驾驭任何鬼魂"), false);
         return false;
      }
   }

   private boolean handleNormalGhostTaming(
      PlayerEntity player, ItemStack containerStack, String ghostType, NbtCompound nbt, int controlSlotIndex, boolean hasControlSlot
   ) {
      if (PlayerEvents.hasGhostType(player, ghostType)) {
         player.sendMessage(Text.literal("无法重复驾驭"), false);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.closeHandledScreen();
         }

         return false;
      } else {
         ItemStack tamedItem = GhostUtils.createTamedItem(ghostType, nbt);
         ItemStack displayStack = tamedItem.copy();
         if (tamedItem.isEmpty()) {
            LOGGER.debug("驾驭转换失败：暂不支持驾驭该鬼类型，物品类型: {}，鬼类型: {}，玩家: {}", containerStack.getItem().getTranslationKey(), ghostType, player.getName().getString());
            player.sendMessage(Text.literal("暂不支持驾驭"), false);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.closeHandledScreen();
            }

            return false;
         } else if (tamedItem.getItem() == ModItems.CONTROL_SLOT) {
            LOGGER.debug("驾驭转换失败：暂不支持驾驭该鬼类型（返回控制槽），鬼类型: {}，玩家: {}", ghostType, player.getName().getString());
            player.sendMessage(Text.literal("暂不支持驾驭"), false);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.closeHandledScreen();
            }

            return false;
         } else {
            if (!hasControlSlot) {
               if (player.getRandom().nextFloat() > 0.2F) {
                  return this.handleTamingFailure(player, containerStack, nbt);
               }

               LOGGER.debug("普通鬼驾驭成功（不使用驾驭名额，20%成功率）");
            } else {
               if (player.getRandom().nextFloat() > 0.85F) {
                  return this.handleTamingFailure(player, containerStack, nbt);
               }

               player.getInventory().getStack(controlSlotIndex).decrement(1);
               LOGGER.debug("普通鬼驾驭成功（使用驾驭名额，85%成功率）");
            }

            containerStack.decrement(1);
            this.tamingInventory.markDirty();
            this.sendContentUpdates();
            ItemStack emptyContainer = new ItemStack(ModItems.GOLDEN_CONTAINER);
            if (!player.getInventory().insertStack(emptyContainer)) {
               player.dropItem(emptyContainer, false);
            }

            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.closeHandledScreen();
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
                     ghostData.putString("Type", ghostType);
                     ghostData.putString("Name", displayStack.getName().getString());
                     PlayerEvents.showGhostAbilityPopup(player, ghostData);
                     player.sendMessage(Text.literal("成功驾驭，获得: " + displayStack.getName().getString()), false);
                     LOGGER.debug("strate转换成功并绑定到槽位{}，获得: {}，玩家: {}", emptySlot + 1, displayStack.getName().getString(), player.getName().getString());
                     FactionManager.addReputation(player, 1000);
                     return true;
                  } catch (Exception e) {
                     LOGGER.error("绑定驾驭物品到槽位 {} 失败: {}", emptySlot, e.getMessage(), e);
                     player.sendMessage(Text.literal("绑定失败，请重试"), false);
                  }
               }
            }

            if (!player.getInventory().insertStack(tamedItem)) {
               player.dropItem(displayStack, false);
               LOGGER.debug("strate转换成功但物品栏已满，物品掉落在地上，获得: {}，玩家: {}", displayStack.getName().getString(), player.getName().getString());
            } else {
               LOGGER.debug("strate转换成功，获得: {}，玩家: {}", displayStack.getName().getString(), player.getName().getString());
            }

            player.sendMessage(Text.literal("成功驾驭，获得: " + displayStack.getName().getString()), false);
            FactionManager.addReputation(player, 1000);
            tryLockGhostAfterTaming(player, ghostType);
            return true;
         }
      }
   }

   private int findFirstItemInPlayerInventory(PlayerEntity player, Item item) {
      for (int i = 0; i < player.getInventory().size(); i++) {
         ItemStack s = player.getInventory().getStack(i);
         if (!s.isEmpty() && s.getItem() == item) {
            return i;
         }
      }

      return -1;
   }

   public void onClosed(PlayerEntity player) {
      super.onClosed(player);

      for (int i = 0; i < 1; i++) {
         ItemStack stack = this.tamingInventory.getStack(i);
         if (!stack.isEmpty()) {
            if (!player.getInventory().insertStack(stack.copy())) {
               player.dropItem(stack.copy(), false);
            }

            this.tamingInventory.setStack(i, ItemStack.EMPTY);
         }
      }

      this.tamingInventory.onClose(player);
   }

   private boolean handleTamingFailure(PlayerEntity player, ItemStack containerStack, NbtCompound nbt) {
      LOGGER.info("驾驭失败（不使用驾驭名额，20%成功率失败），开始处理失败后果");
      String ghostType = "";
      NbtCompound contained = new NbtCompound();
      if (nbt.contains("ContainedGhost")) {
         contained = nbt.getCompound("ContainedGhost");
         ghostType = contained.getString("id");
         if (ghostType.contains(":")) {
            ghostType = ghostType.split(":")[1];
         }
      }

      containerStack.decrement(1);
      this.tamingInventory.markDirty();
      this.sendContentUpdates();
      ItemStack emptyContainer = new ItemStack(ModItems.GOLDEN_CONTAINER);
      if (!player.getInventory().insertStack(emptyContainer)) {
         player.dropItem(emptyContainer, false);
      }

      player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 200, 2));
      if (!ghostType.isEmpty()) {
         if (ghostType.equals("player_ghost")) {
            this.spawnPlayerGhostFromNbt(player, contained);
         } else {
            this.spawnCorrespondingGhost(player, ghostType);
         }
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.closeHandledScreen();
      }

      player.sendMessage(Text.literal("驾驭失败！"), false);
      LOGGER.info("驾驭失败处理完成，玩家: {}，鬼类型: {}", player.getName().getString(), ghostType);
      return false;
   }

   private void spawnCorrespondingGhost(PlayerEntity player, String ghostType) {
      BlockPos playerPos = player.getBlockPos();
      World world = player.getWorld();

      try {
         EntityType<?> entityType = this.getEntityTypeByGhostType(ghostType, world);
         if (entityType != null) {
            MobEntity ghostEntity = (MobEntity)entityType.create(world);
            if (ghostEntity != null) {
               BlockPos spawnPos = playerPos.add(player.getRandom().nextInt(5) - 2, 0, player.getRandom().nextInt(5) - 2);
               spawnPos = this.findSafeSpawnPosition(world, spawnPos);
               ghostEntity.refreshPositionAndAngles(
                  spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getRandom().nextFloat() * 360.0F, 0.0F
               );
               ghostEntity.setTarget(player);
               world.spawnEntity(ghostEntity);
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
            return ghostEntity.getType();
         }

         try {
            return (EntityType<?>)EntityType.get(ghostType).orElse(null);
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
         BlockPos checkPos = pos.up(y);
         if (world.getBlockState(checkPos).isAir() && world.getBlockState(checkPos.up()).isAir()) {
            return checkPos;
         }
      }

      return pos;
   }

   private void spawnPlayerGhostFromNbt(PlayerEntity player, NbtCompound ghostNbt) {
      World world = player.getWorld();

      try {
         Entity entity = (Entity)EntityType.getEntityFromNbt(ghostNbt, world).orElse(null);
         if (entity instanceof PlayerGhostEntity playerGhost) {
            playerGhost.readNbt(ghostNbt);
            playerGhost.readCustomDataFromNbt(ghostNbt);
            BlockPos playerPos = player.getBlockPos();
            BlockPos spawnPos = playerPos.add(player.getRandom().nextInt(5) - 2, 0, player.getRandom().nextInt(5) - 2);
            spawnPos = this.findSafeSpawnPosition(world, spawnPos);
            playerGhost.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getRandom().nextFloat() * 360.0F, 0.0F);
            playerGhost.setTarget(player);
            world.spawnEntity(playerGhost);
         } else {
            LOGGER.warn("玩家鬼NBT创建实体失败，类型: {}", entity != null ? entity.getType().getTranslationKey() : "null");
         }
      } catch (Exception e) {
         LOGGER.error("生成玩家鬼失败: {}", e.getMessage());
      }
   }

   public static void tryLockGhostAfterTaming(PlayerEntity player, String ghostType) {
      if (ModConfig.getInstance().lockAfterTaming) {
         if (ghostType != null && !ghostType.isEmpty()) {
            if (player.getWorld() instanceof ServerWorld serverWorld) {
               GhostEntity ghostEntity = GhostUtils.createGhostEntityByType(ghostType, player.getWorld());
               if (ghostEntity != null) {
                  EntityType<?> entityType = ghostEntity.getType();
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
         if (player.getWorld() instanceof ServerWorld serverWorld) {
            for (int var11 = 0; var11 < 10; var11++) {
               String slotKey = "Slot" + var11;
               if (ghostSlotsData.contains(slotKey)) {
                  NbtCompound slotData = ghostSlotsData.getCompound(slotKey);
                  if (slotData.getBoolean("occupied") && slotData.contains("item")) {
                     NbtCompound itemNbt = slotData.getCompound("item");
                     String ghostType = "";
                     if (itemNbt.contains("TamedGhost")) {
                        NbtCompound tamedGhostData = itemNbt.getCompound("TamedGhost");
                        if (tamedGhostData.contains("Type")) {
                           ghostType = tamedGhostData.getString("Type");
                        }
                     } else if (itemNbt.contains("id")) {
                        String fullId = itemNbt.getString("id");
                        if (fullId.contains(":")) {
                           ghostType = fullId.split(":")[1];
                        }
                     }

                     if (!ghostType.isEmpty()) {
                        GhostEntity ghostEntity = GhostUtils.createGhostEntityByType(ghostType, player.getWorld());
                        if (ghostEntity != null) {
                           EntityType<?> entityType = ghostEntity.getType();
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

      public boolean canInsert(ItemStack stack) {
         return stack.getItem() instanceof GoldenContainerItem;
      }

      public int getMaxItemCount() {
         return 1;
      }

      public int getMaxItemCount(ItemStack stack) {
         return 1;
      }

      public boolean canTakeItems(PlayerEntity playerEntity) {
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
         if (this.containerStack.getItem() instanceof GoldenContainerItem) {
            NbtCompound nbt = this.containerStack.getOrCreateNbt();
            boolean hasGhost = nbt.getBoolean("HasGhost");
            if (hasGhost) {
               NbtCompound contained = nbt.getCompound("ContainedGhost");
               ghostType = contained.getString("id");
               if (ghostType.contains(":")) {
                  ghostType = ghostType.split(":")[1];
               }
            }
         }

         buf.writeString(ghostType);
      }

      public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
         return new GhostTamingScreenHandler(syncId, inv, new SimpleInventory(1));
      }

      public Text getDisplayName() {
         return Text.translatable("screen.smfs.ghost_taming");
      }
   }
}
