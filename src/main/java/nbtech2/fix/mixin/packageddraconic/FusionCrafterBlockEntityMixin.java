package nbtech2.fix.mixin.packageddraconic;

import nbtech2.fix.config.Config;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.inventory.BaseItemHandler;
import thelm.packageddraconic.block.entity.FusionCrafterBlockEntity;
import thelm.packageddraconic.block.entity.MarkedInjectorBlockEntity;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = FusionCrafterBlockEntity.class, remap = false)
public abstract class FusionCrafterBlockEntityMixin {
    @Unique
    private List<ItemStack> nbtech2fix$actualInputs;

    @Inject(method = "acceptPackage", at = @At("HEAD"))
    private void nbtech2fix$captureActualInputs(
            IPackageRecipeInfo recipeInfo,
            List<ItemStack> stacks,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!Config.FIX_PACKAGED_DE_REAL_INPUT.get() || stacks == null) {
            nbtech2fix$actualInputs = null;
            return;
        }

        nbtech2fix$actualInputs = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            nbtech2fix$actualInputs.add(stack.copy());
        }
    }

    @Redirect(
            method = "acceptPackage",
            at = @At(
                    value = "INVOKE",
                    target = "Lthelm/packagedauto/inventory/BaseItemHandler;setStackInSlot(ILnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private void nbtech2fix$useActualCoreInput(BaseItemHandler handler, int slot, ItemStack recipeStack) {
        handler.setStackInSlot(slot, nbtech2fix$extractActualStack(recipeStack));
    }

    @Redirect(
            method = "acceptPackage",
            at = @At(
                    value = "INVOKE",
                    target = "Lthelm/packageddraconic/block/entity/MarkedInjectorBlockEntity;setInjectorStack(Lnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private void nbtech2fix$useActualInjectorInput(
            MarkedInjectorBlockEntity injector,
            ItemStack recipeStack
    ) {
        injector.setInjectorStack(nbtech2fix$extractActualStack(recipeStack));
    }

    @Inject(method = "acceptPackage", at = @At("RETURN"))
    private void nbtech2fix$clearActualInputs(CallbackInfoReturnable<Boolean> cir) {
        nbtech2fix$actualInputs = null;
    }

    @Unique
    private ItemStack nbtech2fix$extractActualStack(ItemStack recipeStack) {
        if (recipeStack.isEmpty()
                || nbtech2fix$actualInputs == null
                || nbtech2fix$actualInputs.isEmpty()) {
            return recipeStack.copy();
        }

        for (int index = 0; index < nbtech2fix$actualInputs.size(); index++) {
            ItemStack candidate = nbtech2fix$actualInputs.get(index);
            if (!ItemStack.isSameItem(candidate, recipeStack)) {
                continue;
            }

            ItemStack result = candidate.split(Math.min(candidate.getCount(), recipeStack.getCount()));
            if (candidate.isEmpty()) {
                nbtech2fix$actualInputs.remove(index);
            }
            return result;
        }
        return recipeStack.copy();
    }
}
