package nbtech2.fix.mixin.mmsm;

import moremekasuitmodules.common.ShieldProviderHandler;
import nbtech2.fix.config.Config;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "moremekasuitmodules.common.ShieldProviderHandler", remap = false)
public abstract class ShieldProviderHandlerMixin {
    @Inject(
            method = "onPlayerDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lmoremekasuitmodules/common/item/interfaces/IShieldProvider;modifyEnergy(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/entity/LivingEntity;)V",
                    ordinal = 0,
                    remap = false
            ),
            cancellable = true,
            remap = false
    )
    private void nbtech2fix$requireEnabledShieldForLastStand(LivingDeathEvent event, CallbackInfo callback) {
        if (!Config.FIX_MMSM_SHIELD_WITHOUT_MODULE.get()) {
            return;
        }
        if (event.getEntity() instanceof Player player
                && !((ShieldProviderHandler) (Object) this).getShieldState(player)) {
            callback.cancel();
        }
    }
}
