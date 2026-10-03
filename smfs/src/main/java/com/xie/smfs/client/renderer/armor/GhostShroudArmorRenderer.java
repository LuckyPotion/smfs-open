package com.xie.smfs.client.renderer.armor;

import com.xie.smfs.client.model.armor.GhostShroudArmorModel;
import com.xie.smfs.item.GhostShroudArmorItem;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class GhostShroudArmorRenderer extends GeoArmorRenderer<GhostShroudArmorItem> implements RenderProvider {
   private static final Identifier TEXTURE = new Identifier("smfs", "textures/item/armor/ghost_shroud_chestplate.png");
   private final GhostShroudItemRenderer itemRenderer = new GhostShroudItemRenderer();

   public GhostShroudArmorRenderer() {
      super(new GhostShroudArmorModel());
   }

   public Identifier getTextureLocation(GhostShroudArmorItem item) {
      return TEXTURE;
   }

   public BuiltinModelItemRenderer getCustomRenderer() {
      return this.itemRenderer;
   }

   public BipedEntityModel<LivingEntity> getHumanoidArmorModel(
      LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, BipedEntityModel<LivingEntity> original
   ) {
      this.prepForRender(livingEntity, itemStack, equipmentSlot, original);
      return this;
   }
}
