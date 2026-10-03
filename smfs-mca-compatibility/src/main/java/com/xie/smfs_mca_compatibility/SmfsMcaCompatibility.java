package com.xie.smfs_mca_compatibility;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmfsMcaCompatibility implements ModInitializer {
   public static final String MOD_ID = "smfs_mca_compatibility";
   public static final Logger LOGGER = LoggerFactory.getLogger("smfs_mca_compatibility");

   @Override
   public void onInitialize() {
      LOGGER.info("神秘复苏凡家物语兼容模组初始化...");
      LOGGER.info("功能: 自动跳过凡家物语(MCA)开局选择界面");
      LOGGER.info("状态: 已启用 - 使用Mixin拦截GUI");
   }
}
