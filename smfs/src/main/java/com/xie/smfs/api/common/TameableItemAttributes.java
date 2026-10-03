package com.xie.smfs.api.common;

public class TameableItemAttributes {
   private final int maxSpiritBonus;
   private final int spiritResistanceBonus;
   private final int spiritDamageBonus;
   private final int sanityBonus;
   private final double revivalFactor;

   private TameableItemAttributes(TameableItemAttributes.Builder builder) {
      this.maxSpiritBonus = builder.maxSpiritBonus;
      this.spiritResistanceBonus = builder.spiritResistanceBonus;
      this.spiritDamageBonus = builder.spiritDamageBonus;
      this.sanityBonus = builder.sanityBonus;
      this.revivalFactor = builder.revivalFactor;
   }

   public int getMaxSpiritBonus() {
      return this.maxSpiritBonus;
   }

   public int getSpiritResistanceBonus() {
      return this.spiritResistanceBonus;
   }

   public int getSpiritDamageBonus() {
      return this.spiritDamageBonus;
   }

   public int getSanityBonus() {
      return this.sanityBonus;
   }

   public double getRevivalFactor() {
      return this.revivalFactor;
   }

   public static TameableItemAttributes.Builder builder() {
      return new TameableItemAttributes.Builder();
   }

   public static TameableItemAttributes empty() {
      return new TameableItemAttributes.Builder().build();
   }

   public static class Builder {
      private int maxSpiritBonus = 0;
      private int spiritResistanceBonus = 0;
      private int spiritDamageBonus = 0;
      private int sanityBonus = 0;
      private double revivalFactor = 0.0;

      public TameableItemAttributes.Builder maxSpiritBonus(int maxSpiritBonus) {
         this.maxSpiritBonus = maxSpiritBonus;
         return this;
      }

      public TameableItemAttributes.Builder spiritResistanceBonus(int spiritResistanceBonus) {
         this.spiritResistanceBonus = spiritResistanceBonus;
         return this;
      }

      public TameableItemAttributes.Builder spiritDamageBonus(int spiritDamageBonus) {
         this.spiritDamageBonus = spiritDamageBonus;
         return this;
      }

      public TameableItemAttributes.Builder sanityBonus(int sanityBonus) {
         this.sanityBonus = sanityBonus;
         return this;
      }

      public TameableItemAttributes.Builder revivalFactor(double revivalFactor) {
         this.revivalFactor = revivalFactor;
         return this;
      }

      public TameableItemAttributes build() {
         return new TameableItemAttributes(this);
      }
   }
}
