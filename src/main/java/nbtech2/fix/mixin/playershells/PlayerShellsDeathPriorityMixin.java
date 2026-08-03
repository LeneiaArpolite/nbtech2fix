package nbtech2.fix.mixin.playershells;

import com.mojang.authlib.GameProfile;
import com.ultramega.playershells.Config;
import com.ultramega.playershells.storage.ShellSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class, priority = 500)
public abstract class PlayerShellsDeathPriorityMixin extends Player {
    protected PlayerShellsDeathPriorityMixin(Level level, BlockPos position, float yRot, GameProfile profile) {
        super(level, position, yRot, profile);
    }

    @Shadow
    public abstract ServerLevel serverLevel();

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void nbtech2fix$runDeathHooksBeforeShellTransfer(DamageSource source, CallbackInfo callback) {
        if (!nbtech2.fix.config.Config.FIX_PLAYER_SHELLS_DEATH_PRIORITY.get()) {
            return;
        }
        if (!Config.TRANSFER_INTO_SHELL_AFTER_DEATH.get()) {
            return;
        }
        if (ShellSavedData.getShellData(serverLevel()).getNearestActive(
                getUUID(), level().dimension().location(), blockPosition()) == null) {
            return;
        }
        if (ForgeHooks.onLivingDeath(this, source)) {
            callback.cancel();
        }
    }
}
