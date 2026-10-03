package com.xie.smfs.client;

import com.xie.smfs.Smfs;
import com.xie.smfs.api.GhostRendererAPI;
import com.xie.smfs.api.impl.GhostRendererAPIImpl;
import com.xie.smfs.block.entity.FootprintBlockEntityRenderer;
import com.xie.smfs.client.data.ClientDataManager;
import com.xie.smfs.client.preset.ScreenPresetRenderer;
import com.xie.smfs.client.preset.ScreenTearRenderer;
import com.xie.smfs.client.render.BoxGhostContainerRenderer;
import com.xie.smfs.client.render.EmptyCloudRenderer;
import com.xie.smfs.client.render.GhostDomainSkyRenderer;
import com.xie.smfs.client.render.GhostDreamHudRenderer;
import com.xie.smfs.client.render.MineralGhostMineralRenderer;
import com.xie.smfs.client.renderer.CoffinBlockRenderer;
import com.xie.smfs.client.renderer.FissuredSpearRenderer;
import com.xie.smfs.client.renderer.FloatingDirtRenderer;
import com.xie.smfs.client.renderer.GhostBedBlockRenderer;
import com.xie.smfs.client.renderer.GhostCandleBlockRenderer;
import com.xie.smfs.client.renderer.GhostDoorBlockRenderer;
import com.xie.smfs.client.renderer.GhostDreamRenderer;
import com.xie.smfs.client.renderer.GhostFurnaceBlockRenderer;
import com.xie.smfs.client.renderer.GhostMirrorBlockRenderer;
import com.xie.smfs.client.renderer.GhostPianoBlockRenderer;
import com.xie.smfs.client.renderer.GhostPortraitBlockRenderer;
import com.xie.smfs.client.renderer.GhostScreenBlockRenderer;
import com.xie.smfs.client.renderer.GhostSkeletonBlockRenderer;
import com.xie.smfs.client.renderer.GhostSlaveRenderer;
import com.xie.smfs.client.renderer.GhostTable2BlockRenderer;
import com.xie.smfs.client.renderer.GhostTableBlockRenderer;
import com.xie.smfs.client.renderer.GoldCoffinBlockRenderer;
import com.xie.smfs.client.renderer.GoldenBulletRenderer;
import com.xie.smfs.client.renderer.GraveWarningRenderer;
import com.xie.smfs.client.renderer.HumanRenderer;
import com.xie.smfs.client.renderer.NewGhostDoorBlockRenderer;
import com.xie.smfs.client.renderer.PlayerGhostRenderer;
import com.xie.smfs.client.renderer.RedCoffinBlockRenderer;
import com.xie.smfs.client.renderer.SpiritBrewingStandBlockRenderer;
import com.xie.smfs.client.renderer.UniversalGhostRenderer;
import com.xie.smfs.client.renderer.block.Footprint2BlockEntityRenderer;
import com.xie.smfs.client.renderer.block.GraveMoundBlockRenderer;
import com.xie.smfs.client.screen.GhostChildCultivationScreen;
import com.xie.smfs.client.screen.GhostChildFeedScreen;
import com.xie.smfs.client.screen.GhostControlScreen;
import com.xie.smfs.client.screen.GhostTamingScreen;
import com.xie.smfs.client.screen.PlayNoticeScreen;
import com.xie.smfs.client.screen.QuestHandledScreen;
import com.xie.smfs.client.screen.RoyalCurseScreen;
import com.xie.smfs.client.screen.SpiritBrewingStandScreen;
import com.xie.smfs.client.util.GhostShadowHeadCameraManager;
import com.xie.smfs.config.ConfigManager;
import com.xie.smfs.event.KeyEventHandler;
import com.xie.smfs.network.ClientModNetwork;
import com.xie.smfs.network.core.ClientSpiritNetworkHandler;
import com.xie.smfs.network.core.GhostPressureDetectionClientHandler;
import com.xie.smfs.network.packets.common.s2c.BoneTreeCoordinatesS2CPacket;
import com.xie.smfs.network.packets.common.s2c.SpiritDamageS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.AberrationPopupS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.GhostAbilityPopupS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.GhostLotFlipS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.LuoQianCombatStateS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.ScreenEffectS2CPacket;
import com.xie.smfs.registry.ModBlockEntities;
import com.xie.smfs.registry.ModBlocks;
import com.xie.smfs.registry.ModEntities;
import com.xie.smfs.registry.ModFluids;
import com.xie.smfs.registry.ModItems;
import com.xie.smfs.registry.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.impl.blockrenderlayer.BlockRenderLayerMapImpl;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmfsClient implements ClientModInitializer {
   public static final String MOD_ID = "smfs";
   public static final Logger LOGGER = LoggerFactory.getLogger("smfs");

   @Override
   public void onInitializeClient() {
      LOGGER.info("=================开始初始化神秘复苏模组客户端=================");
      EntityRendererRegistry.register(ModEntities.FOOD_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.TAITOU_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.DITOU_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.FOG_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.QIAOMEN_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.GHOST_MERCHANT, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.GOODS_SELLER_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.BOX_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.VILLAGER_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.JUMP_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.UNTOUCHABLE_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.DEATH_SIGHT_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.BLOCK_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.LOST_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.GHOST_WIND, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.GHOST_PRESSURE, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.PLAYER_GHOST, PlayerGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.GHOST_SLAVE, GhostSlaveRenderer::new);
      EntityRendererRegistry.register(ModEntities.GIANT_SHADOW_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.GANSHI_BRIDE_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.CROP_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.STEP_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.TRASH_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.WATER_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.BURN_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.CRYING_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.SUONA_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.GONG_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.SILENT_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.GHOST_OFFICER, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.WANG_XIAO_MING, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.CHEN_DOCTOR, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.CAO_YAN_HUA, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.LIU_XIAO_YU, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.YANG_JIAN, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.LI_JUN, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.YE_ZHEN, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.FENG_QUAN, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.CAO_YANG, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.FANG_SHI_MIN, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.NPC1, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.NPC2, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.NPC3, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.NPC4, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.NPC5, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.NPC6, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.YAN_LI, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.LI_LE_PING, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.ZHAO_KAI_MING, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.YANG_XIAO, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.GOLDEN_BULLET, GoldenBulletRenderer::new);
      EntityRendererRegistry.register(ModEntities.FLOATING_DIRT, FloatingDirtRenderer::new);
      EntityRendererRegistry.register(ModEntities.GRAVE_WARNING, GraveWarningRenderer::new);
      EntityRendererRegistry.register(ModEntities.FISSURED_SPEAR, FissuredSpearRenderer::new);
      EntityRendererRegistry.register(ModEntities.PLAGUE_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.MINERAL_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.SHADOW_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.GHOST_SHADOW_HEAD, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.DOOR_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.GHOST_SMOKE, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.GHOST_CHILD, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.SNEAK_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.CLOTHES_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.PUPPET_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.GRAVE_EARTH_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.GIANT_MALE_CORPSE_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.GHOST_DREAM, GhostDreamRenderer::new);
      EntityRendererRegistry.register(ModEntities.XINKAI_GHOST, UniversalGhostRenderer::new);
      EntityRendererRegistry.register(ModEntities.YIN_QI, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.XIAN_WANG, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.GUI_NIAO, HumanRenderer::new);
      EntityRendererRegistry.register(ModEntities.LUO_QIAN_GHOST, UniversalGhostRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.GHOST_COFFIN_BLOCK_ENTITY, CoffinBlockRenderer::new);
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_COFFIN, RenderLayer.method_23583());
      BlockEntityRendererRegistry.register(ModBlockEntities.RED_COFFIN_BLOCK_ENTITY, RedCoffinBlockRenderer::new);
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.RED_COFFIN, RenderLayer.method_23583());
      BlockEntityRendererRegistry.register(ModBlockEntities.GOLD_COFFIN_BLOCK_ENTITY, GoldCoffinBlockRenderer::new);
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GOLD_COFFIN, RenderLayer.method_23583());
      BlockEntityRendererRegistry.register(ModBlockEntities.GHOST_TABLE_BLOCK_ENTITY, GhostTableBlockRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.GHOST_TABLE2_BLOCK_ENTITY, GhostTable2BlockRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.GHOST_BED_BLOCK_ENTITY, GhostBedBlockRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.GHOST_PIANO_BLOCK_ENTITY, GhostPianoBlockRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.GHOST_DOOR_BLOCK_ENTITY, GhostDoorBlockRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.NEW_GHOST_DOOR_BLOCK_ENTITY, NewGhostDoorBlockRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.GHOST_SCREEN_BLOCK_ENTITY, GhostScreenBlockRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.GHOST_CANDLE_BLOCK_ENTITY, GhostCandleBlockRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.GHOST_SKELETON_BLOCK_ENTITY, GhostSkeletonBlockRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.GHOST_PORTRAIT_BLOCK_ENTITY, GhostPortraitBlockRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.GHOST_MIRROR_BLOCK_ENTITY, GhostMirrorBlockRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.GHOST_FURNACE_BLOCK_ENTITY, GhostFurnaceBlockRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.SPIRIT_BREWING_STAND_BLOCK_ENTITY, SpiritBrewingStandBlockRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.FOOTPRINT_BLOCK_ENTITY, FootprintBlockEntityRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.FOOTPRINT2_BLOCK_ENTITY, Footprint2BlockEntityRenderer::new);
      BlockEntityRendererRegistry.register(ModBlockEntities.GRAVE_MOUND_BLOCK_ENTITY, GraveMoundBlockRenderer::new);
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_TABLE, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_TABLE2, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_BED, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_PIANO, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_DOOR, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_PIANO, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_DOOR, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.NEW_GHOST_DOOR, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_BED, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_TABLE, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_TABLE2, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_SCREEN, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_CANDLE, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_SKELETON, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_PORTRAIT, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.GHOST_MIRROR, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.SPIRIT_BREWING_STAND, RenderLayer.method_23583());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.FOOTPRINT, RenderLayer.method_23581());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.FOOTPRINT2, RenderLayer.method_23581());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.DIRTY_CROP, RenderLayer.method_23581());
      BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.FILTHY_CROP, RenderLayer.method_23581());
      EffectRenderHandler.register();
      MusicBoxCurseRenderer.register();
      MusicBoxCurseSoundHandler.init();
      GiantShadowGhostSneakRenderer.register();
      GhostPullRenderHandler.register();
      BoxGhostContainerRenderer.register();
      MineralGhostMineralRenderer.register();
      HandledScreens.method_17542(ModScreenHandlers.GHOST_CONTROL_SCREEN_HANDLER, GhostControlScreen::new);
      HandledScreens.method_17542(ModScreenHandlers.GHOST_TAMING_SCREEN_HANDLER, GhostTamingScreen::new);
      HandledScreens.method_17542(ModScreenHandlers.QUEST_SCREEN_HANDLER, QuestHandledScreen::new);
      HandledScreens.method_17542(ModScreenHandlers.GHOST_CHILD_CULTIVATION_SCREEN_HANDLER, GhostChildCultivationScreen::new);
      HandledScreens.method_17542(ModScreenHandlers.ROYAL_CURSE_SCREEN_HANDLER, RoyalCurseScreen::new);
      HandledScreens.method_17542(ModScreenHandlers.GHOST_CHILD_FEED_SCREEN_HANDLER, GhostChildFeedScreen::new);
      HandledScreens.method_17542(ModScreenHandlers.SPIRIT_BREWING_STAND_SCREEN_HANDLER, SpiritBrewingStandScreen::new);
      KeyEventHandler.register();
      ClientDataManager.init();
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (PlayNoticeScreen.shouldShow() && client.field_1687 != null) {
            PlayNoticeScreen.markAsShown();
            client.method_1507(new PlayNoticeScreen());
         }
      });
      ClientSpiritNetworkHandler.register();
      this.registerBowPredicates();
      GhostAbilityPopupS2CPacket.register();
      AberrationPopupS2CPacket.register();
      ScreenEffectS2CPacket.register();
      GhostLotFlipS2CPacket.register();
      BoneTreeCoordinatesS2CPacket.registerClient();
      SpiritHudOverlay.register();
      GhostLotFlipOverlay.register();
      LuoQianHudRenderer.register();
      LuoQianCombatStateS2CPacket.registerClient();
      ClientEventHandler.register();
      GhostPressureDetectionClientHandler.register();
      SpiritDamageS2CPacket.registerClient();
      GhostDreamHudRenderer.register();
      ClientModNetwork.register();
      HumanSkinPaperClientHandler.register();
      PotionColorProvider.register();
      DimensionRenderingRegistry.registerSkyRenderer(World.field_25179, new GhostDomainSkyRenderer());
      DimensionRenderingRegistry.registerSkyRenderer(Smfs.SPIRIT_REALM_DIMENSION, new GhostDomainSkyRenderer());
      DimensionRenderingRegistry.registerSkyRenderer(Smfs.GHOST_DREAM_DIMENSION, new GhostDomainSkyRenderer());
      DimensionRenderingRegistry.registerCloudRenderer(World.field_25179, new EmptyCloudRenderer());
      DimensionRenderingRegistry.registerCloudRenderer(Smfs.SPIRIT_REALM_DIMENSION, new EmptyCloudRenderer());
      DimensionRenderingRegistry.registerCloudRenderer(Smfs.GHOST_DREAM_DIMENSION, new EmptyCloudRenderer());
      ConfigManager.initialize();
      IrisCompatibility.init();
      WorldRenderEvents.LAST.register(GhostDomainSkyRenderer::renderAtLast);
      HudRenderCallback.EVENT.register(new ScreenTearRenderer());
      HudRenderCallback.EVENT.register(new ScreenPresetRenderer());
      HudRenderCallback.EVENT.register((HudRenderCallback)(context, tickDelta) -> GhostShadowHeadCameraManager.renderFilter(context));
      AttackEntityCallback.EVENT.register((AttackEntityCallback)(player, world, hand, entity, hitResult) -> {
         if (world.field_9236 && player.method_5998(hand).method_31574(ModItems.RUSTY_OLD_BROADSWORD)) {
            ScreenTearRenderer.onRustyBladeHit();
         }

         return ActionResult.field_5811;
      });
      DeafnessClientHandler.init();
      FluidRenderHandlerRegistry.INSTANCE
         .register(
            ModFluids.BLOOD_LAKE_STILL,
            ModFluids.BLOOD_LAKE_FLOWING,
            new SimpleFluidRenderHandler(new Identifier("minecraft:block/water_still"), new Identifier("minecraft:block/water_flow"), 16711680)
         );
      BlockRenderLayerMapImpl.INSTANCE.putFluids(RenderLayer.method_23583(), new Fluid[]{ModFluids.BLOOD_LAKE_STILL, ModFluids.BLOOD_LAKE_FLOWING});
      FluidRenderHandlerRegistry.INSTANCE
         .register(
            ModFluids.GHOST_LAKE_STILL,
            ModFluids.GHOST_LAKE_FLOWING,
            new SimpleFluidRenderHandler(new Identifier("minecraft:block/water_still"), new Identifier("minecraft:block/water_flow"), 65535)
         );
      BlockRenderLayerMapImpl.INSTANCE.putFluids(RenderLayer.method_23583(), new Fluid[]{ModFluids.GHOST_LAKE_STILL, ModFluids.GHOST_LAKE_FLOWING});
      LOGGER.info("=================神秘复苏模组客户端初始化完成=================");
   }

   private static void registerGhostRendererAPI() {
      GhostRendererAPI.register(new GhostRendererAPIImpl());
      LOGGER.info("鬼魂渲染器API注册完成");
   }

   private void registerBowPredicates() {
      ModelPredicateProviderRegistry.method_27879(ModItems.GHOST_BOW, new Identifier("pulling"), (stack, world, entity, seed) -> {
         if (entity == null) {
            return 0.0F;
         } else {
            return entity.method_6115() && entity.method_6030() == stack ? 1.0F : 0.0F;
         }
      });
      ModelPredicateProviderRegistry.method_27879(ModItems.GHOST_BOW, new Identifier("pull"), (stack, world, entity, seed) -> {
         if (entity == null) {
            return 0.0F;
         }

         if (entity.method_6030() != stack) {
            return 0.0F;
         }

         float useTicks = stack.method_7935() - entity.method_6014();
         float pullProgress = useTicks / 20.0F;
         return Math.min(pullProgress, 1.0F);
      });
      LOGGER.info("鬼弓模型predicate注册完成");
   }

   static {
      registerGhostRendererAPI();
      LOGGER.info("客户端API服务已在静态初始化块中注册完成");
   }
}
