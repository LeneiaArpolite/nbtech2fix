package nbtech2.fix.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import nbtech2.fix.config.Config;
import nbtech2.fix.config.ConfigEntries;
import nbtech2.fix.config.ConfigEntries.Entry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

public final class NBCommands {
    private NBCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var configCommand = Commands.literal("config")
                .requires(source -> source.hasPermission(Config.CONFIG_COMMAND_PERMISSION.get()));

        for (Entry entry : ConfigEntries.ALL) {
            configCommand.then(Commands.literal(entry.commandName())
                    .executes(context -> getConfig(context, entry))
                    .then(Commands.literal("get")
                            .executes(context -> getConfig(context, entry)))
                    .then(Commands.literal("set")
                            .then(Commands.argument("value", StringArgumentType.word())
                                    .suggests((context, builder) -> suggestValues(entry, builder))
                                    .executes(context -> setConfig(context, entry)))));
        }

        dispatcher.register(Commands.literal("nbtech2fix")
                .then(configCommand)
                .then(Commands.literal("tools")
                        .requires(source -> source.hasPermission(Config.TOOLS_COMMAND_PERMISSION.get()))
                        .then(Commands.literal("CheckAllThreads")
                                .executes(NBCommands::checkAllThreads))));
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestValues(
            Entry entry,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        ForgeConfigSpec.ConfigValue<?> value = entry.value();
        if (value instanceof ForgeConfigSpec.BooleanValue) {
            builder.suggest("true").suggest("false");
        } else if (value instanceof ForgeConfigSpec.EnumValue<?> enumValue) {
            Arrays.stream(enumValue.get().getClass().getEnumConstants())
                    .map(constant -> ((Enum<?>) constant).name())
                    .forEach(builder::suggest);
        }
        return builder.buildFuture();
    }

    private static int getConfig(CommandContext<CommandSourceStack> context, Entry entry) {
        context.getSource().sendSuccess(
                () -> Component.translatable(
                        "nbtech2fix.commands.config.value",
                        entry.commandName(),
                        entry.value().get()
                ),
                false
        );
        return 1;
    }

    private static int setConfig(CommandContext<CommandSourceStack> context, Entry configEntry) {
        String rawValue = StringArgumentType.getString(context, "value");
        ForgeConfigSpec.ConfigValue<?> entry = configEntry.value();

        try {
            if (entry instanceof ForgeConfigSpec.BooleanValue booleanValue) {
                if (!rawValue.equalsIgnoreCase("true") && !rawValue.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException("Expected true or false");
                }
                booleanValue.set(Boolean.parseBoolean(rawValue));
            } else if (entry instanceof ForgeConfigSpec.IntValue intValue) {
                intValue.set(Integer.parseInt(rawValue));
            } else if (entry instanceof ForgeConfigSpec.EnumValue<?> enumValue) {
                setEnum(enumValue, rawValue);
            } else {
                throw new IllegalArgumentException("Unsupported config value type");
            }
            entry.save();
        } catch (RuntimeException exception) {
            context.getSource().sendFailure(
                    Component.translatable(
                            "nbtech2fix.commands.config.invalid",
                            configEntry.commandName(),
                            rawValue
                    )
            );
            return 0;
        }

        context.getSource().sendSuccess(
                () -> Component.translatable(
                        "nbtech2fix.commands.config.updated",
                        configEntry.commandName(),
                        entry.get()
                ),
                true
        );
        return 1;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setEnum(ForgeConfigSpec.EnumValue enumValue, String rawValue) {
        for (Object constant : enumValue.get().getClass().getEnumConstants()) {
            Enum<?> enumConstant = (Enum<?>) constant;
            if (enumConstant.name().equalsIgnoreCase(rawValue)) {
                enumValue.set(enumConstant);
                return;
            }
        }
        throw new IllegalArgumentException("Unknown enum value");
    }

    private static int checkAllThreads(CommandContext<CommandSourceStack> context) {
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        context.getSource().sendSuccess(
                () -> Component.translatable("nbtech2fix.commands.CheckAllThreads.ThreadCount", threads.size()),
                false
        );
        context.getSource().sendSuccess(
                () -> Component.translatable("nbtech2fix.commands.CheckAllThreads.ThreadList"),
                false
        );
        threads.stream()
                .sorted(Comparator.comparing(Thread::getName))
                .forEach(thread -> context.getSource().sendSuccess(
                        () -> Component.literal("- " + thread.getName() + " [" + thread.getState() + "]"),
                        false
                ));
        return threads.size();
    }
}
