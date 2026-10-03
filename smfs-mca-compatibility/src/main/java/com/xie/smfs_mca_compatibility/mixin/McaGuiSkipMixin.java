package com.xie.smfs_mca_compatibility.mixin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class McaGuiSkipMixin {
   private static final Logger LOGGER = LoggerFactory.getLogger("SMFS MCA Compatibility");

   @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
   private void onSetScreen(Screen screen, CallbackInfo ci) {
      if (screen != null) {
         String className = screen.getClass().getName();
         String simpleName = screen.getClass().getSimpleName();
         LOGGER.debug("检测到界面: {} ({})", simpleName, className);
         if (this.shouldSkipDestinyScreen(className, simpleName)) {
            LOGGER.debug("✓ 已拦截凡家物语命运选择界面: {}", simpleName);
            LOGGER.debug("  → 自动应用默认配置:");
            LOGGER.debug("    - 性别: 男 (Male)");
            LOGGER.debug("    - 模型: 原版 (Vanilla)");
            LOGGER.debug("    - 名字: 使用默认");
            LOGGER.debug("    - 出生点: 随机");
            LOGGER.debug("    - 清除全部Buff: 是");
            this.clearAllPlayerBuffs();
            this.applyDefaultDestinySettings(screen);
            ci.cancel();
         }
      }
   }

   private boolean shouldSkipDestinyScreen(String className, String simpleName) {
      return simpleName.equals("DestinyScreen") || className.endsWith(".DestinyScreen") || className.contains("conczin.mca.client.gui.DestinyScreen");
   }

   private void clearAllPlayerBuffs() {
      try {
         MinecraftClient client = MinecraftClient.method_1551();
         if (client.field_1724 != null) {
            PlayerEntity player = client.field_1724;
            Collection<StatusEffectInstance> effects = player.method_6026();
            int buffCount = effects.size();
            if (buffCount > 0) {
               LOGGER.debug("发现 {} 个Buff，正在清除...", buffCount);

               for (StatusEffectInstance effect : new ArrayList<>(effects)) {
                  String effectName = effect.method_5579().method_5567();
                  player.method_6016(effect.method_5579());
                  LOGGER.debug("  ✓ 已清除Buff: {}", effectName);
               }

               LOGGER.debug("✓ 成功清除全部 {} 个Buff", buffCount);
            } else {
               LOGGER.debug("○ 玩家身上没有Buff");
            }
         } else {
            LOGGER.debug("⚠ 无法获取玩家实例，跳过清除Buff (将在服务器端处理)");
         }
      } catch (Exception e) {
         LOGGER.debug("❌ 清除Buff时出错: {}", e.getMessage(), e);
      }
   }

   private void applyDefaultDestinySettings(Screen screen) {
      try {
         Object destinyScreen = screen;
         Class<?> screenClass = destinyScreen.getClass();

         try {
            Field nameField = null;
            Field genderField = null;
            Field modelField = null;

            for (Field field : screenClass.getDeclaredFields()) {
               field.setAccessible(true);
               String fieldName = field.getName().toLowerCase();
               if (!fieldName.contains("name") || field.getType() != String.class) {
                  if (!fieldName.contains("gender") && !fieldName.contains("sex")
                     || field.getType() != String.class && field.getType() != int.class && field.getType() != Enum.class) {
                     if ((fieldName.contains("model") || fieldName.contains("skin"))
                        && (field.getType() == String.class || field.getType() == int.class || field.getType() == Enum.class)) {
                        modelField = field;
                     }
                  } else {
                     genderField = field;
                  }
               }
            }

            if (genderField != null) {
               genderField.setAccessible(true);
               Class<?> type = genderField.getType();
               if (type == String.class) {
                  genderField.set(destinyScreen, "male");
               } else if (type == int.class || type == Integer.class) {
                  genderField.set(destinyScreen, 0);
               } else if (type.isEnum()) {
                  Object[] enumConstants = type.getEnumConstants();
                  if (enumConstants.length > 0) {
                     for (Object enumConst : enumConstants) {
                        if (enumConst.toString().toLowerCase().contains("male") || enumConst.toString().toLowerCase().equals("m")) {
                           genderField.set(destinyScreen, enumConst);
                           break;
                        }
                     }

                     if (genderField.get(destinyScreen) == null) {
                        genderField.set(destinyScreen, enumConstants[0]);
                     }
                  }
               }

               LOGGER.debug("已设置性别为: Male");
            }

            if (modelField != null) {
               modelField.setAccessible(true);
               Class<?> type = modelField.getType();
               if (type == String.class) {
                  modelField.set(destinyScreen, "vanilla");
               } else if (type == int.class || type == Integer.class) {
                  modelField.set(destinyScreen, 2);
               } else if (type.isEnum()) {
                  Object[] enumConstants = type.getEnumConstants();
                  if (enumConstants.length > 0) {
                     for (Object enumConst : enumConstants) {
                        if (enumConst.toString().toLowerCase().contains("vanilla") || enumConst.toString().toLowerCase().equals("player")) {
                           modelField.set(destinyScreen, enumConst);
                           break;
                        }
                     }

                     if (modelField.get(destinyScreen) == null && enumConstants.length > 1) {
                        modelField.set(destinyScreen, enumConstants[enumConstants.length - 1]);
                     }
                  }
               }

               LOGGER.debug("已设置为原版模型: Vanilla");
            }
         } catch (Exception e) {
            LOGGER.debug("无法设置默认字段 (这是正常的，将使用MCA内置默认值): {}", e.getMessage());
         }

         try {
            Method acceptMethod = null;

            for (Method method : screenClass.getDeclaredMethods()) {
               if ((
                     method.getName().toLowerCase().contains("accept")
                        || method.getName().toLowerCase().contains("confirm")
                        || method.getName().toLowerCase().contains("submit")
                        || method.getName().toLowerCase().contains("done")
                  )
                  && (method.getParameterCount() == 0 || method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(Object.class))) {
                  acceptMethod = method;
                  break;
               }
            }

            if (acceptMethod != null) {
               acceptMethod.setAccessible(true);
               if (acceptMethod.getParameterCount() == 0) {
                  acceptMethod.invoke(destinyScreen);
               } else {
                  acceptMethod.invoke(destinyScreen);
               }

               LOGGER.debug("✓ 已触发默认确认操作");
            } else {
               LOGGER.debug("○ 未找到确认方法，界面已关闭 (MCA将使用内置默认值)");
            }
         } catch (Exception e) {
            LOGGER.debug("无法调用确认方法 (这是正常的): {}", e.getMessage());
            LOGGER.debug("○ 界面已关闭，MCA应会使用合理的默认值");
         }
      } catch (Exception e) {
         LOGGER.debug("处理默认设置时出错: {}", e.getMessage(), e);
      }
   }
}
