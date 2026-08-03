package nbtech2.fix.command;

import nbtech2.fix.NBTech2Fix;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NBTech2Fix.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommandRegistration {
    private CommandRegistration() {
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        NBCommands.register(event.getDispatcher());
    }
}
