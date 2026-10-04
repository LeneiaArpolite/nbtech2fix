package nbtech2.fix.mixin.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.crafting.execution.CraftingCpuHelper;
import appeng.crafting.execution.InputTemplate;
import nbtech2.fix.util.AEHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 提取期：CPU 真正从网络里挑输入模板时，同样接受「同物品、仅 NBT 不同」的变体。
 * <p>
 * 目标是 {@code CraftingCpuHelper} 中过滤模板的合成方法
 * {@code lambda$getValidItemTemplates$0(IInput, Level, InputTemplate)}。
 * 静态 lambda，因此处理器必须是 {@code private static}。
 * <p>
 * <b>@Redirect 的 handler 签名规则</b>（踩过坑，别再改错）：
 * 参数 = 被重定向调用的实参，按出现顺序；随后是 lambda 的捕获变量；最后是返回类型。
 * 本例中实参为 {@code (input, key(), level)}，捕获变量为 {@code (input, level)}。
 * <b>不能</b>追加 {@code CallbackInfoReturnable} —— 那是 {@code @Inject} 专用的。
 */
@Mixin(value = CraftingCpuHelper.class, remap = false)
public abstract class AECraftingCpuHelperMixin {
    /** 仅供 AEMixinVerifier 判定本 Mixin 是否应用；@Unique 避免与他人冲突。 */
    @Unique
    private void nbtech2fix$markApplied() {
    }

    @Redirect(
            method = "lambda$getValidItemTemplates$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/crafting/IPatternDetails$IInput;isValid(Lappeng/api/stacks/AEKey;Lnet/minecraft/world/level/Level;)Z",
                    remap = false
            ),
            remap = false
    )
    @SuppressWarnings("unused")
    private static boolean nbtech2fix$nbtAwareIsValid(
            IPatternDetails.IInput input,
            AEKey candidate,
            Level level,
            IPatternDetails.IInput capturedInput,
            Level capturedLevel,
            InputTemplate capturedTemplate
    ) {
        return AEHelper.isValidInput(input, candidate, level);
    }
}
