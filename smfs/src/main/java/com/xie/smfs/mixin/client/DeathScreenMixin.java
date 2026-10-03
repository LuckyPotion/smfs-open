package com.xie.smfs.mixin.client;

import com.xie.smfs.client.MusicBoxCurseSoundHandler;
import com.xie.smfs.client.screen.CustomDeathScreen;
import java.lang.reflect.Field;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class DeathScreenMixin {
   @Inject(method = "init", at = @At("HEAD"), cancellable = true)
   private void replaceDeathScreen(CallbackInfo ci) {
      MusicBoxCurseSoundHandler.stopOnDeath();
      MinecraftClient client = MinecraftClient.method_1551();
      DeathScreen deathScreen = (DeathScreen)client.field_1755;
      if (!this.isHardcore(deathScreen)) {
         ci.cancel();
         client.method_1507(new CustomDeathScreen(this.getDeathMessage(deathScreen), this.isHardcore(deathScreen)));
      }
   }

   private Text getDeathMessage(DeathScreen screen) {
      try {
         Field field = DeathScreen.class.getDeclaredField("message");
         field.setAccessible(true);
         return (Text)field.get(screen);
      } catch (Exception e) {
         return Text.method_43470("未知死亡原因");
      }
   }

   private boolean isHardcore(DeathScreen screen) {
      try {
         Field field = DeathScreen.class.getDeclaredField("isHardcore");
         field.setAccessible(true);
         return field.getBoolean(screen);
      } catch (Exception e) {
         return false;
      }
   }
}
