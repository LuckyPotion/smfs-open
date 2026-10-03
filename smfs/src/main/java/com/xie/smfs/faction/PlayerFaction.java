package com.xie.smfs.faction;

public enum PlayerFaction {
   ORDINARY("普通人"),
   FOLK_GHOST_MASTER("民间驭鬼者"),
   HEADQUARTERS("驭鬼者总部"),
   SPIRIT_FORUM("灵异论坛"),
   PENGYOU_QUAN("朋友圈"),
   GHOST("厉鬼");

   private final String displayName;

   PlayerFaction(String displayName) {
      this.displayName = displayName;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public static PlayerFaction fromString(String name) {
      for (PlayerFaction faction : values()) {
         if (faction.name().equalsIgnoreCase(name) || faction.displayName.equals(name)) {
            return faction;
         }
      }

      return ORDINARY;
   }
}
