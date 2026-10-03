package com.xie.smfs.block.entity;

import com.xie.smfs.block.SpiritBrewingStandBlock;
import com.xie.smfs.registry.ModBlockEntities;
import com.xie.smfs.registry.ModItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SpiritBrewingStandBlockEntity extends BlockEntity implements Inventory, NamedScreenHandlerFactory, GeoBlockEntity {
   public static final BlockEntityType<SpiritBrewingStandBlockEntity> TYPE = ModBlockEntities.SPIRIT_BREWING_STAND_BLOCK_ENTITY;
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private static final int SLOT_COUNT = 5;
   private static final int MOD_MATERIAL1_SLOT = 0;
   private static final int MOD_MATERIAL2_SLOT = 1;
   private static final int VANILLA_MATERIAL_SLOT = 2;
   private static final int GOLD_CONTAINER_SLOT = 3;
   private static final int BOTTLE_SLOT = 4;
   private final DefaultedList<ItemStack> inventory = DefaultedList.method_10213(5, ItemStack.field_8037);
   private int brewingTime = 0;
   private int maxBrewingTime = 0;
   private boolean isBrewing = false;
   private ItemStack brewingResult = ItemStack.field_8037;
   protected final PropertyDelegate propertyDelegate = new PropertyDelegate() {
      public int method_17390(int index) {
         return switch (index) {
            case 0 -> SpiritBrewingStandBlockEntity.this.brewingTime;
            case 1 -> SpiritBrewingStandBlockEntity.this.maxBrewingTime;
            case 2 -> SpiritBrewingStandBlockEntity.this.isBrewing ? 1 : 0;
            default -> 0;
         };
      }

      public void method_17391(int index, int value) {
         switch (index) {
            case 0:
               SpiritBrewingStandBlockEntity.this.brewingTime = value;
               break;
            case 1:
               SpiritBrewingStandBlockEntity.this.maxBrewingTime = value;
               break;
            case 2:
               SpiritBrewingStandBlockEntity.this.isBrewing = value != 0;
         }
      }

      public int method_17389() {
         return 3;
      }
   };
   private static final Map<String, SpiritBrewingStandBlockEntity.PotionConfig> POTION_CONFIGS = new HashMap<>();

   public SpiritBrewingStandBlockEntity(BlockPos pos, BlockState state) {
      super(TYPE, pos, state);
   }

   public boolean canStartBrewing() {
      if (this.isBrewing) {
         return false;
      } else {
         ItemStack modMaterial1 = (ItemStack)this.inventory.get(0);
         ItemStack modMaterial2 = (ItemStack)this.inventory.get(1);
         ItemStack vanillaMaterial = (ItemStack)this.inventory.get(2);
         ItemStack goldContainer = (ItemStack)this.inventory.get(3);
         ItemStack bottle = (ItemStack)this.inventory.get(4);
         if (modMaterial1.method_7960()) {
            return false;
         } else if (!this.isModMaterial(modMaterial1.method_7909())) {
            return false;
         } else if (modMaterial2.method_7960()) {
            return false;
         } else if (!this.isModMaterial(modMaterial2.method_7909())) {
            return false;
         } else if (vanillaMaterial.method_7960()) {
            return false;
         } else if (goldContainer.method_7960()) {
            return false;
         } else if (!this.isGoldContainerWithGhost(goldContainer)) {
            return false;
         } else {
            return bottle.method_7960() ? false : bottle.method_31574(Items.field_8469);
         }
      }
   }

   private boolean isModMaterial(Item item) {
      return ModItems.isModMaterial(item);
   }

   private boolean isGoldContainerWithGhost(ItemStack stack) {
      return stack.method_31574(ModItems.GOLDEN_CONTAINER)
         && stack.method_7948().method_10577("HasGhost")
         && stack.method_7948().method_10545("ContainedGhost");
   }

   public void startBrewing() {
      this.brewingResult = this.getModPotionResult();
      String potionId = this.getPotionIdFromResult(this.brewingResult);
      this.setBrewingTimeByPotionType(potionId);
      this.isBrewing = true;
      this.brewingTime = this.maxBrewingTime;
      this.method_5431();
   }

   private String getPotionIdFromResult(ItemStack result) {
      for (Entry<String, SpiritBrewingStandBlockEntity.PotionConfig> entry : POTION_CONFIGS.entrySet()) {
         if (result.method_31574(entry.getValue().potionItem)) {
            return entry.getKey();
         }
      }

      return "spirit_erosion_potion";
   }

   private void setBrewingTimeByPotionType(String potionId) {
      SpiritBrewingStandBlockEntity.PotionConfig config = POTION_CONFIGS.get(potionId);
      if (config != null) {
         this.maxBrewingTime = config.brewingTime;
      } else {
         this.maxBrewingTime = 1200;
      }
   }

   public void stopBrewing() {
      if (this.isBrewing) {
         ItemStack result = new ItemStack(ModItems.DISGUSTING_LIQUID);
         if (!result.method_7960() && !((ItemStack)this.inventory.get(4)).method_7960()) {
            ((ItemStack)this.inventory.get(4)).method_7934(1);
            this.inventory.set(4, result);
         }

         this.inventory.set(0, ItemStack.field_8037);
         this.inventory.set(1, ItemStack.field_8037);
         this.inventory.set(2, ItemStack.field_8037);
         this.inventory.set(3, new ItemStack(ModItems.GOLDEN_CONTAINER));
         this.isBrewing = false;
         this.brewingTime = 0;
         this.maxBrewingTime = 0;
         this.brewingResult = ItemStack.field_8037;
         this.method_5431();
      }
   }

   private float calculateMaterialValue() {
      float value = 0.0F;
      ItemStack modMaterial1 = (ItemStack)this.inventory.get(0);
      ItemStack modMaterial2 = (ItemStack)this.inventory.get(1);
      ItemStack vanillaMaterial = (ItemStack)this.inventory.get(2);
      Random random = new Random();
      if (!modMaterial1.method_7960()) {
         int count1 = modMaterial1.method_7947();
         int optimal1 = 4 + random.nextInt(5);
         value += 15.0F * (float)Math.exp(-0.08 * Math.pow(count1 - optimal1, 2.0));
      }

      if (!modMaterial2.method_7960()) {
         int count2 = modMaterial2.method_7947();
         int optimal2 = 4 + random.nextInt(5);
         value += 15.0F * (float)Math.exp(-0.08 * Math.pow(count2 - optimal2, 2.0));
      }

      if (!vanillaMaterial.method_7960()) {
         int count3 = vanillaMaterial.method_7947();
         int optimal3 = 3 + random.nextInt(4);
         value += 8.0F * (float)Math.exp(-0.12 * Math.pow(count3 - optimal3, 2.0));
      }

      return value;
   }

   private ItemStack getModPotionResult() {
      ItemStack modMaterial1 = (ItemStack)this.inventory.get(0);
      ItemStack modMaterial2 = (ItemStack)this.inventory.get(1);
      ItemStack vanillaMaterial = (ItemStack)this.inventory.get(2);
      ItemStack goldContainer = (ItemStack)this.inventory.get(3);
      String ghostType = this.getGhostTypeFromContainer(goldContainer);
      return this.generateModPotion(modMaterial1, modMaterial2, vanillaMaterial, ghostType);
   }

   private String getGhostTypeFromContainer(ItemStack container) {
      NbtCompound nbt = container.method_7948();
      if (nbt.method_10577("HasGhost") && nbt.method_10545("ContainedGhost")) {
         NbtCompound ghostData = nbt.method_10562("ContainedGhost");
         if (ghostData.method_10545("id")) {
            String ghostId = ghostData.method_10558("id");
            if (ghostId.contains(":")) {
               return ghostId.split(":")[1];
            }

            return ghostId;
         }
      }

      return "default";
   }

   private ItemStack generateModPotion(ItemStack modMaterial1, ItemStack modMaterial2, ItemStack vanillaMaterial, String ghostType) {
      Map<String, Float> potionProbabilities = new HashMap<>();

      for (Entry<String, SpiritBrewingStandBlockEntity.PotionConfig> entry : POTION_CONFIGS.entrySet()) {
         potionProbabilities.put(entry.getKey(), entry.getValue().probability);
      }

      this.adjustProbabilitiesByMaterial(potionProbabilities, modMaterial1, modMaterial2, vanillaMaterial);
      float materialValue = this.calculateMaterialValue();
      float valueMultiplier = 0.8F + materialValue / 50.0F;

      for (Entry<String, Float> entry : potionProbabilities.entrySet()) {
         potionProbabilities.put(entry.getKey(), entry.getValue() * valueMultiplier);
      }

      this.adjustProbabilitiesByGhostType(potionProbabilities, ghostType);
      String selectedPotion = this.selectPotionByProbability(potionProbabilities);
      return this.createModPotionStack(selectedPotion);
   }

   private void adjustProbabilitiesByMaterial(Map<String, Float> probabilities, ItemStack modMaterial1, ItemStack modMaterial2, ItemStack vanillaMaterial) {
      Item material1 = modMaterial1.method_7909();
      Item material2 = modMaterial2.method_7909();
      Item vanilla = vanillaMaterial.method_7909();
      Random random = new Random();
      if (material1 == ModItems.DEFILED_FRAGMENT) {
         int count1 = modMaterial1.method_7947();
         int optimal1 = 4 + random.nextInt(5);
         float boost1 = (float)(5.0 * Math.exp(-0.08 * Math.pow(count1 - optimal1, 2.0)));
         probabilities.put("spirit_erosion_potion", probabilities.get("spirit_erosion_potion") + boost1);
      }

      if (material2 == ModItems.GHOST_CHINESE_MEDICINE) {
         int count2 = modMaterial2.method_7947();
         int optimal2 = 4 + random.nextInt(5);
         float boost2 = (float)(5.0 * Math.exp(-0.08 * Math.pow(count2 - optimal2, 2.0)));
         probabilities.put("ghost_suppression_potion", probabilities.get("ghost_suppression_potion") + boost2);
      }

      if (vanilla == Items.field_8695) {
         int count3 = vanillaMaterial.method_7947();
         int optimal3 = 3 + random.nextInt(4);
         float boost3 = (float)(5.0 * Math.exp(-0.12 * Math.pow(count3 - optimal3, 2.0)));
         probabilities.put("spirit_immunity_potion", probabilities.get("spirit_immunity_potion") + boost3);
      }
   }

   private void adjustProbabilitiesByGhostType(Map<String, Float> probabilities, String ghostType) {
      for (Entry<String, SpiritBrewingStandBlockEntity.PotionConfig> entry : POTION_CONFIGS.entrySet()) {
         String potionId = entry.getKey();
         SpiritBrewingStandBlockEntity.PotionConfig config = entry.getValue();
         if (config.ghostTypes.contains(ghostType) && config.ghostProbabilityBoost > 0.0F) {
            probabilities.put(potionId, probabilities.get(potionId) + config.ghostProbabilityBoost);
         }
      }
   }

   private String selectPotionByProbability(Map<String, Float> probabilities) {
      Random random = new Random();
      float totalProb = 0.0F;

      for (Float prob : probabilities.values()) {
         totalProb += prob;
      }

      float roll = random.nextFloat() * totalProb;
      float cumulative = 0.0F;

      for (Entry<String, Float> entry : probabilities.entrySet()) {
         cumulative += entry.getValue();
         if (roll < cumulative) {
            return entry.getKey();
         }
      }

      return "spirit_erosion_potion";
   }

   private ItemStack createModPotionStack(String potionId) {
      SpiritBrewingStandBlockEntity.PotionConfig config = POTION_CONFIGS.get(potionId);
      return config != null && config.potionItem != null ? new ItemStack(config.potionItem) : new ItemStack(ModItems.SPIRIT_EROSION_POTION);
   }

   public static void tick(World world, BlockPos pos, BlockState state, SpiritBrewingStandBlockEntity blockEntity) {
      if (!blockEntity.isBrewing && hasFireBelow(world, pos)) {
         if (blockEntity.canStartBrewing()) {
            blockEntity.startBrewing();
            world.method_8501(pos, (BlockState)state.method_11657(SpiritBrewingStandBlock.LIT, true));
         } else {
            BlockPos belowPos = pos.method_10074();
            world.method_8501(belowPos, Blocks.field_10124.method_9564());
         }
      } else if (blockEntity.isBrewing && !hasFireBelow(world, pos)) {
         blockEntity.stopBrewing();
         world.method_8501(pos, (BlockState)state.method_11657(SpiritBrewingStandBlock.LIT, false));
      }

      if (blockEntity.isBrewing) {
         blockEntity.brewingTime--;
         if (blockEntity.brewingTime <= 0) {
            blockEntity.finishBrewing();
            world.method_8501(pos, (BlockState)state.method_11657(SpiritBrewingStandBlock.LIT, false));
         }
      }
   }

   private static boolean hasFireBelow(World world, BlockPos pos) {
      BlockPos belowPos = pos.method_10074();
      BlockState belowState = world.method_8320(belowPos);
      return belowState.method_27852(Blocks.field_10036) || belowState.method_27852(Blocks.field_22089);
   }

   private void finishBrewing() {
      if (!this.brewingResult.method_7960() && !((ItemStack)this.inventory.get(4)).method_7960()) {
         ((ItemStack)this.inventory.get(4)).method_7934(1);
         this.inventory.set(4, this.brewingResult);
      }

      this.inventory.set(0, ItemStack.field_8037);
      this.inventory.set(1, ItemStack.field_8037);
      this.inventory.set(2, ItemStack.field_8037);
      this.inventory.set(3, new ItemStack(ModItems.GOLDEN_CONTAINER));
      this.isBrewing = false;
      this.brewingTime = 0;
      this.maxBrewingTime = 0;
      this.brewingResult = ItemStack.field_8037;
      this.method_5431();
   }

   public void method_11014(NbtCompound nbt) {
      super.method_11014(nbt);
      Inventories.method_5429(nbt, this.inventory);
      this.brewingTime = nbt.method_10550("BrewingTime");
      this.maxBrewingTime = nbt.method_10550("MaxBrewingTime");
      this.isBrewing = nbt.method_10577("IsBrewing");
   }

   public void method_11007(NbtCompound nbt) {
      super.method_11007(nbt);
      Inventories.method_5426(nbt, this.inventory);
      nbt.method_10569("BrewingTime", this.brewingTime);
      nbt.method_10569("MaxBrewingTime", this.maxBrewingTime);
      nbt.method_10556("IsBrewing", this.isBrewing);
   }

   public DefaultedList<ItemStack> getItems() {
      return this.inventory;
   }

   public boolean isBrewing() {
      return this.isBrewing;
   }

   public int method_5439() {
      return this.inventory.size();
   }

   public boolean method_5442() {
      for (ItemStack stack : this.inventory) {
         if (!stack.method_7960()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack method_5438(int slot) {
      return (ItemStack)this.inventory.get(slot);
   }

   public ItemStack method_5434(int slot, int amount) {
      return this.isBrewing ? ItemStack.field_8037 : Inventories.method_5430(this.inventory, slot, amount);
   }

   public ItemStack method_5441(int slot) {
      return this.isBrewing ? ItemStack.field_8037 : Inventories.method_5428(this.inventory, slot);
   }

   public void method_5447(int slot, ItemStack stack) {
      if (!this.isBrewing) {
         if (slot == 4) {
            if (!stack.method_7960()) {
               stack.method_7939(1);
            }
         } else if (stack.method_7947() > this.method_5444()) {
            stack.method_7939(this.method_5444());
         }

         this.inventory.set(slot, stack);
      }
   }

   public boolean method_5443(PlayerEntity player) {
      return true;
   }

   public void method_5448() {
      if (!this.isBrewing) {
         this.inventory.clear();
      }
   }

   public Text method_5476() {
      return Text.method_43471("container.smfs.spirit_brewing_stand");
   }

   @Nullable
   public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
      return new SpiritBrewingStandScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "controller", 0, state -> {
         state.getController().setAnimation(RawAnimation.begin().thenLoop("animation.unknown.new"));
         return PlayState.CONTINUE;
      }));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   static {
      POTION_CONFIGS.put("ghost_wind_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 12000, ModItems.GHOST_WIND_POTION, List.of("ghost_wind")));
      POTION_CONFIGS.put("ghost_knock_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 12000, ModItems.GHOST_KNOCK_POTION));
      POTION_CONFIGS.put("lost_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 12000, ModItems.LOST_POTION));
      POTION_CONFIGS.put("thick_fog_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 12000, ModItems.THICK_FOG_POTION, List.of("fog_ghost")));
      POTION_CONFIGS.put("starving_ghost_curse_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 12000, ModItems.STARVING_GHOST_CURSE_POTION));
      POTION_CONFIGS.put("red_ghost_domain_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 12000, ModItems.RED_GHOST_DOMAIN_POTION));
      POTION_CONFIGS.put("green_ghost_domain_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 12000, ModItems.GREEN_GHOST_DOMAIN_POTION));
      POTION_CONFIGS.put("blue_ghost_domain_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 12000, ModItems.BLUE_GHOST_DOMAIN_POTION));
      POTION_CONFIGS.put("gray_ghost_domain_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 12000, ModItems.GRAY_GHOST_DOMAIN_POTION));
      POTION_CONFIGS.put("purple_ghost_domain_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 12000, ModItems.PURPLE_GHOST_DOMAIN_POTION));
      POTION_CONFIGS.put("golden_ghost_domain_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 12000, ModItems.GOLDEN_GHOST_DOMAIN_POTION));
      POTION_CONFIGS.put("black_ghost_domain_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 12000, ModItems.BLACK_GHOST_DOMAIN_POTION));
      POTION_CONFIGS.put("cyan_ghost_domain_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 12000, ModItems.CYAN_GHOST_DOMAIN_POTION));
      POTION_CONFIGS.put("mark_curse_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 12000, ModItems.MARK_CURSE_POTION));
      POTION_CONFIGS.put("scapegoat_mark_potion", new SpiritBrewingStandBlockEntity.PotionConfig(8.0F, 1200, ModItems.SCAPEGOAT_MARK_POTION));
      POTION_CONFIGS.put("trauma_curse_potion", new SpiritBrewingStandBlockEntity.PotionConfig(5.0F, 36000, ModItems.TRAUMA_CURSE_POTION));
      POTION_CONFIGS.put("illusion_curse_potion", new SpiritBrewingStandBlockEntity.PotionConfig(5.0F, 36000, ModItems.ILLUSION_CURSE_POTION));
      POTION_CONFIGS.put("silence_potion", new SpiritBrewingStandBlockEntity.PotionConfig(5.0F, 36000, ModItems.SILENCE_POTION));
      POTION_CONFIGS.put(
         "ghost_pressure_potion", new SpiritBrewingStandBlockEntity.PotionConfig(5.0F, 36000, ModItems.GHOST_PRESSURE_POTION, List.of("ghost_pressure"))
      );
      POTION_CONFIGS.put(
         "fatal_poison_potion", new SpiritBrewingStandBlockEntity.PotionConfig(5.0F, 36000, ModItems.FATAL_POISON_POTION, List.of("burn_ghost"))
      );
      POTION_CONFIGS.put(
         "spirit_erosion_potion", new SpiritBrewingStandBlockEntity.PotionConfig(5.0F, 36000, ModItems.SPIRIT_EROSION_POTION, List.of("plague_ghost"))
      );
      POTION_CONFIGS.put(
         "ghost_suppression_potion", new SpiritBrewingStandBlockEntity.PotionConfig(5.0F, 36000, ModItems.GHOST_SUPPRESSION_POTION, List.of("ghost_pressure"))
      );
      POTION_CONFIGS.put(
         "spirit_immunity_potion", new SpiritBrewingStandBlockEntity.PotionConfig(5.0F, 36000, ModItems.SPIRIT_IMMUNITY_POTION, List.of("untouchable_ghost"))
      );
      POTION_CONFIGS.put(
         "spirit_surge_potion", new SpiritBrewingStandBlockEntity.PotionConfig(3.0F, 60000, ModItems.SPIRIT_SURGE_POTION, List.of("giant_shadow_ghost"))
      );
      POTION_CONFIGS.put("deafness_potion", new SpiritBrewingStandBlockEntity.PotionConfig(3.0F, 60000, ModItems.DEAFNESS_POTION));
      POTION_CONFIGS.put("knocking_curse_potion", new SpiritBrewingStandBlockEntity.PotionConfig(3.0F, 60000, ModItems.KNOCKING_CURSE_POTION));
      POTION_CONFIGS.put(
         "purification_potion", new SpiritBrewingStandBlockEntity.PotionConfig(3.0F, 60000, ModItems.PURIFICATION_POTION, List.of("ghost_officer"))
      );
      POTION_CONFIGS.put("ghost_deadlock_potion", new SpiritBrewingStandBlockEntity.PotionConfig(3.0F, 60000, ModItems.GHOST_DEADLOCK_POTION));
      POTION_CONFIGS.put("revival_suppression_potion", new SpiritBrewingStandBlockEntity.PotionConfig(3.0F, 60000, ModItems.REVIVAL_SUPPRESSION_POTION));
   }

   private static class PotionConfig {
      float probability;
      int brewingTime;
      Item potionItem;
      List<String> ghostTypes;
      float ghostProbabilityBoost;

      PotionConfig(float probability, int brewingTime, Item potionItem) {
         this(probability, brewingTime, potionItem, new ArrayList<>(), 0.0F);
      }

      PotionConfig(float probability, int brewingTime, Item potionItem, List<String> ghostTypes) {
         this(probability, brewingTime, potionItem, ghostTypes, 5.0F);
      }

      PotionConfig(float probability, int brewingTime, Item potionItem, List<String> ghostTypes, float ghostProbabilityBoost) {
         this.probability = probability;
         this.brewingTime = brewingTime;
         this.potionItem = potionItem;
         this.ghostTypes = ghostTypes;
         this.ghostProbabilityBoost = ghostProbabilityBoost;
      }
   }
}
