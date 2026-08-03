package nbtech2.fix.Mixin.PkgAuto.Fix_PackagedDE_Doesnt_Use_Input;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import nbtech2.fix.Config.Config;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.integration.appeng.blockentity.AEPackagingProviderBlockEntity;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = AEPackagingProviderBlockEntity.class, remap = false)
public class MixinAEPackagingProviderBlockEntity {

    @Redirect(
            method = "pushPattern",
            at = @At(value = "INVOKE", target = "Lthelm/packagedauto/api/IPackageCraftingMachine;acceptPackage(Lthelm/packagedauto/api/IPackageRecipeInfo;Ljava/util/List;Lnet/minecraft/core/Direction;Z)Z")
    )
    private boolean passActualAEStacks(IPackageCraftingMachine machine, IPackageRecipeInfo recipe, List<ItemStack> originalList, Direction dir, boolean blocking, IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        List<ItemStack> stacksToPass = originalList;

        if (Config.Fix_PackagedDE_Doesnt_Use_Input.getAsBoolean() && inputHolder != null) {
            List<ItemStack> actualInputs = new ArrayList<>();
            for (KeyCounter kc : inputHolder) {
                if (kc != null) {
                    for (var entry : kc) {
                        if (entry.getKey() instanceof AEItemKey itemKey) {
                            actualInputs.add(itemKey.toStack((int) entry.getLongValue()));
                        }
                    }
                }
            }
            if (!actualInputs.isEmpty()) {
                stacksToPass = actualInputs;
            }
        }

        return machine.acceptPackage(recipe, stacksToPass, dir, blocking);
    }
}
