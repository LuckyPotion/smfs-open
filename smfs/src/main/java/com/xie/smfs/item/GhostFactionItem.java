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

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (!world.isClient) {
         LivingEntity firstGhost = null;

         for (GhostEntity entity : world.getEntitiesByClass(GhostEntity.class, user.getBoundingBox().expand(5.0), e -> true)) {
            if (firstGhost != null) {
               GhostEntity ghost1 = (GhostEntity)firstGhost;
               GhostEntity ghost2 = entity;
               ghost1.setGhostFactionTarget(ghost2);
               ghost2.setGhostFactionTarget(ghost1);
               world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 1.0F, 1.0F);
               ServerWorld serverWorld = (ServerWorld)world;
               serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, ghost1.getX(), ghost1.getY() + 1.0, ghost1.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
               serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, ghost2.getX(), ghost2.getY() + 1.0, ghost2.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
               if (!user.isCreative()) {
                  stack.decrement(1);
               }

               return TypedActionResult.success(stack);
            }

            firstGhost = entity;
         }
      }

      return TypedActionResult.pass(stack);
   }

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.BOW;
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.ghost_faction.description.source"));
      tooltip.add(Text.translatable("item.smfs.ghost_faction.description.desc"));
      tooltip.add(Text.translatable("item.smfs.ghost_faction.description.type"));
   }
}
