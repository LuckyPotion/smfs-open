package com.xie.smfs.effect;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class WishCurseEffect extends StatusEffect implements ICurseEffect {
   public WishCurseEffect() {
      super(StatusEffectCategory.field_18272, 9109504);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (entity.field_6012 % 60 == 0) {
         this.spawnLightningAtTarget(entity);
         if (!entity.method_37908().method_8608()) {
            ServerWorld serverWorld = (ServerWorld)entity.method_37908();
            serverWorld.method_43128(
               null, entity.method_23317(), entity.method_23318(), entity.method_23321(), SoundEvents.field_14865, SoundCategory.field_15252, 1.0F, 1.0F
            );
         }

         if (entity instanceof PlayerEntity player) {
            player.method_7353(Text.method_43471("effect.smfs.wish_curse.lightning_hit").method_27692(Formatting.field_1061), true);
         }
      }

      if (entity.field_6012 % 20 == 0) {
         entity.method_6092(new StatusEffectInstance(StatusEffects.field_5911, 40, amplifier));
         entity.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 40, amplifier));
         entity.method_6092(new StatusEffectInstance(StatusEffects.field_5901, 40, amplifier));
      }

      if (!entity.method_37908().method_8608()) {
         ServerWorld serverWorld = (ServerWorld)entity.method_37908();
         Vec3d pos = entity.method_19538();

         for (int i = 0; i < 3; i++) {
            double offsetX = (entity.method_6051().method_43058() - 0.5) * 2.0;
            double offsetY = entity.method_6051().method_43058() * 2.0;
            double offsetZ = (entity.method_6051().method_43058() - 0.5) * 2.0;
            serverWorld.method_14199(
               ParticleTypes.field_11239, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 2, 0.1, 0.1, 0.1, 0.02
            );
         }
      }
   }

   private void spawnLightningAtTarget(LivingEntity target) {
      if (!target.method_37908().method_8608()) {
         ServerWorld serverWorld = (ServerWorld)target.method_37908();
         LightningEntity lightning = new LightningEntity(EntityType.field_6112, serverWorld);
         lightning.method_5814(target.method_23317(), target.method_23318() + 1.0, target.method_23321());
         lightning.method_29498(true);
         serverWorld.method_8649(lightning);
      }
   }

   public boolean method_5573() {
      return false;
   }
}
