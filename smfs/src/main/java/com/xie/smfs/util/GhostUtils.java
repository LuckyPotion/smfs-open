package com.xie.smfs.util;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.ghost.BlockGhostEntity;
import com.xie.smfs.entity.ghost.BoxGhostEntity;
import com.xie.smfs.entity.ghost.BurnGhostEntity;
import com.xie.smfs.entity.ghost.ClothesGhostEntity;
import com.xie.smfs.entity.ghost.CropGhostEntity;
import com.xie.smfs.entity.ghost.CryingGhostEntity;
import com.xie.smfs.entity.ghost.DeathSightGhostEntity;
import com.xie.smfs.entity.ghost.DitouGhostEntity;
import com.xie.smfs.entity.ghost.DoorGhostEntity;
import com.xie.smfs.entity.ghost.FogGhostEntity;
import com.xie.smfs.entity.ghost.FoodGhostEntity;
import com.xie.smfs.entity.ghost.GanshiBrideGhostEntity;
import com.xie.smfs.entity.ghost.GhostMerchantEntity;
import com.xie.smfs.entity.ghost.GhostOfficerEntity;
import com.xie.smfs.entity.ghost.GhostPressureEntity;
import com.xie.smfs.entity.ghost.GhostShadowHeadEntity;
import com.xie.smfs.entity.ghost.GhostSmokeEntity;
import com.xie.smfs.entity.ghost.GhostWindEntity;
import com.xie.smfs.entity.ghost.GiantShadowGhostEntity;
import com.xie.smfs.entity.ghost.GongGhostEntity;
import com.xie.smfs.entity.ghost.GraveEarthGhostEntity;
import com.xie.smfs.entity.ghost.JumpGhostEntity;
import com.xie.smfs.entity.ghost.LostGhostEntity;
import com.xie.smfs.entity.ghost.MineralGhostEntity;
import com.xie.smfs.entity.ghost.PlagueGhostEntity;
import com.xie.smfs.entity.ghost.PuppetGhostEntity;
import com.xie.smfs.entity.ghost.QiaomenGhostEntity;
import com.xie.smfs.entity.ghost.ShadowGhostEntity;
import com.xie.smfs.entity.ghost.SilentGhostEntity;
import com.xie.smfs.entity.ghost.SneakGhostEntity;
import com.xie.smfs.entity.ghost.StepGhostEntity;
import com.xie.smfs.entity.ghost.SuonaGhostEntity;
import com.xie.smfs.entity.ghost.TaitouGhostEntity;
import com.xie.smfs.entity.ghost.TrashGhostEntity;
import com.xie.smfs.entity.ghost.UntouchableGhostEntity;
import com.xie.smfs.entity.ghost.VillagerGhostEntity;
import com.xie.smfs.entity.ghost.WaterGhostEntity;
import com.xie.smfs.entity.ghost.XinKaiGhostEntity;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.registry.ModEntities;
import com.xie.smfs.registry.ModItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostUtils {
   private static final Logger LOGGER = LoggerFactory.getLogger(GhostUtils.class);
   private static final Map<String, Function<NbtCompound, ItemStack>> ADDON_GHOST_MAPPINGS = new HashMap<>();
   private static final Map<String, Function<String, String>> ADDON_DISPLAY_NAME_MAPPINGS = new HashMap<>();
   private static final Map<String, Function<String, List<String>>> ADDON_ABILITY_MAPPINGS = new HashMap<>();
   private static final Map<String, Function<World, GhostEntity>> ADDON_ENTITY_CREATION_MAPPINGS = new HashMap<>();
   private static final Map<String, Item> GHOST_TYPE_TO_ITEM_MAP = new HashMap<>();
   private static final Map<String, Function<World, GhostEntity>> GHOST_TYPE_TO_ENTITY_MAP = new HashMap<>();
   private static final Map<EntityType<?>, String> ENTITY_TYPE_TO_GHOST_TYPE = new HashMap<>();
   private static final Map<String, String> GHOST_TYPE_TO_DISPLAY_NAME_MAP = new HashMap<>();
   public static final Item[] GHOST_CONTROL_ITEMS = new Item[]{
      ModItems.TAITOU_GHOST,
      ModItems.DITOU_GHOST,
      ModItems.FOOD_GHOST,
      ModItems.BLOCK_GHOST,
      ModItems.BOX_GHOST,
      ModItems.VILLAGER_GHOST,
      ModItems.JUMP_GHOST,
      ModItems.UNTOUCHABLE_GHOST,
      ModItems.DEATH_SIGHT_GHOST,
      ModItems.QIAOMEN_GHOST,
      ModItems.GHOST_MERCHANT,
      ModItems.FOG_GHOST,
      ModItems.LOST_GHOST,
      ModItems.GHOST_FIRE,
      ModItems.GHOST_PRESSURE,
      ModItems.SILENT_GHOST,
      ModItems.SCAPEGOAT_GHOST,
      ModItems.GHOST_WIND,
      ModItems.GHOST_BLOOD,
      ModItems.GHOST_OFFICER,
      ModItems.CROP_GHOST,
      ModItems.STEP_GHOST,
      ModItems.GRAVE_EARTH_GHOST,
      ModItems.TRASH_GHOST,
      ModItems.WATER_GHOST,
      ModItems.CRYING_GHOST,
      ModItems.SUONA_GHOST,
      ModItems.GONG_GHOST,
      ModItems.FUNERAL_MUSIC_GHOST,
      ModItems.WISH_GHOST,
      ModItems.CANDY_GHOST,
      ModItems.DOOR_GHOST,
      ModItems.GHOST_SMOKE,
      ModItems.PLAGUE_GHOST,
      ModItems.MINERAL_GHOST,
      ModItems.SHADOW_GHOST,
      ModItems.COMPLETE_SHADOW_GHOST,
      ModItems.GHOST_SHADOW_HEAD,
      ModItems.GHOST_FIST,
      ModItems.SNEAK_GHOST,
      ModItems.CLOTHES_GHOST,
      ModItems.PUPPET_GHOST,
      ModItems.GHOST_DREAM
   };
   private static final Random RANDOM = new Random();
   private static final Map<String, Item> VANILLA_ITEM_MAP = new HashMap<>();
   private static final Map<String, Item> MOD_ITEM_MAP = new HashMap<>();
   private static final Map<String, String> ITEM_DISPLAY_NAME_MAP = new HashMap<>();

   private static void initializeGhostItemMappings() {
      GHOST_TYPE_TO_ITEM_MAP.put("food_ghost", ModItems.FOOD_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("crop_ghost", ModItems.CROP_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("candy_ghost", ModItems.CANDY_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("taitou_ghost", ModItems.TAITOU_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("ditou_ghost", ModItems.DITOU_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("qiaomen_ghost", ModItems.QIAOMEN_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("jump_ghost", ModItems.JUMP_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("step_ghost", ModItems.STEP_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("grave_earth_ghost", ModItems.GRAVE_EARTH_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("block_ghost", ModItems.BLOCK_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("box_ghost", ModItems.BOX_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("trash_ghost", ModItems.TRASH_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("water_ghost", ModItems.WATER_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("untouchable_ghost", ModItems.UNTOUCHABLE_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("death_sight_ghost", ModItems.DEATH_SIGHT_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("lost_ghost", ModItems.LOST_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("fog_ghost", ModItems.FOG_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("ghost_fire", ModItems.GHOST_FIRE);
      GHOST_TYPE_TO_ITEM_MAP.put("burn_ghost", ModItems.GHOST_FIRE);
      GHOST_TYPE_TO_ITEM_MAP.put("ghost_fist", ModItems.GHOST_FIST);
      GHOST_TYPE_TO_ITEM_MAP.put("ghost_wind", ModItems.GHOST_WIND);
      GHOST_TYPE_TO_ITEM_MAP.put("ghost_blood", ModItems.GHOST_BLOOD);
      GHOST_TYPE_TO_ITEM_MAP.put("ghost_pressure", ModItems.GHOST_PRESSURE);
      GHOST_TYPE_TO_ITEM_MAP.put("villager_ghost", ModItems.VILLAGER_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("ghost_merchant", ModItems.GHOST_MERCHANT);
      GHOST_TYPE_TO_ITEM_MAP.put("giant_shadow_ghost", ModItems.GIANT_SHADOW_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("ganshi_bride_ghost", ModItems.GANSHI_BRIDE_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("crying_ghost", ModItems.CRYING_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("suona_ghost", ModItems.SUONA_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("gong_ghost", ModItems.GONG_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("funeral_music_ghost", ModItems.FUNERAL_MUSIC_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("silent_ghost", ModItems.SILENT_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("silent_ghost_eye", ModItems.SILENT_GHOST_EYE);
      GHOST_TYPE_TO_ITEM_MAP.put("wish_ghost", ModItems.WISH_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("scapegoat_ghost", ModItems.SCAPEGOAT_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("ghost_officer", ModItems.GHOST_OFFICER);
      GHOST_TYPE_TO_ITEM_MAP.put("plague_ghost", ModItems.PLAGUE_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("mineral_ghost", ModItems.MINERAL_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("shadow_ghost", ModItems.SHADOW_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("complete_shadow_ghost", ModItems.COMPLETE_SHADOW_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("ghost_shadow_head", ModItems.GHOST_SHADOW_HEAD);
      GHOST_TYPE_TO_ITEM_MAP.put("door_ghost", ModItems.DOOR_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("ghost_smoke", ModItems.GHOST_SMOKE);
      GHOST_TYPE_TO_ITEM_MAP.put("sneak_ghost", ModItems.SNEAK_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("clothes_ghost", ModItems.CLOTHES_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("puppet_ghost", ModItems.PUPPET_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("xinkai_ghost", ModItems.XINKAI_GHOST);
      GHOST_TYPE_TO_ITEM_MAP.put("ghost_lake", ModItems.GHOST_LAKE);
      GHOST_TYPE_TO_ITEM_MAP.put("ghost_dream", ModItems.GHOST_DREAM);
      VANILLA_ITEM_MAP.put("cookie", Items.field_8423);
      VANILLA_ITEM_MAP.put("gold_ingot", Items.field_8695);
      VANILLA_ITEM_MAP.put("iron_ingot", Items.field_8620);
      VANILLA_ITEM_MAP.put("diamond", Items.field_8477);
      VANILLA_ITEM_MAP.put("experience_bottle", Items.field_8287);
      VANILLA_ITEM_MAP.put("golden_helmet", Items.field_8862);
      VANILLA_ITEM_MAP.put("golden_chestplate", Items.field_8678);
      VANILLA_ITEM_MAP.put("golden_leggings", Items.field_8416);
      VANILLA_ITEM_MAP.put("golden_boots", Items.field_8753);
      VANILLA_ITEM_MAP.put("enchanted_golden_apple", Items.field_8367);
      VANILLA_ITEM_MAP.put("golden_apple", Items.field_8463);
      VANILLA_ITEM_MAP.put("totem_of_undying", Items.field_8288);
      VANILLA_ITEM_MAP.put("iron_sword", Items.field_8371);
      VANILLA_ITEM_MAP.put("iron_leggings", Items.field_8396);
      VANILLA_ITEM_MAP.put("iron_chestplate", Items.field_8523);
      VANILLA_ITEM_MAP.put("diamond_sword", Items.field_8802);
      VANILLA_ITEM_MAP.put("diamond_chestplate", Items.field_8058);
      VANILLA_ITEM_MAP.put("diamond_leggings", Items.field_8348);
      MOD_ITEM_MAP.put("control_slot", ModItems.CONTROL_SLOT);
      MOD_ITEM_MAP.put("red_ghost_candle", ModItems.RED_GHOST_CANDLE);
      MOD_ITEM_MAP.put("mystery_coordinate", ModItems.MYSTERY_INFO);
      MOD_ITEM_MAP.put("ghost_porcelain", ModItems.GHOST_PORCELAIN);
      MOD_ITEM_MAP.put("ghost_pearl", ModItems.GHOST_BUDDHA_BEADS);
      MOD_ITEM_MAP.put("ghost_firewood_axe", ModItems.RUSTY_FIREWOOD_KNIFE);
      MOD_ITEM_MAP.put("rich_mall_mystery_info", ModItems.FUREN_MALL_MYSTERY_INFO);
      MOD_ITEM_MAP.put("tame_slot", ModItems.CONTROL_SLOT);
      MOD_ITEM_MAP.put("chinese_medicine_shop_mystery_info", ModItems.CHINESE_MEDICINE_SHOP_MYSTERY_INFO);
      initializeItemDisplayNames();
   }

   private static void initializeItemDisplayNames() {
      ITEM_DISPLAY_NAME_MAP.put("cookie", "饼干");
      ITEM_DISPLAY_NAME_MAP.put("gold_ingot", "金锭");
      ITEM_DISPLAY_NAME_MAP.put("iron_ingot", "铁锭");
      ITEM_DISPLAY_NAME_MAP.put("diamond", "钻石");
      ITEM_DISPLAY_NAME_MAP.put("experience_bottle", "经验瓶");
      ITEM_DISPLAY_NAME_MAP.put("golden_helmet", "黄金头盔");
      ITEM_DISPLAY_NAME_MAP.put("golden_chestplate", "黄金胸甲");
      ITEM_DISPLAY_NAME_MAP.put("golden_leggings", "黄金护腿");
      ITEM_DISPLAY_NAME_MAP.put("golden_boots", "黄金靴子");
      ITEM_DISPLAY_NAME_MAP.put("iron_sword", "铁剑");
      ITEM_DISPLAY_NAME_MAP.put("iron_leggings", "铁护腿");
      ITEM_DISPLAY_NAME_MAP.put("iron_chestplate", "铁胸甲");
      ITEM_DISPLAY_NAME_MAP.put("diamond_sword", "钻石剑");
      ITEM_DISPLAY_NAME_MAP.put("diamond_chestplate", "钻石胸甲");
      ITEM_DISPLAY_NAME_MAP.put("diamond_leggings", "钻石护腿");
      ITEM_DISPLAY_NAME_MAP.put("enchanted_golden_apple", "附魔金苹果");
      ITEM_DISPLAY_NAME_MAP.put("golden_apple", "金苹果");
      ITEM_DISPLAY_NAME_MAP.put("totem_of_undying", "不死图腾");
      ITEM_DISPLAY_NAME_MAP.put("control_slot", "驾驭名额");
      ITEM_DISPLAY_NAME_MAP.put("red_ghost_candle", "红色鬼烛");
      ITEM_DISPLAY_NAME_MAP.put("mystery_coordinate", "神秘坐标");
      ITEM_DISPLAY_NAME_MAP.put("ghost_porcelain", "鬼瓷");
      ITEM_DISPLAY_NAME_MAP.put("ghost_pearl", "鬼珍珠");
      ITEM_DISPLAY_NAME_MAP.put("ghost_firewood_axe", "鬼柴刀");
      ITEM_DISPLAY_NAME_MAP.put("rich_mall_mystery_info", "富仁商场神秘信息");
      ITEM_DISPLAY_NAME_MAP.put("chinese_medicine_shop_mystery_info", "中药铺神秘信息");
      ITEM_DISPLAY_NAME_MAP.put("tame_slot", "驾驭名额");
   }

   private static void initializeGhostEntityMappings() {
      GHOST_TYPE_TO_ENTITY_MAP.put("food_ghost", world -> new FoodGhostEntity(ModEntities.FOOD_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.FOOD_GHOST, "food_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("crop_ghost", world -> new CropGhostEntity(ModEntities.CROP_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.CROP_GHOST, "crop_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("taitou_ghost", world -> new TaitouGhostEntity(ModEntities.TAITOU_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.TAITOU_GHOST, "taitou_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("ditou_ghost", world -> new DitouGhostEntity(ModEntities.DITOU_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.DITOU_GHOST, "ditou_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("qiaomen_ghost", world -> new QiaomenGhostEntity(ModEntities.QIAOMEN_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.QIAOMEN_GHOST, "qiaomen_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("jump_ghost", world -> new JumpGhostEntity(ModEntities.JUMP_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.JUMP_GHOST, "jump_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("step_ghost", world -> new StepGhostEntity(ModEntities.STEP_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.STEP_GHOST, "step_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("grave_earth_ghost", world -> new GraveEarthGhostEntity(ModEntities.GRAVE_EARTH_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.GRAVE_EARTH_GHOST, "grave_earth_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("block_ghost", world -> new BlockGhostEntity(ModEntities.BLOCK_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.BLOCK_GHOST, "block_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("box_ghost", world -> new BoxGhostEntity(ModEntities.BOX_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.BOX_GHOST, "box_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("trash_ghost", world -> new TrashGhostEntity(ModEntities.TRASH_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.TRASH_GHOST, "trash_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("water_ghost", world -> new WaterGhostEntity(ModEntities.WATER_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.WATER_GHOST, "water_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("untouchable_ghost", world -> new UntouchableGhostEntity(ModEntities.UNTOUCHABLE_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.UNTOUCHABLE_GHOST, "untouchable_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("death_sight_ghost", world -> new DeathSightGhostEntity(ModEntities.DEATH_SIGHT_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.DEATH_SIGHT_GHOST, "death_sight_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("lost_ghost", world -> new LostGhostEntity(ModEntities.LOST_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.LOST_GHOST, "lost_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("fog_ghost", world -> new FogGhostEntity(ModEntities.FOG_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.FOG_GHOST, "fog_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("ghost_pressure", world -> new GhostPressureEntity(ModEntities.GHOST_PRESSURE, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.GHOST_PRESSURE, "ghost_pressure");
      GHOST_TYPE_TO_ENTITY_MAP.put("ghost_wind", world -> new GhostWindEntity(ModEntities.GHOST_WIND, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.GHOST_WIND, "ghost_wind");
      GHOST_TYPE_TO_ENTITY_MAP.put("villager_ghost", world -> new VillagerGhostEntity(ModEntities.VILLAGER_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.VILLAGER_GHOST, "villager_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("ghost_merchant", world -> new GhostMerchantEntity(ModEntities.GHOST_MERCHANT, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.GHOST_MERCHANT, "ghost_merchant");
      GHOST_TYPE_TO_ENTITY_MAP.put("giant_shadow_ghost", world -> new GiantShadowGhostEntity(ModEntities.GIANT_SHADOW_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.GIANT_SHADOW_GHOST, "giant_shadow_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("ganshi_bride_ghost", world -> new GanshiBrideGhostEntity(ModEntities.GANSHI_BRIDE_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.GANSHI_BRIDE_GHOST, "ganshi_bride_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("crying_ghost", world -> new CryingGhostEntity(ModEntities.CRYING_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.CRYING_GHOST, "crying_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("suona_ghost", world -> new SuonaGhostEntity(ModEntities.SUONA_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.SUONA_GHOST, "suona_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("gong_ghost", world -> new GongGhostEntity(ModEntities.GONG_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.GONG_GHOST, "gong_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("silent_ghost", world -> new SilentGhostEntity(ModEntities.SILENT_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.SILENT_GHOST, "silent_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("ghost_fire", world -> new BurnGhostEntity(ModEntities.BURN_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.BURN_GHOST, "ghost_fire");
      GHOST_TYPE_TO_ENTITY_MAP.put("burn_ghost", world -> new BurnGhostEntity(ModEntities.BURN_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.BURN_GHOST, "burn_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("ghost_officer", world -> new GhostOfficerEntity(ModEntities.GHOST_OFFICER, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.GHOST_OFFICER, "ghost_officer");
      GHOST_TYPE_TO_ENTITY_MAP.put("plague_ghost", world -> new PlagueGhostEntity(ModEntities.PLAGUE_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.PLAGUE_GHOST, "plague_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("mineral_ghost", world -> new MineralGhostEntity(ModEntities.MINERAL_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.MINERAL_GHOST, "mineral_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("shadow_ghost", world -> new ShadowGhostEntity(ModEntities.SHADOW_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.SHADOW_GHOST, "shadow_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("ghost_shadow_head", world -> new GhostShadowHeadEntity(ModEntities.GHOST_SHADOW_HEAD, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.GHOST_SHADOW_HEAD, "ghost_shadow_head");
      GHOST_TYPE_TO_ENTITY_MAP.put("door_ghost", world -> new DoorGhostEntity(ModEntities.DOOR_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.DOOR_GHOST, "door_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("ghost_smoke", world -> new GhostSmokeEntity(ModEntities.GHOST_SMOKE, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.GHOST_SMOKE, "ghost_smoke");
      GHOST_TYPE_TO_ENTITY_MAP.put("sneak_ghost", world -> new SneakGhostEntity(ModEntities.SNEAK_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.SNEAK_GHOST, "sneak_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("clothes_ghost", world -> new ClothesGhostEntity(ModEntities.CLOTHES_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.CLOTHES_GHOST, "clothes_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("puppet_ghost", world -> new PuppetGhostEntity(ModEntities.PUPPET_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.PUPPET_GHOST, "puppet_ghost");
      GHOST_TYPE_TO_ENTITY_MAP.put("xinkai_ghost", world -> new XinKaiGhostEntity(ModEntities.XINKAI_GHOST, world));
      ENTITY_TYPE_TO_GHOST_TYPE.put(ModEntities.XINKAI_GHOST, "xinkai_ghost");
   }

   private static void initializeGhostDisplayNameMappings() {
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("silent_ghost_eye", "沉寂的鬼眼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("ghost_fire", "鬼火");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("burn_ghost", "鬼火");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("ghost_fist", "鬼拳");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("fog_ghost", "鬼雾");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("taitou_ghost", "抬头鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("ditou_ghost", "低头鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("food_ghost", "食物鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("block_ghost", "方块鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("box_ghost", "开箱鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("villager_ghost", "村民鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("jump_ghost", "跳跃鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("untouchable_ghost", "不可触摸鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("death_sight_ghost", "死亡凝视鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("qiaomen_ghost", "敲门鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("ghost_merchant", "鬼商人");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("lost_ghost", "遗忘鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("crop_ghost", "作物鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("step_ghost", "踩人鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("grave_earth_ghost", "坟土鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("trash_ghost", "垃圾鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("water_ghost", "水鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("giant_shadow_ghost", "无头鬼影");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("ganshi_bride_ghost", "干尸新娘");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("crying_ghost", "哭丧鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("suona_ghost", "唢呐鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("gong_ghost", "敲锣鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("funeral_music_ghost", "丧乐鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("ghost_wind", "鬼风");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("ghost_blood", "鬼血");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("ghost_pressure", "鬼压人");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("wish_ghost", "许愿鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("scapegoat_ghost", "替死鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("candy_ghost", "糖果鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("silent_ghost", "静悄悄");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("ghost_officer", "鬼差");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("invisible_ghost", "不可视鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("plague_ghost", "瘟鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("mineral_ghost", "矿物鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("shadow_ghost", "黑影鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("complete_shadow_ghost", "完整鬼影");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("ghost_shadow_head", "鬼影头");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("door_ghost", "开门鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("ghost_smoke", "鬼烟");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("ghost_lake", "鬼湖");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("sneak_ghost", "潜行鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("clothes_ghost", "裁缝鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("puppet_ghost", "木偶鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("xinkai_ghost", "新凯鬼");
      GHOST_TYPE_TO_DISPLAY_NAME_MAP.put("ghost_dream", "鬼梦");
   }

   public static String getGhostDisplayName(String ghostType) {
      if (ghostType != null && !ghostType.isEmpty()) {
         Function<String, String> addonMapping = ADDON_DISPLAY_NAME_MAPPINGS.get(ghostType);
         if (addonMapping != null) {
            try {
               String displayName = addonMapping.apply(ghostType);
               if (displayName != null && !displayName.isEmpty()) {
                  LOGGER.info("使用附属模组映射获取显示名称: {}", ghostType);
                  return displayName;
               }
            } catch (Exception e) {
               LOGGER.error("附属模组映射获取显示名称失败: {}", ghostType, e);
            }
         }

         String displayName = GHOST_TYPE_TO_DISPLAY_NAME_MAP.get(ghostType);
         if (displayName != null) {
            return displayName;
         }

         LOGGER.warn("未知的鬼类型: {}", ghostType);
         return "未知鬼魂";
      } else {
         return "未知鬼魂";
      }
   }

   public static String getGhostDisplayName(EntityType<?> ghostType) {
      if (ghostType == null) {
         return "未知鬼魂";
      }

      String ghostTypeStr = convertEntityTypeToString(ghostType);
      return ghostTypeStr != null ? getGhostDisplayName(ghostTypeStr) : ghostType.toString();
   }

   private static String convertEntityTypeToString(EntityType<?> entityType) {
      return entityType == null ? null : ENTITY_TYPE_TO_GHOST_TYPE.get(entityType);
   }

   public static String getGhostTypeFromEntity(GhostEntity entity) {
      return entity == null ? null : convertEntityTypeToString(entity.method_5864());
   }

   public static Item getGhostItemByType(String ghostType) {
      return ghostType == null ? null : GHOST_TYPE_TO_ITEM_MAP.get(ghostType);
   }

   public static ItemStack createTamedItem(String ghostType) {
      return createTamedItem(ghostType, new NbtCompound());
   }

   public static ItemStack createTamedItem(String ghostType, NbtCompound ghostNbt) {
      if (ghostType != null && !ghostType.isEmpty()) {
         Function<NbtCompound, ItemStack> addonMapping = ADDON_GHOST_MAPPINGS.get(ghostType);
         if (addonMapping != null) {
            try {
               ItemStack itemStack = addonMapping.apply(ghostNbt);
               if (itemStack != null && !itemStack.method_7960()) {
                  LOGGER.info("使用附属模组映射创建驾驭物品: {}", ghostType);
                  return itemStack;
               }
            } catch (Exception e) {
               LOGGER.error("附属模组映射创建驾驭物品失败: {}", ghostType, e);
            }
         }

         Item ghostItem = GHOST_TYPE_TO_ITEM_MAP.get(ghostType);
         if (ghostItem != null) {
            return new ItemStack(ghostItem);
         }

         LOGGER.warn("未知的鬼类型: {}，使用默认控制槽物品", ghostType);
         return new ItemStack(ModItems.CONTROL_SLOT);
      } else {
         LOGGER.warn("尝试创建驾驭物品时鬼类型为空");
         return ItemStack.field_8037;
      }
   }

   public static String getGhostTypeFromItem(ItemStack itemStack) {
      if (itemStack != null && !itemStack.method_7960()) {
         Item item = itemStack.method_7909();

         for (Entry<String, Item> entry : GHOST_TYPE_TO_ITEM_MAP.entrySet()) {
            if (entry.getValue() == item) {
               return entry.getKey();
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static Item getRandomGhostControlItem() {
      return GHOST_CONTROL_ITEMS[RANDOM.nextInt(GHOST_CONTROL_ITEMS.length)];
   }

   public static boolean isItemMatchGhostType(ItemStack itemStack, String ghostType) {
      if (itemStack != null && !itemStack.method_7960() && ghostType != null) {
         Item expectedItem = GHOST_TYPE_TO_ITEM_MAP.get(ghostType);
         return expectedItem != null && itemStack.method_7909() == expectedItem;
      } else {
         return false;
      }
   }

   public static List<String> getAllGhostTypes() {
      return new ArrayList<>(GHOST_TYPE_TO_ITEM_MAP.keySet());
   }

   public static List<String> getAllGhostEntityTypes() {
      return new ArrayList<>(GHOST_TYPE_TO_ENTITY_MAP.keySet());
   }

   public static void addGhostItemMapping(String ghostType, Item item) {
      if (ghostType != null && item != null) {
         GHOST_TYPE_TO_ITEM_MAP.put(ghostType, item);
         LOGGER.info("添加新的鬼类型映射: {} -> {}", ghostType, item);
      }
   }

   public static void addGhostEntityMapping(String ghostType, Function<World, GhostEntity> entityCreator) {
      if (ghostType != null && entityCreator != null) {
         GHOST_TYPE_TO_ENTITY_MAP.put(ghostType, entityCreator);
         LOGGER.info("添加新的鬼实体映射: {}", ghostType);
      }
   }

   public static GhostEntity createGhostEntityByType(String ghostType, World world) {
      if (ghostType != null && world != null) {
         Function<World, GhostEntity> addonMapping = ADDON_ENTITY_CREATION_MAPPINGS.get(ghostType);
         if (addonMapping != null) {
            try {
               GhostEntity ghostEntity = addonMapping.apply(world);
               if (ghostEntity != null) {
                  LOGGER.info("使用附属模组映射创建鬼实体: {}", ghostType);
                  return ghostEntity;
               }
            } catch (Exception e) {
               LOGGER.error("附属模组映射创建鬼实体失败: {}", ghostType, e);
            }
         }

         Function<World, GhostEntity> entityCreator = GHOST_TYPE_TO_ENTITY_MAP.get(ghostType);
         if (entityCreator != null) {
            try {
               GhostEntity ghostEntity = entityCreator.apply(world);
               if (ghostEntity != null) {
                  return ghostEntity;
               }
            } catch (Exception e) {
               LOGGER.error("使用映射表创建鬼实体失败: {}", ghostType, e);
            }
         }

         LOGGER.warn("未知的鬼类型: {}，无法创建实体", ghostType);
         return null;
      } else {
         return null;
      }
   }

   public static boolean isGhostEntityType(EntityType<?> entityType) {
      return AdvancementManager.isGhostEntityType(entityType);
   }

   public static String getGhostRegistryId(EntityType<?> ghostType) {
      if (ghostType == null) {
         return "unknown";
      }

      try {
         return Registries.field_41177.method_10221(ghostType).toString();
      } catch (Exception e) {
         return "unknown";
      }
   }

   public static void registerAddonGhostMapping(String ghostType, Function<NbtCompound, ItemStack> itemCreator) {
      if (ghostType != null && itemCreator != null) {
         ADDON_GHOST_MAPPINGS.put(ghostType, itemCreator);
         LOGGER.info("成功注册附属模组鬼类型映射: {}", ghostType);
      } else {
         LOGGER.warn("尝试注册无效的附属模组鬼类型映射: ghostType={}, itemCreator={}", ghostType, itemCreator);
      }
   }

   public static void registerAddonGhostMapping(String ghostType, ItemStack itemStack) {
      if (ghostType != null && itemStack != null && !itemStack.method_7960()) {
         ADDON_GHOST_MAPPINGS.put(ghostType, nbt -> itemStack.method_7972());
         LOGGER.info("成功注册附属模组鬼类型映射（简化版）: {}", ghostType);
      } else {
         LOGGER.warn("尝试注册无效的附属模组鬼类型映射: ghostType={}, itemStack={}", ghostType, itemStack);
      }
   }

   public static void registerAddonDisplayNameMapping(String ghostType, Function<String, String> displayNameProvider) {
      if (ghostType != null && displayNameProvider != null) {
         ADDON_DISPLAY_NAME_MAPPINGS.put(ghostType, displayNameProvider);
         LOGGER.info("成功注册附属模组显示名称映射: {}", ghostType);
      } else {
         LOGGER.warn("尝试注册无效的附属模组显示名称映射: ghostType={}, displayNameProvider={}", ghostType, displayNameProvider);
      }
   }

   public static void registerAddonDisplayNameMapping(String ghostType, String displayName) {
      if (ghostType != null && displayName != null && !displayName.isEmpty()) {
         ADDON_DISPLAY_NAME_MAPPINGS.put(ghostType, key -> displayName);
         LOGGER.info("成功注册附属模组显示名称映射（简化版）: {}", ghostType);
      } else {
         LOGGER.warn("尝试注册无效的附属模组显示名称映射: ghostType={}, displayName={}", ghostType, displayName);
      }
   }

   public static void registerAddonAbilityMapping(String ghostType, Function<String, List<String>> abilityProvider) {
      if (ghostType != null && abilityProvider != null) {
         ADDON_ABILITY_MAPPINGS.put(ghostType, abilityProvider);
         LOGGER.info("成功注册附属模组能力描述映射: {}", ghostType);
      } else {
         LOGGER.warn("尝试注册无效的附属模组能力描述映射: ghostType={}, abilityProvider={}", ghostType, abilityProvider);
      }
   }

   public static void registerAddonAbilityMapping(String ghostType, List<String> abilities) {
      if (ghostType != null && abilities != null && !abilities.isEmpty()) {
         ADDON_ABILITY_MAPPINGS.put(ghostType, key -> abilities);
         LOGGER.info("成功注册附属模组能力描述映射（简化版）: {}", ghostType);
      } else {
         LOGGER.warn("尝试注册无效的附属模组能力描述映射: ghostType={}, abilities={}", ghostType, abilities);
      }
   }

   public static void registerAddonEntityCreationMapping(String ghostType, Function<World, GhostEntity> entityCreator) {
      if (ghostType != null && entityCreator != null) {
         ADDON_ENTITY_CREATION_MAPPINGS.put(ghostType, entityCreator);
         LOGGER.info("成功注册附属模组实体创建映射: {}", ghostType);
      } else {
         LOGGER.warn("尝试注册无效的附属模组实体创建映射: ghostType={}, entityCreator={}", ghostType, entityCreator);
      }
   }

   public static void registerAddonEntityCreationMapping(String ghostType, EntityType<? extends GhostEntity> entityType) {
      if (ghostType != null && entityType != null) {
         ADDON_ENTITY_CREATION_MAPPINGS.put(ghostType, world -> {
            try {
               return (GhostEntity)entityType.method_5883(world);
            } catch (Exception e) {
               LOGGER.error("创建鬼实体失败: ghostType={}, entityType={}", ghostType, entityType, e);
               return null;
            }
         });
         LOGGER.info("成功注册附属模组实体创建映射（简化版）: {}", ghostType);
      } else {
         LOGGER.warn("尝试注册无效的附属模组实体创建映射: ghostType={}, entityType={}", ghostType, entityType);
      }
   }

   public static List<String> getGhostAbilityDescriptions(String ghostType) {
      List<String> abilities = new ArrayList<>();
      if (ghostType != null && !ghostType.isEmpty()) {
         Function<String, List<String>> addonMapping = ADDON_ABILITY_MAPPINGS.get(ghostType);
         if (addonMapping != null) {
            try {
               List<String> addonAbilities = addonMapping.apply(ghostType);
               if (addonAbilities != null && !addonAbilities.isEmpty()) {
                  LOGGER.info("使用附属模组映射获取能力描述: {}", ghostType);
                  return addonAbilities;
               }
            } catch (Exception e) {
               LOGGER.error("附属模组映射获取能力描述失败: {}", ghostType, e);
            }
         }

         switch (ghostType) {
            case "silent_ghost_eye":
               abilities.add("代号：腥红鬼眼");
               abilities.add("被动：周围有鬼时感知危险");
               abilities.add("袭击：修复骗人鬼项链");
               abilities.add("一级：感知鬼的恐怖程度");
               abilities.add("二级：洞察鬼域内一切生物");
               abilities.add("三级：解锁鬼域飞行");
               abilities.add("四级：解锁主动技能一[鬼域瞬移]，出现在目标位置");
               abilities.add("");
               abilities.add("五级：解锁主动技能二[鬼域放逐]，放逐目标生物或方块");
               abilities.add("");
               abilities.add("六级：无");
               abilities.add("");
               abilities.add("七级：解锁主动技能三[重启]，移除所有负面状态");
               abilities.add("");
               abilities.add("鬼域：是");
               abilities.add("");
               abilities.add("§6成神：放逐加强，可永久封锁厉鬼");
               break;
            case "silent_ghost":
               abilities.add("代号：静悄悄");
               abilities.add("被动：出现在任何呼唤你名的地方");
               abilities.add("袭击：同意传送");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               abilities.add("§6成神：获得新被动，死亡后可重新入侵回死亡地点，且保留全部灵异");
               break;
            case "ghost_fire":
               abilities.add("代号：鬼火");
               abilities.add("被动：免疫火焰伤害");
               abilities.add("被动：与水鬼同时存在时，伤害翻倍");
               abilities.add("");
               abilities.add("袭击：袭击所有燃烧的生物");
               abilities.add("");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：感知鬼的恐怖程度");
               abilities.add("二级：解锁主动技能一[点燃]，解锁被动：洞察生物");
               abilities.add("");
               abilities.add("三级：解锁鬼域飞行");
               abilities.add("");
               abilities.add("四级：解锁主动技能二[燃烧]，燃烧鬼域内生物附近方块");
               abilities.add("");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：解锁主动技能三[焚烧]，焚烧周围的一切事物");
               abilities.add("");
               abilities.add("鬼域：是");
               abilities.add("点燃：点燃鬼域内所以生物");
               break;
            case "burn_ghost":
               abilities.add("代号：鬼火");
               abilities.add("被动：免疫火焰伤害");
               abilities.add("袭击：袭击所有燃烧的生物");
               abilities.add("");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：感知鬼的恐怖程度");
               abilities.add("二级：解锁主动技能一[点燃]，解锁被动：洞察生物");
               abilities.add("");
               abilities.add("三级：解锁鬼域飞行");
               abilities.add("");
               abilities.add("四级：解锁主动技能二[燃烧]，燃烧鬼域内生物附近方块");
               abilities.add("");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：解锁主动技能三[焚烧]，焚烧周围的一切事物");
               abilities.add("");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：是");
               abilities.add("点燃：点燃鬼域内所以生物");
               break;
            case "ghost_fist":
               abilities.add("代号：鬼拳");
               abilities.add("被动：攻击附带沉寂效果");
               abilities.add("袭击：空手任意袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "fog_ghost":
               abilities.add("代号：鬼雾");
               abilities.add("被动：自动标记雾中游荡的生物");
               abilities.add("袭击：对带有标记的生物发动袭击");
               abilities.add("");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：感知鬼的恐怖程度");
               abilities.add("二级：洞察鬼域内一切生物");
               abilities.add("三级：解锁鬼域飞行");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：是");
               break;
            case "qiaomen_ghost":
               abilities.add("代号：敲门鬼");
               abilities.add("被动：[尸斑诅咒]");
               abilities.add("袭击：通过敲门触发灵异袭击");
               abilities.add("");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：感知鬼的恐怖程度");
               abilities.add("二级：解锁主动技能一[鬼敲门]，解锁被动：洞察生物");
               abilities.add("");
               abilities.add("三级：解锁鬼域飞行");
               abilities.add("");
               abilities.add("四级：解锁主动技能二[瞬移]，在门之间快速移动");
               abilities.add("");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：解锁主动技能三[灵异叠加]，敲响鬼域内所有的门，并叠加袭击");
               abilities.add("");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：是");
               abilities.add("尸斑诅咒:腐烂触碰到的生物");
               abilities.add("鬼敲门：在目标位置生成门");
               break;
            case "taitou_ghost":
               abilities.add("代号：抬头鬼");
               abilities.add("");
               abilities.add("被动：与低头鬼同时存在时，伤害翻倍");
               abilities.add("");
               abilities.add("袭击：标记抬头生物并对其发动袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "ditou_ghost":
               abilities.add("代号：低头鬼");
               abilities.add("");
               abilities.add("被动：与抬头鬼同时存在时，伤害翻倍");
               abilities.add("");
               abilities.add("袭击：标记低头生物并对其发动袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "food_ghost":
               abilities.add("代号：食物鬼");
               abilities.add("被动：自动标记食用食物的玩家");
               abilities.add("被动：不会再感到饥饿");
               abilities.add("袭击：对已标记的生物发动袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("");
               abilities.add("一级：感知鬼的恐怖程度");
               abilities.add("二级：洞察鬼域内一切生物");
               abilities.add("三级：解锁鬼域飞行");
               abilities.add("四级：无");
               abilities.add("五级：解锁主动技能一[饥荒]");
               abilities.add("");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：是");
               abilities.add("饥荒：夺走目标饱食度，如果已空则触发标记。");
               break;
            case "block_ghost":
               abilities.add("代号：方块鬼");
               abilities.add("被动：自动标记破坏/放置的玩家");
               abilities.add("袭击：对带有标记的生物发动袭击");
               abilities.add("");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：感知鬼的恐怖程度");
               abilities.add("二级：洞察鬼域内一切生物");
               abilities.add("三级：解锁鬼域飞行");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：解锁主动技能一[创世]，获得随机方块。");
               abilities.add("");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：是");
               break;
            case "box_ghost":
               abilities.add("代号：开箱鬼");
               abilities.add("被动：显示周围宝藏位置");
               abilities.add("袭击：标记箱子附近生物发动袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "villager_ghost":
               abilities.add("代号：村民鬼");
               abilities.add("被动：鬼域内僵尸会转换为村民");
               abilities.add("袭击：同化村民为自身鬼奴");
               abilities.add("一级：感知鬼的恐怖程度");
               abilities.add("二级：洞察鬼域内一切生物");
               abilities.add("");
               abilities.add("三级：解锁鬼域飞行，解锁主动技能一[速度]，提高鬼奴的速度。");
               abilities.add("");
               abilities.add("四级：无");
               abilities.add("");
               abilities.add("五级：解锁主动技能二[力量]，提高鬼奴的力量。");
               abilities.add("");
               abilities.add("六级：无");
               abilities.add("");
               abilities.add("七级：解锁主动技能三[献祭]，引爆所有鬼奴。");
               abilities.add("");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：是");
               break;
            case "jump_ghost":
               abilities.add("代号：跳跃鬼");
               abilities.add("");
               abilities.add("被动：与潜行鬼同时存在时，伤害翻倍");
               abilities.add("");
               abilities.add("袭击：标记跳跃生物对其发动袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "untouchable_ghost":
               abilities.add("代号：不可触摸鬼");
               abilities.add("被动：开裂诅咒");
               abilities.add("袭击：无");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               abilities.add("开裂诅咒：腐蚀所有触碰到的生物");
               break;
            case "death_sight_ghost":
               abilities.add("代号：死亡凝视鬼");
               abilities.add("被动：无");
               abilities.add("袭击：标记看到自己的生物发动袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "ghost_merchant":
               abilities.add("代号：鬼商人");
               abilities.add("被动：袭击可对所有商人有效");
               abilities.add("袭击：标记没钱的玩家发动袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "lost_ghost":
               abilities.add("代号：遗忘鬼");
               abilities.add("被动：每10s触发一次遗忘效果");
               abilities.add("袭击：直接袭击所有有意识的生物");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("");
               abilities.add("二级：解锁主动技能一[遗忘]，让目标玩家遗忘行走。");
               abilities.add("");
               abilities.add("三级：解锁主动技能二[遗忘]，让目标玩家遗忘秩序。");
               abilities.add("");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("");
               abilities.add("七级：解锁主动技能三[重启]，清除负面效果并遗忘厉鬼复苏。");
               abilities.add("");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               abilities.add("遗忘效果：隐藏自身，并清除周围生物对自身的仇恨");
               break;
            case "crying_ghost":
               abilities.add("代号：哭丧鬼");
               abilities.add("被动：无");
               abilities.add("袭击：直接袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "suona_ghost":
               abilities.add("代号：唢呐鬼");
               abilities.add("被动：无");
               abilities.add("袭击：直接袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "gong_ghost":
               abilities.add("代号：敲锣鬼");
               abilities.add("被动：无");
               abilities.add("袭击：直接袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "crop_ghost":
               abilities.add("代号：作物鬼");
               abilities.add("");
               abilities.add("被动：位于前台时，脚下泥土自动转变成耕地");
               abilities.add("");
               abilities.add("袭击：标记地里生物并对其发动袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "step_ghost":
               abilities.add("代号：踩人鬼");
               abilities.add("被动：无");
               abilities.add("袭击：标记低于自己的生物并对其发动袭击");
               abilities.add("");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "grave_earth_ghost":
               abilities.add("代号：坟土鬼");
               abilities.add("被动：免疫坟土的掩埋效果");
               abilities.add("袭击：在目标位置放置坟堆");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "trash_ghost":
               abilities.add("代号：垃圾鬼");
               abilities.add("被动：无");
               abilities.add("袭击：标记周围有掉落物的生物并对其发动袭击");
               abilities.add("");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "water_ghost":
               abilities.add("代号：水鬼");
               abilities.add("被动：免疫鬼湖和鬼血的压制");
               abilities.add("被动：与鬼火同时存在时，伤害翻倍");
               abilities.add("");
               abilities.add("袭击：标记水中生物并对其发动袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "giant_shadow_ghost":
               abilities.add("代号：无头鬼影");
               abilities.add("");
               abilities.add("被动：蹲下可遁入黑影，提高移速，离开时造成一次范围袭击。");
               abilities.add("");
               abilities.add("被动：免疫柴刀带来的副作用");
               abilities.add("");
               abilities.add("被动：鬼影会持续修复身体，恢复伤势");
               abilities.add("");
               abilities.add("被动：单次受伤超过自身最大灵异强度的10%时，免疫溢出部分");
               abilities.add("");
               abilities.add("袭击：标记背对生物并对其发动袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("§d异类：右键可直接远距离触发柴刀/长枪的媒介攻击");
               break;
            case "ganshi_bride_ghost":
               abilities.add("代号：干尸新娘");
               abilities.add("被动：无");
               abilities.add("袭击：偷取目标玩家身上的鬼。");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               abilities.add("§d异类：免疫招鬼");
               break;
            case "ghost_wind":
               abilities.add("代号：鬼风");
               abilities.add("被动：吹起鬼蜮内生物");
               abilities.add("袭击：袭击被吹起的生物");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：感知鬼的恐怖程度");
               abilities.add("二级：洞察鬼域内一切生物");
               abilities.add("三级：解锁鬼域飞行");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：是");
               break;
            case "ghost_blood":
               abilities.add("代号：鬼血");
               abilities.add("被动：压制厉鬼复苏");
               abilities.add("袭击：暂时压制厉鬼");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "ghost_pressure":
               abilities.add("代号：鬼压人");
               abilities.add("被动：复苏程度过高会压迫自身");
               abilities.add("袭击：让身上的鬼压迫目标");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               abilities.add("§d异类：免疫压迫自身的负面效果，目标灵异强度低于20%触发斩杀");
               abilities.add("");
               abilities.add("§6成神：斩杀效果加强到50%");
               break;
            case "wish_ghost":
               abilities.add("代号：许愿鬼");
               abilities.add("被动：无");
               abilities.add("许愿/袭击：我说我身强体壮，灾病全无");
               abilities.add("");
               abilities.add("许愿/一技能：我说我必定离开这片鬼域");
               abilities.add("");
               abilities.add("许愿/二技能：我说我行不可摧，志不可改，力可至极限");
               abilities.add("");
               abilities.add("许愿/三技能：我说眼前灵异必将退散");
               abilities.add("");
               abilities.add("鬼域：否");
               break;
            case "scapegoat_ghost":
               abilities.add("代号：替死鬼");
               abilities.add("");
               abilities.add("被动：受到致命伤害后，会将伤害转移给周围的其他生物。");
               abilities.add("");
               abilities.add("袭击：指定目标作为伤害转移对象");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               abilities.add("备注：替死效果随复苏程度变化");
               break;
            case "candy_ghost":
               abilities.add("代号：糖果鬼");
               abilities.add("被动：无。");
               abilities.add("袭击：将生命力转化为鬼糖果。");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "ghost_officer":
               abilities.add("代号：鬼差");
               abilities.add("");
               abilities.add("被动：根据自身厉鬼数量自动[压制]鬼域内其他灵异");
               abilities.add("");
               abilities.add("袭击：优先[压制]选择的目标");
               abilities.add("一级：感知鬼的恐怖程度");
               abilities.add("二级：洞察鬼域内一切生物");
               abilities.add("三级：解锁鬼域飞行");
               abilities.add("四级：解锁主动技能一[转换]，解除/恢复目标的压制");
               abilities.add("");
               abilities.add("五级：解锁主动技能二[处决]，秒杀被压制的目标");
               abilities.add("");
               abilities.add("七级：解锁主动技能三[重启]，恢复自身状态。");
               abilities.add("");
               abilities.add("鬼域：是");
               abilities.add("");
               abilities.add("压制：对方体内鬼数量低于自己则直接沉寂");
               abilities.add("");
               abilities.add("§6成神：处决技能增强，可以吞噬厉鬼并增加压制名额");
               break;
            case "plague_ghost":
               abilities.add("代号：瘟鬼");
               abilities.add("被动：免疫瘟疫效果");
               abilities.add("袭击：传染目标生物瘟疫");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：无");
               abilities.add("瘟疫：持续受到伤害并传播给周围生物");
               break;
            case "mineral_ghost":
               abilities.add("代号：矿物鬼");
               abilities.add("被动：显示周围矿物位置");
               abilities.add("袭击：引爆周围的矿物");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：无");
               break;
            case "shadow_ghost":
               abilities.add("代号：黑影鬼");
               abilities.add("被动：在阴影中移速提升且隐匿");
               abilities.add("袭击：标记处于阴影中的生物并对其发动袭击");
               abilities.add("");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：无");
               break;
            case "complete_shadow_ghost":
               abilities.add("代号：完整鬼影");
               abilities.add("");
               abilities.add("被动：获得无头鬼影的全部被动效果");
               abilities.add("");
               abilities.add("被动：获得鬼影头的全部被动效果");
               abilities.add("");
               abilities.add("袭击：入侵目标生物的意识");
               abilities.add("袭击倍率：100%");
               abilities.add("三级：解锁主动技能一，袭击被入侵的生物并收回意识");
               abilities.add("");
               abilities.add("四级：无");
               abilities.add("");
               abilities.add("五级：解锁主动技能二，降低被入侵的状态");
               abilities.add("");
               abilities.add("七级：解锁主动技能三，夺舍被入侵生物，影响其行为");
               abilities.add("");
               abilities.add("§d异类：夺舍目标可获得更高的控制权，右键可直接远距离触发柴刀/长枪的媒介攻击");
               abilities.add("");
               abilities.add("§6成神：夺舍目标可获得完全控制权");
               break;
            case "door_ghost":
               abilities.add("代号：开门鬼");
               abilities.add("被动：无");
               abilities.add("袭击：开门对周围生物造成袭击");
               abilities.add("袭击倍率：200%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：无");
               break;
            case "ghost_smoke":
               abilities.add("代号：鬼烟");
               abilities.add("被动：缓慢侵蚀吸入烟气的生物");
               abilities.add("袭击：无");
               abilities.add("一级：感知鬼的恐怖程度");
               abilities.add("二级：洞察鬼域内一切生物");
               abilities.add("三级：解锁鬼域飞行");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：是");
               break;
            case "ghost_shadow_head":
               abilities.add("代号：鬼影头");
               abilities.add("");
               abilities.add("被动：获得大量陌生记忆，尤其是关于如何弹钢琴");
               abilities.add("");
               abilities.add("被动：周围有厉鬼时，理智不再消耗");
               abilities.add("");
               abilities.add("袭击：入侵目标生物的意识");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：解锁主动技能一，袭击被入侵的生物并收回意识");
               abilities.add("");
               abilities.add("四级：无");
               abilities.add("");
               abilities.add("五级：解锁主动技能二，降低被入侵的状态");
               abilities.add("");
               abilities.add("七级：解锁主动技能三，夺舍被入侵生物，影响其行为");
               abilities.add("");
               abilities.add("§d异类：夺舍目标可获得更高的控制权");
               abilities.add("§6成神：夺舍目标可获得完全控制权");
               break;
            case "sneak_ghost":
               abilities.add("代号：潜行鬼");
               abilities.add("");
               abilities.add("被动：潜行状态下大幅提高灵异抗性与灵异伤害");
               abilities.add("");
               abilities.add("被动：与跳跃鬼同时存在时，伤害翻倍");
               abilities.add("");
               abilities.add("袭击：标记蹲下的玩家并对其发动袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "clothes_ghost":
               abilities.add("代号：裁缝鬼");
               abilities.add("被动：自动缝补身上装备，恢复耐久");
               abilities.add("袭击：标记未穿戴装备的玩家并对其发动袭击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "puppet_ghost":
               abilities.add("代号：木偶鬼");
               abilities.add("");
               abilities.add("被动：自动标记周围静止不动的生物");
               abilities.add("");
               abilities.add("被动：静止不动时每5s获得10点灵异抗性，最大2000点。");
               abilities.add("");
               abilities.add("袭击：同时袭击周围被标记的全部生物");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "ghost_dream":
               abilities.add("代号：鬼梦");
               abilities.add("");
               abilities.add("被动：周围有厉鬼时，理智不再消耗");
               abilities.add("");
               abilities.add("袭击：召唤恶犬将目标拉入梦中");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：解锁主动技能一，进入/离开[鬼梦世界]");
               abilities.add("");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：是");
               break;
            case "funeral_music_ghost":
               abilities.add("代号：丧乐鬼");
               abilities.add("");
               abilities.add("被动：如果一技能/二技能在非沉寂效果下释放，净化效果将延长至30s。");
               abilities.add("");
               abilities.add("被动：演奏状态下，每3s叠加一层标记至周围生物，同时自身扣除100点灵异强度。");
               abilities.add("");
               abilities.add("被动：灵异强度低于1000点时自动退出演奏状态并触发所有标记。");
               abilities.add("");
               abilities.add("袭击：触发全部标记，按照层数造成不同程度的灵异叠加袭击。");
               abilities.add("");
               abilities.add("袭击倍率：100%");
               abilities.add("");
               abilities.add("丧乐/一技能：立刻解除自身沉寂并进入演奏状态，同时获得3s净化效果");
               abilities.add("");
               abilities.add("丧乐/二技能：退出演奏状态，并立刻解除自身沉寂，同时获得3s净化效果");
               abilities.add("");
               abilities.add("丧乐/三技能：触发全部标记，并使目标陷入短暂沉寂。");
               abilities.add("");
               abilities.add("鬼域：否");
               break;
            case "ghost_lake":
               abilities.add("代号：鬼湖");
               abilities.add("");
               abilities.add("被动：在鬼湖中时，灵异伤害提高50%，免疫负面效果。");
               abilities.add("");
               abilities.add("袭击：在目标位置召唤一滩湖水，概率生成敌对鬼奴。");
               abilities.add("");
               abilities.add("二级：解锁主动技能一，立即出现在另一处鬼湖");
               abilities.add("");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：解锁主动技能二，以湖水为媒介将灵异低于自身的目标关押/释放");
               abilities.add("");
               abilities.add("六级：无");
               abilities.add("");
               abilities.add("七级：解锁主动技能三，以湖水为媒介进入/离开[真·鬼湖]");
               abilities.add("");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
               break;
            case "xinkai_ghost":
               abilities.add("代号：新凯鬼");
               abilities.add("");
               abilities.add("被动：靠近玩家时自动攻击");
               abilities.add("");
               abilities.add("袭击：灵异攻击");
               abilities.add("袭击倍率：100%");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：是");
               break;
            default:
               abilities.add("未知鬼类型");
               abilities.add("被动：无");
               abilities.add("袭击：无");
               abilities.add("一级：无");
               abilities.add("二级：无");
               abilities.add("三级：无");
               abilities.add("四级：无");
               abilities.add("五级：无");
               abilities.add("六级：无");
               abilities.add("七级：无");
               abilities.add("八级：无");
               abilities.add("九级：无");
               abilities.add("十级：无");
               abilities.add("鬼域：否");
         }

         return abilities;
      } else {
         abilities.add("未知鬼类型");
         abilities.add("被动：无");
         abilities.add("袭击：无");
         abilities.add("一级：无");
         abilities.add("二级：无");
         abilities.add("三级：无");
         abilities.add("四级：无");
         abilities.add("五级：无");
         abilities.add("六级：无");
         abilities.add("七级：无");
         abilities.add("八级：无");
         abilities.add("九级：无");
         abilities.add("十级：无");
         abilities.add("鬼域：否");
         return abilities;
      }
   }

   public static Map<String, Item> getVanillaItemMap() {
      return VANILLA_ITEM_MAP;
   }

   public static Map<String, Item> getModItemMap() {
      return MOD_ITEM_MAP;
   }

   public static Item getVanillaItem(String itemId) {
      return VANILLA_ITEM_MAP.get(itemId);
   }

   public static Item getModItem(String itemId) {
      return MOD_ITEM_MAP.get(itemId);
   }

   public static boolean containsVanillaItem(String itemId) {
      return VANILLA_ITEM_MAP.containsKey(itemId);
   }

   public static boolean containsModItem(String itemId) {
      return MOD_ITEM_MAP.containsKey(itemId);
   }

   public static void addVanillaItemMapping(String itemId, Item item) {
      VANILLA_ITEM_MAP.put(itemId, item);
   }

   public static void addModItemMapping(String itemId, Item item) {
      MOD_ITEM_MAP.put(itemId, item);
   }

   public static void removeVanillaItemMapping(String itemId) {
      VANILLA_ITEM_MAP.remove(itemId);
   }

   public static void removeModItemMapping(String itemId) {
      MOD_ITEM_MAP.remove(itemId);
   }

   public static Set<String> getVanillaItemIds() {
      return VANILLA_ITEM_MAP.keySet();
   }

   public static Set<String> getModItemIds() {
      return MOD_ITEM_MAP.keySet();
   }

   public static ItemStack createItemStack(String itemId, int count) {
      Item vanillaItem = VANILLA_ITEM_MAP.get(itemId);
      if (vanillaItem != null) {
         return new ItemStack(vanillaItem, count);
      }

      Item modItem = MOD_ITEM_MAP.get(itemId);
      if (modItem != null) {
         return new ItemStack(modItem, count);
      }

      LOGGER.warn("未知的物品ID: {}", itemId);
      return ItemStack.field_8037;
   }

   public static Map<String, String> getItemDisplayNameMap() {
      return ITEM_DISPLAY_NAME_MAP;
   }

   public static String getItemDisplayName(String itemId) {
      return ITEM_DISPLAY_NAME_MAP.getOrDefault(itemId, itemId);
   }

   public static boolean hasItemDisplayName(String itemId) {
      return ITEM_DISPLAY_NAME_MAP.containsKey(itemId);
   }

   public static void addItemDisplayNameMapping(String itemId, String displayName) {
      ITEM_DISPLAY_NAME_MAP.put(itemId, displayName);
   }

   public static void removeItemDisplayNameMapping(String itemId) {
      ITEM_DISPLAY_NAME_MAP.remove(itemId);
   }

   public static Set<String> getItemDisplayNameIds() {
      return ITEM_DISPLAY_NAME_MAP.keySet();
   }

   static {
      initializeGhostItemMappings();
      initializeGhostEntityMappings();
      initializeGhostDisplayNameMappings();
   }
}
