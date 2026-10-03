package com.xie.smfs.item;

import com.xie.smfs.Smfs;
import com.xie.smfs.damage.ModDamageSources;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Item.Settings;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;
import org.jetbrains.annotations.Nullable;

public class GhostBowItem extends BowItem implements SpiritWeapon {
   public GhostBowItem(Settings settings) {
      super(settings);
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.method_5998(hand);
      if (world.method_27983() == Smfs.GHOST_DREAM_DIMENSION) {
         return TypedActionResult.method_22431(itemStack);
      }

      boolean hasArrow = !user.method_18808(itemStack).method_7960();
      if (!user.method_31549().field_7477 && !hasArrow) {
         return TypedActionResult.method_22431(itemStack);
      }

      user.method_6019(hand);
      return TypedActionResult.method_22428(itemStack);
   }

   public void method_7840(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
      if (user instanceof PlayerEntity player) {
         boolean creative = player.method_31549().field_7477;
         ItemStack projectileStack = player.method_18808(stack);
         if (!projectileStack.method_7960() || creative) {
            int useTicks = this.method_7881(stack) - remainingUseTicks;
            float pullProgress = method_7722(useTicks);
            if (!(pullProgress < 0.1)) {
               PersistentProjectileEntity projectileEntity = this.createArrow(world, player, stack, projectileStack, creative);
               if (projectileEntity != null) {
                  projectileEntity.method_24919(player, player.method_36455(), player.method_36454(), 0.0F, pullProgress * 3.0F, 1.0F);
                  if (pullProgress == 1.0F) {
                     projectileEntity.method_7439(true);
                  }

                  if (world.method_27983() != Smfs.GHOST_DREAM_DIMENSION) {
                     projectileEntity.method_7438(projectileEntity.method_7448() + this.getSpiritDamageBonus());
                  }

                  if (!creative) {
                     projectileStack.method_7934(1);
                     if (projectileStack.method_7960()) {
                        player.method_31548().method_7378(projectileStack);
                     }
                  }

                  world.method_43128(
                     null,
                     player.method_23317(),
                     player.method_23318(),
                     player.method_23321(),
                     SoundEvents.field_14600,
                     SoundCategory.field_15248,
                     1.0F,
                     1.0F / (world.method_8409().method_43057() * 0.4F + 1.2F) + pullProgress * 0.5F
                  );
                  if (!world.field_9236) {
                     world.method_8649(projectileEntity);
                  }

                  projectileEntity.method_7432(player);
               }
            }
         }
      }
   }

   private PersistentProjectileEntity createArrow(World world, PlayerEntity player, ItemStack bowStack, ItemStack arrowStack, boolean creative) {
      if (arrowStack.method_7960() && creative) {
         arrowStack = new ItemStack(Items.field_8107);
      }

      final ItemStack finalArrowStack = arrowStack;
      return new PersistentProjectileEntity(EntityType.field_6122, player, world) {
         private boolean hitEntity = false;

         protected void method_7454(EntityHitResult entityHitResult) {
            super.method_7454(entityHitResult);
            this.hitEntity = true;
            if (!this.method_37908().field_9236 && entityHitResult.method_17782() instanceof LivingEntity target) {
               float damage = (float)this.method_7448();
               target.method_5643(this.method_48923().method_48803(this, this.method_24921()), damage);
            }

            if (!this.method_31481()) {
               this.method_31472();
            }
         }

         protected void method_7450(LivingEntity target) {
            super.method_7450(target);
         }

         protected void method_7488(HitResult hitResult) {
            super.method_7488(hitResult);
            Type type = hitResult.method_17783();
            if (type == Type.field_1331) {
               this.method_37908().method_32888(GameEvent.field_28162, hitResult.method_17784(), Emitter.method_43286(this, (BlockState)null));
            } else if (type == Type.field_1332) {
               BlockHitResult blockHitResult = (BlockHitResult)hitResult;
               BlockPos blockPos = blockHitResult.method_17777();
               this.method_37908().method_43276(GameEvent.field_28162, blockPos, Emitter.method_43286(this, this.method_37908().method_8320(blockPos)));
            }
         }

         protected void method_24920(BlockHitResult blockHitResult) {
            super.method_24920(blockHitResult);
            if (!this.hitEntity && this.method_24921() instanceof PlayerEntity owner && !this.method_31481()) {
               float damage = (float)this.method_7448();
               owner.method_5643(ModDamageSources.ghost(owner.method_37908()), damage);
               world.method_43128(
                  null, owner.method_23317(), owner.method_23318(), owner.method_23321(), SoundEvents.field_15115, SoundCategory.field_15248, 1.0F, 1.0F
               );
            }
         }

         public ItemStack method_7445() {
            return finalArrowStack.method_7972();
         }
      };
   }

   @Override
   public float getSpiritDamageBonus() {
      return 50.0F;
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.ghost_bow.description.source"));
      tooltip.add(Text.method_43471("item.smfs.ghost_bow.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.ghost_bow.description.type"));
      tooltip.add(Text.method_43471("item.smfs.ghost_bow.effect1.miss"));
      tooltip.add(Text.method_43471("item.smfs.ghost_bow.effect2.miss"));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_multiplier", new Object[]{this.getSpiritDamageMultiplier() * 100.0F}));
   }
}
