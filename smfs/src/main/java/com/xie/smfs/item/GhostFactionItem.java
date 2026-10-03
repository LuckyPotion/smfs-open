package com.xie.smfs.item;

import com.xie.smfs.entity.GhostEntity;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GhostFactionItem extends Item {
   public GhostFactionItem(Settings settings) {
      super(settings);
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.method_5998(hand);
      if (!world.field_9236) {
         LivingEntity firstGhost = null;

         for (GhostEntity entity : world.method_8390(GhostEntity.class, user.method_5829().method_1014(5.0), e -> true)) {
            if (firstGhost != null) {
               GhostEntity ghost1 = (GhostEntity)firstGhost;
               GhostEntity ghost2 = entity;
               ghost1.setGhostFactionTarget(ghost2);
               ghost2.setGhostFactionTarget(ghost1);
               world.method_43128(
                  null, user.method_23317(), user.method_23318(), user.method_23321(), SoundEvents.field_19149, SoundCategory.field_15248, 1.0F, 1.0F
               );
               ServerWorld serverWorld = (ServerWorld)world;
               serverWorld.method_14199(
                  ParticleTypes.field_11211, ghost1.method_23317(), ghost1.method_23318() + 1.0, ghost1.method_23321(), 10, 0.5, 0.5, 0.5, 0.1
               );
               serverWorld.method_14199(
                  ParticleTypes.field_11211, ghost2.method_23317(), ghost2.method_23318() + 1.0, ghost2.method_23321(), 10, 0.5, 0.5, 0.5, 0.1
               );
               if (!user.method_7337()) {
                  stack.method_7934(1);
               }

               return TypedActionResult.method_22427(stack);
            }

            firstGhost = entity;
         }
      }

      return TypedActionResult.method_22430(stack);
   }

   public UseAction method_7853(ItemStack stack) {
      return UseAction.field_8953;
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.ghost_faction.description.source"));
      tooltip.add(Text.method_43471("item.smfs.ghost_faction.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.ghost_faction.description.type"));
   }
}
