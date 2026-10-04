package nbtech2.fix.mixin.ae2;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AECraftingPattern;
import nbtech2.fix.util.AEHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 普通合成样板：让「同物品、仅 NBT 不同」的变体通过材料校验。
 * <p>
 * 这个方法同时是两条路径的入口：
 * <ul>
 *   <li>{@code AECraftingPattern$Input.isValid} 在开启 substitution 时转调它（规划 / 提取期）；</li>
 *   <li>{@code AECraftingPattern.assemble} 在装配室内再次调用它，失败会返回
 *       {@link net.minecraft.world.item.ItemStack#EMPTY}，导致分子装配室永远不出货（任务卡死）。</li>
 * </ul>
 * 因此只补这一处即可同时修好「合成无法开始」与「合成永久卡住」。
 * <p>
 * 采用 HEAD 注入 + 前置守卫：只在能证明「该槽位的声明输入与候选仅 NBT 不同」时返回 true，
 * 其余情况一律放行给原版逻辑，绝不把原版的 true 改成 false。
 */
@Mixin(value = AECraftingPattern.class, remap = false)
public abstract class AECraftingPatternMixin {
    /** 仅供 AEMixinVerifier 判定本 Mixin 是否应用；@Unique 避免与他人冲突。 */
    @Unique
    private void nbtech2fix$markApplied() {
    }

    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true, remap = false)
    private void nbtech2fix$allowNbtVariant(
            int slot,
            AEItemKey key,
            Level level,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (key == null || !key.hasTag() || !AEHelper.isNbtPolicyActive()) {
            return;
        }

        GenericStack[] declared = ((AECraftingPattern) (Object) this).getSparseInputs();
        if (declared == null || slot < 0 || slot >= declared.length) {
            return;
        }

        if (AEHelper.isNbtEquivalentInput(declared[slot], key)) {
            cir.setReturnValue(true);
        }
    }
}
