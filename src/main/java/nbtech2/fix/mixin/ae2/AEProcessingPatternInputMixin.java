package nbtech2.fix.mixin.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import nbtech2.fix.config.Config;
import nbtech2.fix.util.AEHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "appeng.crafting.pattern.AEProcessingPattern$Input", remap = false)
public abstract class AEProcessingPatternInputMixin {
    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void nbtech2fix$allowConfiguredNbtVariant(
            AEKey input,
            Level level,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (Config.FIX_AE_CRAFT_NBT.get() == Config.AEIgnoreNbt.DISABLED) {
            return;
        }

        GenericStack[] possibleInputs = ((IPatternDetails.IInput) (Object) this).getPossibleInputs();
        if (possibleInputs.length > 0
                && AEHelper.matchesWithConfiguredNbtPolicy(possibleInputs[0].what(), input)) {
            cir.setReturnValue(true);
        }
    }
}
