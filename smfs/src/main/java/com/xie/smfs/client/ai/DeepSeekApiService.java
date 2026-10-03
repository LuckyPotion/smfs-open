package com.xie.smfs.client.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DeepSeekApiService {
   private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
   private static final Gson GSON = new Gson();
   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();

   public static void generateAsync(String apiKey, String model, List<DeepSeekApiService.Message> messages, DeepSeekApiService.AiCallback callback) {
      CompletableFuture.runAsync(
         () -> {
            try {
               JsonObject body = new JsonObject();
               body.addProperty("model", model);
               JsonArray msgs = new JsonArray();

               for (DeepSeekApiService.Message msg : messages) {
                  JsonObject msgObj = new JsonObject();
                  msgObj.addProperty("role", msg.role);
                  msgObj.addProperty("content", msg.content);
                  msgs.add(msgObj);
               }

               body.add("messages", msgs);
               body.addProperty("temperature", 0.9);
               body.addProperty("max_tokens", 500);
               HttpRequest request = HttpRequest.newBuilder()
                  .uri(URI.create("https://api.deepseek.com/v1/chat/completions"))
                  .header("Authorization", "Bearer " + apiKey)
                  .header("Content-Type", "application/json")
                  .timeout(Duration.ofSeconds(30L))
                  .POST(BodyPublishers.ofString(GSON.toJson(body)))
                  .build();
               HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
               if (response.statusCode() == 200) {
                  JsonObject respObj = GSON.fromJson(response.body(), JsonObject.class);
                  JsonArray choices = respObj.getAsJsonArray("choices");
                  if (choices != null && !choices.isEmpty()) {
                     JsonObject choice = choices.get(0).getAsJsonObject();
                     JsonObject message = choice.getAsJsonObject("message");
                     String content = message.get("content").getAsString();
                     callback.onSuccess(content);
                  } else {
                     callback.onError("no_response");
                  }
               } else if (response.statusCode() == 401) {
                  callback.onError("invalid_key");
               } else {
                  callback.onError("http_" + response.statusCode());
               }
            } catch (Exception e) {
               callback.onError("network");
            }
         }
      );
   }

   public interface AiCallback {
      void onSuccess(String string);

      void onError(String string);
   }

   public static class Message {
      public final String role;
      public final String content;

      public Message(String role, String content) {
         this.role = role;
         this.content = content;
      }
   }
}
