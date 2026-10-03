package com.xie.smfs.api.impl;

import com.xie.smfs.api.PopupAPI;
import com.xie.smfs.client.screen.GhostAbilityPopupScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;

public class PopupAPIImpl implements PopupAPI {
   private final List<Consumer<Void>> closeListeners = new ArrayList<>();

   @Override
   public PopupAPI.PopupConfig createPopupConfig() {
      return new PopupAPI.PopupConfig();
   }

   @Override
   public void showSimplePopup(String title, List<String> contents) {
      this.showSimplePopup(title, contents, "确认", () -> {});
   }

   @Override
   public void showSimplePopup(String title, List<String> contents, String buttonText, Runnable callback) {
      this.executeOnClient(() -> {
         GhostAbilityPopupScreen.showSingleColumnPopup(title, 16777215, 1.0F, contents);
         if (callback != null) {
            this.closeListeners.add(v -> callback.run());
         }
      });
   }

   @Override
   public void showTwoColumnPopup(String title, int titleColor, float titleScale, List<String> contents) {
      this.executeOnClient(() -> GhostAbilityPopupScreen.showPopup(title, titleColor, titleScale, contents, true));
   }

   @Override
   public void showCustomPopup(PopupAPI.PopupConfig config) {
      this.executeOnClient(() -> {
         if (config.isTwoColumnLayout()) {
            GhostAbilityPopupScreen.showPopup(config.getTitle(), config.getTitleColor(), config.getTitleScale(), config.getContents(), true);
         } else {
            GhostAbilityPopupScreen.showSingleColumnPopup(config.getTitle(), config.getTitleColor(), config.getTitleScale(), config.getContents());
         }

         if (config.getButtonAction() != null) {
            this.closeListeners.add(v -> config.getButtonAction().execute());
         }
      });
   }

   @Override
   public void showNotification(String message, int durationTicks) {
      this.executeOnClient(() -> {
         List<String> contents = List.of(message);
         GhostAbilityPopupScreen.showSingleColumnPopup("通知", 65280, 1.0F, contents);
         if (durationTicks > 0) {
            new Thread(() -> {
               try {
                  Thread.sleep(durationTicks * 50);
                  this.closeCurrentPopup();
               } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
               }
            }).start();
         }
      });
   }

   @Override
   public void showErrorPopup(String errorMessage) {
      this.executeOnClient(() -> {
         List<String> contents = List.of(errorMessage);
         GhostAbilityPopupScreen.showSingleColumnPopup("错误", 16711680, 1.0F, contents);
      });
   }

   @Override
   public void showSuccessPopup(String successMessage) {
      this.executeOnClient(() -> {
         List<String> contents = List.of(successMessage);
         GhostAbilityPopupScreen.showSingleColumnPopup("成功", 65280, 1.0F, contents);
      });
   }

   @Override
   public void closeCurrentPopup() {
      this.executeOnClient(() -> {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client != null && client.currentScreen instanceof GhostAbilityPopupScreen) {
            client.currentScreen.close();
            this.notifyCloseListeners();
         }
      });
   }

   @Override
   public boolean isPopupOpen() {
      MinecraftClient client = MinecraftClient.getInstance();
      return client != null && client.currentScreen instanceof GhostAbilityPopupScreen;
   }

   @Override
   public void addPopupCloseListener(Consumer<Void> listener) {
      this.closeListeners.add(listener);
   }

   @Override
   public void removePopupCloseListener(Consumer<Void> listener) {
      this.closeListeners.remove(listener);
   }

   private void notifyCloseListeners() {
      List<Consumer<Void>> listeners = new ArrayList<>(this.closeListeners);
      listeners.forEach(listener -> listener.accept(null));
      this.closeListeners.clear();
   }

   private void executeOnClient(Runnable task) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null) {
         client.execute(task);
      }
   }
}
