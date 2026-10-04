package nbtech2.fix.mixin.ae2;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEStonecuttingPattern;
import nbtech2.fix.util.AEHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 切石机样板。结构与锻造台样板完全同构：{@code assemble} 只看原版配方判定，
 * 失败返回 {@link ItemStack#EMPTY}，且不调用 {@code isItemValid}。
 * <p>
 * 处理方式与 {@link AESmithingTablePatternMixin} 相同：用样板编码的 {@code input}
 * 放回容器重跑同一个原版配方。产物仍由原版配方产生。
 */
@Mixin(value = AEStonecuttingPattern.class, remap = false)
public abstract class AEStonecuttingPatternMixin {
    /** 与 AEStonecuttingPattern.CRAFTING_GRID_SLOT 对应 */
    private static final int nbtech2fix$GRID_INPUT = 4;

    private static final int[] nbtech2fix$GRID_SLOTS = { nbtech2fix$GRID_INPUT };

    @Shadow
    private StonecutterRecipe recipe;

    @Shadow
    private AEItemKey input;

    /** 仅供 AEMixinVerifier 判定本 Mixin 是否应用；@Unique 避免与他人冲突。 */
    @Unique
    private void nbtech2fix$markApplied() {
    }

    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true, remap = false)
    private void nbtech2fix$rematchWithDeclaredStack(
            Container container,
            Level level,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (cir.getReturnValue() != null && !cir.getReturnValue().isEmpty()) {
            return;
        }

        GenericStack[] declared = { input == null ? null : new GenericStack(input, 1) };

        if (!AEHelper.hasNbtVariantInput(declared, nbtech2fix$GRID_SLOTS, container)) {
            return;
        }

        ItemStack rematched = AEHelper.rematchWithDeclaredStacks(
                (rematchContainer, registryAccess) -> recipe.matches(rematchContainer, level)
                        ? recipe.assemble(rematchContainer, registryAccess)
                        : ItemStack.EMPTY,
                nbtech2fix$GRID_INPUT + 1,
                nbtech2fix$GRID_SLOTS,
                declared,
                level
        );

        if (!rematched.isEmpty()) {
            cir.setReturnValue(rematched);
        }
    }
}
