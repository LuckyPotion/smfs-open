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
      super(Text.literal("游玩须知"));
      this.playPageSound(1);
   }

   private void playPageSound(int pageNumber) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && pageNumber >= 1 && pageNumber <= 6) {
         SoundEvent[] pageSounds = new SoundEvent[]{
            ModSounds.NOTICE_PAGE_1,
            ModSounds.NOTICE_PAGE_2,
            ModSounds.NOTICE_PAGE_3,
            ModSounds.NOTICE_PAGE_4,
            ModSounds.NOTICE_PAGE_5,
            ModSounds.NOTICE_PAGE_6
         };
         client.player.playSound(pageSounds[pageNumber - 1], SoundCategory.VOICE, 1.0F, 1.0F);
      }
   }

   protected void init() {
      super.init();
      int buttonWidth = 100;
      int buttonHeight = 20;
      int buttonY = this.height - 40 - buttonHeight;
      this.nextButton = ButtonWidget.builder(Text.literal("点击继续"), button -> this.nextPage())
         .dimensions(this.width / 2 - buttonWidth / 2, buttonY, buttonWidth, buttonHeight)
         .build();
      this.confirmButton = ButtonWidget.builder(Text.literal("我已知晓"), button -> this.handleConfirm())
         .dimensions(this.width / 2 - buttonWidth - 10, buttonY, buttonWidth, buttonHeight)
         .build();
      this.supportButton = ButtonWidget.builder(Text.literal("创作者"), button -> this.openCreatorPage())
         .dimensions(this.width / 2 + 10, buttonY, buttonWidth, buttonHeight)
         .build();
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

   public void tick() {
      super.tick();
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
      this.clearChildren();
      if (this.canProceed) {
         this.addDrawableChild(this.confirmButton);
         this.addDrawableChild(this.supportButton);
      }
   }

   private void openCreatorPage() {
      MinecraftClient.getInstance().setScreen(new CreditsScreen());
   }

   private void nextPage() {
      if (this.currentPage < PAGES.length - 1) {
         this.currentPage++;
         this.playPageSound(this.currentPage + 1);
         this.resetTimer();
         this.updateButtons();
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      context.fill(0, 0, this.width, this.height, -16777216);
      super.render(context, mouseX, mouseY, delta);
      String content = PAGES[this.currentPage].replace("\\n", "\n");
      Text noticeText = Text.literal(content);
      List<OrderedText> wrappedText = this.textRenderer.wrapLines(noticeText, 400);
      int textY = this.height / 2 - wrappedText.size() * (9 + 12) / 2;

      for (OrderedText line : wrappedText) {
         int lineWidth = this.textRenderer.getWidth(line);
         int textX = this.width / 2 - lineWidth / 2;
         context.drawTextWithShadow(this.textRenderer, line, textX, textY, -65536);
         textY += 9 + 12;
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.canProceed) {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }

      if (keyCode != 32 && keyCode != 257) {
         return super.keyPressed(keyCode, scanCode, modifiers);
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
      this.close();
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public void close() {
      this.client.setScreen(null);
   }

   public static boolean shouldShow() {
      return !ClientDataManager.hasSeenPlayNotice();
   }

   public static void show() {
      MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().setScreen(new PlayNoticeScreen()));
   }

   public static void markAsShown() {
      ClientDataManager.setHasSeenPlayNotice(true);
   }
}
