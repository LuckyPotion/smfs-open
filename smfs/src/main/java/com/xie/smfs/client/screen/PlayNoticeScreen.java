package com.xie.smfs.client.screen;

import com.xie.smfs.client.data.ClientDataManager;
import com.xie.smfs.registry.ModSounds;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class PlayNoticeScreen extends Screen {
   private static final int TEXT_WIDTH = 400;
   private static final int PADDING = 40;
   private static final int DEFAULT_WAIT_TICKS = 60;
   private int currentPage = 0;
   private ButtonWidget confirmButton;
   private ButtonWidget nextButton;
   private ButtonWidget supportButton;
   private int waitTimer = 0;
   private boolean canProceed = false;
   private static final String[] PAGES = new String[]{
      "五浊恶世、地狱已空、厉鬼复苏、人间如狱。",
      "欢迎游玩《神秘复苏》，这是一个以起点同名小说《神秘复苏》为世界观的生存模组。",
      "模组复刻了大量小说中的内容与设定，同时也参杂有许多原创内容。\\n请勿完全代入小说，如有冲突以小说为准。",
      "作者：进击的蟹某人",
      "按键操作：\\n\\n灵异界面（U）、配置界面（~）、切换厉鬼（Ctrl）、鬼域（K）、袭击（J)、技能(V)(G)(N)。",
      "建议先完成新手引导任务，以了解我的世界玩法/模组机制。\\n\\n祝你生存愉快！"
   };
   private static final int[] PAGE_WAIT_TICKS = new int[]{130, 130, 195, 70, 100, 140};

   public PlayNoticeScreen() {
      super(Text.method_43470("游玩须知"));
      this.playPageSound(1);
   }

   private void playPageSound(int pageNumber) {
      MinecraftClient client = MinecraftClient.method_1551();
      if (client.field_1724 != null && pageNumber >= 1 && pageNumber <= 6) {
         SoundEvent[] pageSounds = new SoundEvent[]{
            ModSounds.NOTICE_PAGE_1,
            ModSounds.NOTICE_PAGE_2,
            ModSounds.NOTICE_PAGE_3,
            ModSounds.NOTICE_PAGE_4,
            ModSounds.NOTICE_PAGE_5,
            ModSounds.NOTICE_PAGE_6
         };
         client.field_1724.method_17356(pageSounds[pageNumber - 1], SoundCategory.field_15246, 1.0F, 1.0F);
      }
   }

   protected void method_25426() {
      super.method_25426();
      int buttonWidth = 100;
      int buttonHeight = 20;
      int buttonY = this.field_22790 - 40 - buttonHeight;
      this.nextButton = ButtonWidget.method_46430(Text.method_43470("点击继续"), button -> this.nextPage())
         .method_46434(this.field_22789 / 2 - buttonWidth / 2, buttonY, buttonWidth, buttonHeight)
         .method_46431();
      this.confirmButton = ButtonWidget.method_46430(Text.method_43470("我已知晓"), button -> this.handleConfirm())
         .method_46434(this.field_22789 / 2 - buttonWidth - 10, buttonY, buttonWidth, buttonHeight)
         .method_46431();
      this.supportButton = ButtonWidget.method_46430(Text.method_43470("创作者"), button -> this.openCreatorPage())
         .method_46434(this.field_22789 / 2 + 10, buttonY, buttonWidth, buttonHeight)
         .method_46431();
      this.resetTimer();
      this.updateButtons();
   }

   private void resetTimer() {
      this.waitTimer = 0;
      this.canProceed = false;
   }

   private int getCurrentPageWaitTicks() {
      if (this.currentPage >= 0 && this.currentPage < PAGE_WAIT_TICKS.length) {
         int ticks = PAGE_WAIT_TICKS[this.currentPage];
         return ticks > 0 ? ticks : 60;
      } else {
         return 60;
      }
   }

   public void method_25393() {
      super.method_25393();
      if (!this.canProceed) {
         this.waitTimer++;
         if (this.waitTimer >= this.getCurrentPageWaitTicks()) {
            if (this.currentPage < PAGES.length - 1) {
               this.nextPage();
            } else {
               this.canProceed = true;
               this.updateButtons();
            }
         }
      }
   }

   private void updateButtons() {
      this.method_37067();
      if (this.canProceed) {
         this.method_37063(this.confirmButton);
         this.method_37063(this.supportButton);
      }
   }

   private void openCreatorPage() {
      MinecraftClient.method_1551().method_1507(new CreditsScreen());
   }

   private void nextPage() {
      if (this.currentPage < PAGES.length - 1) {
         this.currentPage++;
         this.playPageSound(this.currentPage + 1);
         this.resetTimer();
         this.updateButtons();
      }
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      context.method_25294(0, 0, this.field_22789, this.field_22790, -16777216);
      super.method_25394(context, mouseX, mouseY, delta);
      String content = PAGES[this.currentPage].replace("\\n", "\n");
      Text noticeText = Text.method_43470(content);
      List<OrderedText> wrappedText = this.field_22793.method_1728(noticeText, 400);
      int textY = this.field_22790 / 2 - wrappedText.size() * (9 + 12) / 2;

      for (OrderedText line : wrappedText) {
         int lineWidth = this.field_22793.method_30880(line);
         int textX = this.field_22789 / 2 - lineWidth / 2;
         context.method_35720(this.field_22793, line, textX, textY, -65536);
         textY += 9 + 12;
      }
   }

   public boolean method_25404(int keyCode, int scanCode, int modifiers) {
      if (!this.canProceed) {
         return super.method_25404(keyCode, scanCode, modifiers);
      }

      if (keyCode != 32 && keyCode != 257) {
         return super.method_25404(keyCode, scanCode, modifiers);
      }

      if (this.currentPage < PAGES.length - 1) {
         this.nextPage();
      } else {
         this.handleConfirm();
      }

      return true;
   }

   private void handleConfirm() {
      ClientDataManager.setHasSeenPlayNotice(true);
      this.method_25419();
   }

   public boolean method_25422() {
      return false;
   }

   public void method_25419() {
      this.field_22787.method_1507(null);
   }

   public static boolean shouldShow() {
      return !ClientDataManager.hasSeenPlayNotice();
   }

   public static void show() {
      MinecraftClient.method_1551().execute(() -> MinecraftClient.method_1551().method_1507(new PlayNoticeScreen()));
   }

   public static void markAsShown() {
      ClientDataManager.setHasSeenPlayNotice(true);
   }
}
