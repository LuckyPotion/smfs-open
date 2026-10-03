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
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608()) {
         if (this.skillCooldown > 0) {
            this.skillCooldown--;
         }

         if (!this.skillUsed && this.skillCooldown <= 0 && !this.isSkillActive) {
            float healthPercentage = this.method_6032() / this.method_6063();
            if (healthPercentage < 0.5F) {
               this.startSkill();
            }
         }

         if (this.isSkillActive) {
            this.handleSkill();
         }

         if (this.method_5968() != null) {
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
      this.method_5665(Text.method_43470("§6赵开明"));
      this.method_5880(true);
      this.faction = PlayerFaction.HEADQUARTERS;
   }

   public static Builder createZhaoKaiMingAttributes() {
      return MobEntity.method_26828()
         .method_26868(EntityAttributes.field_23716, 600.0)
         .method_26868(EntityAttributes.field_23719, 0.35)
         .method_26868(EntityAttributes.field_23721, 15.0)
         .method_26868(EntityAttributes.field_23717, 20.0);
   }

   @Override
   protected void method_5959() {
      super.method_5959();
   }

   @Override
   public boolean method_5810() {
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
         float healthPercentage = this.method_6032() / this.method_6063();
         if (healthPercentage < 0.5F) {
            this.currentSkillType = this.field_5974.method_43048(3);
            super.startSkill();
            if (!this.method_37908().method_8608()) {
               if (this.currentSkillType == 0) {
                  LivingEntity target = this.method_5968();
                  if (target != null) {
                     this.method_37908().method_18456().forEach(player -> player.method_7353(Text.method_43470("§6[赵开明] §c我愿承受许愿的代价，让面前之人彻底消失！"), false));
                     this.spawnLightningAtTarget(target);
                     this.teleportTargetToNether(target);
                  }
               } else if (this.currentSkillType == 1) {
                  this.method_37908().method_18456().forEach(player -> player.method_7353(Text.method_43470("§6[赵开明] §c我愿承受许愿的代价，让我离开这里！"), false));
                  this.clearAllStatusEffects();
                  this.spawnLightningAtTarget(this);
                  this.teleportSelfToRandomLocation();
               } else {
                  LivingEntity target = this.method_5968();
                  if (target != null) {
                     this.method_37908().method_18456().forEach(player -> player.method_7353(Text.method_43470("§6[赵开明] §c我愿承受许愿的代价，让面前之人气运全无！"), false));
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
         LivingEntity target = this.method_5968();
         if (target == null || !target.method_5805()) {
            this.endSkill();
            return;
         }
      } else if (this.currentSkillType == 2) {
         LivingEntity target = this.method_5968();
         if (target == null || !target.method_5805()) {
            this.endSkill();
            return;
         }
      }

      this.endSkill();
   }

   private void clearAllStatusEffects() {
      this.method_6012();
      if (this.method_37908() instanceof ServerWorld serverWorld) {
         serverWorld.method_8396(null, this.method_24515(), SoundEvents.field_14941, SoundCategory.field_15248, 1.0F, 1.0F);
      }
   }

   private void applyWishCurseToTarget(LivingEntity target) {
      if (target != null && !target.method_37908().method_8608()) {
         target.method_6092(new StatusEffectInstance(ModEffects.WISH_CURSE, 1200, 0, false, false, false));
         if (target instanceof PlayerEntity player) {
            player.method_7353(Text.method_43471("entity.smfs.zhao_kai_ming.wish_curse_applied").method_27692(Formatting.field_1079), true);
         }
      }
   }

   private void teleportSelfToRandomLocation() {
      if (this.method_37908() instanceof ServerWorld serverWorld) {
         double angle = this.field_5974.method_43058() * 2.0 * Math.PI;
         double distance = 100.0;
         double newX = this.method_23317() + Math.cos(angle) * distance;
         double newZ = this.method_23321() + Math.sin(angle) * distance;
         BlockPos safePos = this.findSafeGroundPosition(serverWorld, new BlockPos((int)newX, (int)this.method_23318(), (int)newZ));
         if (safePos != null) {
            this.method_20620(safePos.method_10263() + 0.5, safePos.method_10264(), safePos.method_10260() + 0.5);
            this.method_5783(SoundEvents.field_14879, 1.0F, 1.0F);

            for (int i = 0; i < 20; i++) {
               serverWorld.method_14199(
                  ParticleTypes.field_11214,
                  this.method_23317() + (this.field_5974.method_43058() - 0.5) * 3.0,
                  this.method_23318() + this.field_5974.method_43058() * 3.0,
                  this.method_23321() + (this.field_5974.method_43058() - 0.5) * 3.0,
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
                  BlockPos testPos = centerPos.method_10069(x, 0, z);

                  for (int y = world.method_31600(); y >= world.method_31607(); y--) {
                     BlockPos groundPos = new BlockPos(testPos.method_10263(), y, testPos.method_10260());
                     BlockPos abovePos = groundPos.method_10084();
                     if (world.method_8320(groundPos).method_51366()
                        && !world.method_8320(abovePos).method_51366()
                        && !world.method_8320(abovePos.method_10084()).method_51366()) {
                        return abovePos;
                     }
                  }
               }
            }
         }
      }

      for (int y = world.method_31600(); y >= world.method_31607(); y--) {
         BlockPos groundPos = new BlockPos(centerPos.method_10263(), y, centerPos.method_10260());
         if (world.method_8320(groundPos).method_51366()) {
            return groundPos.method_10084();
         }
      }

      return centerPos;
   }

   private void spawnLightningAtTarget(LivingEntity target) {
      if (this.method_37908() instanceof ServerWorld serverWorld) {
         BlockPos targetPos = target.method_24515();
         serverWorld.method_8649(new LightningEntity(EntityType.field_6112, serverWorld));
         serverWorld.method_8396(null, targetPos, SoundEvents.field_14865, SoundCategory.field_15252, 1.0F, 1.0F);

         for (int i = 0; i < 10; i++) {
            serverWorld.method_14199(
               ParticleTypes.field_29644,
               target.method_23317() + (this.field_5974.method_43058() - 0.5) * 3.0,
               target.method_23318() + this.field_5974.method_43058() * 3.0,
               target.method_23321() + (this.field_5974.method_43058() - 0.5) * 3.0,
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
      if (this.method_37908() instanceof ServerWorld currentWorld && target instanceof PlayerEntity player) {
         ServerWorld netherWorld = currentWorld.method_8503().method_3847(World.field_25180);
         if (netherWorld != null) {
            BlockPos netherPos = this.findSafeNetherPosition(netherWorld, player);
            if (netherPos != null) {
               if (!player.method_37908().method_8608()) {
                  player.method_48105(
                     netherWorld,
                     netherPos.method_10263() + 0.5,
                     netherPos.method_10264(),
                     netherPos.method_10260() + 0.5,
                     Set.of(),
                     player.method_36454(),
                     player.method_36455()
                  );
               }

               player.method_5783(SoundEvents.field_14879, 1.0F, 1.0F);

               for (int i = 0; i < 15; i++) {
                  netherWorld.method_14199(
                     ParticleTypes.field_11214,
                     player.method_23317() + (this.field_5974.method_43058() - 0.5) * 2.0,
                     player.method_23318() + this.field_5974.method_43058() * 2.0,
                     player.method_23321() + (this.field_5974.method_43058() - 0.5) * 2.0,
                     3,
                     0.1,
                     0.1,
                     0.1,
                     0.02
                  );
               }

               player.method_7353(Text.method_43470("§c你被未知的灵异送入了地狱！"), true);
            }
         }
      }
   }

   private BlockPos findSafeNetherPosition(ServerWorld netherWorld, PlayerEntity player) {
      BlockPos playerPos = player.method_24515();
      int netherX = playerPos.method_10263() / 8;
      int netherZ = playerPos.method_10260() / 8;

      for (int y = 30; y <= 100; y += 5) {
         BlockPos testPos = new BlockPos(netherX, y, netherZ);
         if (this.isPositionSafeInNether(netherWorld, testPos)) {
            return testPos;
         }
      }

      return netherWorld.method_43126();
   }

   private boolean isPositionSafeInNether(ServerWorld netherWorld, BlockPos pos) {
      BlockPos standingPos = pos;
      BlockPos headPos = pos.method_10084();
      BlockPos feetPos = pos.method_10074();
      return netherWorld.method_8320(feetPos).method_26204() == Blocks.field_10164
         ? false
         : !netherWorld.method_8320(standingPos).method_51366() && !netherWorld.method_8320(headPos).method_51366();
   }

   @Override
   protected void initTradeOffers() {
      if (!this.tradeOffersInitialized) {
         this.tradeOffers = new TradeOfferList();
         ItemStack ghostMoney3Input = new ItemStack(ModItems.GHOST_MONEY_3, 3);
         ItemStack goldIngotOutput2 = new ItemStack(Items.field_8695, 23);
         this.tradeOffers.add(new TradeOffer(ghostMoney3Input, goldIngotOutput2, 12, 5, 0.05F));
         ItemStack rustyFruitKnifeInput = new ItemStack(ModItems.RUSTY_FRUIT_KNIFE, 1);
         ItemStack goldNuggetOutput = new ItemStack(Items.field_8494, 3);
         this.tradeOffers.add(new TradeOffer(rustyFruitKnifeInput, goldNuggetOutput, 8, 3, 0.05F));
         this.tradeOffersInitialized = true;
      }
   }
}
