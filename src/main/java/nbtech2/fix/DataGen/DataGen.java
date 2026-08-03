package nbtech2.fix.DataGen;

import nbtech2.fix.NBTech2fix;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = NBTech2fix.MODID)
public class DataGen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();

        generator.addProvider(event.includeClient(),
                new ModLangProvider(output, NBTech2fix.MODID, "en_us"));

        generator.addProvider(event.includeClient(),
                new ModLangProvider(output, NBTech2fix.MODID, "zh_cn"));

    }
}
