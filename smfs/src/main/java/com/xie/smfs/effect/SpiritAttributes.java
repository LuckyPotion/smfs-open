package com.xie.smfs.effect;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class SpiritAttributes {
   public static final EntityAttribute SPIRIT_RESISTANCE = register("spirit_resistance", 50.0, 0.0, 100.0);
   public static final EntityAttribute SPIRIT_DAMAGE = register("spirit_damage", 10.0, 0.0, 500.0);
   public static final EntityAttribute CURRENT_SPIRIT = register("current_spirit", 50.0, 0.0, 10000.0);
   public static final EntityAttribute MAX_SPIRIT = register("max_spirit", 1000.0, 0.0, 10000.0);
   public static final EntityAttribute REVIVAL_FACTOR = register("revival_factor", 2.0, 0.0, 10.0);
   public static final EntityAttribute SANITY = register("sanity", 0.0, 0.0, 100.0);
   public static final EntityAttribute MAX_SANITY = register("max_sanity", 100.0, 0.0, 100.0);
   public static final EntityAttribute TEMP_SPIRIT_RESISTANCE = register("temp_spirit_resistance", 0.0, 0.0, 1000.0);
   public static final EntityAttribute TEMP_SPIRIT_DAMAGE = register("temp_spirit_damage", 0.0, 0.0, 500.0);
   public static final EntityAttribute TEMP_SPIRIT_DAMAGE_MULTIPLIER = register("temp_spirit_damage_multiplier", 1.0, 0.0, 10.0);
   public static final EntityAttribute TEMP_SPIRIT_RESISTANCE_MULTIPLIER = register("temp_spirit_resistance_multiplier", 1.0, 0.0, 10.0);
   public static final EntityAttribute TEMP_MAX_SPIRIT = register("temp_max_spirit", 0.0, 0.0, 10000.0);
   public static final EntityAttribute TEMP_SANITY = register("temp_sanity", 0.0, 0.0, 100.0);

   private static EntityAttribute register(String name, double defaultValue, double min, double max) {
      return (EntityAttribute)Registry.register(
         Registries.ATTRIBUTE, new Identifier("smfs", name), new ClampedEntityAttribute("attribute.smfs." + name, defaultValue, min, max).setTracked(true)
      );
   }

   public static void register() {
   }
}
