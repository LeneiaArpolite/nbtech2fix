package nbtech2.fix.Mixin.MMSM.FixPhaseCamera;

import nbtech2.fix.Config.Config;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class MixinCamera {
    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void disableCameraClip(float startingDistance, CallbackInfoReturnable<Float> cir) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) return;

        Config.PhaseCameraMode mode = Config.Fix_Phase_Camera.get();

        boolean shouldDisable = switch (mode) {
            case DISABLED -> false;
            case ALWAYS -> true;
            case PHASE_ONLY -> player.noPhysics && !player.onGround();
        };

        if (shouldDisable) {
            cir.setReturnValue(startingDistance);
        }
    }
}
