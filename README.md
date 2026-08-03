# NBTech2Fix 1.20.1

> Source available under All Rights Reserved. No permission is granted to copy,
> modify, redistribute, or create derivative works. See [LICENSE](LICENSE).

此分支源码公开仅供查看，作者保留所有权利。

这是从 1.21.1 NeoForge AI移植的 Minecraft 1.20.1 版本。

本工程生成一个 Forge / NeoForge 共用 JAR。FML 47.x 会在启动期检查已发现模组，只有目标模组存在时才注册对应 Mixin；玩家无需安装整套目标模组。共用 JAR 不代表每项功能在两端都有目标模组，实际范围如下。

| 目标模组 | 1.20.1 官方加载器文件 | 本移植功能 |
|---|---|---|
| BrandonsCore / Draconic Evolution | Forge + NeoForge 共用文件 | 传送器线程、客户端空进程、Curios 护盾选择、HUD 聚合 |
| MoreMekaSuitModules | Forge + NeoForge 共用文件 | 未安装/启用护盾模块时不再错误免死 |
| Applied Energistics 2 15.4.10 | Forge | 加工输入 NBT、CPU 等待键与最终产物回流 |
| AdvancedAE | 文件标记 Forge + NeoForge；该修复还要求 AE2 | 适配其独立合成 CPU 的 NBT 回流 |
| Neo ECO AE Extension | Forge | 按 20.3.0 新状态机适配等待键和 in-flight 输出 |
| PackagedAuto / PackagedDraconic | Forge + NeoForge 共用文件；该修复还要求 AE2 | 传递并消费真实输入，保留 NBT；SimpleInput 放宽 NBT 验证 |
| Observable | Forge（运行还需要 Architectury API 与 Kotlin for Forge） | 上传禁用/超时与 OSHI 诊断超时 |
| Player Shells | NeoForge | 延后意识转移，让可取消死亡事件先执行 |

未移植的旧功能：MMSM 免死耗电整数除法、MMSM 空中游泳、相位第三人称镜头、DE 模型懒加载。原因分别是 1.20.1 最新目标版本中 bug 或目标模块不存在，或已经由上游处理；强行注入会改变既有逻辑。

配置生成在 `config/nbtech2fix-common.toml`。命令：

- `/nbtech2fix config <item> get`
- `/nbtech2fix config <item> set <value>`
- `/nbtech2fix tools CheckAllThreads`

开发构建使用 Java 17：`./gradlew build`（Windows 为 `gradlew.bat build`）。
