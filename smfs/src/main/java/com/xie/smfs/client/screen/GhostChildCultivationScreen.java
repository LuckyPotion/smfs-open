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
      this.field_2792 = 176;
      this.field_2779 = 166;
      this.field_25270 = this.field_2779 - 94;
   }

   protected void method_25426() {
      super.method_25426();
      int centerX = this.field_22789 / 2;
      int centerY = this.field_22790 / 2;
      int totalWidth = 300;
      int startX = centerX - totalWidth / 2;
      this.feedButton = ButtonWidget.method_46430(Text.method_43470("喂养"), button -> {
         if (this.field_22787 != null && this.field_22787.field_1724 != null) {
            this.field_22787.field_1724.field_3944.method_45730("xie gui feed");
         }
      }).method_46434(startX, centerY + 100, 60, 20).method_46431();
      this.breakthroughButton = ButtonWidget.method_46430(Text.method_43470("突破"), button -> this.onBreakthroughButtonClicked())
         .method_46434(startX + 80, centerY + 100, 60, 20)
         .method_46431();
      ButtonWidget skillButton = ButtonWidget.method_46430(Text.method_43470("技能树"), button -> {
         if (this.field_22787 != null && this.field_22787.field_1724 != null) {
            this.field_22787.field_1724.method_7353(Text.method_43470("§a敬请期待"), true);
            this.field_22787.method_1507(null);
         }
      }).method_46434(startX + 160, centerY + 100, 60, 20).method_46431();
      ButtonWidget fusionButton = ButtonWidget.method_46430(Text.method_43470("融合"), button -> this.onFusionButtonClicked())
         .method_46434(startX + 240, centerY + 100, 60, 20)
         .method_46431();
      this.closeButton = ButtonWidget.method_46430(Text.method_43470("关闭"), button -> this.onCloseButtonClicked())
         .method_46434(this.field_22789 - 70, 10, 60, 20)
         .method_46431();
      this.summonButton = ButtonWidget.method_46430(Text.method_43470("召唤"), button -> this.onSummonButtonClicked())
         .method_46434(this.field_22789 - 70, 40, 60, 20)
         .method_46431();
      this.recallButton = ButtonWidget.method_46430(Text.method_43470("收回"), button -> this.onRecallButtonClicked())
         .method_46434(this.field_22789 - 70, 70, 60, 20)
         .method_46431();
      this.method_37063(this.feedButton);
      this.method_37063(this.breakthroughButton);
      this.method_37063(skillButton);
      this.method_37063(fusionButton);
      this.method_37063(this.closeButton);
      this.method_37063(this.summonButton);
      this.method_37063(this.recallButton);
      this.updateButtonState();
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      super.method_25394(context, mouseX, mouseY, delta);
      this.renderGhostChildModel(context, delta);
      this.drawGhostChildInfo(context);
      this.method_2380(context, mouseX, mouseY);
   }

   private void renderGhostChildModel(DrawContext context, float delta) {
      int centerX = this.field_22789 / 2;
      int centerY = this.field_22790 / 2;
      MinecraftClient client = MinecraftClient.method_1551();
      World world = client.field_1687;
      if (world != null) {
         try {
            GhostChildEntity ghostChild = new GhostChildEntity(ModEntities.GHOST_CHILD, world);
            MatrixStack matrices = context.method_51448();
            matrices.method_22903();
            matrices.method_46416(centerX, centerY + 90, 100.0F);
            float scale = 120.0F;
            matrices.method_22905(scale, scale, scale);
            matrices.method_22907(new Quaternionf().rotateX((float)Math.toRadians(180.0)));
            matrices.method_22907(new Quaternionf().rotateY((float)Math.toRadians(180.0)));
            matrices.method_22907(new Quaternionf().rotateY((float)Math.toRadians(this.rotation)));
            Immediate provider = client.method_22940().method_23000();
            EntityRenderer<GhostChildEntity> renderer = client.method_1561().method_3953(ghostChild);
            renderer.method_3936(ghostChild, 0.0F, delta, matrices, provider, 15728880);
            matrices.method_22909();
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
      GhostChildData ghostChildData = ((GhostChildCultivationScreenHandler)this.field_2797).getGhostChildData();
      if (ghostChildData != null) {
         if (this.breakthroughButton != null) {
            this.breakthroughButton.field_22763 = ghostChildData.isAtBreakthroughLevel();
         }

         if (this.summonButton != null) {
            this.summonButton.field_22763 = !ghostChildData.isSummoned();
         }

         if (this.recallButton != null) {
            this.recallButton.field_22763 = ghostChildData.isSummoned();
         }

         if (this.feedButton != null) {
            this.feedButton.field_22763 = true;
         }
      }
   }

   private void onBreakthroughButtonClicked() {
      ((GhostChildCultivationScreenHandler)this.field_2797).performBreakthrough();
   }

   private void onSummonButtonClicked() {
      GhostChildData ghostChildData = ((GhostChildCultivationScreenHandler)this.field_2797).getGhostChildData();
      if (ghostChildData != null) {
         ghostChildData.summon();
         this.updateButtonState();
      }

      ((GhostChildCultivationScreenHandler)this.field_2797).summonGhostChild();
      if (this.field_22787 != null) {
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
      GhostChildData ghostChildData = ((GhostChildCultivationScreenHandler)this.field_2797).getGhostChildData();
      if (ghostChildData != null) {
         ghostChildData.recall();
         this.updateButtonState();
      }

      ((GhostChildCultivationScreenHandler)this.field_2797).recallGhostChild();
      if (this.field_22787 != null) {
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

   private void onFusionButtonClicked() {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         this.field_22787.method_1507(null);
         ClientPlayerEntity player = this.field_22787.field_1724;
         GhostChildData data = ((GhostChildCultivationScreenHandler)this.field_2797).getGhostChildData();
         if (!GoldBlockProtectionManager.isPlayerInGoldBlockShelter(player)) {
            player.method_7353(Text.method_43470("§c融合失败：需要在金块包裹状态下才能进行融合！"), false);
         } else {
            int tamedCount = PlayerEvents.countOccupiedGhostSlots(player);
            if (tamedCount < 6) {
               player.method_7353(Text.method_43470("§c融合失败：需要驾驭6只鬼才能进行融合！当前驾驭：" + tamedCount + "只"), false);
            } else if (data == null || data.getLevel() < 10) {
               int level = data != null ? data.getLevel() : 0;
               player.method_7353(Text.method_43470("§c融合失败：鬼童需要10级才能进行融合！当前等级：" + level), false);
            } else if (data.isSummoned()) {
               player.method_7353(Text.method_43470("§c融合失败：鬼童需要处于收回状态才能进行融合！"), false);
            } else {
               GhostChildFusionC2SPacket.sendToServer();
            }
         }
      }
   }

   private void drawGhostChildInfo(DrawContext context) {
      GhostChildData ghostChildData = ((GhostChildCultivationScreenHandler)this.field_2797).getGhostChildData();
      if (ghostChildData != null) {
         int centerX = this.field_22789 / 2;
         int centerY = this.field_22790 / 2;
         if (ghostChildData.isAtBreakthroughLevel()) {
            context.method_27535(this.field_22793, Text.method_43470("等级: " + ghostChildData.getLevel() + "（可突破）"), centerX - 200, 20, 16711680);
         } else {
            context.method_27535(this.field_22793, Text.method_43470("等级: " + ghostChildData.getLevel()), centerX - 200, 20, 16776960);
         }

         int requiredExp = this.getRequiredExperience(ghostChildData.getLevel());
         context.method_27535(this.field_22793, Text.method_43470("经验: " + ghostChildData.getExperience() + "/" + requiredExp), centerX - 200, 40, 16776960);
         context.method_27535(
            this.field_22793,
            Text.method_43470("灵异强度: " + ghostChildData.getSpiritPower() + "/" + ghostChildData.getMaxSpiritPower()),
            centerX - 200,
            60,
            16776960
         );
         context.method_27535(this.field_22793, Text.method_43470("灵异抗性: " + ghostChildData.getSpiritResistance()), centerX - 200, 80, 16776960);
         context.method_27535(this.field_22793, Text.method_43470("灵异力量: " + ghostChildData.getSpiritDamage()), centerX - 200, 100, 16776960);
         context.method_27535(this.field_22793, Text.method_43470("复苏因子: " + ghostChildData.getRevivalFactor()), centerX - 200, 120, 16776960);
         String releaseStatus = ghostChildData.isSummoned() ? "已释放" : "未释放";
         int statusColor = ghostChildData.isSummoned() ? 65280 : 16711680;
         context.method_27535(this.field_22793, Text.method_43470("状态: " + releaseStatus), centerX - 200, 140, statusColor);
      }
   }

   private int getRequiredExperience(int level) {
      return 100 * level * level;
   }

   protected void method_2388(DrawContext context, int mouseX, int mouseY) {
   }
}
