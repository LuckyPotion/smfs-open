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
      public int getDurability(Type type) {
         return switch (type) {
            case HELMET -> 363;
            case CHESTPLATE -> 528;
            case LEGGINGS -> 495;
            case BOOTS -> 429;
            default -> throw new IncompatibleClassChangeError();
         };
      }

      public int getProtection(Type type) {
         return switch (type) {
            case HELMET -> 3;
            case CHESTPLATE -> 8;
            case LEGGINGS -> 6;
            case BOOTS -> 3;
            default -> throw new IncompatibleClassChangeError();
         };
      }

      public int getEnchantability() {
         return 10;
      }

      public SoundEvent getEquipSound() {
         return SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND;
      }

      public Ingredient getRepairIngredient() {
         return Ingredient.ofItems(new ItemConvertible[]{(ItemConvertible)Registries.ITEM.get(new Identifier("smfs", "defiled_ingot"))});
      }

      public String getName() {
         return "smfs:defiled";
      }

      public float getToughness() {
         return 2.0F;
      }

      public float getKnockbackResistance() {
         return 0.0F;
      }
   };
   private static final UUID SPIRIT_RESISTANCE_MODIFIER_ID = UUID.fromString("7E9D6B3A-4C8D-4A1E-9B2F-1A3B4C5D6E7F");

   public DefiledArmorItem(Type type, Settings settings) {
      super(DEFILED_ARMOR_MATERIAL, type, settings);
   }

   public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
      if (slot == this.type.getEquipmentSlot()) {
         Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
         builder.putAll(super.getAttributeModifiers(slot));
         return builder.build();
      } else {
         return super.getAttributeModifiers(slot);
      }
   }

   public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      super.inventoryTick(stack, world, entity, slot, selected);
      if (!world.isClient() && entity instanceof PlayerEntity player) {
         EquipmentSlot equipmentSlot = null;
         if (slot >= 0 && slot < 4) {
            equipmentSlot = EquipmentSlot.fromTypeIndex(net.minecraft.entity.EquipmentSlot.Type.ARMOR, slot);
         }

         if (equipmentSlot != null && equipmentSlot == this.type.getEquipmentSlot()) {
            PlayerEvents.getSpiritAttributes(player);
         }
      }
   }

   public boolean isDamageable() {
      return false;
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);

      String itemKey = switch (this.type) {
         case HELMET -> "defiled_helmet";
         case CHESTPLATE -> "defiled_chestplate";
         case LEGGINGS -> "defiled_leggings";
         case BOOTS -> "defiled_boots";
         default -> throw new IncompatibleClassChangeError();
      };
      tooltip.add(Text.translatable("item.smfs." + itemKey + ".description.source"));
      tooltip.add(Text.translatable("item.smfs." + itemKey + ".description.desc"));
      tooltip.add(Text.translatable("item.smfs." + itemKey + ".description.type"));
      tooltip.add(Text.translatable("item.smfs." + itemKey + ".effect.spirit_resistance"));
      tooltip.add(Text.translatable("item.smfs." + itemKey + ".effect.max_sanity"));
      tooltip.add(Text.translatable("item.smfs." + itemKey + ".effect.max_spirit"));
   }

   static {
      ARMOR_MODIFIER_IDS.put(Type.BOOTS, UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
      ARMOR_MODIFIER_IDS.put(Type.LEGGINGS, UUID.fromString("D8499B04-0E66-4726-AB29-64490D7C3B51"));
      ARMOR_MODIFIER_IDS.put(Type.CHESTPLATE, UUID.fromString("9F3D476D-C118-4C66-A356-95C4C6D8C7B2"));
      ARMOR_MODIFIER_IDS.put(Type.HELMET, UUID.fromString("2AD3F246-FEE1-4ECB-ACB6-7F4B2A7F5F3A"));
   }
}
