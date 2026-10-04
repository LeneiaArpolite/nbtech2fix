package nbtech2.fix.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue FIX_DE_DISLOCATOR_THREAD_BUG = BUILDER
            .define("FixDEDislocatorThreadBug", true);
    public static final ForgeConfigSpec.BooleanValue FIX_BRANDONS_CORE_NULL = BUILDER
            .define("FixBrandonsCoreNullptr", true);
    public static final ForgeConfigSpec.BooleanValue FIX_MMSM_SHIELD_WITHOUT_MODULE = BUILDER
            .define("FixMMSMEnergyShieldAntiDieBug", true);
    public static final ForgeConfigSpec.BooleanValue FIX_DE_HUD = BUILDER
            .define("FixDEHUDRenderBug", true);
    public static final ForgeConfigSpec.BooleanValue FIX_DE_CURIOS_ORDER = BUILDER
            .define("FixDECuriosOrderBug", true);
    public static final ForgeConfigSpec.BooleanValue FIX_PLAYER_SHELLS_DEATH_PRIORITY = BUILDER
            .define("FixPlayerShellsDiePriority", true);
    public static final ForgeConfigSpec.EnumValue<AEIgnoreNbt> FIX_AE_CRAFT_NBT = BUILDER
            .defineEnum("FixAECraftNBT", AEIgnoreNbt.DE_ONLY);
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> FIX_AE_CRAFT_NBT_LIST = BUILDER
            .comment("Only used when FixAECraftNBT = LIST. Item namespaces whose NBT differences are ignored.")
            .defineListAllowEmpty(
                    "FixAECraftNBTList",
                    List.of("draconicevolution", "minecraft"),
                    Config::isValidNamespace
            );
    public static final ForgeConfigSpec.BooleanValue FIX_PACKAGED_DE_REAL_INPUT = BUILDER
            .define("FixPackagedDEDoesntUseInput", true);
    public static final ForgeConfigSpec.EnumValue<ObservableNetworkFix> FIX_OBSERVABLE_NETWORK = BUILDER
            .defineEnum("FixObNetStuck", ObservableNetworkFix.NO_UPLOAD);
    public static final ForgeConfigSpec.IntValue OBSERVABLE_UPLOAD_TIMEOUT_MS = BUILDER
            .defineInRange("ObservableUploadTimeoutTime", 3000, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.BooleanValue FIX_OBSERVABLE_DIAGNOSTICS = BUILDER
            .define("FixObNoOshiFileStuck", true);
    public static final ForgeConfigSpec.IntValue OBSERVABLE_DIAGNOSTICS_TIMEOUT_MS = BUILDER
            .defineInRange("ObNoFileTimeoutTime", 3000, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue CONFIG_COMMAND_PERMISSION = BUILDER
            .defineInRange("CommandConfigPermissionLevel", 3, 0, 4);
    public static final ForgeConfigSpec.IntValue TOOLS_COMMAND_PERMISSION = BUILDER
            .defineInRange("CommandToolsPermissionLevel", 4, 0, 4);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    /**
     * 该配置路径是否为"命名空间列表"（配置界面据此决定是否用逗号分隔的编辑框）。
     */
    public static boolean isNamespaceListPath(String configPath) {
        return "FixAECraftNBTList".equals(configPath);
    }

    public static boolean isValidNamespace(Object value) {
        if (!(value instanceof String text)) {
            return false;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty() || trimmed.contains(":")) {
            return false;
        }
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (!(c >= 'a' && c <= 'z') && !(c >= '0' && c <= '9') && c != '_' && c != '-' && c != '.') {
                return false;
            }
        }
        return true;
    }

    public enum AEIgnoreNbt {
        DISABLED,
        DE_ONLY,
        ALL,
        /**
         * 仅对 {@link #FIX_AE_CRAFT_NBT_LIST} 中列出的命名空间忽略 NBT 差异。
         * 追加在末尾，保证 DISABLED / DE_ONLY / ALL 的 ordinal 不变。
         */
        LIST
    }

    public enum ObservableNetworkFix {
        DISABLED,
        TIMEOUT,
        NO_UPLOAD
    }
}
