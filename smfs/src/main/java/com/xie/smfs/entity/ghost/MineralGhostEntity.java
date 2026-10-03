package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.registry.ModEffects;
import java.util.Objects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class MineralGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'C';

   public static Builder createLivingAttributes() {
      return LivingEntity.method_26827()
         .method_26868(EntityAttributes.field_23716, 90000.0)
         .method_26868(EntityAttributes.field_23719, 0.18)
         .method_26868(EntityAttributes.field_23721, 8.0);
   }

   public MineralGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1000, 100, 50, 0.1F);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23716)).method_6192(90000.0);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23719)).method_6192(0.18);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23721)).method_6192(8.0);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (RedGhostCandleItem.isHoldingCandle(player)) {
         return false;
      } else if (CoffinEffectManager.isPlayerInGoldCoffin(player)) {
         return false;
      } else if (player.method_6059(ModEffects.SPIRIT_IMMUNITY)) {
         return false;
      } else {
         return !this.isPlayerInRange(player) ? false : this.hasMineralsInInventory(player);
      }
   }

   private boolean isPlayerInRange(PlayerEntity player) {
      return this.method_5858(player) <= 1024.0;
   }

   private boolean hasMineralsInInventory(PlayerEntity player) {
      for (int i = 0; i < player.method_31548().method_5439(); i++) {
         ItemStack stack = player.method_31548().method_5438(i);
         if (!stack.method_7960() && this.isMineral(stack)) {
            return true;
         }
      }

      return false;
   }

   private boolean isMineral(ItemStack stack) {
      return stack.method_31574(Items.field_8713)
         || stack.method_31574(Items.field_8620)
         || stack.method_31574(Items.field_8695)
         || stack.method_31574(Items.field_8477)
         || stack.method_31574(Items.field_8687)
         || stack.method_31574(Items.field_8759)
         || stack.method_31574(Items.field_8725)
         || stack.method_31574(Items.field_8155)
         || stack.method_31574(Items.field_22020)
         || stack.method_31574(Items.field_27022)
         || stack.method_31574(Items.field_27063)
         || stack.method_31574(Items.field_33400)
         || stack.method_31574(Items.field_33402)
         || stack.method_31574(Items.field_33401)
         || stack.method_31574(Items.field_8476)
         || stack.method_31574(Items.field_8599)
         || stack.method_31574(Items.field_8775)
         || stack.method_31574(Items.field_8787)
         || stack.method_31574(Items.field_8837)
         || stack.method_31574(Items.field_8809)
         || stack.method_31574(Items.field_8604)
         || stack.method_31574(Items.field_8702)
         || stack.method_31574(Items.field_29212)
         || stack.method_31574(Items.field_29020)
         || stack.method_31574(Items.field_29019)
         || stack.method_31574(Items.field_29022)
         || stack.method_31574(Items.field_29216)
         || stack.method_31574(Items.field_29021)
         || stack.method_31574(Items.field_29023)
         || stack.method_31574(Items.field_27018)
         || stack.method_31574(Items.field_29211)
         || stack.method_31574(Items.field_22019);
   }
}
