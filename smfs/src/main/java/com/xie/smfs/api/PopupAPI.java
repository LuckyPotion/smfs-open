package com.xie.smfs.api;

import java.util.List;
import java.util.function.Consumer;

public interface PopupAPI {
   static void register(PopupAPI instance) {
      PopupAPIHolder.INSTANCE = instance;
   }

   static PopupAPI getInstance() {
      if (PopupAPIHolder.INSTANCE == null) {
         throw new IllegalStateException("PopupAPI not registered yet. Please call PopupAPI.register() first.");
      } else {
         return PopupAPIHolder.INSTANCE;
      }
   }

   static boolean isRegistered() {
      return PopupAPIHolder.INSTANCE != null;
   }

   PopupAPI.PopupConfig createPopupConfig();

   void showSimplePopup(String string, List<String> list);

   void showSimplePopup(String string, List<String> list, String string2, Runnable runnable);

   void showTwoColumnPopup(String string, int i, float f, List<String> list);

   void showCustomPopup(PopupAPI.PopupConfig popupConfig);

   void showNotification(String string, int i);

   void showErrorPopup(String string);

   void showSuccessPopup(String string);

   void closeCurrentPopup();

   boolean isPopupOpen();

   void addPopupCloseListener(Consumer<Void> consumer);

   void removePopupCloseListener(Consumer<Void> consumer);

   @FunctionalInterface
   interface ButtonAction {
      void execute();
   }

   class PopupConfig {
      private String title = "";
      private int titleColor = 16777215;
      private float titleScale = 1.0F;
      private List<String> contents = List.of();
      private boolean twoColumnLayout = false;
      private String buttonText = "确认";
      private PopupAPI.ButtonAction buttonAction = () -> {};
      private boolean shouldPause = false;
      private boolean closeOnEsc = true;

      public String getTitle() {
         return this.title;
      }

      public PopupAPI.PopupConfig setTitle(String title) {
         this.title = title;
         return this;
      }

      public int getTitleColor() {
         return this.titleColor;
      }

      public PopupAPI.PopupConfig setTitleColor(int titleColor) {
         this.titleColor = titleColor;
         return this;
      }

      public float getTitleScale() {
         return this.titleScale;
      }

      public PopupAPI.PopupConfig setTitleScale(float titleScale) {
         this.titleScale = titleScale;
         return this;
      }

      public List<String> getContents() {
         return this.contents;
      }

      public PopupAPI.PopupConfig setContents(List<String> contents) {
         this.contents = contents;
         return this;
      }

      public boolean isTwoColumnLayout() {
         return this.twoColumnLayout;
      }

      public PopupAPI.PopupConfig setTwoColumnLayout(boolean twoColumnLayout) {
         this.twoColumnLayout = twoColumnLayout;
         return this;
      }

      public String getButtonText() {
         return this.buttonText;
      }

      public PopupAPI.PopupConfig setButtonText(String buttonText) {
         this.buttonText = buttonText;
         return this;
      }

      public PopupAPI.ButtonAction getButtonAction() {
         return this.buttonAction;
      }

      public PopupAPI.PopupConfig setButtonAction(PopupAPI.ButtonAction buttonAction) {
         this.buttonAction = buttonAction;
         return this;
      }

      public boolean shouldPause() {
         return this.shouldPause;
      }

      public PopupAPI.PopupConfig setShouldPause(boolean shouldPause) {
         this.shouldPause = shouldPause;
         return this;
      }

      public boolean closeOnEsc() {
         return this.closeOnEsc;
      }

      public PopupAPI.PopupConfig setCloseOnEsc(boolean closeOnEsc) {
         this.closeOnEsc = closeOnEsc;
         return this;
      }
   }
}
