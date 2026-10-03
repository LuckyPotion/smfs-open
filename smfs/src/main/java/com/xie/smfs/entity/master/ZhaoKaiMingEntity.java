package com.xie.smfs.entity.master;

import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import java.util.Set;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;

public class ZhaoKaiMingEntity extends GhostMasterEntity {
   protected int currentSkillType = 2;
   protected static final int SKILL_TYPE_SEND_TO_HELL = 0;
   protected static final int SKILL_TYPE_SELF_TELEPORT = 1;
   protected static final int SKILL_TYPE_CURSE = 2;
   protected boolean skillUsed = false;
   protected boolean isSkillActive = false;
   protected int skillCooldown = 0;

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient()) {
         if (this.skillCooldown > 0) {
            this.skillCooldown--;
         }

         if (!this.skillUsed && this.skillCooldown <= 0 && !this.isSkillActive) {
            float healthPercentage = this.getHealth() / this.getMaxHealth();
            if (healthPercentage < 0.5F) {
               this.startSkill();
            }
         }

         if (this.isSkillActive) {
            this.handleSkill();
         }

         if (this.getTarget() != null) {
         }
      }
   }

   public ZhaoKaiMingEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.setGhostDomainLevel(0);
      this.setGhostDomainRadius(24.0);
      this.setSpiritualStrength(1200);
      this.setSpiritualDamage(30);
      this.setSpiritualResistance(25);
      this.setRecoveryFactor(0.05F);
      this.shouldFleeFromGhosts = false;
      this.shouldAttackPlayers = false;
      this.setCustomName(Text.literal("§6赵开明"));
      this.setCustomNameVisible(true);
      this.faction = PlayerFaction.HEADQUARTERS;
   }

   public static Builder createZhaoKaiMingAttributes() {
      return MobEntity.createMobAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 600.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0);
   }

   @Override
   protected void initGoals() {
      super.initGoals();
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   protected String getGhostMasterDisplayName() {
      return "赵开明";
   }

   @Override
   protected void startSkill() {
      if (this.skillUsed) {
         this.endSkill();
      } else {
         float healthPercentage = this.getHealth() / this.getMaxHealth();
         if (healthPercentage < 0.5F) {
            this.currentSkillType = this.random.nextInt(3);
            super.startSkill();
            if (!this.getWorld().isClient()) {
               if (this.currentSkillType == 0) {
                  LivingEntity target = this.getTarget();
                  if (target != null) {
                     this.getWorld().getPlayers().forEach(player -> player.sendMessage(Text.literal("§6[赵开明] §c我愿承受许愿的代价，让面前之人彻底消失！"), false));
                     this.spawnLightningAtTarget(target);
                     this.teleportTargetToNether(target);
                  }
               } else if (this.currentSkillType == 1) {
                  this.getWorld().getPlayers().forEach(player -> player.sendMessage(Text.literal("§6[赵开明] §c我愿承受许愿的代价，让我离开这里！"), false));
                  this.clearAllStatusEffects();
                  this.spawnLightningAtTarget(this);
                  this.teleportSelfToRandomLocation();
               } else {
                  LivingEntity target = this.getTarget();
                  if (target != null) {
                     this.getWorld().getPlayers().forEach(player -> player.sendMessage(Text.literal("§6[赵开明] §c我愿承受许愿的代价，让面前之人气运全无！"), false));
                     this.applyWishCurseToTarget(target);
                  }
               }

               this.skillUsed = true;
            }
         } else {
            this.endSkill();
         }
      }
   }

   @Override
   protected void handleSkill() {
      if (this.currentSkillType == 0) {
         LivingEntity target = this.getTarget();
         if (target == null || !target.isAlive()) {
            this.endSkill();
            return;
         }
      } else if (this.currentSkillType == 2) {
         LivingEntity target = this.getTarget();
         if (target == null || !target.isAlive()) {
            this.endSkill();
            return;
         }
      }

      this.endSkill();
   }

   private void clearAllStatusEffects() {
      this.clearStatusEffects();
      if (this.getWorld() instanceof ServerWorld serverWorld) {
         serverWorld.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.PLAYERS, 1.0F, 1.0F);
      }
   }

   private void applyWishCurseToTarget(LivingEntity target) {
      if (target != null && !target.getWorld().isClient()) {
         target.addStatusEffect(new StatusEffectInstance(ModEffects.WISH_CURSE, 1200, 0, false, false, false));
         if (target instanceof PlayerEntity player) {
            player.sendMessage(Text.translatable("entity.smfs.zhao_kai_ming.wish_curse_applied").formatted(Formatting.DARK_RED), true);
         }
      }
   }

   private void teleportSelfToRandomLocation() {
      if (this.getWorld() instanceof ServerWorld serverWorld) {
         double angle = this.random.nextDouble() * 2.0 * Math.PI;
         double distance = 100.0;
         double newX = this.getX() + Math.cos(angle) * distance;
         double newZ = this.getZ() + Math.sin(angle) * distance;
         BlockPos safePos = this.findSafeGroundPosition(serverWorld, new BlockPos((int)newX, (int)this.getY(), (int)newZ));
         if (safePos != null) {
            this.teleport(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
            this.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);

            for (int i = 0; i < 20; i++) {
               serverWorld.spawnParticles(
                  ParticleTypes.PORTAL,
                  this.getX() + (this.random.nextDouble() - 0.5) * 3.0,
                  this.getY() + this.random.nextDouble() * 3.0,
                  this.getZ() + (this.random.nextDouble() - 0.5) * 3.0,
                  5,
                  0.1,
                  0.1,
                  0.1,
                  0.02
               );
            }
         }
      }
   }

   private BlockPos findSafeGroundPosition(ServerWorld world, BlockPos centerPos) {
      for (int radius = 0; radius <= 10; radius++) {
         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               if (Math.abs(x) == radius || Math.abs(z) == radius) {
                  BlockPos testPos = centerPos.add(x, 0, z);

                  for (int y = world.getTopY(); y >= world.getBottomY(); y--) {
                     BlockPos groundPos = new BlockPos(testPos.getX(), y, testPos.getZ());
                     BlockPos abovePos = groundPos.up();
                     if (world.getBlockState(groundPos).blocksMovement()
                        && !world.getBlockState(abovePos).blocksMovement()
                        && !world.getBlockState(abovePos.up()).blocksMovement()) {
                        return abovePos;
                     }
                  }
               }
            }
         }
      }

      for (int y = world.getTopY(); y >= world.getBottomY(); y--) {
         BlockPos groundPos = new BlockPos(centerPos.getX(), y, centerPos.getZ());
         if (world.getBlockState(groundPos).blocksMovement()) {
            return groundPos.up();
         }
      }

      return centerPos;
   }

   private void spawnLightningAtTarget(LivingEntity target) {
      if (this.getWorld() instanceof ServerWorld serverWorld) {
         BlockPos targetPos = target.getBlockPos();
         serverWorld.spawnEntity(new LightningEntity(EntityType.LIGHTNING_BOLT, serverWorld));
         serverWorld.playSound(null, targetPos, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 1.0F, 1.0F);

         for (int i = 0; i < 10; i++) {
            serverWorld.spawnParticles(
               ParticleTypes.ELECTRIC_SPARK,
               target.getX() + (this.random.nextDouble() - 0.5) * 3.0,
               target.getY() + this.random.nextDouble() * 3.0,
               target.getZ() + (this.random.nextDouble() - 0.5) * 3.0,
               5,
               0.1,
               0.1,
               0.1,
               0.02
            );
         }
      }
   }

   private void teleportTargetToNether(LivingEntity target) {
      if (this.getWorld() instanceof ServerWorld currentWorld && target instanceof PlayerEntity player) {
         ServerWorld netherWorld = currentWorld.getServer().getWorld(World.NETHER);
         if (netherWorld != null) {
            BlockPos netherPos = this.findSafeNetherPosition(netherWorld, player);
            if (netherPos != null) {
               if (!player.getWorld().isClient()) {
                  player.teleport(netherWorld, netherPos.getX() + 0.5, netherPos.getY(), netherPos.getZ() + 0.5, Set.of(), player.getYaw(), player.getPitch());
               }

               player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);

               for (int i = 0; i < 15; i++) {
                  netherWorld.spawnParticles(
                     ParticleTypes.PORTAL,
                     player.getX() + (this.random.nextDouble() - 0.5) * 2.0,
                     player.getY() + this.random.nextDouble() * 2.0,
                     player.getZ() + (this.random.nextDouble() - 0.5) * 2.0,
                     3,
                     0.1,
                     0.1,
                     0.1,
                     0.02
                  );
               }

               player.sendMessage(Text.literal("§c你被未知的灵异送入了地狱！"), true);
            }
         }
      }
   }

   private BlockPos findSafeNetherPosition(ServerWorld netherWorld, PlayerEntity player) {
      BlockPos playerPos = player.getBlockPos();
      int netherX = playerPos.getX() / 8;
      int netherZ = playerPos.getZ() / 8;

      for (int y = 30; y <= 100; y += 5) {
         BlockPos testPos = new BlockPos(netherX, y, netherZ);
         if (this.isPositionSafeInNether(netherWorld, testPos)) {
            return testPos;
         }
      }

      return netherWorld.getSpawnPos();
   }

   private boolean isPositionSafeInNether(ServerWorld netherWorld, BlockPos pos) {
      BlockPos standingPos = pos;
      BlockPos headPos = pos.up();
      BlockPos feetPos = pos.down();
      return netherWorld.getBlockState(feetPos).getBlock() == Blocks.LAVA
         ? false
         : !netherWorld.getBlockState(standingPos).blocksMovement() && !netherWorld.getBlockState(headPos).blocksMovement();
   }

   @Override
   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         ItemStack ghostMoney3Input = new ItemStack(ModItems.GHOST_MONEY_3, 3);
         ItemStack goldIngotOutput2 = new ItemStack(Items.GOLD_INGOT, 23);
         this.tradeOffers.add(new TradeOffer(ghostMoney3Input, goldIngotOutput2, 12, 5, 0.05F));
         ItemStack rustyFruitKnifeInput = new ItemStack(ModItems.RUSTY_FRUIT_KNIFE, 1);
         ItemStack goldNuggetOutput = new ItemStack(Items.GOLD_BLOCK, 3);
         this.tradeOffers.add(new TradeOffer(rustyFruitKnifeInput, goldNuggetOutput, 8, 3, 0.05F));
         this.tradeOffersInitialized = true;
      }
   }
}
