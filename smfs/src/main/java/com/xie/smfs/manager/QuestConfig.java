package com.xie.smfs.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestConfig {
   public static final List<QuestManager.QuestTemplate> NEWBIE_QUESTS = Arrays.asList(
      new QuestManager.QuestTemplate(
         "newbie_get_log",
         "梦的开始",
         "获得一块橡树原木",
         "newbie",
         Arrays.asList(new QuestManager.QuestObjective("get_log", "获得原木", 1)),
         Map.of("items", "cookie:6"),
         new ArrayList<>(),
         true,
         1
      ),
      new QuestManager.QuestTemplate(
         "newbie_get_stone",
         "原石",
         "获得一块原石",
         "newbie",
         Arrays.asList(new QuestManager.QuestObjective("get_stone", "获得原石", 1)),
         Map.of("items", "iron_sword:1"),
         Arrays.asList("newbie_get_log"),
         true,
         2
      ),
      new QuestManager.QuestTemplate(
         "newbie_kill_zombie",
         "僵尸",
         "击杀一只僵尸",
         "newbie",
         Arrays.asList(new QuestManager.QuestObjective("kill_zombie", "击杀僵尸", 1)),
         Map.of("items", "golden_apple:1"),
         Arrays.asList("newbie_get_stone"),
         true,
         3
      ),
      new QuestManager.QuestTemplate(
         "newbie_get_iron",
         "铁锭",
         "获得一块铁锭",
         "newbie",
         Arrays.asList(new QuestManager.QuestObjective("get_iron", "获得铁锭", 1)),
         Map.of("items", "iron_leggings:1"),
         Arrays.asList("newbie_kill_zombie"),
         true,
         4
      ),
      new QuestManager.QuestTemplate(
         "newbie_get_gold",
         "黄金",
         "获得一块黄金",
         "newbie",
         Arrays.asList(new QuestManager.QuestObjective("get_gold", "获得黄金", 1)),
         Map.of("items", "iron_chestplate:1"),
         Arrays.asList("newbie_get_iron"),
         true,
         5
      ),
      new QuestManager.QuestTemplate(
         "newbie_craft_gold_container",
         "黄金容器",
         "使用金锭打造一个黄金容器",
         "newbie",
         Arrays.asList(new QuestManager.QuestObjective("craft_gold_container", "获得黄金容器", 1)),
         Map.of("items", "mystery_coordinate:1"),
         Arrays.asList("newbie_get_gold"),
         true,
         6
      )
   );
   public static final List<QuestManager.QuestTemplate> MAIN_QUESTS = Arrays.asList(
      new QuestManager.QuestTemplate(
         "main_kill_ghost_slave",
         "鬼奴Ⅰ",
         "使用原版武器击杀一个鬼奴",
         "main",
         Arrays.asList(new QuestManager.QuestObjective("kill_ghost_slave", "击杀鬼奴", 1)),
         Map.of("items", "gold_ingot:12"),
         Arrays.asList("newbie_craft_gold_container"),
         false,
         7
      ),
      new QuestManager.QuestTemplate(
         "main_craft_ghost_faction",
         "鬼派",
         "合成一个鬼派",
         "main",
         Arrays.asList(new QuestManager.QuestObjective("craft_ghost_faction", "获得鬼派", 1)),
         Map.of("items", "ghost_porcelain:1"),
         Arrays.asList("main_kill_ghost_slave"),
         false,
         8
      ),
      new QuestManager.QuestTemplate(
         "main_tame_ghost",
         "驭鬼者",
         "成功驾驭任意一只鬼",
         "main",
         Arrays.asList(new QuestManager.QuestObjective("tame_ghost", "驾驭鬼", 1)),
         Map.of("items", "red_ghost_candle:1"),
         Arrays.asList("main_craft_ghost_faction"),
         false,
         9
      ),
      new QuestManager.QuestTemplate(
         "main_survive_5_days",
         "存活到第五天以后",
         "在世界中存活到第五天以后",
         "main",
         Arrays.asList(new QuestManager.QuestObjective("survive_5_days", "存活天数≥5", 5)),
         Map.of("items", "diamond_sword:1"),
         Arrays.asList("main_tame_ghost"),
         false,
         10
      ),
      new QuestManager.QuestTemplate(
         "main_kill_5_ghost_slaves",
         "鬼奴Ⅱ",
         "使用原版武器击杀5只鬼奴",
         "main",
         Arrays.asList(new QuestManager.QuestObjective("kill_ghost_slave", "击杀鬼奴", 5)),
         Map.of("items", "diamond_chestplate:1"),
         Arrays.asList("main_survive_5_days"),
         false,
         11
      ),
      new QuestManager.QuestTemplate(
         "main_capture_ghost",
         "关押",
         "成功关押一只厉鬼",
         "main",
         Arrays.asList(new QuestManager.QuestObjective("capture_ghost", "关押鬼", 1)),
         Map.of("items", "diamond_leggings:1"),
         Arrays.asList("main_kill_5_ghost_slaves"),
         false,
         12
      ),
      new QuestManager.QuestTemplate(
         "main_trade_with_wangxiaoming",
         "交易",
         "找到王小明并与他进行一次交易",
         "main",
         Arrays.asList(new QuestManager.QuestObjective("trade_with_wangxiaoming", "和王小明交易", 1)),
         Map.of("items", "chinese_medicine_shop_mystery_info:1"),
         Arrays.asList("main_capture_ghost"),
         false,
         13
      ),
      new QuestManager.QuestTemplate(
         "main_enter_chinese_medicine_shop",
         "中药铺",
         "找到并进入中药铺",
         "main",
         Arrays.asList(new QuestManager.QuestObjective("enter_chinese_medicine_shop", "进入中药铺", 1)),
         Map.of("items", "tame_slot:1"),
         Arrays.asList("main_trade_with_wangxiaoming"),
         false,
         14
      ),
      new QuestManager.QuestTemplate(
         "main_complete_all_quests",
         "完成",
         "恭喜你完成全部任务",
         "main",
         new ArrayList<>(),
         Map.of("items", "ghost_pearl:1"),
         Arrays.asList("main_enter_chinese_medicine_shop"),
         false,
         15
      )
   );
   public static final List<QuestManager.QuestTemplate> DAILY_QUESTS = Arrays.asList(
      new QuestManager.QuestTemplate(
         "daily_get_corpse_oil",
         "尸油收集（日常）",
         "获得10个尸油",
         "daily",
         Arrays.asList(new QuestManager.QuestObjective("get_corpse_oil", "获得尸油", 10)),
         Map.of("items", "gold_ingot:5"),
         Arrays.asList("newbie_craft_gold_container"),
         false,
         0
      ),
      new QuestManager.QuestTemplate(
         "daily_get_corpse_piece",
         "尸块收集（日常）",
         "获得10个尸块",
         "daily",
         Arrays.asList(new QuestManager.QuestObjective("get_corpse_piece", "获得尸块", 10)),
         Map.of("items", "gold_ingot:5"),
         Arrays.asList("newbie_craft_gold_container"),
         false,
         0
      ),
      new QuestManager.QuestTemplate(
         "daily_kill_zombies",
         "僵尸猎手（日常）",
         "击杀10只僵尸",
         "daily",
         Arrays.asList(new QuestManager.QuestObjective("kill_zombie", "击杀僵尸", 10)),
         Map.of("items", "gold_ingot:5"),
         Arrays.asList("newbie_craft_gold_container"),
         false,
         0
      ),
      new QuestManager.QuestTemplate(
         "daily_kill_skeletons",
         "骷髅射手（日常）",
         "击杀10只小白",
         "daily",
         Arrays.asList(new QuestManager.QuestObjective("kill_skeleton", "击杀小白", 10)),
         Map.of("items", "gold_ingot:5"),
         Arrays.asList("newbie_craft_gold_container"),
         false,
         0
      ),
      new QuestManager.QuestTemplate(
         "daily_kill_spiders",
         "蜘蛛猎手（日常）",
         "击杀5只蜘蛛",
         "daily",
         Arrays.asList(new QuestManager.QuestObjective("kill_spider", "击杀蜘蛛", 5)),
         Map.of("items", "gold_ingot:5"),
         Arrays.asList("newbie_craft_gold_container"),
         false,
         0
      ),
      new QuestManager.QuestTemplate(
         "daily_kill_creepers",
         "爆炸专家（日常）",
         "击杀5只苦力怕",
         "daily",
         Arrays.asList(new QuestManager.QuestObjective("kill_creeper", "击杀苦力怕", 5)),
         Map.of("items", "gold_ingot:5"),
         Arrays.asList("newbie_craft_gold_container"),
         false,
         0
      ),
      new QuestManager.QuestTemplate(
         "daily_kill_ghost_slave",
         "鬼奴猎手（日常）",
         "击杀一个鬼奴",
         "daily",
         Arrays.asList(new QuestManager.QuestObjective("kill_ghost_slave", "击杀鬼奴", 1)),
         Map.of("items", "gold_ingot:10"),
         Arrays.asList("newbie_craft_gold_container"),
         false,
         0
      )
   );
   public static final List<QuestManager.QuestTemplate> EVENT_QUESTS = Arrays.asList(
      new QuestManager.QuestTemplate(
         "event_ghost_knock",
         "鬼敲门",
         "遭遇鬼敲门事件",
         "side",
         Arrays.asList(new QuestManager.QuestObjective("ghost_knock_event", "鬼敲门事件", 1)),
         Map.of("items", "gold_ingot:5"),
         Arrays.asList("main_tame_ghost"),
         false,
         0
      ),
      new QuestManager.QuestTemplate(
         "event_ghost_look_up",
         "鬼抬头",
         "遭遇鬼抬头事件",
         "side",
         Arrays.asList(new QuestManager.QuestObjective("ghost_look_up_event", "鬼抬头事件", 1)),
         Map.of("items", "diamond:1"),
         Arrays.asList("main_tame_ghost"),
         false,
         0
      ),
      new QuestManager.QuestTemplate(
         "event_invisible",
         "不可视",
         "遭遇不可视事件",
         "side",
         Arrays.asList(new QuestManager.QuestObjective("invisible_event", "不可视事件", 1)),
         Map.of("items", "red_ghost_candle:1"),
         Arrays.asList("main_tame_ghost"),
         false,
         0
      ),
      new QuestManager.QuestTemplate(
         "event_dense_fog",
         "浓雾",
         "遭遇浓雾事件",
         "side",
         Arrays.asList(new QuestManager.QuestObjective("dense_fog_event", "浓雾事件", 1)),
         Map.of("items", "ghost_porcelain:1"),
         Arrays.asList("main_tame_ghost"),
         false,
         0
      ),
      new QuestManager.QuestTemplate(
         "event_ghost_look_down",
         "鬼低头",
         "遭遇鬼低头事件",
         "side",
         Arrays.asList(new QuestManager.QuestObjective("ghost_look_down_event", "鬼低头事件", 1)),
         Map.of("items", "mystery_coordinate:1"),
         Arrays.asList("main_tame_ghost"),
         false,
         0
      )
   );
   public static final List<QuestManager.QuestTemplate> SELLER_QUESTS = Arrays.asList(
      new QuestManager.QuestTemplate(
         "seller_basic_collection",
         "卖货郎的交易（限时1h）",
         "使用黄金容器关押一只鬼。",
         "side",
         Arrays.asList(new QuestManager.QuestObjective("collect_heavy_gold_container", "收集沉重黄金容器", 1)),
         Map.of("loot_table", "smfs:quests/seller_basic_collection"),
         new ArrayList<>(),
         false,
         0,
         60
      )
   );
   private static final Map<String, ItemMapping> ITEM_MAPPINGS = new HashMap<>();
   private static final Map<String, StrategyConfig> STRATEGY_CONFIGS = new HashMap<>();

   public static Map<String, QuestManager.QuestTemplate> getAllQuestTemplates() {
      Map<String, QuestManager.QuestTemplate> templates = new HashMap<>();

      for (QuestManager.QuestTemplate template : NEWBIE_QUESTS) {
         templates.put(template.id, template);
      }

      for (QuestManager.QuestTemplate template : MAIN_QUESTS) {
         templates.put(template.id, template);
      }

      for (QuestManager.QuestTemplate template : DAILY_QUESTS) {
         templates.put(template.id, template);
      }

      for (QuestManager.QuestTemplate template : EVENT_QUESTS) {
         templates.put(template.id, template);
      }

      for (QuestManager.QuestTemplate template : SELLER_QUESTS) {
         templates.put(template.id, template);
      }

      return templates;
   }

   public static List<QuestManager.QuestTemplate> getQuestsByType(String questType) {
      switch (questType) {
         case "newbie":
            return new ArrayList<>(NEWBIE_QUESTS);
         case "main":
            return new ArrayList<>(MAIN_QUESTS);
         case "daily":
            return new ArrayList<>(DAILY_QUESTS);
         case "side":
            List<QuestManager.QuestTemplate> sideQuests = new ArrayList<>(EVENT_QUESTS);
            sideQuests.addAll(SELLER_QUESTS);
            return sideQuests;
         default:
            return new ArrayList<>();
      }
   }

   public static List<String> getNewbieQuestIds() {
      List<String> ids = new ArrayList<>();

      for (QuestManager.QuestTemplate template : NEWBIE_QUESTS) {
         ids.add(template.id);
      }

      return ids;
   }

   public static List<String> getMainQuestIds() {
      List<String> ids = new ArrayList<>();

      for (QuestManager.QuestTemplate template : MAIN_QUESTS) {
         ids.add(template.id);
      }

      return ids;
   }

   public static List<String> getDailyQuestIds() {
      List<String> ids = new ArrayList<>();

      for (QuestManager.QuestTemplate template : DAILY_QUESTS) {
         ids.add(template.id);
      }

      return ids;
   }

   public static List<String> getEventQuestIds() {
      List<String> ids = new ArrayList<>();

      for (QuestManager.QuestTemplate template : EVENT_QUESTS) {
         ids.add(template.id);
      }

      return ids;
   }

   public static boolean shouldConsumeItems(String questId) {
      return "daily_get_corpse_oil".equals(questId) || "daily_get_corpse_piece".equals(questId);
   }

   public static String getQuestTypeName(String questType, boolean isNewbie) {
      switch (questType) {
         case "newbie":
            return "新手任务";
         case "main":
            return "主线任务";
         case "daily":
            return "日常任务";
         case "side":
            return "突发事件";
         default:
            return "未知类型";
      }
   }

   private static void initializeItemMappings() {
      ITEM_MAPPINGS.put("get_log", new ItemMapping("get_log", "oak_log", false));
      ITEM_MAPPINGS.put("get_stone", new ItemMapping("get_stone", "cobblestone", false));
      ITEM_MAPPINGS.put("get_iron", new ItemMapping("get_iron", "iron_ingot", false));
      ITEM_MAPPINGS.put("get_gold", new ItemMapping("get_gold", "gold_ingot", false));
      ITEM_MAPPINGS.put("craft_gold_container", new ItemMapping("craft_gold_container", "golden_container", false));
      ITEM_MAPPINGS.put("get_corpse_oil", new ItemMapping("get_corpse_oil", "corpse_oil", true));
      ITEM_MAPPINGS.put("get_corpse_piece", new ItemMapping("get_corpse_piece", "corpse_piece", true));
      ITEM_MAPPINGS.put("craft_ghost_faction", new ItemMapping("craft_ghost_faction", "ghost_faction", false));
      ITEM_MAPPINGS.put("collect_heavy_gold_container", new ItemMapping("collect_heavy_gold_container", "heavy_golden_container", true));
   }

   private static void initializeStrategyConfigs() {
      STRATEGY_CONFIGS.put("tame_ghost", new StrategyConfig("tame_ghost", "special"));
      STRATEGY_CONFIGS.put("craft_gold_container", new StrategyConfig("craft_gold_container", "special"));
      STRATEGY_CONFIGS.put("capture_ghost", new StrategyConfig("capture_ghost", "special"));
      STRATEGY_CONFIGS.put("enter_rich_mall", new StrategyConfig("enter_rich_mall", "special"));
      STRATEGY_CONFIGS.put("enter_chinese_medicine_shop", new StrategyConfig("enter_chinese_medicine_shop", "special"));
      STRATEGY_CONFIGS.put("trade_with_wangxiaoming", new StrategyConfig("trade_with_wangxiaoming", "special"));
      STRATEGY_CONFIGS.put("kill_zombie", new StrategyConfig("kill_zombie", "kill"));
      STRATEGY_CONFIGS.put("kill_ghost_slave", new StrategyConfig("kill_ghost_slave", "kill"));
      STRATEGY_CONFIGS.put("kill_skeleton", new StrategyConfig("kill_skeleton", "kill"));
      STRATEGY_CONFIGS.put("kill_spider", new StrategyConfig("kill_spider", "kill"));
      STRATEGY_CONFIGS.put("kill_creeper", new StrategyConfig("kill_creeper", "kill"));
      STRATEGY_CONFIGS.put("survive_5_days", new StrategyConfig("survive_5_days", "day"));
      STRATEGY_CONFIGS.put("ghost_knock_event", new StrategyConfig("ghost_knock_event", "event"));
      STRATEGY_CONFIGS.put("ghost_look_up_event", new StrategyConfig("ghost_look_up_event", "event"));
      STRATEGY_CONFIGS.put("invisible_event", new StrategyConfig("invisible_event", "event"));
      STRATEGY_CONFIGS.put("dense_fog_event", new StrategyConfig("dense_fog_event", "event"));
      STRATEGY_CONFIGS.put("ghost_look_down_event", new StrategyConfig("ghost_look_down_event", "event"));
      STRATEGY_CONFIGS.put("collect_heavy_gold_container", new StrategyConfig("collect_heavy_gold_container", "seller"));
   }

   public static ItemMapping getItemMapping(String objectiveId) {
      return ITEM_MAPPINGS.get(objectiveId);
   }

   public static StrategyConfig getStrategyConfig(String objectiveId) {
      return STRATEGY_CONFIGS.get(objectiveId);
   }

   public static boolean shouldConsumeItemsForObjective(String objectiveId) {
      ItemMapping mapping = getItemMapping(objectiveId);
      return mapping != null && mapping.shouldConsumeItems();
   }

   public static String getItemIdForObjective(String objectiveId) {
      ItemMapping mapping = getItemMapping(objectiveId);
      return mapping != null ? mapping.getItemId() : objectiveId;
   }

   public static String getStrategyTypeForObjective(String objectiveId) {
      StrategyConfig config = getStrategyConfig(objectiveId);
      return config != null ? config.getStrategyType() : "item";
   }

   static {
      initializeItemMappings();
      initializeStrategyConfigs();
   }
}
