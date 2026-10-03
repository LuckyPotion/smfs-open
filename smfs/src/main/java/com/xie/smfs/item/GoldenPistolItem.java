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
   private static final SoundEvent GOLDEN_PISTOL_SHOT = SoundEvent.method_47908(new Identifier("smfs", "weapon.golden_pistol.shot"));
   private static final int COOLDOWN_TICKS = 40;
   private static final float RECOIL_STRENGTH = 5.0F;

   public GoldenPistolItem(Settings settings) {
      super(settings);
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.method_5998(hand);
      if (user.method_7357().method_7904(this)) {
         return TypedActionResult.method_22431(itemStack);
      }

      boolean hasBullet = user.method_31548().method_7379(new ItemStack(ModItems.GOLDEN_BULLET));
      if (!user.method_31549().field_7477 && !hasBullet) {
         return TypedActionResult.method_22431(itemStack);
      }

      if (!world.field_9236) {
         this.shootBullet(world, user);
         user.method_7357().method_7906(this, 40);
      }

      this.applyRecoil(user);
      world.method_43128(null, user.method_23317(), user.method_23318(), user.method_23321(), GOLDEN_PISTOL_SHOT, SoundCategory.field_15248, 1.0F, 1.0F);
      return TypedActionResult.method_22427(itemStack);
   }

   private void shootBullet(World world, PlayerEntity player) {
      if (!player.method_31549().field_7477) {
         for (int i = 0; i < player.method_31548().method_5439(); i++) {
            ItemStack stack = player.method_31548().method_5438(i);
            if (stack.method_7909() == ModItems.GOLDEN_BULLET) {
               stack.method_7934(1);
               break;
            }
         }
      }

      GoldenBulletEntity bullet = new GoldenBulletEntity(ModEntities.GOLDEN_BULLET, player, world);
      bullet.method_24919(player, player.method_36455(), player.method_36454(), 0.0F, 15.0F, 1.0F);
      bullet.method_7438(5.0);
      world.method_8649(bullet);
   }

   private void applyRecoil(PlayerEntity player) {
      float pitchRecoil = -5.0F;
      player.method_36457(player.method_36455() + pitchRecoil);
      if (player.method_36455() < -90.0F) {
         player.method_36457(-90.0F);
      }

      player.method_5762(0.0, 0.1, 0.0);
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.golden_pistol.description.source"));
      tooltip.add(Text.method_43471("item.smfs.golden_pistol.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.golden_pistol.description.type"));
      tooltip.add(Text.method_43469("item.smfs.golden_pistol.damage", new Object[]{90.0F}));
   }
}
