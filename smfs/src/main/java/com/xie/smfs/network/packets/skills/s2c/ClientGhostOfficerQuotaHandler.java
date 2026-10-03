package com.xie.smfs.network.packets.skills.s2c;

public class ClientGhostOfficerQuotaHandler {
   private static int remainingQuota = 1;
   private static int maxQuota = 1;

   public static void updateQuota(int remaining, int max) {
      remainingQuota = remaining;
      maxQuota = max;
   }

   public static int getRemainingQuota() {
      return remainingQuota;
   }

   public static int getMaxQuota() {
      return maxQuota;
   }
}
