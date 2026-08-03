package nbtech2.fix.Config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue Fix_DE_Dislocator_Thread_Bug = BUILDER
            .define("FixDEDislocatorThreadBug",true);

    public static final ModConfigSpec.BooleanValue Fix_BrandonsCore_Nullptr = BUILDER
            .define("FixBrandonsCoreNullptr",true);

    public static final ModConfigSpec.BooleanValue Fix_MMSM_Swim_In_Air = BUILDER
            .define("FixMMSMSwimInAir",true);

    public static final ModConfigSpec.BooleanValue Fix_MMSM_Energy_Shield_AntiDie_BUG = BUILDER
            .define("FixMMSMEnergyShieldAntiDieBug",true);

    public static final ModConfigSpec.BooleanValue Fix_MMSM_Energy_Shield_Cost_BUG = BUILDER
            .define("FixMMSM",true);

    public static final ModConfigSpec.EnumValue<PhaseCameraMode> Fix_Phase_Camera = BUILDER
            .defineEnum("FixPhaseCamera",PhaseCameraMode.PHASE_ONLY);

    public enum PhaseCameraMode{
        DISABLED,
        PHASE_ONLY,
        ALWAYS;
    }

    public static final ModConfigSpec.BooleanValue Fix_DE_HUD_Render_Bug = BUILDER
            .define("FixDEHUDRenderBug",true);

    public static final ModConfigSpec.BooleanValue Fix_DE_Curios_Order_Bug = BUILDER
            .define("FixDECuriosOrderBug",true);

    public static final ModConfigSpec.BooleanValue Fix_Player_Shells_Die_Priority = BUILDER
            .define("FixPlayerShellsDiePriority",true);

    public static final ModConfigSpec.EnumValue<AEIgnoreNBT> Fix_AE_Craft_NBT = BUILDER
            .defineEnum("FixAECraftNBT",AEIgnoreNBT.DE_ONLY);

    public enum AEIgnoreNBT{
        DISABLED,
        DE_ONLY,
        ALL;
    }


    public static final ModConfigSpec.IntValue Command_config_Permission_Level = BUILDER
            .defineInRange("CommandConfigPremissionLevel",3,Integer.MIN_VALUE,Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue Command_tools_Permission_Level = BUILDER
            .defineInRange("CommandToolsPermissionLevel",4,Integer.MIN_VALUE,Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue Fix_PackagedDE_Doesnt_Use_Input = BUILDER
            .define("FixPackagedDEDoesntUseInput",true);

    public static final ModConfigSpec.EnumValue<ObNetStuckFix> Fix_Observable_Net_Stuck = BUILDER
            .defineEnum("FixObNetStuck",ObNetStuckFix.NO_UPLOAD);

    public enum ObNetStuckFix{
        DISABLED,
        TIMEOUT,
        NO_UPLOAD
    }

    public static final ModConfigSpec.IntValue Observable_Upload_Timeout_Time = BUILDER
            .defineInRange("ObservableUploadTimeoutTime", 3000, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue Fix_Observable_No_oshi_File_Stuck = BUILDER
            .define("FixObNoOshiFileStuck",true);

    public static final ModConfigSpec.IntValue Observable_No_File_Timeout_Time = BUILDER
            .defineInRange("ObNoFileTimeoutTime",3000, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec SPEC = BUILDER.build();

}
