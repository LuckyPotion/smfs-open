package com.xie.smfs.item;

import com.xie.smfs.client.renderer.armor.GhostShroudArmorRenderer;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
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
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtils;

public class GhostShroudArmorItem extends ArmorItem implements GeoItem {
   private static final EnumMap<Type, UUID> ARMOR_MODIFIER_IDS = new EnumMap<>(Type.class);
   public static final ArmorMaterial GHOST_SHROUD_ARMOR_MATERIAL = new ArmorMaterial() {
      public int getDurability(Type type) {
         return switch (type) {
            case HELMET -> 250;
            case CHESTPLATE -> 380;
            case LEGGINGS -> 350;
            case BOOTS -> 300;
            default -> throw new IncompatibleClassChangeError();
         };
      }

      public int getProtection(Type type) {
         return switch (type) {
            case HELMET -> 2;
            case CHESTPLATE -> 6;
            case LEGGINGS -> 5;
            case BOOTS -> 2;
            default -> throw new IncompatibleClassChangeError();
         };
      }

      public int getEnchantability() {
         return 15;
      }

      public SoundEvent getEquipSound() {
         return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
      }

      public Ingredient getRepairIngredient() {
         return Ingredient.ofItems(new ItemConvertible[]{(ItemConvertible)Registries.ITEM.get(new Identifier("smfs", "eerie_rag"))});
      }

      public String getName() {
         return "smfs:ghost_shroud";
      }

      public float getToughness() {
         return 1.0F;
      }

      public float getKnockbackResistance() {
         return 0.0F;
      }
   };

   public GhostShroudArmorItem(Type type, Settings settings) {
      super(GHOST_SHROUD_ARMOR_MATERIAL, type, settings);
   }

   public boolean isDamageable() {
      return false;
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);

      String itemKey = switch (this.type) {
         case HELMET -> "ghost_shroud_helmet";
         case CHESTPLATE -> "ghost_shroud_chestplate";
         case LEGGINGS -> "ghost_shroud_leggings";
         case BOOTS -> "ghost_shroud_boots";
         default -> throw new IncompatibleClassChangeError();
      };
      tooltip.add(Text.translatable("item.smfs." + itemKey + ".description.source"));
      tooltip.add(Text.translatable("item.smfs." + itemKey + ".description.desc"));
      tooltip.add(Text.translatable("item.smfs." + itemKey + ".description.type"));
      tooltip.add(Text.translatable("item.smfs." + itemKey + ".effect.pre_aberration"));
      tooltip.add(Text.translatable("item.smfs." + itemKey + ".effect.post_aberration"));
   }

   @Override
   public void registerControllers(ControllerRegistrar registrar) {
   }

   @Environment(EnvType.CLIENT)
   @Override
   public void createRenderer(Consumer<Object> consumer) {
      consumer.accept(new GhostShroudArmorRenderer());
   }

   @Override
   public Supplier<Object> getRenderProvider() {
      return GeoItem.makeRenderer(this);
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return GeckoLibUtil.createInstanceCache(this);
   }

   @Override
   public double getTick(Object itemStack) {
      return RenderUtils.getCurrentTick();
   }

   static {
      ARMOR_MODIFIER_IDS.put(Type.BOOTS, UUID.fromString("A1B2C3D4-E5F6-7890-ABCD-EF1234567890"));
      ARMOR_MODIFIER_IDS.put(Type.LEGGINGS, UUID.fromString("B1C2D3E4-F5A6-7890-BCDE-F01234567890"));
      ARMOR_MODIFIER_IDS.put(Type.CHESTPLATE, UUID.fromString("C1D2E3F4-A5B6-7890-CDEF-A12345678901"));
      ARMOR_MODIFIER_IDS.put(Type.HELMET, UUID.fromString("D1E2F3A4-B5C6-7890-DEFA-B12345678901"));
   }
}
