package nbtech2.fix.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.function.BooleanSupplier;

public final class ConfigEntries {
    public static final List<Entry> ALL = List.of(
            entry("Fix_DE_Dislocator_Thread_Bug", Config.FIX_DE_DISLOCATOR_THREAD_BUG),
            entry("Fix_BrandonsCore_Nullptr", Config.FIX_BRANDONS_CORE_NULL),
            entry("Fix_MMSM_Energy_Shield_AntiDie_BUG", Config.FIX_MMSM_SHIELD_WITHOUT_MODULE),
            entry("Fix_DE_HUD_Render_Bug", Config.FIX_DE_HUD),
            entry("Fix_DE_Curios_Order_Bug", Config.FIX_DE_CURIOS_ORDER),
            entry("Fix_Player_Shells_Die_Priority", Config.FIX_PLAYER_SHELLS_DEATH_PRIORITY),
            entry("Fix_AE_Craft_NBT", Config.FIX_AE_CRAFT_NBT),
            entry("Fix_PackagedDE_Doesnt_Use_Input", Config.FIX_PACKAGED_DE_REAL_INPUT),
            entry("Fix_Observable_Net_Stuck", Config.FIX_OBSERVABLE_NETWORK),
            intEntry(
                    "ObservableUploadTimeoutTime",
                    Config.OBSERVABLE_UPLOAD_TIMEOUT_MS,
                    0,
                    Integer.MAX_VALUE,
                    () -> Config.FIX_OBSERVABLE_NETWORK.get() == Config.ObservableNetworkFix.TIMEOUT
            ),
            entry("Fix_Observable_Diagnostics", Config.FIX_OBSERVABLE_DIAGNOSTICS),
            intEntry(
                    "ObservableDiagnosticsTimeoutTime",
                    Config.OBSERVABLE_DIAGNOSTICS_TIMEOUT_MS,
                    0,
                    Integer.MAX_VALUE,
                    Config.FIX_OBSERVABLE_DIAGNOSTICS::get
            ),
            intEntry("CommandConfigPermissionLevel", Config.CONFIG_COMMAND_PERMISSION, 0, 4),
            intEntry("CommandToolsPermissionLevel", Config.TOOLS_COMMAND_PERMISSION, 0, 4)
    );

    private ConfigEntries() {
    }

    private static Entry entry(String commandName, ForgeConfigSpec.ConfigValue<?> value) {
        return new Entry(commandName, value, Integer.MIN_VALUE, Integer.MAX_VALUE, () -> true);
    }

    private static Entry intEntry(
            String commandName,
            ForgeConfigSpec.IntValue value,
            int minimum,
            int maximum
    ) {
        return intEntry(commandName, value, minimum, maximum, () -> true);
    }

    private static Entry intEntry(
            String commandName,
            ForgeConfigSpec.IntValue value,
            int minimum,
            int maximum,
            BooleanSupplier activeWhen
    ) {
        return new Entry(commandName, value, minimum, maximum, activeWhen);
    }

    public record Entry(
            String commandName,
            ForgeConfigSpec.ConfigValue<?> value,
            int minimum,
            int maximum,
            BooleanSupplier activeWhen
    ) {
        public String configPath() {
            List<String> path = value.getPath();
            return path.get(path.size() - 1);
        }

        public String translationKey() {
            return "nbtech2fix.configuration." + configPath();
        }

        public String tooltipKey() {
            return translationKey() + ".tooltip";
        }

        public boolean isActive() {
            return activeWhen.getAsBoolean();
        }
    }
}
