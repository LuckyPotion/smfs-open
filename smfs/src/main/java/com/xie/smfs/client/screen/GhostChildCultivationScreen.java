package com.xie.smfs.client.screen;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.data.GhostChildData;
import com.xie.smfs.entity.ghost.GhostChildEntity;
import com.xie.smfs.event.screen.GhostChildCultivationScreenHandler;
import com.xie.smfs.event.screen.GhostControlScreenHandler;
import com.xie.smfs.manager.GoldBlockProtectionManager;
import com.xie.smfs.network.packets.ghostchild.c2s.GhostChildFusionC2SPacket;
import com.xie.smfs.registry.ModEntities;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.joml.Quaternionf;

public class GhostChildCultivationScreen extends HandledScreen<GhostChildCultivationScreenHandler> {
   private static final Identifier BACKGROUND_TEXTURE = new Identifier("smfs", "textures/gui/child.png");
   private static final int TEXTURE_WIDTH = 176;
   private static final int TEXTURE_HEIGHT = 166;
   private ButtonWidget breakthroughButton;
   private ButtonWidget summonButton;
   private ButtonWidget recallButton;
   private ButtonWidget closeButton;
   private ButtonWidget feedButton;
   private float rotation = 0.0F;

   public GhostChildCultivationScreen(GhostChildCultivationScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.backgroundWidth = 176;
      this.backgroundHeight = 166;
      this.playerInventoryTitleY = this.backgroundHeight - 94;
   }

   protected void init() {
      super.init();
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      int totalWidth = 300;
      int startX = centerX - totalWidth / 2;
      this.feedButton = ButtonWidget.builder(Text.literal("喂养"), button -> {
         if (this.client != null && this.client.player != null) {
            this.client.player.networkHandler.sendChatCommand("xie gui feed");
         }
      }).dimensions(startX, centerY + 100, 60, 20).build();
      this.breakthroughButton = ButtonWidget.builder(Text.literal("突破"), button -> this.onBreakthroughButtonClicked())
         .dimensions(startX + 80, centerY + 100, 60, 20)
         .build();
      ButtonWidget skillButton = ButtonWidget.builder(Text.literal("技能树"), button -> {
         if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.literal("§a敬请期待"), true);
            this.client.setScreen(null);
         }
      }).dimensions(startX + 160, centerY + 100, 60, 20).build();
      ButtonWidget fusionButton = ButtonWidget.builder(Text.literal("融合"), button -> this.onFusionButtonClicked())
         .dimensions(startX + 240, centerY + 100, 60, 20)
         .build();
      this.closeButton = ButtonWidget.builder(Text.literal("关闭"), button -> this.onCloseButtonClicked()).dimensions(this.width - 70, 10, 60, 20).build();
      this.summonButton = ButtonWidget.builder(Text.literal("召唤"), button -> this.onSummonButtonClicked()).dimensions(this.width - 70, 40, 60, 20).build();
      this.recallButton = ButtonWidget.builder(Text.literal("收回"), button -> this.onRecallButtonClicked()).dimensions(this.width - 70, 70, 60, 20).build();
      this.addDrawableChild(this.feedButton);
      this.addDrawableChild(this.breakthroughButton);
      this.addDrawableChild(skillButton);
      this.addDrawableChild(fusionButton);
      this.addDrawableChild(this.closeButton);
      this.addDrawableChild(this.summonButton);
      this.addDrawableChild(this.recallButton);
      this.updateButtonState();
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      super.render(context, mouseX, mouseY, delta);
      this.renderGhostChildModel(context, delta);
      this.drawGhostChildInfo(context);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }

   private void renderGhostChildModel(DrawContext context, float delta) {
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      MinecraftClient client = MinecraftClient.getInstance();
      World world = client.world;
      if (world != null) {
         try {
            GhostChildEntity ghostChild = new GhostChildEntity(ModEntities.GHOST_CHILD, world);
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(centerX, centerY + 90, 100.0F);
            float scale = 120.0F;
            matrices.scale(scale, scale, scale);
            matrices.multiply(new Quaternionf().rotateX((float)Math.toRadians(180.0)));
            matrices.multiply(new Quaternionf().rotateY((float)Math.toRadians(180.0)));
            matrices.multiply(new Quaternionf().rotateY((float)Math.toRadians(this.rotation)));
            Immediate provider = client.getBufferBuilders().getEntityVertexConsumers();
            EntityRenderer<GhostChildEntity> renderer = client.getEntityRenderDispatcher().getRenderer(ghostChild);
            renderer.render(ghostChild, 0.0F, delta, matrices, provider, 15728880);
            matrices.pop();
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (button == 0) {
         this.rotation += (float)(deltaX * 0.15F);
         if (this.rotation > 360.0F) {
            this.rotation -= 360.0F;
         } else if (this.rotation < 0.0F) {
            this.rotation += 360.0F;
         }

         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      context.drawTexture(BACKGROUND_TEXTURE, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
   }

   protected void handledScreenTick() {
      super.handledScreenTick();
      this.updateButtonState();
      this.rotation += 0.01F;
   }

   private void updateButtonState() {
      GhostChildData ghostChildData = ((GhostChildCultivationScreenHandler)this.handler).getGhostChildData();
      if (ghostChildData != null) {
         if (this.breakthroughButton != null) {
            this.breakthroughButton.active = ghostChildData.isAtBreakthroughLevel();
         }

         if (this.summonButton != null) {
            this.summonButton.active = !ghostChildData.isSummoned();
         }

         if (this.recallButton != null) {
            this.recallButton.active = ghostChildData.isSummoned();
         }

         if (this.feedButton != null) {
            this.feedButton.active = true;
         }
      }
   }

   private void onBreakthroughButtonClicked() {
      ((GhostChildCultivationScreenHandler)this.handler).performBreakthrough();
   }

   private void onSummonButtonClicked() {
      GhostChildData ghostChildData = ((GhostChildCultivationScreenHandler)this.handler).getGhostChildData();
      if (ghostChildData != null) {
         ghostChildData.summon();
         this.updateButtonState();
      }

      ((GhostChildCultivationScreenHandler)this.handler).summonGhostChild();
      if (this.client != null) {
         this.client.execute(() -> {
            try {
               Thread.sleep(100L);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }

            this.updateButtonState();
         });
      }
   }

   private void onRecallButtonClicked() {
      GhostChildData ghostChildData = ((GhostChildCultivationScreenHandler)this.handler).getGhostChildData();
      if (ghostChildData != null) {
         ghostChildData.recall();
         this.updateButtonState();
      }

      ((GhostChildCultivationScreenHandler)this.handler).recallGhostChild();
      if (this.client != null) {
         this.client.execute(() -> {
            try {
               Thread.sleep(100L);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }

            this.updateButtonState();
         });
      }
   }

   private void onCloseButtonClicked() {
      this.client
         .setScreen(
            new GhostControlScreen(
               new GhostControlScreenHandler(0, this.client.player.getInventory()),
               this.client.player.getInventory(),
               Text.translatable("screen.smfs.ghost_control")
            )
         );
   }

   private void onFusionButtonClicked() {
      if (this.client != null && this.client.player != null) {
         this.client.setScreen(null);
         ClientPlayerEntity player = this.client.player;
         GhostChildData data = ((GhostChildCultivationScreenHandler)this.handler).getGhostChildData();
         if (!GoldBlockProtectionManager.isPlayerInGoldBlockShelter(player)) {
            player.sendMessage(Text.literal("§c融合失败：需要在金块包裹状态下才能进行融合！"), false);
         } else {
            int tamedCount = PlayerEvents.countOccupiedGhostSlots(player);
            if (tamedCount < 6) {
               player.sendMessage(Text.literal("§c融合失败：需要驾驭6只鬼才能进行融合！当前驾驭：" + tamedCount + "只"), false);
            } else if (data == null || data.getLevel() < 10) {
               int level = data != null ? data.getLevel() : 0;
               player.sendMessage(Text.literal("§c融合失败：鬼童需要10级才能进行融合！当前等级：" + level), false);
            } else if (data.isSummoned()) {
               player.sendMessage(Text.literal("§c融合失败：鬼童需要处于收回状态才能进行融合！"), false);
            } else {
               GhostChildFusionC2SPacket.sendToServer();
            }
         }
      }
   }

   private void drawGhostChildInfo(DrawContext context) {
      GhostChildData ghostChildData = ((GhostChildCultivationScreenHandler)this.handler).getGhostChildData();
      if (ghostChildData != null) {
         int centerX = this.width / 2;
         int centerY = this.height / 2;
         if (ghostChildData.isAtBreakthroughLevel()) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("等级: " + ghostChildData.getLevel() + "（可突破）"), centerX - 200, 20, 16711680);
         } else {
            context.drawTextWithShadow(this.textRenderer, Text.literal("等级: " + ghostChildData.getLevel()), centerX - 200, 20, 16776960);
         }

         int requiredExp = this.getRequiredExperience(ghostChildData.getLevel());
         context.drawTextWithShadow(this.textRenderer, Text.literal("经验: " + ghostChildData.getExperience() + "/" + requiredExp), centerX - 200, 40, 16776960);
         context.drawTextWithShadow(
            this.textRenderer, Text.literal("灵异强度: " + ghostChildData.getSpiritPower() + "/" + ghostChildData.getMaxSpiritPower()), centerX - 200, 60, 16776960
         );
         context.drawTextWithShadow(this.textRenderer, Text.literal("灵异抗性: " + ghostChildData.getSpiritResistance()), centerX - 200, 80, 16776960);
         context.drawTextWithShadow(this.textRenderer, Text.literal("灵异力量: " + ghostChildData.getSpiritDamage()), centerX - 200, 100, 16776960);
         context.drawTextWithShadow(this.textRenderer, Text.literal("复苏因子: " + ghostChildData.getRevivalFactor()), centerX - 200, 120, 16776960);
         String releaseStatus = ghostChildData.isSummoned() ? "已释放" : "未释放";
         int statusColor = ghostChildData.isSummoned() ? 65280 : 16711680;
         context.drawTextWithShadow(this.textRenderer, Text.literal("状态: " + releaseStatus), centerX - 200, 140, statusColor);
      }
   }

   private int getRequiredExperience(int level) {
      return 100 * level * level;
   }

   protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
   }
}
