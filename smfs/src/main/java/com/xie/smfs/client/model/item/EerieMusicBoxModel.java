package com.xie.smfs.client.model.item;

import com.xie.smfs.item.EerieMusicBoxItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class EerieMusicBoxModel extends GeoModel<EerieMusicBoxItem> {
   public static ItemStack currentStack;

   public Identifier getModelResource(EerieMusicBoxItem animatable) {
      return currentStack != null && animatable.isOnCooldown(currentStack)
         ? new Identifier("smfs", "geo/eerie_music_box_open.geo.json")
         : new Identifier("smfs", "geo/eerie_music_box_close.geo.json");
   }

   public Identifier getTextureResource(EerieMusicBoxItem animatable) {
      return new Identifier("smfs", "textures/item/eerie_music_box.png");
   }

   public Identifier getAnimationResource(EerieMusicBoxItem animatable) {
      return null;
   }
}
