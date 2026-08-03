package nbtech2.fix;

import com.mojang.logging.LogUtils;
import nbtech2.fix.Config.Config;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(NBTech2fix.MODID)
public final class NBTech2fix {
    public static final String MODID = "nbtech2fix";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NBTech2fix(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
