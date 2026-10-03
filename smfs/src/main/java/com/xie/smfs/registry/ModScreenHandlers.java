package com.xie.smfs.registry;

import com.xie.smfs.Smfs;
import com.xie.smfs.block.entity.SpiritBrewingStandScreenHandler;
import com.xie.smfs.event.screen.GhostChildCultivationScreenHandler;
import com.xie.smfs.event.screen.GhostChildFeedScreenHandler;
import com.xie.smfs.event.screen.GhostControlScreenHandler;
import com.xie.smfs.event.screen.GhostTamingScreenHandler;
import com.xie.smfs.event.screen.QuestScreenHandler;
import com.xie.smfs.event.screen.RoyalCurseScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
   public static final ScreenHandlerType<GhostControlScreenHandler> GHOST_CONTROL_SCREEN_HANDLER = new ExtendedScreenHandlerType(
      (syncId, inventory, buf) -> new GhostControlScreenHandler(syncId, inventory, buf)
   );
   public static final ScreenHandlerType<GhostTamingScreenHandler> GHOST_TAMING_SCREEN_HANDLER = new ExtendedScreenHandlerType(
      (syncId, inventory, buf) -> new GhostTamingScreenHandler(syncId, inventory, buf)
   );
   public static final ScreenHandlerType<QuestScreenHandler> QUEST_SCREEN_HANDLER = new ExtendedScreenHandlerType(
      (syncId, inventory, buf) -> new QuestScreenHandler(syncId, inventory, buf)
   );
   public static final ScreenHandlerType<GhostChildCultivationScreenHandler> GHOST_CHILD_CULTIVATION_SCREEN_HANDLER = new ExtendedScreenHandlerType(
      (syncId, inventory, buf) -> new GhostChildCultivationScreenHandler(syncId, inventory, buf)
   );
   public static final ScreenHandlerType<RoyalCurseScreenHandler> ROYAL_CURSE_SCREEN_HANDLER = new ExtendedScreenHandlerType(
      (syncId, inventory, buf) -> new RoyalCurseScreenHandler(syncId, inventory, buf)
   );
   public static final ScreenHandlerType<GhostChildFeedScreenHandler> GHOST_CHILD_FEED_SCREEN_HANDLER = new ExtendedScreenHandlerType(
      (syncId, inventory, buf) -> new GhostChildFeedScreenHandler(syncId, inventory, buf)
   );
   public static final ScreenHandlerType<SpiritBrewingStandScreenHandler> SPIRIT_BREWING_STAND_SCREEN_HANDLER = new ScreenHandlerType(
      SpiritBrewingStandScreenHandler::new, FeatureFlags.VANILLA_FEATURES
   );

   public static void registerScreenHandlers() {
      Registry.register(Registries.SCREEN_HANDLER, new Identifier("smfs", "ghost_control"), GHOST_CONTROL_SCREEN_HANDLER);
      Registry.register(Registries.SCREEN_HANDLER, new Identifier("smfs", "ghost_taming"), GHOST_TAMING_SCREEN_HANDLER);
      Registry.register(Registries.SCREEN_HANDLER, new Identifier("smfs", "quest"), QUEST_SCREEN_HANDLER);
      Registry.register(Registries.SCREEN_HANDLER, new Identifier("smfs", "ghost_child_cultivation"), GHOST_CHILD_CULTIVATION_SCREEN_HANDLER);
      Registry.register(Registries.SCREEN_HANDLER, new Identifier("smfs", "royal_curse"), ROYAL_CURSE_SCREEN_HANDLER);
      Registry.register(Registries.SCREEN_HANDLER, new Identifier("smfs", "ghost_child_feed"), GHOST_CHILD_FEED_SCREEN_HANDLER);
      Registry.register(Registries.SCREEN_HANDLER, new Identifier("smfs", "spirit_brewing_stand"), SPIRIT_BREWING_STAND_SCREEN_HANDLER);
      Smfs.LOGGER.info("成功注册厉鬼控制屏幕处理器 - 模块ID: {}", "smfs");
      Smfs.LOGGER.info("成功注册驭鬼界面处理器 - 模块ID: {}", "smfs");
      Smfs.LOGGER.info("成功注册任务界面处理器 - 模块ID: {}", "smfs");
      Smfs.LOGGER.info("成功注册鬼童养成界面处理器 - 模块ID: {}", "smfs");
      Smfs.LOGGER.info("成功注册王家诅咒界面处理器 - 模块ID: {}", "smfs");
   }
}
