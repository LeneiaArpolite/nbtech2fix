package nbtech2.fix.Mixin.DE.FixBrandonsCoreNullptr;

import nbtech2.fix.NBTech2fix;
import nbtech2.fix.Config.Config;
import com.brandon3055.brandonscore.client.ProcessHandlerClient;
import com.brandon3055.brandonscore.handlers.IProcess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ProcessHandlerClient.class)
public class MixinProcessHandlerClient {

    @Redirect(
            method = "onClientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/brandonscore/handlers/IProcess;isDead()Z",
                    remap = false
            )
    )
    private boolean onCheckIsDead(IProcess instance) {

        if (!Config.Fix_BrandonsCore_Nullptr.getAsBoolean()) {
            return instance.isDead();
        }

        if (instance == null) {
            NBTech2fix.LOGGER.warn("BrandonsCore又tm的nullptr了");

            return true;
        }

        return instance.isDead();
    }

}
