package nbtech2.fix.mixin;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class OptionalMixinPlugin implements IMixinConfigPlugin {
    private Set<String> loadedMods = Set.of();

    @Override
    public void onLoad(String mixinPackage) {
        loadedMods = LoadingModList.get().getMods().stream()
                .map(ModInfo::getModId)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return requiredMod(mixinClassName).map(loadedMods::contains).orElse(true);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        List<String> mixins = new ArrayList<>();

        if (loadedMods.contains("draconicevolution")) {
            mixins.add("draconicevolution.DislocatorThreadMixin");
            mixins.add("draconicevolution.ModularArmorEventHandlerMixin");
            if (FMLEnvironment.dist == Dist.CLIENT) {
                mixins.add("draconicevolution.ShieldHudElementMixin");
            }
        }
        if (FMLEnvironment.dist == Dist.CLIENT && loadedMods.contains("brandonscore")) {
            mixins.add("brandonscore.ProcessHandlerClientMixin");
        }
        if (loadedMods.contains("moremekasuitmodules")) {
            mixins.add("mmsm.ShieldProviderHandlerMixin");
        }
        if (loadedMods.contains("observable")) {
            mixins.add("observable.ProfilerMixin");
        }
        if (loadedMods.contains("ae2")) {
            mixins.add("ae2.AEProcessingPatternInputMixin");
            mixins.add("ae2.AEProcessingPatternMixin");
            mixins.add("ae2.CraftingCpuLogicMixin");
        }
        if (loadedMods.contains("ae2") && loadedMods.contains("advanced_ae")) {
            mixins.add("advancedae.AdvCraftingCpuLogicMixin");
        }
        if (loadedMods.contains("ae2") && loadedMods.contains("neoecoae")) {
            mixins.add("neoecoae.ECOExecutingCraftingJobAccessor");
            mixins.add("neoecoae.ECOCraftingCpuLogicMixin");
        }
        if (loadedMods.contains("ae2") && loadedMods.contains("packagedauto")) {
            mixins.add("packagedauto.SimpleInputMixin");
            mixins.add("packagedauto.AEPackagingProviderBlockEntityMixin");
        }
        if (loadedMods.contains("packagedauto") && loadedMods.contains("packageddraconic")) {
            mixins.add("packageddraconic.FusionCrafterBlockEntityMixin");
        }

        return List.copyOf(mixins);
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private static java.util.Optional<String> requiredMod(String mixinClassName) {
        if (mixinClassName.contains(".ae2.")) return java.util.Optional.of("ae2");
        if (mixinClassName.contains(".advancedae.")) return java.util.Optional.of("advanced_ae");
        if (mixinClassName.contains(".neoecoae.")) return java.util.Optional.of("neoecoae");
        if (mixinClassName.contains(".brandonscore.")) return java.util.Optional.of("brandonscore");
        if (mixinClassName.contains(".draconicevolution.")) return java.util.Optional.of("draconicevolution");
        if (mixinClassName.contains(".mmsm.")) return java.util.Optional.of("moremekasuitmodules");
        if (mixinClassName.contains(".observable.")) return java.util.Optional.of("observable");
        if (mixinClassName.contains(".packagedauto.")) return java.util.Optional.of("packagedauto");
        if (mixinClassName.contains(".packageddraconic.")) return java.util.Optional.of("packageddraconic");
        return java.util.Optional.empty();
    }
}
