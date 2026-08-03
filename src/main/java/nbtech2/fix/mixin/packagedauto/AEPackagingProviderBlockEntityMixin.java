package nbtech2.fix.mixin.packagedauto;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import nbtech2.fix.config.Config;
import nbtech2.fix.util.AEHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.integration.appeng.blockentity.AEPackagingProviderBlockEntity;

import java.util.List;

@Mixin(value = AEPackagingProviderBlockEntity.class, remap = false)
public abstract class AEPackagingProviderBlockEntityMixin {
    @Redirect(
            method = "pushPattern",
            at = @At(
                    value = "INVOKE",
                    target = "Lthelm/packagedauto/api/IPackageCraftingMachine;acceptPackage(Lthelm/packagedauto/api/IPackageRecipeInfo;Ljava/util/List;Lnet/minecraft/core/Direction;Z)Z"
            )
    )
    private boolean nbtech2fix$passActualAeStacks(
            IPackageCraftingMachine machine,
            IPackageRecipeInfo recipe,
            List<ItemStack> templateInputs,
            Direction direction,
            boolean blocking,
            IPatternDetails patternDetails,
            KeyCounter[] inputHolder
    ) {
        List<ItemStack> inputs = Config.FIX_PACKAGED_DE_REAL_INPUT.get()
                ? AEHelper.getRealInputs(inputHolder, templateInputs)
                : templateInputs;
        return machine.acceptPackage(recipe, inputs, direction, blocking);
    }
}
