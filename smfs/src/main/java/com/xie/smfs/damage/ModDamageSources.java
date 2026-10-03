package com.xie.smfs.damage;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ModDamageSources {
   public static final RegistryKey<DamageType> GHOST = RegistryKey.method_29179(RegistryKeys.field_42534, new Identifier("smfs", "ghost"));
   public static final RegistryKey<DamageType> GHOST_PORCELAIN_CURSE = RegistryKey.method_29179(
      RegistryKeys.field_42534, new Identifier("smfs", "ghost_porcelain_curse")
   );
   public static final RegistryKey<DamageType> SANITY_ZERO = RegistryKey.method_29179(RegistryKeys.field_42534, new Identifier("smfs", "sanity_zero"));
   public static final RegistryKey<DamageType> GHOST_SHROUD = RegistryKey.method_29179(RegistryKeys.field_42534, new Identifier("smfs", "ghost_shroud"));

   public static DamageSource of(World world, RegistryKey<DamageType> key) {
      return world.method_48963().method_48795(key);
   }

   public static DamageSource ghost(World world) {
      return of(world, GHOST);
   }

   public static DamageSource ghostPorcelainCurse(World world) {
      return of(world, GHOST_PORCELAIN_CURSE);
   }

   public static DamageSource sanityZero(World world) {
      return of(world, SANITY_ZERO);
   }

   public static DamageSource ghostShroud(World world) {
      return of(world, GHOST_SHROUD);
   }
}
