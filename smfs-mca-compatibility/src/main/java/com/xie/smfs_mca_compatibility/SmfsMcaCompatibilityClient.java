package com.xie.smfs_mca_compatibility;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmfsMcaCompatibilityClient implements ClientModInitializer {
   public static final Logger LOGGER = LoggerFactory.getLogger("SMFS MCA Compatibility Client");

   public void onInitializeClient() {
      LOGGER.info("神秘复苏凡家物语兼容模组客户端初始化完成");
   }
}
