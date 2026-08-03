package nbtech2.fix.DataGen;

import net.minecraft.data.PackOutput;

public class ModLangProvider extends net.neoforged.neoforge.common.data.LanguageProvider {

    public String locale;

    public ModLangProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        switch (locale) {
            case "zh_cn" -> addZhCN();
            case "en_us" -> addEnUS();
            default -> addEnUS();
        }
    }

    private void addZhCN() {
        add("nbtech2fix.configuration.FixDEDislocatorThreadBug", "[修复]龙之研究传送器多线程访问崩溃");
        add("nbtech2fix.configuration.FixDEDislocatorThreadBug.tooltip", "龙之研究的传送器有多线程bug，会在客户端访问服务端内容，与c2me一起时会使游戏崩溃（虽然可以通过关闭c2me的审查来避免崩溃，但本质上是龙研的bug，所以在此添加一个修复）此优化将重写龙研该部分内容（此选项用来控制该mixin是否加载，更改后重启游戏生效）");

        add("nbtech2fix.configuration.FixBrandonsCoreNullptr", "[修复]BrandonsCore空指针崩溃");
        add("nbtech2fix.configuration.FixBrandonsCoreNullptr.tooltip", "[理论有效]修复com/brandon3055/brandonscore/client/ProcessHandlerClient.class:49有概率nullptr崩溃（仅当nullptr时跳过调用）因触发频率极低（200天触发两次），触发原因未知，无法确定必定生效");

        add("nbtech2fix.configuration.FixMMSMSwimInAir", "[修复]MoreMekaSuitModule穿墙模块空中趴下");
        add("nbtech2fix.configuration.FixMMSMSwimInAir.tooltip", "修复 更多MekaSuit模块 在飞行时启用穿墙模块会因卡头在空中趴下，皆若空游无所依~");

        add("nbtech2fix.configuration.FixMMSMEnergyShieldAntiDieBug", "[修复]MoreMekaSuitModule无模块免死");
        add("nbtech2fix.configuration.FixMMSMEnergyShieldAntiDieBug.tooltip", "修复 更多MekaSuit模块 未添加护盾模块时，依旧可以触发护盾模块的免死功能");

        add("nbtech2fix.configuration.FixMMSM","[修复]MoreMekaSuitModule护盾模块免死的耗电逻辑");
        add("nbtech2fix.configuration.FixMMSM.tooltip","修复 更多MekaSuit模块 免死耗电逆天逻辑：整数除整数，除非全身仅一件装备有电为1/1，否则恒为0，永远不耗电；修完发现原本调用的方法入口也是扣不了电的。。。新扣电逻辑：全身相同比例扣总计10MFE，总量固定，电多的扣的多，电少的扣的少");

        add("nbtech2fix.configuration.FixPhaseCamera","[优化]视角穿墙");
        add("nbtech2fix.configuration.FixPhaseCamera.tooltip","玩家使用MoreMekaSuitModule量子模块穿墙时，第三人称视角会缩回玩家体内，添加此优化");

        add("nbtech2fix.configuration.FixDEHUDRenderBug","[修复]龙之研究HUD显示的护盾和不死模块问题");
        add("nbtech2fix.configuration.FixDEHUDRenderBug.tooltip","龙之研究的HUD会按饰品栏位靠上的物品显示，不死模块数量不显示护胸、项链的总和，仅显示为饰品栏位靠上的部件中的数量");

        add("nbtech2fix.configuration.FixDECuriosOrderBug","[优化]因饰品栏位顺序对护盾逻辑的影响");
        add("nbtech2fix.configuration.FixDECuriosOrderBug.tooltip","例如，项链和护胸均在饰品栏位时，如果项链在护胸上面（项链在“饰品”栏而不是“项链”栏，护胸在“护胸”栏），护胸中的护盾不会生效");

        add("nbtech2fix.configuration.FixPlayerShellsDiePriority","[优化]PlayerShells死亡优先级");
        add("nbtech2fix.configuration.FixPlayerShellsDiePriority.tooltip","优化'PlayerShells'因注入ServerPlayer Die方法，导致很多基于取消死亡事件的免死手段（例如MMSM的免死单元、DE的免死模块）触发前就被意识转移");

        add("nbtech2fix.configuration.FixAECraftNBT", "[优化]AE无法识别龙研物品作为材料");
        add("nbtech2fix.configuration.FixAECraftNBT.tooltip", "龙之研究物品做出来后带有特殊nbt使得ae无法识别为材料用来合成，添加此选项可选DISABLED:关闭;DE_ONLY:仅对龙之研究忽略nbt;ALL:对所有物品忽略nbt;");

        add("nbtech2fix.configuration.CommandConfigPremissionLevel","[自身]config命令权限等级");
        add("nbtech2fix.configuration.CommandConfigPremissionLevel.tooltip","此选项用于调整本mod添加的/nbtech2fix下子命令config所需的权限等级;不需要reload，但是需要客户端刷新缓存，例如重进世界");

        add("nbtech2fix.configuration.CommandToolsPermissionLevel","[自身]tools命令权限等级");
        add("nbtech2fix.configuration.CommandToolsPermissionLevel.tooltip","此选项用于调整本mod添加的/nbtech2fix下子命令tools所需的权限等级;不需要reload，但是需要客户端刷新缓存，例如重进世界");

        add("nbtech2fix.configuration.FixPackagedDEDoesntUseInput","[修复]封包龙研不使用输入物品");
        add("nbtech2fix.configuration.FixPackagedDEDoesntUseInput.tooltip","封包龙研输入物品后，其实合成中使用的是配方中物品，会直接把实际输入丢弃，提供此选功能以修复；如果你在AE中调用封包龙研进行自动化合成，开启此选项可避免模块消失");

        add("nbtech2fix.configuration.FixObNetStuck","[修复]Observable卡住");
        add("nbtech2fix.configuration.FixObNetStuck.tooltip","Observable在上传时卡住会导致一直无法查看调试结果；添加此选项修复，可选DISABLED关闭修复，TIMEOUT上传超时则中断，NO_UPLOAD禁止上传");

        add("nbtech2fix.configuration.ObservableUploadTimeoutTime","TIMEOUT上传超时时长");
        add("nbtech2fix.configuration.ObservableUploadTimeoutTime.tooltip","\"[修复]Observable卡住\"选择TIMEOUT时，可在此配置超时时长，默认3000，单位毫秒（ms）");

        add("nbtech2fix.configuration.FixObNoOshiFileStuck","[修复]Observable找不到库文件");
        add("nbtech2fix.configuration.FixObNoOshiFileStuck.tooltip","修复[Profiler/DEBUG] [oshi.util.FileUtil/]: No oshi.architecture.properties file found from ClassLoader cpw.mods.modlauncher.TransformingClassLoader后卡住导致无法查看调试结果");

        add("nbtech2fix.configuration.ObNoFileTimeoutTime","FixObNoOshiFileStuck超时时长");
        add("nbtech2fix.configuration.ObNoFileTimeoutTime.tooltip","\"[修复]Observable找不到库文件\"开启时，可在此配置超时时长，默认3000，单位毫秒（ms）");


        add("nbtech2fix.commands.CheckAllThreads.ThreadCount","当前活跃线程数: %s");
        add("nbtech2fix.commands.CheckAllThreads.ThreadList","=== 线程列表 ===");


    }

    private void addEnUS() {
        add("nbtech2fix.configuration.FixDEDislocatorThreadBug", "[Fix] Draconic Evolution Dislocator multi-thread crash");
        add("nbtech2fix.configuration.FixDEDislocatorThreadBug.tooltip", "The Draconic Evolution Dislocator has a multi-threading bug that causes the client to access server-side content. This can crash the game when used with C2ME (although disabling C2ME's strict checks can avoid the crash, it is essentially a DE bug, so a fix is added here). This optimization rewrites the relevant DE code. (This option controls whether the mixin is loaded. Restart the game after changing.)");

        add("nbtech2fix.configuration.FixBrandonsCoreNullptr", "[Fix] BrandonsCore null pointer crash");
        add("nbtech2fix.configuration.FixBrandonsCoreNullptr.tooltip", "[Theoretically effective] Fixes a potential null pointer crash at com/brandon3055/brandonscore/client/ProcessHandlerClient.class:49 (skips the call when null). Since the trigger frequency is extremely low (twice in 200 days), the cause is unknown, so effectiveness is not guaranteed.");

        add("nbtech2fix.configuration.FixMMSMSwimInAir", "[Fix] MoreMekaSuitModule phase module air swimming");
        add("nbtech2fix.configuration.FixMMSMSwimInAir.tooltip", "Fixes the bug where enabling the phase module while flying in 'More MekaSuit Module' causes the player to get stuck and swim in the air. Just like a fish out of water~");

        add("nbtech2fix.configuration.FixMMSMEnergyShieldAntiDieBug", "[Fix] 'MoreMekaSuitModule' death prevention without module");
        add("nbtech2fix.configuration.FixMMSMEnergyShieldAntiDieBug.tooltip", "Fixes the bug where 'More MekaSuit Module' still triggers the energy shield's death prevention even without the shield module installed.");

        add("nbtech2fix.configuration.FixMMSM", "[Fix] 'MoreMekaSuitModule' energy shield death prevention power consumption logic");
        add("nbtech2fix.configuration.FixMMSM.tooltip", "Fixes the outrageous power consumption logic of MMSM death prevention: integer division by integer, causing the cost to be always 0 unless only one piece of equipment has exactly 1/1 power. The new consumption logic: the total 10 MFE is proportionally consumed based on the charge ratio across all equipped items. Items with more power pay more, less pay less.");

        add("nbtech2fix.configuration.FixPhaseCamera", "[Optimization] Camera phase through walls");
        add("nbtech2fix.configuration.FixPhaseCamera.tooltip", "When the player uses the 'MoreMekaSuitModule' quantum module to phase through walls, the third-person camera would retract inside the player. This optimization fixes that.");

        add("nbtech2fix.configuration.FixDEHUDRenderBug", "[Fix] 'Draconic Evolution' HUD shield and death prevention module display issue");
        add("nbtech2fix.configuration.FixDEHUDRenderBug.tooltip", "The Draconic Evolution HUD displays based on the highest curio slot item. The death prevention module count does not show the total of chest and necklace, only the amount in the higher curio slot.");

        add("nbtech2fix.configuration.FixDECuriosOrderBug", "[Optimization] Effect of curio slot order on shield logic");
        add("nbtech2fix.configuration.FixDECuriosOrderBug.tooltip", "For example, when both necklace and chest are in curio slots, if the necklace is above the chest (necklace in a generic 'curio' slot instead of 'necklace' slot, chest in 'chest' slot), the shield in the chest won't activate.");

        add("nbtech2fix.configuration.FixPlayerShellsDiePriority", "[Optimization] PlayerShells death priority");
        add("nbtech2fix.configuration.FixPlayerShellsDiePriority.tooltip", "Optimizes 'PlayerShells' injection into the ServerPlayer die method, which previously caused many death-canceling methods (e.g., MMSM's death prevention unit, DE's death prevention module) to be bypassed by consciousness transfer before they could trigger.");

        add("nbtech2fix.configuration.FixAECraftNBT", "[Optimization]AE can't identify the items of Draconic Evolution as materials.");
        add("nbtech2fix.configuration.FixAECraftNBT.tooltip", "The items of Draconic Evolution are made with special nbt, which makes ae unable to identify them as materials for synthesis. Add this option to select DISABLED: Off; DE_ONLY: nbt is ignored only for the study of dragons; ALL: ignore nbt for all items;");

        add("nbtech2fix.configuration.CommandConfigPremissionLevel","[Self] 'config' command permission level");
        add("nbtech2fix.configuration.CommandConfigPremissionLevel.tooltip","This option is used to adjust the permission level required for the subcommand 'config' under /nbtech2fix added by this mod.No reload is required, but the client cache needs to be refreshed—for example, by rejoining the world.");

        add("nbtech2fix.configuration.CommandToolsPermissionLevel","[Self] 'tools' command permission level");
        add("nbtech2fix.configuration.CommandToolsPermissionLevel.tooltip","This option is used to adjust the permission level required for the subcommand 'tools' under /nbtech2fix added by this mod.No reload is required, but the client cache needs to be refreshed—for example, by rejoining the world.");

        add("nbtech2fix.configuration.FixPackagedDEDoesntUseInput","[Fix]Packaged Draconic doesn't use real input");
        add("nbtech2fix.configuration.FixPackagedDEDoesntUseInput.tooltip","After into Packaged Draconic inputting items, the actual synthesis uses the items in the recipe, and the actual input will be directly discarded. This option is provided to fix this issue. If you call Packaged Draconic for automated synthesis in AE, enabling this option can prevent the module from disappearing.");

        add("nbtech2fix.configuration.FixObNetStuck","[Fix] Observable stuck");
        add("nbtech2fix.configuration.FixObNetStuck.tooltip","When Observable gets stuck during upload, it will prevent the debugging results from being viewed. Adding this option fixes the issue. You can choose DISABLED to turn off the fix, TIMEOUT to interrupt the upload upon timeout, or NO_UPLOAD to prohibit the upload.");

        add("nbtech2fix.configuration.ObservableTimeoutTime","TIMEOUT upload timeout duration");
        add("nbtech2fix.configuration.ObservableTimeoutTime.tooltip","When \"[Fix] Observable stuck\" is set to TIMEOUT, you can configure the timeout duration here. The default is 5000 milliseconds (ms).");


        add("nbtech2fix.commands.CheckAllThreads.ThreadCount", "Current active thread count: %s");
        add("nbtech2fix.commands.CheckAllThreads.ThreadList", "=== Thread List ===");
    }

}
