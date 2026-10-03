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
      this.field_2792 = 176;
      this.field_2779 = 166;
      this.field_25270 = this.field_2779 - 94;
   }

   protected void method_25426() {
      super.method_25426();
      int centerX = this.field_22789 / 2;
      int centerY = this.field_22790 / 2;
      int totalWidth = 340;
      int startX = centerX - totalWidth / 2;
      this.summonButton = ButtonWidget.method_46430(Text.method_43470("召唤"), button -> this.onSummonButtonClicked())
         .method_46434(startX, centerY + 100, 60, 20)
         .method_46431();
      this.recallButton = ButtonWidget.method_46430(Text.method_43470("收回"), button -> this.onRecallButtonClicked())
         .method_46434(startX + 80, centerY + 100, 60, 20)
         .method_46431();
      this.summonAllButton = ButtonWidget.method_46430(Text.method_43470("全部召唤"), button -> this.onSummonAllButtonClicked())
         .method_46434(startX + 160, centerY + 100, 80, 20)
         .method_46431();
      this.recallAllButton = ButtonWidget.method_46430(Text.method_43470("全部收回"), button -> this.onRecallAllButtonClicked())
         .method_46434(startX + 260, centerY + 100, 80, 20)
         .method_46431();
      this.prevButton = ButtonWidget.method_46430(Text.method_43470("<<"), button -> this.onPrevButtonClicked())
         .method_46434(centerX - 150, centerY, 40, 20)
         .method_46431();
      this.nextButton = ButtonWidget.method_46430(Text.method_43470(">>"), button -> this.onNextButtonClicked())
         .method_46434(centerX + 110, centerY, 40, 20)
         .method_46431();
      this.closeButton = ButtonWidget.method_46430(Text.method_43470("关闭"), button -> this.onCloseButtonClicked())
         .method_46434(this.field_22789 - 70, 10, 60, 20)
         .method_46431();
      this.method_37063(this.summonButton);
      this.method_37063(this.recallButton);
      this.method_37063(this.summonAllButton);
      this.method_37063(this.recallAllButton);
      this.method_37063(this.prevButton);
      this.method_37063(this.nextButton);
      this.method_37063(this.closeButton);
      this.updateButtonState();
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      super.method_25394(context, mouseX, mouseY, delta);
      this.renderServantModel(context, delta);
      this.drawServantInfo(context);
      this.method_2380(context, mouseX, mouseY);
   }

   private void renderServantModel(DrawContext context, float delta) {
      int centerX = this.field_22789 / 2;
      int centerY = this.field_22790 / 2;
      MinecraftClient client = MinecraftClient.method_1551();
      World world = client.field_1687;
      if (world != null) {
         try {
            RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.field_2797).getRoyalCurseData();
            if (royalCurseData.getServantCount() > 0 && this.selectedServantIndex < royalCurseData.getServantCount()) {
               RoyalCurseServantData servantData = royalCurseData.getServant(this.selectedServantIndex);
               if (servantData != null) {
                  PlayerGhostEntity servant = new PlayerGhostEntity(ModEntities.PLAYER_GHOST, world);
                  servant.setPlayerName(servantData.getPlayerName());
                  servant.setPlayerUuid(servantData.getPlayerUuid());
                  servant.setGhostDomainColor(servantData.getGhostDomainColor());
                  MatrixStack matrices = context.method_51448();
                  matrices.method_22903();
                  matrices.method_46416(centerX, centerY + 90, 100.0F);
                  float scale = 60.0F;
                  matrices.method_22905(scale, scale, scale);
                  matrices.method_22907(new Quaternionf().rotateX((float)Math.toRadians(180.0)));
                  matrices.method_22907(new Quaternionf().rotateY((float)Math.toRadians(180.0)));
                  matrices.method_22907(new Quaternionf().rotateY((float)Math.toRadians(this.rotation)));
                  Immediate provider = client.method_22940().method_23000();
                  EntityRenderer<PlayerGhostEntity> renderer = client.method_1561().method_3953(servant);
                  renderer.method_3936(servant, 0.0F, delta, matrices, provider, 15728880);
                  matrices.method_22909();
               }
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (button == 0) {
         this.rotation += (float)(deltaX * 0.15F);
         if (this.rotation > 360.0F) {
            this.rotation -= 360.0F;
         } else if (this.rotation < 0.0F) {
            this.rotation += 360.0F;
         }

         return true;
      } else {
         return super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   protected void method_2389(DrawContext context, float delta, int mouseX, int mouseY) {
      context.method_25290(BACKGROUND_TEXTURE, 0, 0, 0.0F, 0.0F, this.field_22789, this.field_22790, this.field_22789, this.field_22790);
   }

   protected void method_37432() {
      super.method_37432();
      this.updateButtonState();
      this.rotation += 0.01F;
   }

   private void updateButtonState() {
      RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.field_2797).getRoyalCurseData();
      int servantCount = royalCurseData.getServantCount();
      this.prevButton.field_22763 = servantCount > 0 && this.selectedServantIndex > 0;
      this.nextButton.field_22763 = servantCount > 0 && this.selectedServantIndex < servantCount - 1;
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

      this.summonAllButton.field_22763 = hasSummonableServants;
      this.recallAllButton.field_22763 = hasRecallableServants;
      if (servantCount > 0 && this.selectedServantIndex < servantCount) {
         RoyalCurseServantData servantData = royalCurseData.getServant(this.selectedServantIndex);
         if (servantData != null) {
            this.summonButton.field_22763 = !servantData.isReleased();
            this.recallButton.field_22763 = servantData.isReleased();
         }
      } else {
         this.summonButton.field_22763 = false;
         this.recallButton.field_22763 = false;
      }
   }

   private void onSummonButtonClicked() {
      LOGGER.info("客户端：点击召唤按钮，索引: {}", this.selectedServantIndex);
      RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.field_2797).getRoyalCurseData();
      if (royalCurseData.getServantCount() > 0 && this.selectedServantIndex < royalCurseData.getServantCount()) {
         RoyalCurseServantData servantData = royalCurseData.getServant(this.selectedServantIndex);
         if (servantData != null) {
            servantData.summon();
            this.updateButtonState();
         }
      }

      if (this.field_22787.field_1724 != null) {
         this.field_22787.field_1724.field_3944.method_45731("xie servant summon " + this.selectedServantIndex);
         this.field_22787.execute(() -> {
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
      RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.field_2797).getRoyalCurseData();
      if (royalCurseData.getServantCount() > 0 && this.selectedServantIndex < royalCurseData.getServantCount()) {
         RoyalCurseServantData servantData = royalCurseData.getServant(this.selectedServantIndex);
         if (servantData != null) {
            servantData.recall();
            this.updateButtonState();
         }
      }

      if (this.field_22787.field_1724 != null) {
         this.field_22787.field_1724.field_3944.method_45731("xie servant recall " + this.selectedServantIndex);
         this.field_22787.execute(() -> {
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
      RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.field_2797).getRoyalCurseData();
      int servantCount = royalCurseData.getServantCount();

      for (int i = 0; i < servantCount; i++) {
         RoyalCurseServantData servantData = royalCurseData.getServant(i);
         if (servantData != null && !servantData.isReleased()) {
            servantData.summon();
         }
      }

      this.updateButtonState();
      if (this.field_22787.field_1724 != null) {
         this.field_22787.field_1724.field_3944.method_45731("xie servant summon_all");
         this.field_22787.execute(() -> {
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
      RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.field_2797).getRoyalCurseData();
      int servantCount = royalCurseData.getServantCount();

      for (int i = 0; i < servantCount; i++) {
         RoyalCurseServantData servantData = royalCurseData.getServant(i);
         if (servantData != null && servantData.isReleased()) {
            servantData.recall();
         }
      }

      this.updateButtonState();
      if (this.field_22787.field_1724 != null) {
         this.field_22787.field_1724.field_3944.method_45731("xie servant recall_all");
         this.field_22787.execute(() -> {
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
      RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.field_2797).getRoyalCurseData();
      if (this.selectedServantIndex < royalCurseData.getServantCount() - 1) {
         this.selectedServantIndex++;
      }
   }

   private void onCloseButtonClicked() {
      this.field_22787
         .method_1507(
            new GhostControlScreen(
               new GhostControlScreenHandler(0, this.field_22787.field_1724.method_31548()),
               this.field_22787.field_1724.method_31548(),
               Text.method_43471("screen.smfs.ghost_control")
            )
         );
   }

   private void drawServantInfo(DrawContext context) {
      RoyalCurseData royalCurseData = ((RoyalCurseScreenHandler)this.field_2797).getRoyalCurseData();
      int servantCount = royalCurseData.getServantCount();
      int centerX = this.field_22789 / 2;
      int centerY = this.field_22790 / 2;
      if (servantCount > 0 && this.selectedServantIndex < servantCount) {
         RoyalCurseServantData servantData = royalCurseData.getServant(this.selectedServantIndex);
         if (servantData != null) {
            context.method_27535(this.field_22793, Text.method_43470("奴仆数量: " + servantCount + "/6"), centerX - 150, 10, 16776960);
            context.method_27535(
               this.field_22793, Text.method_43470("当前: " + (this.selectedServantIndex + 1) + "/" + servantCount), centerX - 150, 25, 16776960
            );
            context.method_27535(this.field_22793, Text.method_43470("名称: " + servantData.getPlayerName()), centerX - 150, 40, 16776960);
            context.method_27535(
               this.field_22793,
               Text.method_43470("强度: " + servantData.getSpiritualStrength() + "/" + servantData.getMaxSpiritualStrength()),
               centerX - 150,
               55,
               16776960
            );
            context.method_27535(this.field_22793, Text.method_43470("抗性: " + servantData.getSpiritualResistance()), centerX - 150, 70, 16776960);
            context.method_27535(this.field_22793, Text.method_43470("伤害: " + servantData.getSpiritualDamage()), centerX - 150, 85, 16776960);
            context.method_27535(this.field_22793, Text.method_43470("复苏: " + servantData.getRecoveryFactor()), centerX - 150, 100, 16776960);
            context.method_27535(this.field_22793, Text.method_43470("状态: " + (servantData.isReleased() ? "已释放" : "未释放")), centerX - 150, 115, 16711680);
            context.method_27535(this.field_22793, Text.method_43470("鬼域: " + servantData.getGhostDomainLevel()), centerX + 110, 40, 16776960);
            context.method_27535(this.field_22793, Text.method_43470("半径: " + servantData.getGhostDomainRadius()), centerX + 110, 55, 16776960);
            List<String> playerGhosts = servantData.getPlayerGhosts();
            if (!playerGhosts.isEmpty()) {
               int lineHeight = 10;
               int currentY = 70;
               context.method_27535(this.field_22793, Text.method_43470("灵异: "), centerX + 110, currentY, 16776960);
               currentY += lineHeight + 5;
               int maxWidth = 100;
               StringBuilder currentLine = new StringBuilder();

               for (int i = 0; i < playerGhosts.size(); i++) {
                  String ghostType = playerGhosts.get(i);
                  String displayName = GhostUtils.getGhostDisplayName(ghostType);
                  int textWidth = this.field_22793.method_1727(currentLine + (currentLine.length() > 0 ? "|" : "") + displayName);
                  if (textWidth > maxWidth) {
                     context.method_27535(this.field_22793, Text.method_43470(currentLine.toString()), centerX + 110, currentY, 16776960);
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
                  context.method_27535(this.field_22793, Text.method_43470(currentLine.toString()), centerX + 110, currentY, 16776960);
               }
            }
         }
      }
   }

   protected void method_2388(DrawContext context, int mouseX, int mouseY) {
   }
}
