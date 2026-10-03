package com.xie.smfs.api.impl;

import com.xie.smfs.api.ConfigAPI;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ConfigAPIImpl implements ConfigAPI {
   public static ConfigAPIImpl INSTANCE = null;
   private final ConcurrentHashMap<String, ConcurrentHashMap<String, ConfigAPIImpl.ConfigEntry<?>>> configs = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<Consumer<Object>>>> listeners = new ConcurrentHashMap<>();

   @Override
   public ConfigAPI.ConfigBuilder<Integer> createIntConfig(String modId, String key) {
      return new ConfigAPIImpl.ConfigBuilderImpl<>(modId, key, Integer.class);
   }

   @Override
   public ConfigAPI.ConfigBuilder<Double> createDoubleConfig(String modId, String key) {
      return new ConfigAPIImpl.ConfigBuilderImpl<>(modId, key, Double.class);
   }

   @Override
   public ConfigAPI.ConfigBuilder<Boolean> createBooleanConfig(String modId, String key) {
      return new ConfigAPIImpl.ConfigBuilderImpl<>(modId, key, Boolean.class);
   }

   @Override
   public ConfigAPI.ConfigBuilder<String> createStringConfig(String modId, String key) {
      return new ConfigAPIImpl.ConfigBuilderImpl<>(modId, key, String.class);
   }

   @Override
   public <T extends Enum<T>> ConfigAPI.ConfigBuilder<T> createEnumConfig(String modId, String key, Class<T> enumClass) {
      return new ConfigAPIImpl.ConfigBuilderImpl<>(modId, key, enumClass);
   }

   @Override
   public boolean addIntConfig(String modId, String key, int defaultValue, String description) {
      return this.createIntConfig(modId, key).withDefault(defaultValue).withDescription(description).register();
   }

   @Override
   public boolean addDoubleConfig(String modId, String key, double defaultValue, String description) {
      return this.createDoubleConfig(modId, key).withDefault(defaultValue).withDescription(description).register();
   }

   @Override
   public boolean addBooleanConfig(String modId, String key, boolean defaultValue, String description) {
      return this.createBooleanConfig(modId, key).withDefault(defaultValue).withDescription(description).register();
   }

   @Override
   public boolean addStringConfig(String modId, String key, String defaultValue, String description) {
      return this.createStringConfig(modId, key).withDefault(defaultValue).withDescription(description).register();
   }

   private <T> ConfigAPIImpl.ConfigEntry<T> getConfigEntry(String modId, String key, Class<T> type) {
      ConcurrentHashMap<String, ConfigAPIImpl.ConfigEntry<?>> modConfigs = this.configs.get(modId);
      if (modConfigs == null) {
         return null;
      }

      ConfigAPIImpl.ConfigEntry<?> entry = modConfigs.get(key);
      return (ConfigAPIImpl.ConfigEntry<T>)(entry != null && type.isAssignableFrom(entry.type) ? entry : null);
   }

   @Override
   public Integer getIntConfig(String modId, String key) {
      ConfigAPIImpl.ConfigEntry<Integer> entry = this.getConfigEntry(modId, key, Integer.class);
      return entry != null ? entry.getValue() : null;
   }

   @Override
   public Double getDoubleConfig(String modId, String key) {
      ConfigAPIImpl.ConfigEntry<Double> entry = this.getConfigEntry(modId, key, Double.class);
      return entry != null ? entry.getValue() : null;
   }

   @Override
   public Boolean getBooleanConfig(String modId, String key) {
      ConfigAPIImpl.ConfigEntry<Boolean> entry = this.getConfigEntry(modId, key, Boolean.class);
      return entry != null ? entry.getValue() : null;
   }

   @Override
   public String getStringConfig(String modId, String key) {
      ConfigAPIImpl.ConfigEntry<String> entry = this.getConfigEntry(modId, key, String.class);
      return entry != null ? entry.getValue() : null;
   }

   @Override
   public <T extends Enum<T>> T getEnumConfig(String modId, String key, Class<T> enumClass) {
      ConfigAPIImpl.ConfigEntry<T> entry = this.getConfigEntry(modId, key, enumClass);
      return entry != null ? entry.getValue() : null;
   }

   @Override
   public boolean setIntConfig(String modId, String key, int value) {
      return this.setConfigValue(modId, key, value, Integer.class);
   }

   @Override
   public boolean setDoubleConfig(String modId, String key, double value) {
      return this.setConfigValue(modId, key, value, Double.class);
   }

   @Override
   public boolean setBooleanConfig(String modId, String key, boolean value) {
      return this.setConfigValue(modId, key, value, Boolean.class);
   }

   @Override
   public boolean setStringConfig(String modId, String key, String value) {
      return this.setConfigValue(modId, key, value, String.class);
   }

   @Override
   public <T extends Enum<T>> boolean setEnumConfig(String modId, String key, T value) {
      return this.setConfigValue(modId, key, value, value.getDeclaringClass());
   }

   private <T> boolean setConfigValue(String modId, String key, T value, Class<T> type) {
      ConfigAPIImpl.ConfigEntry<T> entry = this.getConfigEntry(modId, key, type);
      if (entry == null) {
         return false;
      }

      if (!entry.validate(value)) {
         return false;
      }

      T oldValue = entry.getValue();
      entry.setValue(value);
      this.triggerConfigChange(modId, key, oldValue, value);
      return true;
   }

   @Override
   public boolean resetConfig(String modId, String key) {
      ConcurrentHashMap<String, ConfigAPIImpl.ConfigEntry<?>> modConfigs = this.configs.get(modId);
      if (modConfigs == null) {
         return false;
      }

      ConfigAPIImpl.ConfigEntry<?> entry = modConfigs.get(key);
      if (entry == null) {
         return false;
      }

      Object oldValue = entry.getValue();
      entry.setValue(null);
      this.triggerConfigChange(modId, key, oldValue, entry.getDefaultValue());
      return true;
   }

   @Override
   public boolean hasConfig(String modId, String key) {
      ConcurrentHashMap<String, ConfigAPIImpl.ConfigEntry<?>> modConfigs = this.configs.get(modId);
      return modConfigs != null && modConfigs.containsKey(key);
   }

   @Override
   public boolean removeConfig(String modId, String key) {
      ConcurrentHashMap<String, ConfigAPIImpl.ConfigEntry<?>> modConfigs = this.configs.get(modId);
      if (modConfigs == null) {
         return false;
      }

      ConfigAPIImpl.ConfigEntry<?> entry = modConfigs.remove(key);
      if (entry == null) {
         return false;
      }

      this.listeners.computeIfPresent(modId, (k, v) -> {
         v.remove(key);
         return v;
      });
      return true;
   }

   @Override
   public ConfigAPI.ConfigValidator<?> getConfigValidator(String modId, String key) {
      ConcurrentHashMap<String, ConfigAPIImpl.ConfigEntry<?>> modConfigs = this.configs.get(modId);
      if (modConfigs == null) {
         return null;
      }

      ConfigAPIImpl.ConfigEntry<?> entry = modConfigs.get(key);
      return entry != null ? entry.getValidator() : null;
   }

   @Override
   public Object getConfigDefaultValue(String modId, String key) {
      ConcurrentHashMap<String, ConfigAPIImpl.ConfigEntry<?>> modConfigs = this.configs.get(modId);
      if (modConfigs == null) {
         return null;
      }

      ConfigAPIImpl.ConfigEntry<?> entry = modConfigs.get(key);
      return entry != null ? entry.getDefaultValue() : null;
   }

   @Override
   public String getConfigDescription(String modId, String key) {
      ConcurrentHashMap<String, ConfigAPIImpl.ConfigEntry<?>> modConfigs = this.configs.get(modId);
      if (modConfigs == null) {
         return "";
      }

      ConfigAPIImpl.ConfigEntry<?> entry = modConfigs.get(key);
      return entry != null ? entry.getDescription() : "";
   }

   @Override
   public <T> boolean validateConfigValue(String modId, String key, T value) {
      ConcurrentHashMap<String, ConfigAPIImpl.ConfigEntry<?>> modConfigs = this.configs.get(modId);
      if (modConfigs == null) {
         return false;
      }

      ConfigAPIImpl.ConfigEntry<?> entry = modConfigs.get(key);
      if (entry == null) {
         return false;
      }

      try {
         return this.validateEntryValue(entry, value);
      } catch (ClassCastException e) {
         return false;
      }
   }

   @Override
   public <T> String getValidationErrorMessage(String modId, String key, T value) {
      ConcurrentHashMap<String, ConfigAPIImpl.ConfigEntry<?>> modConfigs = this.configs.get(modId);
      if (modConfigs == null) {
         return "Configuration not found";
      }

      ConfigAPIImpl.ConfigEntry<?> entry = modConfigs.get(key);
      if (entry == null) {
         return "Configuration not found";
      }

      try {
         return this.getEntryValidationErrorMessage(entry, value);
      } catch (ClassCastException e) {
         return "Invalid value type";
      }
   }

   private <T> boolean validateEntryValue(ConfigAPIImpl.ConfigEntry<?> entry, Object value) {
      try {
         ConfigAPIImpl.ConfigEntry<T> typedEntry = (ConfigAPIImpl.ConfigEntry<T>)entry;
         T typedValue = (T)value;
         return typedEntry.validate(typedValue);
      } catch (ClassCastException e) {
         return false;
      }
   }

   private <T> String getEntryValidationErrorMessage(ConfigAPIImpl.ConfigEntry<?> entry, Object value) {
      try {
         ConfigAPIImpl.ConfigEntry<T> typedEntry = (ConfigAPIImpl.ConfigEntry<T>)entry;
         T typedValue = (T)value;
         return typedEntry.getValidationErrorMessage(typedValue);
      } catch (ClassCastException e) {
         return "Invalid value type";
      }
   }

   @Override
   public boolean saveConfig(String modId) {
      return true;
   }

   @Override
   public boolean loadConfig(String modId) {
      return true;
   }

   @Override
   public Set<String> getConfigKeys(String modId) {
      ConcurrentHashMap<String, ConfigAPIImpl.ConfigEntry<?>> modConfigs = this.configs.get(modId);
      return modConfigs != null ? modConfigs.keySet() : Collections.emptySet();
   }

   @Override
   public void addConfigChangeListener(String modId, String key, Consumer<Object> listener) {
      this.listeners.computeIfAbsent(modId, k -> new ConcurrentHashMap<>()).computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(listener);
   }

   @Override
   public void removeConfigChangeListener(String modId, String key, Consumer<Object> listener) {
      ConcurrentHashMap<String, CopyOnWriteArrayList<Consumer<Object>>> modListeners = this.listeners.get(modId);
      if (modListeners != null) {
         CopyOnWriteArrayList<Consumer<Object>> keyListeners = modListeners.get(key);
         if (keyListeners != null) {
            keyListeners.remove(listener);
         }
      }
   }

   @Override
   public void triggerConfigChange(String modId, String key, Object oldValue, Object newValue) {
      ConcurrentHashMap<String, CopyOnWriteArrayList<Consumer<Object>>> modListeners = this.listeners.get(modId);
      if (modListeners != null) {
         CopyOnWriteArrayList<Consumer<Object>> keyListeners = modListeners.get(key);
         if (keyListeners != null) {
            for (Consumer<Object> listener : keyListeners) {
               try {
                  listener.accept(newValue);
               } catch (Exception var10) {
               }
            }
         }
      }
   }

   private class ConfigBuilderImpl<T> implements ConfigAPI.ConfigBuilder<T> {
      private final String modId;
      private final String key;
      private final Class<T> type;
      private T defaultValue;
      private ConfigAPI.ConfigValidator<T> validator;
      private String description;
      private Comparable<T> minValue;
      private Comparable<T> maxValue;

      public ConfigBuilderImpl(String modId, String key, Class<T> type) {
         this.modId = modId;
         this.key = key;
         this.type = type;
      }

      @Override
      public ConfigAPI.ConfigBuilder<T> withDefault(T defaultValue) {
         this.defaultValue = defaultValue;
         return this;
      }

      @Override
      public ConfigAPI.ConfigBuilder<T> withValidator(ConfigAPI.ConfigValidator<T> validator) {
         this.validator = validator;
         return this;
      }

      @Override
      public ConfigAPI.ConfigBuilder<T> withDescription(String description) {
         this.description = description;
         return this;
      }

      @Override
      public ConfigAPI.ConfigBuilder<T> withRange(Comparable<T> min, Comparable<T> max) {
         this.minValue = min;
         this.maxValue = max;
         return this;
      }

      @Override
      public boolean register() {
         ConfigAPIImpl.ConfigEntry<T> entry = new ConfigAPIImpl.ConfigEntry<>(this.key, this.type);
         entry.setDefaultValue(this.defaultValue);
         entry.setValidator(this.validator);
         entry.setDescription(this.description);
         entry.setMinValue(this.minValue);
         entry.setMaxValue(this.maxValue);
         ConfigAPIImpl.this.configs.computeIfAbsent(this.modId, k -> new ConcurrentHashMap<>()).put(this.key, entry);
         return true;
      }
   }

   private static class ConfigEntry<T> {
      private final String key;
      private final Class<T> type;
      private T value;
      private T defaultValue;
      private ConfigAPI.ConfigValidator<T> validator;
      private String description;
      private Comparable<T> minValue;
      private Comparable<T> maxValue;

      public ConfigEntry(String key, Class<T> type) {
         this.key = key;
         this.type = type;
      }

      public T getValue() {
         return this.value != null ? this.value : this.defaultValue;
      }

      public void setValue(T value) {
         this.value = value;
      }

      public T getDefaultValue() {
         return this.defaultValue;
      }

      public void setDefaultValue(T defaultValue) {
         this.defaultValue = defaultValue;
      }

      public ConfigAPI.ConfigValidator<T> getValidator() {
         return this.validator;
      }

      public void setValidator(ConfigAPI.ConfigValidator<T> validator) {
         this.validator = validator;
      }

      public String getDescription() {
         return this.description != null ? this.description : "";
      }

      public void setDescription(String description) {
         this.description = description;
      }

      public Comparable<T> getMinValue() {
         return this.minValue;
      }

      public void setMinValue(Comparable<T> minValue) {
         this.minValue = minValue;
      }

      public Comparable<T> getMaxValue() {
         return this.maxValue;
      }

      public void setMaxValue(Comparable<T> maxValue) {
         this.maxValue = maxValue;
      }

      public boolean validate(T value) {
         if (this.validator != null && !this.validator.isValid(value)) {
            return false;
         } else {
            return this.minValue != null && this.minValue.compareTo(value) > 0 ? false : this.maxValue == null || this.maxValue.compareTo(value) >= 0;
         }
      }

      public String getValidationErrorMessage(T value) {
         if (this.validator != null && !this.validator.isValid(value)) {
            return this.validator.getErrorMessage(value);
         } else if (this.minValue != null && this.minValue.compareTo(value) > 0) {
            return "Value " + value + " is less than minimum " + this.minValue;
         } else {
            return this.maxValue != null && this.maxValue.compareTo(value) < 0 ? "Value " + value + " is greater than maximum " + this.maxValue : "";
         }
      }
   }
}
