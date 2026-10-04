package nbtech2.fix.mixin.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.crafting.CraftingTreeNode;
import nbtech2.fix.util.AEHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 规划期：让 AE2 在为某个输入寻找替代品时，接受「同物品、仅 NBT 不同」的变体。
 * <p>
 * 目标是 {@code CraftingTreeNode} 中承载模糊查找过滤器的合成方法
 * {@code lambda$findCraftedStack$0(AEKey)}。该 lambda 内只有这一个
 * {@code IInput.isValid} 调用点，因此用方法名精确定位（{@code require = 1}）。
 * <p>
 * 注意：Mixin 0.8.5 的选择器不会递归进 lambda，必须直接写合成方法名。
 * <p>
 * <b>@Redirect 的 handler 签名规则</b>（踩过坑，别再改错）：
 * 参数 = 被重定向调用的实参，按出现顺序；随后是 lambda 的捕获变量；最后是返回类型。
 * <b>不能</b>追加 {@code CallbackInfoReturnable} —— 那是 {@code @Inject} 专用的。
 */
@Mixin(value = CraftingTreeNode.class, remap = false)
public abstract class AECraftingTreeNodeMixin {
    /** 仅供 AEMixinVerifier 判定本 Mixin 是否应用；@Unique 避免与他人冲突。 */
    @Unique
    private void nbtech2fix$markApplied() {
    }

    @Redirect(
            method = "lambda$findCraftedStack$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/crafting/IPatternDetails$IInput;isValid(Lappeng/api/stacks/AEKey;Lnet/minecraft/world/level/Level;)Z",
                    remap = false
            ),
            remap = false
    )
    private boolean nbtech2fix$nbtAwareIsValid(
            IPatternDetails.IInput input,
            AEKey candidate,
            Level level,
            AEKey capturedCandidate
    ) {
        return AEHelper.isValidInput(input, candidate, level);
    }
}
