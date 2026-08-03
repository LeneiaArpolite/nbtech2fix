package nbtech2.fix.mixin.neoecoae;

import appeng.api.stacks.KeyCounter;
import cn.dancingsnow.neoecoae.api.me.ExecutingCraftingJob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ExecutingCraftingJob.class, remap = false)
public interface ECOExecutingCraftingJobAccessor {
    @Accessor("inFlightOutputs")
    KeyCounter nbtech2fix$getInFlightOutputs();
}
