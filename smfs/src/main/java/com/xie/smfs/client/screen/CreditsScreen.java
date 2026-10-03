package com.xie.smfs.client.screen;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class CreditsScreen extends Screen {
   private static final Identifier BACKGROUND_TEXTURE = new Identifier("smfs", "textures/gui/work_background.png");
   private static final int WINDOW_WIDTH = 320;
   private static final int WINDOW_HEIGHT = 250;
   private static final int PADDING = 10;
   private static final int TITLE_Y = 20;
   private static final int CONTENT_START_Y = 50;
   private static final int LINE_HEIGHT = 12;
   private static final int BUTTON_WIDTH = 100;
   private static final int BUTTON_HEIGHT = 20;
   private static final int SCROLLBAR_WIDTH = 6;
   private static final int SCROLLBAR_PADDING = 2;
   private static final int CONTENT_HEIGHT = 150;
   private int windowX;
   private int windowY;
   private int scrollOffset = 0;
   private int maxScrollOffset = 0;
   private boolean isDraggingScrollbar = false;
   private int scrollbarDragStartY = 0;
   private int scrollbarDragStartOffset = 0;
   private final List<CreditsScreen.TeamMember> teamMembers = new ArrayList<>();

   public CreditsScreen() {
      super(Text.method_43471("screen.smfs.credits.title"));
      this.initializeTeamMembers();
   }

   protected void method_25426() {
      super.method_25426();
      this.windowX = (this.field_22789 - 320) / 2;
      this.windowY = (this.field_22790 - 250) / 2;
      this.calculateMaxScrollOffset();
      int closeButtonX = this.windowX + 110;
      int closeButtonY = this.windowY + 250 - 20 - 10;
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43471("screen.smfs.credits.close"), button -> this.method_25419())
            .method_46434(closeButtonX, closeButtonY, 100, 20)
            .method_46431()
      );
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      context.method_25294(this.windowX, this.windowY, this.windowX + 320, this.windowY + 250, -1442840576);
      context.method_49601(this.windowX, this.windowY, 320, 250, -1);
      TextRenderer textRenderer = this.field_22793;
      Text title = Text.method_43471("screen.smfs.credits.title");
      TextRenderer titleRenderer = this.field_22787.field_1772;
      int titleX = this.windowX + (320 - titleRenderer.method_27525(title)) / 2;
      context.method_51448().method_22903();
      context.method_51448().method_22905(2.0F, 2.0F, 2.0F);
      int scaledTitleWidth = titleRenderer.method_27525(title) * 2;
      int scaledTitleX = (int)((this.windowX + (320 - scaledTitleWidth) / 2) / 2.0F);
      int scaledTitleY = (int)((this.windowY + 20 - 10) / 2.0F);
      context.method_51439(titleRenderer, title, scaledTitleX, scaledTitleY, 16777215, true);
      context.method_51448().method_22909();
      Text subtitle = Text.method_43470("本模组完全公益且免费，警惕任何收费下载！");
      int subtitleX = this.windowX + (320 - textRenderer.method_27525(subtitle)) / 2;
      int subtitleY = this.windowY + 20 + 12 + 2;
      context.method_51439(textRenderer, subtitle, subtitleX, subtitleY, 6710886, true);
      int topDividerY = this.windowY + 50 - 5;
      context.method_25292(this.windowX + 10, this.windowX + 320 - 10, topDividerY, -1);
      int contentX = this.windowX + 10;
      int contentY = this.windowY + 50;
      int contentWidth = 292;
      int contentHeight = 150;
      context.method_44379(contentX, contentY, contentX + contentWidth, contentY + contentHeight);
      int currentY = contentY - this.scrollOffset;

      for (CreditsScreen.TeamMember member : this.teamMembers) {
         if (currentY + 12 >= contentY && currentY <= contentY + contentHeight) {
            if (member.name.equals("---分隔线---") && !member.role.isEmpty()) {
               int dividerY = currentY + 6;
               context.method_25292(contentX, contentX + contentWidth, dividerY, 6710886);
               Text dividerText = Text.method_43470(member.role);
               int textX = contentX + (contentWidth - textRenderer.method_27525(dividerText)) / 2;
               context.method_51439(textRenderer, dividerText, textX, currentY, 6710886, false);
            } else if (member.name.equals("---项目链接---") && !member.role.isEmpty() && member.role.startsWith("http")) {
               Text linkText = Text.method_43470(member.role);
               int textX = contentX + (contentWidth - textRenderer.method_27525(linkText)) / 2;
               boolean isHovered = mouseX >= textX
                  && mouseX <= textX + textRenderer.method_27525(linkText)
                  && mouseY >= currentY
                  && mouseY <= currentY + 12
                  && mouseY >= contentY
                  && mouseY <= contentY + contentHeight;
               int linkColor = isHovered ? 65280 : 43775;
               context.method_51439(textRenderer, linkText, textX, currentY, linkColor, false);
               if (isHovered) {
                  context.method_25292(textX, textX + textRenderer.method_27525(linkText), currentY + 12 - 1, linkColor);
               }
            } else if (member.name.isEmpty() && !member.role.isEmpty() && member.role.startsWith("http")) {
               Text linkText = Text.method_43470(member.role);
               int textX = contentX + (contentWidth - textRenderer.method_27525(linkText)) / 2;
               boolean isHovered = mouseX >= textX
                  && mouseX <= textX + textRenderer.method_27525(linkText)
                  && mouseY >= currentY
                  && mouseY <= currentY + 12
                  && mouseY >= contentY
                  && mouseY <= contentY + contentHeight;
               int linkColor = isHovered ? 65280 : 43775;
               context.method_51439(textRenderer, linkText, textX, currentY, linkColor, false);
               if (isHovered) {
                  context.method_25292(textX, textX + textRenderer.method_27525(linkText), currentY + 12 - 1, linkColor);
               }
            } else {
               Text nameText = Text.method_43470(member.name);
               context.method_51439(textRenderer, nameText, contentX, currentY, 16776960, false);
               Text roleText = Text.method_43470(" - " + member.role);
               context.method_51439(textRenderer, roleText, contentX + textRenderer.method_27525(nameText), currentY, 14540253, false);
            }
         }

         currentY += 12;
         if (!member.name.equals("---分隔线---")
            && !member.name.equals("---项目链接---")
            && (!member.name.isEmpty() || member.role.isEmpty() || !member.role.startsWith("http"))
            && member.homepage != null
            && !member.homepage.isEmpty()) {
            if (currentY + 12 >= contentY && currentY <= contentY + contentHeight) {
               Text linkText = Text.method_43470("主页: " + member.homepage);
               int linkX = contentX;
               boolean isHovered = mouseX >= linkX
                  && mouseX <= linkX + textRenderer.method_27525(linkText)
                  && mouseY >= currentY
                  && mouseY <= currentY + 12
                  && mouseY >= contentY
                  && mouseY <= contentY + contentHeight;
               int linkColor = isHovered ? 65280 : 43775;
               context.method_51439(textRenderer, linkText, linkX, currentY, linkColor, false);
               if (isHovered) {
                  context.method_25292(linkX, linkX + textRenderer.method_27525(linkText), currentY + 12 - 1, linkColor);
               }
            }

            currentY += 12;
         }

         currentY += 5;
      }

      context.method_44380();
      int bottomDividerY = this.windowY + 50 + 150 + 5;
      context.method_25292(this.windowX + 10, this.windowX + 320 - 10, bottomDividerY, -1);
      if (this.maxScrollOffset > 0) {
         this.renderScrollbar(context, mouseX, mouseY);
      }

      super.method_25394(context, mouseX, mouseY, delta);
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      if (button == 0) {
         if (this.maxScrollOffset > 0) {
            int scrollbarX = this.windowX + 320 - 6 - 2;
            int scrollbarY = this.windowY + 50;
            int scrollbarHeight = 150;
            if (mouseX >= scrollbarX && mouseX <= scrollbarX + 6 && mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight) {
               this.isDraggingScrollbar = true;
               this.scrollbarDragStartY = (int)mouseY;
               this.scrollbarDragStartOffset = this.scrollOffset;
               return true;
            }
         }

         int contentX = this.windowX + 10;
         int contentY = this.windowY + 50;
         int contentWidth = 292;
         int contentHeight = 150;
         int currentY = contentY - this.scrollOffset;

         for (CreditsScreen.TeamMember member : this.teamMembers) {
            int memberStartY = currentY;
            if (member.name.equals("---项目链接---") && !member.role.isEmpty() && member.role.startsWith("http")) {
               Text linkText = Text.method_43470(member.role);
               int textX = contentX + (contentWidth - this.field_22793.method_27525(linkText)) / 2;
               if (mouseX >= textX
                  && mouseX <= textX + this.field_22793.method_27525(linkText)
                  && mouseY >= memberStartY
                  && mouseY <= memberStartY + 12
                  && mouseY >= contentY
                  && mouseY <= contentY + contentHeight) {
                  Util.method_668().method_670(member.role);
                  return true;
               }

               currentY += 12;
            } else if (member.name.isEmpty() && !member.role.isEmpty() && member.role.startsWith("http")) {
               Text linkText = Text.method_43470(member.role);
               int textX = contentX + (contentWidth - this.field_22793.method_27525(linkText)) / 2;
               if (mouseX >= textX
                  && mouseX <= textX + this.field_22793.method_27525(linkText)
                  && mouseY >= memberStartY
                  && mouseY <= memberStartY + 12
                  && mouseY >= contentY
                  && mouseY <= contentY + contentHeight) {
                  Util.method_668().method_670(member.role);
                  return true;
               }

               currentY += 12;
            } else if (member.name.equals("---分隔线---")) {
               currentY += 12;
            } else {
               currentY += 12;
               if (member.homepage != null && !member.homepage.isEmpty()) {
                  Text linkText = Text.method_43470("主页: " + member.homepage);
                  int linkX = contentX;
                  if (mouseX >= linkX
                     && mouseX <= linkX + this.field_22793.method_27525(linkText)
                     && mouseY >= currentY
                     && mouseY <= currentY + 12
                     && mouseY >= contentY
                     && mouseY <= contentY + contentHeight) {
                     Util.method_668().method_670(member.homepage);
                     return true;
                  }

                  currentY += 12;
               }
            }

            currentY += 5;
         }
      }

      return super.method_25402(mouseX, mouseY, button);
   }

   public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.isDraggingScrollbar && button == 0) {
         int scrollbarY = this.windowY + 50;
         int scrollbarHeight = 150;
         int dragDeltaY = (int)mouseY - this.scrollbarDragStartY;
         int scrollDelta = (int)((double)dragDeltaY * this.maxScrollOffset / scrollbarHeight);
         this.scrollOffset = Math.max(0, Math.min(this.maxScrollOffset, this.scrollbarDragStartOffset + scrollDelta));
         return true;
      } else {
         return super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   public boolean method_25406(double mouseX, double mouseY, int button) {
      if (button == 0) {
         this.isDraggingScrollbar = false;
      }

      return super.method_25406(mouseX, mouseY, button);
   }

   public boolean method_25401(double mouseX, double mouseY, double amount) {
      if (this.maxScrollOffset > 0) {
         this.scrollOffset = Math.max(0, Math.min(this.maxScrollOffset, this.scrollOffset - (int)(amount * 20.0)));
         return true;
      } else {
         return super.method_25401(mouseX, mouseY, amount);
      }
   }

   public boolean method_25421() {
      return false;
   }

   public boolean method_25404(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.method_25419();
         return true;
      } else {
         return super.method_25404(keyCode, scanCode, modifiers);
      }
   }

   public void method_25419() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(null);
      }
   }

   private void initializeTeamMembers() {
      this.teamMembers.add(new CreditsScreen.TeamMember("---分隔线---", "项目开源链接", ""));
      this.teamMembers
         .add(
            new CreditsScreen.TeamMember("---项目链接---", "https://gitee.com/xiaoxieY/mysterious-revival.git", "https://gitee.com/xiaoxieY/mysterious-revival.git")
         );
      this.teamMembers.add(new CreditsScreen.TeamMember("---分隔线---", "创作者列表", ""));
      this.teamMembers.add(new CreditsScreen.TeamMember("进击的蟹某人", "策划/编程/美术", "https://space.bilibili.com/488712064?spm_id_from=333.337.0.0"));
      this.teamMembers.add(new CreditsScreen.TeamMember("叶无道_M", "建模", "https://space.bilibili.com/11897609?spm_id_from=333.337.0.0"));
      this.teamMembers.add(new CreditsScreen.TeamMember("安菲尔德情话", "建筑", "https://space.bilibili.com/316974916"));
      this.teamMembers.add(new CreditsScreen.TeamMember("---分隔线---", "其他贡献者", ""));
      this.teamMembers.add(new CreditsScreen.TeamMember("tobyyyyy", "代码帮助", "无"));
      this.teamMembers.add(new CreditsScreen.TeamMember("MC凉城", "代码帮助", "无"));
      this.teamMembers.add(new CreditsScreen.TeamMember(".", "模型帮助", "无"));
      this.teamMembers.add(new CreditsScreen.TeamMember("11", "美术帮助", "无"));
      this.teamMembers.add(new CreditsScreen.TeamMember("浅埋", "美术帮助", "无"));
      this.teamMembers.add(new CreditsScreen.TeamMember("玩家们", "测试BUG、提出改进建议与支持", "无"));
   }

   private void calculateMaxScrollOffset() {
      int totalContentHeight = 0;

      for (CreditsScreen.TeamMember member : this.teamMembers) {
         totalContentHeight += 12;
         if (!member.name.equals("---分隔线---") && !member.name.equals("---项目链接---") && member.homepage != null && !member.homepage.isEmpty()) {
            totalContentHeight += 12;
         }

         totalContentHeight += 5;
      }

      if (!this.teamMembers.isEmpty()) {
         totalContentHeight -= 5;
      }

      this.maxScrollOffset = Math.max(0, totalContentHeight - 150);
   }

   private void renderScrollbar(DrawContext context, int mouseX, int mouseY) {
      int scrollbarX = this.windowX + 320 - 6 - 2;
      int scrollbarY = this.windowY + 50;
      int scrollbarHeight = 150;
      context.method_25294(scrollbarX, scrollbarY, scrollbarX + 6, scrollbarY + scrollbarHeight, 1728053247);
      double scrollRatio = (double)this.scrollOffset / this.maxScrollOffset;
      int sliderHeight = Math.max(20, (int)(scrollbarHeight * (150.0 / (150 + this.maxScrollOffset))));
      int sliderY = scrollbarY + (int)(scrollRatio * (scrollbarHeight - sliderHeight));
      boolean isHovered = mouseX >= scrollbarX && mouseX <= scrollbarX + 6 && mouseY >= sliderY && mouseY <= sliderY + sliderHeight;
      int sliderColor = !isHovered && !this.isDraggingScrollbar ? -5592406 : -1;
      context.method_25294(scrollbarX, sliderY, scrollbarX + 6, sliderY + sliderHeight, sliderColor);
   }

   public static void show() {
      try {
         MinecraftClient client = MinecraftClient.method_1551();
         if (client != null) {
            client.execute(() -> {
               try {
                  client.method_1507(new CreditsScreen());
               } catch (Exception ex) {
                  ex.printStackTrace();
               }
            });
         } else {
            System.err.println("[SMFS] CreditsScreen: MinecraftClient is null");
         }
      } catch (Exception e) {
         System.err.println("[SMFS] CreditsScreen: Error in show method: " + e.getMessage());
         e.printStackTrace();
      }
   }

   private static class TeamMember {
      public final String name;
      public final String role;
      public final String homepage;

      public TeamMember(String name, String role, String homepage) {
         this.name = name;
         this.role = role;
         this.homepage = homepage;
      }
   }
}
