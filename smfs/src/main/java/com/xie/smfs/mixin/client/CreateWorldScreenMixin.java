package com.xie.smfs.mixin.client;

import com.mojang.serialization.Lifecycle;
import com.xie.smfs.client.screen.ConfigScreen;
import com.xie.smfs.client.screen.WorldCreationConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
   @Invoker("addDrawableChild")
   protected abstract <T extends Element> T invokeAddDrawableChild(T element);

   @Inject(method = "init", at = @At("TAIL"))
   private void addConfigButton(CallbackInfo ci) {
      CreateWorldScreen screen = (CreateWorldScreen)this;
      int buttonWidth = 210;
      int buttonHeight = 20;
      int buttonX = (screen.width - buttonWidth) / 2;
      int buttonY = screen.width / 3;
      ButtonWidget modConfigButton = ButtonWidget.builder(Text.translatable("smfs.config.world.open_mod_config"), button -> {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client != null) {
            client.setScreen(new ConfigScreen(screen));
         }
      }).dimensions(buttonX, buttonY, buttonWidth, buttonHeight).build();
      this.invokeAddDrawableChild(modConfigButton);
      buttonY += 24;
      ButtonWidget configButton = ButtonWidget.builder(Text.translatable("smfs.config.world_creation_button"), button -> {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client != null) {
            client.setScreen(new WorldCreationConfigScreen(screen));
         }
      }).dimensions(buttonX, buttonY, buttonWidth, buttonHeight).build();
      this.invokeAddDrawableChild(configButton);
   }

   @Redirect(
      method = "createLevel",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/server/integrated/IntegratedServerLoader;tryLoad(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/screen/world/CreateWorldScreen;Lcom/mojang/serialization/Lifecycle;Ljava/lang/Runnable;Z)V"
      )
   )
   private void redirectTryLoad(MinecraftClient client, CreateWorldScreen parent, Lifecycle lifecycle, Runnable loader, boolean showWarning) {
      IntegratedServerLoader.tryLoad(client, parent, lifecycle, loader, true);
   }
}
