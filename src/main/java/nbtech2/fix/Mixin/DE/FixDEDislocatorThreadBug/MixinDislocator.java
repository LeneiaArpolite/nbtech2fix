package nbtech2.fix.Mixin.DE.FixDEDislocatorThreadBug;

import nbtech2.fix.Config.Config;
import com.brandon3055.brandonscore.lib.DelayedTask;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.brandon3055.brandonscore.utils.TargetPos;
import com.brandon3055.draconicevolution.handlers.DESounds;
import com.brandon3055.draconicevolution.items.tools.Dislocator;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Dislocator.class)
public class MixinDislocator {
    @Redirect(
            method = "dislocateEntity",
            at = @At(value = "INVOKE", target = "Lcom/brandon3055/brandonscore/lib/DelayedTask;run(ILjava/lang/Runnable;)V", remap = false),
            remap = false
    )
    private void fixThreadLocalRandomAccess(
            int delay, Runnable runnable,
            ItemStack stack, Entity user, Entity target, TargetPos targetPos
    ) {

        if (!Config.Fix_DE_Dislocator_Thread_Bug.getAsBoolean()) {
            DelayedTask.run(delay, runnable);
            return;
        }

        float pitch = target.level().random.nextFloat() * 0.1F + 0.9F;
        BCoreNetwork.sendSound(
                target.level(),
                target.blockPosition(),
                DESounds.PORTAL.get(),
                SoundSource.PLAYERS,
                0.1F,
                pitch,
                false
        );
    }
}
