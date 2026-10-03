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

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.getStackInHand(hand);
      if (world.getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION) {
         return TypedActionResult.fail(itemStack);
      }

      boolean hasArrow = !user.getProjectileType(itemStack).isEmpty();
      if (!user.getAbilities().creativeMode && !hasArrow) {
         return TypedActionResult.fail(itemStack);
      }

      user.setCurrentHand(hand);
      return TypedActionResult.consume(itemStack);
   }

   public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
      if (user instanceof PlayerEntity player) {
         boolean creative = player.getAbilities().creativeMode;
         ItemStack projectileStack = player.getProjectileType(stack);
         if (!projectileStack.isEmpty() || creative) {
            int useTicks = this.getMaxUseTime(stack) - remainingUseTicks;
            float pullProgress = getPullProgress(useTicks);
            if (!(pullProgress < 0.1)) {
               PersistentProjectileEntity projectileEntity = this.createArrow(world, player, stack, projectileStack, creative);
               if (projectileEntity != null) {
                  projectileEntity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, pullProgress * 3.0F, 1.0F);
                  if (pullProgress == 1.0F) {
                     projectileEntity.setCritical(true);
                  }

                  if (world.getRegistryKey() != Smfs.GHOST_DREAM_DIMENSION) {
                     projectileEntity.setDamage(projectileEntity.getDamage() + this.getSpiritDamageBonus());
                  }

                  if (!creative) {
                     projectileStack.decrement(1);
                     if (projectileStack.isEmpty()) {
                        player.getInventory().removeOne(projectileStack);
                     }
                  }

                  world.playSound(
                     null,
                     player.getX(),
                     player.getY(),
                     player.getZ(),
                     SoundEvents.ENTITY_ARROW_SHOOT,
                     SoundCategory.PLAYERS,
                     1.0F,
                     1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + pullProgress * 0.5F
                  );
                  if (!world.isClient) {
                     world.spawnEntity(projectileEntity);
                  }

                  projectileEntity.setOwner(player);
               }
            }
         }
      }
   }

   private PersistentProjectileEntity createArrow(World world, PlayerEntity player, ItemStack bowStack, ItemStack arrowStack, boolean creative) {
      if (arrowStack.isEmpty() && creative) {
         arrowStack = new ItemStack(Items.ARROW);
      }

      final ItemStack finalArrowStack = arrowStack;
      return new PersistentProjectileEntity(EntityType.ARROW, player, world) {
         private boolean hitEntity = false;

         protected void onEntityHit(EntityHitResult entityHitResult) {
            super.onEntityHit(entityHitResult);
            this.hitEntity = true;
            if (!this.getWorld().isClient && entityHitResult.getEntity() instanceof LivingEntity target) {
               float damage = (float)this.getDamage();
               target.damage(this.getDamageSources().arrow(this, this.getOwner()), damage);
            }

            if (!this.isRemoved()) {
               this.discard();
            }
         }

         protected void onHit(LivingEntity target) {
            super.onHit(target);
         }

         protected void onCollision(HitResult hitResult) {
            super.onCollision(hitResult);
            Type type = hitResult.getType();
            if (type == Type.ENTITY) {
               this.getWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, hitResult.getPos(), Emitter.of(this, (BlockState)null));
            } else if (type == Type.BLOCK) {
               BlockHitResult blockHitResult = (BlockHitResult)hitResult;
               BlockPos blockPos = blockHitResult.getBlockPos();
               this.getWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, blockPos, Emitter.of(this, this.getWorld().getBlockState(blockPos)));
            }
         }

         protected void onBlockHit(BlockHitResult blockHitResult) {
            super.onBlockHit(blockHitResult);
            if (!this.hitEntity && this.getOwner() instanceof PlayerEntity owner && !this.isRemoved()) {
               float damage = (float)this.getDamage();
               owner.damage(ModDamageSources.ghost(owner.getWorld()), damage);
               world.playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
         }

         public ItemStack asItemStack() {
            return finalArrowStack.copy();
         }
      };
   }

   @Override
   public float getSpiritDamageBonus() {
      return 50.0F;
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.ghost_bow.description.source"));
      tooltip.add(Text.translatable("item.smfs.ghost_bow.description.desc"));
      tooltip.add(Text.translatable("item.smfs.ghost_bow.description.type"));
      tooltip.add(Text.translatable("item.smfs.ghost_bow.effect1.miss"));
      tooltip.add(Text.translatable("item.smfs.ghost_bow.effect2.miss"));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.translatable("item.smfs.spirit_weapon.damage_multiplier", new Object[]{this.getSpiritDamageMultiplier() * 100.0F}));
   }
}
