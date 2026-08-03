package nbtech2.fix.mixin.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.pattern.AEProcessingPattern;
import nbtech2.fix.config.Config;
import nbtech2.fix.util.AEHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AEProcessingPattern.class, remap = false)
public abstract class AEProcessingPatternMixin {
    @Inject(method = "pushInputsToExternalInventory", at = @At("HEAD"), cancellable = true)
    private void nbtech2fix$pushActualNbtVariants(
            KeyCounter[] inputHolder,
            IPatternDetails.PatternInputSink inputSink,
            CallbackInfo ci
    ) {
        if (Config.FIX_AE_CRAFT_NBT.get() == Config.AEIgnoreNbt.DISABLED) {
            return;
        }

        AEProcessingPattern pattern = (AEProcessingPattern) (Object) this;
        GenericStack[] sparseInputs = pattern.getSparseInputs();
        if (sparseInputs.length == pattern.getInputs().length) {
            return;
        }

        KeyCounter allInputs = new KeyCounter();
        for (KeyCounter counter : inputHolder) {
            allInputs.addAll(counter);
        }

        for (GenericStack sparseInput : sparseInputs) {
            if (sparseInput == null) {
                continue;
            }

            AEKey expected = sparseInput.what();
            long required = sparseInput.amount();
            long found = 0;
            for (AEHelper.KeyAmount match : AEHelper.matchingAmounts(allInputs, expected, required)) {
                inputSink.pushInput(match.key(), match.amount());
                allInputs.remove(match.key(), match.amount());
                found += match.amount();
            }
            if (found < required) {
                throw new IllegalStateException(
                        "Expected at least %d of %s when pushing a fuzzy-matched pattern, but only %d was available"
                                .formatted(required, expected, found)
                );
            }
        }

        ci.cancel();
    }
}
