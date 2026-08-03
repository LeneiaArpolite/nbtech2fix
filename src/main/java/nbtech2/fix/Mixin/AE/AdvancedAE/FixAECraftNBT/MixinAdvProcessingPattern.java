package nbtech2.fix.Mixin.AE.AdvancedAE.FixAECraftNBT;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import nbtech2.fix.Config.Config;
import nbtech2.fix.Util.AEHelper;
import net.pedroksl.advanced_ae.common.patterns.AdvProcessingPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AdvProcessingPattern.class)
public class MixinAdvProcessingPattern {
    @Shadow(remap = false)
    private List<GenericStack> sparseInputs;

    @Inject(method = "pushInputsToExternalInventory", at = @At("HEAD"), cancellable = true, remap = false)
    private void customPushInputs(KeyCounter[] inputHolder, IPatternDetails.PatternInputSink inputSink, CallbackInfo ci) {
        Config.AEIgnoreNBT configValue = Config.Fix_AE_Craft_NBT.get();
        if (configValue == Config.AEIgnoreNBT.DISABLED) return;

        KeyCounter allInputs = new KeyCounter();
        for (KeyCounter counter : inputHolder) {
            allInputs.addAll(counter);
        }

        for (GenericStack sparseInput : this.sparseInputs) {
            if (sparseInput != null) {
                AEKey expectedKey = sparseInput.what();
                long amountNeeded = sparseInput.amount();
                long remainingToFind = amountNeeded;

                List<AEKey> keysToDeduct = new ArrayList<>();
                List<Long> amountsToDeduct = new ArrayList<>();

                for (Object2LongMap.Entry<AEKey> entry : allInputs) {
                    if (remainingToFind <= 0) break;

                    AEKey availableKey = entry.getKey();
                    if (AEHelper.isIgnoreNBT(expectedKey, availableKey, configValue)) {
                        long availableAmount = entry.getLongValue();
                        long take = Math.min(remainingToFind, availableAmount);

                        inputSink.pushInput(availableKey, take);
                        keysToDeduct.add(availableKey);
                        amountsToDeduct.add(take);
                        remainingToFind -= take;
                    }
                }

                for (int i = 0; i < keysToDeduct.size(); i++) {
                    allInputs.remove(keysToDeduct.get(i), amountsToDeduct.get(i));
                }

                if (remainingToFind > 0) {
                    throw new RuntimeException("Expected at least %d of %s (fuzzy matched) when pushing pattern, but only found partial".formatted(amountNeeded, expectedKey));
                }
            }
        }

        ci.cancel();
    }
}
