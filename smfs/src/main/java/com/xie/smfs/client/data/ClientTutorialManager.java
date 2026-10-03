package com.xie.smfs.client.data;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.xie.smfs.manager.TutorialManager;
import com.xie.smfs.util.EventStoryEntry;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ClientTutorialManager extends TutorialManager {
   private static ClientTutorialManager instance;

   private ClientTutorialManager() {
      this.loadTutorials();
      this.loadEventStories();
   }

   public static ClientTutorialManager getInstance() {
      if (instance == null) {
         instance = new ClientTutorialManager();
      }

      return instance;
   }

   @Override
   public int calculateCurrentDay() {
      MinecraftClient client = MinecraftClient.getInstance();
      World world = client.world;
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
         ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
         Identifier tutorialResource = new Identifier("smfs", "tutorials/tutorials.json");
         if (resourceManager.getResource(tutorialResource).isPresent()) {
            InputStreamReader reader = new InputStreamReader(resourceManager.open(tutorialResource), StandardCharsets.UTF_8);
            Type tutorialListType = (new TypeToken<List<TutorialManager.TutorialEntry>>() {}).getType();
            List<TutorialManager.TutorialEntry> loadedTutorials = new Gson().fromJson(reader, tutorialListType);
            this.tutorials.addAll(loadedTutorials);
            LOGGER.info("成功加载 {} 个教程条目", this.tutorials.size());
         } else {
            LOGGER.warn("教程文件未找到: {}", tutorialResource);
            this.tutorials.addAll(this.createDefaultTutorials());
         }
      } catch (Exception e) {
         LOGGER.error("加载教程数据失败", e);
         this.tutorials.addAll(this.createDefaultTutorials());
      }
   }

   @Override
   protected void loadEventStories() {
      this.eventStories.clear();

      try {
         ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
         Identifier eventResource = new Identifier("smfs", "tutorials/event_stories.json");
         if (resourceManager.getResource(eventResource).isPresent()) {
            InputStreamReader reader = new InputStreamReader(resourceManager.open(eventResource), StandardCharsets.UTF_8);
            Type eventListType = (new TypeToken<List<EventStoryEntry>>() {}).getType();
            List<EventStoryEntry> loadedEvents = new Gson().fromJson(reader, eventListType);
            this.eventStories.addAll(loadedEvents);
            LOGGER.info("成功加载 {} 个事件剧情条目", this.eventStories.size());
         } else {
            LOGGER.warn("事件剧情文件未找到: {}", eventResource);
            this.eventStories.addAll(this.createDefaultEvents());
         }
      } catch (Exception e) {
         LOGGER.error("加载事件剧情数据失败", e);
         this.eventStories.addAll(this.createDefaultEvents());
      }
   }

   @Override
   protected String getPlayerName() {
      MinecraftClient client = MinecraftClient.getInstance();
      PlayerEntity player = client.player;
      return player != null ? player.getName().getString() : "Player";
   }

   @Override
   protected int getViewCount() {
      return ClientDataManager.getTutorialViewCount();
   }

   @Override
   protected void incrementViewCount() {
      ClientDataManager.incrementTutorialViewCount();
   }

   @Override
   protected List<String> getShownEvents() {
      String shownEventsJson = ClientDataManager.getShownEvents();

      try {
         return new Gson().fromJson(shownEventsJson, (new TypeToken<List<String>>() {}).getType());
      } catch (JsonSyntaxException e) {
         return new ArrayList<>();
      }
   }

   @Override
   protected void addShownEvent(String eventId) {
      List<String> shownEvents = this.getShownEvents();
      if (!shownEvents.contains(eventId)) {
         shownEvents.add(eventId);
         ClientDataManager.setShownEvents(new Gson().toJson(shownEvents));
      }
   }

   @Override
   protected EventStoryEntry getValidEventStory() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && !this.eventStories.isEmpty()) {
         PlayerEntity player = client.player;
         List<String> shownEvents = this.getShownEvents();

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
}
