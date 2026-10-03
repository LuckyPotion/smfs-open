package com.xie.smfs.item;

import com.xie.smfs.entity.other.GoldenBulletEntity;
import com.xie.smfs.registry.ModEntities;
import com.xie.smfs.registry.ModItems;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GoldenPistolItem extends Item {
   private static final SoundEvent GOLDEN_PISTOL_SHOT = SoundEvent.of(new Identifier("smfs", "weapon.golden_pistol.shot"));
   private static final int COOLDOWN_TICKS = 40;
   private static final float RECOIL_STRENGTH = 5.0F;

   public GoldenPistolItem(Settings settings) {
      super(settings);
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.getStackInHand(hand);
      if (user.getItemCooldownManager().isCoolingDown(this)) {
         return TypedActionResult.fail(itemStack);
      }

      boolean hasBullet = user.getInventory().contains(new ItemStack(ModItems.GOLDEN_BULLET));
      if (!user.getAbilities().creativeMode && !hasBullet) {
         return TypedActionResult.fail(itemStack);
      }

      if (!world.isClient) {
         this.shootBullet(world, user);
         user.getItemCooldownManager().set(this, 40);
      }

      this.applyRecoil(user);
      world.playSound(null, user.getX(), user.getY(), user.getZ(), GOLDEN_PISTOL_SHOT, SoundCategory.PLAYERS, 1.0F, 1.0F);
      return TypedActionResult.success(itemStack);
   }

   private void shootBullet(World world, PlayerEntity player) {
      if (!player.getAbilities().creativeMode) {
         for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == ModItems.GOLDEN_BULLET) {
               stack.decrement(1);
               break;
            }
         }
      }

      GoldenBulletEntity bullet = new GoldenBulletEntity(ModEntities.GOLDEN_BULLET, player, world);
      bullet.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 15.0F, 1.0F);
      bullet.setDamage(5.0);
      world.spawnEntity(bullet);
   }

   private void applyRecoil(PlayerEntity player) {
      float pitchRecoil = -5.0F;
      player.setPitch(player.getPitch() + pitchRecoil);
      if (player.getPitch() < -90.0F) {
         player.setPitch(-90.0F);
      }

      player.addVelocity(0.0, 0.1, 0.0);
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.golden_pistol.description.source"));
      tooltip.add(Text.translatable("item.smfs.golden_pistol.description.desc"));
      tooltip.add(Text.translatable("item.smfs.golden_pistol.description.type"));
      tooltip.add(Text.translatable("item.smfs.golden_pistol.damage", new Object[]{90.0F}));
   }
}
