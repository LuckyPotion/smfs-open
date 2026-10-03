package com.xie.smfs.client.sound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.FloatControl.Type;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryUtil;

public class CustomMusicBoxPlayer {
   private static final Path MUSIC_BOX_DIR = FabricLoader.getInstance().getGameDir().resolve("smfs").resolve("music_box");
   private Clip clip;

   public boolean start() {
      File[] files = this.findAudioFiles();
      if (files != null && files.length != 0) {
         for (File file : files) {
            try {
               String name = file.getName().toLowerCase();
               if (name.endsWith(".ogg")) {
                  if (this.playOgg(file)) {
                     return true;
                  }
               } else if (this.playWav(file)) {
                  return true;
               }
            } catch (Exception e) {
               System.err.println("[SMFS] 跳过文件 " + file.getName() + ": " + e.getMessage());
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean playOgg(File file) throws Exception {
      byte[] fileBytes = Files.readAllBytes(file.toPath());
      ByteBuffer fileBuffer = BufferUtils.createByteBuffer(fileBytes.length);
      fileBuffer.put(fileBytes);
      fileBuffer.flip();
      IntBuffer error = BufferUtils.createIntBuffer(1);
      long decoder = STBVorbis.stb_vorbis_open_memory(fileBuffer, error, null);
      if (decoder == 0L) {
         System.err.println("[SMFS] OGG解码失败: " + file.getName() + " (错误码: " + error.get(0) + ")");
         MemoryUtil.memFree(fileBuffer);
         return false;
      }

      try {
         STBVorbisInfo info = STBVorbisInfo.malloc();
         STBVorbis.stb_vorbis_get_info(decoder, info);
         int channels = info.channels();
         int sampleRate = info.sample_rate();
         info.free();
         int bufferSize = 4096;
         ShortBuffer buf = BufferUtils.createShortBuffer(bufferSize * channels);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();

         int read;
         while ((read = STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, buf)) > 0) {
            for (int i = 0; i < read * channels; i++) {
               short sample = buf.get(i);
               baos.write(sample & 255);
               baos.write(sample >> 8 & 0xFF);
            }
         }

         byte[] pcm = baos.toByteArray();
         AudioFormat format = new AudioFormat(sampleRate, 16, channels, true, false);
         ByteArrayInputStream bais = new ByteArrayInputStream(pcm);
         AudioInputStream audioStream = new AudioInputStream(bais, format, pcm.length / 2);
         this.clip = AudioSystem.getClip();
         this.clip.open(audioStream);
         this.setVolume();
         this.clip.loop(-1);
         this.clip.start();
         return true;
      } finally {
         STBVorbis.stb_vorbis_close(decoder);
         MemoryUtil.memFree(fileBuffer);
      }
   }

   private boolean playWav(File file) throws Exception {
      AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
      AudioFormat baseFormat = audioStream.getFormat();
      AudioFormat decodedFormat = new AudioFormat(
         Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false
      );
      AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, audioStream);
      this.clip = AudioSystem.getClip();
      this.clip.open(decodedStream);
      this.setVolume();
      this.clip.loop(-1);
      this.clip.start();
      return true;
   }

   private void setVolume() {
      if (this.clip != null && this.clip.isControlSupported(Type.MASTER_GAIN)) {
         FloatControl gain = (FloatControl)this.clip.getControl(Type.MASTER_GAIN);
         float volume = 0.7F;
         float dB = 20.0F * (float)Math.log10(Math.max(volume, 1.0E-4));
         gain.setValue(Math.max(dB, gain.getMinimum()));
      }
   }

   public void stop() {
      if (this.clip != null && this.clip.isRunning()) {
         this.clip.stop();
         this.clip.close();
         this.clip = null;
      }
   }

   private File[] findAudioFiles() {
      try {
         Files.createDirectories(MUSIC_BOX_DIR);
      } catch (IOException e) {
         System.err.println("[SMFS] 创建音乐文件夹失败: " + e.getMessage());
         return null;
      }

      File dir = MUSIC_BOX_DIR.toFile();
      File[] files = dir.listFiles((d, name) -> {
         String lower = name.toLowerCase();
         return lower.endsWith(".wav") || lower.endsWith(".ogg");
      });
      if (files != null && files.length > 0) {
         Arrays.sort(files, Comparator.comparing(File::getName));
         return files;
      } else {
         return null;
      }
   }
}
