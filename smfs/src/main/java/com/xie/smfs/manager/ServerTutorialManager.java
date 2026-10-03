package com.xie.smfs.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xie.smfs.util.EventStoryEntry;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class ServerTutorialManager extends TutorialManager {
   private static ServerTutorialManager instance;
   private MinecraftServer server;

   private ServerTutorialManager(MinecraftServer server) {
      this.server = server;
      this.loadTutorials();
      this.loadEventStories();
   }

   public static ServerTutorialManager getInstance(MinecraftServer server) {
      if (instance == null) {
         instance = new ServerTutorialManager(server);
      }

      return instance;
   }

   @Override
   public int calculateCurrentDay() {
      World world = this.server.getOverworld();
      if (world != null) {
         long totalTime = world.getTime();
         int day = (int)(totalTime / 24000L) + 1;
         return Math.max(day, 1);
      } else {
         LOGGER.warn("世界未加载，使用默认天数1");
         return 1;
      }
   }

   @Override
   protected void loadTutorials() {
      this.tutorials.clear();

      try {
         InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("assets/smfs/tutorials/tutorials.json");
         if (inputStream != null) {
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Type tutorialListType = (new TypeToken<List<TutorialManager.TutorialEntry>>() {}).getType();
            List<TutorialManager.TutorialEntry> loadedTutorials = new Gson().fromJson(reader, tutorialListType);
            this.tutorials.addAll(loadedTutorials);
            LOGGER.info("服务端成功加载 {} 个教程条目", this.tutorials.size());
         } else {
            LOGGER.warn("服务端教程文件未找到: assets/smfs/tutorials/tutorials.json");
            this.tutorials.addAll(this.createDefaultTutorials());
         }
      } catch (Exception e) {
         LOGGER.error("服务端加载教程数据失败", e);
         this.tutorials.addAll(this.createDefaultTutorials());
      }
   }

   @Override
   protected void loadEventStories() {
      this.eventStories.clear();

      try {
         InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("assets/smfs/tutorials/event_stories.json");
         if (inputStream != null) {
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Type eventListType = (new TypeToken<List<EventStoryEntry>>() {}).getType();
            List<EventStoryEntry> loadedEvents = new Gson().fromJson(reader, eventListType);
            this.eventStories.addAll(loadedEvents);
            LOGGER.info("服务端成功加载 {} 个事件剧情条目", this.eventStories.size());
         } else {
            LOGGER.warn("服务端事件剧情文件未找到: assets/smfs/tutorials/event_stories.json");
            this.eventStories.addAll(this.createDefaultEvents());
         }
      } catch (Exception e) {
         LOGGER.error("服务端加载事件剧情数据失败", e);
         this.eventStories.addAll(this.createDefaultEvents());
      }
   }

   @Override
   protected String getPlayerName() {
      return "Player";
   }

   public String getPlayerName(ServerPlayerEntity player) {
      return player != null ? player.getName().getString() : "Player";
   }

   @Override
   protected int getViewCount() {
      return 0;
   }

   public int getViewCount(ServerPlayerEntity player) {
      return 0;
   }

   @Override
   protected void incrementViewCount() {
   }

   public void incrementViewCount(ServerPlayerEntity player) {
   }

   @Override
   protected List<String> getShownEvents() {
      return new ArrayList<>();
   }

   public List<String> getShownEvents(ServerPlayerEntity player) {
      return new ArrayList<>();
   }

   @Override
   protected void addShownEvent(String eventId) {
   }

   public void addShownEvent(ServerPlayerEntity player, String eventId) {
   }

   @Override
   protected EventStoryEntry getValidEventStory() {
      return null;
   }

   public EventStoryEntry getValidEventStory(ServerPlayerEntity player) {
      if (player != null && !this.eventStories.isEmpty()) {
         List<String> shownEvents = this.getShownEvents(player);

         for (EventStoryEntry event : this.eventStories) {
            EventStoryEntry.TriggerConditions conditions = event.getTriggerConditions();
            if ((conditions.isRepeatable() || !shownEvents.contains(event.getId()))
               && conditions.getMinDay() <= this.currentDay
               && this.hasRequiredItems(player, conditions.getRequiredItems())) {
               return event;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public String getCurrentStory(ServerPlayerEntity player) {
      this.currentDay = this.calculateCurrentDay();
      int viewCount = this.getViewCount(player);
      if (viewCount == 0) {
         String dailyStory = this.processStoryVariables(this.getDailyStory(), this.getPlayerName(player));
         this.incrementViewCount(player);
         return dailyStory;
      }

      EventStoryEntry eventStory = this.getValidEventStory(player);
      if (eventStory != null) {
         if (!eventStory.getTriggerConditions().isRepeatable()) {
            this.addShownEvent(player, eventStory.getId());
         }

         return this.processStoryVariables(eventStory.getStory(), this.getPlayerName(player));
      } else {
         return this.processStoryVariables(this.getDailyStory(), this.getPlayerName(player));
      }
   }

   protected String processStoryVariables(String story, String playerName) {
      return story.replace("{playerName}", playerName);
   }
}
