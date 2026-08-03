package nbtech2.fix;

import nbtech2.fix.Command.NBCommands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = NBTech2fix.MODID)
public class Register {
    @SubscribeEvent
    public static void RegisterCommand(RegisterCommandsEvent event){
        NBCommands.register(event.getDispatcher());
    }
}
