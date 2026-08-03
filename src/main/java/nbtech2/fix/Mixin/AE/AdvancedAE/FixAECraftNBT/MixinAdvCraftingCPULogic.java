package nbtech2.fix.Mixin.AE.AdvancedAE.FixAECraftNBT;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.inv.ListCraftingInventory;
import nbtech2.fix.Util.AEHelper;
import net.pedroksl.advanced_ae.common.logic.AdvCraftingCPULogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AdvCraftingCPULogic.class, remap = false)
public abstract class MixinAdvCraftingCPULogic {
    @Redirect(
            method = "insert",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/crafting/inv/ListCraftingInventory;extract(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;)J"
            )
    )
    private long nbtech2fix$extractMatchingWaitingKey(
            ListCraftingInventory inventory,
            AEKey actual,
            long amount,
            Actionable action
    ) {
        return AEHelper.extractMatching(inventory, actual, amount, action);
    }

    @Redirect(
            method = "insert",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/stacks/AEKey;matches(Lappeng/api/stacks/GenericStack;)Z"
            )
    )
    private boolean nbtech2fix$matchFinalOutput(AEKey actual, GenericStack expected) {
        return AEHelper.matchesFinalOutput(actual, expected);
    }
}
