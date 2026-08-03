package nbtech2.fix.Mixin.AE.FixAECraftNBT;

import nbtech2.fix.Config.Config;
import nbtech2.fix.Util.AEHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.level.Level;

@Mixin(targets = "appeng.crafting.pattern.AEProcessingPattern$Input")
public abstract class MixinAEProcessingPatternInput {

    @Shadow(remap = false)
    @Final
    private GenericStack[] template;

    @Inject(method = "isValid(Lappeng/api/stacks/AEKey;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void onIsValid(AEKey input, Level level, CallbackInfoReturnable<Boolean> cir) {
        Config.AEIgnoreNBT configValue = Config.Fix_AE_Craft_NBT.get();
        if (configValue != Config.AEIgnoreNBT.DISABLED) {
            if (AEHelper.isIgnoreNBT(this.template[0].what(), input, configValue)) {
                cir.setReturnValue(true);
            }
        }
    }
}
