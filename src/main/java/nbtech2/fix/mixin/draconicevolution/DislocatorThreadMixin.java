package nbtech2.fix.mixin.draconicevolution;

import com.brandon3055.brandonscore.lib.DelayedTask;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.brandon3055.brandonscore.utils.TargetPos;
import com.brandon3055.draconicevolution.handlers.DESounds;
import nbtech2.fix.config.Config;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.brandon3055.draconicevolution.items.tools.Dislocator", remap = false)
public abstract class DislocatorThreadMixin {
    @Redirect(
            method = "dislocateEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/brandonscore/lib/DelayedTask;run(ILjava/lang/Runnable;)V",
                    remap = false
            ),
            remap = false
    )
    private void nbtech2fix$sendExitSoundOnServerThread(
            int delay,
            Runnable originalTask,
            ItemStack stack,
            Entity user,
            Entity target,
            TargetPos targetPos
    ) {
        if (!Config.FIX_DE_DISLOCATOR_THREAD_BUG.get()) {
            DelayedTask.run(delay, originalTask);
            return;
        }

        float pitch = target.level().random.nextFloat() * 0.1F + 0.9F;
        BCoreNetwork.sendSound(
                target.level(),
                target.blockPosition(),
                (SoundEvent) DESounds.PORTAL.get(),
                SoundSource.PLAYERS,
                0.1F,
                pitch,
                false
        );
    }
}
