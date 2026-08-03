package nbtech2.fix.mixin.playershells;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class PlayerShellsMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String SUPPORTED_VERSION = "1.20.1-0.0.9";

    private boolean enabled;

    @Override
    public void onLoad(String mixinPackage) {
        ModInfo playerShells = LoadingModList.get().getMods().stream()
                .filter(mod -> "playershells".equals(mod.getModId()))
                .findFirst()
                .orElse(null);

        if (playerShells == null) {
            enabled = false;
            return;
        }

        String version = playerShells.getVersion().toString();
        enabled = SUPPORTED_VERSION.equals(version);
        if (!enabled) {
            LOGGER.warn("Skipping Player Shells death-priority fix: supported version is {}, found {}",
                    SUPPORTED_VERSION, version);
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return enabled && mixinClassName.endsWith(".PlayerShellsDeathPriorityMixin");
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return enabled ? List.of("PlayerShellsDeathPriorityMixin") : List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
