package nbtech2.fix.Mixin.PkgAuto.PDE.FixPackagedDEDoesntUseInput;

import nbtech2.fix.Config.Config;
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

import java.util.List;
import thelm.packageddraconic.block.entity.MarkedInjectorBlockEntity;
import java.util.ArrayList;

@Mixin(value = FusionCrafterBlockEntity.class, remap = false)
public class MixinFusionCrafterBlockEntity {


    @Unique
    private List<ItemStack> pauto$capturedStacks;

    @Inject(method = "acceptPackage", at = @At("HEAD"))
    private void captureStacks(IPackageRecipeInfo recipeInfo, List<ItemStack> stacks, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (Config.Fix_PackagedDE_Doesnt_Use_Input.getAsBoolean() && stacks != null) {
            this.pauto$capturedStacks = new ArrayList<>();
            for (ItemStack s : stacks) {
                this.pauto$capturedStacks.add(s.copy());
            }
        } else {
            this.pauto$capturedStacks = null;
        }
    }

    @Unique
    private ItemStack pauto$extractActualStack(ItemStack recipeStack) {
        if (pauto$capturedStacks == null || pauto$capturedStacks.isEmpty() || recipeStack.isEmpty()) {
            return recipeStack.copy();
        }

        for (int i = 0; i < pauto$capturedStacks.size(); i++) {
            ItemStack candidate = pauto$capturedStacks.get(i);
            if (ItemStack.isSameItem(candidate, recipeStack)) {
                int amountToTake = Math.min(candidate.getCount(), recipeStack.getCount());
                ItemStack result = candidate.split(amountToTake);

                if (candidate.isEmpty()) {
                    pauto$capturedStacks.remove(i);
                }
                return result;
            }
        }
        return recipeStack.copy();
    }

    @Redirect(
            method = "acceptPackage",
            at = @At(value = "INVOKE", target = "Lthelm/packagedauto/inventory/BaseItemHandler;setStackInSlot(ILnet/minecraft/world/item/ItemStack;)V")
    )
    private void redirectCoreInput(BaseItemHandler handler, int slot, ItemStack recipeStack) {
        if (recipeStack.isEmpty()) {
            handler.setStackInSlot(slot, ItemStack.EMPTY);
            return;
        }
        handler.setStackInSlot(slot, pauto$extractActualStack(recipeStack));
    }

    @Redirect(
            method = "acceptPackage",
            at = @At(value = "INVOKE", target = "Lthelm/packageddraconic/block/entity/MarkedInjectorBlockEntity;setInjectorStack(Lnet/minecraft/world/item/ItemStack;)V")
    )
    private void redirectInjectorInput(MarkedInjectorBlockEntity injector, ItemStack recipeStack) {
        if (recipeStack.isEmpty()) {
            injector.setInjectorStack(ItemStack.EMPTY);
            return;
        }
        injector.setInjectorStack(pauto$extractActualStack(recipeStack));
    }

    @Inject(method = "acceptPackage", at = @At("RETURN"))
    private void clearCapturedStacks(CallbackInfoReturnable<Boolean> cir) {
        this.pauto$capturedStacks = null;
    }
}
