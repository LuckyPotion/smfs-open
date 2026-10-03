package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.FoodComponent.Builder;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GhostCandyItem extends Item {
   public GhostCandyItem(Settings settings) {
      super(settings.method_19265(new Builder().method_19238(2).method_19237(0.1F).method_19240().method_19242()));
   }

   public ItemStack method_7861(ItemStack stack, World world, LivingEntity user) {
      if (!world.field_9236 && user instanceof PlayerEntity player) {
         EntityAttributeInstance healthAttribute = player.method_5996(EntityAttributes.field_23716);
         if (healthAttribute != null) {
            double currentMaxHealth = healthAttribute.method_6201();
            healthAttribute.method_6192(Math.max(1.0, currentMaxHealth - 3.0));
            if (player.method_6032() > healthAttribute.method_6201()) {
               player.method_6033((float)healthAttribute.method_6201());
            }
         }

         player.method_6092(new StatusEffectInstance(ModEffects.SPIRIT_SURGE, 1200, 0, false, false));
         if (world.field_9229.method_43057() < 0.02F && !this.hasCandyGhost(player)) {
            ItemStack candyGhostStack = new ItemStack(ModItems.CANDY_GHOST, 1);
            if (!player.method_7270(candyGhostStack)) {
               ItemEntity candyGhostEntity = new ItemEntity(
                  player.method_37908(), player.method_23317(), player.method_23318(), player.method_23321(), candyGhostStack
               );
               player.method_37908().method_8649(candyGhostEntity);
            }

            player.method_7353(Text.method_43471("item.smfs.candy_ghost.obtained").method_27692(Formatting.field_1065), false);
         }
      }

      return super.method_7861(stack, world, user);
   }

   private boolean hasCandyGhost(PlayerEntity player) {
      for (int i = 0; i < player.method_31548().method_5439(); i++) {
         ItemStack stack = player.method_31548().method_5438(i);
         if (stack.method_7909() instanceof CandyGhostItem) {
            return true;
         }
      }

      return false;
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.ghost_candy.description.source"));
      tooltip.add(Text.method_43471("item.smfs.ghost_candy.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.ghost_candy.description.type"));
   }
}
