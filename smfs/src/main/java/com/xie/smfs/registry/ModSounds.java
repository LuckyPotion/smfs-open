package com.xie.smfs.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
   public static SoundEvent KNOCKING_SOUND;
   public static SoundEvent SWING_ATTACK_SOUND;
   public static SoundEvent BACKGROUND_BABY_CRYING;
   public static SoundEvent BACKGROUND_CREEPY_HUMMING;
   public static SoundEvent BACKGROUND_EERIE_PIANO;
   public static SoundEvent BACKGROUND_PIANO;
   public static SoundEvent BACKGROUND_WHISPERING;
   public static SoundEvent BACKGROUND_SUONA_MUSIC;
   public static SoundEvent BACKGROUND_HORROR_BACKGROUND;
   public static SoundEvent BACKGROUND_EERIE_MUSIC_BOX;
   public static SoundEvent SCRIPT_CROW_SOUND;
   public static SoundEvent SCRIPT_TERRIFYING_LAUGH;
   public static SoundEvent SCRIPT_HORROR_SOUND_2;
   public static SoundEvent SCRIPT_HORROR_SOUND_3;
   public static SoundEvent SCRIPT_HORROR_SOUND_4;
   public static SoundEvent SCRIPT_HORROR_SOUND_5;
   public static SoundEvent SCRIPT_EERIE_SOUND_1;
   public static SoundEvent SCRIPT_HORROR_SOUND_6;
   public static SoundEvent SCRIPT_HORROR_SOUND_7;
   public static SoundEvent SCRIPT_HORROR_SOUND_8;
   public static SoundEvent SCRIPT_HORROR_SOUND_9;
   public static SoundEvent SCRIPT_HORROR_SOUND_11;
   public static SoundEvent SCRIPT_HORROR_SOUND_12;
   public static SoundEvent SCRIPT_HORROR_SOUND_13;
   public static SoundEvent SCRIPT_GONG_SOUND;
   public static SoundEvent QUEST_SUCCESS;
   public static SoundEvent QUEST_FAILURE;
   public static SoundEvent GHOST_BRIDE_ENTRANCE;
   public static SoundEvent CRYING_GHOST_ENTRANCE;
   public static SoundEvent YANG_JIAN_ENTRANCE;
   public static SoundEvent YANG_JIAN_BGM;
   public static SoundEvent GHOST_OFFICER_ENTRANCE;
   public static SoundEvent GHOST_WIND_SOUND;
   public static SoundEvent NOTICE_PAGE_1;
   public static SoundEvent NOTICE_PAGE_2;
   public static SoundEvent NOTICE_PAGE_3;
   public static SoundEvent NOTICE_PAGE_4;
   public static SoundEvent NOTICE_PAGE_5;
   public static SoundEvent NOTICE_PAGE_6;

   public static void registerSounds() {
      KNOCKING_SOUND = register("entity.knocking_ghost.knock");
      SWING_ATTACK_SOUND = register("entity.giant_male_corpse.swing_attack");
      BACKGROUND_BABY_CRYING = register("background.baby_crying");
      BACKGROUND_CREEPY_HUMMING = register("background.creepy_humming");
      BACKGROUND_EERIE_PIANO = register("background.eerie_piano");
      BACKGROUND_PIANO = register("background.piano");
      BACKGROUND_WHISPERING = register("background.whispering");
      BACKGROUND_SUONA_MUSIC = register("background.suona_music");
      BACKGROUND_HORROR_BACKGROUND = register("background.horror_background");
      BACKGROUND_EERIE_MUSIC_BOX = register("background.eerie_music_box");
      SCRIPT_CROW_SOUND = register("script.crow_sound");
      SCRIPT_TERRIFYING_LAUGH = register("script.terrifying_laugh");
      SCRIPT_HORROR_SOUND_2 = register("script.horror_sound_2");
      SCRIPT_HORROR_SOUND_3 = register("script.horror_sound_3");
      SCRIPT_HORROR_SOUND_4 = register("script.horror_sound_4");
      SCRIPT_HORROR_SOUND_5 = register("script.horror_sound_5");
      SCRIPT_EERIE_SOUND_1 = register("script.eerie_sound_1");
      SCRIPT_HORROR_SOUND_6 = register("script.horror_sound_6");
      SCRIPT_HORROR_SOUND_7 = register("script.horror_sound_7");
      SCRIPT_HORROR_SOUND_8 = register("script.horror_sound_8");
      SCRIPT_HORROR_SOUND_9 = register("script.horror_sound_9");
      SCRIPT_HORROR_SOUND_11 = register("script.horror_sound_11");
      SCRIPT_HORROR_SOUND_12 = register("script.horror_sound_12");
      SCRIPT_HORROR_SOUND_13 = register("script.horror_sound_13");
      SCRIPT_GONG_SOUND = register("script.gong_sound");
      QUEST_SUCCESS = register("ui.quest.success");
      QUEST_FAILURE = register("ui.quest.failure");
      GHOST_BRIDE_ENTRANCE = register("entity.ghost_bride.entrance");
      CRYING_GHOST_ENTRANCE = register("entity.crying_ghost.entrance");
      YANG_JIAN_ENTRANCE = register("entity.yang_jian.entrance");
      YANG_JIAN_BGM = register("entity.yang_jian.bgm");
      GHOST_OFFICER_ENTRANCE = register("entity.ghost_officer.entrance");
      GHOST_WIND_SOUND = register("effect.ghost_wind");
      NOTICE_PAGE_1 = register("notice.page1");
      NOTICE_PAGE_2 = register("notice.page2");
      NOTICE_PAGE_3 = register("notice.page3");
      NOTICE_PAGE_4 = register("notice.page4");
      NOTICE_PAGE_5 = register("notice.page5");
      NOTICE_PAGE_6 = register("notice.page6");
   }

   private static SoundEvent register(String id) {
      Identifier identifier = new Identifier("smfs", id);
      SoundEvent soundEvent = SoundEvent.method_47908(identifier);
      return (SoundEvent)Registry.method_10230(Registries.field_41172, identifier, soundEvent);
   }
}
