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
| Applied Energistics 2 15.4.10 | Forge | 加工 / 合成 / 锻造台 / 切石机样板的输入 NBT 策略、CPU 等待键与最终产物回流 |
| AdvancedAE | 文件标记 Forge + NeoForge；该修复还要求 AE2 | 适配其独立合成 CPU 的 NBT 回流 |
| Neo ECO AE Extension | Forge | 按 20.3.0 新状态机适配等待键和 in-flight 输出 |
| PackagedAuto / PackagedDraconic | Forge + NeoForge 共用文件；该修复还要求 AE2 | 传递并消费真实输入，保留 NBT；SimpleInput 放宽 NBT 验证 |
| Observable | Forge（运行还需要 Architectury API 与 Kotlin for Forge） | 上传禁用/超时与 OSHI 诊断超时 |
| Player Shells | NeoForge | 延后意识转移，让可取消死亡事件先执行 |

未移植的旧功能：MMSM 免死耗电整数除法、MMSM 空中游泳、相位第三人称镜头、DE 模型懒加载。原因分别是 1.20.1 最新目标版本中 bug 或目标模块不存在，或已经由上游处理；强行注入会改变既有逻辑。

## `FixAECraftNBT`：AE 合成的 NBT 策略

AE2 用 NBT 全等判断两个物品是否为同一个 key，因此同一件装备只要 NBT 不同就会被当作两种东西。
本项配置让 AE2 在**原版判定为否**时，按策略接受「同 `Item`、仅 NBT 不同」的变体。

| 值 | 语义 |
|---|---|
| `DISABLED` | 完全保持 AE2 原逻辑 |
| `DE_ONLY` | 仅 `draconicevolution` 命名空间（保留，兼容旧配置） |
| `ALL` | 所有物品 |
| `LIST` | 仅 `FixAECraftNBTList` 中列出的命名空间 |

`LIST` 适合"只有某些模组的产物需要放宽"的场景。默认列表为
`["draconicevolution", "minecraft"]`，按需追加，例如加上 `irons_spellbooks`，
就不必像 `ALL` 那样把全场的 NBT 判定一起放宽。

覆盖范围：**加工样板、合成样板、锻造台样板、切石机样板**，且包含任意第三方 `IPatternDetails`
（因为这些判定统一收敛到 `IInput.isValid`）。

三条不变量：

1. **只做 `false → true` 的回退。** 原版返回 `true` 的情况一律原样放行，从不把 `true` 改成 `false`。
2. **不会跨物品放行。** 所有放宽分支都先要求 `Item` 是同一实例。
3. **产物 NBT 永远由原版配方决定。** 本模组只影响"什么算合法输入"，不构造任何产物；
   锻造台 / 切石机的回退路径是把样板自己编码的 stack 放回配方重跑，而不是自己拼产物。

> 注意 `ALL` / `LIST` 的语义是"同物品、NBT 任意不同"，因此一把**附魔或已损耗**的同类装备
> 理论上可以被当作材料消耗，产出的是样板声明的那个结果。这是该策略的固有含义。

配置生成在 `config/nbtech2fix-common.toml`。命令：

- `/nbtech2fix config <item> get`
- `/nbtech2fix config <item> set <value>`
- `/nbtech2fix tools CheckAllThreads`
- `/nbtech2fix tools CheckAEMixins` —— 自检 AE 相关 Mixin 是否注入成功，排查问题时请附上输出

开发构建使用 Java 17：`./gradlew build`（Windows 为 `gradlew.bat build`）。

## 更新日志

### 1.6.1

[修复]修复AE合成忽略材料NBT（FixAECraftNBT）只对加工样板生效的问题，现覆盖合成样板、锻造台样板与切石机样板；此前用带NBT的盔甲、工具作材料时，合成会无法发出请求，或在分子装配室永久卡住不出货
[添加]FixAECraftNBT新增LIST模式，可按物品命名空间（modid）指定忽略NBT的范围，默认["draconicevolution", "minecraft"]
[添加]命名空间列表现可在配置界面与命令中直接编辑，无需手动改配置文件
[添加]新增自检命令/nbtech2fix tools CheckAEMixins，用于确认AE相关Mixin是否注入成功
[说明]本次更新由DeepSeek完成

[Fix]Fixed FixAECraftNBT (ignore material NBT in AE crafting) only applying to processing patterns; it now also covers crafting patterns, smithing table patterns and stonecutting patterns. Previously, using NBT-bearing armor or tools as materials would either fail to dispatch a craft at all, or hang the molecular assembler forever without producing output
[Add]Added a LIST mode to FixAECraftNBT, filtering by item namespace (mod id); default is ["draconicevolution", "minecraft"]
[Add]The namespace list can now be edited from the config screen and via command, no need to edit the config file by hand
[Add]Added /nbtech2fix tools CheckAEMixins to self-check whether the AE related Mixins are injected correctly
[Note]This update was completed by DeepSeek


