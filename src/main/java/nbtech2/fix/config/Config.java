package nbtech2.fix.config;

import net.minecraftforge.common.ForgeConfigSpec;

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

    public enum AEIgnoreNbt {
        DISABLED,
        DE_ONLY,
        ALL
    }

    public enum ObservableNetworkFix {
        DISABLED,
        TIMEOUT,
        NO_UPLOAD
    }
}
