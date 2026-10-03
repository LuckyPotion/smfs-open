package com.xie.smfs.api;

import java.util.Set;
import java.util.function.Consumer;

public interface ConfigAPI {
   static void register(ConfigAPI instance) {
      ConfigAPIHolder.INSTANCE = instance;
   }

   static ConfigAPI getInstance() {
      if (ConfigAPIHolder.INSTANCE == null) {
         throw new IllegalStateException("ConfigAPI not registered yet. Please call ConfigAPI.register() first.");
      } else {
         return ConfigAPIHolder.INSTANCE;
      }
   }

   static boolean isRegistered() {
      return ConfigAPIHolder.INSTANCE != null;
   }

   ConfigAPI.ConfigBuilder<Integer> createIntConfig(String string, String string2);

   ConfigAPI.ConfigBuilder<Double> createDoubleConfig(String string, String string2);

   ConfigAPI.ConfigBuilder<Boolean> createBooleanConfig(String string, String string2);

   ConfigAPI.ConfigBuilder<String> createStringConfig(String string, String string2);

   <T extends Enum<T>> ConfigAPI.ConfigBuilder<T> createEnumConfig(String string, String string2, Class<T> class_);

   boolean addIntConfig(String string, String string2, int i, String string3);

   boolean addDoubleConfig(String string, String string2, double d, String string3);

   boolean addBooleanConfig(String string, String string2, boolean bl, String string3);

   boolean addStringConfig(String string, String string2, String string3, String string4);

   Integer getIntConfig(String string, String string2);

   Double getDoubleConfig(String string, String string2);

   Boolean getBooleanConfig(String string, String string2);

   String getStringConfig(String string, String string2);

   <T extends Enum<T>> T getEnumConfig(String string, String string2, Class<T> class_);

   boolean setIntConfig(String string, String string2, int i);

   boolean setDoubleConfig(String string, String string2, double d);

   boolean setBooleanConfig(String string, String string2, boolean bl);

   boolean setStringConfig(String string, String string2, String string3);

   <T extends Enum<T>> boolean setEnumConfig(String string, String string2, T enum_);

   boolean resetConfig(String string, String string2);

   boolean hasConfig(String string, String string2);

   boolean removeConfig(String string, String string2);

   ConfigAPI.ConfigValidator<?> getConfigValidator(String string, String string2);

   Object getConfigDefaultValue(String string, String string2);

   String getConfigDescription(String string, String string2);

   <T> boolean validateConfigValue(String string, String string2, T object);

   <T> String getValidationErrorMessage(String string, String string2, T object);

   boolean saveConfig(String string);

   boolean loadConfig(String string);

   Set<String> getConfigKeys(String string);

   void addConfigChangeListener(String string, String string2, Consumer<Object> consumer);

   void removeConfigChangeListener(String string, String string2, Consumer<Object> consumer);

   void triggerConfigChange(String string, String string2, Object object, Object object2);

   interface ConfigBuilder<T> {
      ConfigAPI.ConfigBuilder<T> withDefault(T object);

      ConfigAPI.ConfigBuilder<T> withValidator(ConfigAPI.ConfigValidator<T> configValidator);

      ConfigAPI.ConfigBuilder<T> withDescription(String string);

      ConfigAPI.ConfigBuilder<T> withRange(Comparable<T> comparable, Comparable<T> comparable2);

      boolean register();
   }

   interface ConfigValidator<T> {
      boolean isValid(T object);

      String getErrorMessage(T object);
   }
}
