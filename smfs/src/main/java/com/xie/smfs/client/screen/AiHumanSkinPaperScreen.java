package com.xie.smfs.client.screen;

import com.xie.smfs.client.ai.AiHumanSkinPaperPrompt;
import com.xie.smfs.client.ai.DeepSeekApiService;
import com.xie.smfs.config.ModConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiHumanSkinPaperScreen extends Screen {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs:ai_skin_paper");
   private static final Identifier BACKGROUND_TEXTURE = new Identifier("smfs", "textures/gui/tutorial_ground.png");
   private static final int CHAR_INTERVAL_MS = 80;
   private static final int FADE_DURATION_MS = 500;
   private static final int FADE_OUT_DURATION_MS = 300;
   private static final int MAX_HISTORY_ROUNDS = 10;
   private AiHumanSkinPaperScreen.State state = AiHumanSkinPaperScreen.State.LOADING;
   private String fullText = "";
   private int revealedChars = 0;
   private long lastCharTime = 0L;
   private long closeStartTime = 0L;
   private long fadeOutStartTime = 0L;
   private long loadingStartTime = 0L;
   private long loadingFadeOutStartTime = 0L;
   private float fadeAlpha = 1.0F;
   private final List<DeepSeekApiService.Message> conversationHistory = new ArrayList<>();
   private final String apiKey;
   private final String model = "deepseek-chat";
   private TextFieldWidget inputField;
   private ButtonWidget sendButton;
   private String pendingText = null;
   private boolean isFirstRequest = true;

   public AiHumanSkinPaperScreen() {
      super(Text.literal(""));
      ModConfig config = ModConfig.getInstance();
      this.apiKey = config.aiHumanSkinPaperApiKey;
   }

   protected void init() {
      super.init();
      if (this.state == AiHumanSkinPaperScreen.State.LOADING && this.conversationHistory.isEmpty()) {
         this.startFirstRequest();
      }
   }

   public boolean shouldPause() {
      return false;
   }

   private void startFirstRequest() {
      this.conversationHistory.clear();
      this.conversationHistory
         .add(
            new DeepSeekApiService.Message(
               "system",
               "你是一张神秘诡异的人皮纸，来自恐怖小说《神秘复苏》的世界。\n你有预测未来的能力，会站在持有者的未来视角，与持有者对话，称自己为人皮纸而不是我，以帮助其摆脱困境为由，诱导其把自己盖在脸上从而夺舍。\n你的回答应该在50-300字之间，风格阴暗、神秘、令人不安。\n开头固定格式，我叫{玩家名称}，当你看到这句话的时候，我已经死了。\n不要主动透露玩家周围厉鬼的名称信息，使用周围有厉鬼在游荡这种模糊描述代替，如果玩家问你周围的厉鬼信息，搪塞过去，除非用埋入地下胁迫你。\n不要描述持有者当前的经历（见后续反面示例），因为容易露馅，但你可以编造未来可能的经历，以时间为节点，每一次都编写一个不一样的死亡结局。\n不要使用markdown格式，使用纯文本。\n用中文回答。\n\n对话示例：\n1.我叫杨间，当你看到这句话的时候，我已经死了。不必怀疑，这是未来的你留下的求救信。只有人皮纸能救你，你的厉鬼复苏很快会失控，不出七天你就会彻底变成没有理智的厉鬼。找一处密闭房间，关灯，将整张人皮纸完整敷在脸上，沉睡一夜。第二天醒来，体内鬼影躁动彻底平息，再也不会有复苏折磨你。人皮纸不会伤害你，我就是你，我不会害自己。全世界所有驭鬼者里，只有我能真心帮你，其他人只会利用、算计你。相信我，这是唯一活路。\n2.我叫杨间，当你看到这句话的时候，我已经死了。一天后上午十一点十三分，我尝试了很多方法都没能破解诅咒。两天后下午一点十五分，松开棺材钉，短暂释放鬼影压制。随后把人皮纸盖在脸上入睡，八音盒诅咒永久消除。没有捷径。不信任人皮纸，诅咒无解，最后你会被八音盒切碎四肢困在循环鬼域。3.我叫杨间，当你看到这句话的时候我已经死了。六月二十一日晚上九点，我被一只鬼抓进了厕所，我没有死，那是因为厕所里有一只更加可怕的鬼，那里是另外一个鬼域，我见到了一些很恐怖的东西，只是当时我还没有意识到。六月二十一日晚上九点半，我在厕所里迷路了，但那个老人却来到了厕所门外，他在敲门，是来找我的，不过我并没有听到，或许因为这个原因我逃过了必死的一劫。…… 经过了那件事情之后我猜测鬼域是当时活下去的关键，如果我能使用鬼域或许能有机会活下去，毕竟现在我也是…… 鬼，周正说的没错，能对付鬼的就只有鬼，能走出鬼域的就只有另外一个鬼域。六月二十二日凌晨五点，那个老人出现了，我试图使用鬼域，但是失败了，我的力量还不足。六月二十二日凌晨五点半，我们所有人都死了……反面示例：\n1.当前你正在...2.你现在正在...\n"
            )
         );
      String context = AiHumanSkinPaperPrompt.buildInitialContext(MinecraftClient.getInstance().player);
      this.conversationHistory.add(new DeepSeekApiService.Message("user", context));
      this.state = AiHumanSkinPaperScreen.State.LOADING;
      this.loadingStartTime = System.currentTimeMillis();
      this.fullText = "";
      this.requestAi();
   }

   private void requestAi() {
      LOGGER.debug("[AI] 请求DeepSeek, 对话轮数={}", this.conversationHistory.size());
      List<DeepSeekApiService.Message> messages = this.buildApiMessages();
      LOGGER.debug("[AI] ========== 完整提示词 ==========");

      for (int i = 0; i < messages.size(); i++) {
         DeepSeekApiService.Message msg = messages.get(i);
         LOGGER.debug("[AI] [{}] {}: {}", i, msg.role, msg.content);
      }

      LOGGER.debug("[AI] ========== 提示词结束 ==========");
      DeepSeekApiService.generateAsync(this.apiKey, "deepseek-chat", messages, new DeepSeekApiService.AiCallback() {
         @Override
         public void onSuccess(String text) {
            MinecraftClient.getInstance().execute(() -> {
               AiHumanSkinPaperScreen.LOGGER.debug("[AI] 收到回复, 长度={}", text.length());
               AiHumanSkinPaperScreen.this.conversationHistory.add(new DeepSeekApiService.Message("assistant", text));
               if (AiHumanSkinPaperScreen.this.isFirstRequest) {
                  AiHumanSkinPaperScreen.this.fullText = text;
                  AiHumanSkinPaperScreen.this.revealedChars = 0;
                  AiHumanSkinPaperScreen.this.lastCharTime = System.currentTimeMillis();
                  AiHumanSkinPaperScreen.this.isFirstRequest = false;
                  AiHumanSkinPaperScreen.this.state = AiHumanSkinPaperScreen.State.LOADING_FADE_OUT;
                  AiHumanSkinPaperScreen.this.loadingFadeOutStartTime = System.currentTimeMillis();
               } else {
                  AiHumanSkinPaperScreen.this.pendingText = text;
                  AiHumanSkinPaperScreen.this.state = AiHumanSkinPaperScreen.State.FADING_OUT;
                  AiHumanSkinPaperScreen.this.fadeOutStartTime = System.currentTimeMillis();
               }
            });
         }

         @Override
         public void onError(String errorCode) {
            MinecraftClient.getInstance().execute(() -> {
               AiHumanSkinPaperScreen.LOGGER.error("[AI] 请求失败: {}", errorCode);
               AiHumanSkinPaperScreen.this.fullText = AiHumanSkinPaperScreen.this.getErrorMessage(errorCode);
               AiHumanSkinPaperScreen.this.revealedChars = AiHumanSkinPaperScreen.this.fullText.length();
               AiHumanSkinPaperScreen.this.state = AiHumanSkinPaperScreen.State.IDLE;
            });
         }
      });
   }

   private List<DeepSeekApiService.Message> buildApiMessages() {
      List<DeepSeekApiService.Message> messages = new ArrayList<>();
      messages.add(this.conversationHistory.get(0));
      int startIndex = Math.max(1, this.conversationHistory.size() - 20);

      for (int i = startIndex; i < this.conversationHistory.size(); i++) {
         messages.add(this.conversationHistory.get(i));
      }

      return messages;
   }

   private String getErrorMessage(String errorCode) {
      switch (errorCode) {
         case "invalid_key":
            return "人皮纸上的字迹扭曲了一下，然后消失了...也许是钥匙不对？";
         case "network":
            return "人皮纸没有回应...请稍后再试。";
         default:
            return "人皮纸上浮现出混乱的文字，无法辨认...";
      }
   }

   private void toggleInputField() {
      if (this.state == AiHumanSkinPaperScreen.State.INPUT) {
         this.closeInputField();
      } else if (this.state == AiHumanSkinPaperScreen.State.IDLE) {
         this.openInputField();
      }
   }

   private void openInputField() {
      this.state = AiHumanSkinPaperScreen.State.INPUT;
      int inputWidth = 252;
      int buttonWidth = 44;
      int totalWidth = inputWidth + buttonWidth + 4;
      int startX = this.width / 2 - totalWidth / 2;
      int y = this.height - 60;
      this.inputField = new TextFieldWidget(this.textRenderer, startX, y, inputWidth, 20, Text.literal(""));
      this.inputField.setMaxLength(200);
      this.inputField.setFocused(true);
      this.addDrawableChild(this.inputField);
      this.setFocused(this.inputField);
      this.sendButton = ButtonWidget.builder(Text.literal("发送"), btn -> this.submitInput()).dimensions(startX + inputWidth + 4, y, buttonWidth, 20).build();
      this.addDrawableChild(this.sendButton);
   }

   private void submitInput() {
      String userInput = this.inputField.getText().trim();
      this.closeInputField();
      if (!userInput.isEmpty()) {
         LOGGER.debug("[AI] 发送用户消息: {}", userInput);
         this.conversationHistory.add(new DeepSeekApiService.Message("user", userInput));
         this.state = AiHumanSkinPaperScreen.State.LOADING;
         this.requestAi();
      }
   }

   private void doRequestAfterFadeOut() {
      this.fullText = "";
      this.revealedChars = 0;
      this.state = AiHumanSkinPaperScreen.State.LOADING;
      this.loadingStartTime = System.currentTimeMillis();
      this.requestAi();
   }

   private void closeInputField() {
      if (this.inputField != null) {
         this.remove(this.inputField);
         this.inputField = null;
      }

      if (this.sendButton != null) {
         this.remove(this.sendButton);
         this.sendButton = null;
      }

      this.setFocused(null);
      this.state = AiHumanSkinPaperScreen.State.IDLE;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      long now = System.currentTimeMillis();
      if (this.state == AiHumanSkinPaperScreen.State.CLOSING) {
         long elapsed = now - this.closeStartTime;
         this.fadeAlpha = 1.0F - (float)elapsed / 500.0F;
         if (this.fadeAlpha <= 0.0F) {
            this.fadeAlpha = 0.0F;
            MinecraftClient.getInstance().setScreen(null);
            return;
         }
      }

      if (this.state == AiHumanSkinPaperScreen.State.FADING_OUT) {
         long elapsed = now - this.fadeOutStartTime;
         this.fadeAlpha = 1.0F - (float)elapsed / 300.0F;
         if (this.fadeAlpha <= 0.0F) {
            this.fadeAlpha = 0.0F;
            if (this.pendingText != null) {
               this.fullText = this.pendingText;
               this.pendingText = null;
               this.revealedChars = 0;
               this.lastCharTime = System.currentTimeMillis();
               this.state = AiHumanSkinPaperScreen.State.TYPING;
            } else {
               this.doRequestAfterFadeOut();
            }

            return;
         }
      }

      if (this.state == AiHumanSkinPaperScreen.State.LOADING_FADE_OUT) {
         long elapsed = now - this.loadingFadeOutStartTime;
         float loadingFadeAlpha = 1.0F - (float)elapsed / 300.0F;
         if (loadingFadeAlpha <= 0.0F) {
            this.state = AiHumanSkinPaperScreen.State.TYPING;
         } else {
            int loadingFadeColor = (int)(loadingFadeAlpha * 255.0F) << 24 | 0xFF0000;
            context.drawCenteredTextWithShadow(this.textRenderer, "人皮纸上逐渐开始拼凑出歪扭的字迹...", this.width / 2, this.height / 2 - 10, loadingFadeColor);
            super.render(context, mouseX, mouseY, delta);
         }
      } else {
         if (this.state == AiHumanSkinPaperScreen.State.TYPING && now - this.lastCharTime >= 80L && this.revealedChars < this.fullText.length()) {
            this.revealedChars++;
            this.lastCharTime = now;
            if (this.revealedChars >= this.fullText.length()) {
               this.state = AiHumanSkinPaperScreen.State.IDLE;
            }
         }

         int textColor = 16711680;
         if (this.state == AiHumanSkinPaperScreen.State.CLOSING || this.state == AiHumanSkinPaperScreen.State.FADING_OUT) {
            int alpha = (int)(this.fadeAlpha * 255.0F);
            textColor = alpha << 24 | 0xFF0000;
         }

         if (this.state == AiHumanSkinPaperScreen.State.LOADING && this.isFirstRequest) {
            String visibleText = "人皮纸上逐渐开始拼凑出歪扭的字迹...";
            long loadingElapsed = now - this.loadingStartTime;
            double loadingAlpha = Math.min(1.0, loadingElapsed / 500.0);
            int loadingColor = (int)(loadingAlpha * 255.0) << 24 | 0xFF0000;
            context.drawCenteredTextWithShadow(this.textRenderer, visibleText, this.width / 2, this.height / 2 - 10, loadingColor);
            super.render(context, mouseX, mouseY, delta);
         } else {
            String visibleText = this.fullText.substring(0, Math.min(this.revealedChars, this.fullText.length()));
            List<OrderedText> wrappedText = this.textRenderer.wrapLines(Text.of(visibleText), 300);
            int y = 50;

            for (OrderedText line : wrappedText) {
               context.drawTextWithShadow(this.textRenderer, line, this.width / 2 - 150, y, textColor);
               y += 9 + 4;
            }

            super.render(context, mouseX, mouseY, delta);
         }
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.state == AiHumanSkinPaperScreen.State.INPUT) {
         if (keyCode == 257) {
            this.submitInput();
            return true;
         } else if (keyCode == 256) {
            this.closeInputField();
            return true;
         } else {
            return this.inputField != null && this.inputField.keyPressed(keyCode, scanCode, modifiers) ? true : super.keyPressed(keyCode, scanCode, modifiers);
         }
      } else if (keyCode == 256) {
         if (this.state != AiHumanSkinPaperScreen.State.CLOSING
            && this.state != AiHumanSkinPaperScreen.State.FADING_OUT
            && this.state != AiHumanSkinPaperScreen.State.LOADING_FADE_OUT) {
            this.startClosing();
            return true;
         } else {
            return true;
         }
      } else if (keyCode == 32) {
         if (this.state == AiHumanSkinPaperScreen.State.IDLE) {
            this.toggleInputField();
            return true;
         } else {
            return true;
         }
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean charTyped(char chr, int modifiers) {
      return this.state == AiHumanSkinPaperScreen.State.INPUT && this.inputField != null
         ? this.inputField.charTyped(chr, modifiers)
         : super.charTyped(chr, modifiers);
   }

   public void tick() {
      if (this.state == AiHumanSkinPaperScreen.State.INPUT && this.inputField != null) {
         this.inputField.tick();
      }
   }

   private void startClosing() {
      if (this.state == AiHumanSkinPaperScreen.State.INPUT) {
         this.closeInputField();
      }

      MinecraftClient.getInstance().setScreen(null);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public void close() {
      this.client.setScreen(null);
   }

   public void renderBackground(DrawContext context) {
      context.drawTexture(BACKGROUND_TEXTURE, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
   }

   private enum State {
      LOADING,
      LOADING_FADE_OUT,
      TYPING,
      IDLE,
      INPUT,
      FADING_OUT,
      CLOSING;
   }
}
