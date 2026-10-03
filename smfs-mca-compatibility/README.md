# 神秘复苏 · 凡家物语兼容 / SMFS MCA Compatibility

让「神秘复苏」与 **MCA Reborn（凡家物语）** 共存时不再弹出开局命运选择界面的兼容补丁。

> ⚠️ **非官方源码。** 由 `smfs_mca_compatibility-1.0.0.jar` 反编译得到。反编译流程见[仓库根目录 README](../README.md) 与 [TOOLS.md](../TOOLS.md)。

## 元数据

| 项 | 值 |
|---|---|
| 模组 ID | `smfs_mca_compatibility` |
| 名称 | 神秘复苏凡家物语兼容 |
| 简介 | 禁用凡家物语开局界面，默认直接选择玩家 |
| 版本 | 1.0.0 |
| 作者 | 进击的蟹某人 |
| 许可证 | **CC0-1.0**（公共领域贡献，无附加限制） |
| 环境 | 客户端 + 服务端（`"environment": "*"`） |
| Minecraft | `~1.20.1` |
| Java | `>=17` |
| 前置 | `fabricloader >=0.17.2`、`fabric-api`、`mca` |

> 注意：主模组 `smfs` 声明的 loader 下限是 `>=0.14.21`，本兼容模组声明的是 `>=0.17.2`。

## 它解决什么问题

MCA Reborn 在玩家首次进入世界时会弹出 `DestinyScreen`（命运选择界面），要求选择性别、模型、名字、出生点等。这个界面在「神秘复苏」的流程里是多余的干扰，且会导致脚本化的开局流程中断。

本模组通过 Mixin 拦截 `MinecraftClient.setScreen`，一旦检测到该界面就**直接跳过**，并代之以一套默认设置。

## 实现

### 1. 拦截 `setScreen`

```java
@Mixin(MinecraftClient.class)
public class McaGuiSkipMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen != null) {
            String className = screen.getClass().getName();
            String simpleName = screen.getClass().getSimpleName();
            if (this.shouldSkipDestinyScreen(className, simpleName)) {
                this.clearAllPlayerBuffs();
                this.applyDefaultDestinySettings(screen);
                ci.cancel();     // 阻止该界面被真正打开
            }
        }
    }
}
```

### 2. 宽松的界面识别

刻意不直接引用 MCA 的类，而是按名字判断，避免对 MCA 版本的硬依赖：

```java
private boolean shouldSkipDestinyScreen(String className, String simpleName) {
    return simpleName.equals("DestinyScreen")
        || className.endsWith(".DestinyScreen")
        || className.contains("conczin.mca.client.gui.DestinyScreen");
}
```

### 3. 清除全部 Buff

```java
Collection<StatusEffectInstance> effects = player.getStatusEffects();
for (StatusEffectInstance effect : new ArrayList<>(effects)) {
    player.removeStatusEffect(effect.getEffectType());
}
```

### 4. 反射设置默认值

`applyDefaultDestinySettings` 用反射扫描该界面的字段与方法：

- **性别**：`String` 字段写 `"male"`，`int` 写 `0`，枚举取名称含 `male` / `m` 的常量
- **模型**：`String` 字段写 `"vanilla"`，`int` 写 `2`，枚举取名称含 `vanilla` / `player` 的常量
- **确认**：查找名称含 `accept` / `confirm` / `submit` / `done` 的方法并调用，触发界面自身的确认逻辑

整套反射都包裹在 `try/catch` 中，字段或方法不存在时仅输出 `debug` 日志并继续，不会导致游戏崩溃——这也是它能在多个 MCA 版本上通用的原因。

## 源码

```
src/main/java/com/xie/smfs_mca_compatibility/
├── SmfsMcaCompatibility.java        主入口，仅打印初始化日志
├── SmfsMcaCompatibilityClient.java  客户端入口，仅打印初始化日志
└── mixin/McaGuiSkipMixin.java       全部实际逻辑（约 8.5 KB）
```

## 构建

依赖 MCA Reborn（模组 ID `mca`）作为编译期依赖，见 [`build.gradle`](build.gradle)：

```groovy
modImplementation "maven.modrinth:minecraft-comes-alive-reborn:${project.mca_version}"
```

`gradle.properties` 中的 `mca_version` 为 `7.6.26+1.20.1`（1.20.1 Fabric 分支的最新稳定版）。若需换用其他版本，或 Modrinth 未收录对应构建，可改用本地 jar：

```groovy
modCompileOnly files('reference/minecraft-comes-alive-<version>.jar')
```

## 授权

CC0-1.0，见 [`LICENSE`](LICENSE)。原始 jar 的 `fabric.mod.json` 中 `license` 字段即为 `CC0-1.0`。
