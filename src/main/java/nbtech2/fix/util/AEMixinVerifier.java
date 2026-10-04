package nbtech2.fix.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 运行时自检：确认 AE2 合成相关的 Mixin 是否真的注入成功。
 * <p>
 * Mixin 在注入点匹配不到目标时，默认只打一条容易淹没在日志里的警告，
 * 因此提供这个主动检查。
 * <p>
 * 判定方式：每个 Mixin 都带一个 {@code @Unique} 的
 * {@code nbtech2fix$markApplied()} 标记方法，Mixin 应用时会把标记方法
 * <b>连同注入方法一起</b>合并进目标类。只要标记方法在，就说明这个 Mixin 真的应用了。
 * <p>
 * 这里刻意<b>不</b>依赖注入方法名（它会随注入器类型变化），也把标记方法与
 * 注入逻辑解耦（标记方法即使注入点匹配失败也依然存在，只是不再被调用）。
 * <p>
 * 副带效果：检查会<b>强制加载</b>这些 AE2 目标类，因此注入失败会在这里立刻暴露。
 */
public final class AEMixinVerifier {
    /** 每个 Mixin 合并进目标类的唯一标记方法名 */
    private static final String MARKER = "nbtech2fix$markApplied";

    private record Target(String label, String className) {
    }

    private static final List<Target> TARGETS = List.of(
            new Target("加工样板输入", "appeng.crafting.pattern.AEProcessingPattern$Input"),
            new Target("加工样板推送", "appeng.crafting.pattern.AEProcessingPattern"),
            new Target("合成 CPU 回流", "appeng.crafting.execution.CraftingCpuLogic"),
            new Target("规划期模糊过滤", "appeng.crafting.CraftingTreeNode"),
            new Target("提取期模板过滤", "appeng.crafting.execution.CraftingCpuHelper"),
            new Target("普通合成样板", "appeng.crafting.pattern.AECraftingPattern"),
            new Target("锻造台样板", "appeng.crafting.pattern.AESmithingTablePattern"),
            new Target("切石机样板", "appeng.crafting.pattern.AEStonecuttingPattern")
    );

    private AEMixinVerifier() {
    }

    public record Result(String label, boolean injected, String detail) {
    }

    /**
     * 逐个加载目标类并检查标记方法是否存在。
     */
    public static List<Result> verify() {
        List<Result> results = new ArrayList<>(TARGETS.size());

        for (Target target : TARGETS) {
            try {
                Class<?> clazz = Class.forName(target.className(), false, AEMixinVerifier.class.getClassLoader());
                if (hasMarker(clazz)) {
                    results.add(new Result(target.label(), true, "已应用"));
                } else {
                    results.add(new Result(target.label(), false, "未找到标记方法，Mixin 未应用"));
                }
            } catch (Throwable throwable) {
                results.add(new Result(target.label(), false,
                        "加载失败: " + throwable.getClass().getSimpleName()));
            }
        }

        return List.copyOf(results);
    }

    private static boolean hasMarker(Class<?> clazz) {
        for (var method : clazz.getDeclaredMethods()) {
            if (MARKER.equals(method.getName())) {
                return true;
            }
        }
        return false;
    }
}
