package nbtech2.fix;

import com.mojang.logging.LogUtils;
import nbtech2.fix.client.ClientConfigScreenRegistration;
import nbtech2.fix.config.Config;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(NBTech2Fix.MOD_ID)
public final class NBTech2Fix {
    public static final String MOD_ID = "nbtech2fix";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NBTech2Fix() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientConfigScreenRegistration::register);
    }
}
