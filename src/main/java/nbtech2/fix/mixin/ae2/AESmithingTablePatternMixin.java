package nbtech2.fix.mixin.ae2;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AESmithingTablePattern;
import nbtech2.fix.util.AEHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 锻造台样板：盔甲 / 工具升级路径。
 * <p>
 * 与普通合成样板不同，{@code AESmithingTablePattern.assemble} <b>只</b>调用原版
 * {@code SmithingRecipe.matches} 判定，失败直接返回 {@link ItemStack#EMPTY}，
 * 全程不经过 {@code isItemValid}。因此仅仅放宽 {@code isItemValid} 无法避免装配室卡死。
 * <p>
 * 修法：当产物为空、且网格里确实存在「与样板声明输入仅 NBT 不同」的变体时，
 * 用样板自己编码的 {@code template / base / addition} 放回容器重跑同一个原版配方。
 * 产物仍由原版配方产生，本类不构造任何产物。
 */
@Mixin(value = AESmithingTablePattern.class, remap = false)
public abstract class AESmithingTablePatternMixin {
    /** 与 AESmithingTablePattern.TEMPLATE_CRAFTING_GRID_SLOT 对应 */
    private static final int nbtech2fix$GRID_TEMPLATE = 3;
    /** 与 AESmithingTablePattern.BASE_CRAFTING_GRID_SLOT 对应 */
    private static final int nbtech2fix$GRID_BASE = 4;
    /** 与 AESmithingTablePattern.ADDITION_CRAFTING_GRID_SLOT 对应 */
    private static final int nbtech2fix$GRID_ADDITION = 5;

    private static final int[] nbtech2fix$GRID_SLOTS =
            { nbtech2fix$GRID_TEMPLATE, nbtech2fix$GRID_BASE, nbtech2fix$GRID_ADDITION };

    @Shadow
    private SmithingRecipe recipe;

    @Shadow
    private AEItemKey template;

    @Shadow
    private AEItemKey base;

    @Shadow
    private AEItemKey addition;

    /** 仅供 AEMixinVerifier 判定本 Mixin 是否应用；@Unique 避免与他人冲突。 */
    @Unique
    private void nbtech2fix$markApplied() {
    }

    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true, remap = false)
    private void nbtech2fix$rematchWithDeclaredStacks(
            Container container,
            Level level,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (cir.getReturnValue() != null && !cir.getReturnValue().isEmpty()) {
            return;
        }

        GenericStack[] declared = {
                template == null ? null : new GenericStack(template, 1),
                base == null ? null : new GenericStack(base, 1),
                addition == null ? null : new GenericStack(addition, 1)
        };

        if (!AEHelper.hasNbtVariantInput(declared, nbtech2fix$GRID_SLOTS, container)) {
            return;
        }

        ItemStack rematched = AEHelper.rematchWithDeclaredStacks(
                (rematchContainer, registryAccess) -> recipe.matches(rematchContainer, level)
                        ? recipe.assemble(rematchContainer, registryAccess)
                        : ItemStack.EMPTY,
                nbtech2fix$GRID_ADDITION + 1,
                nbtech2fix$GRID_SLOTS,
                declared,
                level
        );

        if (!rematched.isEmpty()) {
            cir.setReturnValue(rematched);
        }
    }
}
