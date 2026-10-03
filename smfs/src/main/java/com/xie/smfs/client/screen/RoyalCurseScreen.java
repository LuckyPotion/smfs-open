package com.xie.smfs.client.screen;

import com.xie.smfs.data.RoyalCurseData;
import com.xie.smfs.data.RoyalCurseServantData;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import com.xie.smfs.event.screen.GhostControlScreenHandler;
import com.xie.smfs.event.screen.RoyalCurseScreenHandler;
import com.xie.smfs.registry.ModEntities;
import com.xie.smfs.util.GhostUtils;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoyalCurseScreen extends HandledScreen<RoyalCurseScreenHandler> {
   private static final Logger LOGGER = LoggerFactory.getLogger(RoyalCurseScreen.class);
   private static final Identifier BACKGROUND_TEXTURE = new Identifier("smfs", "textures/gui/child.png");
   private int selectedServantIndex = 0;
   private ButtonWidget summonButton;
   private ButtonWidget recallButton;
   private ButtonWidget summonAllButton;
   private ButtonWidget recallAllButton;
   private ButtonWidget closeButton;
   private ButtonWidget prevButton;
   private ButtonWidget nextButton;
   private float rotation = 0.0F;

   public RoyalCurseScreen(RoyalCurseScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.backgroundWidth = 176;
      this.backgroundHeight = 166;
      this.playerInventoryTitleY = this.backgroundHeight - 94;
   }

   protected void init() {
      super.init();
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      int totalWidth = 340;
      int startX = centerX - totalWidth / 2;
      this.summonButton = ButtonWidget.builder(Text.literal("召唤"), button -> this.onSummonButtonClicked()).dimensions(startX, centerY + 100, 60, 20).build();
      this.recallButton = ButtonWidget.builder(Text.literal("收回"), button -> this.onRecallButtonClicked())
         .dimensions(startX + 80, centerY + 100, 60, 20)
         .build();
      this.summonAllButton = ButtonWidget.builder(Text.literal("全部召唤"), button -> this.onSummonAllButtonClicked())
         .dimensions(startX + 160, centerY + 100, 80, 20)
         .build();
      this.recallAllButton = ButtonWidget.builder(Text.literal("全部收回"), button -> this.onRecallAllButtonClicked())
         .dimensions(startX + 260, centerY + 100, 80, 20)
         .build();
      this.prevButton = ButtonWidget.builder(Text.literal("<<"), button -> this.onPrevButtonClicked()).dimensions(centerX - 150, centerY, 40, 20).build();
      this.nextButton = ButtonWidget.builder(Text.literal(">>"), button -> this.onNextButtonClicked()).dimensions(centerX + 110, centerY, 40, 20).build();
      this.closeButton = ButtonWidget.builder(Text.literal("关闭"), button -> this.onCloseButtonClicked()).dimensions(this.width - 70, 10, 60, 20).build();
      this.addDrawableChild(this.summonButton);
      this.addDrawableChild(this.recallButton);
      this.addDrawableChild(this.summonAllButton);
      this.addDrawableChild(this.recallAllButton);
      this.addDrawableChild(this.prevButton);
      this.addDrawableChild(this.nextButton);
      this.addDrawableChild(this.closeButton);
      this.updateButtonState();
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      super.render(context, mouseX, mouseY, delta);
      this.renderServantModel(context, delta);
      this.drawServantInfo(context);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }

   private void renderServantModel(DrawContext context, float delta) {
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      MinecraftClient client = MinecraftClient.getInstance();
      World world = client.world;
      if (world != null) {
         try {
            RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.handler).getRoyalCurseData();
            if (royalCurseData.getServantCount() > 0 && this.selectedServantIndex < royalCurseData.getServantCount()) {
               RoyalCurseServantData servantData = royalCurseData.getServant(this.selectedServantIndex);
               if (servantData != null) {
                  PlayerGhostEntity servant = new PlayerGhostEntity(ModEntities.PLAYER_GHOST, world);
                  servant.setPlayerName(servantData.getPlayerName());
                  servant.setPlayerUuid(servantData.getPlayerUuid());
                  servant.setGhostDomainColor(servantData.getGhostDomainColor());
                  MatrixStack matrices = context.getMatrices();
                  matrices.push();
                  matrices.translate(centerX, centerY + 90, 100.0F);
                  float scale = 60.0F;
                  matrices.scale(scale, scale, scale);
                  matrices.multiply(new Quaternionf().rotateX((float)Math.toRadians(180.0)));
                  matrices.multiply(new Quaternionf().rotateY((float)Math.toRadians(180.0)));
                  matrices.multiply(new Quaternionf().rotateY((float)Math.toRadians(this.rotation)));
                  Immediate provider = client.getBufferBuilders().getEntityVertexConsumers();
                  EntityRenderer<PlayerGhostEntity> renderer = client.getEntityRenderDispatcher().getRenderer(servant);
                  renderer.render(servant, 0.0F, delta, matrices, provider, 15728880);
                  matrices.pop();
               }
            }
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
      RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.handler).getRoyalCurseData();
      int servantCount = royalCurseData.getServantCount();
      this.prevButton.active = servantCount > 0 && this.selectedServantIndex > 0;
      this.nextButton.active = servantCount > 0 && this.selectedServantIndex < servantCount - 1;
      boolean hasSummonableServants = false;
      boolean hasRecallableServants = false;

      for (int i = 0; i < servantCount; i++) {
         RoyalCurseServantData servantData = royalCurseData.getServant(i);
         if (servantData != null) {
            if (!servantData.isReleased()) {
               hasSummonableServants = true;
            }

            if (servantData.isReleased()) {
               hasRecallableServants = true;
            }
         }
      }

      this.summonAllButton.active = hasSummonableServants;
      this.recallAllButton.active = hasRecallableServants;
      if (servantCount > 0 && this.selectedServantIndex < servantCount) {
         RoyalCurseServantData servantData = royalCurseData.getServant(this.selectedServantIndex);
         if (servantData != null) {
            this.summonButton.active = !servantData.isReleased();
            this.recallButton.active = servantData.isReleased();
         }
      } else {
         this.summonButton.active = false;
         this.recallButton.active = false;
      }
   }

   private void onSummonButtonClicked() {
      LOGGER.info("客户端：点击召唤按钮，索引: {}", this.selectedServantIndex);
      RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.handler).getRoyalCurseData();
      if (royalCurseData.getServantCount() > 0 && this.selectedServantIndex < royalCurseData.getServantCount()) {
         RoyalCurseServantData servantData = royalCurseData.getServant(this.selectedServantIndex);
         if (servantData != null) {
            servantData.summon();
            this.updateButtonState();
         }
      }

      if (this.client.player != null) {
         this.client.player.networkHandler.sendCommand("xie servant summon " + this.selectedServantIndex);
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
      LOGGER.info("客户端：点击收回按钮，索引: {}", this.selectedServantIndex);
      RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.handler).getRoyalCurseData();
      if (royalCurseData.getServantCount() > 0 && this.selectedServantIndex < royalCurseData.getServantCount()) {
         RoyalCurseServantData servantData = royalCurseData.getServant(this.selectedServantIndex);
         if (servantData != null) {
            servantData.recall();
            this.updateButtonState();
         }
      }

      if (this.client.player != null) {
         this.client.player.networkHandler.sendCommand("xie servant recall " + this.selectedServantIndex);
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

   private void onSummonAllButtonClicked() {
      LOGGER.info("客户端：点击全部召唤按钮");
      RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.handler).getRoyalCurseData();
      int servantCount = royalCurseData.getServantCount();

      for (int i = 0; i < servantCount; i++) {
         RoyalCurseServantData servantData = royalCurseData.getServant(i);
         if (servantData != null && !servantData.isReleased()) {
            servantData.summon();
         }
      }

      this.updateButtonState();
      if (this.client.player != null) {
         this.client.player.networkHandler.sendCommand("xie servant summon_all");
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

   private void onRecallAllButtonClicked() {
      LOGGER.info("客户端：点击全部收回按钮");
      RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.handler).getRoyalCurseData();
      int servantCount = royalCurseData.getServantCount();

      for (int i = 0; i < servantCount; i++) {
         RoyalCurseServantData servantData = royalCurseData.getServant(i);
         if (servantData != null && servantData.isReleased()) {
            servantData.recall();
         }
      }

      this.updateButtonState();
      if (this.client.player != null) {
         this.client.player.networkHandler.sendCommand("xie servant recall_all");
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

   private void onPrevButtonClicked() {
      if (this.selectedServantIndex > 0) {
         this.selectedServantIndex--;
      }
   }

   private void onNextButtonClicked() {
      RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.handler).getRoyalCurseData();
      if (this.selectedServantIndex < royalCurseData.getServantCount() - 1) {
         this.selectedServantIndex++;
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

   private void drawServantInfo(DrawContext context) {
      RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.handler).getRoyalCurseData();
      int servantCount = royalCurseData.getServantCount();
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      if (servantCount > 0 && this.selectedServantIndex < servantCount) {
         RoyalCurseServantData servantData = royalCurseData.getServant(this.selectedServantIndex);
         if (servantData != null) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("奴仆数量: " + servantCount + "/6"), centerX - 150, 10, 16776960);
            context.drawTextWithShadow(
               this.textRenderer, Text.literal("当前: " + (this.selectedServantIndex + 1) + "/" + servantCount), centerX - 150, 25, 16776960
            );
            context.drawTextWithShadow(this.textRenderer, Text.literal("名称: " + servantData.getPlayerName()), centerX - 150, 40, 16776960);
            context.drawTextWithShadow(
               this.textRenderer,
               Text.literal("强度: " + servantData.getSpiritualStrength() + "/" + servantData.getMaxSpiritualStrength()),
               centerX - 150,
               55,
               16776960
            );
            context.drawTextWithShadow(this.textRenderer, Text.literal("抗性: " + servantData.getSpiritualResistance()), centerX - 150, 70, 16776960);
            context.drawTextWithShadow(this.textRenderer, Text.literal("伤害: " + servantData.getSpiritualDamage()), centerX - 150, 85, 16776960);
            context.drawTextWithShadow(this.textRenderer, Text.literal("复苏: " + servantData.getRecoveryFactor()), centerX - 150, 100, 16776960);
            context.drawTextWithShadow(this.textRenderer, Text.literal("状态: " + (servantData.isReleased() ? "已释放" : "未释放")), centerX - 150, 115, 16711680);
            context.drawTextWithShadow(this.textRenderer, Text.literal("鬼域: " + servantData.getGhostDomainLevel()), centerX + 110, 40, 16776960);
            context.drawTextWithShadow(this.textRenderer, Text.literal("半径: " + servantData.getGhostDomainRadius()), centerX + 110, 55, 16776960);
            List<String> playerGhosts = servantData.getPlayerGhosts();
            if (!playerGhosts.isEmpty()) {
               int lineHeight = 10;
               int currentY = 70;
               context.drawTextWithShadow(this.textRenderer, Text.literal("灵异: "), centerX + 110, currentY, 16776960);
               currentY += lineHeight + 5;
               int maxWidth = 100;
               StringBuilder currentLine = new StringBuilder();

               for (int i = 0; i < playerGhosts.size(); i++) {
                  String ghostType = playerGhosts.get(i);
                  String displayName = GhostUtils.getGhostDisplayName(ghostType);
                  int textWidth = this.textRenderer.getWidth(currentLine + (currentLine.length() > 0 ? "|" : "") + displayName);
                  if (textWidth > maxWidth) {
                     context.drawTextWithShadow(this.textRenderer, Text.literal(currentLine.toString()), centerX + 110, currentY, 16776960);
                     currentY += lineHeight;
                     currentLine = new StringBuilder(displayName);
                  } else {
                     if (currentLine.length() > 0) {
                        currentLine.append("|");
                     }

                     currentLine.append(displayName);
                  }
               }

               if (currentLine.length() > 0) {
                  context.drawTextWithShadow(this.textRenderer, Text.literal(currentLine.toString()), centerX + 110, currentY, 16776960);
               }
            }
         }
      }
   }

   protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
   }
}
