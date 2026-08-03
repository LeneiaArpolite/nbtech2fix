package nbtech2.fix.mixin.brandonscore;

import com.brandon3055.brandonscore.handlers.IProcess;
import nbtech2.fix.NBTech2Fix;
import nbtech2.fix.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.brandon3055.brandonscore.client.ProcessHandlerClient", remap = false)
public abstract class ProcessHandlerClientMixin {
    @Redirect(
            method = "onClientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/brandonscore/handlers/IProcess;isDead()Z",
                    remap = false
            ),
            remap = false
    )
    private boolean nbtech2fix$treatNullProcessAsDead(IProcess process) {
        if (!Config.FIX_BRANDONS_CORE_NULL.get()) {
            return process.isDead();
        }
        if (process == null) {
            NBTech2Fix.LOGGER.warn("Removed a null process from BrandonsCore's client process list");
            return true;
        }
        return process.isDead();
    }
}
