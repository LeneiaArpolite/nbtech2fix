package nbtech2.fix.Command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import nbtech2.fix.Config.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class NBCommands {
    private static final boolean ALLOW_LOGGING = true;
    private static final List<String> CONFIG_ITEMS = List.of(
            "Fix_DE_Dislocator_Thread_Bug",
            "Fix_BrandonsCore_Nullptr",
            "Fix_MMSM_Swim_In_Air",
            "Fix_MMSM_Energy_Shield_AntiDie_BUG",
            "Fix_MMSM_Energy_Shield_Cost_BUG",
            "Fix_Phase_Camera",
            "Fix_DE_HUD_Render_Bug",
            "Fix_DE_Curios_Order_Bug",
            "Fix_Player_Shells_Die_Priority",
            "Fix_AE_Craft_NBT",
            "Fix_PackagedDE_Doesnt_Use_Input",
            "Fix_Observable_Net_Stuck",
            "ObservableTimeoutTime"
    );

    private NBCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("nbtech2fix")
                        .then(Commands.literal("config")
                                .requires(source -> source.hasPermission(Config.Command_config_Permission_Level.get()))
                                .then(Commands.argument("item", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(CONFIG_ITEMS, builder))
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("value", StringArgumentType.word())
                                                        .suggests((context, builder) -> suggestConfigValues(context, builder))
                                                        .executes(NBCommands::configSet)))
                                        .then(Commands.literal("get").executes(NBCommands::configGet))))
                        .then(Commands.literal("tools")
                                .requires(source -> source.hasPermission(Config.Command_tools_Permission_Level.get()))
                                .then(Commands.literal("CheckAllThreads").executes(NBCommands::checkAllThreads)))
        );
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestConfigValues(
            CommandContext<CommandSourceStack> context,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        return switch (StringArgumentType.getString(context, "item")) {
            case "Fix_Phase_Camera" -> suggestEnum(Config.PhaseCameraMode.values(), builder);
            case "Fix_AE_Craft_NBT" -> suggestEnum(Config.AEIgnoreNBT.values(), builder);
            case "Fix_Observable_Net_Stuck" -> suggestEnum(Config.ObNetStuckFix.values(), builder);
            default -> SharedSuggestionProvider.suggest(List.of("true", "false"), builder);
        };
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestEnum(
            Enum<?>[] values,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        Arrays.stream(values).map(Enum::name).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static int configSet(CommandContext<CommandSourceStack> context) {
        String key = StringArgumentType.getString(context, "item");
        String content = StringArgumentType.getString(context, "value");
        try {
            Field field = Config.class.getDeclaredField(key);
            field.setAccessible(true);
            Object configObject = field.get(null);
            if (configObject instanceof ModConfigSpec.EnumValue<?> enumValue) {
                for (Object constant : enumValue.get().getClass().getEnumConstants()) {
                    Enum<?> enumConstant = (Enum<?>) constant;
                    if (enumConstant.name().equalsIgnoreCase(content)) {
                        @SuppressWarnings({"rawtypes", "unchecked"})
                        ModConfigSpec.EnumValue rawValue = enumValue;
                        rawValue.set(enumConstant);
                        return 1;
                    }
                }
                return 0;
            }
            if (configObject instanceof ModConfigSpec.BooleanValue value) {
                value.set(Boolean.parseBoolean(content));
            } else if (configObject instanceof ModConfigSpec.DoubleValue value) {
                value.set(Double.parseDouble(content));
            } else if (configObject instanceof ModConfigSpec.IntValue value) {
                value.set(Integer.parseInt(content));
            } else if (configObject instanceof ModConfigSpec.LongValue value) {
                value.set(Long.parseLong(content));
            } else {
                return 0;
            }
            return 1;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static int configGet(CommandContext<CommandSourceStack> context) {
        String key = StringArgumentType.getString(context, "item");
        try {
            Field field = Config.class.getDeclaredField(key);
            field.setAccessible(true);
            String result = ((ModConfigSpec.ConfigValue<?>) field.get(null)).get().toString();
            context.getSource().sendSuccess(() -> Component.literal(key + ":" + result), ALLOW_LOGGING);
            return 1;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static int checkAllThreads(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        source.sendSuccess(
                () -> Component.translatable("nbtech2fix.commands.CheckAllThreads.ThreadCount", threads.size()),
                ALLOW_LOGGING
        );
        source.sendSuccess(
                () -> Component.translatable("nbtech2fix.commands.CheckAllThreads.ThreadList"),
                ALLOW_LOGGING
        );
        for (Thread thread : threads) {
            source.sendSuccess(
                    () -> Component.literal("- " + thread.getName() + " [" + thread.getState() + "]"),
                    ALLOW_LOGGING
            );
        }
        return threads.size();
    }
}
