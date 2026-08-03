package nbtech2.fix.Mixin.PkgAuto.FixAECraftNBT;

import org.spongepowered.asm.mixin.Mixin;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import nbtech2.fix.Config.Config;
import nbtech2.fix.Util.AEHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = thelm.packagedauto.integration.appeng.recipe.SimpleInput.class,remap = false)
public abstract class MixinSimpleInput {

    @Inject(method = "isValid(Lappeng/api/stacks/AEKey;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"), cancellable = true)
    private void onIsValid(AEKey input, Level level, CallbackInfoReturnable<Boolean> cir) {
        Config.AEIgnoreNBT configValue = Config.Fix_AE_Craft_NBT.get();
        if (configValue != Config.AEIgnoreNBT.DISABLED) {

            IPatternDetails.IInput self = (IPatternDetails.IInput) (Object) this;
            GenericStack[] possibleInputs = self.getPossibleInputs();

            if (possibleInputs != null && possibleInputs.length > 0) {
                if (AEHelper.isIgnoreNBT(possibleInputs[0].what(), input, configValue)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
