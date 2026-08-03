package nbtech2.fix.Mixin.MMSM.FixMMSMSwimInAir;

import nbtech2.fix.Config.Config;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MixinPlayer {


    @Inject(
            method = "updatePlayerPose",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fixSwimInAir(CallbackInfo ci) {
        Player player = (Player)(Object)this;

        if (!Config.Fix_MMSM_Swim_In_Air.getAsBoolean()) return;

        if (player.noPhysics && !player.onGround()) {

            if (player.getPose() == Pose.SWIMMING) {
                player.setPose(Pose.STANDING);
            }

            ci.cancel();
        }
    }
}
