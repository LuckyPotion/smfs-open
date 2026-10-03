package com.xie.smfs.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.xie.smfs.common.events.PlayerEvents;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ArmorItem.Type;
import net.minecraft.item.Item.Settings;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DefiledArmorItem extends ArmorItem {
   private static final EnumMap<Type, UUID> ARMOR_MODIFIER_IDS = new EnumMap<>(Type.class);
   public static final ArmorMaterial DEFILED_ARMOR_MATERIAL = new ArmorMaterial() {
      public int method_48402(Type type) {
         return switch (type) {
            case field_41934 -> 363;
            case field_41935 -> 528;
            case field_41936 -> 495;
            case field_41937 -> 429;
            default -> throw new IncompatibleClassChangeError();
         };
      }

      public int method_48403(Type type) {
         return switch (type) {
            case field_41934 -> 3;
            case field_41935 -> 8;
            case field_41936 -> 6;
            case field_41937 -> 3;
            default -> throw new IncompatibleClassChangeError();
         };
      }

      public int method_7699() {
         return 10;
      }

      public SoundEvent method_7698() {
         return SoundEvents.field_15103;
      }

      public Ingredient method_7695() {
         return Ingredient.method_8091(new ItemConvertible[]{(ItemConvertible)Registries.field_41178.method_10223(new Identifier("smfs", "defiled_ingot"))});
      }

      public String method_7694() {
         return "smfs:defiled";
      }

      public float method_7700() {
         return 2.0F;
      }

      public float method_24355() {
         return 0.0F;
      }
   };
   private static final UUID SPIRIT_RESISTANCE_MODIFIER_ID = UUID.fromString("7E9D6B3A-4C8D-4A1E-9B2F-1A3B4C5D6E7F");

   public DefiledArmorItem(Type type, Settings settings) {
      super(DEFILED_ARMOR_MATERIAL, type, settings);
   }

   public Multimap<EntityAttribute, EntityAttributeModifier> method_7844(EquipmentSlot slot) {
      if (slot == this.field_41933.method_48399()) {
         Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
         builder.putAll(super.method_7844(slot));
         return builder.build();
      } else {
         return super.method_7844(slot);
      }
   }

   public void method_7888(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      super.method_7888(stack, world, entity, slot, selected);
      if (!world.method_8608() && entity instanceof PlayerEntity player) {
         EquipmentSlot equipmentSlot = null;
         if (slot >= 0 && slot < 4) {
            equipmentSlot = EquipmentSlot.method_20234(net.minecraft.entity.EquipmentSlot.Type.field_6178, slot);
         }

         if (equipmentSlot != null && equipmentSlot == this.field_41933.method_48399()) {
            PlayerEvents.getSpiritAttributes(player);
         }
      }
   }

   public boolean method_7846() {
      return false;
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);

      String itemKey = switch (this.field_41933) {
         case field_41934 -> "defiled_helmet";
         case field_41935 -> "defiled_chestplate";
         case field_41936 -> "defiled_leggings";
         case field_41937 -> "defiled_boots";
         default -> throw new IncompatibleClassChangeError();
      };
      tooltip.add(Text.method_43471("item.smfs." + itemKey + ".description.source"));
      tooltip.add(Text.method_43471("item.smfs." + itemKey + ".description.desc"));
      tooltip.add(Text.method_43471("item.smfs." + itemKey + ".description.type"));
      tooltip.add(Text.method_43471("item.smfs." + itemKey + ".effect.spirit_resistance"));
      tooltip.add(Text.method_43471("item.smfs." + itemKey + ".effect.max_sanity"));
      tooltip.add(Text.method_43471("item.smfs." + itemKey + ".effect.max_spirit"));
   }

   static {
      ARMOR_MODIFIER_IDS.put(Type.field_41937, UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
      ARMOR_MODIFIER_IDS.put(Type.field_41936, UUID.fromString("D8499B04-0E66-4726-AB29-64490D7C3B51"));
      ARMOR_MODIFIER_IDS.put(Type.field_41935, UUID.fromString("9F3D476D-C118-4C66-A356-95C4C6D8C7B2"));
      ARMOR_MODIFIER_IDS.put(Type.field_41934, UUID.fromString("2AD3F246-FEE1-4ECB-ACB6-7F4B2A7F5F3A"));
   }
}
