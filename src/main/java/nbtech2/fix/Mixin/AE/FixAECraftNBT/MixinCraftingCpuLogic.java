package nbtech2.fix.Mixin.AE.FixAECraftNBT;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.inv.ListCraftingInventory;
import nbtech2.fix.Util.AEHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CraftingCpuLogic.class, remap = false)
public abstract class MixinCraftingCpuLogic {
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
