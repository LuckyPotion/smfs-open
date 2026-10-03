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
      public int method_48402(Type type) {
         return switch (type) {
            case field_41934 -> 250;
            case field_41935 -> 380;
            case field_41936 -> 350;
            case field_41937 -> 300;
            default -> throw new IncompatibleClassChangeError();
         };
      }

      public int method_48403(Type type) {
         return switch (type) {
            case field_41934 -> 2;
            case field_41935 -> 6;
            case field_41936 -> 5;
            case field_41937 -> 2;
            default -> throw new IncompatibleClassChangeError();
         };
      }

      public int method_7699() {
         return 15;
      }

      public SoundEvent method_7698() {
         return SoundEvents.field_14581;
      }

      public Ingredient method_7695() {
         return Ingredient.method_8091(new ItemConvertible[]{(ItemConvertible)Registries.field_41178.method_10223(new Identifier("smfs", "eerie_rag"))});
      }

      public String method_7694() {
         return "smfs:ghost_shroud";
      }

      public float method_7700() {
         return 1.0F;
      }

      public float method_24355() {
         return 0.0F;
      }
   };

   public GhostShroudArmorItem(Type type, Settings settings) {
      super(GHOST_SHROUD_ARMOR_MATERIAL, type, settings);
   }

   public boolean method_7846() {
      return false;
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);

      String itemKey = switch (this.field_41933) {
         case field_41934 -> "ghost_shroud_helmet";
         case field_41935 -> "ghost_shroud_chestplate";
         case field_41936 -> "ghost_shroud_leggings";
         case field_41937 -> "ghost_shroud_boots";
         default -> throw new IncompatibleClassChangeError();
      };
      tooltip.add(Text.method_43471("item.smfs." + itemKey + ".description.source"));
      tooltip.add(Text.method_43471("item.smfs." + itemKey + ".description.desc"));
      tooltip.add(Text.method_43471("item.smfs." + itemKey + ".description.type"));
      tooltip.add(Text.method_43471("item.smfs." + itemKey + ".effect.pre_aberration"));
      tooltip.add(Text.method_43471("item.smfs." + itemKey + ".effect.post_aberration"));
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
      ARMOR_MODIFIER_IDS.put(Type.field_41937, UUID.fromString("A1B2C3D4-E5F6-7890-ABCD-EF1234567890"));
      ARMOR_MODIFIER_IDS.put(Type.field_41936, UUID.fromString("B1C2D3E4-F5A6-7890-BCDE-F01234567890"));
      ARMOR_MODIFIER_IDS.put(Type.field_41935, UUID.fromString("C1D2E3F4-A5B6-7890-CDEF-A12345678901"));
      ARMOR_MODIFIER_IDS.put(Type.field_41934, UUID.fromString("D1E2F3A4-B5C6-7890-DEFA-B12345678901"));
   }
}
